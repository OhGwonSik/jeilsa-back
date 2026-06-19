package com.jeil.delivery.system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.domain.ShipperVO;
import com.jeil.delivery.system.mapper.ShipperMapper;
import com.jeil.delivery.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipperService {

    private final ShipperMapper shipperMapper;

	public List<CompanyDTO> selectShipperList(CompanyDTO companyDTO) {
		return shipperMapper.selectShipperList(companyDTO);
	}

	public int insertShipper(ShipperVO shipperVO) {
		int count = shipperMapper.insertShipper(shipperVO);

		if(count == 0) {
			throw new InsertCheckedException("insert Error");
		}

		return count;
	}

	public int updateShipper(CompanyDTO companyDTO) {
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		int count = shipperMapper.updateShipper(companyDTO, memberDTO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		return count;
	}

	public int deleteShipper(CompanyDTO companyDTO) {
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		int count = shipperMapper.deleteShipper(companyDTO, memberDTO);

		if(count == 0) {
			throw new DeleteCheckedException("delete Error");
		}

		return count;
	}
}
