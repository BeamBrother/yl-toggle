package io.yltoggle.api.dao.entity;

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
import jakarta.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "targeting_version")
@DynamicInsert
@ToString(callSuper = true)
@EntityListeners(TenantEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizationId", type = Long.class)})
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
public class TargetingVersion extends AbstractAuditEntity implements TenantSupport {

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "environment_key")
    private String environmentKey;

    @Column(name = "toggle_key")
    private String toggleKey;

    private String comment;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(columnDefinition = "TINYINT")
    private Boolean disabled;

    @Column(name = "organization_id")
    private Long organizationId;

    private Long version;

    @Column(name = "approval_id")
    private Long approvalId;

}
