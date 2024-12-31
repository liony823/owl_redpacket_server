package com.td.client.service.impl;

import com.td.client.dto.request.ReceiveRedPacketRequestDto;
import com.td.client.dto.response.ReceiveRedPacketResponseDto;
import com.td.client.enums.RedPacketRecordType;
import com.td.client.enums.RedisKeyEnums;
import com.td.client.service.RedPacketReceiveService;
import com.td.common.base.LoginUser;
import com.td.common.exception.CustomException;
import com.td.common.lock.CustomRedisLock;
import com.td.common.pojo.RedPacket;
import com.td.common.pojo.RedPacketBalance;
import com.td.common.pojo.RedPacketRecord;
import com.td.common.pojo.User;
import com.td.common.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-10 16:07
 */
@Service("exclusive_RedPacketReceiveServiceImpl")
public class ExclusiveRedPacketReceiveServiceImpl implements RedPacketReceiveService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CustomRedisLock customRedisLock;

    @Override
    public ReceiveRedPacketResponseDto receiveRedPacket(ReceiveRedPacketRequestDto receiveRedPacketRequestDto) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserid = loginUser.getId();

        RedPacket redPacket = receiveRedPacketRequestDto.getRedPacket();

        if (redPacket == null) {
            throw new CustomException("红包不存在");
        }

        Object redPacketCatch = redisUtils.getCacheObject(RedisKeyEnums.RED_PACKET_EXCLUSIVE.getKey() + receiveRedPacketRequestDto.getRedPacketId());
        if (redPacketCatch == null) {
            throw new CustomException("红包已领完");
        }

        if (!redPacket.getReceiverId().equals(currUserid)) {
            throw new CustomException("您不是该红包的接收者");
        }

        if (redPacket.getStatus() == 1) {
            throw new CustomException("您已领取过该红包");
        }

        //查询红包记录表 判断是否已领取
        Query recordQuery = new Query(
                Criteria.where("red_packet_id").is(receiveRedPacketRequestDto.getRedPacketId())
                        .and("user_id").is(currUserid)
                        .and("record_type").is(RedPacketRecordType.RECEIVE_FROM_GROUP.getCode())
        );

        RedPacketRecord redPacketRecord = mongoTemplate.findOne(recordQuery, RedPacketRecord.class);
        if (redPacketRecord != null) {
            throw new CustomException("您已领取过该红包");
        }


        //领取红包
        double totalAmount = redPacket.getTotalAmount();


        String key = RedisKeyEnums.USER_BALANCE.getKey() + currUserid;
        customRedisLock.tryLock(key);

        //更新用户余额
        Query userQuery = new Query(Criteria.where("user_id").is(currUserid));
        RedPacketBalance redPacketBalance = mongoTemplate.findOne(userQuery, RedPacketBalance.class);

        if (redPacketBalance == null) {
            redPacketBalance = new RedPacketBalance()
                    .setUserId(currUserid)
                    .setBalance(0);
        }

        redPacketBalance.setBalance(redPacketBalance.getBalance() + totalAmount);

        if (StringUtils.hasText(redPacketBalance.getId())) {
            //更新
            mongoTemplate.updateFirst(userQuery, new Update().set("balance", redPacketBalance.getBalance()), RedPacketBalance.class);
        } else {
            //新增
            mongoTemplate.save(redPacketBalance);
        }

        //保存红包记录
        RedPacketRecord record = new RedPacketRecord()
                .setRedPacketId(receiveRedPacketRequestDto.getRedPacketId())
                .setUserId(currUserid)
                .setAmount(totalAmount)
                .setRecordType(RedPacketRecordType.RECEIVE_FROM_GROUP.getCode())
                .setCreateTime(LocalDateTime.now().toString())
                .setSendUserId(redPacket.getSenderId())
                .setRedPacketType(redPacket.getType())
                .setGroupID(redPacket.getGroupID())
                ;

        mongoTemplate.save(record);

        //修改红包状态为1
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(receiveRedPacketRequestDto.getRedPacketId())), new Update().set("status", 1), RedPacket.class);

        //删除缓存
        redisUtils.deleteObject(RedisKeyEnums.RED_PACKET_EXCLUSIVE.getKey() + receiveRedPacketRequestDto.getRedPacketId());

        //查询发送者信息
        User sendUser = mongoTemplate.findOne(new Query(Criteria.where("user_id").is(redPacket.getSenderId())), User.class);

        //查询接收者信息
        User receiveUser = mongoTemplate.findOne(new Query(Criteria.where("user_id").is(currUserid)), User.class);

        return new ReceiveRedPacketResponseDto()
                .setAmount(totalAmount)
                .setRedPacketId(receiveRedPacketRequestDto.getRedPacketId())
                .setCreateTime(record.getCreateTime())
                .setSendUserInfo(sendUser)
                .setReceiveUserInfo(receiveUser)
                ;
    }
}