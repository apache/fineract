#!/bin/bash
# Script to create Issue 38 code-quality and architecture issues in andrew-nkhoma/fineract
# Run: bash scripts/create-issue38-issues.sh

set -e

REPO="andrew-nkhoma/fineract"

echo "Creating Issue 38 code-quality and architecture issues in $REPO ..."

# Issue 1
gh issue create --repo "$REPO" \
  --title "Bug: getSessionSummary() queries transactions by cashier+date instead of sessionId" \
  --label "bug" \
  --body '## Problem

`CashierSessionReadPlatformServiceImpl.getSessionSummary()` computes cash-in and cash-out totals using `cashier_id + session_date` as the filter. This means if two sessions exist for the same cashier on the same date (e.g., morning/afternoon shift), they share totals — producing wrong summary figures for both.

### Code Reference
- File: `fineract-provider/src/main/java/org/apache/fineract/organisation/teller/service/CashierSessionReadPlatformServiceImpl.java`
- Method: `getSessionSummary()` — lines ~117–140

```java
final String cashInSql = "select coalesce(sum(ct.txn_amount), 0) from m_cashier_transactions ct "
    + "where ct.cashier_id = ? and ct.txn_date &gt;= ? and ct.txn_type in (1, 101, 102)";
// ❌ Filter is by cashier + date, not by cashier_session_id
```

