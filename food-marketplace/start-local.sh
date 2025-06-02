#!/bin/bash

# Food Marketplace - Local Development Startup Script
echo "🍽️  Starting Food Marketplace Platform (Event-Driven Architecture)"
echo "================================================================="

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

echo "🚀 Starting all services with docker-compose..."

# Start all services
docker-compose up -d --build

echo "⏳ Waiting for services to be ready..."

# Wait for services to be healthy
sleep 30

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

echo ""
echo "🎛️  Management Tools:"
echo "================================"
echo "📊 Kafka UI: http://localhost:8080"
echo "🐘 PostgreSQL: localhost:5432 (user: fooduser, db: foodmarketplace)"
echo "📨 Redis: localhost:6379"

echo ""
echo "📚 API Documentation:"
echo "================================"
echo "👥 User Service API: http://localhost:8081/q/swagger-ui"
echo "🍽️  Menu Service API: http://localhost:8082/q/swagger-ui"
echo "📦 Order Service API: http://localhost:8083/q/swagger-ui"
echo "⭐ Review Service API: http://localhost:8084/q/swagger-ui"

echo ""
echo "🔥 Event Topics (via Kafka UI):"
echo "================================"
echo "• user-events"
echo "• menu-events" 
echo "• order-events"
echo "• review-events"

echo ""
echo "🎯 Food Marketplace Platform is ready!"
echo "All services are running in event-driven mode with Kafka messaging."
echo ""
echo "To stop all services: docker-compose down"
echo "To view logs: docker-compose logs -f [service-name]"
echo "To restart a service: docker-compose restart [service-name]"
