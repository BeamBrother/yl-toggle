package io.yltoggle.api.service;

import io.yltoggle.api.base.db.ExcludeTenant;
import io.yltoggle.api.base.enums.ResourceType;
import io.yltoggle.api.base.util.JsonMapper;
import io.yltoggle.api.component.TrafficIngestBuffer;
import io.yltoggle.api.dao.entity.DebugEvent;
import io.yltoggle.api.dao.entity.Environment;
import io.yltoggle.api.dao.entity.Traffic;
import io.yltoggle.api.dao.exception.ResourceNotFoundException;
import io.yltoggle.api.dao.repository.DebugEventRepository;
import io.yltoggle.api.dao.repository.EnvironmentRepository;
import io.yltoggle.api.dto.TrafficCreateRequest;
import io.yltoggle.api.dto.VariationAccessCounterRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@ExcludeTenant
public class TrafficService {

    private static final long ENVIRONMENT_CACHE_TTL_MS = 60_000L;

    private final static ExecutorService executorService = Executors.newFixedThreadPool(5);

    private EnvironmentRepository environmentRepository;

    private DebugEventRepository debugEventRepository;

    private TrafficIngestBuffer ingestBuffer;

    private final ConcurrentHashMap<String, CachedEnvironment> environmentCache = new ConcurrentHashMap<>();

    public void create(String serverSdkKey, String userAgent, List<TrafficCreateRequest> requests) {
        Environment environment = findEnvironment(serverSdkKey);

        List<Traffic> events = new ArrayList<>();
        requests.forEach(request -> {
            if (request.getAccess() == null) {
                return;
            }
            if (CollectionUtils.isNotEmpty(request.getEvents())) {
                DebugEventStorageTask debugEventStorageTask = new DebugEventStorageTask(serverSdkKey,
                        debugEventRepository, request.getEvents(), userAgent);
                executorService.submit(debugEventStorageTask);
            }
            events.addAll(assembleEvents(request, userAgent, environment));
        });

        if (events.isEmpty()) {
            return;
        }
        if (ingestBuffer.isAsyncEnabled()) {
            ingestBuffer.submit(events);
        } else {
            ingestBuffer.persist(events);
        }
    }

    private List<Traffic> assembleEvents(TrafficCreateRequest request, String userAgent, Environment environment) {
        return Optional.of(request.getAccess().getCounters())
                .orElse(Collections.emptyMap())
                .entrySet()
                .stream()
                .flatMap(entry -> createEventEntities(entry).stream())
                .map(event -> wrapEvent(event, userAgent, environment, request))
                .collect(Collectors.toList());
    }

    /**
     * Event ingestion is a hot path (every SDK poll posts here), so the
     * environment lookup is cached for a short TTL instead of hitting the
     * database per request. Environment keys change rarely; a stale entry
     * only mislabels events for at most TTL.
     */
    private Environment findEnvironment(String serverSdkKey) {
        AtomicReference<Environment> hit = new AtomicReference<>();
        environmentCache.compute(serverSdkKey, (key, cached) -> {
            if (cached != null && !cached.expired()) {
                hit.set(cached.environment);
                return cached;
            }
            Environment environment = environmentRepository.findByServerSdkKey(key)
                    .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ENVIRONMENT, key));
            hit.set(environment);
            return new CachedEnvironment(environment);
        });
        return hit.get();
    }

    private List<Traffic> createEventEntities(Map.Entry<String,
            List<VariationAccessCounterRequest>> toggleToAccessCounter) {
        String toggleKey = toggleToAccessCounter.getKey();

        return Optional.of(toggleToAccessCounter.getValue())
                .orElse(Collections.emptyList())
                .stream()
                .map(accessEvent -> this.createEventEntity(toggleKey, accessEvent))
                .collect(Collectors.toList());
    }

    private Traffic createEventEntity(String toggleKey, VariationAccessCounterRequest accessCounter) {
        Traffic event = new Traffic();
        event.setToggleKey(toggleKey);
        event.setCount(accessCounter.getCount());
        event.setValueIndex(accessCounter.getIndex());
        event.setToggleVersion(accessCounter.getVersion());
        event.setValue(JsonMapper.toJSONString(accessCounter.getValue()));
        return event;
    }

    private Traffic wrapEvent(Traffic event, String userAgent, Environment environment, TrafficCreateRequest request) {
        if (request.getAccess() == null) {
            return event;
        }
        event.setSdkKey(environment.getServerSdkKey());
        event.setProjectKey(environment.getProject().getKey());
        event.setEnvironmentKey(environment.getKey());
        event.setType("access");
        event.setSdkType(getSdkType(userAgent));
        event.setSdkVersion(getSdkVersion(userAgent));
        event.setStartDate(new Date(request.getAccess().getStartTime()));
        event.setEndDate(new Date(request.getAccess().getEndTime()));
        return event;
    }

    private String getSdkType(String userAgent) {
        return extractSdkField(userAgent, 0);
    }

    private String getSdkVersion(String userAgent) {
        return extractSdkField(userAgent, 1);
    }

    private String extractSdkField(String userAgent, int index) {
        if (StringUtils.isBlank(userAgent) || !userAgent.contains("/")) {
            log.error("[Event] SDK user-agent format error. {} ", userAgent);
            return "";
        }
        String[] parts = userAgent.split("/");
        return parts.length > index ? parts[index] : null;
    }

    class DebugEventStorageTask implements Runnable {


        private final String sdkKey;

        private final DebugEventRepository debugEventRepository;

        private final List<Map> events;

        private final String userAgent;

        public DebugEventStorageTask(String sdkKey, DebugEventRepository debugEventRepository, List<Map> events,
                                     String userAgent) {
            this.sdkKey = sdkKey;
            this.debugEventRepository = debugEventRepository;
            this.events = events;
            this.userAgent = userAgent;
        }

        @Override
        public void run() {
            if (Objects.nonNull(events) && CollectionUtils.isNotEmpty(events)) {
                List<DebugEvent> debugEvents = events.stream().filter(event -> "debug".equals(event.get("kind")))
                        .map(event -> buildDebugEvent(event, sdkKey, userAgent)).collect(Collectors.toList());
                debugEventRepository.saveAll(debugEvents);
            }
        }
    }

    private DebugEvent buildDebugEvent(Map event, String sdkKey, String userAgent) {
        DebugEvent debugEvent = new DebugEvent();
        debugEvent.setSdkKey(sdkKey);
        debugEvent.setKind(String.valueOf(event.get("kind")));
        debugEvent.setTime((Long) event.get("time"));
        debugEvent.setUserKey(String.valueOf(event.get("user")));
        debugEvent.setUserDetail(JsonMapper.toJSONString(event.get("userDetail")));
        debugEvent.setToggleKey(String.valueOf(event.get("key")));
        debugEvent.setValue(JsonMapper.toJSONString(event.get("value")));
        debugEvent.setVersion((Integer) event.get("version"));
        debugEvent.setVariationIndex((Integer) event.get("variationIndex"));
        debugEvent.setRuleIndex((Integer) event.get("ruleIndex"));
        debugEvent.setReason(String.valueOf(event.get("reason")));
        debugEvent.setSdkType(getSdkType(userAgent));
        debugEvent.setSdkVersion(getSdkVersion(userAgent));
        return debugEvent;
    }

    private static final class CachedEnvironment {
        private final Environment environment;

        private final long cachedAt = System.currentTimeMillis();

        private CachedEnvironment(Environment environment) {
            this.environment = environment;
        }

        private boolean expired() {
            return System.currentTimeMillis() - cachedAt >= ENVIRONMENT_CACHE_TTL_MS;
        }
    }
}