### Fix Required
- Add a `cashier_session_id` column to `m_cashier_transactions` (see Issue #5 for loan/savings FKs)
- Rewrite the cash-in/cash-out SQL to filter by `cashier_session_id` instead of cashier+date
- Update `CashierTransaction.java` JPA entity to include `@ManyToOne CashierSession cashierSession`
- Populate `cashier_session_id` when creating transactions inside an active session

### Depends On
- Enhancement: Add `m_cashier_sessions` table (#4)'

echo "✅ Issue 1 created"

# Issue 2
gh issue create --repo "$REPO" \
  --title "Bug: closeSession() variance calculation uses incorrect transaction type IDs and is not session-scoped" \
  --label "bug" \
  --body '## Problem

`CashierSessionWritePlatformServiceImpl.closeSession()` calculates variance using `CashierTxnType.INWARD_CASH_TXN.getId()` and `OUTWARD_CASH_TXN.getId()` as the `txnType` filter. These IDs are `103` and `104`. But `getSessionSummary()` in the read service uses `txn_type in (1, 101, 102)` for cash-in and `(2, 201, 202)` for cash-out — completely inconsistent sets. Additionally, both queries filter by `cashier_id + date`, not by `cashier_session_id`, so multi-session days produce wrong variance.

### Code References

**Write service (closeSession):**
- File: `fineract-provider/.../service/CashierSessionWritePlatformServiceImpl.java` lines ~130–145
```java
final BigDecimal sumCashIn = cashierTransactionRepository
    .sumAmountByCashierAndTxnTypeAndDate(cashierId, CashierTxnType.INWARD_CASH_TXN.getId(), sessionDate); // txnType = 103
```

**Read service (getSessionSummary):**
- File: `fineract-provider/.../service/CashierSessionReadPlatformServiceImpl.java` lines ~119–122
```java
"where ct.cashier_id = ? and ct.txn_date &gt;= ? and ct.txn_type in (1, 101, 102)"  // completely different IDs!
```

### Fix Required
- Agree on a single canonical set of `txn_type` IDs for cash-in vs cash-out
- Make both `closeSession()` and `getSessionSummary()` use `cashier_session_id` as the filter (not cashier+date)
- Add `sumAmountBySessionAndTxnType()` method to `CashierTransactionRepository`

### Depends On
- Bug: Fix `getSessionSummary()` to use sessionId (#N)'

echo "✅ Issue 2 created"

# Issue 3
gh issue create --repo "$REPO" \
  --title "Bug/Cleanup: Remove dead entityType code block from doTransactionForCashier()" \
  --label "bug" \
  --body '## Problem

`TellerWritePlatformServiceJpaImpl.doTransactionForCashier()` contains a block of ~20 lines of commented-out code that is explicitly marked with `// TODO: can we please remove this whole block?!?`. The code does nothing — every branch is either empty or commented out. It reads `entityType` from the command but then performs no validation whatsoever.

### Code Reference
- File: `fineract-provider/src/main/java/org/apache/fineract/organisation/teller/service/TellerWritePlatformServiceJpaImpl.java`
- Lines ~376–400

```java
// TODO: can we please remove this whole block?!? this is 20 lines of dead code!!!
final String entityType = command.stringValueOfParameterNamed("entityType");
if (entityType != null) {
    if (entityType.equals("loan account")) {
        // TODO : Check if loan account exists
        // LoanAccount loan = null;
        // if (loan == null) { throw new LoanAccountFoundException(entityId); }
    } else if (entityType.equals("savings account")) {
        // TODO : Check if loan account exists
        // SavingsAccount savingsaccount = null;
    }
    if (entityType.equals("client")) {
        // TODO: Check if client exists
    } else {
        // TODO : Invalid type handling
    }
}
```

### Fix Required
- Delete the entire `entityType` block from `doTransactionForCashier()`
- If entity validation is genuinely needed in the future, implement it properly (not as dead commented code)'

echo "✅ Issue 3 created"

# Issue 4
gh issue create --repo "$REPO" \
  --title "Enhancement: allocateCashToCashier() is missing the cashier transaction data validation that settleCashFromCashier() performs" \
  --label "enhancement" \
  --body '## Problem

`settleCashFromCashier()` calls `this.cashierTransactionDataValidator.validateSettleCashAndCashOutTransactions(cashierId, command)` before delegating to `doTransactionForCashier()`. But `allocateCashToCashier()` calls `doTransactionForCashier()` directly with **no pre-validation**. Both operations should validate the cashier has sufficient balance / active session before proceeding.

### Code Reference
- File: `fineract-provider/src/main/java/org/apache/fineract/organisation/teller/service/TellerWritePlatformServiceJpaImpl.java`

```java
// allocateCashToCashier — no pre-validation:
@Override
public CommandProcessingResult allocateCashToCashier(final Long cashierId, JsonCommand command) {
    return doTransactionForCashier(cashierId, CashierTxnType.ALLOCATE, command); // ❌ no validator call
}

// settleCashFromCashier — has pre-validation:
@Override
public CommandProcessingResult settleCashFromCashier(final Long cashierId, JsonCommand command) {
    this.cashierTransactionDataValidator.validateSettleCashAndCashOutTransactions(cashierId, command); // ✅
    return doTransactionForCashier(cashierId, CashierTxnType.SETTLE, command);
}
```

### Fix Required
- Add a corresponding validator method `validateAllocateCashTransactions(cashierId, command)` to `CashierTransactionDataValidator`
- Call it from `allocateCashToCashier()` before delegating to `doTransactionForCashier()`
- The validation should at minimum check: cashier exists, cashier assignment is not expired, currency code is valid'

echo "✅ Issue 4 created"

# Issue 5
gh issue create --repo "$REPO" \
  --title "Bug: CashierWritePlatformService stub returns null for all operations — either implement or delete" \
  --label "bug" \
  --body '## Problem

`fineract-branch/.../service/CashierWritePlatformService.java` is a concrete class (not an interface) with three methods that all return `null` and contain `// TODO Auto-generated method stub` comments. It is not wired up to any handler, not injected anywhere, and its presence alongside the working `TellerWritePlatformService` creates confusion about which class is the correct one to use.

### Code Reference
- File: `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/service/CashierWritePlatformService.java`

```java
public class CashierWritePlatformService {

    public CommandProcessingResult allocateCashierToTeller(JsonCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

    public CommandProcessingResult deleteCashier(Long entityId) {
        // TODO Auto-generated method stub
        return null;
    }

    public CommandProcessingResult modifyCashier(Long entityId, JsonCommand command) {
        // TODO Auto-generated method stub
        return null;
    }
}
```

### Fix Required
**Option A (preferred):** Delete `CashierWritePlatformService.java` entirely if it is not intended to be used — all cashier write operations are already handled by `TellerWritePlatformServiceJpaImpl`.

**Option B:** If a dedicated cashier service is planned, convert it to a proper interface, create an implementation, and wire it into the relevant command handlers.'

echo "✅ Issue 5 created"

# Issue 6
gh issue create --repo "$REPO" \
  --title "Enhancement: CashierSessionApiResource endpoints have no @Permission checks" \
  --label "enhancement" \
  --body '## Problem

All endpoints in `CashierSessionApiResource.java` (open session, close session, get active session, list sessions, get summary, branch dashboard) call service methods directly without any `context.authenticatedUser()` permission check or `@Permission` annotation. Any authenticated user can open, close, or view any cashier session in any office.

### Code Reference
- File: `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/api/CashierSessionApiResource.java`

The `openSession()` endpoint passes the `currencyCode` directly from the request body and delegates to `commandsSourceWritePlatformService`. But read endpoints like `getActiveSession()`, `getAllSessions()`, `getSessionSummary()`, and `getBranchDashboard()` call read services directly with no office-hierarchy or role check.

### Fix Required
- Add `context.authenticatedUser()` at the top of each read endpoint method
- Validate that the requesting user'"'"'s office is in the hierarchy of the teller'"'"'s office (reuse `validateUserPriviledgeOnTellerAndRetrieve()` pattern from `TellerWritePlatformServiceJpaImpl`)
- Add explicit permission codes (e.g., `READ_CASHIERSESSION`, `CREATE_CASHIERSESSION`, `CLOSE_CASHIERSESSION`) to the permission table and reference them in a `@Permission` annotation or platform security check'

echo "✅ Issue 6 created"

# Issue 7
gh issue create --repo "$REPO" \
  --title "Enhancement: Replace hardcoded txn_type integers in getSessionSummary SQL with named constants" \
  --label "enhancement" \
  --body '## Problem

`CashierSessionReadPlatformServiceImpl.getSessionSummary()` uses raw integer literals `(1, 101, 102)` and `(2, 201, 202)` in SQL to identify cash-in and cash-out transaction types. These numbers have no documentation and do not correspond to the `CashierTxnType` constants (`ALLOCATE=101`, `SETTLE=102`, `INWARD_CASH_TXN=103`, `OUTWARD_CASH_TXN=104`). The values `1` and `2` appear to refer to loan/savings transaction type enums from unrelated tables — this mix is undocumented and fragile.

### Code Reference
- File: `fineract-provider/.../service/CashierSessionReadPlatformServiceImpl.java` lines ~119–122

```java
final String cashInSql = "select coalesce(sum(ct.txn_amount), 0) from m_cashier_transactions ct "
    + "where ct.cashier_id = ? and ct.txn_date &gt;= ? and ct.txn_type in (1, 101, 102)";

final String cashOutSql = "select coalesce(sum(ct.txn_amount), 0) from m_cashier_transactions ct "
    + "where ct.cashier_id = ? and ct.txn_date &gt;= ? and ct.txn_type in (2, 201, 202)";
// ❌ What are 1, 2, 201, 202? Undocumented. Inconsistent with CashierTxnType enum.
```

### Fix Required
- Define the canonical set of `txnType` IDs that represent "cash in" and "cash out" on the cashier transaction table
- Replace literals with `CashierTxnType.INWARD_CASH_TXN.getId()` / `CashierTxnType.OUTWARD_CASH_TXN.getId()` (or a dedicated helper)
- Add a code comment explaining which transaction types map to cash-in vs cash-out
- Cross-check with `TellerManagementReadPlatformServiceImpl.cashierTxnSummarySchema()` which uses the same magic numbers for consistency'

echo "✅ Issue 7 created"

# Issue 8
gh issue create --repo "$REPO" \
  --title "Enhancement: Make summaryandtransactions endpoint session-aware (filter by cashier_session_id)" \
  --label "enhancement" \
  --body '## Problem

The `GET /tellers/{id}/cashiers/{cashierId}/summaryandtransactions` endpoint returns a combined summary + transaction list for a cashier, but it aggregates **all** transactions for the cashier in the given currency, regardless of session. Once session-based tracking is in place (Issue #4, #5), callers need to be able to request the summary and transactions for a **specific session**, not just for the entire cashier history.

### Current Behaviour
`summaryandtransactions` returns totals computed from all `m_cashier_transactions` for the cashier in the given currency code and date range. There is no way to isolate a single session'"'"'s figures.

### Required Change

**Option A:** Add an optional `sessionId` query parameter to the existing endpoint:
```
GET /tellers/{id}/cashiers/{cashierId}/summaryandtransactions?sessionId=42&amp;currencyCode=ZMK
```

**Option B:** Use the new session summary endpoint (`GET /tellers/{id}/cashiers/{cashierId}/sessions/{sId}/summary`) as the canonical session-scoped view, and document that `summaryandtransactions` is the legacy all-time view.

### Deliverables
- Decision documented in code comment or API description
- If Option A: add `@QueryParam("sessionId")` and filter SQL by `cashier_session_id` when provided
- If Option B: mark `summaryandtransactions` as deprecated in the OpenAPI annotation

### Depends On
- Enhancement: Add `m_cashier_sessions` table (#4)
- Enhancement: Add `cashier_session_id` FK to transaction tables (#5)
- Bug: Fix `getSessionSummary()` to use sessionId (Issue #1 in this script)'

echo "✅ Issue 8 created"

echo ""
echo "🎉 All 8 issues created in $REPO"
