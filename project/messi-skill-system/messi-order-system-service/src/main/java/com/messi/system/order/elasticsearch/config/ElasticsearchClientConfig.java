package com.messi.system.order.elasticsearch.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * es client 配置
 */
@Slf4j
@Configuration
public class ElasticsearchClientConfig {

    public static final String COMMA = ",";

    public static final String COLON = ":";

    private RestHighLevelClient restHighLevelClient;

    @Value("${elasticsearch.node}")
    private String nodes;

    @Bean
    public RestHighLevelClient initEsClient() {
        //  解析 es 节点
        HttpHost[] httpHosts = loadEsNode();
        //  创建 client
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        return restHighLevelClient;
    }

    private HttpHost[] loadEsNode() {
        String[] splitNodes = nodes.split(COMMA);
        HttpHost[] httpHosts = new HttpHost[splitNodes.length];
        for (int i = 0; i < splitNodes.length; i++) {
            String nodeStr = splitNodes[i];
            String[] hostAndPort = nodeStr.split(COLON);
            //  host:port
            httpHosts[i] = new HttpHost(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
        }
        return httpHosts;
    }

    @PreDestroy
    public void close() {
        if (restHighLevelClient != null) {
            try {
                log.info("closed elasticsearch rest client.");
                restHighLevelClient.close();
            } catch (IOException e) {
                log.error("elasticsearch rest client close failed.", e);
            }
        }
    }
}
