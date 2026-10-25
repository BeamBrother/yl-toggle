package io.yltoggle.api.service

import io.yltoggle.api.auth.PlaintextEncryptionService
import io.yltoggle.api.base.component.SpringBeanManager
import io.yltoggle.api.config.JWTConfig
import com.yltoggle.sdk.server.YlToggle
import io.yltoggle.api.base.enums.OrganizationRoleEnum
import io.yltoggle.api.dao.entity.Dictionary
import io.yltoggle.api.dao.entity.Environment
import io.yltoggle.api.dao.entity.Member
import io.yltoggle.api.dao.entity.Organization
import io.yltoggle.api.dao.entity.OrganizationMember
import io.yltoggle.api.dao.entity.Project
import io.yltoggle.api.dao.repository.DictionaryRepository
import io.yltoggle.api.dao.repository.EnvironmentRepository
import io.yltoggle.api.dao.repository.MemberRepository
import io.yltoggle.api.dao.repository.OrganizationMemberRepository
import io.yltoggle.api.dao.repository.OrganizationRepository
import io.yltoggle.api.dao.repository.ProjectRepository
import io.yltoggle.api.dao.repository.PublishMessageRepository
import io.yltoggle.api.dao.repository.TargetingSketchRepository
import org.hibernate.internal.SessionImpl
import org.hibernate.query.sql.spi.NativeQueryImplementor
import org.springframework.context.ApplicationContext
import spock.lang.Specification

import jakarta.persistence.EntityManager
import jakarta.persistence.Query

class GuestServiceSpec extends Specification {

    JWTConfig appConfig

    MemberRepository memberRepository

    MemberService memberService

    ProjectRepository projectRepository

    EnvironmentRepository environmentRepository

    TargetingSketchRepository targetingSketchRepository

    EntityManager entityManager

    MemberIncludeDeletedService memberIncludeDeletedService

    OrganizationRepository organizationRepository

    OrganizationMemberRepository organizationMemberRepository

    PublishMessageRepository publishMessageRepository

    DictionaryRepository dictionaryRepository

    ChangeLogService changeLogService

    ProjectService projectService

    GuestService guestService

    ApplicationContext applicationContext

    def setup() {
        appConfig = new JWTConfig()
        appConfig.setGuestDefaultPassword("Password")
        entityManager = Mock(SessionImpl)
        projectRepository = Mock(ProjectRepository)
        environmentRepository = Mock(EnvironmentRepository)
        organizationRepository = Mock(OrganizationRepository)
        targetingSketchRepository = Mock(TargetingSketchRepository)
        publishMessageRepository = Mock(PublishMessageRepository)
        dictionaryRepository = Mock(DictionaryRepository)
        memberRepository = Mock(MemberRepository)
        organizationMemberRepository = Mock(OrganizationMemberRepository)
        memberIncludeDeletedService = new MemberIncludeDeletedService(memberRepository, entityManager)
        memberService = new MemberService(memberRepository, memberIncludeDeletedService, organizationRepository, organizationMemberRepository, entityManager)
        changeLogService = new ChangeLogService(publishMessageRepository, environmentRepository, dictionaryRepository)
        projectService = new ProjectService(projectRepository, environmentRepository, targetingSketchRepository, changeLogService, entityManager)
        guestService = new GuestService(appConfig, memberService, organizationRepository, entityManager, projectService)
        applicationContext = Mock(ApplicationContext)
        SpringBeanManager.applicationContext = applicationContext
    }

    def "init guest data"() {
        given:
        Query query = Mock(NativeQueryImplementor)
        when:
        guestService.initGuest("Admin", "test")
        then:
        1 * memberRepository.save(_) >> new Member(id: 1, account: "Admin",
                organizationMembers: [new OrganizationMember(role: OrganizationRoleEnum.OWNER)])
        1 * organizationRepository.save(_) >> new Organization(name: "Admin")
        1 * projectRepository.count() >> 2
        applicationContext.getBean(_) >> new YlToggle("_")
        1 * projectRepository.save(_) >> new Project(name: "projectName", key: "projectKey",
                environments: [new Environment()])
        1 * dictionaryRepository.findByKey(_) >> Optional.of(new Dictionary(value: "1"))
        1 * dictionaryRepository.save(_)
        1 * publishMessageRepository.save(_)
        entityManager.createNativeQuery(_) >> query
        20 * query.executeUpdate()
    }

}
