package com.jeil.delivery.deliverylogic.service;

import java.util.List;
import java.util.Optional;

import com.common.auth.common.util.SecurityUtil;
import com.jeil.delivery.billing.domain.PagedResult;
import com.jeil.delivery.deliverylogic.domain.WaybillSearchCond;
import org.springframework.stereotype.Service;

import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.deliverylogic.domain.WayBillVO;
import com.jeil.delivery.deliverylogic.mapper.WayBillMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WayBillService {

	private final WayBillMapper wayBillMapper;

	public List<WayBillVO> selectWaybillList(WayBillVO wayBillVO) {
		return wayBillMapper.selectWaybillList(wayBillVO);
	}

    public PagedResult<WayBillVO> selectWaybillListPaged(WaybillSearchCond cond) {
        List<WayBillVO> rows = wayBillMapper.selectWaybillListPaged(cond);
        long total = rows.isEmpty() ? 0L : Optional.ofNullable(rows.get(0).getTotal()).orElse(0L);
        return new PagedResult<>(rows, total);
    }

	public int insertWaybill(WayBillVO wayBillVO) {
		Integer currentMemberId = SecurityUtil.getCurrentMemberId();
		wayBillVO.setChgId(currentMemberId);
		wayBillVO.setRegId(currentMemberId);
		int count = wayBillMapper.insertWaybill(wayBillVO);

		if(count == 0) {
			throw new InsertCheckedException("insert Error");
		}

		return count;
	}

	public int updateWaybill(WayBillVO wayBillVO) {
		Integer currentMemberId = SecurityUtil.getCurrentMemberId();
		wayBillVO.setChgId(currentMemberId);
		int count = wayBillMapper.updateWaybill(wayBillVO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		return count;
	}

	public int deleteWaybill(WayBillVO wayBillVO) {
		Integer currentMemberId = SecurityUtil.getCurrentMemberId();
		wayBillVO.setChgId(currentMemberId);
		int count = wayBillMapper.deleteWaybill(wayBillVO);

		if(count == 0) {
			throw new DeleteCheckedException("delete Error");
		}

		return count;
	}

	public WayBillVO printWaybill(int waybillId) {
		return wayBillMapper.printWaybill(waybillId);
	}
}
