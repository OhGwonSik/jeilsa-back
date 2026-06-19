// src/main/java/com/jeil/delivery/deliverylogic/domain/TransportResponseDTO.java
package com.jeil.delivery.deliverylogic.domain;

import lombok.Data;
import java.util.List;

/** 한 번에 저장 응답 DTO */
@Data
public class TransportResponseDTO {
    private Integer transportId;
    private Integer senderCompanyId;
    private Integer receiverCompanyId;
    private Integer itemCount;
}

