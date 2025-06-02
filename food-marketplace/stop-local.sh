#!/bin/bash

# Food Marketplace - Stop Local Development Environment
echo "🛑 Stopping Food Marketplace Platform..."
echo "========================================"

# Stop all services
docker-compose down

echo "🧹 Cleaning up..."

# Optional: Remove volumes (uncomment if you want to reset data)
# docker-compose down -v

echo ""
echo "✅ Food Marketplace Platform stopped."
echo "All containers have been stopped and removed."
echo ""
echo "To start again: ./start-local.sh"
echo "To reset all data: docker-compose down -v"
