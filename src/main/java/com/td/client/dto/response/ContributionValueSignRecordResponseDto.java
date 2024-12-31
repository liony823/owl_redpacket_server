package com.td.client.dto.response;

import com.td.common.pojo.ContributionValueSignRecord;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-07 11:22
 */
@Data
@Accessors(chain = true)
public class ContributionValueSignRecordResponseDto {

    /**
     * 今天是否签到
     */
    private Boolean todayIsSign;

    /**
     * 当月的签到记录
     */
    private List<ContributionValueSignRecord> signRecord;
}
