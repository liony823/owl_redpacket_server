package com.td.client.task;

import com.td.client.service.ContributionValueService;
import com.td.common.lock.CustomRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-09 13:39
 */
@Slf4j
@Component
public class ContributionValueIncentiveTask {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CustomRedisLock customRedisLock;

    @Autowired
    private ContributionValueService contributionValueService;

    //每晚凌晨十二点执行
    @Scheduled(cron = "0 0 0 * * ?")
    public void incentive() {
        contributionValueService.contributionValueIncentive();
    }
}
