package com.common.auth.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Schema(description = "메뉴 그리드 필터 조건")
@Getter
@Setter
@ToString
public class MenuGridFilterDTO {
    //----- Fields -----//
    @Schema(description = "메뉴 ID 목록")
    private List<Integer> menuIds;
    
    @Schema(description = "메뉴 이름 (부분 검색)")
    private String menuName;
    
    @Schema(description = "메뉴 타입")
    private String menuType;
    
    @Schema(description = "메뉴 경로 (부분 검색)")
    private String menuPath;
    
    @Schema(description = "상위 메뉴 ID")
    private Integer parentId;
    
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