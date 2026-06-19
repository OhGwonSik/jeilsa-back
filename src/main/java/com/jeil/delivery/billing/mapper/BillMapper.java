package com.jeil.delivery.billing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.billing.domain.BillDTO;
import com.jeil.delivery.billing.domain.BillVO;
import com.jeil.delivery.domain.MemberDTO;

@Mapper
public interface BillMapper {

	public BillVO selectBillByTransportData(BillVO billHistoryVO);

    public Boolean existsBillData(BillVO billHistoryVO);

	public List<BillVO> selectBillHistoryList(BillVO billVO);

	public int insertBill(@Param("param") BillVO billHistoryVO, @Param("memberInfo") MemberDTO memberDTO);

	public int updateBillReCalculation(@Param("param") BillDTO billDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int updateBillHistory(BillVO billHistoryVO);

	public int deleteBillHistory(BillVO billHistoryVO);
}
