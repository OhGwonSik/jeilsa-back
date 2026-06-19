package com.common.auth.rolepermission.dto;

import java.util.List;

import com.common.auth.common.dto.OperationOptions;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "역할-권한 매핑 일괄 upsert 요청")
public class RolePermissionGridUpsertRequest {
    
    @Schema(description = "처리할 아이템 목록", required = true)
    @NotEmpty(message = "처리할 아이템이 최소 1개 이상 필요합니다")
    @Valid
    private List<RolePermissionUpsertItem> items;
    
    @Schema(description = "일괄 처리 옵션")
    @Valid
    private OperationOptions options;
    
    public RolePermissionGridUpsertRequest() {
        this.options = new OperationOptions();
    }
    
    public List<RolePermissionUpsertItem> getItems() {
        return items;
    }
    
    public void setItems(List<RolePermissionUpsertItem> items) {
        this.items = items;
    }
    
    public OperationOptions getOptions() {
        return options;
    }
    
    public void setOptions(OperationOptions options) {
        this.options = options;
    }
    
    @Override
    public String toString() {
        return "RolePermissionGridUpsertRequest{" +
                "items=" + items +
                ", options=" + options +
                '}';
    }
}