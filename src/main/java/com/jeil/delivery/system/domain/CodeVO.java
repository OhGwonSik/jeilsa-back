package com.jeil.delivery.system.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CodeVO {

	private int codeId;             // 코드 ID (PK, auto increment)
    private String codeVal;         // 코드 값
    private String codeNm;          // 코드 명
    private String parntsCodeVal;   // 부모 코드 값 (nullable)
    private String type;            // 타입

    private Integer regId;          // 등록자 ID
    private LocalDateTime regDt;    // 등록 일시
    private Integer chgId;          // 변경자 ID
    private LocalDateTime chgDt;    // 변경 일시
    private String delYn;           // 삭제 여부

}
