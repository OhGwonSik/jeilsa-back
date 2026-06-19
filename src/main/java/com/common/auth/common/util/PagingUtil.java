package com.common.auth.common.util;

import java.util.List;

import com.common.auth.common.dto.PageResponseDTO;
import com.common.auth.common.pagination.PagingContext;
import com.common.auth.common.pagination.PagingContextHolder;
import com.github.pagehelper.Page;


/**
 * 페이징 관련 유틸리티 클래스
 */
public class PagingUtil {
    
    public static <T> PageResponseDTO<T> toPageResponse(List<T> list) {
        if (list == null) {
            return new PageResponseDTO<>();
        }
        
        // PageHelper의 Page 객체인 경우
        if (!list.isEmpty() && list instanceof Page) {
            Page<T> page = (Page<T>) list;
            return PageResponseDTO.<T>builder()
                    .list(list)
                    .pageNum(page.getPageNum())
                    .pageSize(page.getPageSize())
                    .total(page.getTotal())
                    .pages(page.getPages())
                    .hasNextPage(page.getPageNum() < page.getPages())
                    .hasPreviousPage(page.getPageNum() > 1)
                    .build();
        }
        
        // 일반 리스트인 경우
        return PageResponseDTO.<T>builder()
                .list(list)
                .pageNum(1)
                .pageSize(list.size())
                .total(list.size())
                .pages(1)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .build();
    }
    
    /**
     * 현재 페이징 컨텍스트를 사용해서 PageResponseDTO 생성
     */
    public static <T> PageResponseDTO<T> toPageResponseWithContext(List<T> list) {
        PagingContext context = PagingContextHolder.getPagingContext();
        if (context == null) {
            // 페이징 컨텍스트가 없는 경우 일반적인 변환 수행
            return toPageResponse(list);
        }
        
        return toPageResponse(list);
    }
    
    /**
     * PageHelper ORDER BY 문자열 생성
     */
    public static String buildOrderBy(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "reg_dt DESC";
        }
        
        String direction = (sortDirection == null || sortDirection.trim().isEmpty()) 
            ? "DESC" : sortDirection.toUpperCase();
            
        // SQL Injection 방지를 위한 기본적인 검증
        if (!isValidSortDirection(direction)) {
            direction = "DESC";
        }
        
        return sortBy + " " + direction;
    }
    
    /**
     * 정렬 방향이 유효한지 검증
     */
    private static boolean isValidSortDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) || "DESC".equalsIgnoreCase(direction);
    }
    
    /**
     * 정렬 컬럼명이 유효한지 검증 (SQL Injection 방지)
     */
    public static boolean isValidSortColumn(String column) {
        if (column == null || column.trim().isEmpty()) {
            return false;
        }
        
        // 기본적인 컬럼명 패턴 검증 (영문자, 숫자, 언더스코어만 허용)
        return column.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
}