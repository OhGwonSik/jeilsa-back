package com.common.auth.common.aop;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.pagination.PagingContext;
import com.common.auth.common.pagination.PagingContextHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @AutoPaging 어노테이션이 붙은 메소드에 자동으로 페이징을 적용하는 AOP 어스펙트
 */
@Slf4j
@Aspect
@Component
@Order(1) // 다른 AOP보다 먼저 실행되도록 우선순위 설정
@RequiredArgsConstructor
public class AutoPagingAspect {
    // ----- Constants -----//
    private static final String DEFAULT_PAGE_PARAM = "page";
    private static final String DEFAULT_SIZE_PARAM = "size";
    private static final String DEFAULT_SORT_BY_PARAM = "sortBy";

    //----- DI Fields -----//
    private final ObjectMapper objectMapper;

    /**
     * @AutoPaging 어노테이션이 붙은 메소드에 페이징 적용
     */
    @Around("@annotation(com.common.auth.common.annotation.AutoPaging)")
    public Object applyAutoPaging(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        return applyPagingAndExecute(joinPoint, method.getAnnotation(AutoPaging.class));
    }

    private Object applyPagingAndExecute(ProceedingJoinPoint joinPoint, AutoPaging autoPaging) throws Throwable {
        try {
            // HTTP 요청에서 페이징 파라미터 추출
            PagingContext pagingContext = extractPagingFromRequest(joinPoint, autoPaging);

            // 페이징 컨텍스트 설정
            PagingContextHolder.setPagingContext(pagingContext);

            // PageHelper 시작
            PageHelper.startPage(
                    pagingContext.getPage(),
                    pagingContext.getSize(),
                    pagingContext.toOrderByString());

            log.debug("Auto paging applied - Page: {}, Size: {}, Sort: {}",
                    pagingContext.getPage(),
                    pagingContext.getSize(),
                    pagingContext.toOrderByString());
            
            return joinPoint.proceed();

        } finally {
            // 페이징 컨텍스트 정리
            PagingContextHolder.clearPagingContext();
            PageHelper.clearPage();
        }
    }

    private PagingContext extractPagingFromRequest(ProceedingJoinPoint joinPoint, AutoPaging autoPaging) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            Map<String, Object> bodyParams = null; // Body 파라미터를 담을 Map

            // 1. POST이고 Content-Type이 application/json인 경우 Body를 읽어 Map으로 변환
            if ("POST".equalsIgnoreCase(request.getMethod()) && request.getContentType() != null
                    && request.getContentType().contains("application/json")) {
                // 필터에서 래핑된 request를 가져옴
                final ContentCachingRequestWrapper cachingRequest = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
                // cachingRequest가 null이 아닌지 반드시 확인해야 합니다.
                if (cachingRequest != null) {
                    byte[] content = cachingRequest.getContentAsByteArray();
                    if (content.length > 0) {
                        try {
                            // JSON Body를 Map으로 파싱
                            bodyParams = objectMapper.readValue(new String(content), new TypeReference<Map<String, Object>>() {});
                        } catch (IOException e) {
                            log.error("Failed to parse request body", e);
                        }
                    }
                }
            }

            // 어노테이션 기본값 또는 기본 설정값 사용
            int defaultPage = (autoPaging != null) ? autoPaging.defaultPage() : 1;
            int defaultSize = (autoPaging != null) ? autoPaging.defaultSize() : 20;
            int maxSize = (autoPaging != null) ? autoPaging.maxSize() : 100;
            String defaultSort = (autoPaging != null) ? autoPaging.defaultSort() : "reg_dt";

            // 2. 파라미터 추출 (Body 우선, 없으면 Query Parameter 사용)
            int page = getPagingParam(request, bodyParams, DEFAULT_PAGE_PARAM, Integer.class, defaultPage);
            int size = getPagingParam(request, bodyParams, DEFAULT_SIZE_PARAM, Integer.class, defaultSize);
            String rawSortBy = getPagingParam(request, bodyParams, DEFAULT_SORT_BY_PARAM, String.class, defaultSort);

            // 컬럼 매핑 적용 (ORDER BY 문자열 완성)
            String orderByClause = mapSortColumn(joinPoint, autoPaging, rawSortBy);

            // 페이지 크기 제한
            if (size > maxSize) {
                size = maxSize;
                log.warn("Requested page size {} exceeds maximum {}, using maximum", size, maxSize);
            }

            // 최소값 보장
            page = Math.max(page, 1);
            size = Math.max(size, 1);

