package io.yltoggle.api.mapper;

import io.yltoggle.api.base.util.JsonMapper;
import io.yltoggle.api.dao.entity.DebugEvent;
import io.yltoggle.api.dao.entity.Event;
import io.yltoggle.api.dto.DebugEventResponse;
import io.yltoggle.api.dto.EventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper
public interface EventMapper {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    EventResponse entityToResponse(Event event);

    @Mapping(target = "userDetail", expression = "java(toMap(debugEvent.getUserDetail()))")
    DebugEventResponse debugEventToResponse(DebugEvent debugEvent);

    default Map toMap(String userDetail) {
        return JsonMapper.toObject(userDetail, Map.class);
    }


}
