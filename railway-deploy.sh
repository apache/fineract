#!/bin/bash

# Railway Deployment Script for Apache Fineract
set -e

echo "🚀 Deploying Apache Fineract to Railway..."

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

# Check if project is linked
if [ ! -f ".railway/config.json" ]; then
    echo "⚠️ Railway project not linked. Running setup first..."
    ./railway-setup-complete.sh
fi

# Build the application
echo "🏗️ Building Fineract..."
./gradlew :fineract-provider:build -x test -x cucumber -x spotlessCheck -x rat --no-daemon

# Deploy to Railway
echo "🚀 Deploying to Railway..."
railway up --detach

# Check deployment status
echo "🔍 Checking deployment status..."
ATTEMPTS=0
MAX_ATTEMPTS=20
          
until $(curl --output /dev/null --silent --head --fail $(railway service url --url)/fineract-provider/actuator/health) || [ $ATTEMPTS -eq $MAX_ATTEMPTS ]; do
    ATTEMPTS=$((ATTEMPTS+1))
    echo "⏳ Waiting for deployment to be ready... ($ATTEMPTS/$MAX_ATTEMPTS)"
    sleep 15
done
          
if [ $ATTEMPTS -eq $MAX_ATTEMPTS ]; then
    echo "❌ Deployment verification timed out"
    echo "🔎 Checking logs for errors..."
    railway logs -n 100
    exit 1
else
    echo "✅ Deployment verified successfully"
    echo "🌐 Application URL: $(railway service url --url)"
    echo "📊 Health status: $(curl -s $(railway service url --url)/fineract-provider/actuator/health | jq -r '.status')"
fi

echo "
📈 Monitoring information:
- Check logs: railway logs
- Open app: railway open
- PostgreSQL console: railway postgres
- Redis console: railway redis

📊 Health endpoints:
- $(railway service url --url)/fineract-provider/actuator/health
- $(railway service url --url)/fineract-provider/actuator/info
- $(railway service url --url)/fineract-provider/actuator/metrics
"
    echo "✅ Deployment verified successfully"
fi

# Show deployment info
echo "
🎉 Deployment Complete!

📊 Application URL: $(railway service url --url)
📈 Dashboard: https://railway.app/dashboard

To check the health of your application:
./railway-health-check.sh

To monitor logs:
railway logs
"