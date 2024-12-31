package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;


@Data
@Accessors(chain = true)
@Document(collection = "red_packet_config")
public class RedPacketConfig implements Serializable {
    /**
     *
     */
    @Id
    private String id;

    /**
     *
     */
    private String configKey;

    /**
     *
     */
    private String configValue;

    /**
     *
     */
    private String description;


}