package com.common.auth.userorganization.dto;

import java.util.List;

import com.common.auth.common.dto.OperationOptions;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "유저-부서 매핑 일괄 upsert 요청")
public class UserOrganizationGridUpsertRequest {
    @Schema(description = "처리할 아이템 목록", required = true)
    @NotEmpty(message = "처리할 아이템이 최소 1개 이상 필요합니다")
    @Valid
    private List<UserOrganizationUpsertItem> items;
    
    @Schema(description = "일괄 처리 옵션")
    @Valid
    private OperationOptions options;
}