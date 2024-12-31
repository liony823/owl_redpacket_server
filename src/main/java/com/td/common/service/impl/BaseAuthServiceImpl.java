package com.td.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.td.common.base.LoginUser;
import com.td.common.dto.AuthParamsDto;
import com.td.common.exception.CustomException;
import com.td.common.service.AuthService;
import com.td.common.utils.AuthenticationContextHolder;
import com.td.common.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 21:03
 */
@Slf4j
@Service("baseAuthServiceImpl")
public class BaseAuthServiceImpl implements AuthService {

    @Autowired
    private TokenUtils tokenUtils;


    @Resource
    private AuthenticationManager authenticationManager;

    @Override
    public String execute(AuthParamsDto authParamsDto) {

        // 用户验证
        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(JSON.toJSON(authParamsDto), authParamsDto.getPassword());

            AuthenticationContextHolder.setContext(authenticationToken);

            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);

        } catch (Exception e) {
            log.info("登录异常信息: ", e);
            throw new CustomException(e.getMessage());
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        //生成token
        return null;
    }


}
