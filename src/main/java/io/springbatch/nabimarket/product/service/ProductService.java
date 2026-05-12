package io.springbatch.nabimarket.product.service;

import io.springbatch.nabimarket.category.domain.Category;
import io.springbatch.nabimarket.category.repository.CategoryRepository;
import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import io.springbatch.nabimarket.product.domain.Product;
import io.springbatch.nabimarket.product.domain.TradeType;
import io.springbatch.nabimarket.product.dto.CreateProductRequest;
import io.springbatch.nabimarket.product.dto.ProductResponse;
import io.springbatch.nabimarket.product.repository.ProductRepository;
import io.springbatch.nabimarket.user.domain.User;
import io.springbatch.nabimarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse create(Long sellerId, CreateProductRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 동네 인증 필수 - region이 null이거나 미인증이면 등록 거부
        if (!seller.isRegionVerified() || seller.getRegion() == null) {
            throw new BusinessException(ErrorCode.REGION_NOT_VERIFIED);
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 가격 정책 - GIVEAWAY는 서버가 0 강제, SALE은 1원 이상
        int price = resolvePrice(request.tradeType(), request.price());

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .region(seller.getRegion())  // seller의 region 자동 복사
                .title(request.title())
                .description(request.description())
                .price(price)
                .tradeType(request.tradeType())
                .build();

        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    // 가격 결정 로직 - GIVEAWAY/SALE 분기 + SALE 시 최소 가격 검증
    private int resolvePrice(TradeType tradeType, int requestedPrice) {
        if (tradeType == TradeType.GIVEAWAY) {
            return 0;  // 사용자가 무엇을 보냈든 무료나눔은 0
        }
        if (requestedPrice < 1) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }
        return requestedPrice;
    }

}
