package com.grocery.userservice;

import com.grocery.userservice.dto.UserProfileResponse;
import com.grocery.userservice.dto.UserRegistrationRequest;
import com.grocery.userservice.model.UserRole;
import com.grocery.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("user_service_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Test
    void testUserRegistration() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirebaseUid("test-user-123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@test.com");
        request.setPhoneNumber("+1234567890");
        request.setRole("CONSUMER");

        // When
        UserProfileResponse response = userService.registerUser(request);

        // Then
        assertNotNull(response);
        assertEquals("test-user-123", response.getFirebaseUid());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@test.com", response.getEmail());
        assertEquals("+1234567890", response.getPhoneNumber());
        assertEquals(UserRole.CONSUMER, response.getRole());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void testGetUserProfile() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirebaseUid("test-user-456");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@test.com");
        request.setRole("CONSUMER");

        userService.registerUser(request);

        // When
        UserProfileResponse response = userService.getUserProfile("test-user-456");

        // Then
        assertNotNull(response);
        assertEquals("test-user-456", response.getFirebaseUid());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("jane.smith@test.com", response.getEmail());
    }

    @Test
    void testUpdateUserProfile() {
        // Given
        UserRegistrationRequest createRequest = new UserRegistrationRequest();
        createRequest.setFirebaseUid("test-user-789");
        createRequest.setFirstName("Bob");
        createRequest.setLastName("Johnson");
        createRequest.setEmail("bob.johnson@test.com");
        createRequest.setRole("CONSUMER");

        userService.registerUser(createRequest);

        UserRegistrationRequest updateRequest = new UserRegistrationRequest();
        updateRequest.setFirstName("Robert");
        updateRequest.setLastName("Johnson");
        updateRequest.setPhoneNumber("+9876543210");

        // When
        UserProfileResponse response = userService.updateUserProfile("test-user-789", updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("test-user-789", response.getFirebaseUid());
        assertEquals("Robert", response.getFirstName());
        assertEquals("Johnson", response.getLastName());
        assertEquals("+9876543210", response.getPhoneNumber());
    }
}

