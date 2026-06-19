package com.common.auth.auth.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RefreshToken {
    //----- Fields -----//
    private String refreshToken;
    private Integer memberId;
    private LocalDateTime expireDt;
    private String delYn;
    private InetAddress ipAddress;
    private String userAgent;
    private LocalDateTime regDt;
    private Integer regId;
    private LocalDateTime chgDt;
    private Integer chgId;
    private LocalDateTime delDt;
    private Integer delId;

    //----- Constructors -----//
    public RefreshToken(String refreshToken, Integer memberId, LocalDateTime expireDt) {
        this.refreshToken = refreshToken;
        this.memberId = memberId;
        this.expireDt = expireDt;
        this.delYn = "N";
        this.regDt = LocalDateTime.now();
    }

    public RefreshToken(String refreshToken, Integer memberId, LocalDateTime expireDt, InetAddress ipAddress, String userAgent) {
        this.refreshToken = refreshToken;
        this.memberId = memberId;
        this.expireDt = expireDt;
        this.delYn = "N";
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.regDt = LocalDateTime.now();
    }

    //----- Methods -----//
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireDt);
    }

    public boolean isValid() {
        return delYn.equals("N") && !isExpired();
    }
}