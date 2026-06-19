package com.jeil.delivery.billing.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.jeil.delivery.billing.domain.BillDTO;
import com.jeil.delivery.billing.domain.CompanyInvoiceDTO;
import com.jeil.delivery.billing.domain.TaxInvoiceExcelDTO;
import com.jeil.delivery.billing.mapper.BillCompanyMapper;
import com.jeil.delivery.billing.mapper.BillDtlMapper;
import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.mapper.CompanyMapper;
import com.jeil.delivery.util.DateUtil;
import com.lowagie.text.pdf.BaseFont;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillPrintService {
	private static final int CHUNK_SIZE = 100; // 한 파일당 최대 건수

	private final CompanyMapper companyMapper;
	private final BillCompanyMapper billCompanyMapper;
	private final BillDtlMapper billDtlMapper;
	private final TemplateEngine templateEngine;

	public void generateExcelAndDownladZip(TaxInvoiceExcelDTO taxInvoiceExcelDTO, HttpServletResponse response) throws IOException{
		List<TaxInvoiceExcelDTO> getTaxInvoiceList = billDtlMapper.selectTaxInvoiceExcelData(taxInvoiceExcelDTO);

		if(getTaxInvoiceList.isEmpty()) {
			throw new IllegalArgumentException("다운로드 할 데이터가 없습니다.");
		}

		String billCompanyNm = getTaxInvoiceList.get(0).getBillCompanyNm(); // 정산업체명
		String writeDate = DateUtil.getLastDateOfMonth(taxInvoiceExcelDTO.getYear(), taxInvoiceExcelDTO.getMonth()); // yyyyMMdd
		int lastDay = DateUtil.getLastDay(taxInvoiceExcelDTO.getYear(), taxInvoiceExcelDTO.getMonth()); // dd

		String zipFileName = String.format("%s_%d년_%02d월_%s일_세금계산서.zip",
		        billCompanyNm, taxInvoiceExcelDTO.getYear(), taxInvoiceExcelDTO.getMonth(), taxInvoiceExcelDTO.getBillCd());

		String encodedFileName = URLEncoder.encode(zipFileName, StandardCharsets.UTF_8)
            								.replaceAll("\\+", "%20");

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

		try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())){


			//0번+청크(100)
			for(int i = 0; i < getTaxInvoiceList.size(); i+= CHUNK_SIZE) {

				int end = Math.min(i + CHUNK_SIZE, getTaxInvoiceList.size());
				List<TaxInvoiceExcelDTO> chunk = new ArrayList<>();

				for(int j = i; j< end; j++) {
                    TaxInvoiceExcelDTO taxData = getTaxInvoiceList.get(j);
                    taxData.setWriteDate(writeDate);
                    taxData.setLastDay(lastDay);

                    chunk.add(taxData);
				}
				int fileIndex = (i / CHUNK_SIZE) + 1;

				// 시트 내 A1 문자
				String headerTitle = String.format("%s_%d-%d월분 전자세금계산서 #%d",
													billCompanyNm,
													taxInvoiceExcelDTO.getYear(),
													taxInvoiceExcelDTO.getMonth(),
													fileIndex);

				//엑셀 파일이름
				String excelFileName = String.format("%s_%02d.xls",
										DateUtil.getLastDateOfMonth(taxInvoiceExcelDTO.getYear(), taxInvoiceExcelDTO.getMonth()),
										fileIndex);

				//엑셀 생성 및 압축파일에 추가
				try(InputStream is = getClass().getResourceAsStream("/templates/jxlsTemplates/tax_invoices_template.xls")){
					if(is == null) {
						throw new FileNotFoundException("템플릿 파일이 존재하지않습니다.");
					}

					zos.putNextEntry(new ZipEntry(excelFileName));

					try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
						Context context = new Context();
						context.putVar("taxList", chunk);
						context.putVar("headerTitle", headerTitle);

						JxlsHelper.getInstance().processTemplate(is, bos, context);

						bos.writeTo(zos);
					}
					zos.closeEntry();
				}
			}
		}
	}

	public CompanyInvoiceDTO selectCompanyDetailInvoice(TaxInvoiceExcelDTO taxInvoiceExcelDTO) {
		CompanyInvoiceDTO companyInvoiceInfo = new CompanyInvoiceDTO();

		companyInvoiceInfo.setYear(taxInvoiceExcelDTO.getYear());
		companyInvoiceInfo.setMonth(taxInvoiceExcelDTO.getMonth());
		companyInvoiceInfo.setBillCompanyInfo(billCompanyMapper.selectBillCompanyInfo(taxInvoiceExcelDTO));
		companyInvoiceInfo.setCompanyInfo(companyMapper.selectInvoiceCompanyById(taxInvoiceExcelDTO));
		companyInvoiceInfo.setShipmentInfoList(billDtlMapper.selectCompanyBillDataList(taxInvoiceExcelDTO));

		return companyInvoiceInfo;
	}

	public CompanyInvoiceDTO selectCompanyDetailInvoicePage(int billId, int billDtlId) {
		TaxInvoiceExcelDTO param = new TaxInvoiceExcelDTO();
		param.setBillId(billId);
		param.setBillDtlId(billDtlId);

		//billId와 billDtlId로 year, month billCompanyId, calculationCompanyId 추출
		BillDTO billInfo = billDtlMapper.selectCompanyDetailInvoicePage(param);

		CompanyInvoiceDTO companyInvoiceInfo = new CompanyInvoiceDTO();

		if(billInfo != null) {

	        param.setYear(billInfo.getYear());
	        param.setMonth(billInfo.getMonth());
	        param.setBillCompanyId(billInfo.getBillCompanyId());
	        param.setCalculationCompanyId(billInfo.getCalculationCompanyId());
	        param.setBillCd(billInfo.getBillCd());

			companyInvoiceInfo.setYear(param.getYear());
			companyInvoiceInfo.setMonth(param.getMonth());
			companyInvoiceInfo.setBillCompanyInfo(billCompanyMapper.selectBillCompanyInfo(param));
			companyInvoiceInfo.setCompanyInfo(companyMapper.selectInvoiceCompanyById(param));
			companyInvoiceInfo.setShipmentInfoList(billDtlMapper.selectCompanyBillDataList(param));
		}

		return companyInvoiceInfo;
	}

	public CompanyInvoiceDTO selectCompanyDetailInvoiceByKindCd(TaxInvoiceExcelDTO taxInvoiceExcelDTO) {
		CompanyInvoiceDTO companyInvoiceInfo = new CompanyInvoiceDTO();

		companyInvoiceInfo.setYear(taxInvoiceExcelDTO.getYear());
		companyInvoiceInfo.setMonth(taxInvoiceExcelDTO.getMonth());
		companyInvoiceInfo.setBillCompanyInfo(billCompanyMapper.selectBillCompanyInfo(taxInvoiceExcelDTO));
		companyInvoiceInfo.setCompanyInfo(companyMapper.selectInvoiceCompanyById(taxInvoiceExcelDTO));
		companyInvoiceInfo.setCompanyInvoiceByKindList(billDtlMapper.selectCompanyBillDataListByKindCd(taxInvoiceExcelDTO));

		return companyInvoiceInfo;
	}

