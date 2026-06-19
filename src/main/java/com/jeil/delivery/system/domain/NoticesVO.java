package com.jeil.delivery.system.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NoticesVO {

	private Integer noticeId;            // 공지사항 ID
	private String title;                // 제목
	private String content;              // 내용
    private Integer regId;               // 등록자 ID
    private LocalDateTime regDt;         // 등록 일시
    private Integer chgId;               // 변경자 ID
    private LocalDateTime chgDt;         // 변경 일시
    private String delYn;                // 삭제 여부
    private String limited;              // 조회시 갯수 제한 여부

    private String regNm;                // 등록자 NAME
}
