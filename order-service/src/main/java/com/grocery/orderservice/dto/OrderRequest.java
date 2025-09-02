package com.grocery.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public class OrderRequest {
    
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    // Constructors
    public OrderRequest() {}
    
    public OrderRequest(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    // Getters and Setters
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
