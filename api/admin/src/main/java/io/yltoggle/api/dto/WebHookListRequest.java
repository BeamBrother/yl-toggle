package io.yltoggle.api.dto;

import io.yltoggle.api.base.hook.HookSettingsStatus;
import io.yltoggle.api.base.model.PaginationRequest;
import lombok.Data;

@Data
public class WebHookListRequest extends PaginationRequest {

    private String nameLike;

    private HookSettingsStatus status;

}
