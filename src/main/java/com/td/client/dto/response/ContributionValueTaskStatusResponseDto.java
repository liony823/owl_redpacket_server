package com.td.client.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-07 23:03
 */
@Data
@Accessors(chain = true)
public class ContributionValueTaskStatusResponseDto {

    /**
     * 任务条件
     * online_time_thirty_minutes:在线时长30分钟
     * online_time_sixty_minutes:在线时长60分钟
     * online_time_one_hundred_twenty_minutes:在线时长120分钟
     * online_time_one_hundred_eighty_minutes:在线时长180分钟
     */
    @ApiModelProperty(value = "任务条件",
            example = "online_time_thirty_minutes",
            allowableValues = "online_time_thirty_minutes, online_time_sixty_minutes, online_time_one_hundred_twenty_minutes, online_time_one_hundred_eighty_minutes",
            notes = "online_time_thirty_minutes:在线时长30分钟\n"
                    + "online_time_sixty_minutes:在线时长60分钟\n"
                    + "online_time_one_hundred_twenty_minutes:在线时长120分钟\n"
                    + "online_time_one_hundred_eighty_minutes:在线时长180分钟")
    private String taskCondition;

    /**
     * 任务奖励
     */
    private String taskReward;

    /**
     * 是否完成
     */
    private Boolean isComplete;
}
