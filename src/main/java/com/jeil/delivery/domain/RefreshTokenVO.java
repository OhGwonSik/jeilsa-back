package com.jeil.delivery.domain;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class RefreshTokenVO {
    private int refreshTokensId;
    private int memberId;
    private String refreshToken;
    private Date expireDt;

    private String userId;
}
