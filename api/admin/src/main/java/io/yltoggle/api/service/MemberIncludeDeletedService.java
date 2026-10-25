package io.yltoggle.api.service;

import io.yltoggle.api.dao.entity.Member;
import io.yltoggle.api.dao.exception.ResourceConflictException;
import io.yltoggle.api.dao.repository.MemberRepository;
import io.yltoggle.api.base.db.IncludeDeleted;
import io.yltoggle.api.base.enums.ResourceType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MemberIncludeDeletedService {

    private MemberRepository memberRepository;

    @PersistenceContext
    public EntityManager entityManager;

    @IncludeDeleted
    public boolean validateAccountIncludeDeleted(String account) {
        if (memberRepository.existsByAccount(account)) {
            throw new ResourceConflictException(ResourceType.MEMBER);
        }
        return true;
    }

    @IncludeDeleted
    public Optional<Member> queryMemberByAccountIncludeDeleted(String account) {
        return memberRepository.findByAccount(account);
    }

}
