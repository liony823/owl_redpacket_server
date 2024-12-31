package com.td.common.pojo;


import java.io.Serializable;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @TableName user
 */
@Data
@Document(collection = "user")
@Accessors(chain = true)
public class User implements Serializable {

    @Id
    private String _id;

    @Field("user_id")
    private String userId;

    private String nickname;

    @Field("face_url")
    private String faceUrl;

    private String ex;


}
