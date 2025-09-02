# Grocery Ordering Platform - Microservices Backend

A microservices-based grocery ordering platform built with Spring Boot, featuring user authentication, product catalog, shopping cart, and order management.

## Architecture

The platform consists of the following microservices:

1. **User Service** - User authentication and profile management
2. **Product Catalog Service** - Grocery item catalog management
3. **Cart Service** - Shopping cart functionality
4. **Order Service** - Order management and processing
5. **API Gateway** - Centralized routing and security
6. **Config Server** - Centralized configuration management

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Authentication**: Firebase Authentication
- **Security**: Spring Security with JWT
- **Service Communication**: REST APIs with Feign Client
- **API Documentation**: Swagger/OpenAPI
- **Containerization**: Docker & Docker Compose
- **Testing**: Testcontainers for integration tests
- **Configuration**: Spring Cloud Config Server

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- PostgreSQL (if running locally)
- Firebase project with Authentication enabled

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd grocery-ordering-platform
   ```

2. **Set up Firebase Configuration**
   - Create a Firebase project
   - Enable Authentication
   - Download the service account key JSON file
   - Place it in the `config-server/src/main/resources` directory

3. **Start the services using Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Access the services**
   - API Gateway: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Config Server: http://localhost:8888

## Service Endpoints

### User Service (Port: 8081)
- `POST /api/users/register` - Register new user
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile

### Product Catalog Service (Port: 8082)
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `GET /api/products/search` - Search products
- `POST /api/products` - Add new product (Admin only)
- `PUT /api/products/{id}` - Update product (Admin only)

### Cart Service (Port: 8083)
- `GET /api/cart` - Get user's cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{productId}` - Update cart item
- `DELETE /api/cart/items/{productId}` - Remove item from cart
- `DELETE /api/cart` - Clear cart

### Order Service (Port: 8084)
- `POST /api/orders` - Place new order
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order details
- `PUT /api/orders/{id}/status` - Update order status (Admin only)

## Authentication

The platform uses Firebase Authentication for user management. All protected endpoints require a valid Firebase JWT token in the Authorization header:

```
Authorization: Bearer <firebase-jwt-token>
```

## Database Schema

Each service has its own PostgreSQL database:

- **user_service_db** - User profiles and authentication data
- **product_catalog_db** - Product information and inventory
- **cart_service_db** - Shopping cart data
- **order_service_db** - Order and order item data

## Testing

Run integration tests using Testcontainers:

```bash
# Run all tests
mvn clean test

# Run specific service tests
mvn test -pl user-service
mvn test -pl product-catalog-service
mvn test -pl cart-service
mvn test -pl order-service
```

## Development

### Building Individual Services

```bash
# User Service
cd user-service && mvn clean install

# Product Catalog Service
cd product-catalog-service && mvn clean install

# Cart Service
cd cart-service && mvn clean install

# Order Service
cd order-service && mvn clean install
```

### Running Services Locally

1. Start PostgreSQL and other dependencies:
   ```bash
   docker-compose up postgres redis -d
   ```

2. Start Config Server:
   ```bash
   cd config-server && mvn spring-boot:run
   ```

3. Start individual services:
   ```bash
   cd user-service && mvn spring-boot:run
   cd product-catalog-service && mvn spring-boot:run
   cd cart-service && mvn spring-boot:run
   cd order-service && mvn spring-boot:run
   ```

## API Documentation

Once the services are running, you can access the Swagger documentation at:
- http://localhost:8080/swagger-ui.html (via API Gateway)

## Monitoring and Logging

- Application logs are available in Docker containers
- Health check endpoints: `/actuator/health`
- Metrics endpoints: `/actuator/metrics`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License.
