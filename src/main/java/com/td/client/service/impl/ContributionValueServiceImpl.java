package com.td.client.service.impl;

import com.td.client.dto.request.ExchangeRequestDto;
import com.td.client.dto.response.ContributionValueOnlineTimeResponseDto;
import com.td.client.dto.response.ContributionValueSignRecordResponseDto;
import com.td.client.dto.response.ContributionValueTaskStatusResponseDto;
import com.td.client.enums.ContributionValueRecordType;
import com.td.client.enums.ContributionValueSignRecordType;
import com.td.client.enums.RedisKeyEnums;
import com.td.client.service.ContributionValueService;
import com.td.common.base.LoginUser;
import com.td.common.exception.CustomException;
import com.td.common.lock.CustomRedisLock;
import com.td.common.pojo.*;
import com.td.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-07 10:00
 */
@Slf4j
@Service
public class ContributionValueServiceImpl implements ContributionValueService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CustomRedisLock customRedisLock;


    /**
     * 签到
     *
     * @return
     */
    @Override
    @Transactional
    public Boolean sign() {

        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserid = loginUser.getId();

        LocalDate now = LocalDate.now();

        String key = RedisKeyEnums.SIGN.getKey() + currUserid + ":" + now.toString();

        String isSign = redisUtils.getCacheObject(key);

        if (StringUtils.hasText(isSign)) {
            throw new CustomException("今日已签到");
        }

        //根据now查询是否已签到
        Query signQuery = new Query(
                Criteria.where("user_id").is(currUserid)
                        .and("sign_date").is(now.toString())
        );
        ContributionValueSignRecord signRecord = mongoTemplate.findOne(signQuery, ContributionValueSignRecord.class);

        if (signRecord != null) {
            throw new CustomException("今日已签到");
        }

        signRecord = new ContributionValueSignRecord()
                .setUserId(currUserid)
                .setSignType(ContributionValueRecordType.CONTINUOUS_SIGN_IN.getCode())
                .setSignDate(now.toString())
                .setCreateTime(LocalDateTime.now().toString())
        ;
        //当前日期
        LocalDate today = LocalDate.now();

        //拿到六天前的日期
        LocalDate sixDaysAgo = today.minusDays(6);

        //查询类型为normal_sign_in 并且日期大于等于sixDaysAgo的记录
        Query normalQuery = new Query(
                Criteria.where("user_id").is(currUserid)
                        .and("sign_type").is(ContributionValueSignRecordType.NORMAL_SIGN_IN.getCode())
                        .and("sign_date").gte(sixDaysAgo.toString())
        )
                .with(Sort.by(Sort.Order.desc("sign_date"))
                );

        //查询count
//        long normalSignCount = mongoTemplate.count(normalQuery, ContributionValueSignRecord.class);
//
//        //如果count等于6 则为连续签到
//        if (normalSignCount == 6) {
//            signRecord.setSignType(ContributionValueSignRecordType.CONTINUOUS_SIGN_IN.getCode());
//        } else {
//            signRecord.setSignType(ContributionValueSignRecordType.NORMAL_SIGN_IN.getCode());
//        }

        //查询normalQuery的记录
        List<ContributionValueSignRecord> normalSignRecords = mongoTemplate.find(normalQuery, ContributionValueSignRecord.class);

        System.out.println(normalSignRecords);


        LocalDate currDate = LocalDate.now();
        int normalSignCount = 0;

        for (int i = 0; i < normalSignRecords.size(); i++) {
            ContributionValueSignRecord item = normalSignRecords.get(i);
            LocalDate signDate = LocalDate.parse(item.getSignDate());
            if (currDate.minusDays(1).equals(signDate)) {
                normalSignCount++;
                currDate = signDate;
            } else {
                break;
            }
        }

        String configKey;
        //如果count等于6 则为连续签到
        if (normalSignCount == 6) {
            signRecord.setSignType(ContributionValueSignRecordType.CONTINUOUS_SIGN_IN.getCode());
            configKey = ContributionValueRecordType.CONTINUOUS_SIGN_IN.getCode();
        } else {
            signRecord.setSignType(ContributionValueSignRecordType.NORMAL_SIGN_IN.getCode());
            configKey = ContributionValueSignRecordType.NORMAL_SIGN_IN.getCode() + ":" + (normalSignCount + 1);
        }

        //保存签到记录
        mongoTemplate.save(signRecord);


        //查询贡献值配置表 获取签到奖励
        Query configQuery = new Query(
                Criteria.where("configKey").is(configKey)
        );
        ContributionValueConfig config = mongoTemplate.findOne(configQuery, ContributionValueConfig.class);

        if (config == null) {
            throw new CustomException("签到配置不存在 请联系管理员");
        }

        ContributionValueRecord contributionValueRecord = new ContributionValueRecord()
                .setUserId(currUserid)
                .setType(signRecord.getSignType())
                .setContributionValueChange(config.getConfigValue())
                .setCreateTime(LocalDateTime.now().toString());

        //保存贡献值记录
        mongoTemplate.save(contributionValueRecord);

        String lockKey = RedisKeyEnums.CONTRIBUTION_VALUE_BALANCE.getKey() + currUserid;
        customRedisLock.spinLock(lockKey);

        //根据用户id查询用户贡献值余额
        Query balanceQuery = new Query(
                Criteria.where("user_id").is(currUserid)
        );
        ContributionValueBalance balance = mongoTemplate.findOne(balanceQuery, ContributionValueBalance.class);

        if (balance == null) {
            balance = new ContributionValueBalance()
                    .setUserId(currUserid)
                    .setBalance(0);
            mongoTemplate.save(balance);
        }

        balance.setBalance(balance.getBalance() + Double.parseDouble(config.getConfigValue()));

        //更新用户贡献值余额
        mongoTemplate.updateFirst(balanceQuery, new Update().set("balance", balance.getBalance()), ContributionValueBalance.class);

        //在redis中记录签到
        redisUtils.setCacheObject(key, "1", 24, TimeUnit.HOURS);
        return true;
    }

    @Override
    public List<ContributionValueRecord> record() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserid = loginUser.getId();

        Query query = new Query(
                Criteria.where("user_id").is(currUserid)
        ).with(Sort.by(Sort.Order.desc("create_time")));

        return mongoTemplate.find(query, ContributionValueRecord.class);
    }

    @Override
    public Double balance() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserid = loginUser.getId();

        Query query = new Query(
                Criteria.where("user_id").is(currUserid)
        );

        ContributionValueBalance balance = mongoTemplate.findOne(query, ContributionValueBalance.class);

        if (balance == null) {
            return 0.0;
        }

        return balance.getBalance();
    }

    @Override
    public Double getBalanceByUserId(String userId) {
        Query query = new Query(
                Criteria.where("user_id").is(userId)
        );

        ContributionValueBalance balance = mongoTemplate.findOne(query, ContributionValueBalance.class);

        if (balance == null) {
            return 0.0;
        }

        return balance.getBalance();
    }

    @Override
    public ContributionValueSignRecordResponseDto signRecord() {
        //拿到当前年份和月份
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserid = loginUser.getId();

        String key = year + "-" + (month < 10 ? "0" + month : month);
        System.out.println(key);

        //查询此用户本月签到记录
        Query query = new Query(
                Criteria.where("user_id").is(currUserid)
                        .and("sign_date").regex("^" + key)
        ).with(Sort.by(Sort.Order.asc("sign_date")));

        List<ContributionValueSignRecord> signRecords = mongoTemplate.find(query, ContributionValueSignRecord.class);

        ContributionValueSignRecordResponseDto responseDto = new ContributionValueSignRecordResponseDto()
                .setTodayIsSign(false)
                .setSignRecord(signRecords);

        for (ContributionValueSignRecord item : signRecords) {
            if (now.toString().equals(item.getSignDate())) {
                responseDto.setTodayIsSign(true);
            }
        }

        return responseDto;
    }

    @Override
    public String exchangeRate() {
        //查询贡献值配置表 获取签到奖励
        Query configQuery = new Query(
                Criteria.where("configKey").is(ContributionValueRecordType.OWL_EXCHANGE_RATE.getCode())
        );
        ContributionValueConfig config = mongoTemplate.findOne(configQuery, ContributionValueConfig.class);

        if (config == null) {
            throw new CustomException("汇率配置不存在 请联系管理员");
        }

        return config.getConfigValue();
    }

    @Override
    @Transactional
    public Boolean exchange(ExchangeRequestDto exchangeRequestDto) {

        if (exchangeRequestDto.getCount() == null) {
            throw new CustomException("兑换数量不能为空");
        }

        if (exchangeRequestDto.getCount() <= 0) {
            throw new CustomException("兑换数量不能小于0");
        }

        Query configQuery = new Query(
                Criteria.where("configKey").is(ContributionValueRecordType.OWL_EXCHANGE_RATE.getCode())
        );
        ContributionValueConfig config = mongoTemplate.findOne(configQuery, ContributionValueConfig.class);

        if (config == null) {
            throw new CustomException("汇率配置不存在 请联系管理员");
        }

        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserid = loginUser.getId();

        String lockKey = RedisKeyEnums.CONTRIBUTION_VALUE_BALANCE.getKey() + currUserid;
        customRedisLock.spinLock(lockKey);

        //查询用户贡献值余额
        Query balanceQuery = new Query(
                Criteria.where("user_id").is(currUserid)
        );
        ContributionValueBalance balance = mongoTemplate.findOne(balanceQuery, ContributionValueBalance.class);

        //需要的贡献值
        double exchangeValue = exchangeRequestDto.getCount() * Double.parseDouble(config.getConfigValue());

        if (balance == null || balance.getBalance() < exchangeValue) {
            throw new CustomException("贡献值余额不足");
        }

        String lockKey2 = RedisKeyEnums.USER_BALANCE.getKey() + currUserid;
        customRedisLock.spinLock(lockKey2);

        //根据用户id查询用户红包余额
        Query query = new Query(
                Criteria.where("user_id").is(currUserid)
        );
        RedPacketBalance redPacketBalance = mongoTemplate.findOne(query, RedPacketBalance.class);

        if (redPacketBalance == null) {
            redPacketBalance = new RedPacketBalance()
                    .setUserId(currUserid)
                    .setBalance(0);
            mongoTemplate.save(redPacketBalance);
        }

        redPacketBalance.setBalance(redPacketBalance.getBalance() + exchangeRequestDto.getCount());

        //更新用户红包余额
        mongoTemplate.updateFirst(query, new Update().set("balance", redPacketBalance.getBalance()), RedPacketBalance.class);

        //更新用户贡献值余额
        balance.setBalance(balance.getBalance() - exchangeValue);
        mongoTemplate.updateFirst(balanceQuery, new Update().set("balance", balance.getBalance()), ContributionValueBalance.class);

        //保存兑换记录
        ContributionValueRecord contributionValueRecord = new ContributionValueRecord()
                .setUserId(currUserid)
                .setType(ContributionValueRecordType.EXCHANGE_OWL.getCode())
                .setContributionValueChange(exchangeValue + "")
                .setOwlCount(exchangeRequestDto.getCount() + "")
                .setCreateTime(LocalDateTime.now().toString());
        mongoTemplate.save(contributionValueRecord);

        return true;
    }

    @Override
    public ContributionValueOnlineTimeResponseDto onlineTime() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserid = loginUser.getId();

        LocalDate now = LocalDate.now();
        String key = RedisKeyEnums.ONLINE_TIME.getKey() + now.toString() + ":" + currUserid;
        Object onlineTime = redisUtils.getCacheObject(key);
        Long todayOnlineTime = onlineTime == null ? 0 : Long.parseLong(onlineTime.toString());

        //根据用户id查询用户总在线时长
        Query query = new Query(
                Criteria.where("user_id").is(currUserid)
        );
        ContributionValueOnline contributionValueOnline = mongoTemplate.findOne(query, ContributionValueOnline.class);


        return new ContributionValueOnlineTimeResponseDto()
                .setTodayOnlineTime(todayOnlineTime)
                .setTotalOnlineTime(contributionValueOnline == null ? 0 : Long.parseLong(contributionValueOnline.getTotalTime()));
    }

    @Override
    public ContributionValueOnlineTimeResponseDto getOnlineTimeByUserId(String userId) {
        LocalDate now = LocalDate.now();
        String key = RedisKeyEnums.ONLINE_TIME.getKey() + now.toString() + ":" + userId;
        Object onlineTime = redisUtils.getCacheObject(key);
        Long todayOnlineTime = onlineTime == null ? 0 : Long.parseLong(onlineTime.toString());

        //根据用户id查询用户总在线时长
        Query query = new Query(
                Criteria.where("user_id").is(userId)
        );
        ContributionValueOnline contributionValueOnline = mongoTemplate.findOne(query, ContributionValueOnline.class);


        return new ContributionValueOnlineTimeResponseDto()
                .setTodayOnlineTime(todayOnlineTime)
                .setTotalOnlineTime(contributionValueOnline == null ? 0 : Long.parseLong(contributionValueOnline.getTotalTime()));
    }

    @Override
    public List<ContributionValueTaskStatusResponseDto> taskStatus() {
        /**
         * 创建三个任务 分别为每天在线5分钟 20分钟 60分钟
         */
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currUserid = loginUser.getId();

        LocalDate now = LocalDate.now();
        String key = RedisKeyEnums.ONLINE_TIME.getKey() + now.toString() + ":" + currUserid;

        Object onlineTime = redisUtils.getCacheObject(key);

        Long todayOnlineTime = onlineTime == null ? 0 : Long.parseLong(onlineTime.toString());


        //查询configKey包含online_time的配置
        Query onlineConfigQuery = new Query(
                Criteria.where("configKey").regex("online_time")
        );
        List<ContributionValueConfig> configList = mongoTemplate.find(onlineConfigQuery, ContributionValueConfig.class);
        List<ContributionValueTaskStatusResponseDto> res = new ArrayList<>();
        //拿到所有的configKey
        for (ContributionValueConfig config : configList) {
            String time = config.getConfigKey().split(":")[1];
            Long timeInt = Long.parseLong(time);

            ContributionValueTaskStatusResponseDto task = new ContributionValueTaskStatusResponseDto()
                    .setTaskCondition(config.getConfigKey())
                    .setTaskReward(config.getConfigValue())
                    .setIsComplete(todayOnlineTime >= timeInt);
            res.add(task);
        }
        return res;

    }

    @Override
    public List<ContributionValueTaskStatusResponseDto> getTaskStatusByUserId(String userId) {
        LocalDate now = LocalDate.now();
        String key = RedisKeyEnums.ONLINE_TIME.getKey() + now.toString() + ":" + userId;

        Object onlineTime = redisUtils.getCacheObject(key);

        Long todayOnlineTime = onlineTime == null ? 0 : Long.parseLong(onlineTime.toString());

        //查询configKey包含online_time的配置
        Query onlineConfigQuery = new Query(
                Criteria.where("configKey").regex("online_time")
        );
        List<ContributionValueConfig> configList = mongoTemplate.find(onlineConfigQuery, ContributionValueConfig.class);
        List<ContributionValueTaskStatusResponseDto> res = new ArrayList<>();
        //拿到所有的configKey
        for (ContributionValueConfig config : configList) {
            String time = config.getConfigKey().split(":")[1];
            Long timeInt = Long.parseLong(time);

            ContributionValueTaskStatusResponseDto task = new ContributionValueTaskStatusResponseDto()
                    .setTaskCondition(config.getConfigKey())
                    .setTaskReward(config.getConfigValue())
                    .setIsComplete(todayOnlineTime >= timeInt);
            res.add(task);
        }
        return res;
    }

    @Override
    public void contributionValueIncentive() {
        //阻塞线程5秒
        try {
            log.info("开始执行贡献值激励任务前阻塞线程5秒");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("开始执行红包余额激励贡献值奖励");

        Query configQuery = new Query(
                Criteria.where("configKey").is(ContributionValueRecordType.RED_PACKET_BALANCE_INCENTIVE.getCode())
        );
        ContributionValueConfig config = mongoTemplate.findOne(configQuery, ContributionValueConfig.class);

        if (config == null) {
            log.error("红包余额激励贡献值奖励配置不存在");
            return;
        }

        //查询余额记录表
        LocalDate now = LocalDate.now();

        //当前日期减一
        LocalDate yesterday = now.minusDays(1);

        //查询yesterday的记录
        Query query = new Query(
                Criteria.where("create_time").regex("^" + yesterday.toString())
        );

        List<ContributionValueBalanceRecord> balanceRecords = mongoTemplate.find(query, ContributionValueBalanceRecord.class);

        //根据userId分组
        balanceRecords.stream().collect(Collectors.groupingBy(ContributionValueBalanceRecord::getUserId)).forEach((userId, list) -> {
            String key = RedisKeyEnums.RED_PACKET_BALANCE_INCENTIVE.getKey() + userId + ":" + now.toString();

            //如果今天已经激励过了 直接跳过
            if (redisUtils.hasKey(key)) {
                return;
            }

            //设置一个flag 此用户今天已经激励过了
            redisUtils.setCacheObject(key, "1", 24, TimeUnit.HOURS);

            //将余额相加
            double totalBalance = list.stream().mapToDouble(ContributionValueBalanceRecord::getBalance).sum();

            //平均值
            double avgBalance = totalBalance / 24;

            //保留两位小数
            avgBalance = new BigDecimal(avgBalance).setScale(2, RoundingMode.HALF_UP).doubleValue();

            //如果平均值小于10000直接跳过
            if (avgBalance < 10000) {
                return;
            }

            //激励值
            double incentiveValue = avgBalance * Double.parseDouble(config.getConfigValue());

            //将incentiveValue四舍五入 保留两位小数
            incentiveValue = new BigDecimal(incentiveValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();


            String lockKey = RedisKeyEnums.CONTRIBUTION_VALUE_BALANCE.getKey() + userId;
            customRedisLock.tryLock(lockKey);


            //查询贡献值余额
            Query contributionValueBalanceQuery = new Query(
                    Criteria.where("user_id").is(userId)
            );
            ContributionValueBalance contributionValueBalance = mongoTemplate.findOne(contributionValueBalanceQuery, ContributionValueBalance.class);

            if (contributionValueBalance == null) {
                contributionValueBalance = new ContributionValueBalance()
                        .setUserId(userId)
                        .setBalance(0);
                mongoTemplate.save(contributionValueBalance);
            }

            double res = contributionValueBalance.getBalance() + incentiveValue;

            //四舍五入
            res = new BigDecimal(res).setScale(2, RoundingMode.HALF_UP).doubleValue();

            //记录
            ContributionValueRecord contributionValueRecord = new ContributionValueRecord()
                    .setUserId(userId)
                    .setType(ContributionValueRecordType.RED_PACKET_BALANCE_INCENTIVE.getCode())
                    .setContributionValueChange(incentiveValue + "")
                    .setCreateTime(LocalDateTime.now().toString());

            mongoTemplate.save(contributionValueRecord);

            //更新用户贡献值余额
            contributionValueBalance.setBalance(res);
            mongoTemplate.updateFirst(contributionValueBalanceQuery, new Update().set("balance", contributionValueBalance.getBalance()), ContributionValueBalance.class);

            System.out.println("userId:" + userId + " avgBalance:" + avgBalance + " incentiveValue:" + incentiveValue);

            //释放锁
            customRedisLock.unlock(lockKey);
        });


    }

    public static void main(String[] args) {
        //2024-09-09 转换为LocalDate
        String dateStr = "2024-09-09";
        LocalDate localDate = LocalDate.parse(dateStr);

        //减1
        LocalDate yesterday = localDate.minusDays(1);

        System.out.println(localDate);
        System.out.println(yesterday);

        //判断localDate减1是否等于2024-09-08
        System.out.println(yesterday.equals(LocalDate.parse("2024-09-08")));


    }
}
