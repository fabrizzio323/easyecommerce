package com.fabrizio.easyecommerce.mapper;

import com.fabrizio.easyecommerce.dto.CategoryRequestDTO;
import com.fabrizio.easyecommerce.dto.CategoryResponseDTO;
import com.fabrizio.easyecommerce.entity.Category;
import com.fabrizio.easyecommerce.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    @Mapping(source="products", target="nameProducts")
    CategoryResponseDTO toResponseDTO(Category category);

    List<CategoryResponseDTO> toCategoryResponseDTOList(List<Category> categories);


    @Mapping(target = "id", ignore = true)
    Category toCategory(CategoryRequestDTO categoryRequestDTO);

    List<Category> toEntityList(List<CategoryRequestDTO> categoryRequestDTOList);

    default String mapProductToString(Product product) {
        return (product != null) ? product.getName() : null;
    }
}