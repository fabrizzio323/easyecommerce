package com.fabrizio.easyecommerce.Service;


import com.fabrizio.easyecommerce.dto.CategoryRequestDTO;
import com.fabrizio.easyecommerce.dto.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> getAllCategories();
    void createCategory(CategoryRequestDTO categoryRequestDTO);
    void deleteCategory(Long id);
    void updateCategory(String name, CategoryRequestDTO categoryRequestDTO);
}
