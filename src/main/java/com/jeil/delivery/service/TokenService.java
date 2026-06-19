package com.jeil.delivery.service;

import org.springframework.stereotype.Service;

import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.domain.RefreshTokenVO;
import com.jeil.delivery.mapper.TokenMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {
	private final TokenMapper tokenMapper;

	public RefreshTokenVO getRefreshTokenByUser(String refreshToken) {
		return tokenMapper.getRefreshTokenByUser(refreshToken);
	}

	public RefreshTokenVO getRefreshTokenByMemberId(MemberDTO memberDTO) {
		return tokenMapper.getRefreshTokenByMemberId(memberDTO);
	}

	public int insertRefreshToken(RefreshTokenVO refreshTokenVO) {
		int count = 0;

		// 혹시 있을 걸 대비
		tokenMapper.deleteRefreshTokenById(refreshTokenVO);

		count = tokenMapper.insertRefreshToken(refreshTokenVO);

		if(count == 0) {
			throw new InsertCheckedException("리프레시 토큰 저장 오류");
		}

		return count;
	}

	public int deleteRefreshToken(RefreshTokenVO refreshTokenVO) {
		return tokenMapper.deleteRefreshToken(refreshTokenVO);
	}
}
