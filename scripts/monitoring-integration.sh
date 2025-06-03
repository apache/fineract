#!/bin/bash
# Railway Deployment Monitoring Integration
# This script integrates deployment notifications with monitoring systems

set -e

# Function to display messages with timestamp
log() {
  echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

# Check command line args
VERBOSE=false
MONITORING_SYSTEM="all"
DRY_RUN=true

# Check notification settings from GitHub Actions or environment variables
NOTIFICATION_PRIORITY="${NOTIFICATION_PRIORITY:-medium}"
SEND_GITHUB="${SEND_GITHUB:-true}"
SEND_SLACK="${SEND_SLACK:-true}"
SEND_TEAMS="${SEND_TEAMS:-true}"
SEND_EMAIL="${SEND_EMAIL:-false}"
SEND_WEBHOOK="${SEND_WEBHOOK:-false}"

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
    --verbose)
      VERBOSE=true
      shift
      ;;
    --system=*)
      MONITORING_SYSTEM="${key#*=}"
      shift
      ;;
    --send)
      DRY_RUN=false
      shift
      ;;
    --help)
      echo "Usage: $0 [options]"
      echo ""
      echo "Options:"
      echo "  --verbose               Enable verbose output"
      echo "  --system=SYSTEM         Specify monitoring system (datadog, prometheus, newrelic, grafana, all)"
      echo "  --send                  Actually send data to monitoring systems"
      echo "  --help                  Show this help message"
      exit 0
      ;;
    *)
      log "Unknown option: $key"
      exit 1
      ;;
  esac
done

if [ "$VERBOSE" = true ]; then
  log "Verbose mode enabled"
  log "Monitoring system: $MONITORING_SYSTEM"
  log "Dry run: $DRY_RUN"
  log "Notification priority: $NOTIFICATION_PRIORITY"
  log "Notification channels:"
  log "  - GitHub: $SEND_GITHUB"
  log "  - Slack: $SEND_SLACK"
  log "  - Teams: $SEND_TEAMS"
  log "  - Email: $SEND_EMAIL"
  log "  - Webhook: $SEND_WEBHOOK"
fi

# Define deployment details
if [ -z "$DEPLOY_URL" ]; then
  DEPLOY_URL="https://example-app.railway.app"
  BRANCH_NAME="test-branch"
  DEPLOY_ID="service-12345"
  DEPLOY_ENV="test-environment"
  PROJECT_NAME="test-project"
  DEPLOY_STATUS="success"
  DEPLOY_TYPE="${DEPLOY_TYPE:-preview}"
  DEPLOY_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

  if [ "$VERBOSE" = true ]; then
    log "Using test deployment details"
  fi
fi

# Set deployment type emoji based on type
DEPLOY_TYPE_EMOJI="🔍"
if [[ "$DEPLOY_TYPE" == "production" ]]; then
  DEPLOY_TYPE_EMOJI="🚀"
elif [[ "$DEPLOY_TYPE" == "staging" ]]; then
  DEPLOY_TYPE_EMOJI="🔄"
elif [[ "$DEPLOY_TYPE" == "feature" ]]; then
  DEPLOY_TYPE_EMOJI="🧩"
elif [[ "$DEPLOY_TYPE" == "hotfix" ]]; then
  DEPLOY_TYPE_EMOJI="🔧"
fi

# Function to collect basic system metrics
collect_metrics() {
  log "Collecting system metrics..."
  
  # These would normally be fetched from actual system monitoring
  METRICS=$(cat << EOF
{
  "cpu_usage": 23.5,
  "memory_usage": 1456.8,
  "memory_percent": 42.3,
  "disk_usage": 68.7,
  "network_in": 12.4,
  "network_out": 8.6,
  "response_time": 187,
  "error_rate": 0.02,
  "requests_per_minute": 246,
  "active_users": 42,
  "database_connections": 15,
  "cache_hit_ratio": 0.94
}
EOF
)
  
  echo "$METRICS"
}

