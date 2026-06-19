package com.jeil.delivery.billing.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.jeil.delivery.billing.domain.BillDTO;
import com.jeil.delivery.billing.domain.BillVO;
import com.jeil.delivery.billing.mapper.BillDtlMapper;
import com.jeil.delivery.billing.mapper.BillMapper;
import com.jeil.delivery.billing.mapper.BillTransportMapper;
import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.NoDataException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.configuration.mail.domain.BillCreatedEvent;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillService {

	private final BillMapper billMapper;
	private final BillDtlMapper billDtlMapper;
	private final BillTransportMapper billTransportMapper;

	public List<BillVO> selectBillHistoryList(BillVO billVO) {
		return billMapper.selectBillHistoryList(billVO);
	}

	public int insertBillHistory(BillVO baseBillVO) {

		// 청구회사, 년, 월, 청구기준일 기준 데이터 있으면 exception
		Boolean existBillData = billMapper.existsBillData(baseBillVO);

		if(Boolean.TRUE.equals(existBillData)) {
			throw new NoDataException("청구 회사의 년, 월 기준 정산 내역이 존재합니다.");
		}

		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		int count = 0;

		BillVO billData = billMapper.selectBillByTransportData(baseBillVO);

		if(billData == null) {
			throw new NoDataException("정산할 데이터가 없습니다.");
		}

		//bill insert
		count = billMapper.insertBill(billData, memberDTO);

		if(count == 0) {
			throw new InsertCheckedException();
		}

		baseBillVO.setBillId(billData.getBillId());
		List<BillDTO> billDtlList = billDtlMapper.selectBillDtlByTransportData(baseBillVO);

		count = billDtlMapper.insertBillDtl(billDtlList, memberDTO);

		if(count == 0) {
			throw new InsertCheckedException();
		}

		count = billTransportMapper.insertBillTransportFromTransport(baseBillVO, memberDTO);

		count = billTransportMapper.insertBillTransportDtlFromTransport(baseBillVO, memberDTO);

		return count;
	}

	//정산 그룹별 재계산인경우 calculationCompanyId가 있고 전체인 경우 calculationCompanyId가 없음.
	public int insertBillReCalculation(BillDTO billDTO) {

		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		int count = 0;

		//정산 그룹별 재계산인경우 calculationCompanyId가 있고 전체인 경우 calculationCompanyId가 없음.
		if(billDTO.getCalculationCompanyId() != null && billDTO.getCalculationCompanyId() != 0) {
			// 정산 그룹별 재계산
			count = billDtlMapper.insertBillDtlReCalculation(billDTO, memberDTO);
			if(count == 0){
				throw new InsertCheckedException();
			}

			count = billTransportMapper.insertBillTransportIfNotExists(billDTO, memberDTO);

			count = billTransportMapper.insertBillTransportDtlReCalculation(billDTO, memberDTO);

		}else {
			// 전체 재계산
			count = billDtlMapper.insertBillDtlReCalculationAll(billDTO, memberDTO);
			if(count == 0){
				throw new InsertCheckedException();
			}

			count = billTransportMapper.insertBillTransportAllIfNotExists(billDTO, memberDTO);

			count = billTransportMapper.insertBillTransportDtlReCalculationAll(billDTO, memberDTO);
		}

		count = billMapper.updateBillReCalculation(billDTO, memberDTO);
		if(count == 0) {
			throw new UpdateCheckedException();
		}

		return count;
	}

	public int updateBillHistory(BillVO billHistoryVO) {
		int count = billMapper.updateBillHistory(billHistoryVO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		return count;
	}

	public int deleteBillHistory(BillVO billHistoryVO) {
		int count = billMapper.deleteBillHistory(billHistoryVO);

		if(count == 0) {
			throw new DeleteCheckedException("delete Error");
		}

		return count;
	}
}
