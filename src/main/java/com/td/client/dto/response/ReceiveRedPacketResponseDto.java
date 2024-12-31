package com.td.client.dto.response;

import com.td.common.pojo.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-05 14:23
 */
@Data
@Accessors(chain = true)
public class ReceiveRedPacketResponseDto {

    @ApiModelProperty(value = "领取红包金额", example = "100")
    private Double amount;

    @ApiModelProperty(value = "红包id", example = "100")
    private String redPacketId;

    @ApiModelProperty(value = "发送人信息")
    private User sendUserInfo;

    @ApiModelProperty(value = "领取人信息")
    private User receiveUserInfo;

    @ApiModelProperty(value = "领取时间")
    private String createTime;
}
