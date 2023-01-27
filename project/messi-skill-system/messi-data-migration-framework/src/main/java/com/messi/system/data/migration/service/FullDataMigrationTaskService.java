package com.messi.system.data.migration.service;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * 执行全量迁移任务的service
 */
public interface FullDataMigrationTaskService {

    Long migrationFullDataTask(String tableName, Date fullMigrationDate,
                               Long minId, Integer batchDataSize, FullDataMigrationTaskService fullDataMigrationTaskService);
}
