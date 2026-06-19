package com.jeil.delivery.deliverylogic.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.deliverylogic.domain.TransportDtlVO;

@Mapper
public interface TransportDtlMapper {

	public List<TransportDtlVO> selectTransportDtlList(TransportDtlVO transportDtlVO);

    public int insertTransportDtl(TransportDtlVO transportDtlVO);

    public int updateTransportDtl(TransportDtlVO transportDtlVO);

    public int deleteTransportDtl( @Param("ids") List<Integer> ids,
                                   @Param("chgId") Integer chgId);

    public int deleteTransportDtlAll(@Param("transportId") Integer transportId,
                                     @Param("chgId") Integer chgId);
    int delete(@Param("transportId") int transportId, @Param("chgId") int chgId);
}
