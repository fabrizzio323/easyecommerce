package com.fabrizio.easyecommerce.Service.product;
import lombok.RequiredArgsConstructor;

import com.fabrizio.easyecommerce.dto.product.ProductRequestDTO;
import com.fabrizio.easyecommerce.dto.product.ProductResponseDTO;
import com.fabrizio.easyecommerce.entity.Category;
import com.fabrizio.easyecommerce.entity.Product;
import com.fabrizio.easyecommerce.exception.Category.CategoryNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.ProductNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.ProductsNotFoundException;
import com.fabrizio.easyecommerce.mapper.product.ProductMapper;
import com.fabrizio.easyecommerce.repository.category.CategoryRepository;
import com.fabrizio.easyecommerce.repository.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        logger.info("Retrieving active products with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> productPage = productRepository.findByActive(true, pageable);
        
        if (productPage.isEmpty()) {
            logger.warn("No active products found for requested page");
            throw new ProductsNotFoundException("No products found");
        }
        
        return productPage.map(productMapper::toProductResponseDTO);
    }

    @Override
    public Page<ProductResponseDTO> getFilteredProducts(
            Long categoryId, String search, Pageable pageable) {
        final String term = (search == null || search.isBlank())
                ? null : search.trim().toLowerCase(Locale.ROOT);

        Specification<Product> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (term != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + term + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(specification, pageable)
                .map(productMapper::toProductResponseDTO);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        logger.info("Retrieving product by ID: {}", id);
        Product product = productRepository.findByIdAndActive(id,true).orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
        logger.info("Product found: {} - {}", id, product.getName());
        return productMapper.toProductResponseDTO(product);
    }

    @Override
    public void creteProduct(ProductRequestDTO request) {
        logger.info("Creating new product: {}", request.getName());
        try {
            Product product = productMapper.toProduct(request);
            logger.debug("Product mapped from DTO: {}", product.getName());

            Category category = categoryRepository.findById(request.getIdCategory()).orElseThrow(() -> new CategoryNotFoundException("Category with id " + request.getIdCategory() + " not found"));
            logger.debug("Category found: {} - {}", category.getId(), category.getName());

            product.setCategory(category);
            product.setActive(true);
            productRepository.save(product);
            logger.info("Product created successfully: {}", request.getName());
        } catch (Exception e) {
            logger.error("Failed to create product: {}", request.getName(), e);
            throw e;
        }
    }

    @Override
    public void updateProduct(Long id, ProductRequestDTO request) {
        logger.info("Updating product ID: {} with name: {}", id, request.getName());
        try {
            Product productUpdated = productRepository.findByIdAndActive(id,true).orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
            logger.debug("Product found for update: {} - {}", id, productUpdated.getName());

            productUpdated.setName(request.getName());
            productUpdated.setDescription(request.getDescription());
            productUpdated.setPrice(request.getPrice());
            productUpdated.setStock(request.getStock());

            Category category = categoryRepository.findById(request.getIdCategory()).orElseThrow(() -> new CategoryNotFoundException("Category with id " + request.getIdCategory() + " not found"));
            logger.debug("New category found: {} - {}", category.getId(), category.getName());

            productUpdated.setCategory(category);
            productRepository.save(productUpdated);
            logger.info("Product updated successfully: ID {}", id);
        } catch (Exception e) {
            logger.error("Failed to update product ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public void deleteProduct(Long id) {
        logger.info("Soft deleting product ID: {}", id);
        try {
            Product productDeleted = productRepository.findByIdAndActive(id,true).orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
            logger.debug("Product found for deletion: {} - {}", id, productDeleted.getName());

            productDeleted.setActive(false);
            productRepository.save(productDeleted);
            logger.info("Product soft deleted successfully: ID {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete product ID: {}", id, e);
            throw e;
        }
    }
}
