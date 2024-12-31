package com.td.common.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-07 9:52
 */
@Data
@Accessors(chain = true)
@Document(collection = "contribution_value_sign_record")
public class ContributionValueSignRecord {

    @Id
    private String id; // 唯一标识

    /**
     * 用户ID，外键，引用 Users 表的 userId
     */
    @Field("user_id")
    private String userId;

    /**
     * 签到类型
     */
    @ApiModelProperty(value = "签到类型",
            example = "normal_sign_in",
            allowableValues = "normal_sign_in, continuous_sign_in",
            notes = "normal_sign_in: 普通签到\n"
                    + "continuous_sign_in: 连续签到")
    @Field("sign_type")
    private String signType;

    /**
     * 签到日期
     */
    @Field("sign_date")
    private String signDate;

    /**
     * 创建时间
     */
    @Field("create_time")
    private String createTime;


}
