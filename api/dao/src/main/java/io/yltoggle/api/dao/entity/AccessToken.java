package io.yltoggle.api.dao.entity;

import io.yltoggle.api.base.enums.AccessTokenType;
import io.yltoggle.api.base.enums.OrganizationRoleEnum;
import io.yltoggle.api.dao.listener.TenantEntityListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "access_token")
@ToString(callSuper = true)
@DynamicInsert
@EntityListeners(TenantEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizationId", type = Long.class)})
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = Boolean.class)})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class AccessToken extends AbstractAuditEntity implements TenantSupport, Serializable {

    private String name;

    @Enumerated(EnumType.STRING)
    private OrganizationRoleEnum role;

    @Column(name = "member_id")
    private Long memberId;

    @Column(columnDefinition = "TINYINT")
    private boolean deleted;

    @Column(name = "organization_id")
    private Long organizationId;

    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "[type]")
    private AccessTokenType type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "visited_time")
    private Date visitedTime;

}
