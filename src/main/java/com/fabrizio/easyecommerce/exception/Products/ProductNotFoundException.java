package com.fabrizio.easyecommerce.exception.Products;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(String message) {
        super(message);
    }
}
