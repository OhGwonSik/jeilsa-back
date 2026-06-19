package com.jeil.delivery.billing.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeil.delivery.billing.domain.CompanyInvoiceDTO;
import com.jeil.delivery.billing.domain.TaxInvoiceExcelDTO;
import com.jeil.delivery.billing.service.BillPrintService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bill-print")
public class BillPrintController {

	private final BillPrintService billPrintService;

	@PostMapping("/download")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','billPrint','read')")
	public void generateExcelAndDownladZip(@RequestBody TaxInvoiceExcelDTO taxInvoiceExcelDTO, HttpServletResponse response) throws IOException {
		billPrintService.generateExcelAndDownladZip(taxInvoiceExcelDTO, response);
	}

	@PostMapping("/company-invoice")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','billPrint','read')")
	public CompanyInvoiceDTO selectCompanyDetailInvoice(@RequestBody TaxInvoiceExcelDTO taxInvoiceExcelDTO) {
		return billPrintService.selectCompanyDetailInvoice(taxInvoiceExcelDTO);
	}

	@GetMapping("/page/company-invoice/{billId}/{billDtlId}")
	public CompanyInvoiceDTO selectCompanyDetailInvoicePage(@PathVariable("billId") int billId, @PathVariable("billDtlId") int billDtlId) {
		return billPrintService.selectCompanyDetailInvoicePage(billId, billDtlId);
	}

	@PostMapping("/company-invoice/kind")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','billPrint','read')")
	public CompanyInvoiceDTO selectCompanyDetailInvoiceByKindCd(@RequestBody TaxInvoiceExcelDTO taxInvoiceExcelDTO) {
		return billPrintService.selectCompanyDetailInvoiceByKindCd(taxInvoiceExcelDTO);
	}

	@PostMapping("/download-invoice-pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @RequestBody TaxInvoiceExcelDTO param) {

        // 1. PDF 생성 (BillPrintService의 단일 메서드 호출)
        byte[] pdfBytes = billPrintService.generatePdfBytes(param);

        // 2. 파일 이름 설정
        String companyNm = param.getCalculationCompanyNm();
        String filename = String.format("%s_%d년_%02d월_청구서.pdf",
                                        companyNm,
                                        param.getYear(),
                                        param.getMonth());

        // 3. HTTP 응답 구성 (다운로드)
        // 파일 이름에 한글이 포함되므로 URI 인코딩 (RFC 5987) 사용
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                // Content-Disposition: 파일 다운로드 지시 및 파일명 전달
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                // Content-Type: 응답이 PDF 파일임을 명시
                .contentType(MediaType.APPLICATION_PDF)
                // 응답 본문에 PDF byte[] 포함
                .body(pdfBytes);
    }
}
