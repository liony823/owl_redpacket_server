package com.td.common.handler;

import com.td.common.base.HttpStatus;
import com.td.common.base.ResponseResult;
import com.td.common.exception.AdminAuthException;
import com.td.common.exception.AuthException;
import com.td.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseResult authExceptionHandler(AuthException e) {
        log.info("AuthException：{}", e.getMessage());
        return ResponseResult.error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AdminAuthException.class)
    public ResponseResult adminAuthExceptionHandler(AdminAuthException e) {
        log.info("AuthException：{}", e.getMessage());
        return ResponseResult.error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseResult customExceptionHandler(CustomException e) {
        log.info("CustomException：{}", e.getMessage());
        return ResponseResult.error(HttpStatus.BUSINESS_ERROR, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult exceptionHandler(Exception e) {
        e.printStackTrace();
        return ResponseResult.error(HttpStatus.ERROR, e.getMessage());
    }
}
