package io.yltoggle.api.mapper;

import io.yltoggle.api.dto.TagRequest;
import io.yltoggle.api.dto.TagResponse;
import io.yltoggle.api.dao.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TagMapper {
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    TagResponse entityToResponse(Tag tag);

    Tag requestToEntity(TagRequest tagRequest);

}
