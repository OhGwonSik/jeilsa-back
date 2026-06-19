package com.jeil.delivery.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.domain.ShipperVO;

@Mapper
public interface ShipperMapper {

	public List<CompanyDTO> selectShipperList(CompanyDTO companyDTO);

	public CompanyDTO existShipperById(CompanyDTO companyDTO);

	public int insertShipper(ShipperVO shipperVO);

	public int insertCompanyShipper(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int updateShipper(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int deleteShipper(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int deleteShipperById(CompanyDTO companyDTO);

    boolean checkCompanyByBillCompanyId(CompanyDTO companyDTO);
}
