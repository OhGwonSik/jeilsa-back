package com.jeil.delivery.billing.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BillDtlVO {

	private Integer billId;
	private Integer billDtlId;
	private Integer billDtlDgre;
	private Integer calculationCompanyId;
	private String companyNm;
	private String email;
	private String faxNo;
	private Integer billCompanyId;

    private Integer totAmount; //총금액
    private Integer totCount; // 총건수
    private Integer communicationFee; // 통신료
    private Integer communicationFeeVat; // 통신료 VAT
    private Integer untpc; //택배금액
    private Integer untpcVat; //택배금액 VAT
    private Integer weightUntpc; //통신금액
    private Integer weightUntpcVat; //통신금액 VAT

    private String sendStatus;

    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private String delYn;
}
