package com.messi.system.data.migration.controller;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.messi.system.data.migration.binlog.BinlogConsumer;
import com.messi.system.data.migration.config.canal.CanalServerProperties;
import com.messi.system.data.migration.task.IncrementalConsumeBinlogTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetSocketAddress;

/**
 * 增量数据迁移的请求入口
 * 增量数据请求一定是在全量数据迁移请求完毕后再执行
 */
@RestController
public class IncrementalDataMigrationController {

    @Autowired
    private CanalServerProperties canalServerProperties;

    @Autowired
    private BinlogConsumer binlogConsumer;

    @GetMapping("/incrementDataMigration")
    public Boolean incrementDataMigration() {
        String hostname = canalServerProperties.getHostname();
        int port = canalServerProperties.getPort();
        String destination = canalServerProperties.getDestination();
        String username = canalServerProperties.getUsername();
        String password = canalServerProperties.getPassword();

        CanalConnector canalConnector
                = CanalConnectors.newSingleConnector(
                        new InetSocketAddress(hostname, port), destination, username, password);

        // 建立连接
        canalConnector.connect();

        // 异步线程执行canal监听binlog操作
        Thread thread = new Thread(new IncrementalConsumeBinlogTask(canalConnector, binlogConsumer));
        thread.setDaemon(true);
        thread.start();

        return true;
    }
}
