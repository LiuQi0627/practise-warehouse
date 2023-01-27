package com.messi.system.data.migration;

import com.messi.system.data.migration.config.items.MigrateDruidConfig;
import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 数据迁移程序服务启动入口
 */
@EnableConfigurationProperties(MigrateDruidConfig.class)
@SpringBootApplication(exclude = SpringBootConfiguration.class)
public class DataMigrationApp {
    public static void main(String[] args) {
        SpringApplication.run(DataMigrationApp.class);
    }
}
