#!/bin/bash
# Railway Deployment Notification Test Script
# This script tests the notification mechanisms for Railway deployments

set -e

# Check for send mode
SEND_MODE=false
if [[ "$1" == "--send" ]]; then
  SEND_MODE=true
  echo "⚠️ Running in SEND mode - notifications will actually be sent!"
  sleep 2
else
  echo "ℹ️ Running in simulation mode - notifications will NOT be sent"
  echo "ℹ️ Use --send flag to send actual notifications"
fi

# Function to display messages with timestamp
log() {
  echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

# Define test variables
TEST_DEPLOY_URL="https://example-app.railway.app"
TEST_BRANCH_NAME="test-branch"
TEST_DEPLOY_ID="service-12345"
TEST_DEPLOY_ENV="test-environment"
TEST_PROJECT_NAME="test-project"

log "Starting notification tests..."

# Test GitHub Step Summary
log "Testing GitHub Step Summary format..."
cat << EOF > test-deployment-summary.md
## 🚀 Deployment Summary

| Detail | Value |
| ------ | ----- |
| **Status** | ✅ Success |
| **Environment** | ${TEST_DEPLOY_ENV} |
| **Branch** | ${TEST_BRANCH_NAME} |
| **Project** | ${TEST_PROJECT_NAME} |
| **Deployment URL** | ${TEST_DEPLOY_URL} |
| **Health Check** | ${TEST_DEPLOY_URL}/fineract-provider/actuator/health |
| **API Docs** | ${TEST_DEPLOY_URL}/fineract-provider/swagger-ui/index.html |
| **Railway Service ID** | ${TEST_DEPLOY_ID} |
| **Deployment Time** | $(date) |

### Quick Links

- 🩺 [Health Check](${TEST_DEPLOY_URL}/fineract-provider/actuator/health)
- 📊 [Metrics](${TEST_DEPLOY_URL}/fineract-provider/actuator/metrics)
- 📝 [API Docs](${TEST_DEPLOY_URL}/fineract-provider/swagger-ui/index.html)
- 🔄 [Railway Dashboard](https://railway.app/project/${TEST_DEPLOY_ID})
EOF

log "GitHub Summary created: test-deployment-summary.md"
cat test-deployment-summary.md

# Test failure summary
log "Testing GitHub Step Failure Summary format..."
cat << EOF > test-deployment-failure.md
## ❌ Deployment Failure

| Detail | Value |
| ------ | ----- |
| **Status** | ❌ Failed |
| **Branch** | ${TEST_BRANCH_NAME} |
| **Failure Time** | $(date) |

### Troubleshooting

1. Check the deployment logs for error details
2. Verify Railway token is valid
3. Check if there are any issues with the Railway platform
4. Review the build logs for any compilation errors
EOF

log "GitHub Failure Summary created: test-deployment-failure.md"
cat test-deployment-failure.md

# Test Slack notification
log "Testing Slack notification format..."
SLACK_PAYLOAD=$(cat << EOF
{
  "blocks": [
    {
      "type": "header",
      "text": {
        "type": "plain_text",
        "text": "🚀 Railway Deployment: Success",
        "emoji": true
      }
    },
    {
      "type": "section",
      "fields": [
        {
          "type": "mrkdwn",
          "text": "*Environment:*\n${TEST_DEPLOY_ENV}"
        },
        {
          "type": "mrkdwn",
          "text": "*Branch:*\n${TEST_BRANCH_NAME}"
        },
        {
          "type": "mrkdwn",
          "text": "*Project:*\n${TEST_PROJECT_NAME}"
        },
        {
          "type": "mrkdwn",
          "text": "*Deployed at:*\n$(date)"
        }
      ]
    },
    {
      "type": "section",
      "text": {
        "type": "mrkdwn",
        "text": "*Deployment URL:* ${TEST_DEPLOY_URL}"
      }
    },
    {
      "type": "actions",
      "elements": [
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": "View Application",
            "emoji": true
          },
          "url": "${TEST_DEPLOY_URL}"
        },
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": "Health Check",
            "emoji": true
          },
          "url": "${TEST_DEPLOY_URL}/fineract-provider/actuator/health"
        },
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": "Railway Dashboard",
            "emoji": true
          },
          "url": "https://railway.app/project/${TEST_DEPLOY_ID}"
        }
      ]
    }
  ]
}
EOF
)

