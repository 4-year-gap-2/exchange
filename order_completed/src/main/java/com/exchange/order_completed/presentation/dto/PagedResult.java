package com.exchange.order_completed.presentation.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PagedResult<T> {
    private List<T> items;
    private LocalDateTime nextCursor;
    private boolean hasNext;

    public PagedResult(List<T> items, LocalDateTime nextCursor, boolean hasNext) {
        this.items = items;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}

