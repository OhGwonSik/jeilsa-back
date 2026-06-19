package com.jeil.delivery.billing.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeil.delivery.billing.domain.BillDTO;
import com.jeil.delivery.billing.domain.BillVO;
import com.jeil.delivery.billing.service.BillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bill")
@Slf4j
public class BillController {

	private final BillService billService;

	@GetMapping("/list")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','bill','read')")
	public List<BillVO> selectBillHistoryList(BillVO billVO) {
		return billService.selectBillHistoryList(billVO);
	}

	@PostMapping("/insert")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','bill','create')")
	public int insertBillHistory(@RequestBody BillVO billVO) {
		return billService.insertBillHistory(billVO);
	}

	@PostMapping("/re-calculation")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','bill','update')")
	public int insertBillReCalculation(@RequestBody BillDTO billDTO) {
		return billService.insertBillReCalculation(billDTO);
	}

	@PutMapping("/update")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','bill','update')")
	public int updateBillHistory(BillVO billHistoryVO) {
		return billService.updateBillHistory(billHistoryVO);
	}

	@DeleteMapping("/delete")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','bill','delete')")
	public int deleteBillHistory(BillVO billHistoryVO) {
		return billService.deleteBillHistory(billHistoryVO);
	}
}
