//package com.messi.system.order.sharding;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
//import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
//
//import java.util.Collection;
//
///**
// * 分表使用的精准分片算法
// */
//@Slf4j
//public class TablePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
//
//    @Override
//    public String doSharding(Collection<String> tableNames, PreciseShardingValue<String> shardingValue) {
//        return getTableName(tableNames, shardingValue.getValue());
//    }
//
//    /**
//     * 获取表所在的数据源
//     *
//     * @param tableNames  表名
//     * @param columnValue 字段值
//     * @return 匹配的表名
//     */
//    private String getTableName(Collection<String> tableNames, String columnValue) {
//        //  获取业务ID的后三位后缀
//        String valueSuffix = columnValue.length() <= 3 ? columnValue : columnValue.substring(columnValue.length() - 3);
//
//        //  获取将要路由到数据源的后缀
//        String tableSuffix = ShardingAlgorithmHelper.getTableSuffix(Integer.parseInt(valueSuffix));
//
//        for (String tableName : tableNames) {
//            //  返回匹配到的数据源
//            if (tableName.endsWith(tableSuffix)) {
//                return tableName;
//            }
//        }
//
//        log.info("路由键:{}没有精准匹配到表", columnValue);
//        return null;
//    }
//
//}
