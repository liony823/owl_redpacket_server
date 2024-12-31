package com.td.client.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-15 9:47
 */
@Data
@Accessors(chain = true)
public class TransactionRecordPageResponseDto {

    private long totalCount;
    private Integer pageSize;
    private long pageNum;
    private List<TransactionRecordResponseDto> record;
}
