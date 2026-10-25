package io.yltoggle.api.dto;

import io.yltoggle.api.base.enums.AccessTokenType;
import io.yltoggle.api.base.model.PaginationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AccessTokenSearchRequest extends PaginationRequest {

    @NotNull
    @Schema(description = "The type of access token to retrieve .")
    private AccessTokenType type;


}
