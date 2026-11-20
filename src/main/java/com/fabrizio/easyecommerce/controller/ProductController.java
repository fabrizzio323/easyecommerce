package com.fabrizio.easyecommerce.controller;
import lombok.RequiredArgsConstructor;


import com.fabrizio.easyecommerce.Service.product.ProductService;
import com.fabrizio.easyecommerce.dto.product.ProductRequestDTO;
import com.fabrizio.easyecommerce.dto.product.ProductResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

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
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    
@Operation(
    summary = "Get products with pagination and filters",
    description = """
        Retrieve active products with pagination.

        Optional filters:
        - categoryId: Filter by category ID
        - search: Search products by name
        """,
    security = @SecurityRequirement(name = "bearerAuth")
)
@PreAuthorize("hasAuthority('READ_ALL_PRODUCTS')")
@GetMapping
public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String search,
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "name"
        ) Pageable pageable
) {
    logger.info(
        "Getting products: categoryId={}, search={}, page={}",
        categoryId,
        search,
        pageable.getPageNumber()
    );

    Page<ProductResponseDTO> products =
        productService.getFilteredProducts(
            categoryId,
            search,
            pageable
        );

    return ResponseEntity.ok(products);
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
