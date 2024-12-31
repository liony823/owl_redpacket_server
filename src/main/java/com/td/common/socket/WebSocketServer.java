package com.td.common.socket;

import com.alibaba.fastjson.JSON;
import com.td.client.dto.response.ContributionValueOnlineTimeResponseDto;
import com.td.client.dto.response.ContributionValueTaskStatusResponseDto;
import com.td.client.enums.RedisKeyEnums;
import com.td.client.service.ContributionValueService;
import com.td.common.lock.CustomRedisLock;
import com.td.common.pojo.ContributionValueBalance;
import com.td.common.pojo.ContributionValueConfig;
import com.td.common.pojo.ContributionValueOnline;
import com.td.common.pojo.ContributionValueRecord;
import com.td.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ServerEndpoint(value = "/ws/{userId}")
@Component
@Slf4j
public class WebSocketServer {

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //接收userId
    private String userId = "";


    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CustomRedisLock customRedisLock;

    @Autowired
    private ContributionValueService contributionValueService;

    //连接建立成功调用的方法
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
        this.session = session;
        this.userId = userId;
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
        } else {
            webSocketMap.put(userId, this);
            addOnlineCount();
        }

        //发送消息
        try {
            this.contributionValueService = SpringContextHolder.getBean(ContributionValueService.class);
            ContributionValueOnlineTimeResponseDto onlineTimeResponseDto = contributionValueService.getOnlineTimeByUserId(userId);
            List<ContributionValueTaskStatusResponseDto> taskStatusResponseDtos = contributionValueService.getTaskStatusByUserId(userId);
            Double balance = contributionValueService.getBalanceByUserId(userId);

            HashMap<String, Object> res = new HashMap<>();
            res.put("code", 200);
            res.put("msg", "success");
            res.put("onlineTime", onlineTimeResponseDto);
            res.put("taskStatus", taskStatusResponseDtos);
            res.put("balance", balance);

            sendMessage(JSON.toJSONString(res));
        } catch (IOException e) {
            HashMap<String, Object> res = new HashMap<>();
            res.put("code", 520);
            res.put("msg", "服务器错误");
            sendMessage(JSON.toJSONString(res));
            log.error("用户错误:" + this.userId + ",原因:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //连接关闭调用的方法
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            subOnlineCount();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            this.redisUtils = SpringContextHolder.getBean(RedisUtils.class);
            this.mongoTemplate = SpringContextHolder.getBean(MongoTemplate.class);
            this.customRedisLock = SpringContextHolder.getBean(CustomRedisLock.class);

            LocalDate now = LocalDate.now();

            if (!StringUtils.hasText(userId)) {
                return;
            }

            //flagKey
            String flagKey = RedisKeyEnums.ONLINE_TIME_FLAG.getKey() + now.toString() + ":" + userId;

            //查询是否存在
            Object flag = redisUtils.getCacheObject(flagKey);

            if (flag != null) {
                this.contributionValueService = SpringContextHolder.getBean(ContributionValueService.class);
                ContributionValueOnlineTimeResponseDto onlineTimeResponseDto = contributionValueService.getOnlineTimeByUserId(userId);
                List<ContributionValueTaskStatusResponseDto> taskStatusResponseDtos = contributionValueService.getTaskStatusByUserId(userId);
                Double balance = contributionValueService.getBalanceByUserId(userId);

                HashMap<String, Object> res = new HashMap<>();
                res.put("code", 200);
                res.put("msg", "success");
                res.put("onlineTime", onlineTimeResponseDto);
                res.put("taskStatus", taskStatusResponseDtos);
                res.put("balance", balance);

                try {
                    sendMessage(JSON.toJSONString(res));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            //设置标记
            redisUtils.setCacheObject(flagKey, 1, 60, TimeUnit.SECONDS);


            String key = RedisKeyEnums.ONLINE_TIME.getKey() + now.toString() + ":" + userId;

            //查询key是否存在
            Integer onlineTime = redisUtils.getCacheObject(key);

            if (onlineTime == null) {
                onlineTime = 0;
                //不存在则新增
                redisUtils.setCacheObject(key, 1, 24, TimeUnit.HOURS);
            } else {
                //存在则累加
                redisUtils.incr(key, 1);
            }
            onlineTime++;

            Query query = new Query(Criteria.where("user_id").is(userId));

            ContributionValueOnline contributionValueOnline = mongoTemplate.findOne(query, ContributionValueOnline.class);

            if (contributionValueOnline == null) {
                contributionValueOnline = new ContributionValueOnline()
                        .setUserId(userId)
                        .setTotalTime(onlineTime + "")
                ;
                mongoTemplate.save(contributionValueOnline);
            } else {
                //查询当前在线时长
                Update update = new Update();
                update.set("total_time", Long.parseLong(contributionValueOnline.getTotalTime()) + 1);
                mongoTemplate.updateFirst(query, update, ContributionValueOnline.class);
            }

            //查询configKey包含online_time的配置
            Query onlineConfigQuery = new Query(
                    Criteria.where("configKey").regex("online_time")
            );

            List<ContributionValueConfig> configList = mongoTemplate.find(onlineConfigQuery, ContributionValueConfig.class);

            HashMap<Integer, ContributionValueConfig> onlineConfigHashMap = new HashMap<>();
            //拿到所有的configKey
            for (ContributionValueConfig config : configList) {
                String time = config.getConfigKey().split(":")[1];
                Integer timeInt = Integer.parseInt(time);
                onlineConfigHashMap.put(timeInt, config);
            }

            ContributionValueConfig contributionValueConfig = onlineConfigHashMap.get(onlineTime);

            if (contributionValueConfig == null) {

                //发送消息
                try {

                    this.contributionValueService = SpringContextHolder.getBean(ContributionValueService.class);
                    ContributionValueOnlineTimeResponseDto onlineTimeResponseDto = contributionValueService.getOnlineTimeByUserId(userId);
                    List<ContributionValueTaskStatusResponseDto> taskStatusResponseDtos = contributionValueService.getTaskStatusByUserId(userId);
                    Double balance = contributionValueService.getBalanceByUserId(userId);

                    HashMap<String, Object> res = new HashMap<>();
                    res.put("code", 200);
                    res.put("msg", "success");
                    res.put("onlineTime", onlineTimeResponseDto);
                    res.put("taskStatus", taskStatusResponseDtos);
                    res.put("balance", balance);

                    sendMessage(JSON.toJSONString(res));
                } catch (IOException e) {
                    HashMap<String, Object> res = new HashMap<>();
                    res.put("code", 520);
                    res.put("msg", "服务器错误");
                    sendMessage(JSON.toJSONString(res));
                    log.error("用户错误:" + this.userId + ",原因:" + e.getMessage());
                    e.printStackTrace();
                }

                return;
            }

            String queryKey = contributionValueConfig.getConfigKey();

            //查询贡献值配置表 获取签到奖励
            Query configQuery = new Query(
                    Criteria.where("configKey").is(queryKey)
            );
            ContributionValueConfig config = mongoTemplate.findOne(configQuery, ContributionValueConfig.class);

            if (config == null) {
                return;
            }

            //自旋锁
            String lockKey = RedisKeyEnums.CONTRIBUTION_VALUE_BALANCE.getKey() + userId;
            customRedisLock.spinLock(lockKey);

            //根据用户id修改用户贡献值
            Update update = new Update();
            update.inc("balance", Long.parseLong(config.getConfigValue()));

            mongoTemplate.updateFirst(query, update, ContributionValueBalance.class);

            //记录贡献值记录
            ContributionValueRecord record = new ContributionValueRecord()
                    .setContributionValueChange(config.getConfigValue())
                    .setType(queryKey)
                    .setCreateTime(LocalDateTime.now().toString())
                    .setUserId(userId);

            mongoTemplate.save(record);

            //发送消息
            try {

                this.contributionValueService = SpringContextHolder.getBean(ContributionValueService.class);
                ContributionValueOnlineTimeResponseDto onlineTimeResponseDto = contributionValueService.getOnlineTimeByUserId(userId);
                List<ContributionValueTaskStatusResponseDto> taskStatusResponseDtos = contributionValueService.getTaskStatusByUserId(userId);
                Double balance = contributionValueService.getBalanceByUserId(userId);

                HashMap<String, Object> res = new HashMap<>();
                res.put("code", 200);
                res.put("msg", "success");
                res.put("onlineTime", onlineTimeResponseDto);
                res.put("taskStatus", taskStatusResponseDtos);
                res.put("balance", balance);

                sendMessage(JSON.toJSONString(res));
            } catch (IOException e) {
                HashMap<String, Object> res = new HashMap<>();
                res.put("code", 520);
                res.put("msg", "服务器错误");
                sendMessage(JSON.toJSONString(res));
                log.error("用户错误:" + this.userId + ",原因:" + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            HashMap<String, Object> res = new HashMap<>();
            res.put("code", 520);
            res.put("msg", "服务器错误");
            try {
                sendMessage(JSON.toJSONString(res));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            log.error("用户错误:" + this.userId + ",原因:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //连接错误调用的方法
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    //服务器主动推送消息
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    //发送自定义消息
    public static void sendInfo(String message, @PathParam("userId") Long userId) throws IOException {
        log.info("发送消息到:" + userId + "，报文:" + message);
        if (userId != null && webSocketMap.containsKey(userId + "")) {
            webSocketMap.get(userId + "").sendMessage(message);
        } else {
            log.error("用户" + userId + ",不在线！");
        }
    }

    //获取在线人数
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}

