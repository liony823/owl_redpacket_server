package com.td.common.handler;

import com.alibaba.fastjson.JSON;
import com.td.common.base.HttpStatus;
import com.td.common.base.ResponseResult;
import com.td.common.utils.ServletUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 16:25
 */
@Component
public class AuthenticationFailHandlerImpl implements AuthenticationEntryPoint, Serializable {


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {

        //获取访问路径
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/admin")) {
            //后台登录
            int code = HttpStatus.FORBIDDEN;
            String msg = "请求访问：" + requestURI + "，认证失败，无法访问系统资源";
            ServletUtils.renderString(response, JSON.toJSONString(ResponseResult.error(code, msg)));
            return;
        }

        int code = HttpStatus.UNAUTHORIZED;
        String msg = "请求访问：" + request.getRequestURI() + "，认证失败，无法访问系统资源";
        ServletUtils.renderString(response, JSON.toJSONString(ResponseResult.error(code, msg)));

    }
}
