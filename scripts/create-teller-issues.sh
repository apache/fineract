#!/bin/bash
# Script to create Teller Cash Management issues in andrew-nkhoma/fineract
# Run: bash scripts/create-teller-issues.sh

set -e

REPO="andrew-nkhoma/fineract"

echo "Creating Teller Cash Management issues in $REPO ..."

# Issue 1
gh issue create --repo "$REPO" \
  --title "Bug: PUT /tellers drops debitAccountId and creditAccountId" \
  --label "bug" \
  --body '## Problem

The `Teller.update()` method in `Teller.java` does not handle updating `debitAccountId` and `creditAccountId` from a PUT payload. Requests to update these fields via `PUT /tellers/{id}` are silently ignored, even though the fields exist in the database (`debit_account_id`, `credit_account_id` on `m_tellers`) and on the JPA entity.

### Code Reference
- File: `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/domain/Teller.java`
- Method: `update()` — lines 94–153
- The method handles: `officeId`, `name`, `description`, `startDate`, `endDate`, `status`
- ❌ Never reads or persists `debitAccountId` or `creditAccountId`

```java
// In Teller.update(), only these fields are handled:
officeId, name, description, startDate, endDate, status
// ❌ debitAccountId and creditAccountId are completely absent
```

### Workaround
Direct SQL only:
```sql
UPDATE m_tellers SET debit_account_id = ?, credit_account_id = ? WHERE id = ?;
```

### Fix Required
- Update `Teller.update()` to read and persist `debitAccountId` and `creditAccountId` (look up via `GLAccountRepository`)
- Update `TellerCommandFromApiJsonDeserializer` to allow and validate these fields
- Update the PUT request DTO to include these fields

### Spec Reference
Spec §2.5 — API Bugs Discovered, Phase 2'

echo "✅ Issue 1 created"

# Issue 2
gh issue create --repo "$REPO" \
  --title "Bug: fromDate/toDate query parameters are ignored on cashier transactions endpoints" \
  --label "bug" \
  --body '## Problem

The endpoints `/tellers/{id}/cashiers/{cashierId}/transactions` and `/tellers/{id}/cashiers/{cashierId}/summaryandtransactions` accept `currencyCode` but completely ignore `fromDate` and `toDate` — they are not even declared as `@QueryParam` in the API resource. The SQL always uses the cashier assignment dates (`c.start_date` / `c.end_date`) regardless of what is passed in the URL.

### Code References

**API layer — `fromDate`/`toDate` not declared as `@QueryParam`:**
- File: `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/api/TellerApiResource.java`
- Method: `getTransactionsForCashier()` — `fromDate`/`toDate` are absent from the method signature

**Service layer — params never bound into SQL:**
- File: `fineract-provider/src/main/java/org/apache/fineract/organisation/teller/service/TellerManagementReadPlatformServiceImpl.java`
- Method: `retrieveCashierTransactions()` — lines 314–354
- `fromDate` and `toDate` exist in the method signature but are **never referenced** in the SQL string or params array:

```java
// Params array — fromDate/toDate completely absent:
Object[] params = new Object[] { cashierId, currencyCode, cashierId, currencyCode, cashierId, currencyCode, cashierId, currencyCode };
```

**SQL always uses cashier assignment dates:**
```java
"and sav_txn.transaction_date between c.start_date and " + nextDay   // c.start_date / c.end_date — never the caller'"'"'s dates
"and loan_txn.transaction_date between c.start_date and " + nextDay
```

### Example
Calling:
```
GET /tellers/1/cashiers/1/transactions?currencyCode=ZMK&fromDate=01 March 2026&toDate=31 March 2026&dateFormat=dd MMMM yyyy&locale=en
```
Returns the same result as calling with `currencyCode=ZMK` only. The date range parameters do nothing.

### Impact
It is impossible to narrow a cashier'"'"'s transactions to a sub-range of their assignment period. All queries always return the full assignment window.

### Fix Required
- Add `@QueryParam("fromDate")` and `@QueryParam("toDate")` (with `@QueryParam("dateFormat")` and `@QueryParam("locale")`) to `getTransactionsForCashier()` and `getTransactionsWithSummaryForCashier()` in `TellerApiResource.java`
- Pass the parsed dates to the service method
- Bind them into the SQL WHERE clauses, replacing `c.start_date`/`c.end_date` when provided

