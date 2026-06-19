package com.jeil.delivery.deliverylogic.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeliveryStausExcelDTO {

	private String transportGubun;
	private String shipmentOperationDate;
	private String senderRegionNm;
	private String senderDeliveryRouteNm;
	private String senderManagerNm;
	private String calculationCompanyNm;
	private String waybillNo;
	private String senderCompanyNm;
	private String chargeNm;
	private Integer qty;
	private Integer untpc;
	private Integer amount;
	private Integer weightQty;
	private Integer weightUntpc;
	private Integer weightAmount;
	private String senderRmk;
	private String recRegionNm;
	private String receiverCompanyNm;
	private String receiverTelNo;
	private String receiverManagerTelNo;
	private String kindNm;
	private String deliveryRouteNm;
	private String receiverRmk;
	private String receiverAddress;


	private String startDate;
	private String endDate;
	private Integer deliveryRouteId;
	private String chargeCd;
	private String senderRegionCd;
	private String receiverRegionCd;
	private String calculationCd;
}
