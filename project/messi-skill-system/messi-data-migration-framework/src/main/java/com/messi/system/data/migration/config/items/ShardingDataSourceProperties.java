package com.messi.system.data.migration.config.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分库分表数据源配置属性
 */
@Data
@NoArgsConstructor
public class ShardingDataSourceProperties {

    /**
     * 数据源
     */
    private List<DruidDataSourceProperties> dataSources;

    /**
     * 分片策略
     */
    private List<ShardingRuleProperties> shardingRules;

    /**
     * 是否显示 sharding-jdbc sql执行日志
     */
    private boolean showSql;

    /**
     * 每个逻辑库中表的数量
     */
    private int tableNum;
}
