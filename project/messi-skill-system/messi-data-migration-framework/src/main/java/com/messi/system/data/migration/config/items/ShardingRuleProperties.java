package com.messi.system.data.migration.config.items;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分片规则配置项
 */
@Data
@NoArgsConstructor
public class ShardingRuleProperties {
    /**
     * 逻辑表名
     */
    private String logicTable;
    /**
     * 库分片列名称,多个列以逗号分隔
     */
    private String dbShardingColumns;
    /**
     * 库分片策略类,全限定类名
     */
    private String dbShardingAlgorithm;
    /**
     * 表分片列名称,多个列以逗号分隔
     */
    private String tableShardingColumns;
    /**
     * 表分片策略类,全限定类名
     */
    private String tableShardingAlgorithm;
}
