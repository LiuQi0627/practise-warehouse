package com.messi.system.data.migration.config;

import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.messi.system.data.migration.config.items.MigrateDruidConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 目标库数据源配置（分库分表后的库）
 */
@Slf4j
@Configuration
@MapperScan(basePackages = "com.messi.system.data.migration.mapper.targetdb", sqlSessionFactoryRef = "TargetSqlSessionFactory")
public class TargetDataSourceConfig extends AbstractDataSourceConfig {

    @Autowired
    private MigrateDruidConfig migrateDruidConfig;

    @Bean(name = "TargetDataSource")
    public DataSource targetDataSource() throws SQLException {
        return buildShardingDataSource(migrateDruidConfig.getTarget());
    }

    @Bean(name = "TargetSqlSessionFactory")
    public SqlSessionFactory targetSqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(targetDataSource());
        factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/targetdb/*.xml"));
        factory.setVfs(SpringBootVFS.class);
        Objects.requireNonNull(factory);
        return factory.getObject();
    }

    @Bean(name = "TargetTransactionManager")
    public DataSourceTransactionManager targetTransactionManager() throws SQLException {
        return new DataSourceTransactionManager(targetDataSource());
    }

    @Bean(name = "TargetSqlSessionTemplate")
    public SqlSessionTemplate targetSqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(targetSqlSessionFactory());
    }

}
