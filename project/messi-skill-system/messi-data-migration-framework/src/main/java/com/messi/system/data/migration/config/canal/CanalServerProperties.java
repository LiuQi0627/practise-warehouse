package com.messi.system.data.migration.config.canal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
@ConfigurationProperties(prefix = "canal.server")
@Getter
@Setter
public class CanalServerProperties {

    private String hostname;

    private int port;

    private String destination;

    private String username;

    private String password;
}
