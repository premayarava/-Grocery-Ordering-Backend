package com.grocery.cartservice.controller;

import com.grocery.cartservice.dto.CartItemRequest;
import com.grocery.cartservice.dto.CartResponse;
import com.grocery.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart", description = "APIs for shopping cart management")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping
    @Operation(summary = "Get user's cart", description = "Retrieve the current user's shopping cart")
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            CartResponse cart = cartService.getCart(userId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product to the user's shopping cart")
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody CartItemRequest request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("firebaseUid");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            CartResponse cart = cartService.addItemToCart(userId, request);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/items/{productId}")
    @Operation(summary = "Update cart item", description = "Update the quantity of an item in the cart")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long productId,
            @Valid @RequestBody CartItemRequest request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("firebaseUid");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            CartResponse cart = cartService.updateCartItem(userId, productId, request);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Remove a product from the user's shopping cart")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable Long productId,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("firebaseUid");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            CartResponse cart = cartService.removeItemFromCart(userId, productId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the user's shopping cart")
    public ResponseEntity<CartResponse> clearCart(HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("firebaseUid");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            CartResponse cart = cartService.clearCart(userId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
