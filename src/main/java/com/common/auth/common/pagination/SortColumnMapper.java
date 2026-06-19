package com.common.auth.common.pagination;

/**
 * 정렬 컬럼 매핑을 위한 인터페이스
 * 각 서비스에서 허용되는 정렬 컬럼을 정의하고 검증
 */
public interface SortColumnMapper {
    /**
     * 클라이언트에서 전달된 sortBy 파라미터를 실제 DB 컬럼명으로 변환
     * @param sortBy 클라이언트에서 전달된 정렬 컬럼명
     * @return 실제 DB 컬럼명 (null이면 기본 정렬 사용)
     */
    String mapSortColumn(String sortBy);
    
    /**
     * 기본 정렬 컬럼 반환
     * @return 기본 정렬 컬럼명
     */
    String getDefaultSortColumn();
    
    /**
     * 기본 정렬 방향 반환
     * @return 기본 정렬 방향 (ASC/DESC)
     */
    String getDefaultSortDirection();
}