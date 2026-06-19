package com.jeil.delivery.billing.domain;

import com.jeil.delivery.domain.CommonColumnDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BillDTO extends CommonColumnDTO{
	private Integer companyId;
    private Integer year;
    private Integer month;
    private String billCd;

	private Integer billId;
	private Integer billDtlId;
	private Integer billDtlDgre;
	private Integer calculationCompanyId;
	private Integer billCompanyId;

    private Integer totAmount; //총금액
    private Integer totCount; // 총건수
    private Integer communicationFee; // 통신료
    private Integer communicationFeeVat; // 통신료 VAT
    private Integer untpc; //택배금액
    private Integer untpcVat; //택배금액 VAT
    private Integer weightUntpc; //통신금액
    private Integer weightUntpcVat; //통신금액 VAT

	private String shipperCd;
	private int transportAmount;

	// 청구서용
	private String senderCompanyNm;
	private String receiverCompanyNm;
	private String receiverTelNo;
	private String calculationCd;
	private String kindCd;
	private String kindNm;
	private String chargeCd;
	private String chargeNm;
	private Integer qty;
	private Integer amount;
	private String billDate;
	private String kindGroup;
}
