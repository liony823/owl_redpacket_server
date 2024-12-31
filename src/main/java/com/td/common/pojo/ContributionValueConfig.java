package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-07 9:50
 */
@Data
@Accessors(chain = true)
@Document(collection = "contribution_value_config")
public class ContributionValueConfig {
    /**
     *
     */
    @Id
    private String id;

    private String configKey;

    private String configValue;

    private String description;
}
