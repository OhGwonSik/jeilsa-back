package com.jeil.delivery.billing.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// PagedResult.java
@Getter
@AllArgsConstructor
public class PagedResult<T> {
    private final List<T> items;
    private final long total;
}
