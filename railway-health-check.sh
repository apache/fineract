#!/bin/bash

# Railway Health Check Script for Apache Fineract
set -e

echo "🔍 Checking Apache Fineract health on Railway..."

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

# Get the service URL
SERVICE_URL=$(railway service url --url)

if [ -z "$SERVICE_URL" ]; then
    echo "❌ Failed to get service URL. Make sure your app is deployed."
    exit 1
fi

echo "🔗 Service URL: $SERVICE_URL"

# Check application health
echo "🩺 Checking application health..."
HEALTH_URL="$SERVICE_URL/fineract-provider/actuator/health"
HEALTH_RESPONSE=$(curl -s "$HEALTH_URL")

if [[ "$HEALTH_RESPONSE" == *"\"status\":\"UP\""* ]]; then
    echo "✅ Application is healthy"
else
    echo "❌ Application health check failed"
    echo "Response: $HEALTH_RESPONSE"
    exit 1
fi

# Check PostgreSQL health
echo "🐘 Checking PostgreSQL health..."
if [[ "$HEALTH_RESPONSE" == *"\"db\":{\"status\":\"UP\""* ]]; then
    echo "✅ PostgreSQL is healthy"
else
    echo "❌ PostgreSQL health check failed"
    echo "Response: $HEALTH_RESPONSE"
fi

# Check Redis/Dragonfly health
echo "🔴 Checking Redis/Dragonfly health..."
if [[ "$HEALTH_RESPONSE" == *"\"redis\":{\"status\":\"UP\""* ]]; then
    echo "✅ Redis/Dragonfly is healthy"
else
    echo "❌ Redis/Dragonfly health check failed"
    echo "Response: $HEALTH_RESPONSE"
fi

# Check disk space
echo "💾 Checking disk space..."
if [[ "$HEALTH_RESPONSE" == *"\"diskSpace\":{\"status\":\"UP\""* ]]; then
    echo "✅ Disk space is sufficient"
else
    echo "❌ Disk space check failed"
    echo "Response: $HEALTH_RESPONSE"
fi

# Fetch metrics for monitoring
echo "📊 Fetching application metrics..."
METRICS_URL="$SERVICE_URL/fineract-provider/actuator/metrics"
METRICS_RESPONSE=$(curl -s "$METRICS_URL")

if [ -n "$METRICS_RESPONSE" ]; then
    echo "✅ Metrics available"
    
    # Fetch JVM metrics
    JVM_MEMORY_URL="$SERVICE_URL/fineract-provider/actuator/metrics/jvm.memory.used"
    JVM_MEMORY=$(curl -s "$JVM_MEMORY_URL")
    
    if [ -n "$JVM_MEMORY" ]; then
        echo "🧠 JVM Memory Usage: $(echo $JVM_MEMORY | jq -r '.measurements[0].value') bytes"
    fi
else
    echo "⚠️ Metrics not available"
fi

echo "
📈 Monitoring URLs:
- Health: $HEALTH_URL
- Info: $SERVICE_URL/fineract-provider/actuator/info
- Metrics: $METRICS_URL

To check detailed logs:
  railway logs

To access the database:
  railway postgres
"

# Check Redis/Dragonfly health
echo "🔴 Checking Redis/Dragonfly health..."
if [[ "$HEALTH_RESPONSE" == *"\"redis\":{\"status\":\"UP\""* ]]; then
    echo "✅ Redis/Dragonfly is healthy"
else
    echo "❌ Redis/Dragonfly health check failed"
    echo "Response: $HEALTH_RESPONSE"
fi

echo "
🎉 Health Check Complete!

To view detailed metrics:
$SERVICE_URL/fineract-provider/actuator/metrics

To view prometheus metrics:
$SERVICE_URL/fineract-provider/actuator/prometheus

To monitor logs:
railway logs
"