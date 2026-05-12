package io.springbatch.nabimarket.product.repository;

import io.springbatch.nabimarket.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
