package io.yltoggle.api.dao.entity;

import io.yltoggle.api.base.enums.EventTypeEnum;
import io.yltoggle.api.base.enums.MatcherTypeEnum;

public interface ServerEventEntity {

    Long getOrganizationId();

    String getProjectKey();

    String getServerSdkKey();

    String getClientSdkKey();

    EventTypeEnum getType();

    String getName();

    MatcherTypeEnum getMatcher();

    String getUrl();

    String getSelector();

}
