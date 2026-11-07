package io.yltoggle.api.dto;

import io.yltoggle.api.base.hook.Action;
import io.yltoggle.api.base.hook.Resource;
import lombok.Data;

@Data
public class WebHookCallbackResponse {

    private Long organizationId;

    private Resource resource;

    private Action action;

    private String operator;

    private Long date;

    private Object data;

    public WebHookCallbackResponse(Resource resource, Action action) {
        this.resource = resource;
        this.action = action;
        this.date = System.currentTimeMillis();
    }

}
