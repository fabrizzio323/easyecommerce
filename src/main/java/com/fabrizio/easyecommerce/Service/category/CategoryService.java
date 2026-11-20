package com.fabrizio.easyecommerce.Service.category;


import com.fabrizio.easyecommerce.dto.category.CategoryRequestDTO;
import com.fabrizio.easyecommerce.dto.category.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> getAllCategories();
    void createCategory(CategoryRequestDTO categoryRequestDTO);
    void deleteCategory(Long id);
    void updateCategory(String name, CategoryRequestDTO categoryRequestDTO);
}