# Function to integrate with DataDog
datadog_integration() {
  log "Preparing DataDog integration..."
  
  # Get metrics
  METRICS=$(collect_metrics)
  
  # Create DataDog event
  DD_EVENT=$(cat << EOF
{
  "title": "${DEPLOY_TYPE_EMOJI} Railway Deployment: ${DEPLOY_TYPE} - ${DEPLOY_STATUS}",
  "text": "Deployment to ${DEPLOY_ENV} environment (${DEPLOY_TYPE}) completed with status: ${DEPLOY_STATUS}",
  "alert_type": "info",
  "source_type_name": "railway",
  "tags": [
    "environment:${DEPLOY_ENV}",
    "branch:${BRANCH_NAME}",
    "project:${PROJECT_NAME}",
    "status:${DEPLOY_STATUS}",
    "type:${DEPLOY_TYPE}",
    "priority:${NOTIFICATION_PRIORITY}"
  ],
  "aggregation_key": "${DEPLOY_ID}"
}
EOF
)
  
  # Create DataDog metrics
  DD_METRICS=$(cat << EOF
{
  "series": [
    {
      "metric": "railway.deployment",
      "points": [
        [
          $(date +%s),
          1
        ]
      ],
      "type": "count",
      "tags": [
        "environment:${DEPLOY_ENV}",
        "branch:${BRANCH_NAME}",
        "project:${PROJECT_NAME}",
        "status:${DEPLOY_STATUS}"
      ]
    }
  ]
}
EOF
)
  
  if [ "$VERBOSE" = true ]; then
    log "DataDog event payload:"
    echo "$DD_EVENT" | jq '.' 2>/dev/null || echo "$DD_EVENT"
    
    log "DataDog metrics payload:"
    echo "$DD_METRICS" | jq '.' 2>/dev/null || echo "$DD_METRICS"
  fi
  
  if [ "$DRY_RUN" = false ] && [ -n "$DATADOG_API_KEY" ]; then
    log "Sending event to DataDog..."
    curl -X POST -H "Content-type: application/json" \
      -H "DD-API-KEY: ${DATADOG_API_KEY}" \
      -d "$DD_EVENT" \
      "https://api.datadoghq.com/api/v1/events"
    
    log "Sending metrics to DataDog..."
    curl -X POST -H "Content-type: application/json" \
      -H "DD-API-KEY: ${DATADOG_API_KEY}" \
      -d "$DD_METRICS" \
      "https://api.datadoghq.com/api/v1/series"
  else
    log "DataDog integration dry run completed (set --send flag to actually send data)"
  fi
}

# Function to integrate with Prometheus
prometheus_integration() {
  log "Preparing Prometheus integration..."
  
  # Get metrics
  METRICS=$(collect_metrics)
  
  # Create Prometheus metrics
  PROM_METRICS=$(cat << EOF
# HELP railway_deployment_status Railway deployment status (1=success, 0=failure)
# TYPE railway_deployment_status gauge
railway_deployment_status{environment="${DEPLOY_ENV}",branch="${BRANCH_NAME}",project="${PROJECT_NAME}",type="${DEPLOY_TYPE}"} $([ "$DEPLOY_STATUS" = "success" ] && echo "1" || echo "0")
# HELP railway_deployment_timestamp_seconds Railway deployment timestamp
# TYPE railway_deployment_timestamp_seconds gauge
railway_deployment_timestamp_seconds{environment="${DEPLOY_ENV}",branch="${BRANCH_NAME}",project="${PROJECT_NAME}",type="${DEPLOY_TYPE}"} $(date +%s)
# HELP railway_deployment_response_time_ms Response time in milliseconds
# TYPE railway_deployment_response_time_ms gauge
railway_deployment_response_time_ms{environment="${DEPLOY_ENV}",branch="${BRANCH_NAME}",project="${PROJECT_NAME}",type="${DEPLOY_TYPE}"} $(echo "$METRICS" | jq -r '.response_time')
# HELP railway_deployment_error_rate Error rate percentage
# TYPE railway_deployment_error_rate gauge
railway_deployment_error_rate{environment="${DEPLOY_ENV}",branch="${BRANCH_NAME}",project="${PROJECT_NAME}",type="${DEPLOY_TYPE}"} $(echo "$METRICS" | jq -r '.error_rate')
# HELP railway_deployment_priority Railway deployment priority (1=high, 2=medium, 3=low)
# TYPE railway_deployment_priority gauge
railway_deployment_priority{environment="${DEPLOY_ENV}",branch="${BRANCH_NAME}",project="${PROJECT_NAME}",type="${DEPLOY_TYPE}"} $([ "$NOTIFICATION_PRIORITY" = "high" ] && echo "1" || ([ "$NOTIFICATION_PRIORITY" = "medium" ] && echo "2" || echo "3"))
EOF
)
  
  if [ "$VERBOSE" = true ]; then
    log "Prometheus metrics:"
    echo "$PROM_METRICS"
  fi
  
  if [ "$DRY_RUN" = false ] && [ -n "$PROMETHEUS_PUSHGATEWAY_URL" ]; then
    log "Sending metrics to Prometheus Pushgateway..."
    echo "$PROM_METRICS" | curl -X POST --data-binary @- \
      "${PROMETHEUS_PUSHGATEWAY_URL}/metrics/job/railway_deployment/instance/${DEPLOY_ID}"
  else
    log "Prometheus integration dry run completed (set --send flag to actually send data)"
    echo "$PROM_METRICS" > prometheus-railway-metrics.txt
    log "Metrics saved to prometheus-railway-metrics.txt"
  fi
}

