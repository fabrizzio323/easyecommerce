package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.Service.CategoryService;
import com.fabrizio.easyecommerce.dto.CategoryRequestDTO;
import com.fabrizio.easyecommerce.dto.CategoryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(
        name = "Category Resource",
        description = """
    Endpoints for managing product categories.

    🔐 Authentication required:
    You must be logged in and provide a valid JWT token.

    📌 How to use:
    1. Login at /api/auth/login
    2. Copy the JWT token
    3. Click 'Authorize' in Swagger and paste: Bearer <your_token>

    ⚠️ Permissions:
    - READ_ALL_CATEGORIES → view categories
    - SAVE_ONE_CATEGORY → create category
    - UPDATE_ONE_CATEGORY → update category
    - DELETE_ONE_CATEGORY → delete category
    """
)
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = "Retrieve all categories. Requires READ_ALL_CATEGORIES permission",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No categories found"),
            @ApiResponse(responseCode = "403", description = "Access denied (missing permissions)")
    })
    @PreAuthorize("hasAuthority('READ_ALL_CATEGORIES')")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(){
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getAllCategories());
    }

    @Operation(
            summary = "Create category",
            description = "Create a new category. Requires SAVE_ONE_CATEGORY permission",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('SAVE_ONE_CATEGORY')")
    @PostMapping
    public ResponseEntity<String> saveOneCategory(@Valid @RequestBody CategoryRequestDTO request){
        categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Category created successfully");
    }

    @Operation(
            summary = "Delete category",
            description = "Delete a category by ID. Requires DELETE_ONE_CATEGORY permission",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('DELETE_ONE_CATEGORY')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Update category",
            description = "Update a category by name. Requires UPDATE_ONE_CATEGORY permission",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('UPDATE_ONE_CATEGORY')")
    @PutMapping("/{name}")
    public ResponseEntity<String> updateOneCategory(@PathVariable String name,
                                                    @Valid @RequestBody CategoryRequestDTO request){
        categoryService.updateCategory(name, request);
        return ResponseEntity.status(HttpStatus.OK).body("Category updated successfully");
    }
}