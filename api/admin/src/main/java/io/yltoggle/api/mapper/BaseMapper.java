package io.yltoggle.api.mapper;

import io.yltoggle.api.dao.entity.Member;

public interface BaseMapper {
    default String getAccount(Member member) {
        if (member == null) {
            return "unknown";
        }
        return member.getAccount();
    }

}
