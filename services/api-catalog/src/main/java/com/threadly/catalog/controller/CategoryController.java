package com.threadly.catalog.controller;

import com.threadly.catalog.dto.request.CreateCategoryRequest;
import com.threadly.catalog.dto.request.UpdateCategoryRequest;
import com.threadly.catalog.dto.response.CategoryResponse;
import com.threadly.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryResponse categoryResponse = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryResponse);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategory() {
        List<CategoryResponse> categoryResponse = categoryService.getAllCategories();

        return ResponseEntity.ok(categoryResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        CategoryResponse categoryResponse = categoryService.getCategoryById(id);

        return ResponseEntity.ok(categoryResponse);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request){
        CategoryResponse updatedCategoryResponse = categoryService.updateCategory(id, request);

        return ResponseEntity
                .ok(updatedCategoryResponse);
    }

    @DeleteMapping("/deactivate/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id){
        categoryService.deactivateCategory(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<Void> activateCategory(@PathVariable UUID id){
        categoryService.activateCategory(id);

        return ResponseEntity.noContent().build();
    }
}
