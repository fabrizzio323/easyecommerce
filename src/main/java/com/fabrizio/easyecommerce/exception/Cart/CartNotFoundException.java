package com.fabrizio.easyecommerce.exception.Cart;

public class CartNotFoundException extends RuntimeException{
    public CartNotFoundException(String message) {
        super(message);
    }
}
