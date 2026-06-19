package com.jeil.delivery.deliverylogic.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.deliverylogic.domain.TransportDtlVO;
import com.jeil.delivery.deliverylogic.mapper.TransportDtlMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransportDtlService {

	private final TransportDtlMapper transportDtlMapper;

	public List<TransportDtlVO> selectTransportDtlList(TransportDtlVO transportDtlVO) {
		return transportDtlMapper.selectTransportDtlList(transportDtlVO);
	}

	public int insertTransportDtl(TransportDtlVO transportDtlVO) {
		int count = transportDtlMapper.insertTransportDtl(transportDtlVO);

		if(count == 0) {
			throw new InsertCheckedException("insert Error");
		}

		return count;
	}

	public int updateTransportDtl(TransportDtlVO transportDtlVO) {
		int count = transportDtlMapper.updateTransportDtl(transportDtlVO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		return count;
	}

	public int deleteTransportDtlByIds(List<Integer> ids, int chgId) {
		if (ids == null || ids.isEmpty()) return 0;

		int count = transportDtlMapper.deleteTransportDtl(ids, chgId);

		if (count == 0) {
			throw new DeleteCheckedException("delete Error: no rows affected");
		}

		return count;
	}
}
