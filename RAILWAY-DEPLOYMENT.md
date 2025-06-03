# Railway Deployment Guide for Apache Fineract

This guide explains how to deploy Apache Fineract on Railway using PostgreSQL and Dragonfly (Redis-compatible).

## Overview

The deployment architecture consists of:

1. **Fineract Java Application** - The main application running on Java 21 (Temurin)
2. **PostgreSQL Database** - For storing tenant and application data
3. **Dragonfly (Redis-compatible)** - For caching and session management

## Prerequisites

- GitHub account
- Railway account (https://railway.app)
- Git installed locally
- Java 21 (Temurin) for local development
- Gradle 8.1.3 for local builds

## Deployment Options

### Option 1: GitHub-Connected Deployment (Recommended)

1. Fork this repository on GitHub
2. Log in to Railway
3. Create a new project and select "Deploy from GitHub repo"
4. Choose your forked repository
5. Configure the environment variables as needed (Railway will automatically set up most of them)
6. Wait for the deployment to complete

### Option 2: Manual Deployment

1. Clone this repository
2. Navigate to the project directory
3. Run the setup script:
   ```bash
   ./railway-setup-complete.sh
   ```
4. Deploy the application:
   ```bash
   ./railway-deploy.sh
   ```

## Environment Variables

Railway automatically provides environment variables for PostgreSQL and Redis services. The application is configured to use these variables:

- `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` - PostgreSQL connection details
- `REDISHOST`, `REDISPORT`, `REDISPASSWORD` - Dragonfly connection details

Additional environment variables you may want to configure:

- `JAVA_OPTS` - JVM options (default: `-Xmx1G -Xms512M -XX:+UseG1GC -XX:+UseStringDeduplication`)
- `SPRING_PROFILES_ACTIVE` - Spring profiles (default: `railway,postgresql`)
- `SENTRY_DSN` - Sentry error tracking DSN
- `TENANT_DB_NAME` - The name of the tenant database (default: `fineract_default`)

## GitHub Actions CI/CD

This repository includes a GitHub Actions workflow that automatically builds, tests, and deploys the application to Railway when you push to certain branches:

- `main` - Production environment
- `develop` - Staging environment
- `feature/*` - Preview environments
- `fix/*` - Preview environments

The workflow performs the following steps:

1. Validates the environment and determines if deployment is needed
2. Runs code linting (Checkstyle, SpotBugs, Spotless)
3. Executes unit tests with JaCoCo code coverage
4. Builds the application
5. Deploys to Railway (if on a deployable branch)
6. Verifies the deployment health

### Setting up GitHub Actions

1. In your GitHub repository, go to Settings > Secrets and variables > Actions
2. Add a new repository secret named `RAILWAY_TOKEN` with your Railway API token
   - You can get this token by running `railway login --browserless` locally

## Monitoring and Maintenance

### Health Checks

Railway automatically performs health checks using the endpoint specified in `railway.toml`:

```
healthcheckPath = "/fineract-provider/actuator/health"
```

You can also manually check the health of your deployment:

```bash
./railway-health-check.sh
```

### Logs

View logs with the Railway CLI:

```bash
railway logs
```

Or through the Railway dashboard.

### Database Management

Access the PostgreSQL database:

```bash
railway postgres
```

### Redis/Dragonfly Management

Access the Redis/Dragonfly instance:

```bash
railway redis
```

## Error Tracking with Sentry

Sentry integration is available for error tracking:

1. Create a Sentry account and project
2. Add your Sentry DSN as an environment variable in Railway: `SENTRY_DSN`
3. Errors will automatically be reported to your Sentry dashboard

See `config/railway/SENTRY-SETUP.md` for detailed setup instructions.

## Development Environment

A VS Code devcontainer configuration is provided for local development. It includes:

- Java 21 (Temurin)
- Gradle 8.1.3
- PostgreSQL
- Required VS Code extensions

To use it:

1. Open the project in VS Code
2. Install the "Remote - Containers" extension
3. Click "Reopen in Container" when prompted
4. Wait for the container to build

## Troubleshooting

### Common Issues

1. **Deployment fails**: Check the logs with `railway logs`
2. **Database connection issues**: Verify PostgreSQL is provisioned correctly
3. **Redis connection issues**: Verify Dragonfly is provisioned correctly
4. **Out of memory errors**: Adjust `JAVA_OPTS` with appropriate memory settings

### Getting Help

If you encounter any issues, please:

1. Check the Railway dashboard for service status
2. Review the logs using `railway logs`
3. Open an issue on GitHub with detailed information

## Health Checks

The application includes health checks for both the main service and its dependencies. To run a health check:

```bash
./railway-health-check.sh
```

This will check:
- Application status
- PostgreSQL connection
- Redis/Dragonfly connection

## CI/CD Pipeline

This repository includes GitHub Actions workflows that:

1. Validate the codebase
2. Run linting tools (Checkstyle, SpotBugs, Spotless)
3. Run unit tests with JaCoCo code coverage
4. Build the application
5. Deploy to Railway (if on main branch)

## Monitoring

The application is configured with:

1. **Sentry** for error tracking and performance monitoring
2. **Spring Boot Actuator** for health checks and metrics
3. **Prometheus endpoints** for metrics collection

## Local Development

For local development with VS Code:

1. Open the project in VS Code
2. Use the Dev Container configuration to set up a consistent environment
3. Run the application with Railway configuration:
   ```bash
   ./gradlew :fineract-provider:bootRun --args='--spring.profiles.active=railway,postgresql --spring.config.additional-location=classpath:/,file:config/railway/'
   ```

## Troubleshooting

If you encounter issues with the deployment:

1. Check the application logs:
   ```bash
   railway logs
   ```
2. Verify the health status:
   ```bash
   ./railway-health-check.sh
   ```
3. Check that all services are running in the Railway dashboard

## Additional Resources

- [Railway Documentation](https://docs.railway.app/)
- [Apache Fineract Documentation](https://fineract.apache.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Dragonfly Documentation](https://github.com/dragonflydb/dragonfly)