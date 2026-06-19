package com.common.auth.organizationrole.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Schema(description = "부서-역할 그리드 검색 필터")
public class OrganizationRoleGridFilterDTO {
    //----- Fields -----//
    @Schema(description = "조직 ID")
    private List<Integer> organizationIds;
    
    @Schema(description = "조직명")
    private String organizationName;
    
    @Schema(description = "역할 ID")
    private List<Integer> roleIds;
    
    @Schema(description = "역할명")
    private String roleName;
    
    @Schema(description = "활성 상태")
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
