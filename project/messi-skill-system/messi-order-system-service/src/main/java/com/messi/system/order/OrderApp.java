package com.messi.system.order;

import com.messi.system.consistency.annotation.EnableConsistencyFramework;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

/**
 * OrderSystem启动类
 */
@SpringBootApplication
@EnableConsistencyFramework
@ComponentScans(value = {@ComponentScan("com.messi.system.rocketmq.producer")})
//  使用最终一致性框架注解后，为了避免和框架的mapper冲突，指定服务在启动时加载自己的mapper
@MapperScan(basePackages = {"com.messi.system.order.mapper"})
public class OrderApp {
    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class);
    }
}
