# Teller Cash Management Redesign — Planned Issues

This document lists all 10 planned issues for the Teller Cash Management redesign. Issues are ordered from highest to lowest priority and in dependency order (bugs first, then enhancements).

---

## Summary Table

| # | Title | Type | Priority | Depends On |
|---|-------|------|----------|------------|
| 1 | Bug: PUT /tellers drops debitAccountId and creditAccountId | Bug | High | — |
| 2 | Bug: fromDate/toDate query parameters are ignored on cashier transactions endpoints | Bug | High | — |
| 3 | Bug: m_cashiers unique constraint (staff_id, teller_id) prevents cashier re-assignment to the same teller | Bug | High | — |
| 4 | Enhancement: Add m_cashier_sessions table and JPA entity | Enhancement | High | #3 |
| 5 | Enhancement: Add cashier_session_id FK to m_loan_transactions and m_savings_account_transaction | Enhancement | High | #4 |
| 6 | Enhancement: Implement Cashier Session lifecycle API endpoints | Enhancement | High | #4, #5 |
| 7 | Bug/Enhancement: Route cash GL through Teller Cash (11140) when a cashier session is open | Bug + Enhancement | High | #4, #5, #6 |
| 8 | Enhancement: Auto-post GL variance journal entry on unbalanced settlement | Enhancement | Medium | #6, #7 |
| 9 | Bug: Cashier assignment expiry causes silent failure with no notification | Bug | Medium | — |
| 10 | Enhancement: Multi-teller concurrent session support | Enhancement | Medium | #4, #5, #6, #7 |

---

## Issue 1 — Bug: PUT /tellers drops debitAccountId and creditAccountId

**Labels:** `bug`  
**Spec Reference:** §2.5 — API Bugs Discovered, Phase 2

### Problem

The `Teller.update()` method in `Teller.java` does not handle updating `debitAccountId` and `creditAccountId` from a PUT payload. Requests to update these fields via `PUT /tellers/{id}` are silently ignored, even though the fields exist in the database (`debit_account_id`, `credit_account_id` on `m_tellers`) and on the JPA entity.

### Code Reference
- File: `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/domain/Teller.java`
- Method: `update()` — lines 94–153
- The method handles: `officeId`, `name`, `description`, `startDate`, `endDate`, `status`
- ❌ Never reads or persists `debitAccountId` or `creditAccountId`

### Workaround
Direct SQL only:
```sql
UPDATE m_tellers SET debit_account_id = ?, credit_account_id = ? WHERE id = ?;
```

### Fix Required
- Update `Teller.update()` to read and persist `debitAccountId` and `creditAccountId` (look up via `GLAccountRepository`)
- Update `TellerCommandFromApiJsonDeserializer` to allow and validate these fields
- Update the PUT request DTO to include these fields

---

## Issue 2 — Bug: fromDate/toDate query parameters are ignored on cashier transactions endpoints

**Labels:** `bug`  
**Spec Reference:** §2.5 — API Bugs Discovered, Phase 2

### Problem

The endpoints `/tellers/{id}/cashiers/{cashierId}/transactions` and `/tellers/{id}/cashiers/{cashierId}/summaryandtransactions` accept `currencyCode` but completely ignore `fromDate` and `toDate` — they are not even declared as `@QueryParam` in the API resource. The SQL always uses the cashier assignment dates (`c.start_date` / `c.end_date`) regardless of what is passed in the URL.

### Code References

- File: `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/api/TellerApiResource.java`
  - Method: `getTransactionsForCashier()` — `fromDate`/`toDate` are absent from the method signature
- File: `fineract-provider/src/main/java/org/apache/fineract/organisation/teller/service/TellerManagementReadPlatformServiceImpl.java`
  - Method: `retrieveCashierTransactions()` — lines 314–354
  - `fromDate` and `toDate` exist in the method signature but are **never referenced** in the SQL string or params array

### Impact
It is impossible to narrow a cashier's transactions to a sub-range of their assignment period. All queries always return the full assignment window.

### Fix Required
- Add `@QueryParam("fromDate")` and `@QueryParam("toDate")` (with `@QueryParam("dateFormat")` and `@QueryParam("locale")`) to `getTransactionsForCashier()` and `getTransactionsWithSummaryForCashier()` in `TellerApiResource.java`
- Pass the parsed dates to the service method
- Bind them into the SQL WHERE clauses, replacing `c.start_date`/`c.end_date` when provided

---

## Issue 3 — Bug: m_cashiers unique constraint (staff_id, teller_id) prevents cashier re-assignment to the same teller

**Labels:** `bug`  
**Spec Reference:** §2.1 — Data Model Limitations, Phase 2

### Problem

The `m_cashiers` table enforces a unique constraint on `(staff_id, teller_id)`, meaning a staff member can only ever be assigned to a given teller **once in the database's lifetime**. Re-assigning the same cashier to the same teller on a new date period is impossible without deleting the old record first.

### Code Reference
- File: `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/domain/Cashier.java`
- Lines 48–51

### Real-world Impact
If a cashier is assigned to Teller 1 for March, they cannot be assigned to Teller 1 again in April without deleting the March assignment. This blocks normal shift rotation in any real MFI environment.

### Fix Required
- Drop the unique constraint `ux_cashiers_staff_teller` from `m_cashiers`
- Provide a Liquibase migration to remove the constraint
- Rely on the existing date-overlap check in `CashierTransactionDataValidator.validateCashierAllowedDateAndTime()` as the only guard against duplicate active assignments

---

## Issue 4 — Enhancement: Add m_cashier_sessions table and JPA entity

**Labels:** `enhancement`  
**Spec Reference:** §3.2.1, §3.3 — New Data Model, Phase 3  
**Depends On:** #3

