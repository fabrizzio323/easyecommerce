package com.fabrizio.easyecommerce.exception.Category;

public class CategoriesNotFoundException extends RuntimeException {
    public CategoriesNotFoundException(String message) {
        super(message);
    }
}
