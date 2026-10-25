package io.yltoggle.api.dao.exception;

import io.yltoggle.api.base.enums.ResourceType;

public class ResourceOverflowException extends RuntimeException {

    public ResourceType resourceType;

    public ResourceOverflowException(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

}
