package com.td.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.td.client.dto.response.ReceiveRedPacketRecordResponseDto;
import com.td.client.enums.RedPacketRecordType;
import com.td.client.enums.RedisKeyEnums;
import com.td.client.service.RedPacketExpireService;
import com.td.client.service.RedPacketService;
import com.td.common.lock.CustomRedisLock;
import com.td.common.pojo.RedPacket;
import com.td.common.pojo.RedPacketBalance;
import com.td.common.pojo.RedPacketRecord;
import com.td.common.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-10 16:30
 */
@Slf4j
@Service("private_RedPacketExpireServiceImpl")
public class PrivateRedPacketExpireServiceImpl implements RedPacketExpireService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CustomRedisLock customRedisLock;

    @Value("${send.msg.url}")
    private String sendMsgUrl;

    @Autowired
    private RedPacketService redPacketService;


    @Override
    public void handlerRedPacketExpire(RedPacket redPacket) {
        //处理红包过期
        log.info("个人红包过期");
        String redPacketId = redPacket.getId();

        String key = RedisKeyEnums.USER_BALANCE.getKey() + redPacket.getSenderId();
        customRedisLock.spinLock(key);

        //红包过期，退款
        double amount = redPacket.getTotalAmount();

        //根据用户id修改用户余额
        Query query = new Query(Criteria.where("user_id").is(redPacket.getSenderId()));
        Update update = new Update();
        update.inc("balance", amount);
        mongoTemplate.updateFirst(query, update, RedPacketBalance.class);

        //修改红包状态
        redPacket.setStatus(2);
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(redPacketId)), new Update().set("status", 2), RedPacket.class);

        //记录红包记录
        RedPacketRecord redPacketRecord = new RedPacketRecord()
                .setAmount(amount)
                .setRecordType(RedPacketRecordType.REFUND.getCode())
                .setRedPacketType(redPacket.getType())
                .setRedPacketId(redPacketId)
                .setCreateTime(LocalDateTime.now().toString())
                .setSendUserId(redPacket.getSenderId());
        mongoTemplate.save(redPacketRecord);

        /**
         * {
         *   "sendID": "9067943559",
         *   "recvID": "4872251310",
         *   "groupID": "",
         *   "senderNickname": "ztmSug",
         *   "senderFaceURL": "http://16.162.220.76:10002/object/imAdmin/owl.png",
         *   "senderPlatformID": 2,
         *   "content": {
         *     "content": "hello!!",
         *     "customType": 1005,
         *     "data":"123"
         *   },
         *   "contentType": 110,
         *   "sessionType": 1
         * }
         */

        //发送退款消息
//        RestTemplate restTemplate = new RestTemplate();

        String senderId = redPacket.getSenderId();
        String receiverId = redPacket.getReceiverId();

        sendRedPacketRefundMsg(senderId, receiverId, redPacketId);
    }

    public String getTokenByUserId() {
        RestTemplate restTemplate = new RestTemplate();

        HashMap<String, Object> param = new HashMap<>();
        param.put("secret", "owlIM123");
        param.put("platformID", 10);
        param.put("userID", "imAdmin");

        // 创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("operationID", "imAdmin");

        // 创建 HttpEntity 对象，包含请求体和请求头
        HttpEntity<HashMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);


        //发送请求
        JSONObject jsonObject = restTemplate.postForObject(sendMsgUrl + "/auth/user_token", httpEntity, JSONObject.class);

        String errCode = jsonObject.getString("errCode");

        if (!"0".equals(errCode)) {
            log.error("错误信息：");
            log.error(jsonObject.toJSONString());
            throw new RuntimeException("获取  token失败");
        }

        JSONObject data = jsonObject.getJSONObject("data");

        String token = data.getString("token");

        return token;
    }

    public void sendRedPacketRefundMsg(String sendId, String receiveId, String redPacketId) {

        ReceiveRedPacketRecordResponseDto receiveRedPacketRecordResponseDto = redPacketService.receiveRecord2(redPacketId);

        //查询发送人信息
        Query query1 = new Query(Criteria.where("user_id").is(sendId));
        User sendUser = mongoTemplate.findOne(query1, User.class);

        if (sendUser == null) {
            log.error("发送人信息为空");
            return;
        }

        //查询接收人信息
        Query query2 = new Query(Criteria.where("user_id").is(receiveId));
        User receiveUser = mongoTemplate.findOne(query2, User.class);

        if (receiveUser == null) {
            log.error("接收人信息为空");
            return;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("token", getTokenByUserId());
        httpHeaders.set("operationID", "imAdmin");

        //使用HashMap封装消息
        HashMap<String, Object> param = new HashMap<>();
        param.put("sendID", sendUser.getUserId());
        param.put("recvID", receiveUser.getUserId());
        param.put("senderNickname", sendUser.getNickname());
        param.put("senderFaceURL", sendUser.getFaceUrl());
        param.put("senderPlatformID", 2);

        HashMap<String, Object> content = new HashMap<>();
        receiveRedPacketRecordResponseDto.setCustomType(1008)
                .setContent("红包过期退款");
        content.put("data", JSON.toJSONString(receiveRedPacketRecordResponseDto));
        param.put("content", content);
        param.put("contentType", 110);
        param.put("sessionType", 1);

        HttpEntity<HashMap<String, Object>> httpEntity = new HttpEntity<>(param, httpHeaders);

        //发送消息
        RestTemplate restTemplate = new RestTemplate();
        JSONObject jsonObject = restTemplate.postForObject(sendMsgUrl + "/msg/send_msg", httpEntity, JSONObject.class);
        System.out.println(jsonObject.toJSONString());
    }
}
