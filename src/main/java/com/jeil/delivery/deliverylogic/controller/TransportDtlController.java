package com.jeil.delivery.deliverylogic.controller;

import com.jeil.delivery.deliverylogic.domain.TransportDtlVO;
import com.jeil.delivery.deliverylogic.service.TransportDtlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransportDtlController {

	private final TransportDtlService transportDtlService;

	@GetMapping("/transport/transportDtl-list")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'transportDtl', 'read')")
	public List<TransportDtlVO> selectedTransportDtlList(TransportDtlVO transportDtlVO) {
		return transportDtlService.selectTransportDtlList(transportDtlVO);
	}

//	@PostMapping("/api/transport-detail/insert")
//	public int insertTransportDtl(TransportDtlVO transportDtlVO) {
//		return transportDtlService.insertTransportDtl(transportDtlVO);
//	}
//
//	@PutMapping("/api/transport-detail/update")
//	public int updateTransportDtl(TransportDtlVO transportDtlVO) {
//		return transportDtlService.updateTransportDtl(transportDtlVO);
//	}
}
