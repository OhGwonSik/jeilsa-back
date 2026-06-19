// src/main/java/com/jeil/delivery/deliverylogic/controller/TransportController.java
package com.jeil.delivery.deliverylogic.controller;

import com.jeil.delivery.deliverylogic.domain.*;
import com.jeil.delivery.deliverylogic.service.TransportService;
import com.jeil.delivery.system.domain.CompanyVO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/transport")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    @GetMapping("/shipments/list")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'transport', 'read')")
    public List<TransportVO> selectTransportList(TransportVO transportVO) {
        return transportService.selectTransportList(transportVO);
    }

    @GetMapping("/arrivals/list")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'transport', 'read')")
    public List<TransportVO> selectArrivalsList(TransportVO transportVO) {
        return transportService.selectArrivalsList(transportVO);
    }

    @GetMapping(value = "/listById", produces = "application/json")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'transport', 'read')")
    public ResponseEntity<TransportSaveDTO> selectTransportListById(  @RequestParam(value = "senderCompanyId",   required = false) Integer senderCompanyId,
                                                                      @RequestParam(value = "receiverCompanyId", required = false) Integer receiverCompanyId,
                                                                      @RequestParam(value = "continueMode", required = false) String continueMode) {
        TransportVO transportVO = new TransportVO();

        if (senderCompanyId != null) {
            transportVO.setSenderCompanyId(senderCompanyId);
        } else {
            transportVO.setReceiverCompanyId(receiverCompanyId);
        }
        
        // continueMode 설정
        transportVO.setContinueMode(continueMode);

        // 1) 평면 VO 조회
        TransportVO vo = transportService.selectTransportListById(transportVO);
        if (vo == null) return ResponseEntity.notFound().build();

        // 2) 상세(물품) 조회
//        List<TransportDtlVO> dtls = transportService.selectTransportDtlByTransportId(transportId);

        // 3) VO → 중첩 DTO 변환
        TransportSaveDTO dto = new TransportSaveDTO();
        TransportSaveDTO.Transport t = new TransportSaveDTO.Transport();

        t.setTransportId(vo.getTransportId());
        t.setChargeCd(vo.getChargeCd());

        CompanyVO sender = new CompanyVO();
        CompanyVO receiver = new CompanyVO();
        if (senderCompanyId != null){
            // sender
            sender.setCompanyId(vo.getSenderCompanyId());
            sender.setCompanyNm(vo.getSenderCompanyNm());
            sender.setShipperCd(vo.getSenderTransportShipperCd());
            sender.setRegionCd(vo.getSenderRegionCd());
            sender.setRegionDtlCd(vo.getSenderRegionDtlCd());
            sender.setAddress(vo.getSenderAddress());
            sender.setManagerNm(vo.getSenderManagerNm());
            sender.setTelNo(vo.getSenderTelNo());
            sender.setManagerTelNo(vo.getSenderManagerTelNo());
            sender.setRmk(vo.getSenderRmk());
            sender.setUntpc(vo.getSenderUntpc());
            sender.setWeightUntpc(vo.getSenderWeightUntpc());
            sender.setDeliveryRouteNm(vo.getSenderDeliveryRouteNm());
        } else {
            // receiver
            receiver.setCompanyId(vo.getReceiverCompanyId());
            receiver.setCompanyNm(vo.getReceiverCompanyNm());
            receiver.setShipperCd(vo.getReceiverTransportShipperCd());
            receiver.setRegionCd(vo.getReceiverRegionCd());
            receiver.setRegionDtlCd(vo.getReceiverRegionDtlCd());
            receiver.setAddress(vo.getReceiverAddress());
            receiver.setManagerNm(vo.getReceiverManagerNm());
            receiver.setTelNo(vo.getReceiverTelNo());
            receiver.setManagerTelNo(vo.getReceiverManagerTelNo());
            receiver.setRmk(vo.getReceiverRmk());
            receiver.setUntpc(vo.getReceiverUntpc());
            receiver.setWeightUntpc(vo.getReceiverWeightUntpc());
            receiver.setDeliveryRouteNm(vo.getReceiverDeliveryRouteNm());
        }

        t.setSender(sender);
        t.setReceiver(receiver);

        dto.setTransport(t);
        dto.setDeletedDtlIds(List.of());  // 조회 응답이므로 빈 배열/혹은 null

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/receiver-list")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'transport', 'read')")
    public List<TransportVO> selectReceiverTransportList(TransportVO transportVO) {
        return transportService.selectReceiverTransportList(transportVO);
    }

    @GetMapping("/sender-list")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'transport', 'read')")
    public List<TransportVO> selectSenderTransportList(TransportVO transportVO) {
        return transportService.selectSenderTransportList(transportVO);
    }


    /** 생성/수정 겸용: sender.senderTransportId == null/0 → 신규, >0 → 수정 */
    @PostMapping("/shipment-save")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('owner', 'transport', 'create', 'update')")
    public ResponseEntity<?> save(@RequestBody TransportSaveDTO req) {
        TransportVO vo = toVO(req);
        transportService.ensureSender(vo);
        transportService.ensureReceiver(vo);
        var result = transportService.save(vo, req.getDeletedDtlIds());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'transport', 'delete')")
    public ResponseEntity<TransportDeleteResponseDTO> delete(@PathVariable("id") int transportId) {
        return ResponseEntity.ok(transportService.delete(transportId));
    }

    // 배송체크 엑셀 다운로드
    @PostMapping("/arrivals/list/excel")
    public void selectDeliveryStatusList(@RequestBody DeliveryStausExcelDTO deliveryStausExcelDTO, HttpServletResponse response) throws IOException {
    	transportService.selectDeliveryStatusList(deliveryStausExcelDTO, response);
    }

    // TODO 위치 이동
    private static int N(Integer v) { return v == null ? 0 : v; }
    private static String S(String v) { return v == null ? "" : v; }

    private TransportVO toVO(TransportSaveDTO req) {
        var t = req.getTransport();

        TransportVO vo = new TransportVO();
        vo.setTransportId(N(t.getTransportId()));
        vo.setChargeCd(S(t.getChargeCd()));
        vo.setShipmentOperationDate(S(t.getShipmentOperationDate()));

        // sender
        if (t.getSender() != null) {
            vo.setSenderCompanyId(t.getSender().getCompanyId());
            vo.setSenderCompanyNm(S(t.getSender().getCompanyNm()));
            vo.setSenderTransportShipperCd(S(t.getSender().getShipperCd()));
            vo.setSenderRegionCd(S(t.getSender().getRegionCd()));
            vo.setSenderRegionDtlCd(S(t.getSender().getRegionDtlCd()));
            vo.setSenderAddress(S(t.getSender().getAddress()));
            vo.setSenderManagerNm(S(t.getSender().getManagerNm()));
            vo.setSenderTelNo(S(t.getSender().getTelNo()));
            vo.setSenderManagerTelNo(S(t.getSender().getManagerTelNo()));
            vo.setSenderRmk(S(t.getSender().getRmk()));
        }

        // receiver
        if (t.getReceiver() != null) {
            vo.setReceiverCompanyId(t.getReceiver().getCompanyId());
            vo.setReceiverCompanyNm(S(t.getReceiver().getCompanyNm()));
            vo.setReceiverTransportShipperCd(S(t.getReceiver().getShipperCd()));
            vo.setReceiverRegionCd(S(t.getReceiver().getRegionCd()));
            vo.setReceiverRegionDtlCd(S(t.getReceiver().getRegionDtlCd()));
            vo.setReceiverAddress(S(t.getReceiver().getAddress()));
            vo.setReceiverManagerNm(S(t.getReceiver().getManagerNm()));
            vo.setReceiverTelNo(S(t.getReceiver().getTelNo()));
            vo.setReceiverManagerTelNo(S(t.getReceiver().getManagerTelNo()));
            vo.setReceiverRmk(S(t.getReceiver().getRmk()));
        }

        // dtl
        var rows = (req.getTransportDtl() == null) ? List.<TransportDtlVO>of()
                : req.getTransportDtl().stream()
                .filter(d -> !"Y".equalsIgnoreCase(S(d.getDelYn())))
                .map(d -> {
                    TransportDtlVO dv = new TransportDtlVO();
                    dv.setTransportId(N(t.getTransportId()));  // 저장 후 최종 id로 보정 가능
                    dv.setTransportDtlId(N(d.getTransportDtlId())); // shipmentId ↔ transportDtlId
                    dv.setCalculationCd(S(d.getCalculationCd()));   // trnsprtCd → calculationCd
                    dv.setKindCd(S(d.getKindCd()));
                    dv.setWaybillNo(S(d.getWaybillNo()));
                    dv.setQty(N(d.getQty()));
                    dv.setUntpc(N(d.getUntpc()));
                    dv.setAmount(N(d.getAmount()));
                    dv.setReceiverAmount(N(d.getReceiverAmount()));
                    dv.setReceiverQty(N(d.getReceiverQty()));
                    dv.setReceiverUntpc(N(d.getReceiverUntpc()));
                    dv.setDelYn("N");
                    dv.setShipmentOperationDate(S(t.getShipmentOperationDate()));
                    return dv;
                })
                .toList();

        vo.setTransportDtl(rows);
        return vo;
    }

}
