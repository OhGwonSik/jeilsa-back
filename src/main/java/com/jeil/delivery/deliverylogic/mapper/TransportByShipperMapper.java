// TransportMapper.java
package com.jeil.delivery.deliverylogic.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jeil.delivery.deliverylogic.domain.TransportByShipperDTO;

@Mapper
public interface TransportByShipperMapper {

	public List<TransportByShipperDTO> selectTransportInfoByshipper(TransportByShipperDTO TransportByShipperDTO);

	public List<TransportByShipperDTO> selectSettlementByShipper(TransportByShipperDTO TransportByShipperDTO);

}
