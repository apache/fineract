# Delinquency Pause and Resume Handling for Progressive Loans

This document explains how loan-level delinquency pause and resume works for progressive loans in Fineract.

The delinquency pause feature controls delinquency classification only. It does not stop interest accrual, change EMI calculation, or change the repayment schedule. For progressive-loan interest accrual pauses, use the separate `interest-pauses` API described near the end of this document.

## Summary

Delinquency pause/resume is stored as a sequence of immutable loan-level actions:

- `PAUSE` creates a period where delinquency calculation is suspended.
- `RESUME` ends an active pause early.
- During an active pause, COB does not recalculate delinquency tags for the loan.
- Existing delinquency tags remain unchanged while the loan is paused.
- After the pause ends, COB recalculates delinquency and subtracts pause days from overdue-day calculations.

The feature applies at loan-account level, not loan-product level. A progressive loan uses the same delinquency pause/resume API as other loans.

## API

Base path:

```text
/fineract-provider/api/v1
```

### Create Delinquency Action

Create a pause or resume action by loan id:

```http
POST /loans/{loanId}/delinquency-actions
Content-Type: application/json
```

Create a pause or resume action by external loan id:

```http
POST /loans/external-id/{loanExternalId}/delinquency-actions
Content-Type: application/json
```

### Retrieve Delinquency Actions

Retrieve actions by loan id:

```http
GET /loans/{loanId}/delinquency-actions
```

Retrieve actions by external loan id:

```http
GET /loans/external-id/{loanExternalId}/delinquency-actions
```

There is no public update/delete API for delinquency actions. A pause is ended by creating a `RESUME` action.

## Create Request

### Pause Request

```json
{
  "action": "pause",
  "startDate": "10 January 2024",
  "endDate": "20 January 2024",
  "dateFormat": "dd MMMM yyyy",
  "locale": "en"
}
```

### Resume Request

```json
{
  "action": "resume",
  "startDate": "15 January 2024",
  "dateFormat": "dd MMMM yyyy",
  "locale": "en"
}
```

## Request Fields

| Field | Required | Applies to | Meaning |
| --- | --- | --- | --- |
| `action` | Yes | Pause, resume | Case-insensitive action name. Supported values are `pause` and `resume`. Stored and returned as `PAUSE` or `RESUME`. |
| `startDate` | Yes | Pause, resume | For `PAUSE`, the first date of the pause period. For `RESUME`, the current business date on which the active pause is resumed. |
| `endDate` | Yes for `pause`; must be omitted for `resume` | Pause | For `PAUSE`, the configured end of the pause period. For `RESUME`, this must not be supplied. |
| `dateFormat` | Recommended | Pause, resume | Date parser pattern used for `startDate` and `endDate`, for example `dd MMMM yyyy` or `yyyy-MM-dd`. Supply it explicitly to avoid relying on default date parsing. |
| `locale` | Recommended | Pause, resume | Locale used with `dateFormat`, for example `en`. Supply it explicitly when using month names or locale-specific formats. |

## Create Response

The create API returns a standard command processing result. The important field is `resourceId`, which is the created delinquency action id.

Example:

```json
{
  "officeId": 1,
  "clientId": 14,
  "loanId": 42,
  "resourceId": 123
}
```

Fields may vary depending on command processing context, but `resourceId` identifies the saved `m_loan_delinquency_action` row.

## Retrieve Response

The retrieve API returns all delinquency actions for the loan.

Example:

```json
[
  {
    "id": 123,
    "action": "PAUSE",
    "startDate": [2024, 1, 10],
    "endDate": [2024, 1, 20],
    "createdById": 1,
    "createdOn": "2024-01-10T09:30:00Z",
    "updatedById": 1,
    "lastModifiedOn": "2024-01-10T09:30:00Z"
  },
  {
    "id": 124,
    "action": "RESUME",
    "startDate": [2024, 1, 15],
    "endDate": null,
    "createdById": 1,
    "createdOn": "2024-01-15T08:00:00Z",
    "updatedById": 1,
    "lastModifiedOn": "2024-01-15T08:00:00Z"
  }
]
```

## Response Fields