# Function to integrate with New Relic
newrelic_integration() {
  log "Preparing New Relic integration..."
  
  # Get metrics
  METRICS=$(collect_metrics)
  
  # Create New Relic event
  NR_EVENT=$(cat << EOF
{
  "eventType": "RailwayDeployment",
  "environment": "${DEPLOY_ENV}",
  "branch": "${BRANCH_NAME}",
  "project": "${PROJECT_NAME}",
  "deploymentUrl": "${DEPLOY_URL}",
  "deploymentId": "${DEPLOY_ID}",
  "status": "${DEPLOY_STATUS}",
  "deploymentType": "${DEPLOY_TYPE}",
  "priority": "${NOTIFICATION_PRIORITY}",
  "timestamp": "${DEPLOY_TIME}",
  "cpuUsage": $(echo "$METRICS" | jq -r '.cpu_usage'),
  "memoryUsage": $(echo "$METRICS" | jq -r '.memory_usage'),
  "memoryPercent": $(echo "$METRICS" | jq -r '.memory_percent'),
  "diskUsage": $(echo "$METRICS" | jq -r '.disk_usage'),
  "responseTime": $(echo "$METRICS" | jq -r '.response_time'),
  "errorRate": $(echo "$METRICS" | jq -r '.error_rate'),
  "requestsPerMinute": $(echo "$METRICS" | jq -r '.requests_per_minute')
}
EOF
)
  
  if [ "$VERBOSE" = true ]; then
    log "New Relic event payload:"
    echo "$NR_EVENT" | jq '.' 2>/dev/null || echo "$NR_EVENT"
  fi
  
  if [ "$DRY_RUN" = false ] && [ -n "$NEW_RELIC_API_KEY" ] && [ -n "$NEW_RELIC_ACCOUNT_ID" ]; then
    log "Sending event to New Relic..."
    curl -X POST -H "Content-Type: application/json" \
      -H "Api-Key: ${NEW_RELIC_API_KEY}" \
      -d "$NR_EVENT" \
      "https://insights-collector.newrelic.com/v1/accounts/${NEW_RELIC_ACCOUNT_ID}/events"
  else
    log "New Relic integration dry run completed (set --send flag to actually send data)"
  fi
}

