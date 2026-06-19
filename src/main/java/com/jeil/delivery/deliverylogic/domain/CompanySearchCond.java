package com.jeil.delivery.deliverylogic.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CompanySearchCond {
    // --- 검색 필드 (기존 CompanyDTO와 1:1 맵핑) ---
    private Integer companyId;          // com.company_id = ?
    private String companyNm;        // 업체명/전화 복합 검색 (기존 로직 그대로 XML에서 처리)
    private String telNo;            // com.tel_no LIKE ?
    private String shipperYn;        // com.shipper_yn = 'Y'/'N'
    private String shipperCd;        // shi.shipper_cd = ?
    private String regionCd;         // com.region_cd = ?
    private Integer billCompanyId;      // shi.bill_company_id = ?

    // --- 권한 필드 (기존 로직 유지) ---
    private Boolean isUser;          // true이면 userCompanyId로 제한
    private Integer userCompanyId;      // com.company_id = userCompanyId

    // --- 페이징/정렬 ---
    private int limit;           // LIMIT
    private int offset;          // OFFSET
    private String orderBy;          // 화이트리스트 변환 결과("com.company_id DESC" 등)
}
