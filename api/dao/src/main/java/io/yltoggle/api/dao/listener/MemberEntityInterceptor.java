package io.yltoggle.api.dao.listener;

import io.yltoggle.api.base.component.SpringBeanManager;
import io.yltoggle.api.base.security.IEncryptionService;
import io.yltoggle.api.dao.entity.Member;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

public class MemberEntityInterceptor {


    @PostLoad
    public void modifyEntity(Object entity) {
        IEncryptionService encryptionService = SpringBeanManager.getBeanByType(IEncryptionService.class);
        Member member = (Member) entity;
        member.setAccount(encryptionService.decrypt(member.getAccount()));
    }

    @PrePersist
    public void prePersist(Object entity) {
        IEncryptionService encryptionService = SpringBeanManager.getBeanByType(IEncryptionService.class);
        Member member = (Member) entity;
        member.setAccount(encryptionService.encrypt(member.getAccount()));
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        IEncryptionService encryptionService = SpringBeanManager.getBeanByType(IEncryptionService.class);
        Member member = (Member) entity;
        member.setAccount(encryptionService.encrypt(member.getAccount()));
    }
}
