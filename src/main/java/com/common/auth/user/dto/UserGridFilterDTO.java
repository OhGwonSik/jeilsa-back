package com.common.auth.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Schema(description = "사용자 그리드 필터 조건")
@Getter
@Setter
@ToString
public class UserGridFilterDTO {
    
    @Schema(description = "사용자 ID 목록")
    private List<Integer> memberIds;
    
    @Schema(description = "사용자명 (부분 검색)")
    private String name;
    
    @Schema(description = "이메일 (부분 검색)")
    private String email;
    
    @Schema(description = "전화번호 (부분 검색)")
    private String telNo;
    
    @Schema(description = "사용자 상태 코드")
    private String userStatusCd;
    
    @Schema(description = "활성 상태")
    private String delYn;
    
    @Schema(description = "페이지 번호", example = "1")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private Integer page = 1;
    
    @Schema(description = "페이지 크기", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    private Integer size = 20;
    
    @Schema(description = "정렬 기준 필드")
    private String sortBy;
}