package com.common.auth.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메소드에 자동 페이징을 적용하는 어노테이션
 * select*WithFilter 패턴의 메소드에 사용
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoPaging {
    
    /**
     * 기본 페이지 번호 (1부터 시작)
     */
    int defaultPage() default 1;
    
    /**
     * 기본 페이지 크기
     */
    int defaultSize() default 20;
    
    /**
     * 최대 페이지 크기 제한
     */
    int maxSize() default 100;
    
    /**
     * 기본 정렬 (컬럼명과 방향 포함)
     * 예: "reg_dt DESC", "test_name ASC", "name ASC, reg_dt DESC"
     */
    String defaultSort() default "reg_dt DESC";
    
    /**
     * 컬럼 매핑을 위한 서비스 메소드명
     * 서비스 클래스에서 SortColumnMapper를 구현한 메소드명을 지정
     * 지정하지 않으면 기본 매핑 로직 사용
     */
    String columnMapperMethod() default "";
}