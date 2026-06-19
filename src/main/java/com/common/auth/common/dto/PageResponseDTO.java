package com.common.auth.common.dto;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "페이징 응답 DTO")
public class PageResponseDTO<T> {
    //----- Fields -----//
    @Schema(description = "데이터 목록")
    private List<T> list;
    
    @Schema(description = "전체 데이터 수", example = "100")
    private long total;
    
    @Schema(description = "현재 페이지 번호", example = "1")
    private int pageNum;
    
    @Schema(description = "페이지 크기", example = "10")
    private int pageSize;
    
    @Schema(description = "전체 페이지 수", example = "10")
    private int pages;
    
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNextPage;
    
    @Schema(description = "이전 페이지 존재 여부", example = "false")
    private boolean hasPreviousPage;
        
    public PageResponseDTO(PageInfo<T> pageInfo) {
        this.list = pageInfo.getList();
        this.total = pageInfo.getTotal();
        this.pageNum = pageInfo.getPageNum();
        this.pageSize = pageInfo.getPageSize();
        this.pages = pageInfo.getPages();
        this.hasNextPage = pageInfo.isHasNextPage();
        this.hasPreviousPage = pageInfo.isHasPreviousPage();
    }
    
    public static <T> PageResponseDTO<T> of(PageInfo<T> pageInfo) {
        return new PageResponseDTO<>(pageInfo);
    }
}
