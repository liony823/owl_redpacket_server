package com.td.common.listener;

import com.td.client.service.RedPacketConfigService;
import com.td.common.pojo.RedPacketConfig;
import com.td.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Td
 * @email td52512@qq.com
 */
@Slf4j
@Component
public class EstablishConfigCacheListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RedPacketConfigService redPacketConfigService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        // 初始化配置缓存
        log.info("初始化配置缓存");
        redPacketConfigService.initConfigCache();
    }
}
