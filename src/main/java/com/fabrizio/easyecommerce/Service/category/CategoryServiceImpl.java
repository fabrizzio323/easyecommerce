package com.fabrizio.easyecommerce.Service.category;
import lombok.RequiredArgsConstructor;


import com.fabrizio.easyecommerce.dto.category.CategoryRequestDTO;
import com.fabrizio.easyecommerce.dto.category.CategoryResponseDTO;
import com.fabrizio.easyecommerce.entity.Category;
import com.fabrizio.easyecommerce.exception.Category.CategoriesNotFoundException;
import com.fabrizio.easyecommerce.exception.Category.CategoryNotFoundException;
import com.fabrizio.easyecommerce.mapper.Category.CategoryMapper;
import com.fabrizio.easyecommerce.repository.category.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if(categories.isEmpty()){
            throw new CategoriesNotFoundException("No categories found");
        }
        return categoryMapper.toCategoryResponseDTOList(categories);
    }

    @Override
    public void createCategory(CategoryRequestDTO categoryRequestDTO) {
     Category category = categoryMapper.toCategory(categoryRequestDTO);
     categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        if(categoryRepository.existsById(id)){
            categoryRepository.deleteById(id);
        }else{
            throw new CategoryNotFoundException("Category with id " + id + " not found");
        }
    }

    @Override
    public void updateCategory(String name, CategoryRequestDTO categoryRequestDTO) {
        Category categoryUpdated = categoryRepository.findByName(name).orElseThrow(() -> new CategoryNotFoundException("Category with name " + name + " not found"));
        categoryUpdated.setName(categoryRequestDTO.getName());
        categoryUpdated.setDescription(categoryRequestDTO.getDescription());
        categoryRepository.save(categoryUpdated);
    }
}
