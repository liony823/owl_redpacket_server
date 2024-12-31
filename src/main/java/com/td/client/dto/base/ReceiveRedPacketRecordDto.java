package com.td.client.dto.base;

import com.td.common.pojo.User;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-07 16:50
 */
@Data
@Accessors(chain = true)
public class ReceiveRedPacketRecordDto {
    private Double amount;
    private User receiveUserInfo;
    private String receiveTime;

}
