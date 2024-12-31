package com.td.common.config;

import com.td.client.enums.RedPacketConfigKeyEnums;
import com.td.client.service.RedPacketConfigService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;

@Log4j2
@Configuration
public class RestTemplateConfig {

    @Autowired
    private RedPacketConfigService redPacketConfigService;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // 获取配置
        String baseUrl = redPacketConfigService.getConfig(RedPacketConfigKeyEnums.BASE_URL.getKey());
        log.info("BASE_URL: {}", baseUrl);
        // 设置前缀 URL
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        return builder
                .uriTemplateHandler(uriBuilderFactory)
                .setConnectTimeout(Duration.ofMinutes(1)) // 设置连接超时为1分钟
                .setReadTimeout(Duration.ofMinutes(1)) // 设置读取超时为1分钟
                .build();
    }
}
