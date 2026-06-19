package com.common.auth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "페이징 요청 DTO")
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {
    //----- Fields-----//
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private int pageNum = 1;
    
    @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    private int pageSize = 10;
    
    @Schema(description = "정렬 조건 (예: 'created_at desc')", example = "created_at desc")
    private String orderBy;
        
    public PageRequestDTO(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
