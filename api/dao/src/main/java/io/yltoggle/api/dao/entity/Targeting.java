package io.yltoggle.api.dao.entity;

import io.yltoggle.api.base.enums.ToggleReleaseStatusEnum;
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
import jakarta.persistence.Version;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "targeting")
@DynamicInsert
@ToString(callSuper = true)
@EntityListeners(TenantEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizationId", type = Long.class)})
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = Boolean.class)})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class Targeting extends AbstractAuditEntity implements TenantSupport {

    @Column(name = "toggle_key")
    private String toggleKey;

    @Column(name = "environment_key")
    private String environmentKey;

    @Column(name = "project_key")
    private String projectKey;

    private Long version;

    @Column(columnDefinition = "BIT", length = 1)
    private boolean disabled;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "publish_time")
    private Date publishTime;

    @Column(columnDefinition = "BIT", length = 1)
    private boolean deleted;

    @Column(name = "organization_id")
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    private ToggleReleaseStatusEnum status;

    @Version
    @Column(name = "_lock_version")
    private Long lockVersion;

    public String uniqueKey() {
        return projectKey + "&" + environmentKey + "&" + toggleKey;
    }

}
