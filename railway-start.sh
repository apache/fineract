#!/bin/bash
# Railway startup script for Apache Fineract

# Set default environment variables if not provided
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-railway}"
export SERVER_PORT="${PORT:-8080}"
export JAVA_OPTS="${JAVA_OPTS:--Xmx2g -Xms512m}"

# Ensure UTF-8 encoding
export LANG="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

# Set timezone to UTC for consistent behavior
export TZ="UTC"

# Railway database URL construction for PostgreSQL
if [ -n "$PGHOST" ] && [ -n "$PGPORT" ] && [ -n "$PGDATABASE" ]; then
    export DATABASE_URL="postgresql://${PGUSER}:${PGPASSWORD}@${PGHOST}:${PGPORT}/${PGDATABASE}"
    echo "Database URL configured for Railway PostgreSQL"
fi

# Railway Redis URL construction
if [ -n "$REDIS_HOST" ] && [ -n "$REDIS_PORT" ]; then
    if [ -n "$REDIS_PASSWORD" ]; then
        export REDIS_URL="redis://:${REDIS_PASSWORD}@${REDIS_HOST}:${REDIS_PORT}/0"
    else
        export REDIS_URL="redis://${REDIS_HOST}:${REDIS_PORT}/0"
    fi
    echo "Redis URL configured for Railway"
fi

# Log startup information
echo "Starting Apache Fineract on Railway..."
echo "Profile: ${SPRING_PROFILES_ACTIVE}"
echo "Port: ${SERVER_PORT}"
echo "Java Opts: ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app/fineract-provider.jar \
    --spring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    --server.port=${SERVER_PORT}
