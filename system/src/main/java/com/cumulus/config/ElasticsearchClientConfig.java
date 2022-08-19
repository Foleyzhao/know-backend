package com.cumulus.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

/**
 * es配置类
 *
 * @author zhaoff
 */
@Configuration
public class ElasticsearchClientConfig extends AbstractElasticsearchConfiguration {

    /**
     * es地址
     */
    @Value("${es-config.host-and-port}")
    private String hostAndPort;

    /**
     * es地址
     */
    @Value("${es-config.username}")
    private String username;

    /**
     * es地址
     */
    @Value("${es-config.password}")
    private String password;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .withBasicAuth(username, password)
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
