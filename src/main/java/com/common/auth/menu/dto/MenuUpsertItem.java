package com.common.auth.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "메뉴 upsert 항목")
public class MenuUpsertItem {
    //----- Fields -----//
    @Schema(description = "임시 ID (클라이언트에서 생성)")
    private String tempId;
    
    @Schema(description = "작업 상태 (ADDED/CHANGED/DELETED)")
    @NotBlank(message = "작업 상태는 필수입니다")
    private String status;
    
    @Schema(description = "메뉴 ID (수정/삭제 시 필수)")
    private Integer menuId;
    
    @Schema(description = "메뉴 이름")
    private String menuName;
    
    @Schema(description = "메뉴 타입")
    private String menuType;
    
    @Schema(description = "설명")
    private String description;
    
    @Schema(description = "상위 메뉴 ID")
    private Integer parentId;
    
    @Schema(description = "메뉴 경로")
    private String menuPath;
    
    @Schema(description = "메뉴 순서")
    private Integer menuOrder;
    
    @Schema(description = "활성 상태")
    private String delYn = "N";
}