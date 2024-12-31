package com.td.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "recharge")
public class CenterServerConfig {
    private String addr;
    private String privateKey;
}  