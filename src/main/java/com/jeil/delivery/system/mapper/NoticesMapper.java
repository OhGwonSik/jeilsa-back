package com.jeil.delivery.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.domain.NoticesVO;
import com.jeil.delivery.system.domain.ShipperVO;

@Mapper
public interface NoticesMapper {

	public List<NoticesVO> selectNoticesList(NoticesVO noticesVO);

	public int insertNotices(@Param("param") NoticesVO noticesVO, @Param("memberInfo") MemberDTO memberDTO);

	public int updateNotices(@Param("param") NoticesVO noticesVO, @Param("memberInfo") MemberDTO memberDTO);

	public int deleteNotices(@Param("param") NoticesVO noticesVO, @Param("memberInfo") MemberDTO memberDTO);

}
