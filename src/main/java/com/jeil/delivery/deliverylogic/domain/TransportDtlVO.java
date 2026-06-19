package com.jeil.delivery.deliverylogic.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class TransportDtlVO {
    Integer transportId;
    Integer transportDtlId;
    String startDate;             // "yyyy-MM-dd"
    String endDate;               // "yyyy-MM-dd"

    String calculationCd;     // 정산코드 (예: QTY/WEIGHT 등)
    String senderReceiverCd;  // S/R/T 등 필요 시
    String calculationTargetCd;  // S/R/T 등 필요 시
    String waybillNo;         // 운송장 번호
    String kindCd;            // TAC 코드 등

    Integer untpc;         // 단가
    Integer qty;           // 수량
    Integer amount;        // 금액

    Integer receiverUntpc;         // 단가
    Integer receiverQty;           // 수량
    Integer receiverAmount;        // 금액

    String rmk;
    String shipmentOperationDate; // 물류 작업 일

    Integer regId;
    LocalDateTime regDt;
    Integer chgId;
    LocalDateTime chgDt;
    String delYn;      // 'Y' / 'N'
}
