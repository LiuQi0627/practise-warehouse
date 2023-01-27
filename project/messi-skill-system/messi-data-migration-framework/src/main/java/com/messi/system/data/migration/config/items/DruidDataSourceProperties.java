package com.messi.system.data.migration.config.items;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 普通数据源配置属性
 */
@Data
@NoArgsConstructor
public class DruidDataSourceProperties {

    /**
     * driverClassName
     */
    private String driverClassName;
    /**
     * url
     */
    private String url;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

}