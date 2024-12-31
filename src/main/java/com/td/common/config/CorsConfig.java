package com.td.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            //设置允许跨域的路径
            .addMapping("/**")
            //设置允许跨域请求的域名
            .allowedOriginPatterns("*")
                //设置是否允许携带cookie
            .allowCredentials(true)
            //设置允许的请求方式
            .allowedMethods("GET","POST","DELETE","PUT")
            //设置允许携带哪些请求头
            .allowedHeaders("*")
            //跨域允许时间 3600s 
            //作用：实际上每次跨域请求过来之后 浏览器都会发一个请求去询问服务器是否允许此次跨域请求
            //如果每次都需要询问的话 那么会影响性能 所以添加此时间后 
            //当一次跨域请求被允许后 在这个时间内 该请求将不在询问服务器 直接
            .maxAge(3600);
    }
}