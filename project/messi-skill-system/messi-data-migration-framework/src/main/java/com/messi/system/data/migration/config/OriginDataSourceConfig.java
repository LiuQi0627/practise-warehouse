package com.messi.system.data.migration.config;

import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.messi.system.data.migration.config.items.MigrateDruidConfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * 源库数据源配置（单库单表）
 */
@Configuration
@MapperScan(basePackages = "com.messi.system.data.migration.mapper.origindb", sqlSessionFactoryRef = "OriginSqlSessionFactory")
public class OriginDataSourceConfig extends AbstractDataSourceConfig {

    @Autowired
    private MigrateDruidConfig migrateDruidConfig;

    @Bean(name = "OriginDataSource")
    public DataSource originDataSource() {
        return buildDruidDataSource(migrateDruidConfig.getOrigin());
    }

    @Bean(name = "OriginSqlSessionFactory")
    @Primary
    public SqlSessionFactory originSqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(originDataSource());
        factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/origindb/*.xml"));
        factory.setVfs(SpringBootVFS.class);
        Objects.requireNonNull(factory);
        return factory.getObject();

    }

    @Bean(name = "OriginTransactionManager")
    @Primary
    public DataSourceTransactionManager originTransactionManager() {
        return new DataSourceTransactionManager(originDataSource());
    }

    @Bean(name = "OriginSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate originSqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(originSqlSessionFactory());
    }
}
