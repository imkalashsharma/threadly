package com.threadly.catalog.service;

import com.threadly.catalog.dto.request.CreateCategoryRequest;
import com.threadly.catalog.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);
}
