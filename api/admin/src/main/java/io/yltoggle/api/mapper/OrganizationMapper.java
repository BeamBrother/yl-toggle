package io.yltoggle.api.mapper;

import io.yltoggle.api.dao.entity.Organization;
import io.yltoggle.api.dto.OrganizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrganizationMapper extends BaseMapper {

    OrganizationMapper INSTANCE = Mappers.getMapper(OrganizationMapper.class);

    OrganizationResponse entityToResponse(Organization organization);
}
