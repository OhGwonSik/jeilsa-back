// TransportRowDTO.java
package com.jeil.delivery.deliverylogic.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransportRowDTO {
        // ===== Sender =====
        private Integer senderTransportId;
        private Integer senderCompanyId;

        private String  chargeCd;          // st.charge_cd
        private String  chargeNm;          // c_charge.code_nm

        private String  senderDelYn;       // st.del_yn
        private LocalDateTime regDt;       // st.reg_dt (sender 기준)

        private String  senderRegionCd;    // st.region_cd
        private String  senderRegionNm;    // c_sregion.code_nm
        private String  senderRegionDtlCd; // st.region_dtl_cd
        private String  senderRegionDtlNm; // c_sregion_dtl.code_nm

        private String  senderDeliveryRouteNm; // dr.delivery_route_nm
        private String  senderCompanyNm;       // sc.company_nm

        private String  senderShipperCd;   // CASE(Y: ssp.shipper_cd, N: 'DLV')
        private String  senderShipperNm;   // c_sender_shipper.code_nm

        private String  senderTelNo;
        private String  senderAddress;
        private String  senderManagerNm;
        private String  senderManagerTelNo;
        private String  senderRmk;

        // ===== Receiver =====
        private Integer receiverTransportId;            // r.receiver_transport_id
        private Integer receiverCompanyId;
        private String  receiverCompanyNm;

        private String  receiverShipperCd;     // CASE(Y: rsp.shipper_cd, N: 'DLV')
        private String  receiverShipperNm;     // c_receiver_shipper.code_nm

        private String  receiverDelYn;
        private LocalDateTime receiverRegDt;   // r.reg_dt (쿼리에서 별칭 필요)

        private String  receiverTelNo;
        private String  receiverManagerTelNo;

        private String  receiverRegionCd;
        private String  receiverRegionNm;      // c_rregion.code_nm
        private String  receiverRegionDtlCd;
        private String  receiverRegionDtlNm;   // c_rregion_dtl.code_nm

        private String  receiverAddress;
        private String  receiverRmk;

        // ===== Shipment =====
        private Integer totalSenderAmount;
        private Integer totalReceiverAmount;

        private Integer shipmentId;
        private String  kindCd;
        private String  kindCdNm;
        private String  waybillNo;
        private Integer  receiverQty;
        private Integer  receiverAmount;
        private Integer receiverUntpc;
}
