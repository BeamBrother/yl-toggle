package io.yltoggle.api.dao.entity;

import io.yltoggle.api.dao.listener.TenantEntityListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "variation_history")
@DynamicInsert
@ToString(callSuper = true)
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizationId", type = Long.class)})
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
public class VariationHistory implements Serializable, TenantSupport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "toggle_key")
    private String toggleKey;

    @Column(name = "environment_key")
    private String environmentKey;

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "toggle_version")
    private Long toggleVersion;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Column(name = "value_index")
    private Integer valueIndex;

    private String name;

    @Column(name = "organization_id")
    private Long organizationId;
}
