package io.yltoggle.api.base.model;

import io.yltoggle.api.base.hook.Action;
import io.yltoggle.api.base.hook.Resource;
import lombok.Data;

@Data
public class HookContext {

    private Long organizationId;

    private Resource resource;

    private String projectKey;

    private String environmentKey;

    private String toggleKey;

    private String segmentKey;

    private Long id;

    private Action action;

    private String operator;

    private Long timestamp;

    private Object request;

    private Object response;

    public HookContext(Resource resource, Action action) {
        this.resource = resource;
        this.action = action;
        this.timestamp = System.currentTimeMillis();
    }

}
