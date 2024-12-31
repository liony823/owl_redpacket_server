package com.td.client.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-06 11:22
 */
@Data
public class RechargeRequestDto {
    @ApiModelProperty(value = "转账Hash", example = "0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501")
    private String txHash;

    @ApiModelProperty(value = "转账金额", example = "100")
    private Double amount;
}
