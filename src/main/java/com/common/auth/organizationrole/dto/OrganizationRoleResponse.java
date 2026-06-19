package com.common.auth.organizationrole.dto;


import com.common.auth.organizationrole.domain.OrganizationRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 부서-역할 매핑 응답 DTO
 */
@Getter
@Setter
@ToString
@Schema(description = "부서-역할 매핑 응답 DTO")
public class OrganizationRoleResponse {
    //----- Fields -----//
    @Schema(description = "조직 ID")
    private Integer organizationId;
    
    @Schema(description = "조직명")
    private String organizationName;
    
    @Schema(description = "역할 ID")
    private Integer roleId;
    
    @Schema(description = "역할명")
    private String roleName;
    
    @Schema(description = "활성 상태")
    private String delYn;
    
    @Schema(description = "생성자 ID")
    private Integer regId;
    
    @Schema(description = "생성일시")
    private LocalDateTime regDt;
    
    @Schema(description = "수정자 ID")
    private Integer chgId;

    @Schema(description = "수정일시")
    private LocalDateTime chgDt;

    @Schema(description = "삭제자 ID")
    private Integer delId;
    
    @Schema(description = "삭제일시")
    private LocalDateTime delDt;

    public OrganizationRoleResponse(OrganizationRole organizationRole) {
        this.organizationId = organizationRole.getOrganizationId();
        this.roleId = organizationRole.getRoleId();
        this.delYn = organizationRole.getDelYn();
        this.regId = organizationRole.getRegId();
        this.regDt = organizationRole.getRegDt();
        this.chgId = organizationRole.getChgId();
        this.chgDt = organizationRole.getChgDt();
        this.delId = organizationRole.getDelId();
        this.delDt = organizationRole.getDelDt();
    }
}
