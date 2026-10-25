mod handler;

use axum::{
    extract::{Extension, Query},
    http::StatusCode,
    response::{Html, IntoResponse, Response},
    routing::{get, post},
    Json, Router,
};
use axum_extra::TypedHeader;
use headers::HeaderName;
use reqwest::header;

use crate::{events_buffer::EventsStats, YTServerError};
#[allow(unused_imports)]
pub use handler::{FpHttpHandler, HttpHandler, LocalFileHttpHandlerForTest};
use serde::Deserialize;
use serde_json::json;
use std::collections::HashMap;
use std::net::SocketAddr;
use yl_toggle_event::collector::{post_events, EventHandler};
use yl_toggle_server_sdk::SdkAuthorization;
#[cfg(feature = "unstable")]
use yl_toggle_server_sdk::Segment;
use yl_toggle_server_sdk::Toggle;

pub async fn serve_http<T>(port: u16, handler: T)
where
    T: HttpHandler + EventHandler + Clone + Send + Sync + 'static,
{
    let app = Router::new()
        .route("/", get(root_handler))
        .route(
            "/api/client-sdk/toggles",
            get(client_sdk_toggles::<T>).options(client_cors),
        )
        .route(
            "/api/client-sdk/events",
            get(client_sdk_events::<T>).options(client_cors),
        )
        .route("/api/server-sdk/toggles", get(server_sdk_toggles::<T>))
        .route("/api/events", post(post_events::<T>).options(client_cors))
        .route("/internal/all_secrets", get(all_secrets::<T>)) // not for public network
        .route("/internal/events/stats", get(events_stats::<T>))
        .route("/internal/update_toggles", post(update_toggles::<T>))
        .layer(Extension(handler))
        .fallback(handler_404);

    #[cfg(feature = "unstable")]
    let app = app
        .route("/internal/server/segments", post(update_segments::<T>))
        .route("/intelnal/server/check_secrets", post(check_secrets::<T>));

    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}

async fn client_cors() -> Response {
    (StatusCode::OK, cors_headers()).into_response()
}

async fn client_sdk_toggles<T>(
    params: Query<ClientParams>,
    sdk_key: TypedHeader<SdkAuthorization>,
    Extension(handler): Extension<T>,
) -> Result<Response, YTServerError>
where
    T: HttpHandler + Clone + Send + Sync + 'static,
{
    handler.client_sdk_toggles(params.0, sdk_key.0 .0).await
}

async fn client_sdk_events<T>(
    sdk_key: TypedHeader<SdkAuthorization>,
    Extension(handler): Extension<T>,
) -> Result<Response, YTServerError>
where
    T: HttpHandler + Clone + Send + Sync + 'static,
{
    handler.client_sdk_events(sdk_key.0 .0).await
}

async fn server_sdk_toggles<T>(
    sdk_key: TypedHeader<SdkAuthorization>,
    Extension(handler): Extension<T>,
) -> Result<Response, YTServerError>
where
    T: HttpHandler + Clone + Send + Sync + 'static,
{
    handler.server_sdk_toggles(sdk_key.0 .0).await
}

async fn update_toggles<T>(
    Extension(handler): Extension<T>,
    params: Json<ToggleUpdateParams>,
) -> Result<Response, YTServerError>
where
    T: HttpHandler + Clone + Send + Sync + 'static,
{
    handler.update_toggles(params.0).await
}

#[cfg(feature = "unstable")]
async fn update_segments<T>(
    Extension(handler): Extension<T>,
    params: Json<SegmentUpdateParams>,
) -> Result<Response, YTServerError>
where
    T: HttpHandler + Clone + Send + Sync + 'static,
{
    handler.update_segments(params.0).await
}

#[cfg(feature = "unstable")]
async fn check_secrets<T>(
    Extension(handler): Extension<T>,
    params: Json<SecretsParams>,
) -> Result<Json<HashMap<String, String>>, YTServerError>
where
    T: HttpHandler + Clone + Send + Sync + 'static,
{
    handler.check_secrets(params.0).await
}

async fn events_stats<T>(
    Extension(handler): Extension<T>,
) -> Result<Json<EventsStats>, YTServerError>
where
    T: HttpHandler + Clone + Send + Sync + 'static,
{
    handler.events_stats().await
}

