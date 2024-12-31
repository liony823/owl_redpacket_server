package com.td.client.dto.response;

import com.td.client.dto.base.ReceiveRedPacketRecordDto;
import com.td.common.pojo.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-07 16:49
 */
@Data
@Accessors(chain = true)
public class ReceiveRedPacketRecordResponseDto {
    private List<ReceiveRedPacketRecordDto> receiveRedPacketRecordDtoList;
    private User sendUserInfo;

    /**
     * 红包金额
     */
    private Double amount;

    /**
     * 红包个数
     */
    private Integer totalCount;

    /**
     * 表情
     */
    private String emoji;

    /**
     * 备注
     */
    private String remark;

    private RedPacketStatusResponseDto redPacketStatus;

    /**
     * 退款消息
     */
    private Integer customType;
    private String content;
}
