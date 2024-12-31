package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Document(collection = "red_packet_record")
@Accessors(chain = true)
public class RedPacketRecord implements Serializable {
    /**
     * ID
     */
    @Id
    private String id;

    /**
     * 操作人的id
     */
    @Field("user_id")
    private String userId;

    /**
     * 金额
     */
    private double amount;

    /**
     * 交易类型 recharge 充值，withdraw 提现，refund 退款，sendToUser 发送给用户，sendToGroup 发送给群，receiveFromUser 接收来自用户，receiveFromGroup 接收来自群
     */
    @Field("record_type")
    private String recordType;

    /**
     * 红包类型
     */
    @Field("red_packet_type")
    private String redPacketType;

    /**
     * 红包ID
     */
    @Field("red_packet_id")
    private String redPacketId;

    /**
     * 创建时间
     */
    @Field("create_time")
    private String createTime;

    /**
     * 区块链交易哈希
     */
    @Field("transaction_hash")
    private String transactionHash;

    /**
     * 交易状态 (例如：1成功，0失败，2 处理中)
     */
    @Field("recharge_status")
    private Integer rechargeStatus;

    /**
     * 群ID
     */
    @Field("group_id")
    private String groupID;

    /**
     * 发送的用户ID
     */
    @Field("send_user_id")
    private String sendUserId;

    /**
     * 充值消息
     */
    @Field("recharge_msg")
    private String rechargeMsg;

    /**
     * 币种
     */
    private String currency;

    /**
     * 提现地址
     */
    @Field("to_address")
    private String toAddress;
}