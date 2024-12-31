package com.td.common.handler;

import com.alibaba.fastjson.JSON;
import com.td.common.base.LoginUser;
import com.td.common.dto.AuthParamsDto;
import com.td.common.exception.CustomException;
import com.td.common.pojo.SysUser;
import com.td.common.pojo.User;
import com.td.common.service.AuthService;
import com.td.common.utils.AuthenticationContextHolder;
import com.td.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 16:47
 */
@Slf4j
@Primary
@Service("AccessUserDetailsService")
public class AccessUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ApplicationContext beanFactory;


    @Override
    public UserDetails loadUserByUsername(String authParamDtoStr) throws UsernameNotFoundException {
        AuthParamsDto authParamDto = JSON.parseObject(authParamDtoStr, AuthParamsDto.class);
        String authType = authParamDto.getAuthType();

        //后台用户认证
        if ("admin".equals(authType)) {

//            SysUser sysUser = sysUserMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, authParamDto.getUsername()));
//
//            if (sysUser == null) {
//                throw new CustomException("用户名不存在！");
//            }
//
//
//            Authentication context = AuthenticationContextHolder.getContext();
//            String password = context.getCredentials().toString();
//
//            if (!SecurityUtils.matchesPassword(password, sysUser.getPassword())) {
//                throw new CustomException("密码错误！");
//            }
//
//            return LoginUser.createSysUser(sysUser.getId(), sysUser, null, null);
            return null;
        }


        //客户端用户认证 根据认证的方式拼接出处理此次认证的service
        String beanName = authType + "_AuthServiceImpl";
        AuthService authService;

        try {
            authService = (AuthService) beanFactory.getBean(beanName);
        } catch (Exception e) {
            throw new CustomException("认证方式不存在！");
        }


        User user = JSON.parseObject(authService.execute(authParamDto), User.class);
        return LoginUser.createClientUser(user);
    }
}
