#!/bin/bash
# Conditional Railway Deployment Notification Script
# This script sends notifications based on the deployment type/environment

set -e

# Check for debug mode
DEBUG_MODE=false
if [[ "$1" == "--debug" ]]; then
  DEBUG_MODE=true
  echo "🔍 Running in DEBUG mode"
fi

# Function to display messages with timestamp
log() {
  echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

# Function for debug messages
debug() {
  if [ "$DEBUG_MODE" = true ]; then
    echo "[DEBUG] $1"
  fi
}

# Define required environment variables
if [ -z "$DEPLOY_TYPE" ] || [ -z "$DEPLOY_STATUS" ]; then
  if [ "$DEBUG_MODE" = true ]; then
    # Use sample values for debugging
    debug "Using sample values for debugging"
    DEPLOY_TYPE=${DEPLOY_TYPE:-"production"}
    DEPLOY_STATUS=${DEPLOY_STATUS:-"success"}
    BRANCH_NAME=${BRANCH_NAME:-"main"}
    DEPLOY_URL=${DEPLOY_URL:-"https://example-app.railway.app"}
    DEPLOY_ENV=${DEPLOY_ENV:-"production"}
  else
    log "❌ Error: Required environment variables are missing"
    log "Required: DEPLOY_TYPE, DEPLOY_STATUS"
    log "Run with --debug flag to use sample values"
    exit 1
  fi
fi

log "Starting conditional notification process..."
log "Deployment type: $DEPLOY_TYPE"
log "Deployment status: $DEPLOY_STATUS"
debug "Branch name: $BRANCH_NAME"
debug "Deploy URL: $DEPLOY_URL"
debug "Environment: $DEPLOY_ENV"

# Define notification types per environment
SEND_GITHUB_SUMMARY=true  # Always send GitHub summary
SEND_SLACK=false
SEND_TEAMS=false
SEND_EMAIL=false
SEND_WEBHOOK=false

# Configure notification channels based on deployment type
case "$DEPLOY_TYPE" in
  "production")
    log "Setting up notifications for PRODUCTION deployment"
    SEND_SLACK=true
    SEND_TEAMS=true
    SEND_EMAIL=true
    SEND_WEBHOOK=true
    NOTIFICATION_PRIORITY="high"
    ;;
  "staging")
    log "Setting up notifications for STAGING deployment"
    SEND_SLACK=true
    SEND_TEAMS=true
    SEND_EMAIL=false
    SEND_WEBHOOK=true
    NOTIFICATION_PRIORITY="medium"
    ;;
  "feature")
    log "Setting up notifications for FEATURE deployment"
    SEND_SLACK=true
    SEND_TEAMS=false
    SEND_EMAIL=false
    SEND_WEBHOOK=false
    NOTIFICATION_PRIORITY="low"
    ;;
  "hotfix")
    log "Setting up notifications for HOTFIX deployment"
    SEND_SLACK=true
    SEND_TEAMS=true
    SEND_EMAIL=false
    SEND_WEBHOOK=true
    NOTIFICATION_PRIORITY="medium"
    ;;
  *)
    log "Setting up notifications for PREVIEW deployment"
    SEND_SLACK=false
    SEND_TEAMS=false
    SEND_EMAIL=false
    SEND_WEBHOOK=false
    NOTIFICATION_PRIORITY="low"
    ;;
esac

# Override settings based on deployment status
if [ "$DEPLOY_STATUS" != "success" ]; then
  log "Deployment failed - adjusting notification settings"
  # Always notify about failures in more channels
  SEND_SLACK=true
  SEND_TEAMS=true
  
  # Send email for production and staging failures
  if [ "$DEPLOY_TYPE" == "production" ] || [ "$DEPLOY_TYPE" == "staging" ]; then
    SEND_EMAIL=true
  fi
  
  NOTIFICATION_PRIORITY="high"
fi

# Output notification configuration for GitHub Actions
log "Notification configuration:"
if [ -n "$GITHUB_OUTPUT" ]; then
  # GitHub Actions environment
  echo "send-github=$SEND_GITHUB_SUMMARY" >> $GITHUB_OUTPUT
  echo "send-slack=$SEND_SLACK" >> $GITHUB_OUTPUT
  echo "send-teams=$SEND_TEAMS" >> $GITHUB_OUTPUT
  echo "send-email=$SEND_EMAIL" >> $GITHUB_OUTPUT
  echo "send-webhook=$SEND_WEBHOOK" >> $GITHUB_OUTPUT
  echo "priority=$NOTIFICATION_PRIORITY" >> $GITHUB_OUTPUT
else
  # Running locally
  echo "send-github: $SEND_GITHUB_SUMMARY"
  echo "send-slack: $SEND_SLACK"
  echo "send-teams: $SEND_TEAMS"
  echo "send-email: $SEND_EMAIL"
  echo "send-webhook: $SEND_WEBHOOK"
  echo "priority: $NOTIFICATION_PRIORITY"
fi

log "Notification settings configured successfully"

# These settings can be used in GitHub Actions to conditionally run notification steps
# For example:
# if: steps.notification-config.outputs.send-slack == 'true'
