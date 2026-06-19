package com.common.auth.audit.constants;

import java.util.List;
import java.util.Map;

import com.common.auth.audit.dto.KeyValueDTO;

import lombok.experimental.UtilityClass;

/**
 * Audit 시스템 공통 상수 
 */
@UtilityClass
public final class AuditConstants {
    
    // 성능 최적화: 불변 빈 컬렉션 재사용
    public static final List<KeyValueDTO> EMPTY_KEYVALUE_LIST = List.of();
    public static final List<List<KeyValueDTO>> EMPTY_ROWS_LIST = List.of();
    public static final List<String> EMPTY_STRING_LIST = List.of();
    public static final Map<String,Integer> EMPTY_Integer_MAP = Map.of();
    
    // DB 호환성 상수 
    public static final String DB_IDENTIFIER_QUOTE = "\"";
    
    // 파라미터 처리 상수 (복잡한 정규식만 상수화)
    public static final String MYBATIS_FOREACH_PREFIX_REGEX = "^__frch_[^.]+_\\d+\\.";
    public static final String ARRAY_INDEX_REGEX = "\\[\\d+\\]";
    
    // 타입 식별 상수
    public static final String Integer_TYPE = "Integer";

    // 정규식 상수
    public static final String REGEX_Integer_CANONICAL = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    public static final String REGEX_TRIM_QUOTED_IDENTIFIER = "^\"+|\"+$";
}
