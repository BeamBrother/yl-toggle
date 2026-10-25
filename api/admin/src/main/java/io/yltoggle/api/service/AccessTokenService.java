package io.yltoggle.api.service;

import io.yltoggle.api.auth.TokenHelper;
import io.yltoggle.api.base.db.ExcludeTenant;
import io.yltoggle.api.base.enums.AccessTokenType;
import io.yltoggle.api.base.enums.MemberSourceEnum;
import io.yltoggle.api.base.enums.ResourceType;
import io.yltoggle.api.base.util.KeyGenerateUtil;
import io.yltoggle.api.dao.entity.AccessToken;
import io.yltoggle.api.dao.entity.Member;
import io.yltoggle.api.dao.exception.ResourceConflictException;
import io.yltoggle.api.dao.exception.ResourceNotFoundException;
import io.yltoggle.api.dao.repository.AccessTokenRepository;
import io.yltoggle.api.dao.utils.PageRequestUtil;
import io.yltoggle.api.dto.AccessTokenCreateRequest;
import io.yltoggle.api.dto.AccessTokenResponse;
import io.yltoggle.api.dto.AccessTokenSearchRequest;
import io.yltoggle.api.dto.MemberCreateRequest;
import io.yltoggle.api.dto.TokenResponse;
import io.yltoggle.api.mapper.AccessTokenMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.Optional;

@AllArgsConstructor
@Service
public class AccessTokenService {

    private AccessTokenRepository accessTokenRepository;

    @PersistenceContext
    public EntityManager entityManager;

    private MemberService memberService;

    @Transactional(rollbackFor = Exception.class)
    public TokenResponse create(AccessTokenCreateRequest createRequest) {
        validateExists(createRequest.getName(), createRequest.getType());

        AccessToken accessToken = accessTokenRepository.save(newToken(createRequest));
        return new TokenResponse(accessToken.getToken());
    }

    private AccessToken newToken(AccessTokenCreateRequest createRequest) {
        AccessToken token = AccessTokenMapper.INSTANCE.requestToEntity(createRequest);
        token.setToken(KeyGenerateUtil.getAPIAccessToken());
        if (createRequest.getType() == AccessTokenType.PERSON) {
            token.setMemberId(TokenHelper.getUserId());
            token.setRole(null);
        } else {
            Member member = createAccessTokenMember(createRequest, token);
            token.setMemberId(member.getId());
        }
        return token;
    }

    private Member createAccessTokenMember(AccessTokenCreateRequest createRequest, AccessToken token) {
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest();
        String account = "api:" + token.getName();
        memberCreateRequest.setAccounts(Lists.newArrayList(account));
        memberCreateRequest.setPassword(String.valueOf(System.nanoTime()));
        memberCreateRequest.setSource(MemberSourceEnum.ACCESS_TOKEN.name());
        memberCreateRequest.setRole(createRequest.getRole());
        memberService.createUserInCurrentOrganization(memberCreateRequest);
        Member member = memberService.findByAccount(account).get();
        return member;
    }

    @Transactional(rollbackFor = Exception.class)
    public AccessTokenResponse delete(Long tokenId) {
        AccessToken accessToken = getAccessTokenById(tokenId);
        accessToken.setDeleted(true);
        if (accessToken.getMemberId() != null && accessToken.getType() == AccessTokenType.APPLICATION) {
            Member member = memberService.findById(accessToken.getMemberId()).orElse(null);
            if (member != null) {
                memberService.delete(member.getAccount());
            }
        }
        return AccessTokenMapper.INSTANCE.entityToResponse(accessTokenRepository.save(accessToken));
    }

    public Page<AccessTokenResponse> list(AccessTokenSearchRequest searchRequest) {
        Pageable pageable = PageRequestUtil.toCreatedTimeDescSortPageable(searchRequest);
        Page<AccessToken> tokens = accessTokenRepository.findAll(buildQuerySpec(searchRequest), pageable);
        return tokens.map(token -> AccessTokenMapper.INSTANCE.entityToResponse(token));
    }

    private Specification<AccessToken> buildQuerySpec(AccessTokenSearchRequest searchRequest) {
        return (root, query, cb) -> {
            Predicate p3 = cb.equal(root.get("type"), searchRequest.getType());
            return query.where(p3).getRestriction();
        };
    }

    public AccessTokenResponse queryById(Long tokenId) {
        return AccessTokenMapper.INSTANCE.entityToResponse(getAccessTokenById(tokenId));
    }

    private AccessToken getAccessTokenById(Long tokenId) {
        AccessToken accessToken = accessTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ACCESS_TOKEN, String.valueOf(tokenId)));
        return accessToken;
    }

    public void validateExists(String name, AccessTokenType type) {
        boolean existed = accessTokenRepository.existsByNameAndType(name, type);
        if (existed) {
            throw new ResourceConflictException(ResourceType.ACCESS_TOKEN);
        }
    }

    @ExcludeTenant
    public Optional<AccessToken> findByToken(String token) {
        return accessTokenRepository.findByToken(token);
    }

    @ExcludeTenant
    public void updateVisitedTime(Long id) {
        AccessToken accessToken = getAccessTokenById(id);
        accessToken.setVisitedTime(new Date());
        accessTokenRepository.save(accessToken);
    }
}
