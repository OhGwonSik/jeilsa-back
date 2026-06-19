package com.jeil.delivery.deliverylogic.controller;

import java.util.HashMap;
import java.util.Map;

import com.jeil.delivery.billing.domain.PagedResult;
import com.jeil.delivery.deliverylogic.domain.WaybillSearchCond;
import com.jeil.delivery.deliverylogic.mapper.WaybillSortMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.jeil.delivery.deliverylogic.domain.WayBillVO;
import com.jeil.delivery.deliverylogic.service.WayBillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WayBillController {

	private final WayBillService wayBillService;

//	@GetMapping("/waybill/list")
//	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'wayBill', 'read')")
//	public List<WayBillVO> selectWaybillList(WayBillVO wayBillVO) {
//		return wayBillService.selectWaybillList(wayBillVO);
//	}

    @GetMapping("/waybill/list")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'wayBill', 'read')")
    public Map<String, Object> selectWaybillListPaged(
            WayBillVO wayBillVO,                       // 기존 검색필드 재사용: senderCompanyNm, receiverCompanyNm, searchNo, waybillId ...
            @RequestParam(name="pageIndex", defaultValue = "1") int pageIndex,  // 1-base
            @RequestParam(name="pageSize", defaultValue = "200") int pageSize,
            @RequestParam(name="sortCol", required = false) String sortCol,   // 선택: 정렬
            @RequestParam(name="sortDir", required = false) String sortDir
    ) {
        int size = Math.max(1, Math.min(pageSize, 1000));
        int offset = Math.max(0, (Math.max(1, pageIndex) - 1) * size);
        String orderBy = WaybillSortMapper.toOrderBy(sortCol, sortDir); // 화이트리스트 매핑

        WaybillSearchCond cond = WaybillSearchCond.builder()
                .waybillId(wayBillVO.getWaybillId())
                .senderCompanyNm(wayBillVO.getSenderCompanyNm())
                .receiverCompanyNm(wayBillVO.getReceiverCompanyNm())
                .searchNo(wayBillVO.getSearchNo())
                .orderBy(orderBy)
                .limit(size)
                .offset(offset)
                .build();

        PagedResult<WayBillVO> result = wayBillService.selectWaybillListPaged(cond);

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", result.getItems());
        resp.put("total", result.getTotal());
        return resp;
    }

	@PostMapping("/waybill/insert")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'wayBill', 'create')")
	public int insertWaybill(@RequestBody WayBillVO wayBillVO) {
		return wayBillService.insertWaybill(wayBillVO);
	}

	@PutMapping("/waybill/update")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'wayBill', 'update')")
	public int updateWaybill(@RequestBody WayBillVO wayBillVO) {
		return wayBillService.updateWaybill(wayBillVO);
	}

	@DeleteMapping("/waybill/delete/{id}")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'wayBill', 'delete')")
	public int deleteWaybill(@PathVariable("id") int id) {
		WayBillVO wayBillVO = new WayBillVO();
		wayBillVO.setWaybillId(id);
		return wayBillService.deleteWaybill(wayBillVO);
	}

	@GetMapping("/waybill/print/{waybillId}")
	public WayBillVO printWaybill(@PathVariable("waybillId") int waybillId) {
		return wayBillService.printWaybill(waybillId);
	}
}
