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
 * @date 2024-08-10 16:06
 */
@Service("exclusive_RedPacketSendServiceImpl")
public class ExclusiveRedPacketSendServiceImpl implements RedPacketSendService {
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

        if (!StringUtils.hasText(sendRedPacketRequestDto.getGroupID())) {
            throw new CustomException("群组id不能为空");
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
                .setType(RedPacketType.EXCLUSIVE.getCode())
                .setGroupID(sendRedPacketRequestDto.getGroupID())
                .setSenderId(currUserId)
                .setReceiverId(sendRedPacketRequestDto.getReceiveUserId())
                .setTotalAmount(sendRedPacketRequestDto.getAmount())
                .setCreateTime(now.toString())
                .setExpireTime(now.plusDays(1).toString())
                .setStatus(0)
                .setTotalCount(1)
                .setEmoji(sendRedPacketRequestDto.getEmoji())
                .setRemark(sendRedPacketRequestDto.getRemark())
                .setId(sendRedPacketRequestDto.getClientMsgID());

        RedPacket packet = mongoTemplate.save(redPacket);

        if (!StringUtils.hasText(packet.getId())) {
            throw new CustomException("发送失败");
        }

        //保存红包记录
        RedPacketRecord redPacketRecord = new RedPacketRecord()
                .setRedPacketId(packet.getId())
                .setAmount(-sendRedPacketRequestDto.getAmount())
                .setRecordType(RedPacketRecordType.SEND_TO_GROUP.getCode())
                .setSendUserId(currUserId)
                .setUserId(currUserId)
                .setGroupID(sendRedPacketRequestDto.getGroupID())
                .setCreateTime(LocalDateTime.now().toString())
                .setRedPacketType(RedPacketType.EXCLUSIVE.getCode());
        RedPacketRecord packetRecord = mongoTemplate.save(redPacketRecord);

        if (!StringUtils.hasText(packetRecord.getId())) {
            customRedisLock.unlock(key);
            throw new CustomException("发送失败");
        }

        redisUtils.setCacheObject(RedisKeyEnums.RED_PACKET_EXCLUSIVE.getKey() + packet.getId(), 1, 24, TimeUnit.HOURS);
        customRedisLock.unlock(key);
        return new SendRedPacketResponseDto().setRedPacketId(packet.getId());
    }
}