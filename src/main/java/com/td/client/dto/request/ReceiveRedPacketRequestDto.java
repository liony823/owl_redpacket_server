package com.td.client.dto.request;

import com.td.common.pojo.RedPacket;
import lombok.Data;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-05 9:11
 */
@Data
public class ReceiveRedPacketRequestDto {
        private String redPacketId;

        private RedPacket redPacket;
}
