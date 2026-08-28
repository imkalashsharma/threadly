package com.threadly.catalog.service;

import com.threadly.catalog.dto.request.CreateCategoryRequest;
import com.threadly.catalog.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse getCategoryById(UUID id);

    List<CategoryResponse> getAllCategories();
}