### Spec Reference
Spec §2.5 — API Bugs Discovered, Phase 2'

echo "✅ Issue 2 created"

# Issue 3
gh issue create --repo "$REPO" \
  --title "Bug: m_cashiers unique constraint (staff_id, teller_id) prevents cashier re-assignment to the same teller" \
  --label "bug" \
  --body '## Problem

The `m_cashiers` table enforces a unique constraint on `(staff_id, teller_id)`, meaning a staff member can only ever be assigned to a given teller **once in the database'"'"'s lifetime**. Re-assigning the same cashier to the same teller on a new date period is impossible without deleting the old record first.

### Code Reference
- File: `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/domain/Cashier.java`
- Lines 48–51:

```java
@Table(name = "m_cashiers", uniqueConstraints = {
    @UniqueConstraint(name = "ux_cashiers_staff_teller", columnNames = { "staff_id", "teller_id" }) })
```

### Real-world Impact
If Jasinta is assigned to Teller 1 for March, she cannot be assigned to Teller 1 again in April without deleting the March assignment. This blocks normal shift rotation in any real MFI environment.

### Fix Required
- Drop the unique constraint `ux_cashiers_staff_teller` from `m_cashiers`
- Provide a Liquibase migration to remove the constraint
- Rely on the existing date-overlap check in `CashierTransactionDataValidator.validateCashierAllowedDateAndTime()` as the only guard against duplicate active assignments

### Spec Reference
Spec §2.1 — Data Model Limitations, Phase 2'

echo "✅ Issue 3 created"

# Issue 4
gh issue create --repo "$REPO" \
  --title "Enhancement: Add m_cashier_sessions table and JPA entity" \
  --label "enhancement" \
  --body '## Feature: Cashier Session as a First-Class Entity

Introduce a `m_cashier_sessions` table and corresponding JPA entity. This is the **foundational change** for the entire session-aware teller redesign. All subsequent issues (GL routing, transaction isolation, session API endpoints) depend on this.

### Background
The current teller module tracks transactions by user and date range, not by session. There is no concept of a "session" — a cashier opens, processes transactions, and settles, but none of that is recorded as a discrete event with a start/end boundary. This causes reconciliation failures in multi-cashier, rotating-shift environments (see Spec §2.4).

### Schema
```sql
CREATE TABLE m_cashier_sessions (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  cashier_id            BIGINT NOT NULL,
  teller_id             BIGINT NOT NULL,
  user_id               BIGINT NOT NULL,
  office_id             BIGINT NOT NULL,
  session_date          DATE NOT NULL,
  opened_at             TIMESTAMP NOT NULL,
  closed_at             TIMESTAMP NULL,
  opening_allocation    DECIMAL(19,6) NOT NULL DEFAULT 0,
  total_settled         DECIMAL(19,6) NOT NULL DEFAULT 0,
  status                VARCHAR(20) NOT NULL,  -- OPEN, SETTLED, CLOSED
  opening_txn_id        BIGINT NULL,
  closing_txn_id        BIGINT NULL,
  currency_code         VARCHAR(3) NOT NULL,
  created_by            BIGINT NOT NULL,
  created_date          TIMESTAMP NOT NULL
);
```

### Deliverables
- Liquibase changeset for `m_cashier_sessions`
- `CashierSession.java` JPA entity
- `CashierSessionRepository.java`
- `CashierSessionStatus` enum: `OPEN`, `SETTLED`, `CLOSED`

### Business Rules (from Spec §3.5.1)
- A cashier can only have ONE open session per day per teller
- A user cannot open a new session if they have an unsettled session from a previous day

