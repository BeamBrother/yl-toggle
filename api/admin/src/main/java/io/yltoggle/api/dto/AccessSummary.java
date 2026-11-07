package io.yltoggle.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class AccessSummary {

    @NotNull
    private Long startTime;

    @NotNull
    private Long endTime;

    @NotEmpty
    private Map<String, List<VariationAccessCounter>> counters;
}
