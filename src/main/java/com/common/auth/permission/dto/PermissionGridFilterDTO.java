package com.common.auth.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Schema(description = "권한 그리드 필터 조건")
@Getter
@Setter
@ToString
public class PermissionGridFilterDTO {
    //----- Fields -----//
    @Schema(description = "권한 ID 목록")
    private List<Integer> permissionIds;   
    
    @Schema(description = "권한 이름 (부분 검색)")
    private String permissionName;
    
    @Schema(description = "설명 (부분 검색)")
    private String description;
    
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