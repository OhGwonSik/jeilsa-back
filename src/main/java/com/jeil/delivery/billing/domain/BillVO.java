package com.jeil.delivery.billing.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BillVO {

	private Integer billId;
	private Integer billDtlId;
	private Integer companyId;
    private Integer year;
    private Integer month;
    private Integer billCompanyId;
    private String companyNm;
    private String billCd;

    private Integer totAmount; //총금액
    private Integer communicationFee; // 통신료
    private Integer communicationFeeVat; // 통신료 VAT
    private Integer untpc; //택배금액
    private Integer untpcVat; //택배금액 VAT
    private Integer weightUntpc; //통신금액
    private Integer weightUntpcVat; //통신금액 VAT

    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private String delYn;
}
