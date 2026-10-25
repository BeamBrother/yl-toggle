package io.yltoggle.api.config;

import io.yltoggle.api.base.cache.MemoryCache;
import io.yltoggle.api.base.component.SpringBeanManager;
import io.yltoggle.api.dao.repository.PublishMessageRepository;
import io.yltoggle.api.server.CacheServerDataSource;
import io.yltoggle.api.server.DBServerDataSource;
import io.yltoggle.api.server.ServerDataSource;
import io.yltoggle.api.service.BaseServerService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ServerDataSourceConfig {

    private static final String SERVER_DATA_SOURCE_DB = "DB";

    private static final String SERVER_DATA_SOURCE_CACHE = "MEMORY";

    AppConfig appConfig;

    PublishMessageRepository publishMessageRepository;

    BaseServerService baseServerService;

    ApplicationEventPublisher eventPublisher;

    SpringBeanManager springBeanManager;

    @Bean
    public ServerDataSource serverDataSource() {
        String serverDataSource = appConfig.getServerDataSource();
        if (StringUtils.isNotBlank(serverDataSource) && StringUtils.equalsIgnoreCase(serverDataSource,
                SERVER_DATA_SOURCE_DB)) {
            return new DBServerDataSource(baseServerService);
        } else if (StringUtils.isNotBlank(serverDataSource) && StringUtils.equalsIgnoreCase(serverDataSource,
                SERVER_DATA_SOURCE_CACHE)) {
            MemoryCache<String, byte[]> cache = MemoryCache.createArrayByteCache(600);
            return new CacheServerDataSource(cache, publishMessageRepository, baseServerService, eventPublisher);
        } else {
            return new DBServerDataSource(baseServerService);
        }
    }

}
