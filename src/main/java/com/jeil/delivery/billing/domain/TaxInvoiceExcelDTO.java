package com.jeil.delivery.billing.domain;

import com.jeil.delivery.domain.CommonColumnDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TaxInvoiceExcelDTO extends CommonColumnDTO{
	   private Integer year;
	   private Integer month;
	   private String billCd;

	   private Integer billId;
	   private Integer billDtlId;

	   private Integer calculationCompanyId;
	   private String calculationCompanyNm;
	   private String bizNo;
	   private String representativeNm;
	   private String bizType;
	   private String bizItem;
	   private String email;
	   private String address;

	   private Integer billCompanyId;
	   private String billCompanyNm;
	   private String billBizNo;
	   private String billRepresentativeNm;
	   private String billBizType;
	   private String billBizItem;
	   private String billEmail;
	   private String billAddress;

	   private Integer totAmount;
	   private Integer totAmountVat;
	   private Integer communicationFee;
	   private Integer communicationFeeVat;
	   private Integer useFee;
	   private Integer useFeeVat;

	   private Integer lastDay;
	   private String writeDate;

}
