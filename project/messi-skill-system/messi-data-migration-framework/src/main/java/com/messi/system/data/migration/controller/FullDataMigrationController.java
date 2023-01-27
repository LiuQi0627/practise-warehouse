package com.messi.system.data.migration.controller;

import com.google.common.base.Stopwatch;
import com.messi.system.data.migration.service.FullDataMigrationTaskService;
import com.messi.system.data.migration.service.targetdb.OrderInfoTargetService;
import com.messi.system.data.migration.service.targetdb.OrderItemInfoTargetService;
import com.messi.system.data.migration.service.targetdb.OrderPriceDetailsTargetService;
import com.messi.system.data.migration.task.FullDataMigrationTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 全量数据迁移的请求入口
 * 指定一个固定的时间执行全量数据迁移请求
 * 例如 00：00执行，那么就以此时间作为分界点，
 * 00:00之前的数据是全量数据，00：00之后新产生的数据会在全量迁移后，由另一个增量请求做新生成的数据迁移
 */
@RestController
@Slf4j
public class FullDataMigrationController {

    @Autowired
    private OrderInfoTargetService orderInfoTargetService;

    @Autowired
    private OrderItemInfoTargetService orderItemInfoTargetService;

    @Autowired
    private OrderPriceDetailsTargetService orderPriceDetailsTargetService;

    @Autowired
    private FullDataMigrationTaskService fullDataMigrationTaskService;

    private static final Long minId = 0L;

    /**
     * 自定义全量数据的截止时间，此时间之前的数据都是全量数据，此时间之后产生的数据都算增量数据
     */
    @Value("${data.migration.fullDataDeadline}")
    private long fullDataDeadline;

    /**
     * 每次迁移处理的数据量
     */
    @Value("${data.migration.batchDataSize}")
    private int batchDataSize;

    /**
     * 全量数据迁移入口
     */
    @GetMapping("/fullDataMigration")
    public Boolean fullDataMigration() {
        Date fullMigrationDate = new Date(fullDataDeadline);
        //  要迁移几张表，就设置几个线程计数器
        CountDownLatch latch = new CountDownLatch(3);
        Stopwatch started = Stopwatch.createStarted();

        //  order_info表
        Long orderInfoMaxId = orderInfoTargetService.getMaxId();
        if (orderInfoMaxId == null) {
            orderInfoMaxId = minId;
        }
        Thread orderInfoThread = new Thread(
                new FullDataMigrationTask("order_info", latch, fullMigrationDate,
                        orderInfoMaxId, batchDataSize, fullDataMigrationTaskService));
        orderInfoThread.setDaemon(true);
        orderInfoThread.start();

        //  order_item_info表
        Long orderItemMaxId = orderItemInfoTargetService.getMaxId();
        if (orderItemMaxId == null) {
            orderItemMaxId = minId;
        }
        Thread orderItemInfoThread = new Thread(
                new FullDataMigrationTask("order_item_info", latch, fullMigrationDate,
                        orderItemMaxId, batchDataSize, fullDataMigrationTaskService));
        orderItemInfoThread.setDaemon(true);
        orderItemInfoThread.start();

        //  order_price_details表
        Long orderPriceDetailsMaxId = orderPriceDetailsTargetService.getMaxId();
        if (orderPriceDetailsMaxId == null) {
            orderPriceDetailsMaxId = minId;
        }
        Thread orderPriceDetailsThread = new Thread(
                new FullDataMigrationTask("order_price_details", latch, fullMigrationDate,
                        orderPriceDetailsMaxId, batchDataSize, fullDataMigrationTaskService));
        orderPriceDetailsThread.setDaemon(true);
        orderPriceDetailsThread.start();

        try {
            latch.await();
            log.info("所有工作线程执行完毕,共耗时:{}/s", started.elapsed(TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;

    }

}
