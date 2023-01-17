package com.messi.system.order.service.impl;

import com.messi.system.order.converter.OrderConverter;
import com.messi.system.order.domain.dto.OrderDetailDTO;
import com.messi.system.order.elasticsearch.client.ElasticsearchClientService;
import com.messi.system.order.elasticsearch.index.OrderEsIndex;
import com.messi.system.order.service.OrderPushService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * 推送订单service实现类
 */
@Service
public class OrderPushServiceImpl implements OrderPushService {

    //  使用指定的自定义ThreadPoolTaskExecutor
    @Autowired
    @Qualifier("pushEsDataFixedThreadPool")
    private ThreadPoolTaskExecutor threadPoolExecutor;

    @Autowired
    private ElasticsearchClientService elasticsearchClientService;

    @Override
    public void sendOrderToEs(OrderDetailDTO orderDetailDTO) {
        //  线程池执行
        threadPoolExecutor.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                elasticsearchClientService.sendOrderToEs(OrderEsIndex.class, orderDetailDTO);
            }
        });

    }
}
