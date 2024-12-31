package com.td.client.dto.response;

import com.td.common.pojo.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-05 10:53
 */
@Data
@Accessors(chain = true)
public class RedPacketStatusResponseDto {

    /**
     * 红包id
     */
    private String redPacketId;

    /**
     * 红包状态
     */
    private Integer status; // 0未领取 1已领取 2已过期

    /**
     * 发送人信息
     */
    private User sendUserInfo;

    /**
     * 过期时间
     */
    private String expireTime;

    /**
     * 备注
     */
    private String remark;

    private String emoji;

    /**
     * 金额
     */
    private Double amount;

    /**
     * 领取人信息
     */
    private User receiveUserInfo;

    /**
     * 红包类型 private私密红包 luck拼手气红包 exclusive专属红包 如果是专属红包 会显示接收者的信息
     */
    private String redPacketType;
}
