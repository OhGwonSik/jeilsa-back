package com.common.auth.common.exception;

public class AuthenticationException extends BusinessException {
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public AuthenticationException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }
    
    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public AuthenticationException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}