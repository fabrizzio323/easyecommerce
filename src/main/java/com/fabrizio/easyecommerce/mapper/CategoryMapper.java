package com.fabrizio.easyecommerce.mapper;

import com.fabrizio.easyecommerce.dto.CategoryRequestDTO;
import com.fabrizio.easyecommerce.dto.CategoryResponseDTO;
import com.fabrizio.easyecommerce.entity.Category;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    @Mapping(target="name", ignore=true)
    @Mapping(target = "description", ignore = true)
    CategoryResponseDTO toCategoryResponseDTO(Category category);

    @InheritConfiguration
    Category toCategory(CategoryRequestDTO categoryRequestDTO);
    List<Category> toCategoryList(List<CategoryResponseDTO> categoryResponseDTOs);
    List<CategoryResponseDTO> toCategoryResponseDTOList(List<Category> categories);
}
