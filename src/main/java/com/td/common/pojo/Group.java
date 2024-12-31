package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-10 14:03
 */
@Data
@Accessors(chain = true)
@Document(collection = "group")
public class Group {

    @Id
    private String id;

    /**
     * 群组id
     */
    @Field("group_id")
    private String groupID;

    /**
     * 群组名称
     */
    @Field("group_name")
    private String groupName;

    /**
     * 通知
     */
    private String notification;

    /**
     * 介绍
     */
    private String introduction;

    /**
     * 群组头像
     */
    @Field("face_url")
    private String faceUrl;

    private String ex;

}
