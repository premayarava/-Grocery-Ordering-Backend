package com.grocery.orderservice.dto;

import java.math.BigDecimal;

public class OrderItemResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productUnit;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    
    // Constructors
    public OrderItemResponse() {}
    
    public OrderItemResponse(Long id, Long productId, String productName, String productUnit, 
                           Integer quantity, BigDecimal price) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productUnit = productUnit;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductUnit() {
        return productUnit;
    }
    
    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (this.price != null) {
            this.totalPrice = this.price.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
        if (this.quantity != null) {
            this.totalPrice = price.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
