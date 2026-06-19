package com.jeil.delivery.deliverylogic.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WaybillSearchCond {
    private Integer waybillId;
    private String senderCompanyNm;
    private String receiverCompanyNm;
    private String searchNo;      // 숫자/하이픈 혼합 검색
    private String orderBy;       // 화이트리스트 된 정렬식
    private int limit;
    private int offset;
}

