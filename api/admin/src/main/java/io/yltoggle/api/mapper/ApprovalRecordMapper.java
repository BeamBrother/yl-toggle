package io.yltoggle.api.mapper;

import io.yltoggle.api.dto.ApprovalRecordResponse;
import io.yltoggle.api.dao.entity.ApprovalRecord;
import io.yltoggle.api.dto.ApprovalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ApprovalRecordMapper extends BaseMapper {

    ApprovalRecordMapper INSTANCE = Mappers.getMapper(ApprovalRecordMapper.class);

    @Mapping(target = "reviewers", ignore = true)
    ApprovalRecordResponse entityToResponse(ApprovalRecord approvalRecord);

    @Mapping(target = "approvalDate", source = "modifiedTime")
    ApprovalResponse entityToApprovalResponse(ApprovalRecord approvalRecord);
}
