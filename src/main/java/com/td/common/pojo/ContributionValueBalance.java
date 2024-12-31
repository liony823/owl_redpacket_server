package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-07 9:27
 */
@Data
@Accessors(chain = true)
@Document(collection = "contribution_value_balance")
public class ContributionValueBalance {

    @Id
    private String id; // 唯一标识

    /**
     * 用户ID，外键，引用 Users 表的 userId
     */
    @Field("user_id")
    private String userId;

    /**
     * 余额
     */
    private double balance;
}
