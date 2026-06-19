// TransportMapper.java
package com.jeil.delivery.deliverylogic.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.deliverylogic.domain.DeliveryStausExcelDTO;
import com.jeil.delivery.deliverylogic.domain.TransportVO;

@Mapper
public interface TransportMapper {
    List<TransportVO> selectTransportList(TransportVO transportVO);

    TransportVO selectTransportListByCompanyId(TransportVO transportVO);
    TransportVO selectTransportListById(TransportVO transportVO);

    List<TransportVO> selectArrivalsList(TransportVO transportVO);

    int insertTransport(TransportVO transportVO);

    int updateTransport(TransportVO transportVO);

    List<DeliveryStausExcelDTO> selectDeliveryStatusList(DeliveryStausExcelDTO deliveryStausExcelDTO);

    int delete(@Param("transportId") int transportId, @Param("chgId") int chgId);

    List<TransportVO> selectReceiverTransportList(TransportVO transportVO);
    List<TransportVO> selectSenderTransportList(TransportVO transportVO);
}
