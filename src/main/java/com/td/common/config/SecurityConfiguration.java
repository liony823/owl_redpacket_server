package com.td.common.config;

import com.td.common.filter.JwtAuthenticationTokenFilter;
import com.td.common.handler.AuthenticationFailHandlerImpl;
import com.td.common.handler.LogoutSuccessHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Td
 * @ClassName SecurityConfiguration
 * @description: Security配置类
 * @date 2023-12-04
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private PermitAllUrlProperties permitAllUrl;
    /**
     * 退出处理类
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * 认证失败处理类
     */
    @Autowired
    private AuthenticationFailHandlerImpl unauthorizedHandler;

    /**
     * token认证过滤器
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * 自定义用户认证逻辑
     */
    @Autowired
    @Qualifier("AccessUserDetailsService")
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //放行所有请求
        http.cors().and().csrf().disable()
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
                // permitAll 配置
                .antMatchers(permitAllUrl.getUrls().toArray(new String[0])).permitAll()
                //放行swagger
                .antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/webjars/**").anonymous()
                // 对于登录login 注册register 验证码captchaImage 允许匿名访问 忘记密码, 不建议使用AntPathRequestMatcher
                .antMatchers("/login", "/admin/login", "/register", "/captchaImage", "/callback/**", "/forgot", "/forgot/**", "/upload","/ws/**").anonymous()
                //登录后也可以访问
                .antMatchers("/test/*", "/callback/applet").permitAll()
                // 静态资源，可匿名访问
                .antMatchers(HttpMethod.GET, "/", "/*.html", "/*/*.html", "/*.*/*.css", "/*.*/*.js", "/profile/*").permitAll()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated().and().headers().frameOptions().disable().and().logout().logoutUrl("/logout")
                // 添加Logout filter
                .logoutSuccessHandler(logoutSuccessHandler);

        // 添加JWT filter
        http.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        //自定义用户认证逻辑
        http.userDetailsService(userDetailsService);

    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


}
