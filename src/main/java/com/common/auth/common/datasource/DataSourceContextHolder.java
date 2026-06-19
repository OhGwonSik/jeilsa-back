package com.common.auth.common.datasource;

/**
 * 현재 스레드의 데이터소스 컨텍스트를 관리하는 홀더
 */
public class DataSourceContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    
    public static void setDataSource(String dataSource) {
        contextHolder.set(dataSource);
    }
    
    public static String getDataSource() {
        return contextHolder.get();
    }
    
    public static void clearDataSource() {
        contextHolder.remove();
    }
}