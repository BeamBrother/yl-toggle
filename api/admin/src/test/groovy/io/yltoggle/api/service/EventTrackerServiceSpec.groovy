package io.yltoggle.api.service


import io.yltoggle.api.config.AppConfig
import io.yltoggle.api.dao.entity.DebugEvent
import io.yltoggle.api.dao.entity.Environment
import io.yltoggle.api.dao.entity.EventTracker
import io.yltoggle.api.dao.entity.Traffic
import io.yltoggle.api.dao.repository.DebugEventRepository
import io.yltoggle.api.dao.repository.DictionaryRepository
import io.yltoggle.api.dao.repository.EnvironmentRepository
import io.yltoggle.api.dao.repository.EventTrackerRepository
import io.yltoggle.api.dao.repository.PublishMessageRepository
import io.yltoggle.api.dao.repository.TrafficRepository
import io.yltoggle.api.dto.EventTrackerStatusRequest
import org.hibernate.internal.SessionImpl
import spock.lang.Specification

import jakarta.persistence.EntityManager

class EventTrackerServiceSpec extends Specification {


    EnvironmentRepository environmentRepository

    EventTrackerRepository eventTrackerRepository

    AppConfig appConfig

    AnalysisServerService analysisServerService

    DebugEventRepository debugEventRepository

    TrafficRepository trafficRepository

    PublishMessageRepository publishMessageRepository

    DictionaryRepository dictionaryRepository

    ChangeLogService changeLogService

    EventTrackerService eventTrackerService

    EntityManager entityManager

    def projectKey
    def environmentKey

    def setup() {
        environmentRepository = Mock(EnvironmentRepository)
        eventTrackerRepository = Mock(EventTrackerRepository)
        appConfig = new AppConfig(analysisBaseUrl: "http://127.0.0.1:4006")
        analysisServerService = new AnalysisServerService(appConfig)
        debugEventRepository = Mock(DebugEventRepository)
        trafficRepository = Mock(TrafficRepository)
        publishMessageRepository = Mock(PublishMessageRepository)
        dictionaryRepository = Mock(DictionaryRepository)
        changeLogService = new ChangeLogService(publishMessageRepository, environmentRepository, dictionaryRepository)
        entityManager = Mock(SessionImpl)
        eventTrackerService = new EventTrackerService(environmentRepository, eventTrackerRepository,
                analysisServerService, debugEventRepository, trafficRepository, changeLogService, entityManager)
        projectKey = "test_project"
        environmentKey = "online"
    }

    def "Get event stream : tracker is close"() {
        when:
        def events = eventTrackerService.getEventStream(projectKey, environmentKey, "dccb93e9-4a9c-459a-ae32-2ce267c6ed13")
        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >> Optional.of(new Environment(debuggerUntilTime: 1482393612836))
        !events.debuggerEnabled
        events.events.isEmpty()
    }

    def "Get event stream : tracker is open"() {
        given:
        def uuid = "dccb93e9-4a9c-459a-ae32-2ce267c6ed13"
        def server_sdk = "server-123"
        def time = System.currentTimeMillis()
        when:
        def events = eventTrackerService.getEventStream(projectKey, environmentKey, uuid)
        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >> Optional.of(new Environment(debuggerUntilTime: System.currentTimeMillis() + 1000 * 60 * 30, serverSdkKey: server_sdk))
        1 * eventTrackerRepository
                .findByProjectKeyAndEnvironmentKeyAndUuid(projectKey, environmentKey, uuid) >> Optional.of(new EventTracker(time: time))
        1 * debugEventRepository
                .findAllBySdkKeyAndTimeGreaterThanEqual(server_sdk, time) >> [new DebugEvent(time: System.currentTimeMillis())]
        1 * trafficRepository
                .findAllBySdkKeyAndStartDateGreaterThanEqual(server_sdk, new Date(time)) >> [new Traffic(startDate: new Date())]
        1 * eventTrackerRepository.save(_)
        events.debuggerEnabled
        2 == events.events.size()
    }

    def "Get event stream : tracker is open & first call"() {
        given:
        def uuid = "dccb93e9-4a9c-459a-ae32-2ce267c6ed13"
        def server_sdk = "server-123"
        def time = System.currentTimeMillis()
        when:
        def events = eventTrackerService.getEventStream(projectKey, environmentKey, uuid)
        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >> Optional.of(new Environment(debuggerUntilTime: System.currentTimeMillis() + 1000 * 60 * 30, serverSdkKey: server_sdk))
        1 * eventTrackerRepository
                .findByProjectKeyAndEnvironmentKeyAndUuid(projectKey, environmentKey, uuid) >> Optional.empty()
        1 * eventTrackerRepository.save(_)
        events.debuggerEnabled
        events.events.isEmpty()
    }

    def "Update event tracker status"() {
        given:
        def server_sdk = "server-123"
        EventTrackerStatusRequest statusRequest = new EventTrackerStatusRequest(enabled: true)
        when:
        eventTrackerService.status(projectKey, environmentKey, statusRequest)
        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >> Optional.of(new Environment(version: 1))
        1 * environmentRepository.save(_)
        1 * environmentRepository.save(_)
        1 * publishMessageRepository.save(_)
    }

}
