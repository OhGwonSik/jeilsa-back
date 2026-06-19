package com.jeil.delivery.billing.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jeil.delivery.billing.domain.BillDtlVO;
import com.jeil.delivery.billing.mapper.BillDtlMapper;
import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillDtlService {

	private final BillDtlMapper billHistoryDtlMapper;

	public List<BillDtlVO> selectBillHistoryDtlList(BillDtlVO billDtlVO) {
		return billHistoryDtlMapper.selectBillHistoryDtlList(billDtlVO);
	}

	public int updateBillHistoryDtl(BillDtlVO billHistoryDtlVO) {
		int count = billHistoryDtlMapper.updateBillHistoryDtl(billHistoryDtlVO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		return count;
	}

	public int deleteBillHistoryDtl(BillDtlVO billHistoryDtlVO) {
		int count = billHistoryDtlMapper.deleteBillHistoryDtl(billHistoryDtlVO);

		if(count == 0) {
			throw new DeleteCheckedException("delete Error");
		}

		return count;
	}
}
