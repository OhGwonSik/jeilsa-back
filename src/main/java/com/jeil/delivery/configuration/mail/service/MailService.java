package com.jeil.delivery.configuration.mail.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.jeil.delivery.billing.domain.BillDtlVO;
import com.jeil.delivery.billing.domain.BillVO;
import com.jeil.delivery.billing.domain.CompanyInvoiceDTO;
import com.jeil.delivery.billing.domain.TaxInvoiceExcelDTO;
import com.jeil.delivery.billing.mapper.BillCompanyMapper;
import com.jeil.delivery.billing.mapper.BillDtlMapper;
import com.jeil.delivery.configuration.error.NoDataException;
import com.jeil.delivery.configuration.mail.component.MailSendComponent;
import com.jeil.delivery.configuration.mail.domain.MailDTO;
import com.jeil.delivery.configuration.mail.domain.MailHistoryVO;
import com.jeil.delivery.configuration.mail.mapper.MailMapper;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.mapper.CompanyMapper;
import com.jeil.delivery.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

//	@Value("${aes.encrypt.key}")
//	private String encryptKey;

	private final MailSendComponent mailSendComponent;
	private final BillDtlMapper billDtlMapper;
	private final CompanyMapper companyMapper;
	private final BillCompanyMapper billCompanyMapper;
	private final MailMapper mailMapper;

	public String getCurrentFormattedDate() {
	    LocalDate currentDate = LocalDate.now();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    String formattedDate = currentDate.format(formatter);
	    return formattedDate;
	}


	public Boolean invoiceMailSend(BillVO baseBillVO) {

		if(baseBillVO == null) {
			throw new NoDataException();
		}

		BillDtlVO billTargetParam = new BillDtlVO();
		billTargetParam.setBillId(baseBillVO.getBillId());

		if(baseBillVO.getBillDtlId() != null) {
			billTargetParam.setBillDtlId(baseBillVO.getBillDtlId());
		}

		List<BillDtlVO> billTargetList = billDtlMapper.selectBillHistoryDtlList(billTargetParam);

		for(BillDtlVO billDtlVO : billTargetList) {
			MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
			MailHistoryVO mailHistory = new MailHistoryVO();
            String failLog = null;
            String status = "CMP";

			try {
				TaxInvoiceExcelDTO param = new TaxInvoiceExcelDTO();

				param.setBillId(baseBillVO.getBillId());
				param.setYear(baseBillVO.getYear());
				param.setMonth(baseBillVO.getMonth());
				param.setBillCompanyId(baseBillVO.getBillCompanyId());
				param.setBillCd(baseBillVO.getBillCd());
				param.setCalculationCompanyId(billDtlVO.getCalculationCompanyId());

		        CompanyInvoiceDTO invoice = new CompanyInvoiceDTO();
		        invoice.setYear(param.getYear());
		        invoice.setMonth(param.getMonth());
		        invoice.setBillCompanyInfo(billCompanyMapper.selectBillCompanyInfo(param));
		        invoice.setCompanyInfo(companyMapper.selectInvoiceCompanyById(param));

		        // 업체null 체크 추가
		        if (invoice.getCompanyInfo() == null) {
		            status = "SKIP";
		            failLog = "정산대상 업체가 아닙니다.";
		            continue;  // finally로 이동
		        }

		        invoice.setCompanyInvoiceByKindList(billDtlMapper.selectCompanyBillDataListByKindCd(param));

		        int totalAmount = invoice.getCompanyInvoiceByKindList()
		        	    .stream()
		        	    .mapToInt(dto -> dto.getTotalAmount() != null ? dto.getTotalAmount() : 0)
		        	    .sum();

	        	int totalAmountVat = (int) Math.round(totalAmount * 0.1);

	        	int totalCommunicationPrepaid = invoice.getCompanyInvoiceByKindList().stream()
	        		    .mapToInt(dto -> dto.getCommunicationPrepaid() != null ? dto.getCommunicationPrepaid() : 0)
	        		    .sum();
	    		int totalCommunicationCollect = invoice.getCompanyInvoiceByKindList().stream()
	    		    .mapToInt(dto -> dto.getCommunicationCollect() != null ? dto.getCommunicationCollect() : 0)
	    		    .sum();
	    		int totalDeliveryPrepaid = invoice.getCompanyInvoiceByKindList().stream()
	    		    .mapToInt(dto -> dto.getDeliveryPrepaid() != null ? dto.getDeliveryPrepaid() : 0)
	    		    .sum();
	    		int totalDeliveryCollect = invoice.getCompanyInvoiceByKindList().stream()
	    		    .mapToInt(dto -> dto.getDeliveryCollect() != null ? dto.getDeliveryCollect() : 0)
	    		    .sum();
	    		int totalQty = invoice.getCompanyInvoiceByKindList().stream()
	    		    .mapToInt(dto -> dto.getTotalQty() != null ? dto.getTotalQty() : 0)
	    		    .sum();

	        	// 통신료
	        	int communicationFee = invoice.getCompanyInfo().getCommunicationFee() != null ? invoice.getCompanyInfo().getCommunicationFee() : 0;
	        	int communicationFeeVat = (int) Math.round(communicationFee * 0.1);

	        	invoice.setTotAmount(totalAmount);
	        	invoice.setTotAmountVat(totalAmountVat);
	        	invoice.setCommunicationFee(communicationFee);
	        	invoice.setCommunicationFeeVat(communicationFeeVat);

	        	invoice.setTotalCommunicationPrepaid(totalCommunicationPrepaid);
	        	invoice.setTotalCommunicationCollect(totalCommunicationCollect);
	        	invoice.setTotalDeliveryPrepaid(totalDeliveryPrepaid);
	        	invoice.setTotalDeliveryCollect(totalDeliveryCollect);
	        	invoice.setTotalQty(totalQty);

	        	// 전체 합계
	        	int allTotAmount = communicationFee + communicationFeeVat + totalAmount + totalAmountVat;
	        	invoice.setAllTotAmount(allTotAmount);

	        	// 총 청구 금액이 0원이면 메일 발송 스킵
	        	if (allTotAmount <= 0) {
	        	    status = "SKIP";
	        	    failLog = "총 청구 금액이 0원이므로 발송하지 않음";
	        	    continue;  // finally 블록으로 이동하여 히스토리 기록 후 다음 업체로
	        	}

		        MailDTO mailDTO = MailDTO.builder()
		                .to(billDtlVO.getEmail())
		                .title(String.format("[%s] %d년 %d월 청구서",
		                        invoice.getBillCompanyInfo().getBillCompanyNm(),
		                        invoice.getYear(),
		                        invoice.getMonth()))
		                .build();

		        Context context = new Context();
		        context.setVariable("invoice", invoice);
		        context.setVariable("categories", Arrays.asList("서류", "롤", "샘플", "기타"));
		        context.setVariable("billId", billDtlVO.getBillId());
		        context.setVariable("billDtlId", billDtlVO.getBillDtlId());

		        mailSendComponent.sendMail(context, "/mail/invoiceTemplate", mailDTO);

			} catch (Exception e) {
                status = "FAIL";
                failLog = e.getMessage();
			}finally {
                mailHistory.setToCompanyId(billDtlVO.getCalculationCompanyId()); // 메일_수신회사ID
                mailHistory.setFromCompanyId(baseBillVO.getBillCompanyId()); // 메일_발신회사ID
                mailHistory.setMailStatusCd(status); // 발송_상태_코드
                mailHistory.setFailLog(failLog); // 실패_로그
                mailHistory.setBillId(billDtlVO.getBillId()); // 청구_ID
                mailHistory.setBillDtlId(billDtlVO.getBillDtlId()); // 청구_상세_ID
                //발송 기록
				mailMapper.insertMailHistory(mailHistory, memberDTO);
			}

		}
		return true;
	}
}
