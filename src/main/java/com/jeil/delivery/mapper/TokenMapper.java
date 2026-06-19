package com.jeil.delivery.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.domain.RefreshTokenVO;

@Mapper
public interface TokenMapper {
    public int insertRefreshToken(RefreshTokenVO token);
    public int deleteRefreshTokenById(RefreshTokenVO token);
    public int deleteRefreshToken(RefreshTokenVO token);
    public RefreshTokenVO getRefreshTokenByUser(String refreshToken);
    public RefreshTokenVO getRefreshTokenByMemberId(MemberDTO memberDTO);
}
