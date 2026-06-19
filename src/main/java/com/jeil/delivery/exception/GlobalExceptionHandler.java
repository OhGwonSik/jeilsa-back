package com.jeil.delivery.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.exception.UserManagementException;
import com.common.auth.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("Business exception occurred: {} - {}", errorCode.getCode(), ex.getMessage());

        String details = shouldIncludeDetails() ? ex.getDetails() : null;
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), ex.getMessage(), details);

        HttpStatus status = getHttpStatusForErrorCode(errorCode);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 인증 예외 처리
     */
    @ExceptionHandler(com.common.auth.common.exception.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(com.common.auth.common.exception.AuthenticationException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("Authentication exception occurred: {} - {}", errorCode.getCode(), ex.getMessage());

        String details = shouldIncludeDetails() ? ex.getDetails() : null;
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), ex.getMessage(), details);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 사용자 관리 예외 처리
     */
    @ExceptionHandler(UserManagementException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserManagementException(UserManagementException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("User management exception occurred: {} - {}", errorCode.getCode(), ex.getMessage());

        String details = shouldIncludeDetails() ? ex.getDetails() : null;
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), ex.getMessage(), details);

        HttpStatus status = getHttpStatusForErrorCode(errorCode);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 검증 예외 처리
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("Validation exception occurred: {} - {}", errorCode.getCode(), ex.getMessage());

        String details = shouldIncludeDetails() ? ex.getDetails() : null;
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), ex.getMessage(), details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 데이터베이스 접근 예외 처리
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException ex) {
        log.error("Database access error occurred: {}", ex.getMessage(), ex);

        // 프로덕션 환경에서는 상세 오류 메시지를 숨기고 일반적인 오류 메시지를 반환
        String errorMessage = "데이터베이스 작업 중 오류가 발생했습니다.";
        String details = shouldIncludeDetails() ? ex.getMessage() : null;

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                errorMessage,
                details
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Spring Validation 예외 처리
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        String errorMessage;
        String details = null;

        if (ex instanceof MethodArgumentNotValidException validEx) {
            errorMessage = validEx.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            if (shouldIncludeDetails()) {
                details = validEx.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining("; "));
            }
        } else if (ex instanceof BindException bindEx) {
            errorMessage = bindEx.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            if (shouldIncludeDetails()) {
                details = bindEx.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining("; "));
            }
        } else {
            errorMessage = "Validation failed";
        }

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), errorMessage, details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 잘못된 JSON 형식 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON format: {}", ex.getMessage());

        String details = shouldIncludeDetails() ? ex.getMessage() : null;
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INVALID_REQUEST_FORMAT.getCode(),
                "Invalid request format",
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 메서드 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch: {}", ex.getMessage());

        String message = String.format("Invalid parameter type for '%s'", ex.getName());
        String details = shouldIncludeDetails() ? ex.getMessage() : null;

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), message, details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 지원하지 않는 HTTP 메서드 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("HTTP method not supported: {}", ex.getMessage());

        String details = shouldIncludeDetails() ? ex.getMessage() : null;
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.OPERATION_NOT_ALLOWED.getCode(),
                "HTTP method not supported",
                details
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 권한 부족 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        String details = shouldIncludeDetails() ? ex.getMessage() : null;
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INSUFFICIENT_PERMISSIONS.getCode(),
                "Access denied",
                details
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 일반적인 IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        String details = shouldIncludeDetails() ? ex.getMessage() : null;
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), ex.getMessage(), details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 예상치 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

//        String details = shouldIncludeDetails() ? ex.getMessage() : null;
        String details = ex.getMessage();
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "서버 내부 오류가 발생했습니다.",
                details
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 에러 코드에 따른 HTTP 상태 코드 매핑
     */
    private HttpStatus getHttpStatusForErrorCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case AUTHENTICATION_FAILED, TOKEN_EXPIRED, INVALID_TOKEN -> HttpStatus.UNAUTHORIZED;
            case ACCOUNT_LOCKED, ACCOUNT_DISABLED -> HttpStatus.FORBIDDEN;
            case USER_NOT_FOUND, RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case USER_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case VALIDATION_FAILED, INVALID_REQUEST_FORMAT, REQUIRED_FIELD_MISSING -> HttpStatus.BAD_REQUEST;
            case INSUFFICIENT_PERMISSIONS -> HttpStatus.FORBIDDEN;
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case INTERNAL_SERVER_ERROR, DATABASE_ERROR, EXTERNAL_SERVICE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    /**
     * 상세 에러 정보 포함 여부 결정
     */
    private boolean shouldIncludeDetails() {
        return !"prod".equals(activeProfile);
    }

    /**
     * 매핑되지 않은 요청 (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        String details = shouldIncludeDetails() ? ex.getMessage() : null;
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                "No endpoint " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                details
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
