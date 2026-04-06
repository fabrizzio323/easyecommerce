package com.fabrizio.easyecommerce.exception.Cart;

public class CartAlreadyExistsException extends RuntimeException{
    public CartAlreadyExistsException(String message) {
        super(message);
    }
}
