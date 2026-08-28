package com.threadly.catalog.service.impl;

import com.threadly.catalog.dto.request.CreateCategoryRequest;
import com.threadly.catalog.dto.response.CategoryResponse;
import com.threadly.catalog.entity.Category;
import com.threadly.catalog.entity.CategoryStatus;
import com.threadly.catalog.exception.DuplicateCategoryException;
import com.threadly.catalog.repository.CategoryRepository;
import com.threadly.catalog.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        boolean alreadyExists = categoryRepository.existsByName(request.name());

        if(alreadyExists){
            log.info("Category already exists: {}",  request.name());

            throw new DuplicateCategoryException("Category already exists.");
        }

        // create new category
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setStatus(CategoryStatus.ACTIVE);  // default

        // save
        category = categoryRepository.save(category);

        log.info("Category created successfully. Id: {}", category.getId());

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getStatus(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Category not found with id: {}", id);
                    return new RuntimeException("Category not found with id " + id);
                });

        log.info("Category found with id: {}", id);

        return new CategoryResponse(
                existingCategory.getId(),
                existingCategory.getName(),
                existingCategory.getDescription(),
                existingCategory.getStatus(),
                existingCategory.getCreatedAt(),
                existingCategory.getUpdatedAt()
        );
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.getStatus(),
                        category.getCreatedAt(),
                        category.getUpdatedAt()
                ))
                .toList();
    }
}
