package com.fabrizio.easyecommerce.controller;


import com.fabrizio.easyecommerce.Service.ProductService;
import com.fabrizio.easyecommerce.dto.ProductRequestDTO;
import com.fabrizio.easyecommerce.dto.ProductResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(
        name = "Product Resource",
        description = """
    Endpoints for managing products in the e-commerce system.

    🔐 Authentication required:
    You must be logged in and provide a valid JWT token.

    📌 How to use:
    1. Login at /api/auth/login
    2. Copy the JWT token
    3. Click 'Authorize' in Swagger and paste: Bearer <your_token>

    ⚠️ Permissions:
    - READ_ALL_PRODUCTS → view all products
    - READ_ONE_PRODUCT → view single product
    - SAVE_ONE_PRODUCT → create product (ADMIN only)
    - UPDATE_ONE_PRODUCT → update product (ADMIN only)
    - DELETE_ONE_PRODUCT → delete product (ADMIN only)

    📦 Product data includes:
    - Basic info (name, description, price, stock)
    - Category relationship
    - Active status (soft delete)
    """
)
public class ProductController {
    @Autowired
    private ProductService productService;

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Operation(
            summary = "Get all products",
            description = """
        Retrieve all active products with their category information.
        Requires READ_ALL_PRODUCTS permission.

        📋 Returns:
        - Product ID, name, description
        - Price and stock information
        - Category name
        - Creation date
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No products found"),
            @ApiResponse(responseCode = "403", description = "Access denied (missing READ_ALL_PRODUCTS permission)")
    })
    @PreAuthorize("hasAuthority('READ_ALL_PRODUCTS')")
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(){
        logger.info("Request to get all products");
        try {
            List<ProductResponseDTO> products = productService.getAllProducts();
            logger.info("Retrieved {} products successfully", products.size());
            return ResponseEntity.status(HttpStatus.OK).body(products);
        } catch (Exception e) {
            logger.error("Failed to retrieve products", e);
            throw e;
        }
    }

    @Operation(
            summary = "Get product by ID",
            description = """
        Retrieve a specific product by its ID.
        Requires READ_ONE_PRODUCT permission.

        📋 Returns:
        - Complete product information
        - Category details
        - Stock and pricing info
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Access denied (missing READ_ONE_PRODUCT permission)")
    })
    @PreAuthorize("hasAuthority('READ_ONE_PRODUCT')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@Valid @PathVariable Long id){
        logger.info("Request to get product by ID: {}", id);
        try {
            ProductResponseDTO product = productService.getProductById(id);
            logger.info("Product retrieved successfully: {} - {}", id, product.getName());
            return ResponseEntity.status(HttpStatus.OK).body(product);
        } catch (Exception e) {
            logger.error("Failed to retrieve product with ID: {}", id, e);
            throw e;
        }
    }

    @Operation(
            summary = "Create new product",
            description = """
        Create a new product in the system.
        Requires SAVE_ONE_PRODUCT permission (ADMIN only).

        📝 Required fields:
        - name: Product name (not blank)
        - description: Product description (not blank)
        - price: Positive decimal value (not null)
        - stock: Non-negative integer (minimum 0)
        - idCategory: Valid category ID (not null)

        ⚠️ Only administrators can create products.
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data (validation errors)"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN permission required)")
    })
    @PreAuthorize("hasAuthority('SAVE_ONE_PRODUCT')")
    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductRequestDTO request){
        logger.info("Attempting to create product: {}", request.getName());
        try {
            productService.creteProduct(request);
            logger.info("Product created successfully: {}", request.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body("Product created successfully");
        } catch (Exception e) {
            logger.error("Failed to create product: {}", request.getName(), e);
            throw e;
        }
    }

    @Operation(
            summary = "Update existing product",
            description = """
        Update an existing product by its ID.
        Requires UPDATE_ONE_PRODUCT permission (ADMIN only).

        📝 Updatable fields:
        - name: Product name
        - description: Product description
        - price: Product price (must be positive)
        - stock: Available stock (minimum 0)
        - idCategory: Category ID (must exist)

        ⚠️ Only administrators can update products.
        Product must exist and be active.
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Product or category not found"),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN permission required)")
    })
    @PreAuthorize("hasAuthority('UPDATE_ONE_PRODUCT')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@Valid @PathVariable Long id, @Valid @RequestBody ProductRequestDTO request){
        logger.info("Attempting to update product ID: {} with name: {}", id, request.getName());
        try {
            productService.updateProduct(id, request);
            logger.info("Product updated successfully: ID {}", id);
            return ResponseEntity.status(HttpStatus.OK).body("Product updated successfully");
        } catch (Exception e) {
            logger.error("Failed to update product ID: {}", id, e);
            throw e;
        }
    }

    @Operation(
            summary = "Delete product (soft delete)",
            description = """
        Soft delete a product by setting active = false.
        Requires DELETE_ONE_PRODUCT permission (ADMIN only).

        ⚠️ Important notes:
        - This performs a SOFT DELETE (product becomes inactive)
        - Product data is preserved for audit purposes
        - Product won't appear in regular listings
        - Only administrators can delete products
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully (soft delete)"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN permission required)")
    })
    @PreAuthorize("hasAuthority('DELETE_ONE_PRODUCT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@Valid @PathVariable Long id){
        logger.info("Attempting to delete product ID: {}", id);
        try {
            productService.deleteProduct(id);
            logger.info("Product soft deleted successfully: ID {}", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Failed to delete product ID: {}", id, e);
            throw e;
        }
    }
}
