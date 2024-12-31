package com.td.client.dto.base;

import com.td.common.pojo.Friend;
import com.td.common.pojo.User;
import lombok.Data;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-15 15:36
 */
@Data
public class FriendDto extends User {

    private Friend friendInfo;
}
