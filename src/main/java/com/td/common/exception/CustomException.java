package com.td.common.exception;


public class CustomException extends RuntimeException{

    public CustomException() {
        super();
    }

    public CustomException(String message) {
        super(message);
    }
}
