package com.fabrizio.easyecommerce.mapper;


import com.fabrizio.easyecommerce.dto.ProductRequestDTO;
import com.fabrizio.easyecommerce.dto.ProductResponseDTO;
import com.fabrizio.easyecommerce.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface ProductMapper {

    @Mapping(source="category.name", target="categoryName")
    ProductResponseDTO toProductResponseDTO(Product product);

    List<ProductResponseDTO> toProductResponseDTOList(List<Product> products);

    @Mapping(target="id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target="category", ignore = true)
    @Mapping(target="active", ignore = true)
    Product toProduct(ProductRequestDTO productRequestDTO);

    List<Product> toProductList(List<ProductRequestDTO> productRequestDTOList);


}
