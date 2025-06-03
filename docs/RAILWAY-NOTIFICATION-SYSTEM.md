# Railway Deployment Notification System

This document provides an overview of the Railway Deployment Notification System implemented for Fineract.

## Notification Channels

The system supports multiple notification channels:

- **GitHub**: Deployment summaries directly in GitHub Actions workflow
- **Slack**: Detailed Slack messages with action buttons
- **MS Teams**: Rich Teams notifications with deployment details
- **Email**: HTML email notifications
- **Webhook**: Custom webhook payloads for integration with other systems
- **Monitoring**: Integration with monitoring systems (DataDog, Prometheus, New Relic, Grafana)

## Conditional Notification Logic

The system uses a smart conditional notification system based on:

1. **Deployment Type**:
   - 🚀 **Production**: High priority, all channels notified
   - 🔄 **Staging**: Medium priority, most channels notified
   - 🧩 **Feature**: Low priority, minimal notifications
   - 🔧 **Hotfix**: Medium priority, important channels notified
   - 🔍 **Preview**: Low priority, minimal notifications

2. **Deployment Status**:
   - **Success**: Normal notification rules based on deployment type
   - **Failure**: Enhanced notifications (more channels) to ensure visibility of failures

3. **Notification Priority**:
   - **High**: All notification channels and monitoring systems
   - **Medium**: Main notification channels and most monitoring systems
   - **Low**: Limited notification channels and reduced monitoring

## Scripts and Utilities

### Conditional Notifications

The `conditional-notifications.sh` script determines which notification channels to use:

```bash
# Run with deployment type and status
export DEPLOY_TYPE=production DEPLOY_STATUS=success
bash ./scripts/conditional-notifications.sh
```

### Monitoring Integration

The `monitoring-integration.sh` script integrates with monitoring systems:

```bash
# Run with various options
bash ./scripts/monitoring-integration.sh --verbose
bash ./scripts/monitoring-integration.sh --system=datadog --send
```

### Test Notifications

The `test-notifications.sh` script tests notification formats:

```bash
# Test all notification formats
bash ./scripts/test-notifications.sh

# Actually send notifications (requires configuration)
bash ./scripts/test-notifications.sh --send
```

## VS Code Tasks

The project includes VS Code tasks for testing the notification system:

- **Railway: Test Notifications**: Test notification formats without sending
- **Railway: Test Notifications (Send Mode)**: Test with actual sending
- **Railway: Test Conditional Notifications**: Test the conditional logic for success
- **Railway: Test Conditional Notifications (Failure)**: Test the conditional logic for failure
- **Railway: Test Monitoring Integration**: Test monitoring system integration
- **Railway: Test Monitoring with Priority**: Test with different priority levels
- **Railway: Test Monitoring for Feature Deployment**: Test monitoring for feature deployments

## GitHub Actions Workflow

The GitHub Actions workflow (`railway-deployment.yml`) uses the conditional notification system to determine which notifications to send based on the deployment type and status.

## Configuration

To configure notification channels, set the following secrets in your GitHub repository:

- **Slack**: `SLACK_WEBHOOK_URL`
- **MS Teams**: `MS_TEAMS_WEBHOOK_URL`
- **Email**: `EMAIL_SERVER`, `EMAIL_PORT`, `EMAIL_USERNAME`, `EMAIL_PASSWORD`, `EMAIL_RECIPIENTS`
- **Webhook**: `CUSTOM_WEBHOOK_URL`
- **DataDog**: `DATADOG_API_KEY`
- **Prometheus**: `PROMETHEUS_PUSHGATEWAY_URL`
- **New Relic**: `NEW_RELIC_API_KEY`, `NEW_RELIC_ACCOUNT_ID`
- **Grafana**: `GRAFANA_API_KEY`, `GRAFANA_URL`

## Troubleshooting

If notifications aren't working as expected:

1. Check the logs in GitHub Actions for any error messages
2. Run the test scripts locally with the `--verbose` or `--debug` flag
3. Verify that the required secrets are configured
4. Ensure the conditional notification script is determining the correct channels
5. Check that the monitoring integration script is connecting to the monitoring systems
