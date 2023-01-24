package com.messi.system.order.sharding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 分片策略算法辅助组件
 */
@Slf4j
@Component
public class ShardingAlgorithmHelper {

    /**
     * 分4个库
     */
    protected static int DATABASE_SIZE = 4;

    /**
     * 每个库分8张表
     */
    protected static int TABLE_SIZE = 8;

//    //  这样的写法，是为了让DATABASE_SIZE和TABLE_SIZE能够传入静态方法中
//    @Value("${spring.shardingsphere.sub.database.number:4}")
//    public void setDatabaseSize(int databaseSize) {
//        DATABASE_SIZE = databaseSize;
//    }
//
//    @Value("${spring.shardingsphere.sub.table.number:8}")
//    public void setTableSize(int tableSize) {
//        TABLE_SIZE = tableSize;
//    }

    /**
     * 计算匹配数据源的后缀
     * 路由键 对 数据库 做取模
     * +"" => 让值转换成string类型
     *
     * @param valueSuffix 分片键后三位
     * @return 数据源后缀
     */
    public static String getDatabaseSuffix(int valueSuffix) {
        return valueSuffix % DATABASE_SIZE + "";
    }

    /**
     * 计算匹配表的后缀
     * 路由键 / 数据库 对表的数量做取模
     *
     * @param valueSuffix 分片键后三位
     * @return 数据源后缀
     */
    public static String getTableSuffix(int valueSuffix) {
        return valueSuffix / DATABASE_SIZE % TABLE_SIZE + "";
    }

}
