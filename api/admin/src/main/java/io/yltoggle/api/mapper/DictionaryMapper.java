package io.yltoggle.api.mapper;

import io.yltoggle.api.dto.DictionaryResponse;
import io.yltoggle.api.dao.entity.Dictionary;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DictionaryMapper {

    DictionaryMapper INSTANCE = Mappers.getMapper(DictionaryMapper.class);

    DictionaryResponse entityToResponse(Dictionary dictionary);
}
