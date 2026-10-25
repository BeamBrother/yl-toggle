package io.yltoggle.api.config;

import io.yltoggle.api.component.SdkVersionScheduler;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = {
    SdkVersionScheduler.class,
})
public class ScheduleConfig {
}