async fn all_secrets<T>(
    Extension(handler): Extension<T>,
) -> Result<Json<HashMap<String, String>>, YTServerError>
where
    T: HttpHandler + Clone + Send + Sync + 'static,
{
    handler.all_secrets().await
}

async fn root_handler() -> Html<&'static str> {
    Html("<h1>yl-toggle Server</h1>")
}

async fn handler_404() -> impl IntoResponse {
    (StatusCode::NOT_FOUND, "")
}

#[derive(Debug, Deserialize)]
pub struct ClientParams {
    user: String,
}

#[allow(unused)]
#[derive(Debug, Deserialize)]
pub struct ToggleUpdateParams {
    sdk_key: String,
    #[serde(default)]
    toggles: HashMap<String, Toggle>,
    #[serde(default)]
    version: Option<String>,
}

#[cfg(feature = "unstable")]
#[derive(Debug, Deserialize)]
pub struct SegmentUpdateParams {
    segments: HashMap<String, Segment>,
}

#[cfg(feature = "unstable")]
#[derive(Debug, Deserialize)]
pub struct SecretsParams {
    _secrets: HashMap<String, String>,
}

impl IntoResponse for YTServerError {
    fn into_response(self) -> Response {
        let (status, error_message) = match self {
            YTServerError::NotFound(_) => (StatusCode::NOT_FOUND, self.to_string()),
            YTServerError::UserDecodeError => (StatusCode::BAD_REQUEST, self.to_string()),
            _ => (StatusCode::INTERNAL_SERVER_ERROR, self.to_string()),
        };

        let body = Json(json!({
            "error": error_message,
        }));

        (status, cors_headers(), body).into_response()
    }
}

