package com.jeil.delivery.system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.domain.NoticesVO;
import com.jeil.delivery.system.mapper.NoticesMapper;
import com.jeil.delivery.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticesService {

    private final NoticesMapper noticesMapper;

	public List<NoticesVO> selectNoticesList(NoticesVO noticesVO) {
		return noticesMapper.selectNoticesList(noticesVO);
	}

	public int insertNotices(NoticesVO noticesVO) {
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		int count = noticesMapper.insertNotices(noticesVO, memberDTO);

		if(count == 0) {
			throw new InsertCheckedException("insert Error");
		}

		return count;
	}

	public int updateNotices(NoticesVO noticesVO) {
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		int count = noticesMapper.updateNotices(noticesVO, memberDTO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		return count;
	}

	public int deleteNotices(NoticesVO noticesVO) {
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		int count = noticesMapper.deleteNotices(noticesVO, memberDTO);

		if(count == 0) {
			throw new DeleteCheckedException("delete Error");
		}

		return count;
	}
}