| Field | Meaning |
| --- | --- |
| `id` | Delinquency action id. This is the row id in `m_loan_delinquency_action`. |
| `action` | Action type. `PAUSE` means a pause period. `RESUME` means an active pause was ended through a resume action. |
| `startDate` | Action start date. For `PAUSE`, this is the configured pause start. For `RESUME`, this is the resume business date. |
| `endDate` | Action end date. Present for `PAUSE`; `null` for `RESUME`. |
| `createdById` | User id that created the action. |
| `createdOn` | Audit timestamp for creation. |
| `updatedById` | Last user id that modified the row. Delinquency actions are not normally updated through public API, so this usually matches `createdById`. |
| `lastModifiedOn` | Audit timestamp for last modification. |

## Validation Rules

### Common Rules

- The loan must be active.
- `action` must be `pause` or `resume`.
- Dates are parsed using the supplied `dateFormat` and `locale`; if omitted, parsing relies on the platform default behavior.

### Pause Rules

- `startDate` is required.
- `endDate` is required.
- `startDate` must not be before the first disbursement date.
- `startDate` and `endDate` must not be the same day.
- The pause must not overlap an existing effective pause period.
- Adjacent pauses are allowed. For example, `10 Jan - 11 Jan` and `12 Jan - 13 Jan` are valid.

### Resume Rules

- `startDate` is required.
- `startDate` must equal the current business date.
- `endDate` must not be supplied.
- A resume can be created only while the loan is inside an active effective pause period.
- Only one resume is allowed for the same date.

## How It Works

1. `POST /delinquency-actions` stores a `LoanDelinquencyAction`.
2. The action is persisted in `m_loan_delinquency_action` with `loan_id`, `action`, `start_date`, and `end_date`.
3. COB reads all delinquency actions for the loan.
4. The effective pause list is calculated from saved `PAUSE` and `RESUME` actions.
5. If the current business date falls inside an effective pause period, COB skips delinquency-tag recalculation for the loan.
6. If the current business date is outside all effective pause periods, COB calculates delinquency normally and subtracts applicable pause days from overdue-day calculations.

## Effective Pause Periods

Saved actions are converted to effective pause periods:

- A `PAUSE` without a matching `RESUME` is effective from its `startDate` to its `endDate`.
- A `RESUME` whose `startDate` falls inside a pause changes that pause's effective `endDate` to the resume `startDate`.
- The saved `PAUSE` row is not updated. The shortened period is calculated in memory from the saved action list.

Example:

| Saved action | Saved start | Saved end | Effective result |
| --- | --- | --- | --- |
| `PAUSE` | `2024-01-10` | `2024-01-20` | Effective pause starts `2024-01-10`. |
| `RESUME` | `2024-01-15` | `null` | Effective pause end becomes `2024-01-15`. |

## Included and Excluded Dates

There are two date interpretations to be aware of.

### COB Pause Status

For deciding whether COB should skip delinquency recalculation, effective pause periods are inclusive:

```text
effectiveStartDate <= businessDate <= effectiveEndDate
```

If the effective pause is `2024-01-10` to `2024-01-20`, COB considers the loan paused on both `2024-01-10` and `2024-01-20`.

If a resume is created on `2024-01-15`, the effective end date becomes `2024-01-15`, so the pause status check still treats `2024-01-15` as inside the pause period.

### Pause-Day Counting

For subtracting pause days from overdue-day calculations, helper methods count days using a start-inclusive, end-exclusive range:

```text
[startInclusive, endExclusive)
```

Examples:

- Pause days before `2024-01-15` for an effective pause starting `2024-01-10` count `2024-01-10` through `2024-01-14`.
- Pause days within range `2024-01-10` to `2024-01-15` count 5 days.
- The `endExclusive` date itself is not counted in the day difference.

This means the COB pause-status boundary and pause-day-counting boundary are not identical. Status uses inclusive end dates. Day counting uses date differences over half-open ranges.

## Examples

### Example 1: Simple Pause

Request:

```http
POST /fineract-provider/api/v1/loans/42/delinquency-actions?tenantIdentifier=default
Content-Type: application/json
```

