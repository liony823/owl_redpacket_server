package com.td.client.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-12 10:33
 */
@Data
public class TransactionRecordRequestDto {
    @ApiModelProperty(value = "交易类型,recharge:充值,withdraw:提现,receive:收到,send:发送,refund:退款", example = "recharge")
    private String recordType;
}
