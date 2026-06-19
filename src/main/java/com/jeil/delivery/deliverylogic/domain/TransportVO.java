package com.jeil.delivery.deliverylogic.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class TransportVO {

    // 검색 조건
    String startDate;             // "yyyy-MM-dd"
    String endDate;               // "yyyy-MM-dd"
    Integer deliveryRouteId;
    String continueMode;          // 연속 저장 모드 플래그 ("true" / null)

    Integer transportId;
    Integer totalAmount;
    Integer totalSenderAmount;
    Integer totalReceiverAmount;


    // 발신
    Integer senderCompanyId;
    String senderCompanyNm;
    String senderTransportShipperCd;
    String senderTransportShipperNm;
    String chargeCd;
    String chargeNm;
    String senderRegionCd;
    String senderRegionNm;
    String senderRegionDtlCd;
    String senderRegionDtlNm;
    Integer senderUntpc;
    Integer senderWeightUntpc;
    String senderManagerNm;
    String senderAddress;
    String senderTelNo;
    String senderManagerTelNo;
    String senderRmk;
    String senderDeliveryRouteNm;

    // 수신
    Integer receiverCompanyId;
    String receiverCompanyNm;
    String receiverTransportShipperCd;
    String receiverTransportShipperNm;
    String receiverRegionCd;
    String receiverRegionNm;
    String receiverRegionDtlCd;
    String receiverRegionDtlNm;
    Integer receiverUntpc;
    Integer receiverWeightUntpc;
    String receiverAddress;
    String receiverTelNo;
    String receiverManagerTelNo;
    String receiverManagerNm;
    String receiverRmk;
    String receiverDeliveryRouteNm;

    // 공통
    Integer regId;
    LocalDateTime regDt;
    Integer chgId;
    LocalDateTime chgDt;
    String delYn; // 'Y' / 'N'

    // 물품
    List<TransportDtlVO> transportDtl;

    Integer transportDtlId;

    String calculationCd;     // 정산코드 (예: QTY/WEIGHT 등)
    String senderReceiverCd;  // S/R/T 등 필요 시
    String waybillNo;         // 운송장 번호
    String kindCd;            // TAC 코드 등
    String kindCdNm;

    Integer untpc;         // 단가
    Integer weightUntpc;
    Integer qty;           // 수량
    Integer amount;        // 금액

    Integer receiverQty;           // 수량
    Integer receiverAmount;        // 금액

    String rmk;
    String shipmentOperationDate; // 물류 작업 일
}
