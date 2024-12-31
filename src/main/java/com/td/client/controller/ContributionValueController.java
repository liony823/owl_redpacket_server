package com.td.client.controller;

import com.td.client.dto.request.ExchangeRequestDto;
import com.td.client.service.ContributionValueService;
import com.td.common.annotations.RedisSynchronized;
import com.td.common.base.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-06 22:47
 */
@RestController
@RequestMapping("/v1/contributionValue")
public class ContributionValueController {
    @Autowired
    private ContributionValueService contributionValueService;

    /**
     * 签到
     */
    @PostMapping("/sign")
    @RedisSynchronized(key = "sign:lock")
    public ResponseResult sign() {
        return ResponseResult.success(contributionValueService.sign());
    }

    /**
     * 查询贡献值记录
     */
    @PostMapping("/record")
    public ResponseResult record() {
        return ResponseResult.success(contributionValueService.record());
    }

    /**
     * 签到日志
     */
    @PostMapping("/signRecord")
    public ResponseResult signRecord() {
        return ResponseResult.success(contributionValueService.signRecord());
    }

    /**
     * 查询贡献值余额
     */
    @PostMapping("/balance")
    public ResponseResult balance() {
        return ResponseResult.success(contributionValueService.balance());
    }

    /**
     * 查询汇率
     */
    @GetMapping("/exchangeRate")
    public ResponseResult exchangeRate() {
        return ResponseResult.success("汇率",contributionValueService.exchangeRate());
    }

    /**
     * 贡献值兑换红包余额
     */
    @PostMapping("/exchange")
    @RedisSynchronized(key = "exchange")
    public ResponseResult exchange(@RequestBody ExchangeRequestDto exchangeRequestDto) {
        return ResponseResult.success(contributionValueService.exchange(exchangeRequestDto));
    }

    /**
     * 查询在线时长
     */
    @PostMapping("/onlineTime")
    public ResponseResult onlineTime() {
        return ResponseResult.success("在线时长",contributionValueService.onlineTime());
    }

    /**
     * 查询今日任务状态
     */
    @PostMapping("/taskStatus")
    public ResponseResult taskStatus() {
        return ResponseResult.success(contributionValueService.taskStatus());
    }
}
