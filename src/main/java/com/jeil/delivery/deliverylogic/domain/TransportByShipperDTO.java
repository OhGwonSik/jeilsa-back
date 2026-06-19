package com.jeil.delivery.deliverylogic.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TransportByShipperDTO {

	private Integer billId;
	private Integer billDtlId;

	//화주별 청구서
    private Integer companyId;     // 회사 ID
    private String companyNm;      // 회사명
    private String type;           // 발신 / 수신 / 합계

    private Integer documents;     // 서류 수량
    private Integer boxes;         // 박스 수량
    private Integer roll;          // 롤 수량
    private Integer samples;       // 샘플 수량
    private Integer tarp;          // 갑바 수량
    private Integer bags;          // 마대 수량
    private Integer luggage;       // 행랑 수량

    private Integer total;         // 합계
    private Integer sortOrder;     // 정렬 순서 (발신=1, 수신=2, 합계=3)
    private String calculationTargetCd; // 청구대상
    private String calculationTargetNm; // 청구대상

    //화주별 내역
    private Integer transportId; // 운송 ID
    private Integer transportDtlId; // 운송 상세 ID
    private String shipmentOperationDate; // 물류일자
    private String senderRegionCd; //발신지역코드
    private String senderRegionNm; //발신지역명
    private String senderRegionDtlCd; //발신지역상세코드
    private String senderRegionDtlNm; //발신지역상세명
    private Integer senderCompanyId; //발신회사 ID
    private String senderCompanyNm; //발신회사명
    private String senderTelNo; // 발신 번호
    private String senderManagerNm; //발신매니저명
    private String senderManagerTelNo; //발신매니저번호
    private Integer receiverCompanyId; //수신회사ID
    private String receiverCompanyNm; // 수신회사명
    private String receiverTelNo; // 수신 번호
    private String receiverManagerNm; //수신매니저명
    private String receiverManagerTelNo; //수신매니저번호
    private String calculationCd; //거래코드
    private String calculationNm; // 거래코드명
    private String kindCd; // 물품종류코드
    private String kindNm; // 물품종류명
    private Integer comQty; // 택배수량
    private Integer weightQty; // 통신수량
    private Integer prepaidAmount; //선불가격
    private Integer collectAmount; //착불가격
    private String rmk; // 비고

    private Integer year;
    private Integer month;
    private String startDate;     // 검색조건 startDate
    private String endDate;       // 검색조건 endDate

    private Integer userCompanyId;
    private Boolean isUser; //유저여부
}
