package com.jeil.delivery.configuration.mail.mapper;

import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.configuration.mail.domain.MailHistoryVO;
import com.jeil.delivery.domain.MemberDTO;

public interface MailMapper {

	public int insertMailHistory(@Param("param")MailHistoryVO mailhistoryVO, @Param("memberInfo") MemberDTO memberDTO);
}
