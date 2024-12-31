package com.td.client.service;

import com.td.client.dto.request.SendRedPacketRequestDto;
import com.td.client.dto.response.SendRedPacketResponseDto;

public interface RedPacketSendService {
    SendRedPacketResponseDto sendRedPacket(SendRedPacketRequestDto sendRedPacketRequestDto);
}
