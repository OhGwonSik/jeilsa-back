package com.common.auth.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class PkConditionDTO {
    private String columnName;
    private Object value;
    private String columnType;
}


