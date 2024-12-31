package com.td.client.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-05 14:34
 */
@Data
@Accessors(chain = true)
public class SendRedPacketResponseDto {

    /**
     * 红包id
     */
    private String redPacketId;


}
