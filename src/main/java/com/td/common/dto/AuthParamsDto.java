package com.td.common.dto;

import lombok.Data;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 16:56
 */
@Data
public class AuthParamsDto {

    private String username; //用户名
    private String password;
    private String cellphone;//手机号
    private String code;//验证码
    private String authType; // 认证的类型   password:用户名密码模式类型    sms:短信模式类型

}
