#!/bin/bash

# Railway Redis/Dragonfly Setup Script for Apache Fineract
set -e

echo "🔴 Setting up Redis/Dragonfly for Apache Fineract on Railway..."

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI not found. Installing..."
    curl -fsSL https://railway.app/install.sh | sh
    echo "✅ Railway CLI installed"
fi

# Check if user is logged in
if ! railway whoami &> /dev/null; then
    echo "🔐 Please log in to Railway..."
    railway login
fi

echo "✅ Authenticated with Railway"

# Link or create project
echo "🔗 Linking to Railway project..."
if [ ! -f ".railway/config.json" ]; then
    echo "Creating new Railway project..."
    railway init
fi

# Add Redis/Dragonfly if not already added
echo "🔴 Setting up Redis/Dragonfly..."
railway add redis || echo "Redis plugin already exists or failed to add"

# Check Redis connection
echo "🔍 Checking Redis/Dragonfly connection..."
REDISHOST=$(railway variables get REDISHOST)
REDISPORT=$(railway variables get REDISPORT)
REDISPASSWORD=$(railway variables get REDISPASSWORD)

if [ -z "$REDISHOST" ] || [ -z "$REDISPORT" ]; then
    echo "❌ Could not retrieve Redis/Dragonfly connection details"
    exit 1
fi

echo "🔍 Redis/Dragonfly connection details:"
echo "  Host: $REDISHOST"
echo "  Port: $REDISPORT"

# Attempt to connect to Redis
echo "🔄 Testing connection to Redis/Dragonfly..."
if command -v redis-cli &> /dev/null; then
    if [ -z "$REDISPASSWORD" ]; then
        redis-cli -h $REDISHOST -p $REDISPORT ping > /dev/null 2>&1
    else
        redis-cli -h $REDISHOST -p $REDISPORT -a $REDISPASSWORD ping > /dev/null 2>&1
    fi
    
    if [ $? -eq 0 ]; then
        echo "✅ Successfully connected to Redis/Dragonfly"
    else
        echo "❌ Failed to connect to Redis/Dragonfly"
        exit 1
    fi
else
    echo "⚠️ redis-cli command not available, skipping connection test"
fi

echo "
🎉 Redis/Dragonfly setup complete!

Your Fineract application is configured to use Railway's Redis/Dragonfly service.
This will be used for caching and session management.

Next steps:
1. Deploy the application with: ./railway-deploy.sh
"
