#!/bin/bash

# Railway Setup Script for Apache Fineract with PostgreSQL and Dragonfly
set -e

echo "🚀 Starting Railway setup for Apache Fineract..."

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
if [ -f ".railway/config.json" ]; then
    echo "✅ Already linked to Railway project"
else
    echo "Creating new Railway project..."
    railway init
fi

# Add PostgreSQL if not already added
echo "🐘 Setting up PostgreSQL..."
railway add postgresql || echo "PostgreSQL plugin already exists or failed to add"

# Add Dragonfly (Redis-compatible) if not already added
echo "🔴 Setting up Dragonfly (Redis-compatible)..."
railway add redis || echo "Redis plugin already exists or failed to add"

# Add environment variables for configuring database names
echo "🔧 Setting up environment variables..."
railway variables set \
    PGDATABASE=fineract_tenants \
    TENANT_DB_NAME=fineract_default \
    SPRING_PROFILES_ACTIVE=railway,postgresql \
    JAVA_OPTS="-Xmx1G -Xms512M -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxGCPauseMillis=200"

echo "📋 Current services in your Railway project:"
railway service

echo "🌍 Environment variables available:"
railway variables

echo "
🎉 Setup complete! 

Next steps:
1. Connect your GitHub repository for CI/CD:
   Visit https://railway.app/new and select 'Deploy from GitHub repository'

2. Select your repository and configure the following:
   - Branch: main
   - Root Directory: /
   - Environment: Production
   
3. Configure service links:
   Link your PostgreSQL and Redis (Dragonfly) services to your app

4. Monitor your deployment:
   railway logs

5. Open your deployed app:
   railway open

Your Fineract application is configured to automatically use Railway's PostgreSQL and Redis environment variables.
"
