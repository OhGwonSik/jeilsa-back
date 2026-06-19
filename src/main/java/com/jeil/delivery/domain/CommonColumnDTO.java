package com.jeil.delivery.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonColumnDTO {
	// 추후 DTO 만들떄 상속 받아서 사용
    private Integer regId;          // 등록자 ID
    private LocalDateTime regDt;    // 등록 일시
    private Integer chgId;          // 수정자 ID
    private LocalDateTime chgDt;    // 수정 일시
    private String delYn;           // 삭제 여부
}
