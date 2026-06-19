package com.common.auth.userorganization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@Schema(description = "사용자-조직 매핑 upsert 아이템")
public class UserOrganizationUpsertItem {
    
    @Schema(description = "사용자 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "사용자 ID는 필수입니다")
    private Integer memberId;
    
    @Schema(description = "조직 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
    @NotNull(message = "조직 ID는 필수입니다")
    private Integer organizationId;
    
    @Schema(description = "활성 상태", example = "true")
    private String delYn = "N";
    
    @Schema(description = "상태", required = true, example = "ADDED", allowableValues = {"ADDED", "CHANGED", "DELETED"})
    @NotNull(message = "작업 유형은 필수입니다")
    @Pattern(regexp = "^(ADDED|CHANGED|DELETED)$", message = "작업 유형은 ADDED, CHANGED, DELETED 중 하나여야 합니다")
    private String status;
    

    @Schema(description = "클라이언트 임시 ID (프론트엔드에서 추적용)", example = "temp_001")
    private String tempId;
    
    public UserOrganizationUpsertItem() {}
    
    public UserOrganizationUpsertItem(Integer memberId, Integer organizationId) {
        this.memberId = memberId;
        this.organizationId = organizationId;
    }
}