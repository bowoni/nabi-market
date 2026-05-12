package io.springbatch.nabimarket.category.dto;

import io.springbatch.nabimarket.category.domain.Category;

public record CategoryResponse(
        Long id,
        String name,
        int displayOrder
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDisplayOrder()
        );
    }
}
