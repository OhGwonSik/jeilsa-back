package com.common.auth.common.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 동적 데이터소스 라우팅을 처리하는 클래스
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = DataSourceContextHolder.getDataSource();
        return dataSource != null ? dataSource : "auth.write";
    }
}