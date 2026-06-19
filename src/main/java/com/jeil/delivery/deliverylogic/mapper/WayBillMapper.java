package com.jeil.delivery.deliverylogic.mapper;

import java.util.List;

import com.jeil.delivery.deliverylogic.domain.WaybillSearchCond;
import org.apache.ibatis.annotations.Mapper;

import com.jeil.delivery.deliverylogic.domain.WayBillVO;

@Mapper
public interface WayBillMapper {

	public List<WayBillVO> selectWaybillList(WayBillVO wayBillVO);

    public int insertWaybill(WayBillVO wayBillVO);

    public int updateWaybill(WayBillVO wayBillVO);

    public int deleteWaybill(WayBillVO wayBillVO);

    public WayBillVO printWaybill(int waybillId);

    List<WayBillVO> selectWaybillListPaged(WaybillSearchCond cond);
}
