package io.yltoggle.api.mapper;

import io.yltoggle.api.base.model.HookContext;

import io.yltoggle.api.hook.CallbackRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HookContextMapper {

    HookContextMapper INSTANCE = Mappers.getMapper(HookContextMapper.class);

    @Mapping(target = "data", source = "response")
    CallbackRequestBody contextToRequestBody(HookContext hookContext);


}
