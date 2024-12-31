package com.td.client.controller;

import com.td.client.dto.request.ReceiveRedPacketRequestDto;
import com.td.client.dto.request.RedPacketIdRequestDto;
import com.td.client.dto.request.SendRedPacketRequestDto;
import com.td.client.service.RedPacketService;
import com.td.common.annotations.RedisSynchronized;
import com.td.common.base.ResponseResult;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-03 13:44
 */
@RestController
@RequestMapping("/v1/redPacket")
public class RedPacketController {

    @Autowired
    private RedPacketService redPacketService;

    /**
     * 查询用户余额
     */
    @PostMapping("/balance")
    public ResponseResult balance() {
        return ResponseResult.success("操作成功", redPacketService.balance());
    }

    /**
     * 查询红包状态
     *
     * @param redPacketId 红包id
     * @return ResponseResult 红包状态
     */
    @PostMapping("/status")
    @ApiOperation(value = "查询红包状态", notes = "查询红包状态")
    @ApiResponse(code = 200, message = "成功", response = ResponseResult.class)
    public ResponseResult receiveDetail(@RequestBody RedPacketIdRequestDto redPacketIdRequestDto) {
        return ResponseResult.success(redPacketService.redPacketStatus(redPacketIdRequestDto.getClientMsgID()));
    }

    /**
     * 发送红包接口
     */
    @PostMapping("/send")
    @RedisSynchronized(key = "red-packet:lock:send")
    public ResponseResult sendRedPacket(@RequestBody SendRedPacketRequestDto sendRedPacketRequestDto) {
        return ResponseResult.success(redPacketService.sendRedPacket(sendRedPacketRequestDto));
    }

    /**
     * 领取红包
     */
    @PostMapping("/receive")
    @RedisSynchronized(key = "red-packet:lock:receive")
    public ResponseResult receiveRedPacket(@RequestBody ReceiveRedPacketRequestDto receiveRedPacketRequestDto) {
        return ResponseResult.success(redPacketService.receiveRedPacket(receiveRedPacketRequestDto));
    }


    /**
     * 红包领取记录
     */
    @PostMapping("/record")
    public ResponseResult receiveRecord(@RequestBody RedPacketIdRequestDto redPacketIdRequestDto) {
        return ResponseResult.success(redPacketService.receiveRecord(redPacketIdRequestDto.getClientMsgID()));
    }

}
