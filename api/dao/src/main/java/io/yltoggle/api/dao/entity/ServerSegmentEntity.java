package io.yltoggle.api.dao.entity;

public interface ServerSegmentEntity {

    Long getOrganizationId();

    String getProjectKey();

    String getSegmentKey();

    String getSegmentRules();

    Long getSegmentVersion();

    String getSegmentUniqueKey();

}
