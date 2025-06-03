# Sentry Configuration for Railway Deployment

This document outlines how to set up Sentry for error monitoring in your Fineract deployment on Railway.

## What is Sentry?

Sentry is an error monitoring platform that helps developers track and fix crashes in real-time. It provides detailed error reports, performance monitoring, and user feedback.

## Setup Instructions

1. **Create a Sentry Account**:
   - Go to [https://sentry.io/signup/](https://sentry.io/signup/)
   - Create a new account or use your existing one

2. **Create a New Project**:
   - Select "Java" as your platform
   - Name your project "Fineract-Railway"

3. **Get Your DSN**:
   - After creating the project, you'll be given a DSN (Data Source Name)
   - It looks like: `https://examplePublicKey@o0.ingest.sentry.io/0`

4. **Add the DSN to Railway**:
   - In your Railway project, add the following environment variable:
   ```
   SENTRY_DSN=your-dsn-here
   ```

5. **Verify Installation**:
   - Restart your application
   - Check Sentry dashboard to see if the integration is working

## Configuring Error Levels

The application is configured to:
- Send ERROR level events to Sentry
- Record INFO level breadcrumbs
- Sample 10% of traces for performance monitoring

You can adjust these settings in the `application-railway.properties` file.

## Additional Configuration

For more advanced configuration options, refer to the [Sentry Java SDK documentation](https://docs.sentry.io/platforms/java/).

## Troubleshooting

If you don't see events in Sentry:
1. Check that the DSN is correctly set in Railway
2. Verify that the application is generating errors (try accessing a non-existent endpoint)
3. Check application logs for any Sentry initialization errors
