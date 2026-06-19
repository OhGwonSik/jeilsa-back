package com.jeil.delivery.deliverylogic.service;

import java.util.List;

import com.common.auth.common.util.SecurityUtil;
import org.springframework.stereotype.Service;

import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.deliverylogic.domain.DeliveryRouteDTO;
import com.jeil.delivery.deliverylogic.domain.DeliveryRouteVO;
import com.jeil.delivery.deliverylogic.mapper.DeliveryRouteMapper;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.domain.CodeVO;
import com.jeil.delivery.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryRouteService {

	private final DeliveryRouteMapper deliveryRouteMapper;

    public List<DeliveryRouteVO> selectDeliveryRouteList(DeliveryRouteVO deliveryRouteVO) {
        return deliveryRouteMapper.selectDeliveryRouteList(deliveryRouteVO);
    }

    public List<CodeVO> selectDeliveryRouteCodeList(CodeVO codeVO){
    	return deliveryRouteMapper.selectDeliveryRouteCodeList(codeVO);
    }

    public List<DeliveryRouteDTO> selectCompanyDeliveryRouteList(DeliveryRouteDTO deliveryRouteDTO){
    	return deliveryRouteMapper.selectCompanyDeliveryRouteList(deliveryRouteDTO);
    }

    public int insertDeliveryRoute(DeliveryRouteDTO deliveryRouteDTO) {
    	MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
        int count = deliveryRouteMapper.insertDeliveryRoute(deliveryRouteDTO, memberDTO);

        if (count == 0) {
            throw new InsertCheckedException("배송 경로 등록 실패");
        }
        return count;
    }

    public int updateDeliveryRoute(DeliveryRouteDTO deliveryRouteDTO) {
    	MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
        int count = deliveryRouteMapper.updateDeliveryRoute(deliveryRouteDTO, memberDTO);

        if (count == 0) {
            throw new UpdateCheckedException("배송 경로 수정 실패");
        }
        return count;
    }

    public boolean checkCompanyDeliveryRouteInfo(Integer deliveryRouteId) {
    	//값 존재시 true, 값 없으면 false
    	return deliveryRouteMapper.checkCompanyDeliveryRouteInfo(deliveryRouteId);
    }

    public int deleteDeliveryRoute(DeliveryRouteDTO deliveryRouteDTO) {
    	MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
        int count = deliveryRouteMapper.deleteDeliveryRoute(deliveryRouteDTO, memberDTO);

        if (count == 0) {
            throw new DeleteCheckedException("배송 경로 삭제 실패");
        }

        return count;
    }
}
