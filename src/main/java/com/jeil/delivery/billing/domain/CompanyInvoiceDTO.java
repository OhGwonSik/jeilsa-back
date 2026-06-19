package com.jeil.delivery.billing.domain;

import java.util.List;

import com.jeil.delivery.domain.CommonColumnDTO;
import com.jeil.delivery.system.domain.CompanyDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CompanyInvoiceDTO extends CommonColumnDTO{
	   private Integer year;
	   private Integer month;
	   private String billCd;

	   private CompanyDTO companyInfo;

	   private BillCompanyDTO billCompanyInfo;

	   private List<BillDTO> shipmentInfoList;

	   private Integer totAmount;
	   private Integer totAmountVat;
	   private Integer communicationFee;
	   private Integer communicationFeeVat;
	   private Integer allTotAmount;

	   private int totalCommunicationPrepaid;
	   private int totalCommunicationCollect;
	   private int totalDeliveryPrepaid;
	   private int totalDeliveryCollect;
	   private int totalQty;


	   //종류별 청구서용
	   private List<CompanyInvoiceByKindCdDTO> companyInvoiceByKindList;
}
