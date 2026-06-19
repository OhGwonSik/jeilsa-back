package com.jeil.delivery.system.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jeil.delivery.deliverylogic.domain.CompanySearchCond;
import com.jeil.delivery.billing.domain.PagedResult;
import com.jeil.delivery.deliverylogic.mapper.CompanySortMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.service.CompanyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
@Slf4j
public class CompanyController {

	private final CompanyService companyService;

	@GetMapping("/biz-no/check/{bizNo}")
	@PreAuthorize("@permissionHelper.hasMinLevel('user', 'company', 'read')")
	public boolean checkBizNo(@PathVariable("bizNo") String bizNo) {
		return companyService.checkBizNo(bizNo);
	}

	@GetMapping("/list")
	@PreAuthorize("@permissionHelper.hasMinLevel('user', 'company', 'read')")
	public List<CompanyDTO> selectCompanyList(CompanyDTO companyDTO) {
		return companyService.selectCompanyList(companyDTO);
	}

    @GetMapping("/listPaged")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'company', 'read')")
    public Map<String, Object> selectCompanyListPaged(
            CompanyDTO companyDTO,
            @RequestParam(name = "pageIndex", defaultValue = "1") int pageIndex,  // 1-base
            @RequestParam(name = "pageSize", defaultValue = "200") int pageSize,
            @RequestParam(name = "sortCol", required = false) String sortCol,   // 선택: 정렬
            @RequestParam(name = "sortDir", required = false) String sortDir
    ) {
        int size = Math.max(1, Math.min(pageSize, 1000));
        int offset = Math.max(0, (Math.max(1, pageIndex) - 1) * size);
        String orderBy = CompanySortMapper.toOrderBy(sortCol, sortDir); // 화이트리스트 매핑

        CompanySearchCond cond = CompanySearchCond.builder()
                .companyId(companyDTO.getCompanyId())
                .companyNm(companyDTO.getCompanyNm())
                .telNo(companyDTO.getTelNo())
                .shipperYn(companyDTO.getShipperYn())
                .shipperCd(companyDTO.getShipperCd())
                .regionCd(companyDTO.getRegionCd())
                .billCompanyId(companyDTO.getBillCompanyId())
                .isUser(companyDTO.getIsUser())
                .userCompanyId(companyDTO.getUserCompanyId())
                .limit(size)
                .offset(offset)
                .orderBy(orderBy)
                .build();

        PagedResult<CompanyDTO> result = companyService.selectCompanyListPaged(cond);

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", result.getItems());
        resp.put("total", result.getTotal());
        return resp;
    }

	@PostMapping("/insert")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'company', 'create')")
	public int insertCompany(@RequestBody CompanyDTO companyDTO) {
		return companyService.insertCompany(companyDTO);
	}

	@PutMapping("/update")
	@PreAuthorize("@permissionHelper.hasMinLevel('user', 'company', 'update')")
	public int updateCompany(@RequestBody CompanyDTO companyDTO) {
		return companyService.updateCompany(companyDTO);
	}

	@DeleteMapping("/delete")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'company', 'delete')")
	public int deleteCompany(@RequestBody CompanyDTO companyDTO) {
		return companyService.deleteCompany(companyDTO);
	}
}
