package com.jeil.delivery.billing.domain;

import com.jeil.delivery.domain.CommonColumnDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BillCompanyDTO extends CommonColumnDTO{
	   private Integer billCompanyId;
	   private String billCompanyNm;
	   private String billBizNo;
	   private String billRepresentativeNm;
	   private String billBizType;
	   private String billBizItem;
	   private String billEmail;
	   private String billAddress;
	   private String billTelNo;
	   private String billFaxNo;
	   private String billAccountInfo;
}
