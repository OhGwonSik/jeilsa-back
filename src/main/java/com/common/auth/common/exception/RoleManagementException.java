package com.common.auth.common.exception;

public class RoleManagementException extends BusinessException {
    public RoleManagementException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public RoleManagementException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public RoleManagementException(ErrorCode errorCode, String message, String details) {
        super(errorCode, message, details);
    }
    
    public RoleManagementException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}