### Feature: Cashier Session as a First-Class Entity

Introduce a `m_cashier_sessions` table and corresponding JPA entity. This is the **foundational change** for the entire session-aware teller redesign. All subsequent issues (GL routing, transaction isolation, session API endpoints) depend on this.

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

---

## Issue 5 — Enhancement: Add cashier_session_id FK to m_loan_transactions and m_savings_account_transaction

**Labels:** `enhancement`  
**Spec Reference:** §3.2.2 — Alter existing tables, Phase 3  
**Depends On:** #4

### Feature: Link Transactions to Cashier Sessions

Add a `cashier_session_id` nullable foreign key column to both `m_loan_transactions` and `m_savings_account_transaction`. This is what enables true per-session transaction isolation and accurate session-level reconciliation.

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

---

## Issue 6 — Enhancement: Implement Cashier Session lifecycle API endpoints

**Labels:** `enhancement`  
**Spec Reference:** §3.3, §4 — Session Lifecycle + API Endpoints, Phase 4  
**Depends On:** #4, #5

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

### Deliverables
- `CashierSessionApiResource.java`
- `CashierSessionWritePlatformService.java` + implementation
- `CashierSessionReadPlatformService.java` + implementation
- Command wrapper additions in `CommandWrapperBuilder`
- `CashierSessionData.java` DTO
- `CashierSessionSummaryData.java` DTO

---

## Issue 7 — Bug/Enhancement: Route cash GL through Teller Cash (11140) when a cashier session is open

**Labels:** `bug`, `enhancement`  
**Spec Reference:** §2.3, §3.4 — GL Routing Fix, Phase 5  
**Depends On:** #4, #5, #6

### Problem + Feature: GL Routing for Cash Transactions

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
```

### Files to Update
- `fineract-provider/src/main/java/org/apache/fineract/accounting/journalentry/service/AccountingProcessorForLoan.java`
- `fineract-provider/src/main/java/org/apache/fineract/accounting/journalentry/service/AccountingProcessorForSavings.java`

### Financial Activity Mapping Required
| ID | Activity | GL Code | Account Name |
|----|----------|---------|--------------|
| 101 | cashAtMainVault | 11130 | Cash on Hand (Vault) |
| 102 | cashAtTeller | 11140 | Teller Cash — Head Office |

---

## Issue 8 — Enhancement: Auto-post GL variance journal entry on unbalanced settlement

**Labels:** `enhancement`  
**Spec Reference:** §3.5.3 — Variance Handling, Phase 7  
**Depends On:** #6, #7

### Feature: Variance Journal Entry on Settlement

When a cashier settles and the amount returned does not equal the expected cash on hand, the system must automatically calculate and post a correcting GL journal entry.

### Variance Formula (Spec §3.5.3)
```
Expected Cash = Opening Allocation + Cash In − Cash Out
Variance = Settled Amount − Expected Cash
```

### GL Entries

**Short settlement (cashier returns less than expected):**
```sql
DEBIT  53920  Cash Shortage — Teller        [variance amount]
CREDIT 11140  Teller Cash — Head Office     [variance amount]
```

**Over settlement (cashier returns more than expected):**
```sql
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

### Implementation Location
- `TellerWritePlatformServiceJpaImpl.java` — `settleCashFromCashier()` method
- Or new `CashierSessionWritePlatformServiceImpl.java` session close handler

---

## Issue 9 — Bug: Cashier assignment expiry causes silent failure with no notification

**Labels:** `bug`  
**Spec Reference:** §7 — Known Limitations, Phase 9

### Problem

When a cashier's `end_date` on `m_cashiers` is reached, operations fail silently. No notification is sent, no warning is shown, and errors returned to users are unclear.

### Current Behaviour
- Cashier assignment expires silently at midnight on `end_date`
- Subsequent allocation, repayment, or settlement calls fail without a helpful error
- No API response distinguishes "cashier assignment expired" from other errors
- No advance warning when expiry is approaching

### Files Involved
- `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/domain/Cashier.java`
- `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/service/TellerWritePlatformServiceJpaImpl.java`
- `fineract-branch/src/main/java/org/apache/fineract/organisation/teller/api/TellerApiResource.java`

### Fix Required
- On any teller operation, check if `cashier.getEndDate()` is in the past and return `422 Unprocessable Entity` with a clear message
- On cashier retrieval, include an `expiryWarning` flag in the response if `end_date` is within 7 days

---

## Issue 10 — Enhancement: Multi-teller concurrent session support

**Labels:** `enhancement`  
**Spec Reference:** §8 — Implementation Plan Phase 10  
**Depends On:** #4, #5, #6, #7

### Feature: Allow Cashier to Hold Concurrent Sessions on Multiple Tellers

The proposed session model (Issue #6) assumes one active session per cashier per day. To support cashiers rotating between multiple teller stations within the same day, the session model needs to permit concurrent sessions on different tellers.

### Required Changes

**Session uniqueness rule change:**
- Current proposed rule: one open session per cashier per day
- New rule: one open session per cashier **per teller** per day

**Session lookup change:**
```java
// Required (this issue):
cashierSessionRepository.findOpenSessionByUserAndTeller(userId, tellerId, officeId, date);
```

### Deliverables
- Update `CashierSessionRepository.findOpenSessionByUser()` to accept `tellerId`
- Update session open validation to check uniqueness per `(cashier_id, teller_id, session_date)`
- Update GL routing in `AccountingProcessorForLoan` and `AccountingProcessorForSavings` to resolve teller account from the active session's `teller_id`
- Add `teller_id` index on `m_cashier_sessions`
