package com.td.common.filter;

import com.td.common.base.LoginUser;
import com.td.common.pojo.User;
import com.td.common.utils.RedisUtils;
import com.td.common.utils.SecurityUtils;
import com.td.common.utils.TokenUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 16:10
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private TokenUtils tokenUtils;
    ;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private RedisUtils redisUtils;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        //获取请求体中的operationID
        String operationID = request.getHeader("operationID");

        LoginUser loginUser = null;

        if (StringUtils.hasText(operationID)) {
            loginUser = new LoginUser();
            loginUser.setId(operationID);

        } else {
            loginUser = tokenUtils.getLoginUser(request);
        }

        if (loginUser != null) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        chain.doFilter(request, response);
    }
}