### Depends On
- Issue: Relax or remove `m_cashiers` unique constraint (#3)

### Spec Reference
Spec §3.2.1, §3.3 — New Data Model, Phase 3'

echo "✅ Issue 4 created"

# Issue 5
gh issue create --repo "$REPO" \
  --title "Enhancement: Add cashier_session_id FK to m_loan_transactions and m_savings_account_transaction" \
  --label "enhancement" \
  --body '## Feature: Link Transactions to Cashier Sessions

Add a `cashier_session_id` nullable foreign key column to both `m_loan_transactions` and `m_savings_account_transaction`. This is what enables true per-session transaction isolation and accurate session-level reconciliation.

### Background
Currently, loan repayments and savings transactions have **no linkage to a teller or cashier session**. They are only linked to the `m_appuser` who created them. This means:
- Transaction queries are by user + date range, not by session
- Rotating cashiers see each other'"'"'s transactions if date ranges overlap
- The teller summary screen is a user activity report, not a GL position

### Schema
```sql
ALTER TABLE m_loan_transactions
  ADD COLUMN cashier_session_id BIGINT NULL,
  ADD CONSTRAINT fk_loan_txn_cashier_session
    FOREIGN KEY (cashier_session_id) REFERENCES m_cashier_sessions(id);

ALTER TABLE m_savings_account_transaction
  ADD COLUMN cashier_session_id BIGINT NULL,
  ADD CONSTRAINT fk_sav_txn_cashier_session
    FOREIGN KEY (cashier_session_id) REFERENCES m_cashier_sessions(id);
```

### Deliverables
- Liquibase changeset for both ALTER statements
- Update `LoanTransaction.java` JPA entity to include `@ManyToOne CashierSession cashierSession`
- Update `SavingsAccountTransaction.java` JPA entity similarly

### Depends On
- Issue: Add `m_cashier_sessions` table and JPA entity (#4)

### Spec Reference
Spec §3.2.2 — Alter existing tables, Phase 3'

echo "✅ Issue 5 created"

# Issue 6
gh issue create --repo "$REPO" \
  --title "Enhancement: Implement Cashier Session lifecycle API endpoints" \
  --label "enhancement" \
  --body '## Feature: Cashier Session API Endpoints

Implement the full cashier session lifecycle via new REST API endpoints. These replace the implicit user-tracking approach with explicit session management.

### New Endpoints Required

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/tellers/{id}/cashiers/{cId}/sessions` | Open a new cashier session |
| `GET` | `/tellers/{id}/cashiers/{cId}/sessions/active` | Get the currently open session |
| `GET` | `/tellers/{id}/cashiers/{cId}/sessions` | List all sessions (paginated) |
| `POST` | `/tellers/{id}/cashiers/{cId}/sessions/{sId}/close` | Close session, post variance entry if applicable |
| `GET` | `/tellers/{id}/cashiers/{cId}/sessions/{sId}/summary` | Full session summary with GL reconciliation |
| `GET` | `/users/{id}/session/active` | Resolve active session for logged-in user |
| `GET` | `/tellers/branch/{officeId}/dashboard` | Supervisor view — all open sessions and positions |

### Session Lifecycle (Spec §3.3)

| Step | Action | GL Entry |
|------|--------|----------|
| 1 | Open Session | None — record created |
| 2 | Allocate Cash | DR 11140 Teller Cash / CR 11130 Vault |
| 3 | Process Transactions | Auto-linked to open session |
| 4 | Settle Cash | DR 11130 Vault / CR 11140 Teller Cash |
| 5 | Close Session | Variance journal if applicable |

### Business Rules (Spec §3.5.1)
- One open session per cashier per day per teller
- Cannot open if prior day has unsettled session
- Allocations only on OPEN sessions
- Settlements only on sessions with at least one allocation

### Transaction Blocking Rules (Spec §3.5.2)

| Transaction Type | OPEN | SETTLED | No Session |
|-----------------|------|---------|------------|
| Cash Repayment | ✅ Allow | ⚠ Warn | ✅ Allow (vault) |
| Cash Disbursement | ✅ Allow | 🚫 Block | ✅ Allow (bank) |
| Bank/Mobile Wallet | ✅ Allow | ✅ Allow | ✅ Allow |
| New Cash Allocation | ✅ Allow (top-up) | 🚫 Block | 🚫 Block |
| Settlement | ✅ Allow | ✅ Allow | 🚫 Block |

### Deliverables
- `CashierSessionApiResource.java`
- `CashierSessionWritePlatformService.java` + implementation
- `CashierSessionReadPlatformService.java` + implementation
- Command wrapper additions in `CommandWrapperBuilder`
- `CashierSessionData.java` DTO
- `CashierSessionSummaryData.java` DTO

### Depends On
- Issue: Add `m_cashier_sessions` table (#4)
- Issue: Add `cashier_session_id` FK to transaction tables (#5)

### Spec Reference
Spec §3.3, §4 — Session Lifecycle + API Endpoints, Phase 4'

echo "✅ Issue 6 created"

# Issue 7
gh issue create --repo "$REPO" \
  --title "Bug/Enhancement: Route cash GL through Teller Cash (11140) when a cashier session is open" \
  --label "bug" --label "enhancement" \
  --body '## Problem + Feature: GL Routing for Cash Transactions

Currently, all cash repayments and disbursements post to `11130 Cash on Hand (Vault)` regardless of whether a cashier session is active. They should route through `11140 Teller Cash` when an active session exists.

### Current Behaviour (Wrong)
```
-- Cash repayment with active cashier session:
DEBIT  11130  Cash on Hand (Vault)    ← wrong, bypasses teller
CREDIT 11210  Gross Loan Portfolio
```

### Expected Behaviour
```
-- Cash repayment with active cashier session:
DEBIT  11140  Teller Cash             ← correct, routes through cashier drawer
CREDIT 11210  Gross Loan Portfolio

-- No active session (fallback):
DEBIT  11130  Cash on Hand (Vault)    ← correct fallback
CREDIT 11210  Gross Loan Portfolio
```

### Files to Update
- `fineract-provider/src/main/java/org/apache/fineract/accounting/journalentry/service/AccountingProcessorForLoan.java`
- `fineract-provider/src/main/java/org/apache/fineract/accounting/journalentry/service/AccountingProcessorForSavings.java`

### Required Logic (Spec §3.4)
```java
if (paymentDetail.isCashPayment()) {
    CashierSession activeSession = cashierSessionRepository
        .findOpenSessionByUser(currentUser.getId(), officeId, transactionDate);

    if (activeSession != null) {
        // Route through teller cash account (Financial Activity 102 = cashAtTeller)
        glAccount = financialActivityAccountRepository
            .findByFinancialActivity(CASH_AT_TELLER);  // 11140
        transaction.setCashierSessionId(activeSession.getId());
    } else {
        // Fall back to vault (Financial Activity 101 = cashAtMainVault)
        glAccount = financialActivityAccountRepository
            .findByFinancialActivity(CASH_AT_MAIN_VAULT);  // 11130
    }
}
```

### Financial Activity Mapping Required
| ID | Activity | GL Code | Account Name |
|----|----------|---------|--------------|
| 101 | cashAtMainVault | 11130 | Cash on Hand (Vault) |
| 102 | cashAtTeller | 11140 | Teller Cash — Head Office |

### Depends On
- Issue: Add `m_cashier_sessions` table (#4)
- Issue: Add `cashier_session_id` FK to transaction tables (#5)
- Issue: Implement Session API endpoints (#6)

### Spec Reference
Spec §2.3, §3.4 — GL Routing Fix, Phase 5'

echo "✅ Issue 7 created"

# Issue 8
gh issue create --repo "$REPO" \
  --title "Enhancement: Auto-post GL variance journal entry on unbalanced settlement" \
  --label "enhancement" \
  --body '## Feature: Variance Journal Entry on Settlement

When a cashier settles and the amount returned does not equal the expected cash on hand, the system must automatically calculate and post a correcting GL journal entry.

### Variance Formula (Spec §3.5.3)
```
Expected Cash = Opening Allocation + Cash In − Cash Out
Variance = Settled Amount − Expected Cash
```

### GL Entries

**Short settlement (cashier returns less than expected):**
```sql
-- Cashier owes the difference:
DEBIT  53920  Cash Shortage — Teller        [variance amount]
CREDIT 11140  Teller Cash — Head Office     [variance amount]
```

**Over settlement (cashier returns more than expected):**
```sql
-- Cashier returned too much:
DEBIT  11140  Teller Cash — Head Office     [variance amount]
CREDIT 43210  Miscellaneous Income          [variance amount]
```

### GL Accounts Required
| GL Code | Account Name | Type |
|---------|-------------|------|
| 53920 | Cash Shortage — Teller | Expense — Detail |
| 43210 | Miscellaneous Income | Income — Detail |

### Business Rules
- If variance ≠ 0, a mandatory supervisor note is required before settlement is accepted
- The correcting journal must be auto-posted as part of the session close flow
- Both overage and shortage must be handled

### Implementation Location
- `TellerWritePlatformServiceJpaImpl.java` — `settleCashFromCashier()` method
- Or new `CashierSessionWritePlatformServiceImpl.java` session close handler

### Depends On
- Issue: Implement Session API endpoints (#6)
- Issue: Fix GL routing through Teller Cash (#7)

### Spec Reference
Spec §3.5.3 — Variance Handling, Phase 7'

echo "✅ Issue 8 created"

# Issue 9
gh issue create --repo "$REPO" \
  --title "Bug: Cashier assignment expiry causes silent failure with no notification" \
  --label "bug" \
  --body '## Problem

When a cashier'"'"'s `end_date` on `m_cashiers` is reached, operations fail silently. No notification is sent, no warning is shown, and errors returned to users are unclear. Cashiers simply stop being able to operate without understanding why.

### Current Behaviour
- Cashier assignment expires silently at midnight on `end_date`
- Subsequent allocation, repayment, or settlement calls fail without a helpful error
- No API response distinguishes "cashier assignment expired" from other errors
- No advance warning when expiry is approaching

### Files Involved
- `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/domain/Cashier.java`
- `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/service/TellerWritePlatformServiceJpaImpl.java` — allocation/settlement handlers
- `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/api/TellerApiResource.java`

### Fix Required
- On any teller operation (allocate, transact, settle), check if `cashier.getEndDate()` is in the past and return a clear `422 Unprocessable Entity` with message: `"Cashier assignment for [name] expired on [date]. Please renew the assignment."`
- On cashier retrieval (`GET /tellers/{id}/cashiers/{cId}`), include an `expiryWarning` flag in the response if `end_date` is within 7 days
- Optional: add a scheduled job or startup check to flag assignments expiring within 7 days

### Spec Reference
Spec §7 — Known Limitations, Phase 9'

echo "✅ Issue 9 created"

# Issue 10
gh issue create --repo "$REPO" \
  --title "Enhancement: Multi-teller concurrent session support" \
  --label "enhancement" \
  --body '## Feature: Allow Cashier to Hold Concurrent Sessions on Multiple Tellers

The proposed session model (Issue #6) assumes one active session per cashier per day. To support cashiers rotating between multiple teller stations within the same day, the session model needs to permit concurrent sessions on different tellers.

### Background
In some MFI branch configurations, a staff member may operate at multiple teller windows in a single day. Under the current session model (one-session-per-cashier-per-day), this is not supported.

### Required Changes

**Session uniqueness rule change:**
- Current proposed rule: one open session per cashier per day
- New rule: one open session per cashier **per teller** per day

**Session lookup change:**
```java
// Current (to be built in Issue #6):
cashierSessionRepository.findOpenSessionByUser(userId, officeId, date);

// Required (this issue):
cashierSessionRepository.findOpenSessionByUserAndTeller(userId, tellerId, officeId, date);
```

**GL routing change:**
The teller cash account (11140) will need to be resolved per session, not just per user, since different tellers may have different GL accounts configured.

### Deliverables
- Update `CashierSessionRepository.findOpenSessionByUser()` to accept `tellerId`
- Update session open validation to check uniqueness per `(cashier_id, teller_id, session_date)`
- Update GL routing in `AccountingProcessorForLoan` and `AccountingProcessorForSavings` to resolve teller account from the active session'"'"'s `teller_id`
- Add `teller_id` index on `m_cashier_sessions`

### Depends On
- Issue: Add `m_cashier_sessions` table (#4)
- Issue: Add `cashier_session_id` FK to transaction tables (#5)
- Issue: Session API endpoints (#6)
- Issue: GL routing fix (#7)

### Spec Reference
Spec §8 — Implementation Plan Phase 10'

echo "✅ Issue 10 created"

echo ""
echo "🎉 All 10 Teller Cash Management issues created in $REPO"
