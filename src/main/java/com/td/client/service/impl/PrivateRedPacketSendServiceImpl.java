package com.td.client.service.impl;

import com.mongodb.client.result.UpdateResult;
import com.td.client.dto.request.SendRedPacketRequestDto;
import com.td.client.dto.response.SendRedPacketResponseDto;
import com.td.client.enums.RedPacketRecordType;
import com.td.client.enums.RedPacketType;
import com.td.client.enums.RedisKeyEnums;
import com.td.client.service.RedPacketSendService;
import com.td.common.base.LoginUser;
import com.td.common.exception.CustomException;
import com.td.common.lock.CustomRedisLock;
import com.td.common.pojo.RedPacket;
import com.td.common.pojo.RedPacketBalance;
import com.td.common.pojo.RedPacketRecord;
import com.td.common.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-05 12:01
 */
@Service("private_RedPacketSendServiceImpl")
public class PrivateRedPacketSendServiceImpl implements RedPacketSendService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CustomRedisLock customRedisLock;

    @Override
    @Transactional
    public SendRedPacketResponseDto sendRedPacket(SendRedPacketRequestDto sendRedPacketRequestDto) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserId = loginUser.getId();

        if (currUserId.equals(sendRedPacketRequestDto.getReceiveUserId())) {
            throw new CustomException("不能给自己发红包");
        }

        if (!StringUtils.hasText(sendRedPacketRequestDto.getReceiveUserId())) {
            throw new CustomException("接收者id不能为空");
        }

        //尝试获取自旋锁 防止重复修改
        String key = RedisKeyEnums.USER_BALANCE.getKey() + currUserId;
        customRedisLock.tryLock(key);

        //userid查询用户余额
        Query query = new Query(Criteria.where("user_id").is(currUserId));
        RedPacketBalance redPacketBalance = mongoTemplate.findOne(query, RedPacketBalance.class);

        Double amount = sendRedPacketRequestDto.getAmount();


        if (redPacketBalance == null) {
            customRedisLock.unlock(key);
            throw new CustomException("余额不足");
        }

        Double balance = redPacketBalance.getBalance();

        //判断余额是否足够
        if (balance - amount < 0) {
            customRedisLock.unlock(key);
            throw new CustomException("余额不足");
        }


        //减去发送者的余额
        redPacketBalance.setBalance(redPacketBalance.getBalance() - sendRedPacketRequestDto.getAmount());
        UpdateResult updateResult = mongoTemplate.updateFirst(query, new Update().set("balance", redPacketBalance.getBalance()), RedPacketBalance.class);
        if (updateResult.getModifiedCount() == 0) {
            customRedisLock.unlock(key);
            throw new CustomException("发送失败");
        }


        //创建红包
        LocalDateTime now = LocalDateTime.now();
        RedPacket redPacket = new RedPacket()
                .setTotalAmount(sendRedPacketRequestDto.getAmount())
                .setTotalCount(1)
                .setSenderId(currUserId)
                .setReceiverId(sendRedPacketRequestDto.getReceiveUserId())
                .setType(RedPacketType.PRIVATE.getCode())
                .setGroupID("0")
                .setCreateTime(now.toString())
                .setExpireTime(now.plusDays(1).toString())
                .setStatus(0)
                .setEmoji(sendRedPacketRequestDto.getEmoji())
                .setRemark(sendRedPacketRequestDto.getRemark())
                .setId(sendRedPacketRequestDto.getClientMsgID())
                ;
        RedPacket packet = mongoTemplate.save(redPacket);

        if (!StringUtils.hasText(packet.getId())) {
            customRedisLock.unlock(key);
            throw new CustomException("发送失败");
        }

        //保存红包记录
        RedPacketRecord redPacketRecord = new RedPacketRecord()
                .setRedPacketId(packet.getId())
                .setAmount(-sendRedPacketRequestDto.getAmount())
                .setRecordType(RedPacketRecordType.SEND_TO_USER.getCode())
                .setSendUserId(currUserId)
                .setUserId(sendRedPacketRequestDto.getReceiveUserId())
                .setCreateTime(now.toString())
                .setRedPacketType(RedPacketType.PRIVATE.getCode());
        RedPacketRecord packetRecord = mongoTemplate.save(redPacketRecord);

        if (!StringUtils.hasText(packetRecord.getId())) {
            customRedisLock.unlock(key);
            throw new CustomException("发送失败");
        }

        redisUtils.setCacheObject(RedisKeyEnums.RED_PACKET_PRIVATE.getKey() + packet.getId(), 1, 24, TimeUnit.HOURS);
//        redisUtils.setCacheObject(RedisKeyEnums.RED_PACKET_PRIVATE.getKey() + packet.getId(), 1, 30, TimeUnit.SECONDS);

        customRedisLock.unlock(key);

        return new SendRedPacketResponseDto()
                .setRedPacketId(packet.getId());
    }
}
