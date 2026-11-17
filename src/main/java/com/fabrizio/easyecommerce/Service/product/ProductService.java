package com.fabrizio.easyecommerce.Service.product;

import com.fabrizio.easyecommerce.dto.product.ProductRequestDTO;
import com.fabrizio.easyecommerce.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponseDTO> getAllProducts(Pageable pageable);
    Page<ProductResponseDTO> getFilteredProducts(Long categoryId, String search, Pageable pageable);
    ProductResponseDTO getProductById(Long id);
    void creteProduct(ProductRequestDTO request);
    void updateProduct(Long id, ProductRequestDTO request);
    void deleteProduct(Long id);
}
