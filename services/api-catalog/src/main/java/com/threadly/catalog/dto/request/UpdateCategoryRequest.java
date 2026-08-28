package com.threadly.catalog.dto.request;

import com.threadly.catalog.entity.CategoryStatus;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest (
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        CategoryStatus status
) {}
