package com.common.auth.common.pagination;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 페이징 컨텍스트 정보를 담는 클래스
 */
@Getter
@Builder
@ToString
public class PagingContext {
    //----- Fields-----//
    private final int page;
    private final int size;
    private final String sortBy; // 이미 방향이 포함된 ORDER BY 문자열
    
    /**
     * 기본 페이징 컨텍스트 생성
     */
    public static PagingContext getDefault() {
        return PagingContext.builder()
                .page(1)
                .size(20)
                .sortBy("reg_dt DESC")
                .build();
    }
    
    /**
     * PageHelper용 ORDER BY 문자열 반환
     * sortBy에 이미 방향이 포함되어 있으므로 그대로 반환
     */
    public String toOrderByString() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "reg_dt DESC";
        }
        return sortBy;
    }
}