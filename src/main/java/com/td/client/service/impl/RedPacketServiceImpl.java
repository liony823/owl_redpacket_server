package com.td.client.service.impl;

import com.td.client.dto.base.ReceiveRedPacketRecordDto;
import com.td.client.dto.request.ReceiveRedPacketRequestDto;
import com.td.client.dto.request.SendRedPacketRequestDto;
import com.td.client.dto.response.ReceiveRedPacketRecordResponseDto;
import com.td.client.dto.response.ReceiveRedPacketResponseDto;
import com.td.client.dto.response.RedPacketStatusResponseDto;
import com.td.client.dto.response.SendRedPacketResponseDto;
import com.td.client.enums.RedPacketRecordType;
import com.td.client.enums.RedPacketType;
import com.td.client.service.RedPacketExpireService;
import com.td.client.service.RedPacketReceiveService;
import com.td.client.service.RedPacketSendService;
import com.td.client.service.RedPacketService;
import com.td.common.base.LoginUser;
import com.td.common.exception.CustomException;
import com.td.common.pojo.RedPacket;
import com.td.common.pojo.RedPacketBalance;
import com.td.common.pojo.RedPacketRecord;
import com.td.common.pojo.User;
import com.td.common.utils.RedisUtils;
import com.td.common.utils.Web3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-03 13:46
 */
