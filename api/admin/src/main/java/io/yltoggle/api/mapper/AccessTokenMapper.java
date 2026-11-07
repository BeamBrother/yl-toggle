package io.yltoggle.api.mapper;

import io.yltoggle.api.dao.entity.AccessToken;
import io.yltoggle.api.dto.AccessTokenCreateRequest;
import io.yltoggle.api.dto.AccessTokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccessTokenMapper extends BaseMapper {

    AccessTokenMapper INSTANCE = Mappers.getMapper(AccessTokenMapper.class);

    AccessTokenResponse entityToResponse(AccessToken accessToken);

    AccessToken requestToEntity(AccessTokenCreateRequest request);
}
