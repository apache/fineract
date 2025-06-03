#!/bin/bash
# Script to verify removal of Docker and MariaDB/MySQL references

set -e

# Function to display messages
log() {
  echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

# Function to check for patterns in files
check_pattern() {
  local pattern="$1"
  local exclude_dirs="$2"
  local description="$3"
  
  log "Checking for $description..."
  
  if [ -n "$exclude_dirs" ]; then
    exclude_params=$(echo "$exclude_dirs" | tr ',' '\n' | sed 's/^/--exclude-dir=/g' | tr '\n' ' ')
    results=$(grep -r "$pattern" --include="*.java" --include="*.properties" --include="*.xml" --include="*.yml" --include="*.yaml" --include="*.gradle" --include="*.sh" $exclude_params . 2>/dev/null || true)
  else
    results=$(grep -r "$pattern" --include="*.java" --include="*.properties" --include="*.xml" --include="*.yml" --include="*.yaml" --include="*.gradle" --include="*.sh" . 2>/dev/null || true)
  fi
  
  if [ -z "$results" ]; then
    log "✅ No $description found"
    return 0
  else
    log "❌ Found $description in the following files:"
    echo "$results" | sed 's/^/  /'
    return 1
  fi
}

# Title
log "===== Docker and MariaDB/MySQL Reference Check ====="
log "This script will check for Docker and MariaDB/MySQL references that should be removed."

# Initialize error count
errors=0

# Check for Docker references
check_pattern "Dockerfile" "node_modules,.git,build,dist" "Dockerfile references" || ((errors++))
check_pattern "docker-compose" "node_modules,.git,build,dist" "docker-compose references" || ((errors++))
check_pattern "docker run" "node_modules,.git,build,dist" "docker run commands" || ((errors++))
check_pattern "docker build" "node_modules,.git,build,dist" "docker build commands" || ((errors++))
check_pattern "image:" "node_modules,.git,build,dist,railway-preview-environment.yml,railway-deployment.yml" "Docker image references" || ((errors++))
check_pattern "container:" "node_modules,.git,build,dist,railway-preview-environment.yml,railway-deployment.yml" "Docker container references" || ((errors++))

# Check for MariaDB references
check_pattern "mariadb" "node_modules,.git,build,dist" "MariaDB references" || ((errors++))
check_pattern "org.mariadb" "node_modules,.git,build,dist" "MariaDB Java dependencies" || ((errors++))
check_pattern "maria-db" "node_modules,.git,build,dist" "Maria-DB references" || ((errors++))

# Check for MySQL references
check_pattern "mysql" "node_modules,.git,build,dist,railway-postgres-setup.sh" "MySQL references" || ((errors++))
check_pattern "com.mysql" "node_modules,.git,build,dist" "MySQL Java dependencies" || ((errors++))
check_pattern "MySql" "node_modules,.git,build,dist" "MySql references (case-sensitive)" || ((errors++))

# Check for specific database connection strings
check_pattern "jdbc:mysql" "node_modules,.git,build,dist" "MySQL JDBC URLs" || ((errors++))
check_pattern "jdbc:mariadb" "node_modules,.git,build,dist" "MariaDB JDBC URLs" || ((errors++))

# Print summary
log "===== Summary ====="
if [ $errors -eq 0 ]; then
  log "✅ All checks passed! No Docker or MariaDB/MySQL references found."
else
  log "❌ Found $errors types of references that should be removed."
  log "Please check the output above and remove any remaining references."
fi

exit $errors
