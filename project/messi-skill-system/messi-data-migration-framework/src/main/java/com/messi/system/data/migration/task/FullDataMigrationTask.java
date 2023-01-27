package com.messi.system.data.migration.task;

import com.messi.system.data.migration.service.FullDataMigrationTaskService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * 全量迁移数据任务执行器
 */
@Slf4j
public class FullDataMigrationTask implements Runnable {

    private final String tableName;

    private final CountDownLatch countDownLatch;

    private final Date fullMigrationDate;

    private Long minId;

    private final Integer batchDataSize;

    private final FullDataMigrationTaskService fullDataMigrationTaskService;

    private boolean run_status = true;

    public FullDataMigrationTask(String tableName, CountDownLatch countDownLatch, Date fullMigrationDate,
                                 Long minId, Integer batchDataSize, FullDataMigrationTaskService fullDataMigrationTaskService) {
        this.tableName = tableName;
        this.countDownLatch = countDownLatch;
        this.fullMigrationDate = fullMigrationDate;
        this.minId = minId;
        this.batchDataSize = batchDataSize;
        this.fullDataMigrationTaskService = fullDataMigrationTaskService;
    }

    @Override
    public void run() {
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        log.info("开始迁移[{}]表的全量数据,开始迁移时间：{}", tableName, startTime);

        //  初始状态是true,表示可以一直运行
        while (run_status) {
            try {
                Long curDbId = fullDataMigrationTaskService.migrationFullDataTask(tableName,
                        fullMigrationDate, minId, batchDataSize, fullDataMigrationTaskService);

                if (curDbId == null) {
                    //  queryByBatch查不出新的数据，表示数据已经迁移完成
                    String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                    log.info("[{}]表的全量数据已经迁移完成,完成迁移时间:{}", tableName, endTime);
                    break;
                }

                minId = curDbId;

            } catch (Exception e) {
                log.error("FullDataMigrationTask执行失败，tableName:{},minId:{},batchDataSize:{},失败原因:{}",
                        this.tableName, this.minId, this.batchDataSize, e.getLocalizedMessage());

                run_status = false;
                throw new RuntimeException(e);
            }

        }

        countDownLatch.countDown();
    }

}
