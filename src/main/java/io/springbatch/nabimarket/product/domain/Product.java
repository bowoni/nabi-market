package io.springbatch.nabimarket.product.domain;

import io.springbatch.nabimarket.category.domain.Category;
import io.springbatch.nabimarket.global.common.BaseEntity;
import io.springbatch.nabimarket.region.domain.Region;
import io.springbatch.nabimarket.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "product",
        indexes = {
                @Index(name = "idx_product_region_status_created", columnList = "region_id, status, created_at"),
                @Index(name = "idx_product_category_status_created", columnList = "category_id, status, created_at"),
                @Index(name = "idx_product_seller_status", columnList = "seller_id, status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 20)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "wishlist_count", nullable = false)
    private int wishlistCount;

    @Builder
    private Product(User seller, Category category, Region region, String title,
                    String description, int price, TradeType tradeType) {
        this.seller = seller;
        this.category = category;
        this.region = region;
        this.title = title;
        this.description = description;
        this.price = price;
        this.tradeType = tradeType;
        this.status = ProductStatus.SELLING;
        this.viewCount = 0;
        this.wishlistCount = 0;
    }

    public void update(String title, String description, int price,
                       Category category, TradeType tradeType) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.tradeType = tradeType;
    }

    public void changeStatus(ProductStatus status) {
        this.status = status;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseWishlistCount() {
        this.wishlistCount++;
    }

    public void decreaseWishlistCount() {
        if (this.wishlistCount > 0) this.wishlistCount--;
    }

    public boolean isSeller(User user) {
        return this.seller.getId().equals(user.getId());
    }
}