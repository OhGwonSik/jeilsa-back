// src/main/java/com/jeil/delivery/deliverylogic/service/TransportService.java
package com.jeil.delivery.deliverylogic.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.auth.common.util.SecurityUtil;
import com.jeil.delivery.deliverylogic.domain.DeliveryStausExcelDTO;
import com.jeil.delivery.deliverylogic.domain.TransportDeleteResponseDTO;
import com.jeil.delivery.deliverylogic.domain.TransportDtlVO;
import com.jeil.delivery.deliverylogic.domain.TransportResponseDTO;
import com.jeil.delivery.deliverylogic.domain.TransportVO;
import com.jeil.delivery.deliverylogic.mapper.TransportDtlMapper;
import com.jeil.delivery.deliverylogic.mapper.TransportMapper;
import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.service.CompanyService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransportService {

    private final TransportMapper transportMapper;
    private final TransportDtlMapper transportDtlMapper;
    private final CompanyService companyService;

    @Transactional
    public TransportResponseDTO save(TransportVO transportVO, List<Integer> deletedDtlIds) {
        Integer currentMemberId = SecurityUtil.getCurrentMemberId();
        int upserted = 0;
        Integer transportId = transportVO.getTransportId();
        String shipmentOperationDate = null;
        boolean isChargeCdUpdated = false;
        List<TransportDtlVO> transportDtlVO = transportVO.getTransportDtl();

        if (transportId == null || transportId == 0) {
            shipmentOperationDate = transportVO.getShipmentOperationDate();
        } else {
            TransportVO savedTransportVO = transportMapper.selectTransportListById(transportVO);
            // 최초 입력시 운송정보에 입력된 물류작업일로 모든 데이터의 물류작업일을 맞춤
            shipmentOperationDate = savedTransportVO.getShipmentOperationDate();
            // 운송코드 값이 기존과 변경되었는지 확인하여 플래그값 변경
            if(!savedTransportVO.getChargeCd().equals(transportVO.getChargeCd())) isChargeCdUpdated = true;
        }

        // 발신 , 수신 데이터 저장
        transportVO.setChgId(currentMemberId);
        if (transportId == null || transportId == 0) {
            // 최초 저장
            transportVO.setRegId(currentMemberId);
            transportMapper.insertTransport(transportVO);
            transportId = transportVO.getTransportId();
        } else {
            // 업데이트
            transportMapper.updateTransport(transportVO);
        }

        if(isChargeCdUpdated){
            transportDtlMapper.deleteTransportDtlAll(transportId,currentMemberId);
            transportDtlVO = transportVO.getTransportDtl();
            for (TransportDtlVO td : transportDtlVO) {
                td.setTransportDtlId(null);
            }
        }

        // 물류 데이터 저장
        for (TransportDtlVO td : transportDtlVO) {
            td.setTransportId(transportId);
            td.setChgId(currentMemberId);
            boolean isNew = (td.getTransportDtlId() == null || td.getTransportDtlId() == 0);
            boolean isWeight = "WEIGHT".equals(td.getCalculationCd());
            String chargeCd = transportVO.getChargeCd();
            boolean both     = "CH003".equals(chargeCd);  // 양방향 청구
            boolean prepaid  = "CH001".equals(chargeCd);  // 선불: SENDER 기준
            boolean collect  = "CH002".equals(chargeCd);  // 착불: RECEIVER 기준

            if (isNew) {
                td.setRegId(currentMemberId);

            	// SENDER의 물류작업일(String)을 LocalDate로 변환
                LocalDate senderDate = LocalDate.parse(shipmentOperationDate, DateTimeFormatter.ISO_LOCAL_DATE);
                String receiverShipmentOperationDate = shipmentOperationDate;

                // 통신 거래(WEIGHT)이면서 양방향(CH003) 또는 착불(CH002)인 경우
                if (isWeight && (both || collect)) {
                    // 수신(RECEIVER) 측 물류작업일을 SENDER 날짜 + 1일로 설정
                    LocalDate receiverDate = senderDate.plusDays(1); //
                    receiverShipmentOperationDate = receiverDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                }

                if (isWeight) {
                    // 두 건은 그대로 넣되, "금액 계산 기준(calc target)"만 chargeCd로 컨트롤
                    // 행의 주체(side)는 각자 유지(SENDER/RECEIVER)
                    String senderCalcTarget = both ? "SENDER" : (prepaid ? "SENDER" : "RECEIVER");
                    String receiverCalcTarget = both ? "RECEIVER" : (prepaid ? "SENDER" : "RECEIVER");

                    TransportDtlVO senderRow = shallowCopyForInsert(td, "SENDER", senderCalcTarget, shipmentOperationDate, true);
                    transportDtlMapper.insertTransportDtl(senderRow);

                    TransportDtlVO receiverRow = shallowCopyForInsert(td, "RECEIVER", receiverCalcTarget, receiverShipmentOperationDate, true);
                    transportDtlMapper.insertTransportDtl(receiverRow);

                    upserted += 2;
                } else {
                    // QTY → 선불/착불 한 건
                    String side = "SENDER";
                    if ("CH002".equals(chargeCd)) side = "RECEIVER";
                    TransportDtlVO row = shallowCopyForInsert(td, side, side ,shipmentOperationDate, false);
                    transportDtlMapper.insertTransportDtl(row);
                    upserted++;
                }
            } else {
                // 기존 행은 기존대로 업데이트 (필요 시 WEIGHT 짝행 처리 별도 확장)
                transportDtlMapper.updateTransportDtl(td);
                upserted++;
            }
        }


        // 물류 소프트 삭제
        if (deletedDtlIds != null && !deletedDtlIds.isEmpty()) {
            transportDtlMapper.deleteTransportDtl(
                    /* ids */ deletedDtlIds,
                    /* chgId */ currentMemberId
            );
        }

        TransportResponseDTO resp = new TransportResponseDTO();
        resp.setTransportId(transportId);
        resp.setSenderCompanyId(transportVO.getSenderCompanyId());
        resp.setReceiverCompanyId(transportVO.getReceiverCompanyId());
        resp.setItemCount(upserted);
        return resp;
    }

    @Transactional
    public void ensureSender(TransportVO vo) {
        // 1) 이미 pk가 있으면 패스
        if (vo.getSenderCompanyId() != null && vo.getSenderCompanyId() > 0) {
            return;
        }

        // 2) 같은 이름 회사 존재 시 연결 (자동완성 무시하고 수기입력한 경우까지 포함)
        boolean exists = companyService.checkCompanyByCompanyNm(vo.getSenderCompanyNm());
        if (exists) {
            CompanyDTO dto = companyService.selectCompanyByCompanyNm(vo.getSenderCompanyNm());
            if (dto != null) {
                vo.setSenderCompanyId(dto.getCompanyId());
                return;
            }
        }

        // 3) 없으면 신규 등록
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setCompanyNm(vo.getSenderCompanyNm());
        companyDTO.setRegionCd(vo.getSenderRegionCd());
        companyDTO.setRegionDtlCd(vo.getSenderRegionDtlCd());
        companyDTO.setAddress(vo.getSenderAddress());
        companyDTO.setManagerNm(vo.getSenderManagerNm());
        companyDTO.setRmk(vo.getSenderRmk());
        companyDTO.setTelNo(vo.getSenderTelNo());
        companyDTO.setManagerTelNo(vo.getSenderManagerTelNo());
        companyDTO.setShipperYn("N");

        companyService.insertCompany(companyDTO);

        // 방금 생성한 pk를 VO에 반영
        vo.setSenderCompanyId(companyDTO.getCompanyId());
    }

    @Transactional
    public void ensureReceiver(TransportVO vo) {
        // 1) 이미 저장되어있는 회사인지 확인
        if (vo.getReceiverCompanyId() != null && vo.getReceiverCompanyId() > 0) {
            return;
        }

        // 2) 같은 이름 회사가 존재하는지 확인 & 자동입력을 무시하고 수기입력으로 진행한 경우
        boolean exists = companyService.checkCompanyByCompanyNm(vo.getReceiverCompanyNm());
        if (exists) {
            CompanyDTO dto = companyService.selectCompanyByCompanyNm(vo.getReceiverCompanyNm());
            if (dto != null) {
                vo.setReceiverCompanyId(dto.getCompanyId());
                return;
            }
        }

        // 3) 등록된적이 없으면 신규 업체 등록
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setCompanyNm(vo.getReceiverCompanyNm());
        // 운송구분값은 저장 못함
        companyDTO.setRegionCd(vo.getReceiverRegionCd());
        companyDTO.setRegionDtlCd(vo.getReceiverRegionDtlCd());
        companyDTO.setAddress(vo.getReceiverAddress());
        companyDTO.setManagerNm(vo.getReceiverManagerNm());
        companyDTO.setRmk(vo.getReceiverRmk());
        companyDTO.setTelNo(vo.getReceiverTelNo());
        companyDTO.setShipperYn("N");
        companyDTO.setManagerTelNo(vo.getReceiverManagerTelNo());

        companyService.insertCompany(companyDTO);

        // 저장된 수신처 pk 값을 다시 vo에 담아서 리턴
        vo.setReceiverCompanyId(companyDTO.getCompanyId());
    }

    private String normalizeTel(String raw) {
        return raw == null ? null : raw.replaceAll("[^0-9]", "");
    }

    private static int NZ(Integer v) { return v == null ? 0 : v; }

    private TransportDtlVO shallowCopyForInsert(
            TransportDtlVO src,
            String side,                // "SENDER" | "RECEIVER"  (행의 주체)
            String calculationTargetCd,      // "SENDER" | "RECEIVER"  (금액 계산 기준)
            String shipmentOperationDate,
            Boolean isWeight
    ) {
        TransportDtlVO c = new TransportDtlVO();

        // PK/공통
        c.setTransportDtlId(null);
        c.setTransportId(src.getTransportId());
        c.setCalculationCd(src.getCalculationCd());
        c.setWaybillNo(src.getWaybillNo());
        c.setKindCd(src.getKindCd());
        c.setRmk(src.getRmk());
        c.setShipmentOperationDate(shipmentOperationDate);
        c.setRegId(src.getRegId());
        c.setChgId(src.getChgId());

        // 가격/수량 매핑
        if ("RECEIVER".equalsIgnoreCase(side)) {
            if(isWeight){
                // 수신 기준: receiver* 필드 우선 사용, 비었으면 송신값/계산으로 보정
                int qty = Optional.ofNullable(src.getReceiverQty())
                        .filter(v -> v != 0)
                        .orElse(NZ(src.getQty()));
                int untpc  = src.getReceiverUntpc() != null && src.getReceiverUntpc() != 0 ? src.getReceiverUntpc() : NZ(src.getUntpc());
                int amount = src.getReceiverAmount()!= null && src.getReceiverAmount()!= 0 ? src.getReceiverAmount(): qty * untpc;

                c.setQty(qty);
                c.setUntpc(untpc);
                c.setAmount(amount);
            } else {
                c.setQty(NZ(src.getQty()));
                c.setUntpc(NZ(src.getUntpc()));
                c.setAmount(src.getAmount() != null ? src.getAmount() : NZ(src.getQty()) * NZ(src.getUntpc()));
            }
        } else {
            // 송신 기준: 기존 값 그대로
            c.setQty(NZ(src.getQty()));
            c.setUntpc(NZ(src.getUntpc()));
            c.setAmount(src.getAmount() != null ? src.getAmount() : NZ(src.getQty()) * NZ(src.getUntpc()));
            side = "SENDER"; // normalize
        }

        c.setSenderReceiverCd(side);
        c.setCalculationTargetCd(calculationTargetCd);
        return c;
    }

    public List<TransportVO> selectTransportList(TransportVO transportVO) {
        return transportMapper.selectTransportList(transportVO);
    }

    public TransportVO selectTransportListById(TransportVO transportVO) {
        return transportMapper.selectTransportListByCompanyId(transportVO);
    }

    public List<TransportVO> selectArrivalsList(TransportVO transportVO) {
        return transportMapper.selectArrivalsList(transportVO);
    }

    public TransportDeleteResponseDTO delete(int transportId) {
        Integer currentMemberId = SecurityUtil.getCurrentMemberId();
        // 1) 물품(Y) -> 2) 수신처 & 발신처 (Y) 순
        int affectedTransportDtl = transportDtlMapper.delete(transportId, currentMemberId);
        int affectedTransport = transportMapper.delete(transportId, currentMemberId);

        if (affectedTransport == 0) {
            // 이미 삭제됐거나 존재X
            // 필요시 커스텀 예외로 변경 가능
            // throw new UpdateCheckedException("삭제 대상이 없거나 이미 삭제됨");
        }

        return TransportDeleteResponseDTO.builder()
                .transportId(transportId)
                .affectedTransport(affectedTransport)
                .affectedTransportDtl(affectedTransportDtl)
                .build();
    }

    public List<TransportVO> selectReceiverTransportList(TransportVO transportVO) {
        return transportMapper.selectReceiverTransportList(transportVO);
    }

    public List<TransportVO> selectSenderTransportList(TransportVO transportVO) {
        return transportMapper.selectSenderTransportList(transportVO);
    }

//    배송체크 현황 엑셀 다운로드
    public void selectDeliveryStatusList(DeliveryStausExcelDTO deliveryStausExcelDTO, HttpServletResponse response) throws IOException{
    	List<DeliveryStausExcelDTO> deliveryStatusList = transportMapper.selectDeliveryStatusList(deliveryStausExcelDTO);

    	if(deliveryStatusList.isEmpty()) {
    		throw new IllegalArgumentException("다운로드 할 데이터가 없습니다.");
    	}

    	String excelFileName = "배송체크현황.xls";

    	String encodedFileName = URLEncoder.encode(excelFileName, StandardCharsets.UTF_8)
    	                                   .replaceAll("\\+", "%20");

    	response.setContentType("application/vnd.ms-excel");
    	response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (
                InputStream template = getClass().getResourceAsStream("/templates/jxlsTemplates/delivery_status_template.xls");
                OutputStream os = response.getOutputStream()
            ) {
                if (template == null) {
                    throw new FileNotFoundException("템플릿 파일을 찾을 수 없습니다: /templates/jxlsTemplates/delivery_status_template.xls");
                }

                // JXLS 컨텍스트 생성
                Context context = new Context();
                context.putVar("deliveryStatusList", deliveryStatusList);

                // 템플릿 처리 및 출력
                JxlsHelper.getInstance().processTemplate(template, os, context);
            }
    }
}
