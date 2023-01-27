package com.messi.system.data.migration.config.items;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 基于druid的多数据源配置
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "migrate.datasource.druid")
@NoArgsConstructor
public class MigrateDruidConfig {

    /**
     * 数据迁移应用自己的数据源
     */
    private DruidDataSourceProperties migrate;

    /**
     * 源数据源配置
     */
    private DruidDataSourceProperties origin;

    /**
     * 目标数据源配置
     */
    private ShardingDataSourceProperties target;

}