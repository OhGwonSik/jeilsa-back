package com.common.auth.userrole.dto;

import java.util.List;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "유저-역할 매핑 그리드 필터 조건")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserRoleGridFilterDTO {
    //----- Fields -----//
        @Schema(description = "유저 ID 목록")
    private List<Integer> memberIds;
    
    @Schema(description = "역할 ID 목록")
    private List<Integer> roleIds;
    
    @Schema(description = "유저명으로 검색", example = "관리자")
    private String name;
    
    @Schema(description = "역할명으로 검색", example = "관리자")
    private String roleName;
    
    @Schema(description = "활성 상태", example = "true")
    private String delYn;

    @Schema(description = "부모 활성 상태", example = "true")
    private Boolean parentIsActive;
    
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private Integer page = 1;
    
    @Schema(description = "페이지 크기", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    private Integer size = 20;
    
    @Schema(description = "정렬 필드", example = "roleName")
    private String sortBy;
}