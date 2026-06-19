package com.common.auth.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class TokenStatistics {
    private final int totalTokens;
    private final int validTokens;
    private final int expiredTokens;
    private final int revokedTokens;
}