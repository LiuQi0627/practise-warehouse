//package com.messi.system.order.sharding;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
//import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
//
//import java.util.Collection;
//
///**
// * 分库使用的精准分片算法
// */
//@Slf4j
//public class DatabasePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
//
//    /**
//     * 重写自定义的分片策略
//     *
//     * @param datasourceNames      数据源名称集合
//     * @param preciseShardingValue 分片值
//     * @return 匹配的数据源名
//     */
//    @Override
//    public String doSharding(Collection<String> datasourceNames, PreciseShardingValue<String> preciseShardingValue) {
//        return getDatabaseName(datasourceNames, preciseShardingValue.getValue());
//    }
//
//    /**
//     * 通过用户id做路由，返回用户id所在的数据库
//     */
//    private String getDatabaseName(Collection<String> datasourceNames, String columnValue) {
//        //  获取业务ID的后三位后缀
//        String valueSuffix = columnValue.length() <= 3 ? columnValue : columnValue.substring(columnValue.length() - 3);
//
//        //  获取将要路由到数据源的后缀
//        String databaseSuffix = ShardingAlgorithmHelper.getDatabaseSuffix(Integer.parseInt(valueSuffix));
//
//        for (String datasourceName : datasourceNames) {
//            //  返回匹配到的数据源
//            if (datasourceName.endsWith(databaseSuffix)) {
//                return datasourceName;
//            }
//        }
//
//        log.info("路由键:{}没有精准匹配到数据源", columnValue);
//        return null;
//    }
//}
