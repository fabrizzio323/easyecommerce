package com.fabrizio.easyecommerce.exception.Cart;

public class CartEmptyException extends RuntimeException {
    public CartEmptyException(String message) {
        super(message);
    }
}
