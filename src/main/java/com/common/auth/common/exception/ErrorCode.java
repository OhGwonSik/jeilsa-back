package com.common.auth.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 인증 관련 에러 (AUTH_xxx)
    AUTHENTICATION_FAILED("AUTH_001", "비밀번호가 틀렸습니다."),
    ACCOUNT_LOCKED("AUTH_002", "Account is locked due to too many failed login attempts. Please contact administrator."),
    ACCOUNT_DISABLED("AUTH_003", "탈퇴처리된 계정입니다."),
    TOKEN_EXPIRED("AUTH_004", "Token has expired"),
    INVALID_TOKEN("AUTH_005", "Invalid token"),
    TOKEN_REVOKE_FAILED("AUTH_006", "Failed to revoke token"),
    LOGOUT_FAILED("AUTH_007", "Failed to logout"),
    
    // 사용자 관리 에러 (USER_xxx)
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS("USER_002", "User already exists"),
    USER_CREATE_FAILED("USER_003", "Failed to create user"),
    USER_UPDATE_FAILED("USER_004", "Failed to update user"),
    USER_DELETE_FAILED("USER_005", "Failed to delete user"),
    PASSWORD_CHANGE_FAILED("USER_006", "Failed to change password"),
    ACCOUNT_UNLOCK_FAILED("USER_007", "Failed to unlock account"),
    
    // 권한 관리 에러 (ROLE_xxx)
    ROLE_NOT_FOUND("ROLE_001", "Role not found"),
    ROLE_ASSIGN_FAILED("ROLE_002", "Failed to assign roles"),
    INSUFFICIENT_PERMISSIONS("ROLE_003", "Insufficient permissions"),
    ROLE_ALREADY_EXISTS("ROLE_004", "Role already exists"),
    ROLE_CREATE_FAILED("ROLE_005", "Failed to create role"),
    ROLE_UPDATE_FAILED("ROLE_006", "Failed to update role"),
    ROLE_DELETE_FAILED("ROLE_007", "Failed to delete role"),
    ROLE_IN_USE("ROLE_008", "Role is in use and cannot be deleted"),
    
    // 검증 에러 (VALID_xxx)
    VALIDATION_FAILED("VALID_001", "Validation failed"),
    INVALID_REQUEST_FORMAT("VALID_002", "Invalid request format"),
    REQUIRED_FIELD_MISSING("VALID_003", "Required field is missing"),
    
    // 서버 에러 (SERVER_xxx)
    INTERNAL_SERVER_ERROR("SERVER_001", "Internal server error"),
    DATABASE_ERROR("SERVER_002", "Database operation failed"),
    EXTERNAL_SERVICE_ERROR("SERVER_003", "External service error"),
    
    // 기타 에러 (COMMON_xxx)
    RESOURCE_NOT_FOUND("COMMON_001", "Resource not found"),
    OPERATION_NOT_ALLOWED("COMMON_002", "Operation not allowed"),
    RATE_LIMIT_EXCEEDED("COMMON_003", "Rate limit exceeded"),
    DUPLICATE_RESOURCE("COMMON_004", "Duplicate resource");
    
    //----- Fields-----//
    private final String code;
    private final String message;
    
    @Override
    public String toString() {
        return String.format("%s: %s", this.code, this.message);
    }
}