package io.yltoggle.api.mapper;

import io.yltoggle.api.base.model.TargetingContent;
import io.yltoggle.api.dto.MetricIterationResponse;
import io.yltoggle.api.dto.TargetingVersionResponse;
import io.yltoggle.api.dao.entity.TargetingVersion;
import io.yltoggle.api.base.util.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TargetingVersionMapper extends BaseMapper {

    TargetingVersionMapper INSTANCE = Mappers.getMapper(TargetingVersionMapper.class);

    @Mapping(target = "content",
            expression = "java(toTargetingContent(targetingVersion.getContent()))")
    @Mapping(target = "createdBy", expression = "java(getAccount(targetingVersion.getCreatedBy()))")
    TargetingVersionResponse entityToResponse(TargetingVersion targetingVersion);


    @Mapping(target = "publishTime", source = "createdTime")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "releaseNote", source = "comment")
    MetricIterationResponse.PublishRecord entityToPublishRecord(TargetingVersion targetingVersion);

    default TargetingContent toTargetingContent(String content) {
        if (StringUtils.isNotBlank(content)) {
            return JsonMapper.toObject(content, TargetingContent.class);
        }
        return null;
    }

}
