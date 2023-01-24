package com.messi.system.order.sharding;

import cn.hutool.core.collection.CollectionUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 分库使用的复杂分片算法
 */
@Slf4j
@NoArgsConstructor
public class DatabaseComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<String> {

    /**
     * 分片键优先级从高到底排序:order_id -> user_id
     *
     * @param datasourceNames 数据源名称集合
     * @param shardingValue   分片值
     * @return 匹配的数据源名
     */
    @Override
    public Collection<String> doSharding(Collection<String> datasourceNames, ComplexKeysShardingValue<String> shardingValue) {
        Collection<String> orderIds = shardingValue.getColumnNameAndShardingValuesMap().get("order_id");
        Collection<String> userIds = shardingValue.getColumnNameAndShardingValuesMap().get("user_id");

        //  在设计分布式ID时，就要把业务关联关系考虑进去，这样才不会在后面做分库分表时产生其他的麻烦
        //  按优先级排序
        if (CollectionUtil.isNotEmpty(orderIds)) {
            return getDatabaseName(datasourceNames, orderIds);
        }

        if (CollectionUtil.isNotEmpty(userIds)) {
            return getDatabaseName(datasourceNames, userIds);
        }

        log.info("路由键:{}、{} 没有精准匹配到数据源", orderIds, userIds);
        return null;
    }

    /**
     * 通过路由，返回所在的数据库
     */
    private Set<String> getDatabaseName(Collection<String> datasourceNames, Collection<String> columnValues) {
        Set<String> set = new HashSet<>();
        for (String columnValue : columnValues) {
            //  获取业务ID的后三位后缀
            String valueSuffix = columnValue.length() <= 3 ? columnValue : columnValue.substring(columnValue.length() - 3);

            //  获取将要路由到数据源的后缀
            String databaseSuffix = ShardingAlgorithmHelper.getDatabaseSuffix(Integer.parseInt(valueSuffix));

            for (String datasourceName : datasourceNames) {
                //  返回匹配到的数据源
                if (datasourceName.endsWith(databaseSuffix)) {
                    set.add(datasourceName);
                }
            }
        }

        return set;
    }
}
