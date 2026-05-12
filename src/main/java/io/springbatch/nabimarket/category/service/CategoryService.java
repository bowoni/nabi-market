package io.springbatch.nabimarket.category.service;

import io.springbatch.nabimarket.category.dto.CategoryResponse;
import io.springbatch.nabimarket.category.repository.CategoryRepository;
import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryResponse> getChildren(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(parentId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
