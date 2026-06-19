// src/main/java/com/jeil/delivery/deliverylogic/domain/TransportDeleteResponseDTO.java
package com.jeil.delivery.deliverylogic.domain;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TransportDeleteResponseDTO {
    private Integer transportId;  // 요청한 발신처 PK
    private Integer affectedTransport;  // 소프트딜리트된 수신,발신처 수
    private Integer affectedTransportDtl;  // 소프트딜리트된 물품 수
}
