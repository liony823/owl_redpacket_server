package com.td.common.controller;

import com.td.common.base.ResponseResult;
import com.td.common.dto.AuthParamsDto;
import com.td.common.service.AuthService;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 21:01
 */
@RestController
public class AuthController {




    @Autowired
    @Qualifier("baseAuthServiceImpl")
    private AuthService authService;

    @ApiModelProperty("用户登录")
    @PostMapping("/login")
    public ResponseResult login(@RequestBody AuthParamsDto authParamsDto) {
        String execute = authService.execute(authParamsDto);
        return ResponseResult.success("登录成功", execute);
    }
}
