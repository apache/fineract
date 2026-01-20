# Fineract Loans API Improvements

This document describes the improvements made to the Apache Fineract Loans API.

## 1. Fix: Loans Endpoint Pagination on MariaDB/MySQL

### Problem

The `GET /v1/loans` endpoint was returning `totalFilteredRecords: 0` even when loans existed in the database. This broke pagination in frontend applications.

**Example of broken response:**
```json
{
  "totalFilteredRecords": 0,
  "pageItems": [
    {
      "id": 1,
      "accountNo": "000000001"
    }
  ]
}
```

Other endpoints like `/v1/clients` and `/v1/groups` worked correctly on the same database.

### Root Cause

The pagination mechanism uses `SQL_CALC_FOUND_ROWS` and `SELECT FOUND_ROWS()` for MySQL/MariaDB databases. Due to the complexity of the loans query (multiple JOINs across offices, clients, groups, and transfer offices), `FOUND_ROWS()` was returning 0 instead of the actual count.

### Solution

Added a fallback mechanism in `PaginationHelper.java` that uses an explicit `COUNT(*)` query when `FOUND_ROWS()` returns 0 but items were actually retrieved.

### Files Modified

- `fineract-core/src/main/java/org/apache/fineract/infrastructure/core/service/PaginationHelper.java`

### Affected Database Versions

- MariaDB >= 11.5.2
- MySQL (all supported versions)

---

## 2. Feature: Add productId Filter to Loans Endpoint

### Description

Added a new `productId` query parameter to the `GET /v1/loans` endpoint to allow filtering loans by loan product.

### Usage

```
GET /v1/loans?productId=1
GET /v1/loans?productId=1&limit=10
GET /v1/loans?productId=1&clientId=5&status=300
```

### Files Modified

- `fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/api/LoansApiResource.java`
- `fineract-core/src/main/java/org/apache/fineract/infrastructure/core/service/SearchParameters.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanReadPlatformServiceImpl.java`

### API Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `productId` | Long | Filter loans by loan product ID |
| `clientId` | Long | Filter loans by client ID |
| `status` | String | Filter loans by status |
| `accountNo` | String | Filter by loan account number |
| `externalId` | String | Filter by external ID |
| `offset` | Integer | Pagination offset |
| `limit` | Integer | Pagination limit |
| `orderBy` | String | Field to order by |
| `sortOrder` | String | Sort order (ASC or DESC) |

---

## License

These changes are part of Apache Fineract and are licensed under the Apache License, Version 2.0.