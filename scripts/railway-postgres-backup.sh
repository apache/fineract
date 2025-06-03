#!/bin/bash
# PostgreSQL Backup Script for Railway Deployments
# This script creates a backup of the PostgreSQL database and uploads it to a secure storage location

set -e

# Function to display messages with timestamp
log() {
  echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

# Variables - these should be set by the environment or passed as arguments
DB_NAME=${1:-$RAILWAY_POSTGRES_DATABASE}
DB_USER=${2:-$RAILWAY_POSTGRES_USERNAME}
DB_HOST=${3:-$RAILWAY_POSTGRES_HOST}
DB_PORT=${4:-$RAILWAY_POSTGRES_PORT}
BACKUP_PATH=${5:-"./backups"}
BACKUP_RETENTION=${6:-7} # Days to keep backups

# Ensure the backup directory exists
mkdir -p "$BACKUP_PATH"

# Create filename with timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILENAME="${DB_NAME}_${TIMESTAMP}.sql"
BACKUP_FILEPATH="${BACKUP_PATH}/${BACKUP_FILENAME}"

log "Starting PostgreSQL backup of database: $DB_NAME"

# Create the database dump
if [ -z "$RAILWAY_POSTGRES_PASSWORD" ]; then
  log "Error: Database password not set in environment"
  exit 1
fi

# Set PGPASSWORD environment variable for passwordless connection
export PGPASSWORD="$RAILWAY_POSTGRES_PASSWORD"

# Perform the backup
log "Creating database dump to $BACKUP_FILEPATH"
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -F p -f "$BACKUP_FILEPATH"

# Compress the backup
log "Compressing backup file"
gzip "$BACKUP_FILEPATH"
COMPRESSED_FILEPATH="${BACKUP_FILEPATH}.gz"

# Calculate backup size
BACKUP_SIZE=$(du -h "$COMPRESSED_FILEPATH" | cut -f1)
log "Backup completed successfully. Size: $BACKUP_SIZE"

# Upload to cloud storage if configured
if [ -n "$BACKUP_STORAGE_URL" ]; then
  log "Uploading backup to storage"
  
  # Determine upload method based on URL scheme
  if [[ "$BACKUP_STORAGE_URL" == s3://* ]]; then
    # AWS S3 upload
    aws s3 cp "$COMPRESSED_FILEPATH" "$BACKUP_STORAGE_URL/${BACKUP_FILENAME}.gz"
  elif [[ "$BACKUP_STORAGE_URL" == gs://* ]]; then
    # Google Cloud Storage upload
    gsutil cp "$COMPRESSED_FILEPATH" "$BACKUP_STORAGE_URL/${BACKUP_FILENAME}.gz"
  else
    # Generic upload using curl
    curl -X PUT -T "$COMPRESSED_FILEPATH" "$BACKUP_STORAGE_URL/${BACKUP_FILENAME}.gz"
  fi
  
  log "Upload completed"
fi

# Clean up old backups
log "Cleaning up backups older than $BACKUP_RETENTION days"
find "$BACKUP_PATH" -name "*.gz" -type f -mtime +$BACKUP_RETENTION -delete

log "Backup process completed successfully"

# Output the backup file path for reference
echo "$COMPRESSED_FILEPATH"
