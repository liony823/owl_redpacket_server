package com.td.client.controller;

import com.td.client.dto.request.RechargeRequestDto;
import com.td.client.dto.request.TransactionRecordRequestDto;
import com.td.client.dto.request.WithdrawRequestDto;
import com.td.client.dto.response.TransactionRecordPageResponseDto;
import com.td.client.service.TransactionService;
import com.td.common.annotations.Anonymous;
import com.td.common.annotations.RedisSynchronized;
import com.td.common.base.PageRequest;
import com.td.common.base.ResponseResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-03 13:44
 */
@RestController
@RequestMapping("/v1/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * 获取收款地址
     */
    @GetMapping("/getAddress")
    @Transactional
    @Anonymous
    public ResponseResult getAddress() {
        return ResponseResult.success("操作成功", transactionService.getAddress());
    }

    /**
     * 充值
     */
    @PostMapping("/recharge")
    @RedisSynchronized(key = "transaction:recharge")
    public ResponseResult recharge(@RequestBody RechargeRequestDto rechargeRequestDto) {
        transactionService.recharge(rechargeRequestDto);
        return ResponseResult.success();
    }

    /**
     * 提现
     */
    @PostMapping("/withdraw")
    @RedisSynchronized(key = "transaction:withdraw")
    public ResponseResult withdraw(@RequestBody WithdrawRequestDto withdrawRequestDto) {
        transactionService.withdraw(withdrawRequestDto);
        return ResponseResult.success();
    }

    /**
     * 交易记录
     */
    @PostMapping("/record")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = TransactionRecordPageResponseDto.class)
    })
    public ResponseResult record(@RequestBody PageRequest<TransactionRecordRequestDto> pageRequest) {
        return ResponseResult.success("操作成功", transactionService.record(pageRequest));
    }
}
