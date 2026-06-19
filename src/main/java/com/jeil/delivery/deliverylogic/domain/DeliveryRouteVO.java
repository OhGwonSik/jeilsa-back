package com.jeil.delivery.deliverylogic.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeliveryRouteVO {
    private Integer deliveryRouteId;    // 배송 코스 ID (Auto Increment)
    private String deliveryRouteNm;     // 배송 코스 명

    private Integer regId;              // 등록자 ID
    private LocalDateTime regDt;        // 등록 일시

    private Integer chgId;              // 변경자 ID
    private LocalDateTime chgDt;        // 변경 일시

    private String delYn;               // 삭제 여부 ('Y' or 'N')
}
