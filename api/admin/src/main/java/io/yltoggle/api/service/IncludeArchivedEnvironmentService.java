package io.yltoggle.api.service;

import io.yltoggle.api.dao.exception.ResourceConflictException;
import io.yltoggle.api.dao.repository.EnvironmentRepository;
import io.yltoggle.api.base.db.Archived;
import io.yltoggle.api.base.enums.ResourceType;
import io.yltoggle.api.base.enums.ValidateTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@AllArgsConstructor
@Service
public class IncludeArchivedEnvironmentService {

    private EnvironmentRepository environmentRepository;

    @PersistenceContext
    public EntityManager entityManager;

    @Archived
    public void validateIncludeArchivedEnvironment(String projectKey, ValidateTypeEnum type, String value) {
        switch (type) {
            case KEY:
                validateIncludeArchivedEnvironmentByKey(projectKey, value);
                break;
            case NAME:
                validateIncludeArchivedEnvironmentByName(projectKey, value);
                break;
            default:
                break;
        }
    }

    @Archived
    public void validateIncludeArchivedEnvironmentByName(String projectKey, String name) {
        if (environmentRepository.existsByProjectKeyAndName(projectKey, name)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }

    @Archived
    public void validateIncludeArchivedEnvironmentByKey(String projectKey, String key) {
        if (environmentRepository.existsByProjectKeyAndKey(projectKey, key)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }
}
