package com.common.auth.common.pagination;

/**
 * ThreadLocal을 사용한 페이징 컨텍스트 홀더
 */
public class PagingContextHolder {
    private static final ThreadLocal<PagingContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    /**
     * 현재 스레드에 페이징 컨텍스트 설정
     */
    public static void setPagingContext(PagingContext context) {
        CONTEXT_HOLDER.set(context);
    }
    
    /**
     * 현재 스레드의 페이징 컨텍스트 반환
     */
    public static PagingContext getPagingContext() {
        return CONTEXT_HOLDER.get();
    }
    
    /**
     * 현재 스레드의 페이징 컨텍스트 제거
     */
    public static void clearPagingContext() {
        CONTEXT_HOLDER.remove();
    }
    
    /**
     * 페이징 컨텍스트가 설정되어 있는지 확인
     */
    public static boolean hasPagingContext() {
        return CONTEXT_HOLDER.get() != null;
    }
}