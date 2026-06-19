package com.jeil.delivery.billing.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeil.delivery.billing.domain.BillDtlVO;
import com.jeil.delivery.billing.service.BillDtlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bill-detail")
public class BillDtlController {

	private final BillDtlService billHistoryDtlService;

	@GetMapping("/list")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','bill','read')")
	public List<BillDtlVO> selectBillHistoryDtlList(BillDtlVO billDtlVO) {
		return billHistoryDtlService.selectBillHistoryDtlList(billDtlVO);
	}

	@PutMapping("/update")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','bill','update')")
	public int updateBillHistoryDtl(BillDtlVO billHistoryDtlVO) {
		return billHistoryDtlService.updateBillHistoryDtl(billHistoryDtlVO);
	}

	@DeleteMapping("/delete")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','bill','delete')")
	public int deleteBillHistoryDtl(BillDtlVO billHistoryDtlVO) {
		return billHistoryDtlService.deleteBillHistoryDtl(billHistoryDtlVO);
	}
}
