package io.yltoggle.api.dto;

import io.yltoggle.api.base.model.SegmentRuleModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SegmentPublishRequest {

    private List<SegmentRuleModel> rules;

    @Schema(description = "Release notes")
    private String comment;

    @Schema(description = "The current updated version.")
    private Long baseVersion;

}
