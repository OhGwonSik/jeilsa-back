package com.common.auth.rolepermission.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "역할-권한 매핑 그리드 필터 조건")
@Getter
@Setter
@ToString
public class RolePermissionGridFilterDTO {
    @Schema(description = "역할 ID 목록", example = "[\"uuid1\", \"uuid2\"]")
    private List<Integer> roleIds;
    
    @Schema(description = "권한 ID 목록", example = "[\"uuid3\", \"uuid4\"]")
    private List<Integer> permissionIds;
    
    @Schema(description = "역할명으로 검색", example = "관리자")
    private String roleName;
    
    @Schema(description = "권한명으로 검색", example = "사용자")
    private String permissionName;
    
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