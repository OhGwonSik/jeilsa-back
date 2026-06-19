package com.common.auth.audit.util;

import java.util.Arrays;
import java.util.List;

import com.common.auth.audit.constants.AuditConstants;

import lombok.experimental.UtilityClass;

/**
 * Audit 시스템 통합 유틸리티 - 공통 소스
 */
@UtilityClass
public class AuditUtil {
    
    // ========== 파라미터 처리 관련 메서드 ==========
    
    /**
     * 파라미터 키 정규화 (MyBatis foreach, 배치 처리 대응)
     */
    public static String normalizeParamKey(String original) {
        if (original == null || original.isBlank()) {
            return original;  // null-safe: 원본 그대로 반환
        }
        
        String key = original;
        
        // MyBatis foreach 접두사 제거: __frch_item_0.email -> email
        key = key.replaceFirst(AuditConstants.MYBATIS_FOREACH_PREFIX_REGEX, "");
        
        // 중첩 프로퍼티에서 마지막 부분만 추출: user.profile.email -> email
        int lastDotCharacterIndex = key.lastIndexOf('.');
        if (lastDotCharacterIndex >= 0 && lastDotCharacterIndex < key.length() - 1) {
            key = key.substring(lastDotCharacterIndex + 1);
        }
        
        // 배열 인덱스 제거: items[0] -> items
        return key.replaceAll(AuditConstants.ARRAY_INDEX_REGEX, "");
    }
    
    /**
     * 파라미터 키 정규화 및 SNAKE_UPPER 변환
     */
    public static String normalizeAndSnakeUpper(String paramKey) {
        String normalized = normalizeParamKey(paramKey);
        return toSnakeUpper(normalized);
    }
    
    // ========== 데이터베이스 명명 규칙 처리 ==========
    
    /**
     * 테이블/컬럼 공통 명칭 정규화 (따옴표 제거 + 스키마 분리, 케이스 보존)
     */
    public static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        
        String normalized = name.replace(AuditConstants.DB_IDENTIFIER_QUOTE, "");
        
        int lastDotIndex = normalized.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            normalized = normalized.substring(lastDotIndex + 1);
        }
        return normalized;
    }

    /**
     * PK 컬럼 CSV 파싱 (공백 정리, 불변 리스트 반환)
     */
    public static List<String> parsePkColumns(String pkColumnCsv) {
        if (pkColumnCsv == null || pkColumnCsv.isBlank()) {
            return List.of();
        }
        
        return Arrays.stream(pkColumnCsv.split(","))
                     .map(String::trim)
                     .filter(string -> !string.isEmpty())
                     .map(string -> string.replace(AuditConstants.DB_IDENTIFIER_QUOTE, ""))
                     .map(AuditUtil::normalizeName)
                     .toList();
    }
    
    // ========== 컬렉션 유틸리티 ==========
    
    /**
     * 리스트에 이름이 포함되는지 대소문자 무시하여 확인
     */
    public static boolean containsIgnoreCase(List<String> names, String target) {
        if (names == null || target == null) {
            return false;
        }
        
        for (String candidateName : names) {
            if (candidateName != null && candidateName.equalsIgnoreCase(target)) {
                return true;
            }
        }
        
        return false;
    }
    
    // ========== JSON 처리 유틸리티 ==========
    
    /**
     * JSON 값 정규화 (숫자/불린/null/json객체/배열은 그대로, 나머지는 JSON 문자열로 이스케이프)
     */
    public static String normalizeJsonValue(String raw) {
        if (raw == null) {
            return null;
        }
        
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        
        char firstChar = trimmed.charAt(0);
        if (firstChar == '"' || firstChar == '{' || firstChar == '[') {
            return trimmed;
        }
        
        if ("null".equals(trimmed) || "true".equals(trimmed) || "false".equals(trimmed)) {
            return trimmed;
        }
        
        if (isNumericString(trimmed)) {
            return trimmed;
        }
        
        return "\"" + trimmed.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
    
    /**
     * 숫자 문자열 판별
     */
    public static boolean isNumericString(String str) {
        return str.chars()
                .allMatch(ch -> Character.isDigit(ch) || "-.+eE".indexOf(ch) >= 0);
    }
    
    // ========== 케이스 변환 메서드 ==========
    
    /**
     * camelCase -> SNAKE_UPPER 변환 
     */
    public static String toSnakeUpper(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int characterIndex = 0; characterIndex < camelCase.length(); characterIndex++) {
            char currentChar = camelCase.charAt(characterIndex);
            if (Character.isUpperCase(currentChar) && characterIndex > 0) {
                result.append("_");
            }
            result.append(Character.toUpperCase(currentChar));
        }
        
        return result.toString();
    }
    
    /**
     * SNAKE_CASE -> camelCase 변환
     */
    public static String toCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        
        String[] parts = snakeCase.toLowerCase().split("_");
        if (parts.length <= 1) {
            return snakeCase.toLowerCase();
        }
        
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        for (int partIndex = 1; partIndex < parts.length; partIndex++) {
            if (!parts[partIndex].isEmpty()) {
                result.append(Character.toUpperCase(parts[partIndex].charAt(0)))
                      .append(parts[partIndex].substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
}
