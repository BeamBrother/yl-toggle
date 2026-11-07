package io.yltoggle.api.dao.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "organization")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@DynamicInsert
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = Boolean.class)})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
@EqualsAndHashCode
public class Organization extends AbstractAuditEntity {

    private String name;

    @Column(columnDefinition = "TINYINT")
    private boolean deleted;

    public Organization(String name) {
        this.name = name;
    }

    public static Organization createDefaultOrganization() {
        Organization organization = new Organization();
        organization.setName("Default Organization");
        return organization;
    }
}
