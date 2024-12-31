package com.td.client.service;

import com.td.client.dto.request.ReceiveRedPacketRequestDto;
import com.td.client.dto.request.SendRedPacketRequestDto;
import com.td.client.dto.response.ReceiveRedPacketRecordResponseDto;
import com.td.client.dto.response.ReceiveRedPacketResponseDto;
import com.td.client.dto.response.RedPacketStatusResponseDto;
import com.td.client.dto.response.SendRedPacketResponseDto;

public interface RedPacketService {
    RedPacketStatusResponseDto redPacketStatus(String redPacketId);

    SendRedPacketResponseDto sendRedPacket(SendRedPacketRequestDto sendRedPacketRequestDto);

    ReceiveRedPacketResponseDto receiveRedPacket(ReceiveRedPacketRequestDto receiveRedPacketRequestDto);

    String balance();

    ReceiveRedPacketRecordResponseDto receiveRecord(String redPacketId);

    ReceiveRedPacketRecordResponseDto receiveRecord2(String redPacketId);

    void handlerRedPacketExpire(String redPacketId);
}
