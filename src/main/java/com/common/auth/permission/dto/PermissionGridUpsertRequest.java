package com.common.auth.permission.dto;

import java.util.List;

import com.common.auth.common.dto.OperationOptions;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "권한 그리드 일괄 upsert 요청")
@Getter
@Setter
@ToString
public class PermissionGridUpsertRequest {
    //----- Field -----//
    @Schema(description = "upsert 할 메뉴 항목 목록")
    @NotNull(message = "메뉴 항목 목록은 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 항목이 필요합니다")
    @Valid
    private List<PermissionUpsertItem> items;
    
    @Schema(description = "일괄 처리 옵션")
    @Valid
    private OperationOptions options;
}