public byte[] generatePdfBytes(TaxInvoiceExcelDTO param) {

        // 3-1. DTO 조회 및 데이터 집계 (계산 로직 포함)
        CompanyInvoiceDTO invoiceData = getAggregatedInvoiceData(param);

        // 3-2. Thymeleaf Context 설정 (Full Package Name 사용으로 충돌 방지)
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("companyInvoiceInfo", invoiceData);

        String renderedHtml = templateEngine.process("/pdf/invoice", context);

        // 3-3. Flying Saucer를 이용한 PDF 변환
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // [폰트 설정] 리소스에서 폰트 파일을 임시로 복사하여 로드 (OS 호환성 확보)
            ClassPathResource fontResource = new ClassPathResource("/fonts/NanumGothic.ttf");
            if (!fontResource.exists()) {
                throw new RuntimeException("폰트 파일을 찾을 수 없습니다: src/main/resources/fonts/NanumGothic.ttf");
            }

            File fontFile = File.createTempFile("NanumGothic", ".ttf");
            try (InputStream fontStream = fontResource.getInputStream()) {
                Files.copy(fontStream, fontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 임시 파일 경로로 폰트 등록
            renderer.getFontResolver().addFont(
                fontFile.getAbsolutePath(),
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
            );

            // 렌더링 후 임시 파일 삭제 예약
            fontFile.deleteOnExit();

            renderer.setDocumentFromString(renderedHtml);
            renderer.layout();
            renderer.createPDF(os);

            return os.toByteArray();
        } catch (Exception e) {
            log.error("PDF 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("PDF 생성 실패.", e);
        }
    }

    /**
     * PDF 생성을 위해 DTO를 조회하고 필요한 합계를 계산하여 반환
     */
	private CompanyInvoiceDTO getAggregatedInvoiceData(TaxInvoiceExcelDTO param) {
	    BillDTO billInfo = billDtlMapper.selectCompanyDetailInvoicePage(param);
	    CompanyInvoiceDTO companyInvoiceInfo = new CompanyInvoiceDTO();

	    if (billInfo == null) return companyInvoiceInfo;

	    param.setYear(billInfo.getYear());
	    param.setMonth(billInfo.getMonth());
	    param.setBillCompanyId(billInfo.getBillCompanyId());
	    param.setCalculationCompanyId(billInfo.getCalculationCompanyId());
	    param.setBillCd(billInfo.getBillCd());

	    companyInvoiceInfo.setYear(billInfo.getYear());
	    companyInvoiceInfo.setMonth(billInfo.getMonth());
	    companyInvoiceInfo.setBillCd(billInfo.getBillCd());
	    companyInvoiceInfo.setBillCompanyInfo(billCompanyMapper.selectBillCompanyInfo(param));

	    // [중요] 업체 기본 정보 조회 (기본 운임료 확인용)
	    CompanyDTO companyInfo = companyMapper.selectInvoiceCompanyById(param);
	    companyInvoiceInfo.setCompanyInfo(companyInfo);

	    List<BillDTO> shipmentList = billDtlMapper.selectCompanyBillDataList(param);

	    if (shipmentList != null) {
	        for (BillDTO item : shipmentList) {
	            String calcCd = item.getCalculationCd();
	            if (item.getKindGroup() == null || item.getKindGroup().trim().isEmpty()) {
	                if ("QTY".equals(calcCd)) item.setKindGroup("택배");
	                else if ("WEIGHT".equals(calcCd)) item.setKindGroup("통신");
	            }
	        }
	    }
	    companyInvoiceInfo.setShipmentInfoList(shipmentList);

	    // 합계 계산 시 companyInfo(업체기본정보)도 함께 전달하여 기본운임 참조
	    calculateAndSetInvoiceTotals(companyInvoiceInfo, shipmentList, billInfo, companyInfo);

	    return companyInvoiceInfo;
	}

	private void calculateAndSetInvoiceTotals(CompanyInvoiceDTO dto, List<BillDTO> shipmentList, BillDTO masterBillInfo, CompanyDTO companyBasicInfo) {
	    long delivPre = 0, delivCol = 0, commPre = 0, commCol = 0;

	    if (shipmentList != null) {
	        for (BillDTO item : shipmentList) {
	            long amt = item.getAmount() != null ? item.getAmount().longValue() : 0;
	            String type = item.getCalculationCd();
	            String group = item.getKindGroup();
	            String pay = item.getChargeNm();
	            if (pay == null) pay = "";

	            if ("QTY".equals(type) || "택배".equals(group)) {
	                if (pay.contains("선불") || "선불".equals(item.getCalculationCd())) delivPre += amt;
	                else delivCol += amt;
	            } else if ("WEIGHT".equals(type) || "통신".equals(group)) {
	                if (pay.contains("선불") || "선불".equals(item.getCalculationCd())) commPre += amt;
	                else commCol += amt;
	            }
	        }
	    }

	    dto.setTotalDeliveryPrepaid((int) delivPre);
	    dto.setTotalDeliveryCollect((int) delivCol);
	    dto.setTotalCommunicationPrepaid((int) commPre);
	    dto.setTotalCommunicationCollect((int) commCol);

	    if (masterBillInfo != null) {
	        // [수정] 기본 운임 가져오기 우선순위: 1.청구서마스터 -> 2.업체기본정보 -> 0
	        long commFee = 0;
	        if (masterBillInfo.getCommunicationFee() != null && masterBillInfo.getCommunicationFee() > 0) {
	            commFee = masterBillInfo.getCommunicationFee();
	        } else if (companyBasicInfo != null && companyBasicInfo.getCommunicationFee() != null) {
	            commFee = companyBasicInfo.getCommunicationFee(); // 업체 기본 설정값 사용
	        }

	        // 부가세 자동 계산
	        long commFeeVat = masterBillInfo.getCommunicationFeeVat() != null ? masterBillInfo.getCommunicationFeeVat() : 0;
	        if (commFee > 0 && commFeeVat == 0) commFeeVat = (long)(commFee * 0.1);

	        dto.setCommunicationFee((int) commFee);
	        dto.setCommunicationFeeVat((int) commFeeVat);

	        // 운임 합계
	        long netAmt = (masterBillInfo.getUntpc() != null ? masterBillInfo.getUntpc().longValue() : 0) +
	                      (masterBillInfo.getWeightUntpc() != null ? masterBillInfo.getWeightUntpc().longValue() : 0);

	        if (netAmt == 0) netAmt = delivPre + delivCol + commPre + commCol;

	        long netVat = (masterBillInfo.getUntpcVat() != null ? masterBillInfo.getUntpcVat().longValue() : 0) +
	                      (masterBillInfo.getWeightUntpcVat() != null ? masterBillInfo.getWeightUntpcVat().longValue() : 0);

	        if (netAmt > 0 && netVat == 0) netVat = (long)(netAmt * 0.1);

	        dto.setTotAmount((int) netAmt);
	        dto.setTotAmountVat((int) netVat);

	        // 최종 합계
	        long finalTotal = commFee + commFeeVat + netAmt + netVat;
	        dto.setAllTotAmount((int) finalTotal);
	    }
	}
}
