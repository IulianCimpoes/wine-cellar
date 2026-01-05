package com.example.winecellar.common.api;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        PageMeta page
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                new PageMeta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );
    }
}
