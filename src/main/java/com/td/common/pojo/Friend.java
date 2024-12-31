package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-15 15:29
 */
@Data
@Document(collection = "friend")
@Accessors(chain = true)
public class Friend {
    @Id
    private String _id;

    @Field("owner_user_id")
    private String ownerUserId;

    @Field("friend_user_id")
    private String friendUserId;

    private String remark;

    @Field("add_source")
    private String addSource;

    @Field("operator_user_id")
    private String operatorUserId;

    private String ex;

    @Field("is_pinned")
    private String isPinned;

}