@Service
public class RedPacketServiceImpl implements RedPacketService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ApplicationContext beanFactory;

    @Autowired
    private Web3Util web3Util;

    /**
     * 查询红包状态
     *
     * @param redPacketId 红包id
     * @return
     */
    @Override
    public RedPacketStatusResponseDto redPacketStatus(String redPacketId) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //查询当前用户信息
        String currUserId = loginUser.getId();

        RedPacket redPacket = mongoTemplate.findOne(new Query(Criteria.where("id").is(redPacketId)), RedPacket.class);
        if (redPacket == null) {
            throw new CustomException("红包不存在");
        }

        //查询发送的用户信息
        User sendUser = mongoTemplate.findOne(new Query(Criteria.where("user_id").is(redPacket.getSenderId())), User.class);
        if (sendUser == null) {
            throw new CustomException("用户不存在");
        }

        //如果当前用户不是领取者 查询领取者信息
        User receiveUser = null;
        //如果是专属红包 查询接收者信息
        if (RedPacketType.EXCLUSIVE.getCode().equals(redPacket.getType()) || RedPacketType.PRIVATE.getCode().equals(redPacket.getType())) {
            receiveUser = mongoTemplate.findOne(new Query(Criteria.where("user_id").is(redPacket.getReceiverId())), User.class);
        }


        //查询红包记录
        Query query = new Query(Criteria
                .where("red_packet_id").is(redPacketId) //红包id
                .and("user_id").is(currUserId) //用户id
                .and("record_type").in(RedPacketRecordType.RECEIVE_FROM_USER.getCode()
                        , RedPacketRecordType.RECEIVE_FROM_GROUP.getCode())
        );
        RedPacketRecord redPacketRecord = mongoTemplate.findOne(query, RedPacketRecord.class);

        if (redPacketRecord != null) {
            redPacket.setStatus(1);
        } else {
            String expireTimeStr = redPacket.getExpireTime();
            if (StringUtils.hasText(expireTimeStr)) {
                LocalDateTime expireTime = LocalDateTime.parse(expireTimeStr);
                if (expireTime.isBefore(LocalDateTime.now())) {
                    redPacket.setStatus(2);
                }
            }
        }

        return new RedPacketStatusResponseDto()
                .setRedPacketId(redPacket.getId())
                .setStatus(redPacket.getStatus())
                .setRemark(redPacket.getRemark())
                .setExpireTime(redPacket.getExpireTime())
                .setEmoji(redPacket.getEmoji())
                .setSendUserInfo(sendUser)
                .setReceiveUserInfo(receiveUser)
                .setAmount(redPacketRecord == null ? null : redPacketRecord.getAmount())
                .setRedPacketType(redPacket.getType())
                .setEmoji(redPacket.getEmoji())
                .setRemark(redPacket.getRemark())
                ;
    }


    /**
     * 发送红包
     *
     * @param sendRedPacketRequestDto
     */
    @Override
    public SendRedPacketResponseDto sendRedPacket(SendRedPacketRequestDto sendRedPacketRequestDto) {
        if (sendRedPacketRequestDto.getAmount() == null || sendRedPacketRequestDto.getAmount() <= 0) {
            throw new CustomException("红包金额不正确");
        }
        String beanName = sendRedPacketRequestDto.getType() + "_RedPacketSendServiceImpl";
        RedPacketSendService redPacketSendService;

        try {
            redPacketSendService = (RedPacketSendService) beanFactory.getBean(beanName);
        } catch (Exception e) {
            throw new CustomException("红包类型不存在");
        }
        return redPacketSendService.sendRedPacket(sendRedPacketRequestDto);
    }

    /**
     * 领取红包
     *
     * @param receiveRedPacketRequestDto
     */
    @Override
    @Transactional
    public ReceiveRedPacketResponseDto receiveRedPacket(ReceiveRedPacketRequestDto receiveRedPacketRequestDto) {
        //根据红包id查询红包是否存在
        RedPacket redPacket = mongoTemplate.findOne(new Query(Criteria.where("id").is(receiveRedPacketRequestDto.getRedPacketId())), RedPacket.class);

        if (redPacket == null) {
            throw new CustomException("红包不存在");
        }

        String beanName = redPacket.getType() + "_RedPacketReceiveServiceImpl";
        RedPacketReceiveService receiveService;

        try {
            receiveService = (RedPacketReceiveService) beanFactory.getBean(beanName);
        } catch (Exception e) {
            throw new CustomException("红包类型不存在");
        }

        if (redPacket.getStatus() == 2) {
            throw new CustomException("红包已过期");
        }

        String expireTimeStr = redPacket.getExpireTime();
        LocalDateTime expireTime = LocalDateTime.parse(expireTimeStr);
        if (expireTime.isBefore(LocalDateTime.now())) {
            throw new CustomException("红包已过期");
        }

        receiveRedPacketRequestDto.setRedPacket(redPacket);
        return receiveService.receiveRedPacket(receiveRedPacketRequestDto);
    }

    /**
     * 查询余额
     *
     * @return
     */
    @Override
    public String balance() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = loginUser.getId();
        Query query = new Query(Criteria.where("user_id").is(userId));
        RedPacketBalance redPacketBalance = mongoTemplate.findOne(query, RedPacketBalance.class);
        if (redPacketBalance == null) {
            return web3Util.getBalanceHex(0.0);
        } else {
            return web3Util.getBalanceHex(redPacketBalance.getBalance());
        }
    }

    /**
     * 领取红包记录 和 红包状态
     *
     * @param redPacketId
     * @return
     */
    @Override
    public ReceiveRedPacketRecordResponseDto receiveRecord(String redPacketId) {

        RedPacketStatusResponseDto redPacketStatusResponseDto = redPacketStatus(redPacketId);

        //使用红包id 查询红包
        RedPacket redPacket = mongoTemplate.findOne(new Query(Criteria.where("id").is(redPacketId)), RedPacket.class);
        if (redPacket == null) {
            throw new CustomException("红包不存在");
        }

        String senderId = redPacket.getSenderId();
        //查询发送者信息
        User sendUser = mongoTemplate.findOne(new Query(Criteria.where("user_id").is(senderId)), User.class);

        Query query = new Query(Criteria
                .where("red_packet_id").is(redPacketId)
                .and("record_type").regex(".*" + "receive" + ".*")
        );

        //根据红包id查询所有领取记录
        List<RedPacketRecord> redPacketRecords = mongoTemplate.find(query, RedPacketRecord.class);

        List<String> receiveUserIdList = redPacketRecords.stream().map(RedPacketRecord::getUserId).collect(Collectors.toList());

        List<User> userList;
        if (CollectionUtils.isEmpty(receiveUserIdList)) {
            userList = new ArrayList<>();
        } else {
            userList = mongoTemplate.find(new Query(Criteria.where("user_id").in(receiveUserIdList)), User.class);
        }

        Map<String, User> userMap = userList.stream().collect(Collectors.toMap(User::getUserId, user -> user));

        //查询所有领取的用户信息
        List<ReceiveRedPacketRecordDto> receiveRedPacketRecordDtoList = redPacketRecords.stream().map(redPacketRecord -> {
            User user = userMap.get(redPacketRecord.getUserId());
            return new ReceiveRedPacketRecordDto()
                    .setAmount(redPacketRecord.getAmount())
                    .setReceiveTime(redPacketRecord.getCreateTime())
                    .setReceiveUserInfo(user);

        }).collect(Collectors.toList());

        receiveRedPacketRecordDtoList.sort((o1, o2) -> o2.getReceiveTime().compareTo(o1.getReceiveTime()));

        double amount = redPacket.getTotalAmount();
        amount = new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        return new ReceiveRedPacketRecordResponseDto()
                .setSendUserInfo(sendUser)
                .setReceiveRedPacketRecordDtoList(receiveRedPacketRecordDtoList)
                .setAmount(amount)
                .setTotalCount(redPacket.getTotalCount())
                .setRemark(redPacket.getRemark())
                .setEmoji(redPacket.getEmoji())
                .setRedPacketStatus(redPacketStatusResponseDto)
                ;

    }

    public ReceiveRedPacketRecordResponseDto receiveRecord2(String redPacketId) {
        //使用红包id 查询红包
        RedPacket redPacket = mongoTemplate.findOne(new Query(Criteria.where("id").is(redPacketId)), RedPacket.class);
        if (redPacket == null) {
            throw new CustomException("红包不存在");
        }

        String senderId = redPacket.getSenderId();
        //查询发送者信息
        User sendUser = mongoTemplate.findOne(new Query(Criteria.where("user_id").is(senderId)), User.class);

        Query query = new Query(Criteria
                .where("red_packet_id").is(redPacketId)
                .and("record_type").regex(".*" + "receive" + ".*")
        );

        //根据红包id查询所有领取记录
        List<RedPacketRecord> redPacketRecords = mongoTemplate.find(query, RedPacketRecord.class);

        List<String> receiveUserIdList = redPacketRecords.stream().map(RedPacketRecord::getUserId).collect(Collectors.toList());

        List<User> userList;
        if (CollectionUtils.isEmpty(receiveUserIdList)) {
            userList = new ArrayList<>();
        } else {
            userList = mongoTemplate.find(new Query(Criteria.where("user_id").in(receiveUserIdList)), User.class);
        }

        Map<String, User> userMap = userList.stream().collect(Collectors.toMap(User::getUserId, user -> user));

        //查询所有领取的用户信息
        List<ReceiveRedPacketRecordDto> receiveRedPacketRecordDtoList = redPacketRecords.stream().map(redPacketRecord -> {
            User user = userMap.get(redPacketRecord.getUserId());
            return new ReceiveRedPacketRecordDto()
                    .setAmount(redPacketRecord.getAmount())
                    .setReceiveTime(redPacketRecord.getCreateTime())
                    .setReceiveUserInfo(user);

        }).collect(Collectors.toList());

        receiveRedPacketRecordDtoList.sort((o1, o2) -> o2.getReceiveTime().compareTo(o1.getReceiveTime()));

        return new ReceiveRedPacketRecordResponseDto()
                .setSendUserInfo(sendUser)
                .setReceiveRedPacketRecordDtoList(receiveRedPacketRecordDtoList)
                .setAmount(redPacket.getTotalAmount())
                .setTotalCount(redPacket.getTotalCount())
                .setRemark(redPacket.getRemark())
                .setEmoji(redPacket.getEmoji())
                ;

    }

    /**
     * 处理红包过期
     *
     * @param redPacketId
     */
    @Override
    public void handlerRedPacketExpire(String redPacketId) {
        RedPacket redPacket = mongoTemplate.findOne(new Query(Criteria.where("id").is(redPacketId)), RedPacket.class);
        if (redPacket == null || redPacket.getStatus() == 1) {
            return;
        }

        String type = redPacket.getType();
        String beanName = type + "_RedPacketExpireServiceImpl";

        RedPacketExpireService redPacketExpireService;
        try {
            redPacketExpireService = (RedPacketExpireService) beanFactory.getBean(beanName);
        } catch (Exception e) {
            throw new CustomException("红包类型不存在");
        }
        redPacketExpireService.handlerRedPacketExpire(redPacket);

    }

}
