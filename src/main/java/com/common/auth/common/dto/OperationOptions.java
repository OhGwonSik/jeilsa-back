package com.common.auth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "처리 옵션")
@Getter
@Setter
@ToString
public class OperationOptions {
    //----- Field -----//
    @Schema(description = "오류 발생 시 전체 중단 여부", example = "true")
    private boolean stopOnError = true;
    
    //----- Example -----//
    // @Schema(description = "모든 항목 검증 여부", example = "true")
    // private boolean validateAll = true;
    
    // @Schema(description = "일괄 처리 크기", example = "100")
    // private int bulkSize = 100;
    
    // @Schema(description = "기존 데이터 덮어쓰기 여부", example = "true")
    // private boolean overwriteExisting = true;
}