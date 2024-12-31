package com.td.client.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-07 15:46
 */
@Data
@Accessors(chain = true)
public class ContributionValueOnlineTimeResponseDto {

    /**
     * 今日在线时长
     */
    private Long todayOnlineTime;

    /**
     * 总时长
     */
    private Long totalOnlineTime;
}
