package com.common.auth.common.service;

import com.common.auth.common.dto.PerformanceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class PerformanceMonitorService {
    
    // JVM 힙 메모리 최대값 (서버 시작 시 한 번만 설정)
    private final long maxHeapMemoryMB;
    
    // 메서드별 통계 저장
    private final ConcurrentHashMap<String, MethodPerformanceStats> methodStats = new ConcurrentHashMap<>();
    
    public PerformanceMonitorService() {
        // JVM 힙 메모리 최대값 자동 감지
        Runtime runtime = Runtime.getRuntime();
        this.maxHeapMemoryMB = runtime.maxMemory() / (1024 * 1024);
        
        log.info("PerformanceMonitor 초기화 완료 - JVM 힙 메모리: {}MB", maxHeapMemoryMB);
    }
    
    /**
     * 성능 모니터링 시작
     */
    public PerformanceMetrics startMonitoring(String operationType, String className, String methodName) {
        PerformanceMetrics metrics = PerformanceMetrics.builder()
                .operationType(operationType)
                .className(className)
                .methodName(methodName)
                .startTime(LocalDateTime.now())
                .startMemoryMB(getCurrentMemoryMB())
                .maxHeapMemoryMB(maxHeapMemoryMB)
                .build();
        
        log.info("성능 모니터링 시작: {} - {}.{}", operationType, className, methodName);
        return metrics;
    }
    
    /**
     * 성능 모니터링 완료
     */
    public void completeMonitoring(PerformanceMetrics metrics) {
        metrics.setEndTime(LocalDateTime.now());
        metrics.setEndMemoryMB(getCurrentMemoryMB());
        metrics.calculateMetrics();
        
        // 통계 업데이트
        updateMethodStats(metrics);
        
        // 상세 로깅
        logDetailedPerformance(metrics);
        
        // 경고 로그 (심각한 상황)
        logWarnings(metrics);
    }
    
    /**
     * 현재 메모리 사용량 조회 (MB)
     */
    public long getCurrentMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return usedMemory / (1024 * 1024);
    }
    
    /**
     * JVM 힙 메모리 최대값 조회
     */
    public long getMaxHeapMemoryMB() {
        return maxHeapMemoryMB;
    }
    
    /**
     * 현재 메모리 사용률 계산
     */
    public double getCurrentMemoryUsagePercentage() {
        long currentMemory = getCurrentMemoryMB();
        return (double) currentMemory / maxHeapMemoryMB * 100.0;
    }
    
    /**
     * 메서드별 통계 업데이트
     */
    private void updateMethodStats(PerformanceMetrics metrics) {
        String key = metrics.getClassName() + "." + metrics.getMethodName();
        methodStats.computeIfAbsent(key, k -> new MethodPerformanceStats())
                  .updateStats(metrics);
    }
    
    /**
     * 상세 성능 로깅
     */
    private void logDetailedPerformance(PerformanceMetrics metrics) {
        log.info("성능 측정 완료: {} - {}.{}", metrics.getOperationType(), metrics.getClassName(), metrics.getMethodName());
        log.info("  소요시간: {}ms", metrics.getExecutionTimeMs());
        log.info("  메모리: 시작 {}MB, 종료 {}MB, 변화량 {}MB", 
            metrics.getStartMemoryMB(), metrics.getEndMemoryMB(), metrics.getMemoryDeltaMB());
        log.info("  메모리 상태: {}", metrics.getMemoryStatus());
        log.info("  메모리 사용률: {}MB / {}MB ({}%)", 
            metrics.getEndMemoryMB(), metrics.getMaxHeapMemoryMB(), 
            String.format("%.1f", metrics.getMemoryUsagePercentage()));
    }
    
    /**
     * 경고 로그 출력
     */
    private void logWarnings(PerformanceMetrics metrics) {
        if (metrics.isMemoryCritical()) {
            log.error("메모리 위험 상황: {} - {} ({}%)", 
                metrics.getOperationType(), metrics.getMethodName(), 
                String.format("%.1f", metrics.getMemoryUsagePercentage()));
        }
        
        if (metrics.isPerformanceCritical()) {
            log.error("성능 위험 상황: {} - {} ({}ms)", 
                metrics.getOperationType(), metrics.getMethodName(), metrics.getExecutionTimeMs());
        }
    }
    
    /**
     * 메서드별 성능 통계 조회
     */
    public MethodPerformanceStats getMethodStats(String methodKey) {
        return methodStats.get(methodKey);
    }
    
    /**
     * 전체 통계 조회
     */
    public ConcurrentHashMap<String, MethodPerformanceStats> getAllMethodStats() {
        return methodStats;
    }
    
    /**
     * 메서드별 성능 통계 클래스
     */
    public static class MethodPerformanceStats {
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong totalMemoryDelta = new AtomicLong(0);
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong criticalMemoryCount = new AtomicLong(0);
        private final AtomicLong criticalPerformanceCount = new AtomicLong(0);
        
        public void updateStats(PerformanceMetrics metrics) {
            totalExecutionTime.addAndGet(metrics.getExecutionTimeMs());
            totalMemoryDelta.addAndGet(metrics.getMemoryDeltaMB());
            callCount.incrementAndGet();
            
            if (metrics.isMemoryCritical()) {
                criticalMemoryCount.incrementAndGet();
            }
            
            if (metrics.isPerformanceCritical()) {
                criticalPerformanceCount.incrementAndGet();
            }
        }
        
        public long getAverageExecutionTime() {
            long count = callCount.get();
            return count > 0 ? totalExecutionTime.get() / count : 0;
        }
        
        public long getAverageMemoryDelta() {
            long count = callCount.get();
            return count > 0 ? totalMemoryDelta.get() / count : 0;
        }
        
        public long getCallCount() {
            return callCount.get();
        }
        
        public long getCriticalMemoryCount() {
            return criticalMemoryCount.get();
        }
        
        public long getCriticalPerformanceCount() {
            return criticalPerformanceCount.get();
        }
        
        public double getCriticalMemoryRate() {
            long count = callCount.get();
            return count > 0 ? (double) criticalMemoryCount.get() / count * 100.0 : 0.0;
        }
        
        public double getCriticalPerformanceRate() {
            long count = callCount.get();
            return count > 0 ? (double) criticalPerformanceCount.get() / count * 100.0 : 0.0;
        }
    }
}
