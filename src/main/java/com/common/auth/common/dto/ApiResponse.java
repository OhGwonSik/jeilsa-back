package com.common.auth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Schema(description = "API 응답 표준 포맷")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
    //----- Fields-----//
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "에러 정보")
    private ErrorInfo error;
    
    @Schema(description = "응답 시간")
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }
    
    public static <T> ApiResponse<T> error(String code, String message) {
        return error(code, message, null);
    }
    
    public static <T> ApiResponse<T> error(String code, String message, String details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorInfo(code, message, details);
        return response;
    }
    
    @Schema(description = "에러 상세 정보")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        @Schema(description = "에러 코드", example = "AUTH_001")
        private String code;
        
        @Schema(description = "에러 메시지", example = "Invalid credentials")
        private String message;
        
        @Schema(description = "상세 에러 정보 (개발 환경에서만 제공)")
        private String details;
        
        public ErrorInfo(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}