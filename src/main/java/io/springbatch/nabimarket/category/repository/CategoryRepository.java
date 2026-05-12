package io.springbatch.nabimarket.category.repository;

import io.springbatch.nabimarket.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNullAndActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIdAndActiveTrueOrderByDisplayOrderAsc(Long parentId);

}
