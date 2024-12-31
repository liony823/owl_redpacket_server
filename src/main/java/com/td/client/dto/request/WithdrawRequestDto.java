package com.td.client.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-07 14:10
 */
@Data
public class WithdrawRequestDto {

    @ApiModelProperty(value = "提现金额", example = "100")
    private Double amount;

}
