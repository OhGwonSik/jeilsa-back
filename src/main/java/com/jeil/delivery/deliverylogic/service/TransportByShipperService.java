package com.jeil.delivery.deliverylogic.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.common.auth.common.enums.PermissionHierarchyLevel;
import com.jeil.delivery.deliverylogic.domain.TransportByShipperDTO;
import com.jeil.delivery.deliverylogic.mapper.TransportByShipperMapper;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransportByShipperService {
	private final TransportByShipperMapper transportByShipperMapper;

	public List<TransportByShipperDTO> selectTransportInfoByshipper(TransportByShipperDTO transportByShipperDTO){
		//isUser가 Y이면 company_id를 쿼리에 파라미터로 보냄
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		boolean isUser = PermissionHierarchyLevel.USER.getName().equals(memberDTO.getRoles());
		transportByShipperDTO.setIsUser(isUser);
		transportByShipperDTO.setUserCompanyId(memberDTO.getUserCompanyId());
		return transportByShipperMapper.selectTransportInfoByshipper(transportByShipperDTO);
	}

	public List<TransportByShipperDTO> selectTransportInfoByshipperInvoice(TransportByShipperDTO transportByShipperDTO){
		//isUser가 Y이면 company_id를 쿼리에 파라미터로 보냄
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		boolean isUser = PermissionHierarchyLevel.USER.getName().equals(memberDTO.getRoles());
		transportByShipperDTO.setIsUser(isUser);
		transportByShipperDTO.setUserCompanyId(memberDTO.getUserCompanyId());
		return transportByShipperMapper.selectSettlementByShipper(transportByShipperDTO);
	}

}
