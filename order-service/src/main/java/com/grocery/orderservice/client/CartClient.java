package com.grocery.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service", url = "${service.cart.url}")
public interface CartClient {
    
    @GetMapping("/api/cart")
    CartResponse getCart(@RequestHeader("Authorization") String authorization);
}
