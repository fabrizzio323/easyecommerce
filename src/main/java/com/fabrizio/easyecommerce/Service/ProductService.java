package com.fabrizio.easyecommerce.Service;

import com.fabrizio.easyecommerce.dto.ProductRequestDTO;
import com.fabrizio.easyecommerce.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    List<ProductResponseDTO> getAllProducts();
    ProductResponseDTO getProductById(Long id);
    void creteProduct(ProductRequestDTO request);
    void updateProduct(Long id, ProductResponseDTO request);
    void deleteProduct(Long id);
}
