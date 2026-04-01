package com.fabrizio.easyecommerce.Service;

import com.fabrizio.easyecommerce.dto.ProductRequestDTO;
import com.fabrizio.easyecommerce.dto.ProductResponseDTO;
import com.fabrizio.easyecommerce.entity.Category;
import com.fabrizio.easyecommerce.entity.Product;
import com.fabrizio.easyecommerce.exception.Category.CategoryNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.ProductNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.ProductsNotFoundException;
import com.fabrizio.easyecommerce.mapper.ProductMapper;
import com.fabrizio.easyecommerce.repository.CategoryRepository;
import com.fabrizio.easyecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        logger.info("Retrieving all active products");
        List<Product> products = productRepository.findByActive(true);
        if(products.isEmpty()){
            logger.warn("No active products found in database");
            throw new ProductsNotFoundException("No products found");
        }
        logger.info("Found {} active products", products.size());
        return productMapper.toProductResponseDTOList(products);
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
