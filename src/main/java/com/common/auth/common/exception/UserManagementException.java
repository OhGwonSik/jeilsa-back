package com.common.auth.common.exception;

public class UserManagementException extends BusinessException {
    public UserManagementException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public UserManagementException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }
    
    public UserManagementException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public UserManagementException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}