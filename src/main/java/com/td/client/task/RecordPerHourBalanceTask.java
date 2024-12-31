package com.td.client.task;

import com.td.client.service.ContributionValueService;
import com.td.common.pojo.ContributionValueBalanceRecord;
import com.td.common.pojo.RedPacketBalance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-14 21:54
 */
@Slf4j
@Component

public class RecordPerHourBalanceTask {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ContributionValueService contributionValueService;

    //每小时执行一次
    @Scheduled(cron = "0 0 * * * ?")
    public void recordPerHourBalance() {

        //阻塞线程5秒
        try {
            log.info("开始记录当前时间段用户余额，阻塞线程5秒");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LocalDateTime now = LocalDateTime.now();
        log.info("开始记录当前时间段用户余额，当前时间：{}", now);

        //查询用户余额
        List<RedPacketBalance> redPacketBalanceList = mongoTemplate.findAll(RedPacketBalance.class);

        //记录用户余额
        List<ContributionValueBalanceRecord> balanceRecords = redPacketBalanceList.stream().map(item -> {
            return new ContributionValueBalanceRecord()
                    .setUserId(item.getUserId() + "")
                    .setCreateTime(now.toString())
                    .setBalance(item.getBalance());
        }).collect(Collectors.toList());
        mongoTemplate.insertAll(balanceRecords);
        log.info("记录当前时间段用户余额结束，当前时间：{}", now);

        //删除3天前的余额记录
//        LocalDateTime twoDaysAgo = now.minusDays(3);
//
//        mongoTemplate.remove(new Query(Criteria.where("create_time").lt(twoDaysAgo.toString())), ContributionValueBalanceRecord.class);
    }
}
