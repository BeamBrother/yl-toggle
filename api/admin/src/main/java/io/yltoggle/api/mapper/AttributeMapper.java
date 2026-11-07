package io.yltoggle.api.mapper;

import io.yltoggle.api.dto.AttributeResponse;
import io.yltoggle.api.dao.entity.Attribute;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AttributeMapper {

    AttributeMapper INSTANCE = Mappers.getMapper(AttributeMapper.class);

    AttributeResponse entityToResponse(Attribute attribute);

}
