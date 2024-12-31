package com.td.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.td.common.dto.AuthParamsDto;
import com.td.common.exception.CustomException;
import com.td.common.service.AuthService;
import com.td.common.utils.AuthenticationContextHolder;
import com.td.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 17:38
 */
@Service("up_AuthServiceImpl")
public class UsernamePasswordAuthServiceImpl implements AuthService {


    @Override
    public String execute(AuthParamsDto authParamsDto) {

//        String username = authParamsDto.getUsername();
//        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUsername, username));
//        if (user == null) {
//            throw new CustomException("用户名不存在！");
//        }
//
//        Authentication context = AuthenticationContextHolder.getContext();
//        String password = context.getCredentials().toString();
//
//        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
//            throw new CustomException("密码错误！");
//        }

        return null;
    }
}
