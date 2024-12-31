package com.td.client.service;

import com.td.client.dto.request.ReceiveRedPacketRequestDto;
import com.td.client.dto.response.ReceiveRedPacketResponseDto;

public interface RedPacketReceiveService {
    ReceiveRedPacketResponseDto receiveRedPacket(ReceiveRedPacketRequestDto receiveRedPacketRequestDto);
}
