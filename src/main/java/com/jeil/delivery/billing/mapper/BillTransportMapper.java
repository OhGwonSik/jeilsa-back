package com.jeil.delivery.billing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.billing.domain.BillDTO;
import com.jeil.delivery.billing.domain.BillVO;
import com.jeil.delivery.domain.MemberDTO;

@Mapper
public interface BillTransportMapper {

	public int insertBillTransportIfNotExists(@Param("param") BillDTO billDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int insertBillTransportAllIfNotExists(@Param("param") BillDTO billDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int insertBillTransportFromTransport(@Param("param") BillVO billVO, @Param("memberInfo") MemberDTO memberDTO);

	public int insertBillTransportDtlFromTransport(@Param("param") BillVO billVO, @Param("memberInfo") MemberDTO memberDTO);

	public int insertBillTransportDtlReCalculation(@Param("param") BillDTO billDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int insertBillTransportDtlReCalculationAll(@Param("param") BillDTO billDTO, @Param("memberInfo") MemberDTO memberDTO);
}
