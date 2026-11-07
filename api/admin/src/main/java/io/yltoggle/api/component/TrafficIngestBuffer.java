package io.yltoggle.api.component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.yltoggle.api.dao.entity.Traffic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bounded in-process buffer between /internal/server/events and MariaDB.
 * Batches are assembled by TrafficService and offered here; a single flusher
 * thread drains them into batched JDBC writes (traffic insert + traffic_cache
 * upsert) inside one short transaction per flush. A failed database write is
 * retained and retried on the next flush; when the queue is full the incoming
 * batch is dropped and counted.
 */
@Slf4j
@Component
public class TrafficIngestBuffer {

    private static final String INSERT_TRAFFIC_PREFIX =
            "insert into traffic (type, sdk_key, toggle_key, environment_key, project_key, "
                    + "toggle_version, sdk_version, sdk_type, value_index, count, value, "
                    + "start_date, end_date, created_time) values ";
    private static final String UPSERT_TRAFFIC_CACHE =
            "insert into traffic_cache (sdk_key, toggle_key, type, start_date, end_date) "
                    + "values (?, ?, 'EVALUATION', ?, ?) "
                    + "on duplicate key update start_date = values(start_date), end_date = values(end_date)";

    private final JdbcTemplate jdbcTemplate;

    private final TransactionTemplate transactionTemplate;

    private final MeterRegistry meterRegistry;

    private final boolean asyncEnabled;

    private final int bufferCapacity;

    private final long flushIntervalMs;

    private final int flushBatch;

    private ArrayBlockingQueue<List<Traffic>> queue;

    private Thread flusher;

    private volatile boolean running;

    private final AtomicReference<List<Traffic>> retryBatch = new AtomicReference<>();

    private volatile long retryNotBefore;

    private final AtomicLong droppedBatches = new AtomicLong();

    private final AtomicLong droppedEvents = new AtomicLong();

    private final AtomicLong flushedEvents = new AtomicLong();

    private final AtomicLong failedFlushes = new AtomicLong();

