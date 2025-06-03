#!/bin/bash

# Railway Setup Script for Apache Fineract
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

echo "📋 Current services in your Railway project:"
railway service

echo "🌍 Environment variables available:"
railway variables

echo "
🎉 Setup complete! 

Next steps:
1. Build and deploy your application:
   railway up

2. Check logs:
   railway logs

3. Open your deployed app:
   railway open

4. Monitor your database:
   railway pg

Your Fineract application is configured to automatically use Railway's PostgreSQL environment variables.
"
