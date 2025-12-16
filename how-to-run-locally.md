# Apache Fineract - Local Testing Guide

This guide provides comprehensive instructions on how to run local tests with APIs in the Apache Fineract repository.

## Table of Contents

1. [Local Development Setup](#local-development-setup)
2. [Integration Tests](#integration-tests)
3. [Cucumber E2E Tests](#cucumber-e2e-tests)
4. [API Testing with curl](#api-testing-with-curl)
5. [View API Documentation](#view-api-documentation)
6. [Troubleshooting](#troubleshooting)

---

## Local Development Setup

### Prerequisites

- **Java 21** (Azul Zulu JDK recommended)
- **Database**:  MariaDB >= 11.5.2, PostgreSQL >= 17.0, or MySQL >= 9.1
- **RAM**:  Minimum 16GB
- **CPU**: Minimum 8 cores
- **Git**: For source code management
- **Gradle 8.14. 3**:  Included via wrapper

### Starting Fineract Locally

Start the Fineract application in development mode:

```bash
# Create required databases
./gradlew createDB -PdbName=fineract_tenants
./gradlew createDB -PdbName=fineract_default

# Start Fineract in development mode
./gradlew devRun