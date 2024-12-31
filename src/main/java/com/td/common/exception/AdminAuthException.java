package com.td.common.exception;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 16:17
 */
public class AdminAuthException extends RuntimeException {
    public AdminAuthException() {
        super();
    }

    public AdminAuthException(String message) {
        super(message);
    }

}
