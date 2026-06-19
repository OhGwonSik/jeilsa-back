package com.jeil.delivery.deliverylogic.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WayBillVO {
	private Integer waybillId;     // 운송장 ID (PK)
    private Integer qty;           // 수량
    private String startNo;        // 시작 번호
    private String endNo;          // 종료 번호
    private String chargeCd;       // 요금구분

    private Integer regId;         // 등록자 ID
    private LocalDateTime regDt;   // 등록 일시
    private Integer chgId;         // 수정자 ID
    private LocalDateTime chgDt;   // 수정 일시
    private String delYn;          // 삭제 여부

    private Integer senderCompanyId; // 수신처 pk
    private String senderCompanyNm; // 수신처명
    private String senderTelNo; // 수신처전화
    private String senderAddress; // 수신처주소
    private String senderManagerTelNo; //수신처 관리자 번호

    private Integer receiverCompanyId; // 발신처 pk
    private String receiverCompanyNm; // 발신처명
    private String receiverTelNo; // 발신처전화
    private String receiverAddress; // 발신처주소
    private String receiverManagerTelNo; //발신처 관리자 번호


    private String searchNo; // 검색 조건에 사용하는 운송장 번호 입력 값
    private Long total;
}
