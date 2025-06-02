# 🍽️ Food Marketplace - Event-Driven Microservices Platform

A mobile-first food marketplace connecting verified home cooks with customers seeking regional, hygienic, home-style meals across Indian cities.

## 🏗️ Architecture

### Event-Driven Microservices
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  React Native   │    │   Web Dashboard │    │   Admin Panel   │
│  (iOS/Android)  │    │   (Cooks)       │    │   (Operations)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Load Balancer  │
                    │  (Nginx/Ingress)│
                    └─────────────────┘
                                 │
    ┌────────────────────────────┼────────────────────────────┐
    │                            │                            │
┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
│User       │  │Menu       │  │Order      │  │Review     │
│Service    │  │Service    │  │Service    │  │Service    │
│:8081      │  │:8082      │  │:8083      │  │:8084      │
└───────────┘  └───────────┘  └───────────┘  └───────────┘
       │              │              │              │
       └──────────────┼──────────────┼──────────────┘
                      │              │
             ┌─────────────────┐     │
             │     Kafka       │     │
             │ Message Broker  │─────┘
             │    :9092        │
             └─────────────────┘
                      │
          ┌───────────┼───────────┐
          │           │           │
    ┌──────────┐ ┌──────────┐ ┌──────────┐
    │PostgreSQL│ │  Redis   │ │  S3      │
    │  :5432   │ │  :6379   │ │ Storage  │
    └──────────┘ └──────────┘ └──────────┘
```

### Tech Stack
- **Backend**: Java 17 + Quarkus (Supersonic Subatomic Java)
- **Database**: PostgreSQL 15 (ACID compliance, complex queries)
- **Messaging**: Apache Kafka (Event streaming, eventual consistency)
- **Cache**: Redis 7 (Session management, hot data)
- **Frontend**: React Native (Mobile-first, cross-platform)
- **API Documentation**: OpenAPI 3.0 with Swagger UI

## 🚀 Quick Start (Local Development)

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven 3.8+
- Node.js 18+

### 1. Clone and Start
```bash
cd food-marketplace
chmod +x start-local.sh
./start-local.sh
```

### 2. Access Services
- **Kafka UI**: http://localhost:8080
- **User Service**: http://localhost:8081/q/swagger-ui
- **Menu Service**: http://localhost:8082/q/swagger-ui
- **Order Service**: http://localhost:8083/q/swagger-ui
- **Review Service**: http://localhost:8084/q/swagger-ui

### 3. Test Event Flow
```bash
# Register a user (triggers USER_REGISTERED event)
curl -X POST "http://localhost:8081/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Priya Sharma",
    "email": "priya@example.com",
    "phone": "9876543210",
    "role": "COOK",
    "addressLine1": "123 MG Road",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "specialityCuisine": "South Indian"
  }'

# Create a dish (triggers DISH_CREATED event)
curl -X POST "http://localhost:8082/api/menu/dishes" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Masala Dosa",
    "description": "Crispy South Indian crepe",
    "cookId": "[USER_ID_FROM_ABOVE]",
    "cuisineType": "SOUTH_INDIAN",
    "category": "BREAKFAST",
    "price": 120.00,
    "dailyCapacity": 20
  }'

# Place an order (triggers ORDER_CREATION_REQUESTED event)
curl -X POST "http://localhost:8083/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "[CUSTOMER_ID]",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "456 Brigade Road",
    "deliveryCity": "Bangalore",
    "deliveryPincode": "560001",
    "items": [
      {
        "dishId": "[DISH_ID_FROM_ABOVE]",
        "quantity": 2
      }
    ]
  }'
```

## 🔄 Event-Driven Flow

### Order Processing Flow
```
1. Customer places order → ORDER_CREATION_REQUESTED
2. Menu Service validates dishes → ORDER_VALIDATION_RESPONSE
3. Order Service confirms → ORDER_CONFIRMED
4. Cook updates status → ORDER_STATUS_UPDATED
5. Order delivered → ORDER_DELIVERED
6. Customer reviews → ORDER_REVIEWED
```

### Key Events
- **user-events**: USER_REGISTERED, USER_STATUS_UPDATED, USER_VERIFICATION_UPDATED
- **menu-events**: DISH_CREATED, DISH_UPDATED, ORDER_VALIDATION_RESPONSE
- **order-events**: ORDER_CREATION_REQUESTED, ORDER_CONFIRMED, ORDER_STATUS_UPDATED
- **review-events**: REVIEW_CREATED, REVIEW_MODERATED

## 📊 Monitoring & Development

### Kafka Topics (via Kafka UI)
- Monitor real-time event flow
- Debug message processing
- View consumer lag

### Health Checks
- All services expose `/q/health` endpoints
- Readiness and liveness probes
- Metrics available via Micrometer

### Logs
```bash
# View all logs
docker-compose logs -f

# View specific service
docker-compose logs -f user-service
docker-compose logs -f kafka
```

## 🎯 Production Deployment (Kubernetes)

### Helm Charts (TBD)
```bash
# Install on Kubernetes
helm install food-marketplace ./k8s/helm-chart

# Scale services
kubectl scale deployment order-service --replicas=3
```

### Environment Variables
- **Development**: Local PostgreSQL, Kafka
- **Staging**: Cloud PostgreSQL, MSK/Confluent Cloud
- **Production**: RDS, MSK, ElastiCache, S3

## 🔧 Development Workflow

### Adding New Events
1. Define event in producer service
2. Add consumer in target service(s)
3. Update event documentation
4. Test end-to-end flow

### Adding New Service
1. Copy service template
2. Update docker-compose.yml
3. Add health checks
4. Configure event consumers/producers

## 🧪 Testing

### Event Flow Testing
```bash
# Test specific event flow
cd tests
./test-order-flow.sh

# Load testing
./load-test-orders.sh
```

### API Testing
- Swagger UI available for each service
- Postman collection: `./docs/Food-Marketplace.postman_collection.json`

## 📚 Documentation

- **API Docs**: Available via Swagger UI on each service
- **Event Schemas**: `./docs/event-schemas.md`
- **Database Schema**: `./docs/database-schema.md`
- **Deployment Guide**: `./docs/deployment.md`

## 🛑 Troubleshooting

### Common Issues
1. **Kafka connection issues**: Check if Kafka is healthy
2. **Database connection**: Verify PostgreSQL is running
3. **Service startup**: Check health endpoints

### Debug Commands
```bash
# Check service health
curl http://localhost:8081/q/health

# View Kafka topics
docker exec -it food-marketplace-kafka kafka-topics --bootstrap-server localhost:29092 --list

# Check PostgreSQL
docker exec -it food-marketplace-postgres psql -U fooduser -d foodmarketplace
```

---

## 📞 Support

For issues and questions, check:
1. Service logs: `docker-compose logs [service-name]`
2. Kafka UI: http://localhost:8080
3. Health endpoints: `/q/health`

**Event-Driven Architecture**: All services communicate asynchronously via Kafka events for maximum scalability and resilience.
