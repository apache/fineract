#!/bin/bash

# Railway PostgreSQL Setup Script for Apache Fineract
set -e

echo "🐘 Setting up PostgreSQL for Apache Fineract on Railway..."

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

# Add PostgreSQL if not already added
echo "🐘 Setting up PostgreSQL..."
railway add postgresql || echo "PostgreSQL plugin already exists or failed to add"

# Configure database names and settings
echo "🔧 Setting up PostgreSQL environment variables..."
railway variables set \
    PGDATABASE=fineract_tenants \
    TENANT_DB_NAME=fineract_default

# Check PostgreSQL connection
echo "🔍 Checking PostgreSQL connection..."
PGHOST=$(railway variables get PGHOST)
PGPORT=$(railway variables get PGPORT)
PGUSER=$(railway variables get PGUSER)
PGPASSWORD=$(railway variables get PGPASSWORD)
PGDATABASE=$(railway variables get PGDATABASE)

if [ -z "$PGHOST" ] || [ -z "$PGPORT" ] || [ -z "$PGUSER" ] || [ -z "$PGPASSWORD" ]; then
    echo "❌ Could not retrieve PostgreSQL connection details"
    exit 1
fi

echo "🔍 PostgreSQL connection details:"
echo "  Host: $PGHOST"
echo "  Port: $PGPORT"
echo "  User: $PGUSER"
echo "  Database: $PGDATABASE"

# Attempt to connect to PostgreSQL
echo "🔄 Testing connection to PostgreSQL..."
if command -v psql &> /dev/null; then
    PGPASSWORD=$PGPASSWORD psql -h $PGHOST -p $PGPORT -U $PGUSER -d postgres -c "SELECT 1" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ Successfully connected to PostgreSQL"
    else
        echo "❌ Failed to connect to PostgreSQL"
        exit 1
    fi
else
    echo "⚠️ psql command not available, skipping connection test"
fi

# Create tenant database if it doesn't exist
echo "🏗️ Setting up tenant database..."
if command -v psql &> /dev/null; then
    PGPASSWORD=$PGPASSWORD psql -h $PGHOST -p $PGPORT -U $PGUSER -d postgres -c "SELECT 1 FROM pg_database WHERE datname = 'fineract_tenants'" | grep -q 1
    if [ $? -ne 0 ]; then
        echo "Creating fineract_tenants database..."
        PGPASSWORD=$PGPASSWORD psql -h $PGHOST -p $PGPORT -U $PGUSER -d postgres -c "CREATE DATABASE fineract_tenants"
        echo "✅ Created fineract_tenants database"
    else
        echo "✅ fineract_tenants database already exists"
    fi
    
    # Create tenant database
    TENANT_DB_NAME=$(railway variables get TENANT_DB_NAME || echo "fineract_default")
    PGPASSWORD=$PGPASSWORD psql -h $PGHOST -p $PGPORT -U $PGUSER -d postgres -c "SELECT 1 FROM pg_database WHERE datname = '$TENANT_DB_NAME'" | grep -q 1
    if [ $? -ne 0 ]; then
        echo "Creating $TENANT_DB_NAME database..."
        PGPASSWORD=$PGPASSWORD psql -h $PGHOST -p $PGPORT -U $PGUSER -d postgres -c "CREATE DATABASE $TENANT_DB_NAME"
        echo "✅ Created $TENANT_DB_NAME database"
    else
        echo "✅ $TENANT_DB_NAME database already exists"
    fi
else
    echo "⚠️ psql command not available, skipping database creation"
    echo "ℹ️ The application will create the databases automatically on first run"
fi

echo "
🎉 PostgreSQL setup complete!

Your Fineract application is configured to use Railway's PostgreSQL service.
The necessary databases will be created automatically when the application runs.

Next steps:
1. Set up Redis/Dragonfly with: ./railway-redis-setup.sh
2. Deploy the application with: ./railway-deploy.sh
"

echo "✅ PostgreSQL connection details retrieved"
echo "🔗 Host: $PGHOST"
echo "🔢 Port: $PGPORT"
echo "👤 User: $PGUSER"
echo "📚 Database: $PGDATABASE"

echo "
🎉 PostgreSQL Setup Complete!

Your Fineract application is configured to use Railway's PostgreSQL service.
Next, you may want to set up Dragonfly (Redis-compatible) for caching.

To monitor your PostgreSQL instance:
railway service open postgresql

To check the database status:
railway logs postgresql
"