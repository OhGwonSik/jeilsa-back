package com.jeil.delivery.system.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.domain.ShipperVO;
import com.jeil.delivery.system.service.ShipperService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shipper")
public class ShipperController {

	private final ShipperService shipperService;

	@GetMapping("/list")
	@PreAuthorize("@permissionHelper.hasMinLevel('user', 'shipper', 'read')")
	public List<CompanyDTO> selectShipperList(CompanyDTO companyDTO) {
		return shipperService.selectShipperList(companyDTO);
	}

	@PostMapping("/insert")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'shipper', 'create')")
	public int insertShipper(ShipperVO shipperVO) {
		return shipperService.insertShipper(shipperVO);
	}

	@PutMapping("/update")
	@PreAuthorize("@permissionHelper.hasMinLevel('user', 'shipper', 'update')")
	public int updateShipper(@RequestBody CompanyDTO companyDTO) {
		return shipperService.updateShipper(companyDTO);
	}

	@DeleteMapping("/delete")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'shipper', 'delete')")
	public int deleteShipper(@RequestBody CompanyDTO companyDTO) {
		return shipperService.deleteShipper(companyDTO);
	}
}
