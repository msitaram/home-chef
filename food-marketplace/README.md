# Food Marketplace - Mobile-First Platform

## Architecture Overview
This is a microservices-based food marketplace platform built with Quarkus, designed to connect verified home cooks with customers seeking regional, hygienic, home-style meals across Indian cities.

## Tech Stack
- **Backend**: Quarkus (Java 17)
- **Database**: PostgreSQL (per-service schemas)
- **Event Streaming**: Apache Kafka
- **Caching**: Redis
- **Authentication**: OIDC-compliant IdP (Keycloak)
- **Frontend**: React Native (mobile-first)
- **Containerization**: Docker & Kubernetes

## Microservices

### Core Services
1. **User Service** - Registration, authentication, roles (cook/customer/admin)
2. **Menu Service** - Dishes, pricing, schedules, images  
3. **Order Service** - Carts, order flow, state transitions
4. **Review Service** - Ratings, reviews, abuse reporting

### Supporting Services  
5. **Search Service** - Full-text search across dishes, chefs, cuisines
6. **Notification Service** - Push/SMS/email alerts
7. **Delivery Integration Service** - Dunzo, Porter, Shadowfax APIs
8. **Payment Integration Service** - Razorpay/UPI integration
9. **Image Storage Service** - S3-compatible storage

## Event-Driven Architecture
Services communicate via Kafka events:
- `UserRegistered`
- `OrderPlaced` 
- `MenuUpdated`
- `ReviewSubmitted`
- `PaymentProcessed`

## Development Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

### Quick Start
```bash
# Start infrastructure
docker-compose up -d

# Build all services
mvn clean compile

# Run specific service in dev mode
cd user-service
mvn quarkus:dev
```

## Service URLs (Development)
- User Service: http://localhost:8081
- Menu Service: http://localhost:8082  
- Order Service: http://localhost:8083
- Review Service: http://localhost:8084
- Search Service: http://localhost:8085
- Notification Service: http://localhost:8086
- Delivery Integration: http://localhost:8087
- Payment Integration: http://localhost:8088
- Image Storage: http://localhost:8089

## API Documentation
Each service exposes OpenAPI documentation at `/q/swagger-ui`

## Authentication Flow
- OIDC Authorization Code Flow with PKCE
- JWT tokens validated by each microservice
- Role-based access control (COOK, CUSTOMER, ADMIN)
- Aadhaar/PAN verification for cook onboarding
