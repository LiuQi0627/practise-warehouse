package com.messi.system.data.migration.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.messi.system.data.migration.config.items.DruidDataSourceProperties;
import com.messi.system.data.migration.config.items.ShardingDataSourceProperties;
import com.messi.system.data.migration.config.items.ShardingRuleProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * 数据源配置
 */
@Slf4j
public abstract class AbstractDataSourceConfig {

    /**
     * 构建Druid数据源
     */
    protected DruidDataSource buildDruidDataSource(DruidDataSourceProperties druidDataSourceProperties) {

        return DataSourceBuilder.create()
                .type(DruidDataSource.class)
                .driverClassName(druidDataSourceProperties.getDriverClassName())
                .url(druidDataSourceProperties.getUrl())
                .username(druidDataSourceProperties.getUsername())
                .password(druidDataSourceProperties.getPassword())
                .build();
    }

    /**
     * 构建sharding-jdbc数据源
     */
    protected DataSource buildShardingDataSource(ShardingDataSourceProperties shardingDataSourceProperties) throws SQLException {
        // 1.配置真实数据源
        Map<String, DataSource> dataSourceMap = buildDataSourceMap(shardingDataSourceProperties.getDataSources());

        // 2.对各个表配置分库分表规则
        ShardingRuleConfiguration shardingRuleConfig = buildShardingRuleConfiguration(shardingDataSourceProperties);

        // 3.配置其他的属性
        Properties properties = new Properties();
        properties.put("sql.show", shardingDataSourceProperties.isShowSql());
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, properties);
    }

    /**
     * 构建配置真实数据源
     */
    private Map<String, DataSource> buildDataSourceMap(List<DruidDataSourceProperties> druidDataSourceProperties) {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        int dsSize = druidDataSourceProperties.size();
        for (int i = 0; i < dsSize; i++) {
            //根据配置创建dataSource
            DruidDataSource dataSource = buildDruidDataSource(druidDataSourceProperties.get(i));
            //将目标数据源放入dataSourceMap
            dataSourceMap.put("ds" + i, dataSource);
        }
        return dataSourceMap;
    }

    /**
     * 配置分片规则
     */
    private ShardingRuleConfiguration buildShardingRuleConfiguration(ShardingDataSourceProperties shardingDataSourceProperties) {
        //分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

        // 配置默认数据源
        shardingRuleConfig.setDefaultDataSourceName("ds0");

        int dsSize = shardingDataSourceProperties.getDataSources().size();

        //最后一个db的下标
        int dbLastIndex = dsSize - 1;
        //最后一个table的下标
        int tableLastIndex = shardingDataSourceProperties.getTableNum() - 1;

        for (ShardingRuleProperties tableRule : shardingDataSourceProperties.getShardingRules()) {
            //构建分片策略实例
            PreciseShardingAlgorithm<String> dbShardingAlgorithm = buildAlgorithmInstance(tableRule.getDbShardingAlgorithm());
            PreciseShardingAlgorithm<String> tableShardingAlgorithm = buildAlgorithmInstance(tableRule.getTableShardingAlgorithm());

            // 配置表规则和分库分表策略
            TableRuleConfiguration tableRuleConfiguration;

            String logicTable = tableRule.getLogicTable();
            if ("order_info".equals(logicTable) || "order_item_info".equals(logicTable)
                    || "order_price_details".equals(logicTable)) {
                // 分库分表
                tableRuleConfiguration = new TableRuleConfiguration(tableRule.getLogicTable(), "ds${0.." + dbLastIndex + "}." + tableRule.getLogicTable() + "_${0.." + tableLastIndex + "}");
                assert dbShardingAlgorithm != null;
                tableRuleConfiguration.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration(tableRule.getDbShardingColumns(), dbShardingAlgorithm));
                assert tableShardingAlgorithm != null;
                tableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(tableRule.getTableShardingColumns(), tableShardingAlgorithm));
                shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfiguration);
            } else {
                log.info("不分库也不分表，走默认数据源");
            }

        }
        return shardingRuleConfig;
    }

    /**
     * 构建分片策略实例
     */
    @SuppressWarnings("unchecked")
    private PreciseShardingAlgorithm<String> buildAlgorithmInstance(String shardingAlgorithm) {
        if (StringUtils.isBlank(shardingAlgorithm)) {
            return null;
        }
        try {
            Class<?> algorithmClass = Class.forName(shardingAlgorithm);
            return (PreciseShardingAlgorithm<String>) algorithmClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("build shardingAlgorithm instance error,cause:{}", e.getCause());
        }
    }

}