    public TrafficIngestBuffer(JdbcTemplate jdbcTemplate,
                               TransactionTemplate transactionTemplate,
                               MeterRegistry meterRegistry,
                               @Value("${app.event.async-enabled:true}") boolean asyncEnabled,
                               @Value("${app.event.buffer-capacity:20000}") int bufferCapacity,
                               @Value("${app.event.flush-interval-ms:500}") long flushIntervalMs,
                               @Value("${app.event.flush-batch:500}") int flushBatch) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.meterRegistry = meterRegistry;
        this.asyncEnabled = asyncEnabled;
        this.bufferCapacity = Math.max(1, bufferCapacity);
        this.flushIntervalMs = Math.max(1, flushIntervalMs);
        this.flushBatch = Math.max(1, flushBatch);
    }

    @PostConstruct
    public void init() {
        queue = new ArrayBlockingQueue<>(bufferCapacity);
        registerMetrics();
        if (!asyncEnabled) {
            log.info("TrafficIngestBuffer started in synchronous mode: flushBatch={}", flushBatch);
            return;
        }

        running = true;
        flusher = new Thread(this::flushLoop, "yl-toggle-Event-Flusher");
        flusher.setDaemon(true);
        flusher.start();
        log.info("TrafficIngestBuffer started: capacity={}, flushInterval={}ms, flushBatch={}",
                bufferCapacity, flushIntervalMs, flushBatch);
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        if (flusher != null) {
            try {
                flusher.join(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        List<Traffic> pending = retryBatch.getAndSet(null);
        if (pending != null && !pending.isEmpty()) {
            persistQuietly(pending);
        }

        List<List<Traffic>> rest = new ArrayList<>();
        queue.drainTo(rest);
        rest.forEach(this::persistQuietly);
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public void submit(List<Traffic> events) {
        if (events.isEmpty()) {
            return;
        }
        if (!queue.offer(events)) {
            droppedBatches.incrementAndGet();
            droppedEvents.addAndGet(events.size());
            log.warn("Event buffer full, dropped batch of {} events (total dropped {})", events.size(),
                    droppedEvents.get());
        }
    }

    /**
     * Batched write used by both the flusher and the synchronous fallback path
     * (app.event.async-enabled=false).
     */
    public void persist(List<Traffic> events) {
        if (events.isEmpty()) {
            return;
        }
        Date now = new Date();
        transactionTemplate.executeWithoutResult(status -> {
            insertTraffic(events, now);
            jdbcTemplate.batchUpdate(UPSERT_TRAFFIC_CACHE, cacheArgs(events));
        });
        flushedEvents.addAndGet(events.size());
    }

    private void insertTraffic(List<Traffic> events, Date now) {
        String placeholders = String.join(", ",
                Collections.nCopies(events.size(), "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"));
        Object[] args = new Object[events.size() * 14];
        int index = 0;
        for (Traffic event : events) {
            args[index++] = event.getType();
            args[index++] = event.getSdkKey();
            args[index++] = event.getToggleKey();
            args[index++] = event.getEnvironmentKey();
            args[index++] = event.getProjectKey();
            args[index++] = event.getToggleVersion() == null ? 0L : event.getToggleVersion();
            args[index++] = event.getSdkVersion();
            args[index++] = event.getSdkType();
            args[index++] = event.getValueIndex() == null ? 0 : event.getValueIndex();
            args[index++] = event.getCount() == null ? 0L : event.getCount();
            args[index++] = event.getValue();
            args[index++] = toTimestamp(event.getStartDate());
            args[index++] = toTimestamp(event.getEndDate());
            args[index++] = toTimestamp(now);
        }
        jdbcTemplate.update(INSERT_TRAFFIC_PREFIX + placeholders, args);
    }

    int drainOnce() {
        List<Traffic> batch;
        try {
            batch = takeBatch(false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
        if (batch == null || batch.isEmpty()) {
            return 0;
        }
        try {
            persist(batch);
            return batch.size();
        } catch (Throwable t) {
            retryBatch.set(batch);
            failedFlushes.incrementAndGet();
            retryNotBefore = System.currentTimeMillis() + flushIntervalMs;
            log.error("Event flush failed, {} events retained for retry", batch.size(), t);
            return 0;
        }
    }

    private void flushLoop() {
        while (running) {
            try {
                List<Traffic> batch = takeBatch(true);
                if (batch == null || batch.isEmpty()) {
                    continue;
                }
                try {
                    persist(batch);
                } catch (Throwable t) {
                    retryBatch.set(batch);
                    failedFlushes.incrementAndGet();
                    retryNotBefore = System.currentTimeMillis() + flushIntervalMs;
                    log.error("Event flush failed, {} events retained for retry", batch.size(), t);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable t) {
                log.error("Unexpected event flush loop failure", t);
            }
        }
    }

    private List<Traffic> takeBatch(boolean wait) throws InterruptedException {
        List<Traffic> pending = retryBatch.get();
        if (pending != null && !pending.isEmpty()) {
            long delayMs = retryNotBefore - System.currentTimeMillis();
            if (delayMs > 0) {
                if (!wait) {
                    return null;
                }
                Thread.sleep(delayMs);
            }
            retryBatch.getAndSet(null);
            retryNotBefore = 0L;
            List<Traffic> merged = new ArrayList<>(Math.min(pending.size(), flushBatch));
            appendBounded(merged, pending);
            return merged;
        }

        List<Traffic> first = wait ? queue.poll(flushIntervalMs, TimeUnit.MILLISECONDS) : queue.poll();
        if (first == null) {
            return null;
        }
        List<Traffic> merged = new ArrayList<>(Math.min(first.size(), flushBatch));
        appendBounded(merged, first);

        while (merged.size() < flushBatch) {
            List<Traffic> next = queue.poll();
            if (next == null) {
                break;
            }
            appendBounded(merged, next);
        }
        return merged;
    }

    private void appendBounded(List<Traffic> target, List<Traffic> source) {
        int space = flushBatch - target.size();
        if (source.size() <= space) {
            target.addAll(source);
            return;
        }

        target.addAll(source.subList(0, space));
        retryBatch.set(new ArrayList<>(source.subList(space, source.size())));
        retryNotBefore = 0L;
    }

    private void persistQuietly(List<Traffic> events) {
        try {
            persist(events);
        } catch (Throwable t) {
            log.error("Event flush on shutdown failed, {} events lost", events.size(), t);
        }
    }

    private void registerMetrics() {
        Gauge.builder("yl.toggle.event.ingest.queue.batches", queue, Collection::size)
                .description("Number of queued event request batches")
                .register(meterRegistry);
        Gauge.builder("yl.toggle.event.ingest.retry.events", retryBatch,
                TrafficIngestBuffer::retryEventCount)
                .description("Number of events retained after a failed database flush")
                .register(meterRegistry);
        gauge("yl.toggle.event.ingest.dropped.batches", droppedBatches);
        gauge("yl.toggle.event.ingest.dropped.events", droppedEvents);
        gauge("yl.toggle.event.ingest.flushed.events", flushedEvents);
        gauge("yl.toggle.event.ingest.failed.flushes", failedFlushes);
    }

    private void gauge(String name, AtomicLong value) {
        Gauge.builder(name, value, AtomicLong::get).register(meterRegistry);
    }

    private static double retryEventCount(AtomicReference<List<Traffic>> batch) {
        List<Traffic> events = batch.get();
        return events == null ? 0D : events.size();
    }

    /**
     * One cache row per (sdk_key, toggle_key, type), last event wins.
     */
    private List<Object[]> cacheArgs(List<Traffic> events) {
        Map<String, Traffic> unique = new LinkedHashMap<>();
        events.forEach(event -> unique.put(cacheKey(event), event));
        List<Object[]> args = new ArrayList<>(unique.size());
        for (Traffic event : unique.values()) {
            args.add(new Object[]{event.getSdkKey(), event.getToggleKey(), toTimestamp(event.getStartDate()),
                    toTimestamp(event.getEndDate())});
        }
        return args;
    }

    private String cacheKey(Traffic event) {
        return event.getSdkKey() + "&" + event.getToggleKey() + "&" + event.getType();
    }

    private Timestamp toTimestamp(Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
