use std::collections::VecDeque;
use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::Arc;
use std::time::Duration;

use axum::http::HeaderMap;
use parking_lot::Mutex;
use reqwest::{Client, Method, Url};
use serde::Serialize;
use serde_json::Value;
use tracing::{info, warn};

/// Tunables for the outbound event buffer. Defaults favour small latency
/// (200ms flush) over maximum batching; raise `flush_interval` to trade
/// latency for fewer upstream requests.
#[derive(Debug, Clone)]
pub struct EventsBufferConfig {
    /// Maximum number of events kept in memory; oldest are dropped on overflow.
    pub capacity: usize,
    /// Maximum events forwarded in a single upstream POST.
    pub max_batch: usize,
    /// Delay between flush attempts.
    pub flush_interval: Duration,
    /// Attempts per upstream POST (1 initial + retries).
    pub retries: u32,
    /// Backoff base for retries, doubled per attempt.
    pub retry_base_delay: Duration,
}

impl Default for EventsBufferConfig {
    fn default() -> Self {
        Self {
            capacity: 100_000,
            max_batch: 500,
            flush_interval: Duration::from_millis(200),
            retries: 3,
            retry_base_delay: Duration::from_millis(200),
        }
    }
}

impl EventsBufferConfig {
    pub fn from_env() -> Self {
        let mut config = Self::default();
        if let Ok(v) = std::env::var("YT_EVENTS_BUFFER_CAPACITY") {
            if let Ok(n) = v.parse::<usize>() {
                config.capacity = n;
            }
        }
        if let Ok(v) = std::env::var("YT_EVENTS_MAX_BATCH") {
            if let Ok(n) = v.parse::<usize>() {
                config.max_batch = n;
            }
        }
        if let Ok(v) = std::env::var("YT_EVENTS_FLUSH_MS") {
            if let Ok(n) = v.parse::<u64>() {
                config.flush_interval = Duration::from_millis(n);
            }
        }
        if let Ok(v) = std::env::var("YT_EVENTS_RETRIES") {
            if let Ok(n) = v.parse::<u32>() {
                config.retries = n;
            }
        }
        if let Ok(v) = std::env::var("YT_EVENTS_RETRY_BASE_MS") {
            if let Ok(n) = v.parse::<u64>() {
                config.retry_base_delay = Duration::from_millis(n);
            }
        }
        config.sanitize();
        config
    }

    fn sanitize(&mut self) {
        self.capacity = self.capacity.max(1);
        self.max_batch = self.max_batch.max(1);
        self.flush_interval = self.flush_interval.max(Duration::from_millis(1));
        // Ten attempts keep the backoff bounded even if a caller supplies a very large value.
        self.retries = self.retries.clamp(1, 10);
        self.retry_base_delay = self.retry_base_delay.max(Duration::from_millis(1));
    }
}

/// Snapshot for operational monitoring. `upstream_deliveries` counts delivered
/// payload entries, so fan-out to both ingestion services is counted per destination.
#[derive(Debug, Clone, Default, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct EventsStats {
    pub received: u64,
    pub upstream_deliveries: u64,
    pub dropped_overflow: u64,
    pub dropped_send: u64,
    pub queued: usize,
}

/// Bounded buffer between SDK-facing POST /api/events and the upstream
/// ingestion endpoints (backendAPI /internal/server/events and the analysis
/// API). Requests are acknowledged immediately and their events are flushed
/// in per-header-group batches with retry, instead of one fire-and-forget
/// HTTP task per request. On overflow or retry exhaustion events are dropped
/// and counted - never blocking the evaluation path.
pub struct EventsBuffer {
    inner: Arc<BufferInner>,
}

struct BufferInner {
    queue: Mutex<VecDeque<BufferedEvent>>,
    config: EventsBufferConfig,
    events_url: Url,
    analysis_url: Option<Url>,
    timeout: Duration,
    http_client: Arc<Client>,
    received: AtomicU64,
    sent: AtomicU64,
    dropped_overflow: AtomicU64,
    dropped_send: AtomicU64,
}

