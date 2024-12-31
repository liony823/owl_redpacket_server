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
 * @date 2024-09-07 9:29
 */
@Data
@Accessors(chain = true)
@Document(collection = "contribution_value_record")
public class ContributionValueRecord {

    @Id
    private String id; // 唯一标识

    /**
     * 用户ID，外键，引用 Users 表的 userId
     */
    @Field("user_id")
    private String userId;

    /**
     * 类型
     */
    @ApiModelProperty(value = "贡献值记录的类型",
            example = "sign_in",
            allowableValues = "sign_in, continuous_sign_in, online_time_five_minutes, online_time_twenty_minutes, online_time_sixty_minutes, red_packet_balance_incentive, exchange_owl",
            notes = "sign_in: 每日签到\n"
                    + "continuous_sign_in: 连续7天签到\n"
                    + "online_time_five_minutes: 每天在线五分钟\n"
                    + "online_time_twenty_minutes: 每天在线20分钟\n"
                    + "online_time_sixty_minutes: 每天在线60分钟\n"
                    + "red_packet_balance_incentive: 红包余额激励\n"
                    + "exchange_owl: 兑换owl")
    private String type;

    @Field("contribution_value_change")
    private String contributionValueChange;

    @Field("owl_count")
    private String owlCount;

    @Field("create_time")
    private String createTime;
}
