package io.yltoggle.api.service;

import io.yltoggle.api.base.db.IncludeDeleted;
import io.yltoggle.api.dao.entity.Toggle;
import io.yltoggle.api.dao.exception.ResourceConflictException;
import io.yltoggle.api.dao.repository.ToggleRepository;
import io.yltoggle.api.base.db.Archived;
import io.yltoggle.api.base.enums.ResourceType;
import io.yltoggle.api.base.enums.ValidateTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@AllArgsConstructor
@Service
public class IncludeArchivedToggleService {

    private ToggleRepository toggleRepository;

    @PersistenceContext
    public EntityManager entityManager;


    @Archived
    public List<Toggle> findAllByProjectKey(String projectKey) {
        return toggleRepository.findAllByProjectKey(projectKey);
    }

    @Archived
    @IncludeDeleted
    public void validateIncludeArchivedToggle(String projectKey, ValidateTypeEnum type, String value) {
        switch (type) {
            case KEY:
                validateIncludeArchivedToggleByKey(projectKey, value);
                break;
            case NAME:
                validateIncludeArchivedToggleByName(projectKey, value);
                break;
            default:
                break;
        }
    }

    @Archived
    public void validateIncludeArchivedToggleByName(String projectKey, String name) {
        if (toggleRepository.existsByProjectKeyAndName(projectKey, name)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }

    @Archived
    public void validateIncludeArchivedToggleByKey(String projectKey, String key) {
        if (toggleRepository.existsByProjectKeyAndKey(projectKey, key)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }

}
