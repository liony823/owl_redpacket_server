package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-14 21:56
 */
@Data
@Accessors(chain = true)
@Document(collection = "contribution_value_balance_record")
public class ContributionValueBalanceRecord {

    private String id; // 唯一标识

    /**
     * 用户ID，外键，引用 Users 表的 userId
     */
    private String userId;

    /**
     * 时间
     */
    @Field("create_time")
    private String createTime;

    /**
     * 余额
     */
    private double balance;
}
