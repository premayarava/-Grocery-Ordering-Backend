package com.grocery.userservice.controller;

import com.grocery.userservice.dto.UserProfileResponse;
import com.grocery.userservice.dto.UserRegistrationRequest;
import com.grocery.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for user registration and profile management")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new user with Firebase authentication")
    public ResponseEntity<UserProfileResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            UserProfileResponse response = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get the profile of the authenticated user")
    public ResponseEntity<UserProfileResponse> getUserProfile(HttpServletRequest request) {
        String firebaseUid = (String) request.getAttribute("firebaseUid");
        if (firebaseUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            UserProfileResponse response = userService.getUserProfile(firebaseUid);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update the profile of the authenticated user")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Valid @RequestBody UserRegistrationRequest request,
            HttpServletRequest httpRequest) {
        String firebaseUid = (String) httpRequest.getAttribute("firebaseUid");
        if (firebaseUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            UserProfileResponse response = userService.updateUserProfile(firebaseUid, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
