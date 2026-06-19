// src/main/java/com/jeil/delivery/deliverylogic/api/dto/TransportSaveRequest.java
package com.jeil.delivery.deliverylogic.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeil.delivery.system.domain.CompanyVO;
import lombok.Data;

import java.util.List;

@Data
public class TransportSaveDTO {
    private Transport transport;            // { transportId, chargeCd, sender{}, receiver{} }
    private List<TransportDtlVO> transportDtl;    // 물품 목록
    private List<Integer> deletedDtlIds;    // 삭제된 물품 id 목록 (없으면 null)

    @Data
    public static class Transport {
        private Integer transportId;
        private String chargeCd;
        private String shipmentOperationDate;
        private CompanyVO sender;
        private CompanyVO receiver;
    }
}
