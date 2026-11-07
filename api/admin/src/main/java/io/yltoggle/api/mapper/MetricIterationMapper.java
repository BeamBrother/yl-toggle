package io.yltoggle.api.mapper;

import io.yltoggle.api.dao.entity.MetricIteration;
import io.yltoggle.api.dto.MetricIterationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MetricIterationMapper {

    MetricIterationMapper INSTANCE = Mappers.getMapper(MetricIterationMapper.class);

    MetricIterationResponse entityToResponse(MetricIteration iteration);

}
