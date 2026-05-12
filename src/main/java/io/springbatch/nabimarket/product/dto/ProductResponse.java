package io.springbatch.nabimarket.product.dto;

import io.springbatch.nabimarket.product.domain.Product;
import io.springbatch.nabimarket.product.domain.ProductStatus;
import io.springbatch.nabimarket.product.domain.TradeType;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String title,
        String description,
        int price,
        TradeType tradeType,
        ProductStatus status,
        Long categoryId,
        String categoryName,
        String regionFullName,
        Long sellerId,
        String sellerNickname,
        int viewCount,
        int wishlistCount,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getTradeType(),
                product.getStatus(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getRegion().getFullName(),
                product.getSeller().getId(),
                product.getSeller().getNickname(),
                product.getViewCount(),
                product.getWishlistCount(),
                product.getCreatedAt()
        );
    }
}