```json
{
  "action": "pause",
  "startDate": "2024-01-10",
  "endDate": "2024-01-20",
  "dateFormat": "yyyy-MM-dd",
  "locale": "en"
}
```

Result:

- COB skips delinquency recalculation from `2024-01-10` through `2024-01-20`.
- Existing delinquency tags remain unchanged during that period.
- On `2024-01-21`, COB can recalculate delinquency again.
- Pause-day counting before `2024-01-21` counts `2024-01-10` through `2024-01-20`.

### Example 2: Pause Then Resume Early

Create the pause:

```json
{
  "action": "pause",
  "startDate": "2024-01-10",
  "endDate": "2024-01-20",
  "dateFormat": "yyyy-MM-dd",
  "locale": "en"
}
```

Resume on the current business date, `2024-01-15`:

```json
{
  "action": "resume",
  "startDate": "2024-01-15",
  "dateFormat": "yyyy-MM-dd",
  "locale": "en"
}
```

Result:

- The saved pause remains `2024-01-10` to `2024-01-20`.
- The effective pause is calculated as `2024-01-10` to `2024-01-15`.
- COB pause status treats `2024-01-15` as paused because pause-status checks are inclusive.
- Pause-day counting before `2024-01-15` counts `2024-01-10` through `2024-01-14`.

### Example 3: Adjacent Pauses

These pauses are allowed because they do not overlap according to validation:

```json
{
  "action": "pause",
  "startDate": "2024-01-10",
  "endDate": "2024-01-11",
  "dateFormat": "yyyy-MM-dd",
  "locale": "en"
}
```

```json
{
  "action": "pause",
  "startDate": "2024-01-12",
  "endDate": "2024-01-13",
  "dateFormat": "yyyy-MM-dd",
  "locale": "en"
}
```

### Example 4: Invalid Resume With End Date

This request is rejected because `resume` actions must not have `endDate`:

```json
{
  "action": "resume",
  "startDate": "2024-01-15",
  "endDate": "2024-01-20",
  "dateFormat": "yyyy-MM-dd",
  "locale": "en"
}
```

Expected validation error:

```text
validation.msg.loanDelinquencyAction.endDate.resume.should.have.no.end.date
```

## Progressive Loan Interest Pauses Are Different

Progressive loans also have an interest-pause API:

```http
POST /loans/{loanId}/interest-pauses
GET /loans/{loanId}/interest-pauses
PUT /loans/{loanId}/interest-pauses/{variationId}
DELETE /loans/{loanId}/interest-pauses/{variationId}
```

External-id variants are also available:

```http
POST /loans/external-id/{loanExternalId}/interest-pauses
GET /loans/external-id/{loanExternalId}/interest-pauses
PUT /loans/external-id/{loanExternalId}/interest-pauses/{variationId}
DELETE /loans/external-id/{loanExternalId}/interest-pauses/{variationId}
```

Interest pauses:

- Are represented as `INTEREST_PAUSE` loan term variations.
- Are only supported for active progressive loans.
- Require an interest-bearing loan.
- Require interest recalculation to be enabled.
- Regenerate the loan schedule and reprocess transactions.
- Do not create `PAUSE` or `RESUME` delinquency actions.

Interest pause request:

```json
{
  "startDate": "2024-02-05",
  "endDate": "2024-02-10",
  "dateFormat": "yyyy-MM-dd",
  "locale": "en"
}
```

Interest pause retrieve response:

```json
[
  {
    "id": 456,
    "startDate": "2024-02-05",
    "endDate": "2024-02-10",
    "dateFormat": "dd MMMM yyyy",
    "locale": "en_US"
  }
]
```

For interest pauses, the progressive EMI calculator splits affected interest periods and marks the paused portion as non-interest-bearing. In the implementation, the internal paused segment is built from `startDate.minusDays(1)` to `endDate`, so the user-facing `startDate` and `endDate` should be treated as the requested pause period boundaries, while the schedule model handles internal day-boundary conversion.

Use `delinquency-actions` when the goal is to pause delinquency classification. Use `interest-pauses` when the goal is to pause interest accrual and recalculate a progressive loan schedule.
