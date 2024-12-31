package com.td.client.task;

import com.td.client.enums.RedPacketRecordType;
import com.td.client.enums.RedisKeyEnums;
import com.td.client.service.TransactionService;
import com.td.common.lock.CustomRedisLock;
import com.td.common.pojo.RedPacketBalance;
import com.td.common.pojo.RedPacketRecord;
import com.td.common.pojo.TransactionRecords;
import com.td.common.utils.RedisUtils;
import com.td.common.utils.Web3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-03 13:42
 */
@Service
@Slf4j
public class CheckTransactionTask {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Web3Util web3Util;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CustomRedisLock customRedisLock;

    @Autowired
    private TransactionService transactionService;

    //每十秒执行一次
//    @Scheduled(cron = "0/10 * * * * ?")
    public void checkRecharge() {

        //查询所有类型为recharge 且状态为2的记录
        List<RedPacketRecord> redPacketRecords = mongoTemplate.find(new Query(Criteria
                .where("record_type").is(RedPacketRecordType.RECHARGE.getCode())
                .and("recharge_status").is(2)), RedPacketRecord.class);

        for (RedPacketRecord redPacketRecord : redPacketRecords) {
            String txHash = redPacketRecord.getTransactionHash();
            //查询是否处理
            boolean lock = customRedisLock.tryLock(RedisKeyEnums.RED_PACKET_RECHARGE.getKey() + txHash);
            if (!lock) {
                log.error("txHash:{}正在处理中", txHash);
                continue;
            }
            //开启多线程处理
            new Thread(() -> {
                log.info("txHash:{}开始处理", txHash);
                TransactionRecords tx;
                try {
                    tx = web3Util.getTx(txHash);

                    if (tx != null) {

                        String to = tx.getTo();
                        String address = transactionService.getAddress();
                        //判断是否是配置的收款地址
                        if (!to.equals(address)) {
                            log.error("to:{}地址", to);
                            log.error("txHash:{}地址不匹配", txHash);
                            //更新状态
                            Query query = new Query(Criteria.where("id").is(redPacketRecord.getId()));
                            Update update = new Update();
                            update.set("recharge_status", 0);
                            update.set("recharge_msg", "Hash收款地址：" + to + "与配置地址：" + address + "不匹配");
                            mongoTemplate.updateFirst(query, update, RedPacketRecord.class);
                            return;
                        }

                        Double realAmount = web3Util.getRealAmount(tx.getValue());

                        //更新状态
                        Query query = new Query(Criteria.where("id").is(redPacketRecord.getId()));
                        Update update = new Update();
                        update.set("recharge_status", 1);
                        update.set("amount", realAmount);
                        update.set("recharge_msg", "充值成功");

                        mongoTemplate.updateFirst(query, update, RedPacketRecord.class);

                        String key = RedisKeyEnums.USER_BALANCE.getKey() + redPacketRecord.getSendUserId();
                        customRedisLock.spinLock(key);

                        //查询用户余额信息
                        Query queryBalance = new Query(Criteria.where("user_id").is(redPacketRecord.getSendUserId()));
                        RedPacketBalance redPacketBalance = mongoTemplate.findOne(queryBalance, RedPacketBalance.class);
                        if (redPacketBalance == null) {
                            redPacketBalance = new RedPacketBalance()
                                    .setUserId(redPacketRecord.getSendUserId())
                                    .setBalance(0);
                        }

                        redPacketBalance.setBalance(redPacketBalance.getBalance() + realAmount);

                        if (redPacketBalance.getId() == null) {
                            mongoTemplate.save(redPacketBalance);
                        } else {
                            mongoTemplate.updateFirst(queryBalance, new Update().set("balance", redPacketBalance.getBalance()), RedPacketBalance.class);
                        }

                        customRedisLock.spinUnlock(key);
                        log.info("txHash:{}处理成功", txHash);
                    }

                } finally {
                    customRedisLock.unlock(RedisKeyEnums.RED_PACKET_RECHARGE.getKey() + txHash);
                }
            }).start();
        }
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void checkOWLRecharge() {

        //查询所有类型为recharge 且状态为2的记录
        List<RedPacketRecord> redPacketRecords = mongoTemplate.find(new Query(Criteria
                .where("record_type").is(RedPacketRecordType.RECHARGE.getCode())
                .and("recharge_status").is(2)), RedPacketRecord.class);

        for (RedPacketRecord redPacketRecord : redPacketRecords) {
            String txHash = redPacketRecord.getTransactionHash();
            //查询是否处理
            boolean lock = customRedisLock.tryLock(RedisKeyEnums.RED_PACKET_RECHARGE.getKey() + txHash);
            if (!lock) {
                log.error("txHash:{}正在处理中", txHash);
                continue;
            }
            //开启多线程处理
            new Thread(() -> {
                String centerAddress = transactionService.getAddress();
                log.info("txHash:{}开始处理", txHash);
                TransactionRecords tx;
                List<TransactionRecords> owlTxs = web3Util.getOwlTxs(centerAddress);

                //通过hash作为key 转成hashmap
                Map<String, TransactionRecords> owlTxMap = owlTxs.stream().collect(Collectors.toMap(TransactionRecords::getHash, a -> a));
                try {
                    tx = owlTxMap.get(txHash);

                    if (tx != null) {
                        Double realAmount = web3Util.getOwlRealAmount(tx.getValue());

                        //更新状态
                        Query query = new Query(Criteria.where("id").is(redPacketRecord.getId()));
                        Update update = new Update();
                        update.set("recharge_status", 1);
                        update.set("amount", realAmount);
                        update.set("recharge_msg", "充值成功");

                        mongoTemplate.updateFirst(query, update, RedPacketRecord.class);

                        String key = RedisKeyEnums.USER_BALANCE.getKey() + redPacketRecord.getSendUserId();
                        customRedisLock.spinLock(key);

                        //查询用户余额信息
                        Query queryBalance = new Query(Criteria.where("user_id").is(redPacketRecord.getSendUserId()));
                        RedPacketBalance redPacketBalance = mongoTemplate.findOne(queryBalance, RedPacketBalance.class);
                        if (redPacketBalance == null) {
                            redPacketBalance = new RedPacketBalance()
                                    .setUserId(redPacketRecord.getSendUserId())
                                    .setBalance(0);
                        }

                        redPacketBalance.setBalance(redPacketBalance.getBalance() + realAmount);

                        if (redPacketBalance.getId() == null) {
                            mongoTemplate.save(redPacketBalance);
                        } else {
                            mongoTemplate.updateFirst(queryBalance, new Update().set("balance", redPacketBalance.getBalance()), RedPacketBalance.class);
                        }

                        customRedisLock.spinUnlock(key);
                        log.info("txHash:{}处理成功", txHash);
                    }

                } finally {
                    customRedisLock.unlock(RedisKeyEnums.RED_PACKET_RECHARGE.getKey() + txHash);
                }
            }).start();
        }

    }

    //每十秒执行一次
//    @Scheduled(cron = "0/10 * * * * ?")
    public void checkWithdraw() {

        //查询所有类型为withdraw 且状态为2的记录
        List<RedPacketRecord> redPacketRecords = mongoTemplate.find(new Query(Criteria
                .where("record_type").is(RedPacketRecordType.WITHDRAW.getCode())
                .and("recharge_status").is(2)), RedPacketRecord.class);

        for (RedPacketRecord redPacketRecord : redPacketRecords) {
            String txHash = redPacketRecord.getTransactionHash();
            //查询是否处理
            boolean lock = customRedisLock.tryLock(RedisKeyEnums.RED_PACKET_WITHDRAW.getKey() + txHash);
            if (!lock) {
                log.error("txHash:{}正在处理中", txHash);
                continue;
            }
            //开启多线程处理
            new Thread(() -> {
                log.info("txHash:{}开始处理", txHash);
                TransactionRecords tx;
                try {
                    tx = web3Util.getTx(txHash);

                    if (tx != null) {
                        //更新状态
                        Query query = new Query(Criteria.where("id").is(redPacketRecord.getId()));
                        Update update = new Update();
                        update.set("recharge_status", 1);

                        mongoTemplate.updateFirst(query, update, RedPacketRecord.class);

                        log.info("txHash:{}处理成功", txHash);
                    }

                } finally {
                    customRedisLock.unlock(RedisKeyEnums.RED_PACKET_WITHDRAW.getKey() + txHash);
                }
            }).start();
        }
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void checkOWLWithdraw() {

        //查询所有类型为withdraw 且状态为2的记录
        List<RedPacketRecord> redPacketRecords = mongoTemplate.find(new Query(Criteria
                .where("record_type").is(RedPacketRecordType.WITHDRAW.getCode())
                .and("recharge_status").is(2)), RedPacketRecord.class);

        for (RedPacketRecord redPacketRecord : redPacketRecords) {
            String txHash = redPacketRecord.getTransactionHash();
            String toAddress = redPacketRecord.getToAddress();
            //查询是否处理
            boolean lock = customRedisLock.tryLock(RedisKeyEnums.RED_PACKET_WITHDRAW.getKey() + txHash);
            if (!lock) {
                log.error("txHash:{}正在处理中", txHash);
                continue;
            }
            //开启多线程处理
            new Thread(() -> {
                TransactionRecords tx;
                List<TransactionRecords> owlTxs = web3Util.getOwlTxs(toAddress);

                //通过hash作为key 转成hashmap
                Map<String, TransactionRecords> owlTxMap = owlTxs.stream().collect(Collectors.toMap(TransactionRecords::getHash, a -> a));
                log.info("txHash:{}开始处理", txHash);

                try {
                    tx = owlTxMap.get(txHash);

                    if (tx != null) {
                        //更新状态
                        Query query = new Query(Criteria.where("id").is(redPacketRecord.getId()));
                        Update update = new Update();
                        update.set("recharge_status", 1);

                        mongoTemplate.updateFirst(query, update, RedPacketRecord.class);

                        log.info("txHash:{}处理成功", txHash);
                    }

                } finally {
                    customRedisLock.unlock(RedisKeyEnums.RED_PACKET_WITHDRAW.getKey() + txHash);
                }
            }).start();
        }
    }
}

