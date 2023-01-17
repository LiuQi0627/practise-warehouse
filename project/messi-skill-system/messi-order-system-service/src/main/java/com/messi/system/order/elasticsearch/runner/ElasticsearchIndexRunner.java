package com.messi.system.order.elasticsearch.runner;

import com.messi.system.order.constants.OrderElasticsearchConstants;
import com.messi.system.order.elasticsearch.client.ElasticsearchClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * es索引运行器
 */
@Slf4j
@Component
public class ElasticsearchIndexRunner implements ApplicationRunner {

    @Autowired
    private ElasticsearchClientService elasticsearchClientService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //  创建前先删除旧索引，这步方法只在调试时候使用
//        elasticsearchClientService.deleteIndex(OrderElasticsearchConstants.ORDER_ES_INDEX);

        //  如果索引不存在，就创建索引
        elasticsearchClientService.createIndexIfNotExists(OrderElasticsearchConstants.ORDER_ES_INDEX);
    }

}
