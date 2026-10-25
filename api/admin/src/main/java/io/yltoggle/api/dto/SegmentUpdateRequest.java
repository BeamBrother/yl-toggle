package io.yltoggle.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SegmentUpdateRequest {

    @Schema(description = "A human-friendly name for the new segment.")
    private String name;

    @Schema(description = "A custom description for the new segment.")
    private String description;

}
