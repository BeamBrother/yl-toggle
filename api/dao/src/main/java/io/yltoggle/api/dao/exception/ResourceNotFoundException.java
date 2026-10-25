package io.yltoggle.api.dao.exception;

import io.yltoggle.api.base.enums.ResourceType;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceType resourceType;
    public String resourceKey;

    public ResourceNotFoundException(ResourceType resourceType, String resourceKey) {
        this.resourceKey = resourceKey;
        this.resourceType = resourceType;
    }

}
