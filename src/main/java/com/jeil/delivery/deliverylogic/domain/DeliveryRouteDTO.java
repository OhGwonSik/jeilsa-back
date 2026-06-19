package com.jeil.delivery.deliverylogic.domain;

import com.jeil.delivery.domain.CommonColumnDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeliveryRouteDTO extends CommonColumnDTO{
    private Integer deliveryRouteId;    // 배송 코스 ID (Auto Increment)
    private String deliveryRouteNm;     // 배송 코스 명

    private Integer companyId;
}
