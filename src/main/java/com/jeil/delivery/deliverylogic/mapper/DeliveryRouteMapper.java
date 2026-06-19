package com.jeil.delivery.deliverylogic.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.deliverylogic.domain.DeliveryRouteDTO;
import com.jeil.delivery.deliverylogic.domain.DeliveryRouteVO;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.domain.CodeVO;
import com.jeil.delivery.system.domain.CompanyDTO;

@Mapper
public interface DeliveryRouteMapper {

    public List<DeliveryRouteVO> selectDeliveryRouteList(DeliveryRouteVO deliveryRouteVO);

    public List<CodeVO> selectDeliveryRouteCodeList(CodeVO codeVO);

    public int insertDeliveryRoute(@Param("param") DeliveryRouteDTO deliveryRouteDTO, @Param("memberInfo") MemberDTO memberDTO);

    public int updateDeliveryRoute(@Param("param") DeliveryRouteDTO deliveryRouteDTO, @Param("memberInfo") MemberDTO memberDTO);

    public int deleteDeliveryRoute(@Param("param") DeliveryRouteDTO deliveryRouteDTO, @Param("memberInfo") MemberDTO memberDTO);

    public int insertCompanyAndDeliberyRoute(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

    public int updateCompanyAndDeliveryRoute(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

    public int deleteCompanyAndDeliveryRoute(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

    public int deleteCompanyAndDeliveryRouteById(CompanyDTO companyDTO);

    public CompanyDTO selectDeliveryRouteByCompany(CompanyDTO companyDTO);

    public List<DeliveryRouteDTO> selectCompanyDeliveryRouteList(DeliveryRouteDTO deliveryRouteDTO);

    public boolean checkCompanyDeliveryRouteInfo(Integer deliveryRouteId);
}
