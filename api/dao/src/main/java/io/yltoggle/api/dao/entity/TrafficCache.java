package io.yltoggle.api.dao.entity;

import io.yltoggle.api.base.enums.TrafficCacheTypeEnum;
import lombok.AllArgsConstructor;
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
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "traffic_cache")
@DynamicInsert
@ToString(callSuper = true)
public class TrafficCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sdk_key")
    private String sdkKey;

    @Column(name = "toggle_key")
    private String toggleKey;

    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Enumerated(EnumType.STRING)
    private TrafficCacheTypeEnum type;
}
