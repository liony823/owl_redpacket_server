package com.td.client.service.impl;

import com.td.client.dto.base.FriendDto;
import com.td.client.dto.request.RechargeRequestDto;
import com.td.client.dto.request.TransactionRecordRequestDto;
import com.td.client.dto.request.WithdrawRequestDto;
import com.td.client.dto.response.TransactionRecordPageResponseDto;
import com.td.client.dto.response.TransactionRecordResponseDto;
import com.td.client.enums.RedPacketRecordType;
import com.td.client.enums.RedPacketType;
import com.td.client.enums.RedisKeyEnums;
import com.td.client.service.RedPacketConfigService;
import com.td.client.service.TransactionService;
import com.td.common.base.LoginUser;
import com.td.common.base.PageRequest;
import com.td.common.exception.CustomException;
import com.td.common.lock.CustomRedisLock;
import com.td.common.pojo.*;
import com.td.common.utils.RedisUtils;
import com.td.common.utils.SecurityUtils;
import com.td.common.utils.Web3Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-03 13:46
 */
@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private RedPacketConfigService redPacketConfigService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CustomRedisLock customRedisLock;

    @Autowired
    private Web3Util web3Util;

    @Override
    @Transactional
    public void recharge(RechargeRequestDto rechargeRequestDto) {
        if (rechargeRequestDto.getAmount() == null || rechargeRequestDto.getAmount() <= 0) {
            throw new CustomException("充值金额不正确");
        }

        if (!StringUtils.hasText(rechargeRequestDto.getTxHash())) {
            throw new CustomException("交易hash不能为空");
        }

        LoginUser loginUser = SecurityUtils.getLoginUser();
        String currUserId = loginUser.getId();

        //查询hash是否已经存在
        RedPacketRecord redPacketRecord = mongoTemplate.findOne(new Query(Criteria.where("transaction_hash").is(rechargeRequestDto.getTxHash())), RedPacketRecord.class);

        if (redPacketRecord != null) {
            throw new CustomException("交易hash已存在");
        }


        //创建充值记录
        redPacketRecord = new RedPacketRecord()
                .setUserId("")
                .setAmount(rechargeRequestDto.getAmount())
                .setRecordType(RedPacketRecordType.RECHARGE.getCode())
                .setRedPacketId("")
                .setCreateTime(LocalDateTime.now().toString())
                .setTransactionHash(rechargeRequestDto.getTxHash())
                .setRechargeStatus(2)
                .setGroupID("")
                .setSendUserId(currUserId)
                .setCurrency("OWL")
        ;

        mongoTemplate.save(redPacketRecord);
    }

    @Override
    public String getAddress() {
        return redPacketConfigService.getConfig("center_server_address");
    }

    @Override
    @Transactional
    public void withdraw(WithdrawRequestDto withdrawRequestDto) {
        Double amount = withdrawRequestDto.getAmount();
//        if (amount == null || amount < 100) {
//            throw new CustomException("最小提现金额为100");
//        }
        if (amount == null) {
            throw new CustomException("最小提现金额为100");
        }

        String userId = SecurityUtils.getLoginUser().getId();

        //根据user_id 查询用户属性表 拿到收款地址
        Attribute attribute = mongoTemplate.findOne(new Query(Criteria.where("user_id").is(userId)), Attribute.class);
        if (attribute == null) {
            throw new CustomException("用户不存在");
        }

        String toAddress = attribute.getAddress();

        if (!StringUtils.hasText(toAddress)) {
            throw new CustomException("收款地址为空");
        }
        String key = RedisKeyEnums.USER_BALANCE.getKey() + userId;

        customRedisLock.tryLock(key);

        RedPacketBalance userBalance = mongoTemplate.findOne(new Query(Criteria.where("user_id").is(userId)), RedPacketBalance.class);
        if (userBalance == null || userBalance.getBalance() - amount < 0) {
            throw new CustomException("余额不足");
        }

        userBalance.setBalance(userBalance.getBalance() - amount);
        mongoTemplate.updateFirst(new Query(Criteria.where("user_id").is(userId)), new Update().set("balance", userBalance.getBalance()), RedPacketBalance.class);
        String txHash;
        try {
            txHash = web3Util.withdrawOWL(toAddress, amount);
        } catch (Exception e) {
            throw new CustomException("中心服务器 RPC 调用失败");
        }

        //如果交易hash为空
        if (!StringUtils.hasText(txHash)) {
            throw new CustomException("提现失败");
        }

        //保存交易记录
        RedPacketRecord redPacketRecord = new RedPacketRecord()
                .setAmount(-amount)
                .setRecordType(RedPacketRecordType.WITHDRAW.getCode())
                .setSendUserId(userId)
                .setGroupID("")
                .setCreateTime(LocalDateTime.now().toString())
                .setRechargeStatus(2)
                .setTransactionHash(txHash)
                .setRedPacketId("")
                .setUserId("")
                .setToAddress(toAddress);

        mongoTemplate.save(redPacketRecord);
    }

    @Override
    public TransactionRecordPageResponseDto record(PageRequest<TransactionRecordRequestDto> pageRequest) {
        int pageSize = pageRequest.getPageSize() <= 0 ? 10 : pageRequest.getPageSize();
        long pageNum = pageRequest.getPageNumber() <= 0 ? 1 : pageRequest.getPageNumber();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String userId = loginUser.getId();
        //查询send_user_id 或 user_id 为当前用户的记录
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("user_id").is(userId),
                Criteria.where("send_user_id").is(userId)
        ));

        if (StringUtils.hasText(pageRequest.getData().getRecordType())) {
//
            if (RedPacketRecordType.RECHARGE.getCode().equals(pageRequest.getData().getRecordType())
                    || RedPacketRecordType.WITHDRAW.getCode().equals(pageRequest.getData().getRecordType())
            ) {
                query.addCriteria(Criteria.where("record_type").is(pageRequest.getData().getRecordType()));
            }

            //领取和发送
            if ("receive".equals(pageRequest.getData().getRecordType())) {
                query.addCriteria(Criteria.where("record_type").in(RedPacketRecordType.RECEIVE_FROM_USER.getCode(), RedPacketRecordType.RECEIVE_FROM_GROUP.getCode()));
            }

            if ("send".equals(pageRequest.getData().getRecordType())) {
                query.addCriteria(Criteria.where("record_type").in(RedPacketRecordType.SEND_TO_USER.getCode(), RedPacketRecordType.SEND_TO_GROUP.getCode()));
            }

            //退款
            if ("refund".equals(pageRequest.getData().getRecordType())) {
                query.addCriteria(Criteria.where("record_type").is(RedPacketRecordType.REFUND.getCode()));
            }
        }
        //通过create_time 倒序排序
        query.with(Sort.by(Sort.Order.desc("create_time")));

        //查询总条数
        long total = mongoTemplate.count(query, RedPacketRecord.class);
        //分页查询
        query.skip((pageNum - 1) * pageSize).limit(pageSize);
        List<RedPacketRecord> redPacketRecords = mongoTemplate.find(query, RedPacketRecord.class);

        //查询该用户的所有朋友信息
        Query friendQuery = new Query(Criteria.where("owner_user_id").is(userId));
        List<Friend> friends = mongoTemplate.find(friendQuery, Friend.class);

        //ownerUserId-friendUserId 作为key friend作为value
        Map<String, Friend> friendMap = friends.stream().collect(Collectors.toMap(item -> item.getOwnerUserId() + "-" + item.getFriendUserId(), Function.identity()));

        //如果查询结果为空
        if (CollectionUtils.isEmpty(redPacketRecords)) {
            return new TransactionRecordPageResponseDto()
                    .setTotalCount(0)
                    .setRecord(new ArrayList<>())
                    .setPageSize(pageSize)
                    .setPageNum(pageNum);
        }

        //拿到所有不为空的send_user_id 和 user_id 并去重
        Set<String> userIds = new HashSet<>();
        Set<String> groupIds = new HashSet<>();
        redPacketRecords.forEach(item -> {
            if (StringUtils.hasText(item.getSendUserId())) {
                userIds.add(item.getSendUserId());
            }
            if (StringUtils.hasText(item.getUserId())) {
                userIds.add(item.getUserId());
            }
            if (StringUtils.hasText(item.getGroupID())) {
                groupIds.add(item.getGroupID());
            }
        });

        //根据user_id查询用户信息
        Query userQuery = new Query(Criteria.where("user_id").in(userIds));
        Map<String, User> userMap = mongoTemplate.find(userQuery, User.class).stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        //根据group_id查询群信息
        Map<String, Group> groupMap = mongoTemplate.find(new Query(Criteria.where("group_id").in(groupIds)), Group.class).stream().collect(Collectors.toMap(Group::getGroupID, Function.identity()));


        //充值记录
        List<RedPacketRecord> rechargeRecords = new ArrayList<>();
        //提现记录
        List<RedPacketRecord> withdrawRecords = new ArrayList<>();
        //退款记录
        List<RedPacketRecord> refundRecords = new ArrayList<>();
        //领取个人红包记录
        List<RedPacketRecord> receivePrivateRecords = new ArrayList<>();
        //发送个人红包记录
        List<RedPacketRecord> sendPrivateRecords = new ArrayList<>();
        //领取群红包记录
        List<RedPacketRecord> receiveGroupRecords = new ArrayList<>();
        //发送群红包记录
        List<RedPacketRecord> sendGroupRecords = new ArrayList<>();

        for (RedPacketRecord redPacketRecord : redPacketRecords) {
            //充值记录
            if (RedPacketRecordType.RECHARGE.getCode().equals(redPacketRecord.getRecordType())) {
                rechargeRecords.add(redPacketRecord);
                continue;
            }

            //提现记录
            if (RedPacketRecordType.WITHDRAW.getCode().equals(redPacketRecord.getRecordType())) {
                withdrawRecords.add(redPacketRecord);
                continue;
            }

            //退款记录
            if (RedPacketRecordType.REFUND.getCode().equals(redPacketRecord.getRecordType())) {
                refundRecords.add(redPacketRecord);
                continue;
            }

            //领取个人红包记录
            if (RedPacketType.PRIVATE.getCode().equals(redPacketRecord.getRedPacketType())
                    && RedPacketRecordType.RECEIVE_FROM_USER.getCode().equals(redPacketRecord.getRecordType())) {
                receivePrivateRecords.add(redPacketRecord);
                continue;
            }

            //发送个人红包记录
            if (RedPacketType.PRIVATE.getCode().equals(redPacketRecord.getRedPacketType())
                    && RedPacketRecordType.SEND_TO_USER.getCode().equals(redPacketRecord.getRecordType())) {
                sendPrivateRecords.add(redPacketRecord);
                continue;
            }

            //领取群红包记录
            if (RedPacketRecordType.RECEIVE_FROM_GROUP.getCode().equals(redPacketRecord.getRecordType())) {
                receiveGroupRecords.add(redPacketRecord);
                continue;
            }

            //发送群红包记录
            if (RedPacketRecordType.SEND_TO_GROUP.getCode().equals(redPacketRecord.getRecordType())) {
                sendGroupRecords.add(redPacketRecord);
            }
        }

        ArrayList<TransactionRecordResponseDto> records = new ArrayList<>();
        //充值记录
        rechargeRecords.forEach(item -> {

            //如果send_user_id和当前用户id不相同 则不显示
            if (!userId.equals(item.getSendUserId())) {
                return;
            }

            String createTime = item.getCreateTime();
            //如果createTime不为空 将其格式化为yyyy-MM-dd HH:mm
            if (StringUtils.hasText(createTime)) {
                //将时间格式化为yyyy-MM-dd HH:mm
                try {
                    createTime = LocalDateTime.parse(createTime).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    createTime = "";
                }
            }
            records.add(new TransactionRecordResponseDto()
                    .setCreateTime(createTime)
                    .setAmount("+" + item.getAmount() + " OWL")
                    .setType(RedPacketRecordType.RECHARGE.getCode())
                    .setTxHash(item.getTransactionHash())
            );
        });

        //提现记录
        withdrawRecords.forEach(item -> {
            //如果send_user_id和当前用户id不相同 则不显示
            if (!userId.equals(item.getSendUserId())) {
                return;
            }
            String createTime = item.getCreateTime();
            //如果createTime不为空 将其格式化为yyyy-MM-dd HH:mm
            if (StringUtils.hasText(createTime)) {
                //将时间格式化为yyyy-MM-dd HH:mm
                try {
                    createTime = LocalDateTime.parse(createTime).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    createTime = "";
                }
            }
            records.add(new TransactionRecordResponseDto()
                    .setCreateTime(createTime)
                    .setAmount(item.getAmount() + " OWL")
                    .setType(RedPacketRecordType.WITHDRAW.getCode())
                    .setTxHash(item.getTransactionHash())
            );
        });

        //退款记录
        refundRecords.forEach(item -> {
            //如果send_user_id和当前用户id不相同 则不显示
            if (!userId.equals(item.getSendUserId())) {
                return;
            }
            String createTime = item.getCreateTime();
            //如果createTime不为空 将其格式化为yyyy-MM-dd HH:mm
            if (StringUtils.hasText(createTime)) {
                //将时间格式化为yyyy-MM-dd HH:mm
                try {
                    createTime = LocalDateTime.parse(createTime).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    createTime = "";
                }
            }
            records.add(new TransactionRecordResponseDto()
                    .setCreateTime(createTime)
                    .setAmount("+" + item.getAmount() + " OWL")
                    .setType(RedPacketRecordType.REFUND.getCode())
            );
        });

        //领取个人红包记录
        receivePrivateRecords.forEach(item -> {
            if (!userId.equals(item.getUserId())) {
                return;
            }

            User user = userMap.get(item.getSendUserId());
            if (user == null) {
                return;
            }
            FriendDto friendDto = new FriendDto();
            BeanUtils.copyProperties(user, friendDto);
            Friend friendInfo = friendMap.get(userId + "-" + item.getUserId());
            if (friendInfo != null) {
                friendDto.setFriendInfo(friendInfo);
            }
            String createTime = item.getCreateTime();
            //如果createTime不为空 将其格式化为yyyy-MM-dd HH:mm
            if (StringUtils.hasText(createTime)) {
                //将时间格式化为yyyy-MM-dd HH:mm
                try {
                    createTime = LocalDateTime.parse(createTime).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    createTime = "";
                }
            }
            records.add(new TransactionRecordResponseDto()
                    .setCreateTime(createTime)
                    .setAmount("+" + item.getAmount() + " OWL")
                    .setType(RedPacketRecordType.RECEIVE_FROM_USER.getCode())
                    .setFriend(friendDto)
            );
        });

        //发送个人红包记录
        sendPrivateRecords.forEach(item -> {
            //如果send_user_id和当前用户id不相同 则不显示
            if (!userId.equals(item.getSendUserId())) {
                return;
            }
            User user = userMap.get(item.getUserId());
            if (user == null) {
                return;
            }
            FriendDto friendDto = new FriendDto();
            BeanUtils.copyProperties(user, friendDto);
            Friend friendInfo = friendMap.get(userId + "-" + item.getUserId());
            if (friendInfo != null) {
                friendDto.setFriendInfo(friendInfo);
            }
            String createTime = item.getCreateTime();
            //如果createTime不为空 将其格式化为yyyy-MM-dd HH:mm
            if (StringUtils.hasText(createTime)) {
                //将时间格式化为yyyy-MM-dd HH:mm
                try {
                    createTime = LocalDateTime.parse(createTime).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    createTime = "";
                }
            }
            records.add(new TransactionRecordResponseDto()
                    .setCreateTime(createTime)
                    .setAmount(item.getAmount() + " OWL")
                    .setType(RedPacketRecordType.SEND_TO_USER.getCode())
                    .setFriend(friendDto)
            );
        });

        //发送群红包记录
        sendGroupRecords.forEach(item -> {
            RedPacketType redPacketType = RedPacketType.getEnumByCode(item.getRedPacketType());
            if (redPacketType == null) {
                return;
            }

            Group group = groupMap.get(item.getGroupID());
            if (group == null) {
                return;
            }
            FriendDto friendDto = new FriendDto();
            User user = userMap.get(item.getSendUserId());
            if (user == null) {
                return;
            }
            BeanUtils.copyProperties(user, friendDto);
            Friend friendInfo = friendMap.get(userId + "-" + item.getSendUserId());
            if (friendInfo != null) {
                friendDto.setFriendInfo(friendInfo);
            }
            String createTime = item.getCreateTime();
            //如果createTime不为空 将其格式化为yyyy-MM-dd HH:mm
            if (StringUtils.hasText(createTime)) {
                //将时间格式化为yyyy-MM-dd HH:mm
                try {
                    createTime = LocalDateTime.parse(createTime).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    createTime = "";
                }
            }

            records.add(new TransactionRecordResponseDto()
                    .setCreateTime(createTime)
                    .setAmount(item.getAmount() + " OWL")
                    .setType(RedPacketRecordType.SEND_TO_GROUP.getCode() + "_" + redPacketType.getCode())
                    .setGroup(group)
                    .setFriend(friendDto)
            );
        });

        //领取群红包记录
        receiveGroupRecords.forEach(item -> {
            RedPacketType redPacketType = RedPacketType.getEnumByCode(item.getRedPacketType());
            if (redPacketType == null) {
                return;
            }
            Group group = groupMap.get(item.getGroupID());
            if (group == null) {
                return;
            }
            String createTime = item.getCreateTime();
            //如果createTime不为空 将其格式化为yyyy-MM-dd HH:mm
            if (StringUtils.hasText(createTime)) {
                //将时间格式化为yyyy-MM-dd HH:mm
                try {
                    createTime = LocalDateTime.parse(createTime).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    createTime = "";
                }
            }
            //单独处理专属红包
            if (RedPacketType.EXCLUSIVE.getCode().equals(item.getRedPacketType())) {
                User user = userMap.get(item.getSendUserId());
                if (user == null) {
                    return;
                }
                FriendDto friendDto = new FriendDto();
                BeanUtils.copyProperties(user, friendDto);
                Friend friendInfo = friendMap.get(userId + "-" + item.getSendUserId());
                if (friendInfo != null) {
                    friendDto.setFriendInfo(friendInfo);
                }
                records.add(new TransactionRecordResponseDto()
                        .setCreateTime(createTime)
                        .setAmount("+" + item.getAmount() + " OWL")
                        .setType(RedPacketRecordType.RECEIVE_FROM_GROUP.getCode() + "_" + redPacketType.getCode())
                        .setFriend(friendDto)
                        .setGroup(group)
                );
                return;
            }


            records.add(new TransactionRecordResponseDto()
                    .setCreateTime(createTime)
                    .setAmount("+" + item.getAmount() + " OWL")
                    .setType(RedPacketRecordType.RECEIVE_FROM_GROUP.getCode() + "_" + redPacketType.getCode())
                    .setGroup(group)
            );
        });

        //将records中的数据按时间倒序排序
        records.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));

        return new TransactionRecordPageResponseDto()
                .setTotalCount(total)
                .setRecord(records)
                .setPageSize(pageSize)
                .setPageNum(pageNum);

    }
}
