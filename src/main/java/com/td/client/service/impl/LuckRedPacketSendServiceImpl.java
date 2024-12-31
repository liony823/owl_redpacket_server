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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-10 16:55
 */
@Service("luck_RedPacketSendServiceImpl")
public class LuckRedPacketSendServiceImpl implements RedPacketSendService {
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

        if (sendRedPacketRequestDto.getTotalCount() <= 0) {
            throw new CustomException("红包个数不能小于1");
        }

        if (!StringUtils.hasText(sendRedPacketRequestDto.getGroupID())) {
            throw new CustomException("群组id不能为空");
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
                .setTotalCount(sendRedPacketRequestDto.getTotalCount())
                .setSenderId(currUserId)
                .setReceiverId(null)
                .setType(RedPacketType.LUCK.getCode())
                .setGroupID(sendRedPacketRequestDto.getGroupID())
                .setCreateTime(now.toString())
                .setExpireTime(now.plusDays(1).toString())
                .setStatus(0)
                .setEmoji(sendRedPacketRequestDto.getEmoji())
                .setRemark(sendRedPacketRequestDto.getRemark())
                .setId(sendRedPacketRequestDto.getClientMsgID());
        RedPacket packet = mongoTemplate.save(redPacket);

        if (!StringUtils.hasText(packet.getId())) {
            customRedisLock.unlock(key);
            throw new CustomException("发送失败");
        }

        //保存红包记录
        RedPacketRecord redPacketRecord = new RedPacketRecord()
                .setRedPacketId(packet.getId())
                .setAmount(-sendRedPacketRequestDto.getAmount())
                .setRecordType(RedPacketRecordType.SEND_TO_GROUP.getCode())
                .setSendUserId(currUserId)
                .setUserId(null)
                .setGroupID(sendRedPacketRequestDto.getGroupID())
                .setCreateTime(now.toString())
                .setRedPacketType(RedPacketType.LUCK.getCode());
        RedPacketRecord packetRecord = mongoTemplate.save(redPacketRecord);

        if (!StringUtils.hasText(packetRecord.getId())) {
            customRedisLock.unlock(key);
            throw new CustomException("发送失败");
        }

        redisUtils.setCacheObject(RedisKeyEnums.RED_PACKET_LUCK.getKey() + packet.getId(), 1, 24, TimeUnit.HOURS);

        List<BigDecimal> redPacketMoney = calculateRedPackets(sendRedPacketRequestDto.getAmount(), sendRedPacketRequestDto.getTotalCount());
        for (int i = 0; i < redPacketMoney.size(); i++) {
            redisUtils.setCacheObject(RedisKeyEnums.RED_PACKET_RECEIVE.getKey() + packet.getId() + ":" + i, redPacketMoney.get(i), 24, TimeUnit.HOURS);
        }

        customRedisLock.unlock(key);
        return new SendRedPacketResponseDto().setRedPacketId(packet.getId());
    }

    public static List<BigDecimal> calculateRedPackets(double totalAmount, int totalCount) {
        List<BigDecimal> result = new ArrayList<>();
        Random random = new Random();

        // 初始的平均金额
        BigDecimal averageAmount = BigDecimal.valueOf(totalAmount).divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);

        // 避免最后一个红包调整过大
        BigDecimal remainingAmount = BigDecimal.valueOf(totalAmount);
        BigDecimal adjustmentRange = averageAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        for (int i = 0; i < totalCount - 1; i++) {
            // 确保每个红包至少有0.01元，并在合理范围内调整
            BigDecimal maxAmount = remainingAmount.subtract(BigDecimal.valueOf(0.01).multiply(BigDecimal.valueOf(totalCount - i - 1)));
            BigDecimal amount = averageAmount.add(adjustmentRange.multiply(BigDecimal.valueOf(random.nextDouble() * 2 - 1))).setScale(2, RoundingMode.HALF_UP);

            // 确保金额不会小于0.01元
            if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                amount = BigDecimal.valueOf(0.01);
            }

            // 确保金额不超过可用的最大金额
            amount = amount.min(maxAmount).setScale(2, RoundingMode.HALF_UP);

            result.add(amount);
            remainingAmount = remainingAmount.subtract(amount);
        }

        // 最后一个红包分配剩余的所有金额
        result.add(remainingAmount.setScale(2, RoundingMode.HALF_UP));

        // 打乱顺序增加随机性
        Collections.shuffle(result);

        // 判断如果有红包金额小于0.01元，则重新计算
        if (result.stream().anyMatch(amount -> amount.compareTo(BigDecimal.valueOf(0.01)) < 0)) {
            return calculateRedPackets(totalAmount, totalCount);
        }

        return result;
    }
}