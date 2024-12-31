package com.td.common.utils;

import org.springframework.security.core.Authentication;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 17:36
 */
public class AuthenticationContextHolder {
    private static final ThreadLocal<Authentication> contextHolder = new ThreadLocal<>();

    public static Authentication getContext() {
        return contextHolder.get();
    }

    public static void setContext(Authentication context) {
        contextHolder.set(context);
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}
