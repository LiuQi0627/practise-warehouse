package com.messi.system.consistency.annotation.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 组件扫描器配置
 */
@Configuration
@ComponentScan(value = "com.messi.system.consistency")
//  在系统启动时扫描框架里的mapper
@MapperScan(basePackages = {"com.messi.system.consistency.mapper"})
public class ComponentScanConfig {

}
