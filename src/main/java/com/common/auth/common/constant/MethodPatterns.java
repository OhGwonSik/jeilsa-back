package com.common.auth.common.constant;

/**
 * AOP 메서드 패턴 상수 정의 클래스
 * DataSourceRoutingAspect와 TransactionConfig에서 공통으로 사용
 */
public final class MethodPatterns {
    
    private MethodPatterns() {
        // 유틸리티 클래스 - 인스턴스 생성 방지
    }
    
    /**
     * 서비스 레이어 기본 패키지 패턴
     */
    private static final String SERVICE_PACKAGE = "com.common.auth.*.service.*";
    
    /**
     * 읽기 메서드 패턴 배열 (TransactionConfig 용)
     */
    public static final String[] READ_METHOD_PATTERNS = {
        "get*", "select*", "find*", "search*", "list*", 
        "count*", "exists*", "check*", "validate*", "verify*", "is*"
    };
    
    /**
     * 쓰기 메서드 패턴 배열 (TransactionConfig 용)
     */
    public static final String[] WRITE_METHOD_PATTERNS = {
        "create*", "save*", "insert*", "add*", "update*", "modify*", "edit*",
        "delete*", "remove*", "clear*", "reset*", "login*", "logout*", "refresh*",
        "cleanup*", "process*", "execute*", "change*", "enable*", "disable*",
        "activate*", "deactivate*", "record*", "lock*", "unlock*", "assign*",
        "revoke*", "rotate*", "generate*", "batch*", "bulk*", "upsert*", "softDelete*"
    };
    
    /**
     * 읽기 전용 메서드 패턴들 (AspectJ용 - 컴파일 타임 상수)
     */
    public static final String READ_METHODS = 
            "execution(* " + SERVICE_PACKAGE + ".get*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".select*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".find*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".search*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".list*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".count*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".exists*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".check*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".validate*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".verify*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".is*(..))";
    
    /**
     * 쓰기 메서드 패턴들 (AspectJ용 - 컴파일 타임 상수)
     */
    public static final String WRITE_METHODS = 
            "execution(* " + SERVICE_PACKAGE + ".create*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".save*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".insert*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".add*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".update*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".modify*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".edit*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".delete*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".remove*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".clear*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".reset*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".login*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".logout*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".refresh*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".cleanup*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".process*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".execute*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".change*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".enable*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".disable*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".activate*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".deactivate*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".record*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".lock*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".unlock*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".assign*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".revoke*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".rotate*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".generate*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".batch*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".bulk*(..))" +
            " || execution(* " + SERVICE_PACKAGE + ".upsert*(..))" + 
            " || execution(* " + SERVICE_PACKAGE + ".softDelete*(..))";
    
    /**
     * Audit 서비스 제외 패턴
     */
    public static final String EXCLUDE_AUDIT = " && !execution(* com.common.auth.*.service.Audit*.*(..))";
    
    /**
     * 트랜잭션 설정용 읽기 메서드 패턴 (Audit 제외)
     */
    public static final String READ_METHODS_WITH_EXCLUSION = "(" + READ_METHODS + ")" + EXCLUDE_AUDIT;
    
    /**
     * 트랜잭션 설정용 쓰기 메서드 패턴 (Audit 제외)
     */
    public static final String WRITE_METHODS_WITH_EXCLUSION = "(" + WRITE_METHODS + ")" + EXCLUDE_AUDIT;
    
}