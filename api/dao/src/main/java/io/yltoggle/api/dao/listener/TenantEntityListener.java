package io.yltoggle.api.dao.listener;

import io.yltoggle.api.base.tenant.TenantContext;
import io.yltoggle.api.dao.entity.TenantSupport;
import org.springframework.stereotype.Component;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Component
public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof TenantSupport && TenantContext.getCurrentTenant() != null) {
            ((TenantSupport) entity).setOrganizationId(Long.parseLong(TenantContext.getCurrentTenant()));
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof TenantSupport && TenantContext.getCurrentTenant() != null) {
            ((TenantSupport) entity).setOrganizationId(Long.parseLong(TenantContext.getCurrentTenant()));
        }
    }

}
