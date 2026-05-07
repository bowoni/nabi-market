package io.springbatch.nabimarket.chat.domain;

import io.springbatch.nabimarket.global.common.BaseEntity;
import io.springbatch.nabimarket.product.domain.Product;
import io.springbatch.nabimarket.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_room", columnNames = {"product_id", "buyer_id"})
        },
        indexes = {
                @Index(name = "idx_chat_room_buyer_last", columnList = "buyer_id, last_message_at"),
                @Index(name = "idx_chat_room_seller_last", columnList = "seller_id, last_message_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Builder
    private ChatRoom(Product product, User buyer, User seller) {
        this.product = product;
        this.buyer = buyer;
        this.seller = seller;
    }

    public void updateLastMessageAt(LocalDateTime time) {
        this.lastMessageAt = time;
    }
}