# Function to integrate with Grafana
grafana_integration() {
  log "Preparing Grafana integration..."
  
  # Get metrics
  METRICS=$(collect_metrics)
  
  # Create Grafana annotation
  GRAFANA_ANNOTATION=$(cat << EOF
{
  "dashboardId": 1,
  "panelId": 1,
  "time": $(date +%s)000,
  "tags": ["railway", "deployment", "${DEPLOY_ENV}", "${BRANCH_NAME}", "${DEPLOY_TYPE}", "${NOTIFICATION_PRIORITY}"],
  "text": "${DEPLOY_TYPE_EMOJI} Railway Deployment: ${PROJECT_NAME} to ${DEPLOY_ENV} (${DEPLOY_TYPE}) - ${DEPLOY_STATUS}"
}
EOF
)
  
  if [ "$VERBOSE" = true ]; then
    log "Grafana annotation payload:"
    echo "$GRAFANA_ANNOTATION" | jq '.' 2>/dev/null || echo "$GRAFANA_ANNOTATION"
  fi
  
  if [ "$DRY_RUN" = false ] && [ -n "$GRAFANA_API_KEY" ] && [ -n "$GRAFANA_URL" ]; then
    log "Sending annotation to Grafana..."
    curl -X POST -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${GRAFANA_API_KEY}" \
      -d "$GRAFANA_ANNOTATION" \
      "${GRAFANA_URL}/api/annotations"
  else
    log "Grafana integration dry run completed (set --send flag to actually send data)"
  fi
}

# Execute integrations based on the selected monitoring system
# First determine which systems to use based on notification settings
SEND_DATADOG=true
SEND_PROMETHEUS=true
SEND_NEWRELIC=true
SEND_GRAFANA=true

# For low priority deployments that don't send many notifications, limit monitoring too
if [ "$NOTIFICATION_PRIORITY" = "low" ]; then
  if [ "$SEND_SLACK" = "false" ] && [ "$SEND_TEAMS" = "false" ]; then
    log "Low priority deployment with limited notifications - reducing monitoring scope"
    SEND_DATADOG=false
    SEND_PROMETHEUS=true  # Keep Prometheus as it's lightweight
    SEND_NEWRELIC=false
    SEND_GRAFANA=true     # Keep Grafana as it's useful for visualization
  fi
fi

# Execute only the requested/allowed monitoring systems
case "$MONITORING_SYSTEM" in
  "datadog")
    if [ "$SEND_DATADOG" = true ]; then
      datadog_integration
    else
      log "Skipping DataDog integration based on notification settings"
    fi
    ;;
  "prometheus")
    if [ "$SEND_PROMETHEUS" = true ]; then
      prometheus_integration
    else
      log "Skipping Prometheus integration based on notification settings"
    fi
    ;;
  "newrelic")
    if [ "$SEND_NEWRELIC" = true ]; then
      newrelic_integration
    else
      log "Skipping New Relic integration based on notification settings"
    fi
    ;;
  "grafana")
    if [ "$SEND_GRAFANA" = true ]; then
      grafana_integration
    else
      log "Skipping Grafana integration based on notification settings"
    fi
    ;;
  "all")
    if [ "$SEND_DATADOG" = true ]; then datadog_integration; else log "Skipping DataDog integration"; fi
    if [ "$SEND_PROMETHEUS" = true ]; then prometheus_integration; else log "Skipping Prometheus integration"; fi
    if [ "$SEND_NEWRELIC" = true ]; then newrelic_integration; else log "Skipping New Relic integration"; fi
    if [ "$SEND_GRAFANA" = true ]; then grafana_integration; else log "Skipping Grafana integration"; fi
    ;;
  *)
    log "Unknown monitoring system: $MONITORING_SYSTEM"
    log "Supported systems: datadog, prometheus, newrelic, grafana, all"
    exit 1
    ;;
esac

log "Monitoring integration completed!"
if [ "$DRY_RUN" = true ]; then
  log "This was a dry run. Use --send to actually send data to monitoring systems."
  log "Required environment variables for sending data:"
  log "- DATADOG_API_KEY: For DataDog integration"
  log "- PROMETHEUS_PUSHGATEWAY_URL: For Prometheus integration"
  log "- NEW_RELIC_API_KEY and NEW_RELIC_ACCOUNT_ID: For New Relic integration"
  log "- GRAFANA_API_KEY and GRAFANA_URL: For Grafana integration"
fi

# Print notification settings summary
log "Notification settings used:"
log "- Priority: $NOTIFICATION_PRIORITY"
log "- Deployment Type: $DEPLOY_TYPE_EMOJI $DEPLOY_TYPE"
log "- Notification Channels:"
log "  * GitHub: $SEND_GITHUB"
log "  * Slack: $SEND_SLACK"
log "  * Teams: $SEND_TEAMS"
log "  * Email: $SEND_EMAIL"
log "  * Webhook: $SEND_WEBHOOK"
