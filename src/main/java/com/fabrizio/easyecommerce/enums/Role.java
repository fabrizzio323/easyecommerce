package com.fabrizio.easyecommerce.enums;

import java.util.Arrays;
import java.util.List;


public enum Role {

    USER(Arrays.asList(
            Permission.READ_ALL_PRODUCTS,
            Permission.READ_ONE_PRODUCT,
            Permission.READ_ALL_CATEGORIES,
            Permission.READ_ONE_CART,
            Permission.SAVE_ONE_CART,
            Permission.ADD_ITEM_CART,
            Permission.DELETE_ITEM_CART,
            Permission.UPDATE_QUANTITY_ITEM_CART,
            Permission.CLEAR_CART,
            Permission.PERFORM_CHECKOUT,
            Permission.READ_MY_ORDERS)),

    ADMINISTRATOR(Arrays.asList(
            Permission.READ_ALL_PRODUCTS,
            Permission.READ_ONE_PRODUCT,
            Permission.SAVE_ONE_PRODUCT,
            Permission.UPDATE_ONE_PRODUCT,
            Permission.DELETE_ONE_PRODUCT,
            Permission.READ_ALL_CATEGORIES,
            Permission.SAVE_ONE_CATEGORY,
            Permission.DELETE_ONE_CATEGORY,
            Permission.UPDATE_ONE_CATEGORY,
            Permission.READ_ONE_CART,
            Permission.SAVE_ONE_CART,
            Permission.ADD_ITEM_CART,
            Permission.UPDATE_QUANTITY_ITEM_CART,
            Permission.DELETE_ITEM_CART,
            Permission.CLEAR_CART,
            Permission.PERFORM_CHECKOUT,
            Permission.READ_ALL_ORDERS,
            Permission.READ_MY_ORDERS,
            Permission.UPDATE_ORDER_STATUS
    ));


    private List<Permission> permissions;

    Role(List<Permission> permissions){
        this.permissions = permissions;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

}
