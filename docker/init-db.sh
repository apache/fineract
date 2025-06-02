#!/bin/bash
# Database initialization script for Fineract on Railway
set -e

echo "Checking PostgreSQL connection..."
# Allow more time for PostgreSQL to start up completely
MAX_RETRIES=30
RETRY_COUNT=0

until PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h $SPRING_DATASOURCE_HOST -U $SPRING_DATASOURCE_USERNAME -c "SELECT 1" > /dev/null 2>&1; do
    RETRY_COUNT=$((RETRY_COUNT+1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo "Failed to connect to PostgreSQL after $MAX_RETRIES attempts. Exiting."
        exit 1
    fi
    echo "Waiting for PostgreSQL to start... (attempt $RETRY_COUNT/$MAX_RETRIES)"
    sleep 5
done

echo "PostgreSQL is running."

# Check if tenants database exists
TENANTS_DB_EXISTS=$(PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h $SPRING_DATASOURCE_HOST -U $SPRING_DATASOURCE_USERNAME -lqt | cut -d \| -f 1 | grep -w fineract_tenants | wc -l)

if [ $TENANTS_DB_EXISTS -eq 0 ]; then
    echo "Creating fineract_tenants database..."
    PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h $SPRING_DATASOURCE_HOST -U $SPRING_DATASOURCE_USERNAME -c "CREATE DATABASE fineract_tenants;"
    echo "fineract_tenants database created."
else
    echo "fineract_tenants database already exists."
fi

# Check if default tenant database exists
DEFAULT_DB_EXISTS=$(PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h $SPRING_DATASOURCE_HOST -U $SPRING_DATASOURCE_USERNAME -lqt | cut -d \| -f 1 | grep -w fineract_default | wc -l)

if [ $DEFAULT_DB_EXISTS -eq 0 ]; then
    echo "Creating fineract_default database..."
    PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h $SPRING_DATASOURCE_HOST -U $SPRING_DATASOURCE_USERNAME -c "CREATE DATABASE fineract_default;"
    echo "fineract_default database created."
else
    echo "fineract_default database already exists."
fi

echo "Databases initialized successfully."
