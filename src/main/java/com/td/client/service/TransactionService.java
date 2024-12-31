package com.td.client.service;

import com.td.client.dto.request.RechargeRequestDto;
import com.td.client.dto.request.TransactionRecordRequestDto;
import com.td.client.dto.request.WithdrawRequestDto;
import com.td.client.dto.response.TransactionRecordPageResponseDto;
import com.td.client.dto.response.TransactionRecordResponseDto;
import com.td.common.base.PageRequest;

import java.util.HashMap;
import java.util.List;

public interface TransactionService {
    void recharge(RechargeRequestDto rechargeRequestDto);

    String getAddress();

    void withdraw(WithdrawRequestDto withdrawRequestDto);

    TransactionRecordPageResponseDto record(PageRequest<TransactionRecordRequestDto> pageRequest);
}
