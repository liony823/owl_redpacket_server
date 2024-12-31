package com.td.client.service.impl;

import com.td.client.service.RedPacketConfigService;
import com.td.common.pojo.RedPacketConfig;
import com.td.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-06 11:53
 */
@Slf4j
@Service
public class RedPacketConfigServiceImpl implements RedPacketConfigService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void initConfigCache() {
        List<RedPacketConfig> configs = mongoTemplate.findAll(RedPacketConfig.class);
        for (RedPacketConfig config : configs) {
            log.info("config:{}", config);
            redisUtils.setCacheObject(config.getConfigKey(), config.getConfigValue());
        }
    }

    @Override
    public String getConfig(String configKey) {
        Object cacheObject = redisUtils.getCacheObject(configKey);
        if (cacheObject != null) {
            return cacheObject.toString();
        }

        RedPacketConfig packetConfig = mongoTemplate.findOne(new Query(Criteria.where("configKey").is(configKey)), RedPacketConfig.class);
        if (packetConfig != null) {
            redisUtils.setCacheObject(packetConfig.getConfigKey(), packetConfig.getConfigValue());
            return packetConfig.getConfigValue();
        }
        return null;
    }
}
