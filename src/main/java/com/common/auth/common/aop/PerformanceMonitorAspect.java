package com.common.auth.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.common.auth.common.service.PerformanceMonitorService;
import com.common.auth.common.dto.PerformanceMetrics;

@Slf4j
@Aspect
@Component
@Order(300) // AuditAspect(100) 이후에 실행
@RequiredArgsConstructor
public class PerformanceMonitorAspect {
    
    private final PerformanceMonitorService performanceMonitorService;
    
    // 성능 모니터링 대상 메서드 포인트컷
    @Pointcut("execution(* com.common.auth..service..*bulk*(..)) || " +
               "execution(* com.common.auth..service..*batch*(..))")
    public void performanceMonitoringTargets() {}
    
    /**
     * 성능 모니터링
     */
    @Around("performanceMonitoringTargets()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // 성능 모니터링 시작
        PerformanceMetrics metrics = performanceMonitorService.startMonitoring(
            "PERFORMANCE_MONITORING", className, methodName
        );
        
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            // 예외 발생 시에도 메트릭스 완료 처리
            log.error("성능 모니터링 중 예외 발생: {}.{} - {}", className, methodName, e.getMessage());
            throw e;
        } finally {
            // 성능 모니터링 완료 (항상 실행)
            performanceMonitorService.completeMonitoring(metrics);
        }
    }
}
