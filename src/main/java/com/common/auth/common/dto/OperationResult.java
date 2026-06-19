package com.common.auth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "일괄 처리 결과")
@Getter
@Setter
@ToString
public class OperationResult {
    //----- Fields -----//
    @Schema(description = "전체 처리 건수", example = "100")
    private int totalCount;
    
    @Schema(description = "성공 건수", example = "95")
    private int successCount;
    
    @Schema(description = "실패 건수", example = "5")
    private int failCount;
    
    @Schema(description = "처리 시작 시간")
    private LocalDateTime startTime;
    
    @Schema(description = "처리 완료 시간")
    private LocalDateTime endTime;
    
    @Schema(description = "처리 시간(밀리초)", example = "1500")
    private long processingTimeMs;
    
    @Schema(description = "오류 목록")
    private List<OperationError> errors;
    
    @Schema(description = "처리 요약 정보")
    private Map<String, Object> summary;
    
    @Schema(description = "성공한 아이템의 임시 ID 목록")
    private List<String> successfulTempIds;
    
    public OperationResult() {
        this.errors = new ArrayList<>();
        this.summary = new HashMap<>();
        this.successfulTempIds = new ArrayList<>();
        this.startTime = LocalDateTime.now();
    }
    
    public void markComplete() {
        this.endTime = LocalDateTime.now();
        this.processingTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
    }
    
    public void incrementSuccess() {
        this.successCount++;
        this.totalCount++;
    }

    public void incrementSuccess(int count) {
        this.successCount += count;
        this.totalCount += count;
    }
    
    public void incrementFail() {
        this.failCount++;
        this.totalCount++;
    }

    public void incrementFail(int count) {
        this.failCount += count;
        this.totalCount += count;
    }
    
    public void addError(String tempId, String errorMessage, String errorCode) {
        this.errors.add(new OperationError(tempId, errorMessage, errorCode));
        incrementFail();
    }
    
    public void addSuccessfulTempId(String tempId) {
        this.successfulTempIds.add(tempId);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public double getSuccessRate() {
        return totalCount > 0 ? (double) successCount / totalCount * 100.0 : 0.0;
    }
    
    public String getFormattedSuccessRate() {
        return String.format("%.1f%%", getSuccessRate());
    }
}