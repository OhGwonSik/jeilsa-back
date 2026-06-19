package com.jeil.delivery.billing.domain;

import com.jeil.delivery.domain.CommonColumnDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CompanyInvoiceByKindCdDTO extends CommonColumnDTO{
	   //종류별 청구서용
	   private String kindCd;
	   private String kindNm;
	   private String kindGroup;
	   private Integer communicationPrepaid;
	   private Integer communicationCollect;
	   private Integer deliveryPrepaid;
	   private Integer deliveryCollect;
	   private Integer totalQty;
	   private Integer totalAmount;
	   private Integer totalAmountVat;
	   private Integer allTotalAmount;
}
