package com.jeil.delivery.billing.controller;

import com.jeil.delivery.billing.domain.BillCompanyVO;
import com.jeil.delivery.billing.service.BillCompanyService;
import com.jeil.delivery.system.domain.CodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bill-company")
public class BillCompanyController {

	private final BillCompanyService billCompanyService;

	@GetMapping("/list")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'billCompany', 'read')")
	public List<BillCompanyVO> selectBillCompanyList(BillCompanyVO billCompanyVO) {
		return billCompanyService.selectBillCompanyList(billCompanyVO);
	}

	@GetMapping("/code/list")
	@PreAuthorize("@permissionHelper.hasMinLevel('user', 'billCompany', 'read')")
	public List<CodeVO> selectBillCompanyCodeList(CodeVO codeVO){
		return billCompanyService.selectBillCompanyCodeList(codeVO);
	}

	@GetMapping("/code/detail/list")
	@PreAuthorize("@permissionHelper.hasMinLevel('user', 'billCompany', 'read')")
	public List<CodeVO> selectBillCompanyCodeDetailList(CodeVO codeVO){
		return billCompanyService.selectBillCompanyCodeDetailList(codeVO);
	}

	@PostMapping("/insert")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'billCompany', 'create')")
	public int insertBillCompany(@RequestBody BillCompanyVO billCompanyVO) {
		return billCompanyService.insertBillCompany(billCompanyVO);
	}

	@PutMapping("/update")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'billCompany', 'update')")
	public int updateBillCompany(@RequestBody BillCompanyVO billCompanyVO) {
		return billCompanyService.updateBillCompany(billCompanyVO);
	}

	@DeleteMapping("/delete/{id}")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'billCompany', 'delete')")
	public int deleteBillCompany(@PathVariable("id") int id) {
		BillCompanyVO billCompanyVO = new BillCompanyVO();
		billCompanyVO.setBillCompanyId(id);
		return billCompanyService.deleteBillCompany(billCompanyVO);
	}
}
