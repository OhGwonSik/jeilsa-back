package com.jeil.delivery.deliverylogic.mapper;

import java.util.Set;

public final class CompanySortMapper {
    private static final Set<String> ALLOWED_COLS = Set.of(
            "companyId", "companyNm", "telNo", "regionCd", "shipperYn", "regDt", "chgDt"
    );

    public static String toOrderBy(String sortCol, String sortDir) {
        String dir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        if (sortCol == null || !ALLOWED_COLS.contains(sortCol)) {
            return "com.company_id DESC"; // 기본 정렬
        }
        // 컬럼 매핑
        String col = switch (sortCol) {
            case "companyId" -> "com.company_id";
            case "companyNm" -> "com.company_nm";
            case "telNo"     -> "com.tel_no";
            case "regionCd"  -> "com.region_cd";
            case "shipperYn" -> "com.shipper_yn";
            case "regDt"     -> "com.reg_dt";
            case "chgDt"     -> "com.chg_dt";
            default -> "com.company_id";
        };
        return col + " " + dir;
    }
}