pub fn cors_headers() -> [(HeaderName, &'static str); 4] {
    [
        (header::CONTENT_TYPE, "application/json"),
        (header::ACCESS_CONTROL_ALLOW_HEADERS, "*"),
        (header::ACCESS_CONTROL_ALLOW_ORIGIN, "*"),
        (
            header::ACCESS_CONTROL_ALLOW_METHODS,
            "GET, POST, PUT, DELETE, OPTIONS",
        ),
    ]
}

#[cfg(test)]
mod tests {
    use super::{handler::LocalFileHttpHandlerForTest, *};
    #[cfg(feature = "realtime")]
    use crate::realtime::RealtimeSocket;
    use crate::{base::ServerConfig, repo::SdkRepository};
    use base64::{engine::general_purpose::STANDARD, Engine as _};
    use reqwest::header::CONTENT_TYPE;
    use reqwest::{header::AUTHORIZATION, Client, Error, Method, Url};
    use serde_json::Value;
    use std::{fs, path::PathBuf, sync::Arc, time::Duration};
    use yl_toggle_server_sdk::{Repository, SdkAuthorization, YTDetail, YTUser};

    #[test]
    fn deserialize_toggle_udpate_param() {
        let toggle_update_param = r#"{"sdk_key": "key1"}"#;
        let p: ToggleUpdateParams = serde_json::from_str(toggle_update_param).unwrap();
        assert_eq!(p.sdk_key, "key1");
    }

    #[tokio::test]
    async fn test_fp_server_connect_fp_api() {
        let server_sdk_key = "server-sdk-key1".to_owned();
        let client_sdk_key = "client-sdk-key1".to_owned();
        let mock_api_port = 9002;
        let fp_server_port = 9003;
        setup_mock_api(mock_api_port).await;
        let repo = setup_fp_server(
            mock_api_port,
            fp_server_port,
            &client_sdk_key,
            &server_sdk_key,
        )
        .await;
        tokio::time::sleep(Duration::from_millis(100)).await; // wait fp server port listen
        let repo_string = repo.server_sdk_repo_string(&server_sdk_key);
        assert!(repo_string.is_ok());

        let resp = http_get(
            format!("http://127.0.0.1:{}/api/server-sdk/toggles", fp_server_port),
            Some(server_sdk_key.clone()),
        )
        .await;
        assert!(resp.is_ok());
        let body = resp.unwrap().text().await;
        assert!(body.is_ok());
        let body = body.unwrap();
        let r = serde_json::from_str::<Repository>(&body).unwrap();
        assert_eq!(r, repo_from_test_file());

        let resp = http_get(
            format!("http://127.0.0.1:{}/api/server-sdk/toggles", fp_server_port),
            Some("no_exist_server_key".to_owned()),
        )
        .await;
        assert!(resp.is_ok());
        let body = resp.unwrap().text().await;
        assert!(body.is_ok());
        let body = body.unwrap();
        let r = serde_json::from_str::<Repository>(&body).unwrap();
        assert_eq!(r, Repository::default());

        let resp = http_get(
            format!("http://127.0.0.1:{}/internal/all_secrets", fp_server_port),
            None,
        )
        .await;
        assert!(resp.is_ok());

        let resp = http_get(
            format!("http://127.0.0.1:{}/internal/events/stats", fp_server_port),
            None,
        )
        .await;
        assert!(resp.is_ok());
        let resp = resp.unwrap();
        assert!(resp.status().is_success());

        let resp = http_get(
            format!("http://127.0.0.1:{}/internal/all_secrets", mock_api_port),
            None,
        )
        .await;
        assert!(resp.is_ok());
    }

    #[tokio::test]
    async fn test_server_sdk_toggles() {
        let port = 9004;
        let handler = LocalFileHttpHandlerForTest::default();
        tokio::spawn(crate::http::serve_http::<LocalFileHttpHandlerForTest>(
            port, handler,
        ));
        tokio::time::sleep(Duration::from_millis(100)).await; // wait port listen

        let resp = http_get(
            format!("http://127.0.0.1:{}/api/server-sdk/toggles", port),
            Some("sdk-key".to_owned()),
        )
        .await;
        assert!(resp.is_ok(), "response invalid");
        let body = resp.unwrap().text().await;
        assert!(body.is_ok(), "response body error");
        let body = body.unwrap();
        let r = serde_json::from_str::<Repository>(&body).unwrap();
        assert_eq!(r, repo_from_test_file());
    }

    #[tokio::test]
    async fn test_client_sdk_toggles() {
        let server_sdk_key = "server-sdk-key1".to_owned();
        let client_sdk_key = "client-sdk-key1".to_owned();
        let mock_api_port = 9005;
        let fp_server_port = 9006;
        setup_mock_api(mock_api_port).await;
        let _ = setup_fp_server(
            mock_api_port,
            fp_server_port,
            &client_sdk_key,
            &server_sdk_key,
        )
        .await;
        tokio::time::sleep(Duration::from_millis(100)).await; // wait fp server port listen

        let user = YTUser::new().with("city", "1");
        let user_json = serde_json::to_string(&user).unwrap();
        let user_base64 = STANDARD.encode(&user_json);
        let resp = http_get(
            format!(
                "http://127.0.0.1:{}/api/client-sdk/toggles?user={}",
                fp_server_port, user_base64
            ),
            Some(client_sdk_key.clone()),
        )
        .await;
        assert!(resp.is_ok(), "response invalid");
        let text = resp.unwrap().text().await;
        assert!(text.is_ok(), "response text error");
        let text = text.unwrap();
        let toggles = serde_json::from_str::<HashMap<String, YTDetail<Value>>>(&text);
        assert!(toggles.is_ok(), "response can not deserialize");
        let toggles = toggles.unwrap();
        let t = toggles.get("bool_toggle");
        assert!(t.is_some(), "toggle not found");
        let t = t.unwrap();
        assert_eq!(t.rule_index, Some(0));
        assert_eq!(t.value.as_bool(), Some(true));

        // for test coverage
        let resp = http_get(
            format!(
                "http://127.0.0.1:{}/api/client-sdk/toggles?user={}",
                mock_api_port, user_base64
            ),
            Some(client_sdk_key.clone()),
        )
        .await;
        assert!(resp.is_ok())
    }

    #[tokio::test]
    async fn test_events() {
        let server_sdk_key = "server-sdk-key1".to_owned();
        let client_sdk_key = "client-sdk-key1".to_owned();
        let mock_api_port = 9007;
        let fp_server_port = 9008;
        setup_mock_api(mock_api_port).await;
        let _ = setup_fp_server(
            mock_api_port,
            fp_server_port,
            &client_sdk_key,
            &server_sdk_key,
        )
        .await;
        tokio::time::sleep(Duration::from_millis(100)).await; // wait fp server port listen

        let resp = http_post(
            format!("http://127.0.0.1:{}/api/events", fp_server_port),
            Some(client_sdk_key.clone()),
            "[]".to_owned(),
        )
        .await;
        assert!(resp.is_ok());

        let resp = http_post(
            format!("http://127.0.0.1:{}/api/events", mock_api_port),
            Some(client_sdk_key.clone()),
            "[]".to_owned(),
        )
        .await;
        assert!(resp.is_ok());
    }

    async fn http_get(url: String, sdk_key: Option<String>) -> Result<reqwest::Response, Error> {
        let url = Url::parse(&url).unwrap();
        let timeout = Duration::from_secs(1);
        let mut request = Client::new().request(Method::GET, url);

        if let Some(sdk_key) = sdk_key {
            let auth = SdkAuthorization(sdk_key).encode();
            request = request.header(AUTHORIZATION, auth);
        }
        request = request.timeout(timeout);
        request.send().await
    }

    async fn http_post(
        url: String,
        sdk_key: Option<String>,
        body: String,
    ) -> Result<reqwest::Response, Error> {
        let url = Url::parse(&url).unwrap();
        let timeout = Duration::from_secs(1);
        let mut request = Client::new()
            .request(Method::POST, url)
            .header(CONTENT_TYPE, "application/json")
            .header("user-agent", "Rust/0.0.0")
            .header("ua", "Rust/0.0.0")
            .body(body);

        if let Some(sdk_key) = sdk_key {
            let auth = SdkAuthorization(sdk_key).encode();
            request = request.header(AUTHORIZATION, auth);
        }
        request = request.timeout(timeout);
        request.send().await
    }

    fn repo_from_test_file() -> Repository {
        let mut path = PathBuf::from(env!("CARGO_MANIFEST_DIR"));
        path.push("resources/fixtures/repo.json");
        let json_str = fs::read_to_string(path).unwrap();
        serde_json::from_str::<Repository>(&json_str).unwrap()
    }

    async fn setup_mock_api(port: u16) {
        let mock_yl_toggle_api = LocalFileHttpHandlerForTest::default();
        tokio::spawn(crate::http::serve_http::<LocalFileHttpHandlerForTest>(
            port,
            mock_yl_toggle_api,
        ));
        tokio::time::sleep(Duration::from_millis(100)).await;
    }

    async fn setup_fp_server(
        target_port: u16,
        listen_port: u16,
        client_sdk_key: &str,
        server_sdk_key: &str,
    ) -> Arc<SdkRepository> {
        let toggles_url = Url::parse(&format!(
            "http://127.0.0.1:{}/api/server-sdk/toggles",
            target_port
        ))
        .unwrap();
        let events_url =
            Url::parse(&format!("http://127.0.0.1:{}/api/events", target_port)).unwrap();
        let analysis_url = None;
        let config = ServerConfig {
            toggles_url,
            events_url: events_url.clone(),
            refresh_interval: Duration::from_secs(1),
            analysis_url: None,
            keys_url: None,
            client_sdk_key: Some(client_sdk_key.to_owned()),
            server_sdk_key: Some(server_sdk_key.to_owned()),
            server_port: listen_port,
            #[cfg(feature = "realtime")]
            realtime_port: listen_port + 100,
            #[cfg(feature = "realtime")]
            realtime_path: "/server/realtime".to_owned(),
            prerequisite_deep: 20,
        };

        #[cfg(feature = "realtime")]
        let rs = RealtimeSocket::serve(config.realtime_port, &config.realtime_path);

        let repo = SdkRepository::new(
            config,
            #[cfg(feature = "realtime")]
            rs,
        );
        repo.sync(client_sdk_key.to_owned(), server_sdk_key.to_owned(), 1);
        let repo = Arc::new(repo);
        let yl_toggle_server = FpHttpHandler {
            repo: repo.clone(),
            prerequisite_deep: 20,
            events_buffer: Arc::new(crate::events_buffer::EventsBuffer::new(
                events_url,
                analysis_url,
                Duration::from_secs(10),
                Default::default(),
                crate::events_buffer::EventsBufferConfig::default(),
            )),
        };
        tokio::spawn(crate::http::serve_http::<FpHttpHandler>(
            listen_port,
            yl_toggle_server,
        ));
        repo
    }
}
