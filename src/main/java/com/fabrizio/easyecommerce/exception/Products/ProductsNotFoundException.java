package com.fabrizio.easyecommerce.exception.Products;

public class ProductsNotFoundException extends RuntimeException{
    public ProductsNotFoundException(String message) {
        super(message);
    }
}
