package io.yltoggle.api.component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.yltoggle.api.dao.entity.Traffic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TrafficIngestBufferTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private TransactionTemplate transactionTemplate;

    private MeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    public void submitDropsWholeBatchWhenQueueIsFull() {
        TrafficIngestBuffer buffer = newBuffer(false, 1, 10);
        buffer.init();

        buffer.submit(newEvents("toggle-a", 1));
        buffer.submit(newEvents("toggle-b", 2));

        assertThat(meterRegistry.get("yl.toggle.event.ingest.queue.batches").gauge().value()).isEqualTo(1D);
        assertThat(meterRegistry.get("yl.toggle.event.ingest.dropped.batches").gauge().value()).isEqualTo(1D);
        assertThat(meterRegistry.get("yl.toggle.event.ingest.dropped.events").gauge().value()).isEqualTo(2D);
        buffer.shutdown();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void drainPersistsEventsAndDeduplicatesTrafficCacheArgs() {
        TrafficIngestBuffer buffer = newBuffer(false, 10, 10);
        buffer.init();
        List<Traffic> events = newEvents("toggle-a", 2);
        events.addAll(newEvents("toggle-a", 3));
        events.addAll(newEvents("toggle-b", 1));
        buffer.submit(events);

        int flushed = buffer.drainOnce();

        assertThat(flushed).isEqualTo(6);
        assertThat(meterRegistry.get("yl.toggle.event.ingest.flushed.events").gauge().value()).isEqualTo(6D);

        ArgumentCaptor<List<Object[]>> cacheCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(jdbcTemplate).batchUpdate(anyString(), cacheCaptor.capture());
        assertThat(cacheCaptor.getValue()).hasSize(2);
    }

    @Test
    public void failedDatabaseFlushIsRetriedInsteadOfDropped() throws Exception {
        TrafficIngestBuffer buffer = newBuffer(false, 10, 10);
        buffer.init();
        List<Traffic> events = newEvents("toggle-a", 3);
        buffer.submit(events);

        doThrow(new IllegalStateException("database unavailable")).when(transactionTemplate)
                .executeWithoutResult(any());
        assertThat(buffer.drainOnce()).isZero();
        assertThat(meterRegistry.get("yl.toggle.event.ingest.failed.flushes").gauge().value()).isEqualTo(1D);
        assertThat(meterRegistry.get("yl.toggle.event.ingest.retry.events").gauge().value()).isEqualTo(3D);

        executeTransactionCallback();
        Thread.sleep(20L);
        assertThat(buffer.drainOnce()).isEqualTo(3);
        assertThat(meterRegistry.get("yl.toggle.event.ingest.retry.events").gauge().value()).isZero();
        assertThat(meterRegistry.get("yl.toggle.event.ingest.flushed.events").gauge().value()).isEqualTo(3D);
    }

    @Test
    public void shutdownDrainsQueuedBatchesInSynchronousMode() {
        TrafficIngestBuffer buffer = newBuffer(false, 10, 10);
        buffer.init();
        buffer.submit(newEvents("toggle-a", 2));

        buffer.shutdown();

        assertThat(meterRegistry.get("yl.toggle.event.ingest.flushed.events").gauge().value()).isEqualTo(2D);
    }

    private void executeTransactionCallback() {
        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    private TrafficIngestBuffer newBuffer(boolean asyncEnabled, int capacity, int flushBatch) {
        return new TrafficIngestBuffer(jdbcTemplate, transactionTemplate, meterRegistry, asyncEnabled,
                capacity, 10, flushBatch);
    }

    private List<Traffic> newEvents(String toggleKey, int count) {
        List<Traffic> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Traffic event = new Traffic();
            event.setType("access");
            event.setSdkKey("server-sdk-key");
            event.setToggleKey(toggleKey);
            event.setEnvironmentKey("online");
            event.setProjectKey("project");
            event.setToggleVersion(1L);
            event.setSdkType("java");
            event.setSdkVersion("1.0.0");
            event.setValueIndex(i);
            event.setCount(1L);
            event.setValue("true");
            event.setStartDate(new Date());
            event.setEndDate(new Date());
            events.add(event);
        }
        return events;
    }
}
