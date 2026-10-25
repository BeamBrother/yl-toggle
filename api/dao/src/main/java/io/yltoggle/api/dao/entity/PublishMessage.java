package io.yltoggle.api.dao.entity;

import io.yltoggle.api.base.enums.ChangeLogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "publish_message")
@ToString(callSuper = true)
@DynamicInsert
public class PublishMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_sdk_key")
    private String serverSdkKey;

    @Column(name = "client_sdk_key")
    private String clientSdkKey;

    @Enumerated(EnumType.STRING)
    private ChangeLogType type;

    public PublishMessage(Long id) {
        this.id = id;
    }
}
