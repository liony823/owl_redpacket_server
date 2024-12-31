package com.td.common.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-15 10:41
 */
@Data
@Document(collection = "attribute")
public class Attribute {

    @Id
    private String _id;

    @Field("user_id")
    private String userId;

    @Field("acount")
    private String acount;

    @Field("address")
    private String address;

    @Field("public_key")
    private String publicKey;
}
