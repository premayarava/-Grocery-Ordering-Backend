# Grocery Ordering Platform - Setup Instructions

## Prerequisites

1. **Java 17 or higher**
2. **Docker and Docker Compose**
3. **Maven 3.6+**
4. **Firebase Project with Authentication enabled**

## Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Enable Authentication (Email/Password)
4. Go to Project Settings > Service Accounts
5. Generate a new private key (JSON file)
6. Place the JSON file in `config-server/src/main/resources/firebase-service-account.json`
7. Update the Firebase project ID in all service configurations

## Quick Start with Docker Compose

### 1. Build and Start All Services

```bash
# Build all services
docker-compose build

# Start all services
docker-compose up -d
```

### 2. Verify Services are Running

```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

### 3. Access Services

- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Config Server**: http://localhost:8888
- **User Service**: http://localhost:8081
- **Product Catalog Service**: http://localhost:8082
- **Cart Service**: http://localhost:8083
- **Order Service**: http://localhost:8084

## Development Setup

### 1. Start Dependencies

```bash
# Start only databases and Redis
docker-compose up postgres-user postgres-product postgres-cart postgres-order redis -d
```

### 2. Start Config Server

```bash
cd config-server
mvn clean install
mvn spring-boot:run
```

### 3. Start Individual Services

```bash
# User Service
cd user-service
mvn clean install
mvn spring-boot:run

# Product Catalog Service
cd product-catalog-service
mvn clean install
mvn spring-boot:run

# Cart Service
cd cart-service
mvn clean install
mvn spring-boot:run

# Order Service
cd order-service
mvn clean install
mvn spring-boot:run

# API Gateway
cd api-gateway
mvn clean install
mvn spring-boot:run
```

## Testing

### 1. Run Integration Tests

```bash
# Run all tests
mvn clean test

# Run specific service tests
mvn test -pl user-service
mvn test -pl product-catalog-service
mvn test -pl cart-service
mvn test -pl order-service
```

### 2. API Testing with Postman

1. Import the Postman collection: `postman/Grocery_Ordering_Platform.postman_collection.json`
2. Set environment variables:
   - `base_url`: http://localhost:8080
   - `firebase_token`: Your Firebase JWT token

### 3. Generate Firebase JWT Token

```javascript
// Using Firebase Admin SDK in Node.js
const admin = require('firebase-admin');
const serviceAccount = require('./path/to/serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const token = await admin.auth().createCustomToken('user123');
console.log(token);
```

## Database Schema

### User Service Database
- `users` table with user profiles and Firebase UID mapping

### Product Catalog Database
- `products` table with product information, pricing, and inventory

### Cart Service Database
- `carts` table for user shopping carts
- `cart_items` table for individual cart items

### Order Service Database
- `orders` table for order information
- `order_items` table for order line items

## API Endpoints

### User Service (Port 8081)
- `POST /api/users/register` - Register new user
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile

### Product Catalog Service (Port 8082)
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `GET /api/products/search` - Search products
- `POST /api/products` - Add new product (Admin)
- `PUT /api/products/{id}` - Update product (Admin)
- `DELETE /api/products/{id}` - Delete product (Admin)

### Cart Service (Port 8083)
- `GET /api/cart` - Get user's cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{productId}` - Update cart item
- `DELETE /api/cart/items/{productId}` - Remove item from cart
- `DELETE /api/cart` - Clear cart

### Order Service (Port 8084)
- `POST /api/orders` - Place new order
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order details
- `PUT /api/orders/{id}/status` - Update order status (Admin)

## Authentication

All protected endpoints require a Firebase JWT token in the Authorization header:

```
Authorization: Bearer <firebase-jwt-token>
```

## Monitoring

### Health Checks
- Config Server: http://localhost:8888/actuator/health
- User Service: http://localhost:8081/actuator/health
- Product Catalog Service: http://localhost:8082/actuator/health
- Cart Service: http://localhost:8083/actuator/health
- Order Service: http://localhost:8084/actuator/health
- API Gateway: http://localhost:8080/actuator/health

### Metrics
- All services expose metrics at `/actuator/metrics`

## Troubleshooting

### Common Issues

1. **Config Server Connection Issues**
   - Ensure Config Server is running before starting other services
   - Check firewall settings for port 8888

2. **Database Connection Issues**
   - Verify PostgreSQL containers are running
   - Check database credentials in configuration

3. **Firebase Authentication Issues**
   - Ensure Firebase service account JSON is properly configured
   - Verify Firebase project ID is correct

4. **Service Communication Issues**
   - Check service URLs in configuration
   - Ensure all services are running and healthy

### Logs

```bash
# View logs for specific service
docker-compose logs -f user-service

# View all logs
docker-compose logs -f

# View logs for specific time period
docker-compose logs --since="2023-01-01T00:00:00" user-service
```

## Production Deployment

### Environment Variables

Set the following environment variables for production:

```bash
# Database
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=your-db-name
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password

# Firebase
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_SERVICE_ACCOUNT_KEY_PATH=/path/to/service-account.json

# Service URLs
USER_SERVICE_URL=http://user-service:8081
PRODUCT_SERVICE_URL=http://product-catalog-service:8082
CART_SERVICE_URL=http://cart-service:8083
ORDER_SERVICE_URL=http://order-service:8084
```

### Docker Production Build

```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Deploy to production
docker-compose -f docker-compose.prod.yml up -d
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License.

