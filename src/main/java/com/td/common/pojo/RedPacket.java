package com.td.common.pojo;

import com.td.client.enums.RedPacketType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
@Document(collection = "red_packets")
public class RedPacket {
    /**
     * 红包ID
     */
    @Id
    private String id;

    /**
     * 发送者ID
     */
    @Field("sender_id")
    private String senderId;

    /**
     * 接收者ID
     */
    @Field("receiver_id")
    private String receiverId;

    /**
     * 群ID 0为个人红包
     */
    @Field("group_id")
    private String groupID;

    /**
     * 红包类型 normal普通红包 luck拼手气红包 exclusive专属红包
     */
    private String type;

    /**
     * 总金额
     */
    @Field("total_amount")
    private double totalAmount;

    /**
     * 总个数
     */
    @Field("total_count")
    private int totalCount;

    /**
     * 创建时间
     */
    @Field("create_time")
    private String createTime;

    /**
     * 状态 0未领取 1已领取 2已过期
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;


    /**
     * emoji
     */
    private String emoji;

    /**
     * 过期时间
     */
    @Field("expire_time")
    private String expireTime;
}
