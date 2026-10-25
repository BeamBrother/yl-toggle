package io.yltoggle.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class AttributeRequest {

    @Schema(description = "A unique key used to reference the user attribute.")
    @NotBlank
    private String key;

}
