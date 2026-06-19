package com.jeil.delivery.deliverylogic.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jeil.delivery.deliverylogic.domain.DeliveryRouteDTO;
import com.jeil.delivery.deliverylogic.domain.DeliveryRouteVO;
import com.jeil.delivery.deliverylogic.service.DeliveryRouteService;
import com.jeil.delivery.system.domain.CodeVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery-route")
@Slf4j
public class DeliveryRouteController {

	private final DeliveryRouteService deliveryRouteService;

    @GetMapping("/list")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner','deliveryRoute','read')")
    public List<DeliveryRouteVO> selectDeliveryRouteList(DeliveryRouteVO deliveryRouteVO) {
        return deliveryRouteService.selectDeliveryRouteList(deliveryRouteVO);
    }

    @GetMapping("/code/list")
    @PreAuthorize("@permissionHelper.hasMinLevel('user','deliveryRoute','read')")
    public List<CodeVO> selectDeliveryRouteCodeList(CodeVO codeVO){
    	return deliveryRouteService.selectDeliveryRouteCodeList(codeVO);
    }

    @GetMapping("/mapping/list")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner','deliveryRoute','read')")
    public List<DeliveryRouteDTO> selectCompanyDeliveryRouteList(DeliveryRouteDTO deliveryRouteDTO) {
        return deliveryRouteService.selectCompanyDeliveryRouteList(deliveryRouteDTO);
    }

    @GetMapping("/mapping/exists/{deliveryRouteId}")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner','deliveryRoute','read')")
    public boolean existsCompanyDeliveryRouteInfo(@PathVariable("deliveryRouteId") Integer deliveryRouteId) {
    	return deliveryRouteService.checkCompanyDeliveryRouteInfo(deliveryRouteId);
    }

    @PostMapping("/insert")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner','deliveryRoute','create')")
    public int insertDeliveryRoute(@RequestBody DeliveryRouteDTO deliveryRouteDTO) {
        return deliveryRouteService.insertDeliveryRoute(deliveryRouteDTO);
    }

    @PutMapping("/update")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner','deliveryRoute','update')")
    public int updateDeliveryRoute(@RequestBody DeliveryRouteDTO deliveryRouteDTO) {
        return deliveryRouteService.updateDeliveryRoute(deliveryRouteDTO);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner','deliveryRoute','delete')")
    public int deleteDeliveryRoute(@RequestBody DeliveryRouteDTO deliveryRouteDTO) {
        return deliveryRouteService.deleteDeliveryRoute(deliveryRouteDTO);
    }
}
