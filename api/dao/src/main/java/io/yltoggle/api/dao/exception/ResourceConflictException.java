package io.yltoggle.api.dao.exception;

import io.yltoggle.api.base.enums.ResourceType;

public class ResourceConflictException extends RuntimeException {
    ResourceType resourceType;

    public ResourceConflictException(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}
