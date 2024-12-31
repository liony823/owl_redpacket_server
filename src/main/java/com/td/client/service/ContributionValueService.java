package com.td.client.service;

import com.td.client.dto.request.ExchangeRequestDto;
import com.td.client.dto.response.ContributionValueOnlineTimeResponseDto;
import com.td.client.dto.response.ContributionValueSignRecordResponseDto;
import com.td.client.dto.response.ContributionValueTaskStatusResponseDto;
import com.td.common.pojo.ContributionValueRecord;

import java.util.List;

public interface ContributionValueService {


    Boolean sign();

    List<ContributionValueRecord> record();

    Double balance();

    Double getBalanceByUserId(String userId);

    ContributionValueSignRecordResponseDto signRecord();

    String exchangeRate();

    Boolean exchange(ExchangeRequestDto exchangeRequestDto);

    ContributionValueOnlineTimeResponseDto onlineTime();

    ContributionValueOnlineTimeResponseDto getOnlineTimeByUserId(String userId);

    List<ContributionValueTaskStatusResponseDto> taskStatus();

    List<ContributionValueTaskStatusResponseDto> getTaskStatusByUserId(String userId);

    void contributionValueIncentive();
}
