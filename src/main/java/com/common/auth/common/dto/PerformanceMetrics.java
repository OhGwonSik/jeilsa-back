package com.common.auth.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@Setter
@ToString
@Builder
public class PerformanceMetrics {
    
    private String operationType;        // INSERT, UPDATE, DELETE, BULK, BATCH
    private String className;            // 서비스 클래스명
    private String methodName;           // 메서드명
    
    private LocalDateTime startTime;    // 작업 시작 시간
    private LocalDateTime endTime;      // 작업 완료 시간
    
    private long startMemoryMB;          // 시작 시 메모리 사용량 (MB)
    private long endMemoryMB;            // 완료 시 메모리 사용량 (MB)
    private long memoryDeltaMB;          // 메모리 변화량 (MB)
    
    private long maxHeapMemoryMB;        // JVM 힙 메모리 최대값 (MB)
    private double memoryUsagePercentage; // 메모리 사용률 (%)
    private String memoryStatus;         // 적절/부적절 상태
    
    private long executionTimeMs;        // 소요시간 (밀리초)
    
    public void calculateMetrics() {
        // 소요시간 계산
        if (startTime != null && endTime != null) {
            this.executionTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
        
        // 메모리 변화량 계산
        if (startMemoryMB > 0 && endMemoryMB > 0) {
            this.memoryDeltaMB = endMemoryMB - startMemoryMB;
        }
        
        // 메모리 사용률 계산
        if (maxHeapMemoryMB > 0) {
            this.memoryUsagePercentage = (double) endMemoryMB / maxHeapMemoryMB * 100.0;
        }
        
        // 메모리 상태 판단
        this.memoryStatus = calculateMemoryStatus();
    }
    
    private String calculateMemoryStatus() {
        if (memoryUsagePercentage > 90) {
            return "위험 - 메모리 사용률 매우 높음 (" + String.format("%.1f", memoryUsagePercentage) + "%)";
        } else if (memoryUsagePercentage > 80) {
            return "경고 - 메모리 사용률 높음 (" + String.format("%.1f", memoryUsagePercentage) + "%)";
        } else if (memoryUsagePercentage > 70) {
            return "주의 - 메모리 사용률 보통 (" + String.format("%.1f", memoryUsagePercentage) + "%)";
        } else if (memoryUsagePercentage > 50) {
            return "양호 - 메모리 사용률 적정 (" + String.format("%.1f", memoryUsagePercentage) + "%)";
        } else {
            return "정상 - 메모리 사용률 우수 (" + String.format("%.1f", memoryUsagePercentage) + "%)";
        }
    }
    
    public boolean isMemoryCritical() {
        return memoryUsagePercentage > 90;
    }
    
    public boolean isPerformanceCritical() {
        return executionTimeMs > 3000;
    }
}
