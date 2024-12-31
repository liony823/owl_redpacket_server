package com.td.common.listener;

import com.td.client.enums.RedisKeyEnums;
import com.td.client.service.RedPacketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Td
 * @date 2023/5/17 16:38
 * @desc redis key 失效监听回调
 */
@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Autowired
    private RedPacketService redPacketService;

    /**
     * @param listenerContainer must not be {@literal null}.
     */
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 使用该方法监听 , 处理 key 失效时执行
     */
    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        String keyExpire = message.toString();
//        log.info("触发 redis key 过期事件  消息：" + message);
//        log.info(keyExpire);

        if (keyExpire.startsWith(RedisKeyEnums.RED_PACKET_PRIVATE.getKey()) ||
                keyExpire.startsWith(RedisKeyEnums.RED_PACKET_EXCLUSIVE.getKey()) ||
                keyExpire.startsWith(RedisKeyEnums.RED_PACKET_LUCK.getKey())
        ) {
            String redPacketId = keyExpire.split(":")[2];
            redPacketService.handlerRedPacketExpire(redPacketId);
        }
    }
}
