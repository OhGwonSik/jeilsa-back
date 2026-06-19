package com.common.auth.rolemenu.dto;



import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 롤-메뉴 upsert 항목
 */
@Getter
@Setter
@ToString
@Schema(description = "롤-메뉴 upsert 항목")
public class RoleMenuUpsertItem {
    @Schema(description = "임시 ID (클라이언트에서 생성)")
    private String tempId;
    
    @Schema(description = "작업 상태 (ADDED/CHANGED/DELETED)")
    @NotBlank(message = "작업 상태는 필수입니다")
    private String status;
    
    @Schema(description = "역할 ID")
    @NotNull(message = "역할 ID는 필수입니다")
    private Integer roleId;
    
    @Schema(description = "역할명")
    private String roleName;
    
    @Schema(description = "메뉴 ID")
    @NotNull(message = "메뉴 ID는 필수입니다")
    private Integer menuId;
    
    @Schema(description = "메뉴명")
    private String menuName;    
    
    @Schema(description = "활성 상태")
    private String delYn = "N";

    public String getUniqueKey() {
        return roleId + ":" + menuId;
    }
}
