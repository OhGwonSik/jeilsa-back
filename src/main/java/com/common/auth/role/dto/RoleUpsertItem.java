 package com.common.auth.role.dto;



import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "역할 upsert 아이템")
@NoArgsConstructor
public class RoleUpsertItem {
    @Schema(description = "역할 ID (UPDATE/DELETE 시 필수)", example = "550e8400-e29b-41d4-a716-446655440000")
    private Integer roleId;
    
    @Schema(description = "역할명", required = true, example = "시스템관리자")
    @Size(max = 100, message = "역할명은 100자 이하여야 합니다")
    private String roleName;
    
    @Schema(description = "설명", example = "시스템 전체 관리 권한")
    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;
    
    @Schema(description = "기본 역할 여부", example = "false")
    private Boolean isDefault = false;
    
    @Schema(description = "활성 상태", example = "true")
    private String delYn = "N";
    
    @Schema(description = "작업 유형", required = true, example = "ADDED", allowableValues = {"ADDED", "CHANGED", "DELETED"})
    @NotNull(message = "작업 유형은 필수입니다")
    @Pattern(regexp = "^(ADDED|CHANGED|DELETED)$", message = "작업 유형은 ADDED, CHANGED, DELETED 중 하나여야 합니다")
    private String status;
    
    @Schema(description = "클라이언트 임시 ID (프론트엔드에서 추적용)", example = "temp_role_001")
    private String tempId;
        
    public RoleUpsertItem(String roleName, String description, String status) {
        this.roleName = roleName;
        this.description = description;
        this.status = status;
    }
}