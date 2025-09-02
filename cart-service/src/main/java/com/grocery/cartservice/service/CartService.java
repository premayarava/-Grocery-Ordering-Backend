package com.grocery.cartservice.service;

import com.grocery.cartservice.client.ProductClient;
import com.grocery.cartservice.client.ProductResponse;
import com.grocery.cartservice.dto.CartItemRequest;
import com.grocery.cartservice.dto.CartItemResponse;
import com.grocery.cartservice.dto.CartResponse;
import com.grocery.cartservice.model.Cart;
import com.grocery.cartservice.model.CartItem;
import com.grocery.cartservice.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductClient productClient;
    
    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        
        return convertToResponse(cart);
    }
    
    public CartResponse addItemToCart(String userId, CartItemRequest request) {
        // Validate product exists and is active
        ProductResponse product = productClient.getProductById(request.getProductId());
        if (product == null || !product.getIsActive()) {
            throw new RuntimeException("Product not found or inactive");
        }
        
        // Check stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            // Add new item
            CartItem newItem = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getUnit(),
                    request.getQuantity(),
                    product.getPrice()
            );
            cart.addItem(newItem);
        }
        
        Cart savedCart = cartRepository.save(cart);
        return convertToResponse(savedCart);
    }
    
    public CartResponse updateCartItem(String userId, Long productId, CartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        // Validate product exists and is active
        ProductResponse product = productClient.getProductById(productId);
        if (product == null || !product.getIsActive()) {
            throw new RuntimeException("Product not found or inactive");
        }
        
        // Check stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }
        
        // Find and update item
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice()); // Update price in case it changed
        } else {
            throw new RuntimeException("Item not found in cart");
        }
        
        Cart savedCart = cartRepository.save(cart);
        return convertToResponse(savedCart);
    }
    
    public CartResponse removeItemFromCart(String userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        // Find and remove item
        Optional<CartItem> itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        
        if (itemToRemove.isPresent()) {
            cart.removeItem(itemToRemove.get());
            cartRepository.save(cart);
        }
        
        return convertToResponse(cart);
    }
    
    public CartResponse clearCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        cart.clearItems();
        Cart savedCart = cartRepository.save(cart);
        return convertToResponse(savedCart);
    }
    
    private Cart createNewCart(String userId) {
        Cart cart = new Cart(userId);
        return cartRepository.save(cart);
    }
    
    private CartResponse convertToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
        
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                itemResponses,
                cart.getTotalAmount(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
    
    private CartItemResponse convertToItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductUnit(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
