package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.Service.CategoryService;
import com.fabrizio.easyecommerce.dto.CategoryRequestDTO;
import com.fabrizio.easyecommerce.dto.CategoryResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PreAuthorize("hasAuthority('READ_ALL_CATEGORIES')")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(){
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getAllCategories());
    }

    @PreAuthorize("hasAuthority('SAVE_ONE_CATEGORY')")
    @PostMapping
    public ResponseEntity<String> saveOneCategory(@Valid @RequestBody CategoryRequestDTO request){
        categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Category created successfully");
    }

    @PreAuthorize("hasAuthority('DELETE_ONE_CATEGORY')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasAuthority('UPDATE_ONE_CATEGORY')")
    @PutMapping("/{name}")
    public ResponseEntity<String> updateOneCategory(@PathVariable String name, @Valid @RequestBody CategoryRequestDTO request){
        categoryService.updateCategory(name, request);
        return ResponseEntity.status(HttpStatus.OK).body("Category updated successfully");
    }
}