struct BufferedEvent {
    group_key: String,
    headers: HeaderMap,
    event: Value,
}

impl EventsBuffer {
    pub fn new(
        events_url: Url,
        analysis_url: Option<Url>,
        timeout: Duration,
        http_client: Arc<Client>,
        config: EventsBufferConfig,
    ) -> Self {
        let inner = Arc::new(BufferInner {
            queue: Mutex::new(VecDeque::new()),
            config,
            events_url,
            analysis_url,
            timeout,
            http_client,
            received: AtomicU64::new(0),
            sent: AtomicU64::new(0),
            dropped_overflow: AtomicU64::new(0),
            dropped_send: AtomicU64::new(0),
        });
        Self { inner }
    }

    /// Spawn the background flush loop. Must be called inside a tokio runtime.
    pub fn start(&self) {
        let inner = self.inner.clone();
        info!("events buffer started: {:?}", inner.config);
        tokio::spawn(async move {
            let mut interval = tokio::time::interval(inner.config.flush_interval);
            loop {
                interval.tick().await;
                flush_once(&inner).await;
            }
        });
    }

    /// Queue one request's events. `headers` must already carry the upstream
    /// Authorization / user-agent (see build_header). Events with identical
    /// headers are batched together on flush.
    pub fn push(&self, headers: HeaderMap, events: VecDeque<Value>) {
        let group_key = header_group_key(&headers);
        let mut queue = self.inner.queue.lock();
        self.inner
            .received
            .fetch_add(events.len() as u64, Ordering::Relaxed);
        for event in events {
            if queue.len() >= self.inner.config.capacity && queue.pop_front().is_some() {
                self.inner.dropped_overflow.fetch_add(1, Ordering::Relaxed);
            }
            queue.push_back(BufferedEvent {
                group_key: group_key.clone(),
                headers: headers.clone(),
                event,
            });
        }
    }

    pub fn stats(&self) -> EventsStats {
        let inner = &self.inner;
        EventsStats {
            received: inner.received.load(Ordering::Relaxed),
            upstream_deliveries: inner.sent.load(Ordering::Relaxed),
            dropped_overflow: inner.dropped_overflow.load(Ordering::Relaxed),
            dropped_send: inner.dropped_send.load(Ordering::Relaxed),
            queued: inner.queue.lock().len(),
        }
    }
}

fn header_group_key(headers: &HeaderMap) -> String {
    let auth = headers
        .get(reqwest::header::AUTHORIZATION)
        .and_then(|v| v.to_str().ok())
        .unwrap_or("");
    let ua = headers
        .get(reqwest::header::USER_AGENT)
        .and_then(|v| v.to_str().ok())
        .unwrap_or("");
    let js_ua = headers
        .get("ua")
        .and_then(|v| v.to_str().ok())
        .unwrap_or("");
    format!("{}\u{1}{}\u{1}{}", auth, ua, js_ua)
}

async fn flush_once(inner: &BufferInner) {
    let drained: Vec<BufferedEvent> = {
        let mut queue = inner.queue.lock();
        queue.drain(..).collect()
    };
    if drained.is_empty() {
        return;
    }
    let batches = group_and_chunk(drained, inner.config.max_batch);
    for (headers, events) in batches {
        let body = match serde_json::to_string(&events) {
            Ok(b) => b,
            Err(e) => {
                warn!(
                    "events buffer serialize error, dropped {} events: {}",
                    events.len(),
                    e
                );
                inner
                    .dropped_send
                    .fetch_add(events.len() as u64, Ordering::Relaxed);
                continue;
            }
        };
        if let Some(analysis_url) = &inner.analysis_url {
            // SDKs submit backendAPI repository envelopes ({events, access}),
            // while the Analysis API accepts envelopes containing only events.
            // Preserve either shape by flattening nested event arrays.
            let analysis_events: Vec<Value> = events
                .iter()
                .flat_map(|item| match item.get("events").and_then(Value::as_array) {
                    Some(nested) => nested.clone(),
                    None => Vec::from([item.clone()]),
                })
                .collect();
            let analysis_body = match serde_json::to_string(&serde_json::json!([
                { "events": analysis_events }
            ])) {
                Ok(body) => body,
                Err(e) => {
                    warn!(
                        "events buffer analysis serialize error, dropped {} events: {}",
                        analysis_events.len(),
                        e
                    );
                    inner
                        .dropped_send
                        .fetch_add(analysis_events.len() as u64, Ordering::Relaxed);
                    post_with_retry(inner, &inner.events_url, &headers, &body, events.len()).await;
                    continue;
                }
            };
            post_with_retry(
                inner,
                analysis_url,
                &headers,
                &analysis_body,
                analysis_events.len(),
            )
            .await;
        }
        post_with_retry(inner, &inner.events_url, &headers, &body, events.len()).await;
    }
    let sent = inner.sent.load(Ordering::Relaxed);
    let dropped_overflow = inner.dropped_overflow.load(Ordering::Relaxed);
    let dropped_send = inner.dropped_send.load(Ordering::Relaxed);
    if dropped_overflow > 0 || dropped_send > 0 {
        warn!(
            "events buffer: sent {}, dropped (overflow {}, send-failure {}), queued {}",
            sent,
            dropped_overflow,
            dropped_send,
            inner.queue.lock().len()
        );
    }
}