log "Slack payload created:"
echo "$SLACK_PAYLOAD" | jq '.' 2>/dev/null || echo "$SLACK_PAYLOAD"

if [ "$SEND_MODE" = true ] && [ -n "$SLACK_WEBHOOK_URL" ]; then
  log "Sending actual Slack notification..."
  curl -X POST -H "Content-type: application/json" -d "$SLACK_PAYLOAD" "$SLACK_WEBHOOK_URL"
  log "Slack notification sent!"
else
  log "Slack notification not sent (simulation mode or missing webhook URL)"
fi

# Test MS Teams notification
log "Testing MS Teams notification format..."
TEAMS_PAYLOAD=$(cat << EOF
{
  "@type": "MessageCard",
  "@context": "http://schema.org/extensions",
  "themeColor": "0076D7",
  "summary": "Railway Deployment: Success",
  "sections": [
    {
      "activityTitle": "🚀 Railway Deployment: Success",
      "facts": [
        {
          "name": "Status",
          "value": "✅ Success"
        },
        {
          "name": "Environment",
          "value": "${TEST_DEPLOY_ENV}"
        },
        {
          "name": "Branch",
          "value": "${TEST_BRANCH_NAME}"
        },
        {
          "name": "Project",
          "value": "${TEST_PROJECT_NAME}"
        },
        {
          "name": "Deployment URL",
          "value": "${TEST_DEPLOY_URL}"
        },
        {
          "name": "Deployment Time",
          "value": "$(date)"
        }
      ],
      "markdown": true
    }
  ],
  "potentialAction": [
    {
      "@type": "OpenUri",
      "name": "View Application",
      "targets": [
        {
          "os": "default",
          "uri": "${TEST_DEPLOY_URL}"
        }
      ]
    },
    {
      "@type": "OpenUri",
      "name": "Health Check",
      "targets": [
        {
          "os": "default",
          "uri": "${TEST_DEPLOY_URL}/fineract-provider/actuator/health"
        }
      ]
    },
    {
      "@type": "OpenUri",
      "name": "Railway Dashboard",
      "targets": [
        {
          "os": "default",
          "uri": "https://railway.app/project/${TEST_DEPLOY_ID}"
        }
      ]
    }
  ]
}
EOF
)

log "MS Teams payload created:"
echo "$TEAMS_PAYLOAD" | jq '.' 2>/dev/null || echo "$TEAMS_PAYLOAD"

if [ "$SEND_MODE" = true ] && [ -n "$MS_TEAMS_WEBHOOK_URL" ]; then
  log "Sending actual MS Teams notification..."
  curl -X POST -H "Content-type: application/json" -d "$TEAMS_PAYLOAD" "$MS_TEAMS_WEBHOOK_URL"
  log "MS Teams notification sent!"
else
  log "MS Teams notification not sent (simulation mode or missing webhook URL)"
fi

# Test email notification
log "Testing email notification format..."
EMAIL_BODY=$(cat << EOF
<html>
<body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; color: #333;">
  <div style="max-width: 600px; margin: 0 auto; background-color: #f9f9f9; border-radius: 5px; padding: 20px; border: 1px solid #ddd;">
    <h1 style="color: #0066cc;">🚀 Railway Deployment: Success</h1>
    <table style="width: 100%; border-collapse: collapse;">
      <tr>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Status:</strong></td>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;">✅ Success</td>
      </tr>
      <tr>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Environment:</strong></td>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;">${TEST_DEPLOY_ENV}</td>
      </tr>
      <tr>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Branch:</strong></td>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;">${TEST_BRANCH_NAME}</td>
      </tr>
      <tr>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Project:</strong></td>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;">${TEST_PROJECT_NAME}</td>
      </tr>
      <tr>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Deployment URL:</strong></td>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><a href="${TEST_DEPLOY_URL}">${TEST_DEPLOY_URL}</a></td>
      </tr>
      <tr>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Health Check:</strong></td>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><a href="${TEST_DEPLOY_URL}/fineract-provider/actuator/health">View Health</a></td>
      </tr>
      <tr>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Deployment Time:</strong></td>
        <td style="padding: 10px; border-bottom: 1px solid #ddd;">$(date)</td>
      </tr>
    </table>
    <div style="margin-top: 20px;">
      <a href="${TEST_DEPLOY_URL}" style="background-color: #0066cc; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px; margin-right: 10px;">View Application</a>
      <a href="https://railway.app/project/${TEST_DEPLOY_ID}" style="background-color: #5c5c5c; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px;">Railway Dashboard</a>
    </div>
  </div>
</body>
</html>
EOF
)

