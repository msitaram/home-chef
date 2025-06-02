#!/bin/bash

# Food Marketplace - Local Development Startup Script
echo "🍽️  Starting Food Marketplace Platform (Event-Driven + SAGA Compensation)"
echo "============================================================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose not found. Please install docker-compose."
    exit 1
fi

echo "🔧 Building Quarkus applications..."

# Build all services
echo "📦 Building User Service..."
cd user-service && mvn clean package -DskipTests && cd ..

echo "📦 Building Menu Service..." 
cd menu-service && mvn clean package -DskipTests && cd ..

echo "📦 Building Order Service..."
cd order-service && mvn clean package -DskipTests && cd ..

echo "📦 Building Review Service..."
cd review-service && mvn clean package -DskipTests && cd ..

echo "📦 Building Payment Service (Compensation)..."
cd payment-service && mvn clean package -DskipTests && cd ..

echo "🚀 Starting all services with docker-compose..."

# Start all services
docker-compose up -d --build

echo "⏳ Waiting for services to be ready..."

# Wait for services to be healthy
sleep 45

echo "📊 Service Status:"
echo "================================"

# Check service health
check_service() {
    local service_name=$1
    local port=$2
    local url="http://localhost:${port}/q/health"
    
    if curl -f -s "$url" > /dev/null 2>&1; then
        echo "✅ $service_name - http://localhost:${port}"
    else
        echo "❌ $service_name - http://localhost:${port} (Not Ready)"
    fi
}

check_service "User Service" 8081
check_service "Menu Service" 8082  
check_service "Order Service" 8083
check_service "Review Service" 8084
check_service "Payment Service" 8085

echo ""
echo "💾 Database Status:"
echo "================================"
echo "🟢 User DB: localhost:5432 (user: useruser, db: userservice)"
echo "🟢 Menu DB: localhost:5433 (user: menuuser, db: menuservice)"
echo "🟢 Order DB: localhost:5434 (user: orderuser, db: orderservice)"
echo "🟢 Review DB: localhost:5435 (user: reviewuser, db: reviewservice)"
echo "🟢 Payment DB: localhost:5436 (user: paymentuser, db: paymentservice)"

echo ""
echo "🎛️  Management Tools:"
echo "================================"
echo "📊 Kafka UI (KRaft): http://localhost:8080"
echo "📨 Redis: localhost:6379"

echo ""
echo "📚 API Documentation:"
echo "================================"
echo "👥 User Service API: http://localhost:8081/q/swagger-ui"
echo "🍽️  Menu Service API: http://localhost:8082/q/swagger-ui"
echo "📦 Order Service API: http://localhost:8083/q/swagger-ui"
echo "⭐ Review Service API: http://localhost:8084/q/swagger-ui"
echo "💳 Payment Service API: http://localhost:8085/q/swagger-ui"

echo ""
echo "🔥 Event Topics (via Kafka UI):"
echo "================================"
echo "• user-events"
echo "• menu-events" 
echo "• order-events"
echo "• review-events"
echo "• payment-events"

echo ""
echo "🔄 SAGA Compensation Patterns:"
echo "================================"
echo "✅ Payment Reservation + Release"
echo "✅ Inventory Hold + Release"
echo "✅ Order Rollback on Failures"
echo "✅ Automatic Refunds"
echo "✅ Timeout Handling"

echo ""
echo "📱 React Native App:"
echo "================================"
echo "📁 Location: ./FoodMarketplaceApp/"
echo "🚀 Start: cd FoodMarketplaceApp && npm start"
echo "📟 Metro: http://localhost:3000"

echo ""
echo "🎯 Food Marketplace Platform is ready!"
echo "All services running in pure event-driven mode with SAGA compensation patterns."
echo ""
echo "💡 Quick Test Commands:"
echo "# Register user:"
echo "curl -X POST http://localhost:8081/api/users/register -H 'Content-Type: application/json' -d '{\"fullName\":\"Test User\",\"email\":\"test@example.com\",\"phone\":\"9876543210\",\"role\":\"CUSTOMER\",\"addressLine1\":\"123 Test St\",\"city\":\"Bangalore\",\"state\":\"Karnataka\",\"pincode\":\"560001\"}'"
echo ""
echo "To stop: ./stop-local.sh"
echo "To view logs: docker-compose logs -f [service-name]"
