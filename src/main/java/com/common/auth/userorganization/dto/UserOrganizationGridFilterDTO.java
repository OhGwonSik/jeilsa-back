package com.common.auth.userorganization.dto;

import java.util.List;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "유저-부서 매핑 그리드 필터 조건")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserOrganizationGridFilterDTO {
    @Schema(description = "유저 ID 목록", example = "[\"uuid1\", \"uuid2\"]")
    private List<Integer> userIds;
    
    @Schema(description = "부서 ID 목록", example = "[\"uuid3\", \"uuid4\"]")
    private List<Integer> organizationIds;
    
    @Schema(description = "유저명으로 검색", example = "관리자")
    private String userName;
    
    @Schema(description = "부서명으로 검색", example = "사용자")
    private String organizationName;
    
    @Schema(description = "활성 상태", example = "true")
    private String delYn;
    
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private Integer page = 1;
    
    @Schema(description = "페이지 크기", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    private Integer size = 20;
    
    @Schema(description = "정렬 필드", example = "roleName")
    private String sortBy;
}