/// Group drained events by their header set (Authorization + user agents) and
/// split each group into chunks of at most `max_batch` events, preserving
/// arrival order within a group.
fn group_and_chunk(drained: Vec<BufferedEvent>, max_batch: usize) -> Vec<(HeaderMap, Vec<Value>)> {
    let max_batch = max_batch.max(1);
    let mut groups: Vec<(String, HeaderMap, Vec<Value>)> = Vec::new();
    let mut index: std::collections::HashMap<String, usize> = std::collections::HashMap::new();
    for event in drained {
        let group = match index.get(&event.group_key) {
            Some(&i) => &mut groups[i],
            None => {
                index.insert(event.group_key.clone(), groups.len());
                groups.push((event.group_key.clone(), event.headers.clone(), Vec::new()));
                groups.last_mut().expect("just pushed")
            }
        };
        group.2.push(event.event);
    }
    groups
        .into_iter()
        .flat_map(|(_, headers, events)| {
            events
                .chunks(max_batch)
                .map(|c| (headers.clone(), c.to_vec()))
                .collect::<Vec<_>>()
        })
        .collect()
}

async fn post_with_retry(
    inner: &BufferInner,
    url: &Url,
    headers: &HeaderMap,
    body: &str,
    count: usize,
) {
    let mut attempt: u32 = 0;
    loop {
        let request = inner
            .http_client
            .request(Method::POST, url.clone())
            .headers(headers.clone())
            .header(reqwest::header::CONTENT_TYPE, "application/json")
            .timeout(inner.timeout)
            .body(body.to_owned());
        match request.send().await {
            Ok(resp) if resp.status().is_success() => {
                inner.sent.fetch_add(count as u64, Ordering::Relaxed);
                return;
            }
            // A 4xx will not improve on retry; count as dropped.
            Ok(resp) if resp.status().is_client_error() => {
                warn!(
                    "events post to {} rejected with {}, dropped {} events",
                    url,
                    resp.status(),
                    count
                );
                inner
                    .dropped_send
                    .fetch_add(count as u64, Ordering::Relaxed);
                return;
            }
            Ok(resp) => {
                warn!(
                    "events post to {} got {}, attempt {}",
                    url,
                    resp.status(),
                    attempt + 1
                );
            }
            Err(e) => {
                warn!(
                    "events post to {} error: {}, attempt {}",
                    url,
                    e,
                    attempt + 1
                );
            }
        }
        attempt += 1;
        if attempt >= inner.config.retries {
            warn!(
                "events post to {} exhausted {} attempts, dropped {} events",
                url, attempt, count
            );
            inner
                .dropped_send
                .fetch_add(count as u64, Ordering::Relaxed);
            return;
        }
        tokio::time::sleep(inner.config.retry_base_delay * (1u32 << attempt)).await;
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::{routing::post, Router};
    use std::sync::atomic::AtomicUsize;

    fn test_headers(auth: &str) -> HeaderMap {
        let mut headers = HeaderMap::new();
        headers.insert(
            reqwest::header::AUTHORIZATION,
            auth.parse().expect("valid auth header"),
        );
        headers
    }

    #[test]
    fn test_config_sanitizes_invalid_values() {
        let mut config = EventsBufferConfig {
            capacity: 0,
            max_batch: 0,
            flush_interval: Duration::from_millis(0),
            retries: 0,
            retry_base_delay: Duration::from_millis(0),
        };
        config.sanitize();
        assert_eq!(config.capacity, 1);
        assert_eq!(config.max_batch, 1);
        assert_eq!(config.flush_interval, Duration::from_millis(1));
        assert_eq!(config.retries, 1);
        assert_eq!(config.retry_base_delay, Duration::from_millis(1));
    }

    #[test]
    fn test_capacity_drops_oldest() {
        let buffer = EventsBuffer::new(
            Url::parse("http://127.0.0.1:1/events").unwrap(),
            None,
            Duration::from_secs(1),
            Arc::new(Client::new()),
            EventsBufferConfig {
                capacity: 10,
                max_batch: 100,
                flush_interval: Duration::from_secs(3600),
                retries: 1,
                retry_base_delay: Duration::from_millis(1),
            },
        );
        let headers = test_headers("server-key-a");
        let events: VecDeque<Value> = (0..15).map(|i| serde_json::json!({"i": i})).collect();
        buffer.push(headers, events);
        let stats = buffer.stats();
        assert_eq!(stats.received, 15);
        assert_eq!(stats.dropped_overflow, 5);
        assert_eq!(stats.queued, 10);
        // oldest dropped: queue head must be event 5
        let queue = buffer.inner.queue.lock();
        assert_eq!(queue.front().unwrap().event["i"], 5);
    }

    #[test]
    fn test_group_and_chunk() {
        let drained: Vec<BufferedEvent> = vec![("a", 1), ("b", 10), ("a", 2), ("a", 3)]
            .into_iter()
            .map(|(group, i)| BufferedEvent {
                group_key: group.to_owned(),
                headers: test_headers(&format!("server-key-{}", group)),
                event: serde_json::json!({"i": i}),
            })
            .collect();
        let batches = group_and_chunk(drained, 2);
        assert_eq!(batches.len(), 3, "a[1,2] + a[3] + b[10]");
        assert_eq!(batches[0].1.len(), 2);
        assert_eq!(batches[0].1[0]["i"], 1);
        assert_eq!(batches[0].1[1]["i"], 2);
        assert_eq!(batches[1].1.len(), 1);
        assert_eq!(batches[1].1[0]["i"], 3);
        assert_eq!(batches[2].1.len(), 1);
        assert_eq!(batches[2].1[0]["i"], 10);
    }

    #[tokio::test]
    async fn test_flush_flattens_nested_events_for_analysis() {
        let backend_events = Arc::new(AtomicUsize::new(0));
        let analysis_events = Arc::new(AtomicUsize::new(0));

        let backend_state = backend_events.clone();
        let analysis_state = analysis_events.clone();
        let app = Router::new()
            .route(
                "/internal/server/events",
                post(move |body: String| async move {
                    let events: Vec<Value> = serde_json::from_str(&body).expect("backend array");
                    backend_state.fetch_add(events.len(), Ordering::SeqCst);
                    axum::http::StatusCode::OK
                }),
            )
            .route(
                "/events",
                post(move |body: String| async move {
                    let envelopes: Vec<Value> =
                        serde_json::from_str(&body).expect("analysis envelopes");
                    let count = envelopes
                        .iter()
                        .filter_map(|envelope| envelope.get("events"))
                        .filter_map(Value::as_array)
                        .map(Vec::len)
                        .sum::<usize>();
                    analysis_state.fetch_add(count, Ordering::SeqCst);
                    axum::http::StatusCode::OK
                }),
            );
        let listener_addr = std::net::SocketAddr::from(([127, 0, 0, 1], 9411));
        tokio::spawn(async move {
            let listener = tokio::net::TcpListener::bind(listener_addr).await.unwrap();
            axum::serve(listener, app).await.unwrap();
        });
        tokio::time::sleep(Duration::from_millis(100)).await;

        let buffer = EventsBuffer::new(
            Url::parse(&format!("http://{}/internal/server/events", listener_addr)).unwrap(),
            Some(Url::parse(&format!("http://{}/events", listener_addr)).unwrap()),
            Duration::from_secs(2),
            Arc::new(Client::new()),
            EventsBufferConfig {
                capacity: 100,
                max_batch: 100,
                flush_interval: Duration::from_secs(3600),
                retries: 1,
                retry_base_delay: Duration::from_millis(1),
            },
        );
        buffer.push(
            test_headers("server-key-nested"),
            vec![
                serde_json::json!({"events": [{"kind": "access"}, {"kind": "custom"}]}),
                serde_json::json!({"kind": "pageview"}),
            ]
            .into_iter()
            .collect(),
        );

        flush_once(&buffer.inner).await;

        assert_eq!(backend_events.load(Ordering::SeqCst), 2);
        assert_eq!(analysis_events.load(Ordering::SeqCst), 3);
        assert_eq!(buffer.stats().upstream_deliveries, 5);
    }

    #[tokio::test]
    async fn test_flush_posts_batches_and_retries() {
        let received_posts = Arc::new(AtomicUsize::new(0));
        let received_events = Arc::new(AtomicUsize::new(0));
        let seen_auth = Arc::new(Mutex::new(Vec::<String>::new()));

        let state = (
            received_posts.clone(),
            received_events.clone(),
            seen_auth.clone(),
        );
        let app = Router::new().route(
            "/internal/server/events",
            post(move |headers: HeaderMap, body: String| async move {
                let (posts, events, auths) = state.clone();
                let posts_now = posts.fetch_add(1, Ordering::SeqCst) + 1;
                // first two attempts fail with 500 to exercise retry
                if posts_now <= 2 {
                    return axum::http::StatusCode::INTERNAL_SERVER_ERROR;
                }
                let body_events: Vec<Value> = serde_json::from_str(&body).expect("json array body");
                events.fetch_add(body_events.len(), Ordering::SeqCst);
                if let Some(a) = headers.get(reqwest::header::AUTHORIZATION) {
                    auths.lock().push(a.to_str().unwrap_or("").to_owned());
                }
                axum::http::StatusCode::OK
            }),
        );
        let listener_addr = std::net::SocketAddr::from(([127, 0, 0, 1], 9410));
        tokio::spawn(async move {
            let listener = tokio::net::TcpListener::bind(listener_addr).await.unwrap();
            axum::serve(listener, app).await.unwrap();
        });
        tokio::time::sleep(Duration::from_millis(100)).await; // wait server listen

        let buffer = EventsBuffer::new(
            Url::parse(&format!("http://{}/internal/server/events", listener_addr)).unwrap(),
            None,
            Duration::from_secs(2),
            Arc::new(Client::new()),
            EventsBufferConfig {
                capacity: 1000,
                max_batch: 2,
                flush_interval: Duration::from_secs(3600),
                retries: 3,
                retry_base_delay: Duration::from_millis(10),
            },
        );
        buffer.push(
            test_headers("server-key-x"),
            (0..3).map(|i| serde_json::json!({"i": i})).collect(),
        );
        buffer.push(
            test_headers("server-key-y"),
            (10..12).map(|i| serde_json::json!({"i": i})).collect(),
        );

        flush_once(&buffer.inner).await;

        assert_eq!(
            received_events.load(Ordering::SeqCst),
            5,
            "all events delivered"
        );
        assert_eq!(seen_auth.lock().len(), 3, "x[0,1] + x[2] + y[10,11] posts");
        let stats = buffer.stats();
        assert_eq!(stats.received, 5);
        assert_eq!(stats.upstream_deliveries, 5);
        assert_eq!(stats.dropped_overflow, 0);
        assert_eq!(stats.dropped_send, 0);
        assert_eq!(stats.queued, 0);
    }
}