log "Email body created (HTML format)"

if [ "$SEND_MODE" = true ] && [ -n "$EMAIL_TEST_RECIPIENT" ] && [ -n "$EMAIL_SENDER" ] && [ -n "$EMAIL_SERVER" ]; then
  log "Preparing to send actual email notification..."
  # Save email body to a temporary file
  echo "$EMAIL_BODY" > test-email-body.html
  
  # Use mailx/mail command if available
  if command -v mailx &> /dev/null || command -v mail &> /dev/null; then
    log "Sending email using mail command..."
    (echo "Subject: 🚀 Railway Deployment: Success"; echo "Content-Type: text/html"; echo ""; cat test-email-body.html) | sendmail -f "$EMAIL_SENDER" "$EMAIL_TEST_RECIPIENT"
    log "Email notification sent!"
  else
    log "Email client not available. Install mailx or configure SMTP settings to send actual emails."
  fi
  
  # Clean up
  rm -f test-email-body.html
else
  log "Email notification not sent (simulation mode or missing email configuration)"
fi

# Test webhook notification
log "Testing webhook notification format..."
WEBHOOK_PAYLOAD=$(cat << EOF
{
  "deployment": {
    "status": "success",
    "environment": "${TEST_DEPLOY_ENV}",
    "branch": "${TEST_BRANCH_NAME}",
    "project": "${TEST_PROJECT_NAME}",
    "type": "production",
    "type_emoji": "🚀",
    "priority": "high",
    "url": "${TEST_DEPLOY_URL}",
    "health_check_url": "${TEST_DEPLOY_URL}/fineract-provider/actuator/health",
    "api_docs_url": "${TEST_DEPLOY_URL}/fineract-provider/swagger-ui/index.html",
    "service_id": "${TEST_DEPLOY_ID}",
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  }
}
EOF
)

log "Webhook payload created:"
echo "$WEBHOOK_PAYLOAD" | jq '.' 2>/dev/null || echo "$WEBHOOK_PAYLOAD"

if [ "$SEND_MODE" = true ] && [ -n "$CUSTOM_WEBHOOK_URL" ]; then
  log "Sending actual webhook notification..."
  curl -X POST -H "Content-Type: application/json" -d "$WEBHOOK_PAYLOAD" "$CUSTOM_WEBHOOK_URL"
  log "Webhook notification sent!"
else
  log "Webhook notification not sent (simulation mode or missing webhook URL)"
fi

# Clean up
rm -f test-deployment-summary.md test-deployment-failure.md

log "All notification tests completed successfully!"
echo ""
echo "To enable actual notification sending, run with the --send flag:"
echo "./scripts/test-notifications.sh --send"
echo ""
echo "Set the following environment variables to enable sending to each channel:"
echo "- SLACK_WEBHOOK_URL: For Slack notifications"
echo "- MS_TEAMS_WEBHOOK_URL: For MS Teams notifications"
echo "- EMAIL_TEST_RECIPIENT, EMAIL_SENDER, EMAIL_SERVER: For email notifications"
echo "- CUSTOM_WEBHOOK_URL: For custom webhook notifications"
echo ""
echo "To enable additional notification channels in the CI/CD workflow, edit:"
echo ".github/workflows/railway-deployment.yml"
echo ""
echo "For more information, see:"
echo "docs/RAILWAY-NOTIFICATIONS.md"
