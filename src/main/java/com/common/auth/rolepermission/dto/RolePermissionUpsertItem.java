package com.common.auth.rolepermission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Schema(description = "역할-권한 매핑 upsert 아이템")
public class RolePermissionUpsertItem {
    
    @Schema(description = "역할 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "역할 ID는 필수입니다")
    private Integer roleId;
    
    @Schema(description = "권한 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
    @NotNull(message = "권한 ID는 필수입니다")
    private Integer permissionId;
    
    @Schema(description = "활성 상태", example = "true")
    private String delYn = "N";
    
    @Schema(description = "상태", required = true, example = "ADDED", allowableValues = {"ADDED", "CHANGED", "DELETED"})
    @NotNull(message = "작업 유형은 필수입니다")
    @Pattern(regexp = "^(ADDED|CHANGED|DELETED)$", message = "작업 유형은 ADDED, CHANGED, DELETED 중 하나여야 합니다")
    private String status;

    @Schema(description = "클라이언트 임시 ID (프론트엔드에서 추적용)", example = "temp_001")
    private String tempId;

    public RolePermissionUpsertItem(Integer roleId, Integer permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
}