package com.fabrizio.easyecommerce.exception;

import com.fabrizio.easyecommerce.exception.Category.CategoriesNotFoundException;
import com.fabrizio.easyecommerce.exception.Category.CategoryNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.ProductNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.ProductsNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        logger.warn("Validation error occurred: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            logger.debug("Validation error - Field: {}, Message: {}", error.getField(), error.getDefaultMessage());
            errors.put(error.getField(), error.getDefaultMessage());
        });

        logger.info("Returning validation errors: {} fields with errors", errors.size());
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(CategoriesNotFoundException.class)
    public ResponseEntity<String> handleCategoryNotFoundException(CategoriesNotFoundException ex){
        logger.warn("Categories not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<String> handleCategoryNotFoundException(CategoryNotFoundException ex){
        logger.warn("Category not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProductsNotFoundException.class)
    public ResponseEntity<String> handleProductsNotFoundException(ProductsNotFoundException ex){
        logger.warn("Products not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException ex){
        logger.warn("Product not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}