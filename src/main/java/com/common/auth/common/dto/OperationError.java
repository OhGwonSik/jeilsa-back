package com.common.auth.common.dto;




import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Schema(description = "처리 오류 정보")
@Getter
@Setter
@ToString
@AllArgsConstructor
public class OperationError {
    //----- Fields-----//
    @Schema(description = "오류 ID")
    private String errorId;
    
    @Schema(description = "임시 ID (클라이언트 추적용)", example = "temp_001")
    private String tempId;
    
    @Schema(description = "역할 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private Integer roleId;
    
    @Schema(description = "권한 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private Integer permissionId;
    
    @Schema(description = "오류 메시지", example = "중복된 역할-권한 매핑입니다")
    private String errorMessage;
    
    @Schema(description = "오류 코드", example = "DUPLICATE_MAPPING")
    private String errorCode;
    
    @Schema(description = "오류 발생 시간")
    private LocalDateTime errorTime;

    private static String newErrorId() {
        long now = System.currentTimeMillis();                // 13자리
        int rand = ThreadLocalRandom.current().nextInt(1_000_000); // 000000~999999
        return Long.toString(now, 36) + "-" + Integer.toString(rand, 36); // 예: l5n8q1-2r9f
    }

    public OperationError() {
        this.errorId = newErrorId();
        this.errorTime = LocalDateTime.now();
    }
    
    public OperationError(String tempId, String errorMessage, String errorCode) {
        this();
        this.tempId = tempId;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
    
    public OperationError(String tempId, Integer roleId, Integer permissionId, String errorMessage, String errorCode) {
        this(tempId, errorMessage, errorCode);
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
}