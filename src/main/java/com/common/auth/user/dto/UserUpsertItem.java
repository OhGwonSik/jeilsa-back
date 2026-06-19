package com.common.auth.user.dto;



import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 upsert 항목")
public class UserUpsertItem {
    @Schema(description = "임시 ID (클라이언트에서 생성)")
    private String tempId;
    
    @Schema(description = "작업 상태 (ADDED/CHANGED/DELETED)")
    @NotBlank(message = "작업 상태는 필수입니다")
    private String status;
    
    @Schema(description = "사용자 ID (수정/삭제 시 필수)")
    private Integer memberId;
    
    @Schema(description = "사용자명")
    private String userName;
    
    @Schema(description = "이메일")
    private String email;
    
    @Schema(description = "비밀번호")
    private String password;
    
    @Schema(description = "비밀번호 해시")
    private String passwordHash;
    
    @Schema(description = "전화번호")
    private String telNo;
    
    @Schema(description = "사용자 상태 코드")
    private String userStatusCd;
    
    @Schema(description = "활성 상태")
    private String delYn = "N";

    @Override
    public String toString() {
        return "UserUpsertItem{" +
                "tempId='" + tempId + '\'' +
                ", status='" + status + '\'' +
                ", memberId=" + memberId +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", telNo='" + telNo + '\'' +
                ", userStatusCd='" + userStatusCd + '\'' +
                ", delYn=" + delYn +
                '}';
    }
}