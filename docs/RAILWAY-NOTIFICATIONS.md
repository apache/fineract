# Railway Deployment Notification System

This document describes the notification system for Railway deployments in the Apache Fineract project.

## Overview

The notification system provides detailed information about deployment status, including:

- Deployment status (success/failure)
- Environment information
- Application URLs
- Health check status
- Links to documentation and dashboards

## Notification Channels

The system currently supports the following notification channels:

1. **GitHub Step Summary**: Automatically displayed in the GitHub Actions workflow UI
2. **Console Output**: Basic information in the workflow logs
3. **Slack** (optional): Rich formatted messages with interactive buttons
4. **Email** (optional): HTML-formatted emails with deployment details
5. **Custom Webhooks** (optional): JSON payloads for integration with other systems

## Configuration

### GitHub Step Summary

This is enabled by default and requires no additional configuration. The workflow creates a formatted Markdown summary that appears in the GitHub Actions UI.

### Slack Integration

To enable Slack notifications:

1. Create a Slack app and webhook in your workspace
2. Add the webhook URL as a GitHub repository secret: `SLACK_WEBHOOK_URL`
3. Uncomment the Slack notification step in the workflow file

### Email Notifications

To enable email notifications:

1. Set up the following GitHub repository secrets:
   - `EMAIL_SERVER`: SMTP server address
   - `EMAIL_PORT`: SMTP port
   - `EMAIL_USERNAME`: SMTP username
   - `EMAIL_PASSWORD`: SMTP password
   - `EMAIL_RECIPIENTS`: Comma-separated list of recipients

2. Uncomment the email notification step in the workflow file

### Custom Webhooks

To enable webhook notifications:

1. Add your webhook URL as a GitHub repository secret: `CUSTOM_WEBHOOK_URL`
2. Uncomment the webhook notification step in the workflow file

## Notification Format

### GitHub Step Summary (Success)

```markdown
## 🚀 Deployment Summary

| Detail | Value |
| ------ | ----- |
| **Status** | ✅ Success |
| **Environment** | production |
| **Branch** | main |
| **Project** | fineract |
| **Deployment URL** | https://example-app.railway.app |
| **Health Check** | https://example-app.railway.app/fineract-provider/actuator/health |
| **API Docs** | https://example-app.railway.app/fineract-provider/swagger-ui/index.html |
| **Railway Service ID** | service-12345 |
| **Deployment Time** | Tue Jun 3 15:30:45 UTC 2025 |

### Quick Links

- 🩺 [Health Check](https://example-app.railway.app/fineract-provider/actuator/health)
- 📊 [Metrics](https://example-app.railway.app/fineract-provider/actuator/metrics)
- 📝 [API Docs](https://example-app.railway.app/fineract-provider/swagger-ui/index.html)
- 🔄 [Railway Dashboard](https://railway.app/project/service-12345)
```

### GitHub Step Summary (Failure)

```markdown
## ❌ Deployment Failure

| Detail | Value |
| ------ | ----- |
| **Status** | ❌ Failed |
| **Branch** | feature/new-feature |
| **Failure Time** | Tue Jun 3 15:30:45 UTC 2025 |

### Troubleshooting

1. Check the deployment logs for error details
2. Verify Railway token is valid
3. Check if there are any issues with the Railway platform
4. Review the build logs for any compilation errors
```

### Slack Message

The Slack message includes:
- Header with deployment status
- Environment and branch information
- Deployment URL
- Interactive buttons for quick access to the application, health check, and Railway dashboard

### Email Notification

The email includes:
- Subject indicating deployment status
- HTML-formatted body with all deployment details
- Styled buttons for quick access to the application and Railway dashboard

### Webhook Payload

```json
{
  "deployment": {
    "status": "success",
    "environment": "production",
    "branch": "main",
    "project": "fineract",
    "url": "https://example-app.railway.app",
    "health_check_url": "https://example-app.railway.app/fineract-provider/actuator/health",
    "api_docs_url": "https://example-app.railway.app/fineract-provider/swagger-ui/index.html",
    "service_id": "service-12345",
    "timestamp": "2025-06-03T15:30:45Z"
  }
}
```

## Testing Notifications

You can test notifications without actual deployment using the provided test script:

```bash
bash ./scripts/test-notifications.sh
```

This script generates sample notification payloads for all supported channels.

## Extending the System

To add a new notification channel:

1. Add a new step to the `notify` job in `.github/workflows/railway-deployment.yml`
2. Configure the notification content using the deployment details outputs
3. Add any required secrets to your GitHub repository
4. Update this documentation to include the new channel

## Troubleshooting

If notifications are not working as expected:

1. Check that all required secrets are properly configured
2. Verify that the notification steps are not commented out
3. Check the workflow logs for any error messages
4. Run the test script to validate the notification formats
5. For external services, verify that the endpoints are accessible from GitHub Actions