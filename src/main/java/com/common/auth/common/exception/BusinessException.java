package com.common.auth.common.exception;

public class BusinessException extends RuntimeException {
    //----- Fields-----//
    private final ErrorCode errorCode;
    private final String details;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public BusinessException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage, String details) {
        super(customMessage);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public BusinessException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getDetails() {
        return details;
    }
}