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
@Table(name = "targeting_segment")
@DynamicInsert
@EntityListeners(TenantEntityListener.class)
@ToString(callSuper = true)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizationId", type = Long.class)})
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
public class TargetingSegment extends AbstractAuditEntity implements TenantSupport {

    @Column(name = "targeting_id")
    private Long targetingId;

    @Column(name = "segment_key")
    private String segmentKey;

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "organization_id")
    private Long organizationId;

    public TargetingSegment(Long targetingId, String segmentKey, String projectKey) {
        this.targetingId = targetingId;
        this.segmentKey = segmentKey;
        this.projectKey = projectKey;
    }

}
