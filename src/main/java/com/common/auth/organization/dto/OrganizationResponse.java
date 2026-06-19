package com.common.auth.organization.dto;

import com.common.auth.organization.domain.Organization;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    //----- Fields -----//
    private Integer organizationId;
    private String organizationName;
    private String description;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private String delYn;
    
    public OrganizationResponse(Organization organization) {
        this.organizationId = organization.getOrganizationId();
        this.organizationName = organization.getOrganizationName();
        this.description = organization.getDescription();
        this.regId = organization.getRegId();
        this.regDt = organization.getRegDt();
        this.chgId = organization.getChgId();
        this.chgDt = organization.getChgDt();
        this.delYn = organization.getDelYn();
    }

    public static OrganizationResponse from(Organization organization){
        return new OrganizationResponse(organization);
    }
}