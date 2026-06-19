package com.jeil.delivery.deliverylogic.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.enums.PermissionHierarchyLevel;
import com.jeil.delivery.deliverylogic.domain.TransportByShipperDTO;
import com.jeil.delivery.deliverylogic.service.TransportByShipperService;
import com.jeil.delivery.domain.MemberDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transport/shipper")
public class TransportByShipperController {
	private final TransportByShipperService transportByShipperService;

	@GetMapping("/info")
	@PreAuthorize("@permissionHelper.hasMinLevel('user','transportByShipper','read')")
	public List<TransportByShipperDTO> selectTransportInfoByshipper(TransportByShipperDTO transportByShipperDTO){
		return transportByShipperService.selectTransportInfoByshipper(transportByShipperDTO);
	}

	@GetMapping("/invoice/info")
	@PreAuthorize("@permissionHelper.hasMinLevel('user','transportByShipper','read')")
	public List<TransportByShipperDTO> selectTransportInfoByshipperInbovice(TransportByShipperDTO transportByShipperDTO){
		return transportByShipperService.selectTransportInfoByshipperInvoice(transportByShipperDTO);
	}

}
