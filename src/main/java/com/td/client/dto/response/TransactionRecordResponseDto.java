package com.td.client.dto.response;

import com.td.client.dto.base.FriendDto;
import com.td.common.base.ResponseResult;
import com.td.common.pojo.Group;
import com.td.common.pojo.RedPacketRecord;
import com.td.common.pojo.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiResponse;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-10 10:21
 */
@Data
@Accessors(chain = true)
public class TransactionRecordResponseDto {

    @ApiModelProperty(value = "交易时间", example = "2024-08-10 10:21:00")
    private String createTime;

    @ApiModelProperty(value = "交易类型", example = "recharge:充值、withdraw:提现、refund:退款、receive_from_user：领取个人红包、send_to_user：发送个人红包、send_to_group_luck:发送群红包、receive_from_group_luck:领取群红包、send_to_group_exclusive:发送专属红包、receive_from_group_exclusive:领取专属红包")
    private String type;

    @ApiModelProperty(value = "交易金额", example = "+100.00 OWL、-100.00 OWL")
    private String amount;

    @ApiModelProperty(value = "交易hash", example = "0x1234567890abcdef")
    private String txHash;

    @ApiModelProperty(value = "朋友信息")
    private FriendDto friend;

    @ApiModelProperty(value = "群组信息")
    private Group group;

}