            return PagingContext.builder()
                    .page(page)
                    .size(size)
                    .sortBy(orderByClause)
                    .build();
        }

        // HTTP 요청이 없는 경우 기본값 사용
        return PagingContext.getDefault();
    }

    /**
     * Request Body 또는 Query Parameter에서 페이징 파라미터를 가져오는 헬퍼 메서드
     */
    private <T> T getPagingParam(HttpServletRequest request, Map<String, Object> bodyParams, String paramName,
            Class<T> clazz, T defaultValue) {
        // 1. Request Parameter에서 먼저 찾기
        String paramValue = request.getParameter(paramName);
        Object value = null;

        // 2. Request Parameter에 없으면 Request Body에서 찾기
        if (paramValue == null && bodyParams != null) {
            value = bodyParams.get(paramName);
        } else if (paramValue != null) {
            value = paramValue;
        }

        // 3. 값이 없으면 기본값 반환
        if (value == null) {
            return defaultValue;
        }

        // 4. 기본값 타입에 따라 변환
        if (clazz.equals(Integer.class)) {
            try {
                if (value instanceof Number) {
                    // 모든 숫자 타입(Integer, Long, Double 등)에 대해 int 값으로 변환
                    return clazz.cast(Integer.valueOf(((Number) value).intValue()));
                }
                if (value instanceof String) {
                    // 문자열은 Integer로 파싱
                    return clazz.cast(Integer.valueOf((String) value));
                }
            } catch (NumberFormatException e) {
                // 파싱 실패 시 기본값 반환
                return defaultValue;
            }
        }

        if (clazz.equals(String.class) && value instanceof String) {
            return clazz.cast(value);
        }

        return defaultValue;
    }

    /**
     * 동적 정렬 컬럼 매핑 및 ORDER BY 문자열 생성
     * 
     * 지원 형식:
     * 1. 단일 컬럼: "testName" 
     * 2. 여러 컬럼: "testName,delYn,regDt"
     * 3. 방향 포함: "testName:ASC,delYn:DESC"
     */
    private String mapSortColumn(ProceedingJoinPoint joinPoint, AutoPaging autoPaging, String rawSortBy) {
        if (rawSortBy == null || rawSortBy.trim().isEmpty()) {
            return getDefaultSort(autoPaging);
        }

        // 여러 컬럼 처리: "col1:ASC,col2:DESC,col3" 형태
        String[] sortColumns = rawSortBy.split(",");
        StringBuilder orderByClause = new StringBuilder();
        
        for (int i = 0; i < sortColumns.length; i++) {
            String columnSpec = sortColumns[i].trim();
            String[] parts = columnSpec.split(":");
            
            String columnName = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim().toUpperCase() : "ASC";
            
            // 방향 검증
            if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
                direction = "ASC";
            }
            
            // 서비스별 컬럼명 매핑
            String mappedColumn = mapSingleColumnViaService(joinPoint, autoPaging, columnName);
            
            if (i > 0) {
                orderByClause.append(", ");
            }
            orderByClause.append(mappedColumn).append(" ").append(direction);
        }
        
        String result = orderByClause.toString();
        log.debug("Sort column mapping: {} -> {}", rawSortBy, result);
        return result;
    }
    
    /**
     * 서비스의 컬럼 매핑 메소드를 호출하여 단일 컬럼 매핑
     */
    private String mapSingleColumnViaService(ProceedingJoinPoint joinPoint, AutoPaging autoPaging, String columnName) {
        if (autoPaging == null || autoPaging.columnMapperMethod().trim().isEmpty()) {
            // 컬럼 매핑 메소드가 지정되지 않은 경우 원본 반환
            return columnName;
        }

        try {
            Object target = joinPoint.getTarget();
            String methodName = autoPaging.columnMapperMethod();

            // 서비스 클래스에서 지정된 메소드 호출
            Method mapperMethod = target.getClass().getDeclaredMethod(methodName, String.class);
            mapperMethod.setAccessible(true);

            Object result = mapperMethod.invoke(target, columnName);
            if (result instanceof String) {
                String mappedColumn = (String) result;
                return mappedColumn != null ? mappedColumn : columnName;
            }

        } catch (Exception e) {
            log.warn("Failed to map sort column '{}' using method '{}': {}",
                    columnName, autoPaging.columnMapperMethod(), e.getMessage());
        }

        return columnName;
    }
    
    /**
     * 기본 정렬 컬럼 반환
     */
    private String getDefaultSort(AutoPaging autoPaging) {
        if (autoPaging != null && !autoPaging.defaultSort().trim().isEmpty()) {
            return autoPaging.defaultSort();
        }
        return "reg_dt"; // 전체 기본값
    }
}