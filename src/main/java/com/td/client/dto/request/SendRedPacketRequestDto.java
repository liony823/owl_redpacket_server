package com.td.client.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-03 14:57
 */
@Data
public class SendRedPacketRequestDto {
    @ApiModelProperty(value = "领取人ID 一对一和专属红包为接收人ID 其他为null",example = "2719400749")
    private String receiveUserId;

    @ApiModelProperty(value = "金额",example = "100")
    private Double amount;

    @ApiModelProperty(value = "红包类型 private(私聊发送红包) normal(普通红包 群聊直接发红包都是普通红包) luck(群聊拼手气红包) exclusive(群聊用户专属红包)",example = "private")
    private String type;

    @ApiModelProperty(value = "备注",example = "恭喜发财")
    private String remark;

    @ApiModelProperty(value = "支付密码",example = "123456")
    private String password;

    @ApiModelProperty(value = "群ID 私聊发送红包不需要传",example = "")
    private String groupID;

    @ApiModelProperty(value = "红包个数 一对一和专属红包不需要传 其他为用户输入的个数")
    private Integer totalCount;

    @ApiModelProperty(value = "emoji")
    private String emoji;

    @ApiModelProperty(value = "消息id")
    private String clientMsgID;
}
