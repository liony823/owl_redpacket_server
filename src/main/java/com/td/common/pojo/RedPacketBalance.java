package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Accessors(chain = true)
@Document(collection = "red_packet_balances")
public class RedPacketBalance {
    @Id
    private String id; // 唯一标识

    /**
     * 用户ID，外键，引用 Users 表的 userId
     */
    @Field("user_id")
    private String userId;

    /**
     * 余额
     */
    private double balance;
}