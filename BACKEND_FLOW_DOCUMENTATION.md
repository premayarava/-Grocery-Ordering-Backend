# Grocery Ordering Platform - Backend Flow Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Complete User Journey Example](#complete-user-journey-example)
3. [Service Communication Flow](#service-communication-flow)
4. [Authentication & Security Flow](#authentication--security-flow)
5. [Database Operations Flow](#database-operations-flow)
6. [Error Handling & Resilience](#error-handling--resilience)
7. [API Endpoints & Data Flow](#api-endpoints--data-flow)

---

## System Overview

The grocery ordering platform follows a **microservices architecture** with 6 core services:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Config Server   │    │   User Service  │
│   (Port: 8080)  │    │   (Port: 8888)   │    │   (Port: 8081)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Product Service │    │   Cart Service   │    │  Order Service  │
│  (Port: 8082)   │    │   (Port: 8083)   │    │   (Port: 8084)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

---

## Complete User Journey Example

### 🛒 **Scenario: Thousif's Grocery Shopping Experience**

Let's follow Thousif through a complete shopping experience:

#### **Step 1: User Registration & Authentication**

**Frontend Request:**
```javascript
// Flutter app sends registration request
const registrationData = {
  "firstName": "Thousif",
  "lastName": "Ahmed", 
  "email": "thousif.ahmed@email.com",
  "phoneNumber": "+919876543210",
  "role": "CONSUMER"
};
```

**Backend Flow:**
```
1. Flutter App → API Gateway (8080)
   POST /api/users/register
   Authorization: Bearer <firebase-jwt-token>

2. API Gateway → User Service (8081)
   Routes based on path: /api/users/**

3. User Service Processing:
   ├── FirebaseAuthenticationFilter validates JWT
   ├── Extracts firebaseUid from token
   ├── UserController receives request
   ├── UserService creates user profile
   ├── Saves to postgres-user database
   └── Returns UserProfileResponse
```

**Database Operation:**
```sql
-- User Service Database (postgres-user)
INSERT INTO users (firebase_uid, first_name, last_name, email, phone_number, role, created_at)
VALUES ('firebase_uid_123', 'Thousif', 'Ahmed', 'thousif.ahmed@email.com', '+919876543210', 'CONSUMER', NOW());
```

#### **Step 2: Browsing Products**

**Frontend Request:**
```javascript
// Flutter app requests product catalog
GET /api/products?category=FRUITS&page=0&size=10
```

**Backend Flow:**
```
1. Flutter App → API Gateway (8080)
   GET /api/products?category=FRUITS

2. API Gateway → Product Catalog Service (8082)
   Routes based on path: /api/products/**

3. Product Service Processing:
   ├── ProductController receives request
   ├── ProductService queries database
   ├── Applies filters (category=FRUITS)
   ├── Returns paginated results
   └── Sends ProductResponse list
```

**Database Query:**
```sql
-- Product Service Database (postgres-product)
SELECT * FROM products 
WHERE category = 'FRUITS' 
AND is_active = true 
ORDER BY name 
LIMIT 10 OFFSET 0;
```

**Response Example:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Fresh Apples",
      "description": "Organic red apples",
      "price": 2.99,
      "unit": "KG",
      "stockQuantity": 50,
      "category": "FRUITS",
      "imageUrl": "https://example.com/apples.jpg"
    },
    {
      "id": 2,
      "name": "Bananas",
      "description": "Yellow ripe bananas",
      "price": 1.49,
      "unit": "KG", 
      "stockQuantity": 30,
      "category": "FRUITS",
      "imageUrl": "https://example.com/bananas.jpg"
    }
  ],
  "totalElements": 15,
  "totalPages": 2,
  "currentPage": 0
}
```

#### **Step 3: Adding Items to Cart**

**Frontend Request:**
```javascript
// Flutter app adds apples to cart
const cartItem = {
  "productId": 1,
  "quantity": 2
};

POST /api/cart/items
Authorization: Bearer <firebase-jwt-token>
Body: cartItem
```

**Backend Flow:**
```
1. Flutter App → API Gateway (8080)
   POST /api/cart/items

2. API Gateway → Cart Service (8083)
   Routes based on path: /api/cart/**

3. Cart Service Processing:
   ├── FirebaseAuthenticationFilter validates JWT
   ├── Extracts firebaseUid: "firebase_uid_123"
   ├── CartController receives request
   ├── CartService.addItemToCart() called
   │   ├── Calls ProductClient.getProductById(1)
   │   ├── Product Service returns product details
   │   ├── Validates stock availability
   │   ├── Creates/updates cart in database
   │   └── Calculates total amount
   └── Returns updated CartResponse
```

**Inter-Service Communication:**
```java
// Cart Service calls Product Service via Feign Client
@FeignClient(name = "product-catalog-service", url = "${service.product.url}")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);
}

// Product Service responds with:
{
  "id": 1,
  "name": "Fresh Apples",
  "price": 2.99,
  "unit": "KG",
  "stockQuantity": 50
}
```

**Database Operations:**
```sql
-- Cart Service Database (postgres-cart)
-- First, check if cart exists
SELECT * FROM cart WHERE user_id = 'firebase_uid_123';

-- If cart doesn't exist, create it
INSERT INTO cart (user_id, total_amount, created_at) 
VALUES ('firebase_uid_123', 0.00, NOW());

-- Add cart item
INSERT INTO cart_items (cart_user_id, product_id, product_name, product_unit, quantity, price)
VALUES ('firebase_uid_123', 1, 'Fresh Apples', 'KG', 2, 2.99);

-- Update cart total
UPDATE cart SET total_amount = 5.98 WHERE user_id = 'firebase_uid_123';
```

**Response:**
```json
{
  "userId": "firebase_uid_123",
  "items": [
    {
      "productId": 1,
      "productName": "Fresh Apples",
      "productUnit": "KG",
      "quantity": 2,
      "price": 2.99,
      "totalPrice": 5.98
    }
  ],
  "totalAmount": 5.98,
  "itemCount": 1
}
```

#### **Step 4: Adding More Items & Viewing Cart**

**Frontend Request:**
```javascript
// Add bananas to cart
POST /api/cart/items
{
  "productId": 2,
  "quantity": 1.5
}

// View cart
GET /api/cart
```

**Backend Flow:**
```
1. Cart Service processes banana addition
2. Calls Product Service to validate product
3. Updates cart with new item
4. Returns complete cart with both items
```

**Final Cart State:**
```json
{
  "userId": "firebase_uid_123",
  "items": [
    {
      "productId": 1,
      "productName": "Fresh Apples",
      "productUnit": "KG",
      "quantity": 2,
      "price": 2.99,
      "totalPrice": 5.98
    },
    {
      "productId": 2,
      "productName": "Bananas",
      "productUnit": "KG", 
      "quantity": 1.5,
      "price": 1.49,
      "totalPrice": 2.235
    }
  ],
  "totalAmount": 8.215,
  "itemCount": 2
}
```

#### **Step 5: Placing an Order**

**Frontend Request:**
```javascript
// Place order from cart
const orderRequest = {
  "shippingAddress": {
    "street": "456 Tech Park Road",
    "city": "Bangalore",
    "state": "Karnataka",
    "zipCode": "560001",
    "country": "India"
  },
  "paymentMethod": "CREDIT_CARD"
};

POST /api/orders
Authorization: Bearer <firebase-jwt-token>
Body: orderRequest
```

**Backend Flow:**
```
1. Flutter App → API Gateway (8080)
   POST /api/orders

2. API Gateway → Order Service (8084)
   Routes based on path: /api/orders/**

3. Order Service Processing:
   ├── FirebaseAuthenticationFilter validates JWT
   ├── Extracts firebaseUid: "firebase_uid_123"
   ├── OrderController receives request
   ├── OrderService.placeOrder() called
   │   ├── Calls CartClient.getCart(authorization)
   │   ├── Cart Service returns cart contents
   │   ├── Validates cart is not empty
   │   ├── Creates order in database
   │   ├── Creates order items
   │   ├── Sets initial status: PENDING
   │   └── Returns OrderResponse
   └── Order placed successfully
```

**Inter-Service Communication:**
```java
// Order Service calls Cart Service
@FeignClient(name = "cart-service", url = "${service.cart.url}")
public interface CartClient {
    @GetMapping("/api/cart")
    CartResponse getCart(@RequestHeader("Authorization") String authorization);
}

// Cart Service returns current cart contents
```

**Database Operations:**
```sql
-- Order Service Database (postgres-order)
-- Create order
INSERT INTO orders (user_id, status, total_amount, shipping_address, created_at)
VALUES (
  'firebase_uid_123', 
  'PENDING', 
  8.215, 
  '{"street":"456 Tech Park Road","city":"Bangalore","state":"Karnataka","zipCode":"560001","country":"India"}',
  NOW()
);

-- Create order items
INSERT INTO order_items (order_id, product_id, product_name, product_unit, quantity, price)
VALUES 
  (1, 1, 'Fresh Apples', 'KG', 2, 2.99),
  (1, 2, 'Bananas', 'KG', 1.5, 1.49);
```

**Response:**
```json
{
  "id": 1,
  "userId": "firebase_uid_123",
  "status": "PENDING",
  "totalAmount": 8.215,
  "shippingAddress": {
    "street": "456 Tech Park Road",
    "city": "Bangalore", 
    "state": "Karnataka",
    "zipCode": "560001",
    "country": "India"
  },
  "items": [
    {
      "productId": 1,
      "productName": "Fresh Apples",
      "productUnit": "KG",
      "quantity": 2,
      "price": 2.99,
      "totalPrice": 5.98
    },
    {
      "productId": 2,
      "productName": "Bananas",
      "productUnit": "KG",
      "quantity": 1.5,
      "price": 1.49,
      "totalPrice": 2.235
    }
  ],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

## Service Communication Flow

### **Synchronous Communication Pattern**

```mermaid
sequenceDiagram
    participant Client as Flutter App
    participant Gateway as API Gateway
    participant Cart as Cart Service
    participant Product as Product Service
    participant Order as Order Service
    
    Client->>Gateway: POST /api/cart/items
    Gateway->>Cart: Route to Cart Service
    Cart->>Product: GET /api/products/{id}
    Product->>Cart: Product details
    Cart->>Cart: Update cart
    Cart->>Gateway: Cart response
    Gateway->>Client: Updated cart
```

### **Service Dependencies**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Cart Service│───▶│Product Svc  │    │Order Service│
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
                    ┌─────────────┐
                    │User Service │
                    └─────────────┘
```

---

## Authentication & Security Flow

### **JWT Token Validation Process**

```java
// Every request goes through this filter
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Validate with Firebase
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                
                // Attach user info to request
                request.setAttribute("firebaseUid", decodedToken.getUid());
                request.setAttribute("firebaseEmail", decodedToken.getEmail());
            } catch (Exception e) {
                // Token invalid - will be handled by controller
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### **Request Flow with Authentication**

```
1. Client Request:
   Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...

2. API Gateway:
   ├── Receives request
   ├── Routes to appropriate service
   └── Forwards Authorization header

3. Service (e.g., Cart Service):
   ├── FirebaseAuthenticationFilter processes token
   ├── Validates with Firebase servers
   ├── Extracts firebaseUid: "firebase_uid_123"
   ├── Attaches to request attributes
   └── Controller accesses via request.getAttribute("firebaseUid")

4. Business Logic:
   ├── Uses firebaseUid for user identification
   ├── Performs authorization checks
   └── Returns user-specific data
```

---

## Database Operations Flow

### **Data Isolation Pattern**

Each service maintains its own database:

```sql
-- User Service Database (postgres-user)
CREATE TABLE users (
    firebase_uid VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20),
    role VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Product Service Database (postgres-product)
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10,2),
    unit VARCHAR(20),
    stock_quantity INTEGER,
    category VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Cart Service Database (postgres-cart)
CREATE TABLE cart (
    user_id VARCHAR(255) PRIMARY KEY,
    total_amount DECIMAL(10,2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_user_id VARCHAR(255),
    product_id BIGINT,
    product_name VARCHAR(255),
    product_unit VARCHAR(20),
    quantity DECIMAL(10,2),
    price DECIMAL(10,2),
    FOREIGN KEY (cart_user_id) REFERENCES cart(user_id)
);

-- Order Service Database (postgres-order)
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255),
    status VARCHAR(20),
    total_amount DECIMAL(10,2),
    shipping_address JSONB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    product_id BIGINT,
    product_name VARCHAR(255),
    product_unit VARCHAR(20),
    quantity DECIMAL(10,2),
    price DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

### **Transaction Management**

```java
// Example: Cart Service transaction
@Transactional
public CartResponse addItemToCart(String userId, CartItemRequest request) {
    // 1. Validate product exists and has stock
    ProductResponse product = productClient.getProductById(request.getProductId());
    
    // 2. Get or create cart
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> createNewCart(userId));
    
    // 3. Add or update cart item
    CartItem cartItem = findOrCreateCartItem(cart, request.getProductId());
    cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
    cartItem.setPrice(product.getPrice());
    
    // 4. Update cart total
    cart.calculateTotal();
    cartRepository.save(cart);
    
    return mapToCartResponse(cart);
}
```

---

## Error Handling & Resilience

### **Error Response Pattern**

```java
// Controller error handling
@PostMapping("/items")
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
    } catch (ProductNotFoundException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("Product not found", e.getMessage()));
    } catch (InsufficientStockException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("Insufficient stock", e.getMessage()));
    } catch (RuntimeException e) {
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("Internal server error", "Please try again later"));
    }
}
```

### **Service Communication Resilience**

```java
// Feign Client with fallback
@FeignClient(name = "product-catalog-service", 
             url = "${service.product.url}",
             fallback = ProductClientFallback.class)
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);
}

// Fallback implementation
@Component
public class ProductClientFallback implements ProductClient {
    @Override
    public ProductResponse getProductById(Long id) {
        // Return cached data or throw exception
        throw new ServiceUnavailableException("Product service unavailable");
    }
}
```

---

## API Endpoints & Data Flow

### **Complete API Flow Summary**

| Service | Endpoint | Method | Purpose | Data Flow |
|---------|----------|--------|---------|-----------|
| User | `/api/users/register` | POST | User registration | Firebase JWT → User Profile |
| User | `/api/users/profile` | GET | Get profile | Firebase UID → User Data |
| Product | `/api/products` | GET | List products | Filters → Paginated Results |
| Product | `/api/products/{id}` | GET | Product details | Product ID → Product Info |
| Cart | `/api/cart` | GET | View cart | Firebase UID → Cart Contents |
| Cart | `/api/cart/items` | POST | Add item | Product ID + Qty → Updated Cart |
| Order | `/api/orders` | POST | Place order | Cart + Address → Order |
| Order | `/api/orders/{id}` | GET | Order details | Order ID → Order Info |

### **Request/Response Examples**

**Product Search:**
```http
GET /api/products?category=FRUITS&search=apple&page=0&size=10
```

**Add to Cart:**
```http
POST /api/cart/items
Authorization: Bearer <firebase-jwt>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2.5
}
```

**Place Order:**
```http
POST /api/orders
Authorization: Bearer <firebase-jwt>
Content-Type: application/json

{
  "shippingAddress": {
    "street": "456 Tech Park Road",
    "city": "Bangalore",
    "state": "Karnataka",
    "zipCode": "560001"
  },
  "paymentMethod": "CREDIT_CARD"
}
```

---

## Configuration Management Flow

### **Config Server Integration**

```yaml
# Service connects to Config Server on startup
spring:
  cloud:
    config:
      uri: http://config-server:8888
      fail-fast: true

# Config Server serves service-specific configs
# config-server/src/main/resources/config/cart-service.yml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:postgresql://postgres-cart:5432/cart_service_db
    username: postgres
    password: password

service:
  product:
    url: http://product-catalog-service:8082
```

### **Environment-Specific Configuration**

```yaml
# Development
spring:
  profiles: dev
  datasource:
    url: jdbc:postgresql://localhost:5433/cart_service_db

# Production  
spring:
  profiles: prod
  datasource:
    url: jdbc:postgresql://prod-db:5432/cart_service_db

# Docker
spring:
  profiles: docker
  datasource:
    url: jdbc:postgresql://postgres-cart:5432/cart_service_db
```

---

## Monitoring & Observability

### **Health Checks**

```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 419430400000,
        "threshold": 10485760
      }
    }
  }
}
```

### **Metrics & Monitoring**

```http
GET /actuator/metrics/http.server.requests
```

**Response:**
```json
{
  "name": "http.server.requests",
  "description": "Number of HTTP server requests",
  "baseUnit": "requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1250.0
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 45.2
    }
  ]
}
```

---

## Summary

This microservices architecture provides:

✅ **Scalability** - Each service can be scaled independently  
✅ **Maintainability** - Clear service boundaries and responsibilities  
✅ **Fault Tolerance** - Service failures are isolated  
✅ **Technology Flexibility** - Services can use different technologies  
✅ **Security** - Firebase authentication with JWT tokens  
✅ **Observability** - Health checks, metrics, and logging  
✅ **Configuration Management** - Centralized configuration via Config Server  

The complete flow demonstrates how a user can seamlessly browse products, manage their cart, and place orders while the backend services communicate efficiently to provide a smooth shopping experience.
