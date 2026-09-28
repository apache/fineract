# Share Module Foundation: Implementation Handoff

**Status:** Analysis complete. No production code, migrations, configuration, or tests were changed.

## Scope and baseline

Nsimbi should extend Apache Fineract's existing Share Product and Share Account implementation. It already provides products, accounts, lifecycle actions, charges, accounting, dividends, savings payout, permissions, command handlers, and tests. Do not create a parallel share ledger, account model, or dividend engine.

The inspected checkout is on `feat/share-module`; the requested `feat/share-module-foundation` branch was not found. It is two commits behind local `dev` and has no unique commits. Align with `dev` before implementation so share work follows the latest Nsimbi savings command and monetary-authority patterns.

Nsimbi frontend work has not started. Upstream Mifos frontend sources were inspected only to identify API consumers; they are not the Nsimbi UI specification.

## Video/UI behavior summary

Source: `/media/ib-s-muhoza/EXFAT_HAX/Recording of Share Module.mp4` (about 80 seconds).

| Screen | Observed behavior |
|---|---|
| Shares landing page | Sidebar navigation opens `SHARES`, showing `No shares found!` and a `SHARE DIVIDENDS` action. No populated rows, filters, account creation, or product selection are shown. |
| Share Dividends form | Contains Amount, `Use Percentages`, Date, Share Period in months, Method, Saving product, Reference Number, Comment, Password, `SHARE DIVIDENDS`, and `Back to dividends`. |
| Date picker | September 2026 picker opens with `2026-09-20` displayed. This proves date selection, not the financial meaning of that date. |
| Validation | Submission briefly shows `SUBMITTING`, then displays `Password is required To Perform This Action`. No successful allocation, approval, payment, balance change, or transaction ID is shown. |

The only demonstrated journey is:

```text
Dashboard → Shares empty state → Share Dividends form → submit without password → validation error
```

The recording does not establish percentage rules, period calculation, savings-product behavior, password verification, maker-checker, successful account flows, payment channels, reports, or accounting policy.

## Existing backend findings

### Existing domain and workflows

| Capability | Existing implementation |
|---|---|
| Share products | `portfolio/shareproducts/` contains `ShareProduct`, market prices, charges, serializers, read/write services, create/update handlers, product accounting mappings, and business events. Products support currency, unit price, issued/member limits, lock-in, charges, and dividend settings. |
| Share accounts | `portfolio/shareaccounts/` contains `ShareAccount`, transactions, charges, serializers, services, command handlers, bulk import, account summaries, and events. Each account links a client, product, and savings account. |
| Dividends | `ShareProductDividendPayOutDetails`, `ShareAccountDividendDetails`, allocation services, create/approve/delete handlers, share-day calculation, read services, and the `Post Dividends For Shares` scheduled job. |
| Accounting | `CashBasedAccountingProcessorForShares`, share GL mappings, and normal savings `DIVIDEND_PAYOUT` accounting. |
| Commands and audit | `CommandWrapperBuilder`, `PortfolioCommandSourceWritePlatformServiceImpl`, `SynchronousCommandProcessingService`, and `@CommandType` handlers. |
| Nsimbi authority | `MonetaryAuthorityType` includes `SHARES`; `NsimbiMonetaryAuthorityPolicyService` supports currency/minimum/maximum evaluation. No share handler currently calls it. |

Existing account lifecycle:

```text
create → Submitted and pending approval
          ├─ approve → Approved → activate → Active → close → Closed
          └─ reject  → Rejected
Approved → undo approval → Submitted and pending approval
Active → apply additional shares → approve/reject additional-share transaction
Active → redeem shares
```

Account statuses and purchased-share transaction statuses are distinct. Transactions track applied, approved, rejected, purchased, redeemed, and charge-payment states.

### Existing API surface

All paths are under `/fineract-provider/api/v1`.

| API | Purpose |
|---|---|
| `GET/POST /products/share`, `GET /products/share/template`, `GET/PUT /products/share/{id}` | Share-product listing, template, details, creation, and update. Cash accounting requires reference, suspense, equity, and fee-income GL mappings. |
| `GET/POST /accounts/share`, `GET /accounts/share/template`, `GET/PUT /accounts/share/{id}` | Share-account listing, template, details, creation, and update. The global list currently returns active accounts only. |
| `POST /accounts/share/{id}?command=approve|undoapproval|reject|activate|close|applyadditionalshares|approveadditionalshares|rejectadditionalshares|redeemshares` | Existing lifecycle and holdings actions. |
| `GET /shareproduct/{productId}/dividend` | Dividend batch list. |
| `GET /shareproduct/{productId}/dividend/{dividendId}` | Dividend allocations, with optional account-number filter. |
| `POST /shareproduct/{productId}/dividend` | Creates a calculated batch. Current accepted fields: `dividendPeriodStartDate`, `dividendPeriodEndDate`, `dividendAmount`, `dateFormat`, `locale`. |
| `PUT /shareproduct/{productId}/dividend/{dividendId}?command=approve` | Approves a batch for scheduled processing; it does not credit savings accounts immediately. |
| `DELETE /shareproduct/{productId}/dividend/{dividendId}` | Deletes an unapproved batch. |
| `GET/POST /accounts/share/downloadtemplate|uploadtemplate` | Existing share-account bulk-import support. |

Legacy `previewdividends` and `postdividends` command constants exist, but the inspected `ShareProductCommandsServiceImpl` has null-returning implementations. Do not treat those as usable preview or payout APIs.

### Dividend calculation, posting, and accounting

The existing dividend request is a fixed total pool for one share product. `ShareProductDividendAssembler` distributes it among eligible accounts in proportion to approved share-days, considering purchases, redemptions, and the minimum active period. It is not a percentage-rate calculator.

```text
Create batch → product batch INITIATED; account allocations INITIATED
Approve batch → product batch APPROVED; account allocations remain INITIATED
Scheduled job → each successful allocation becomes POSTED with savings transaction ID
```

Approval does not mean that every member has been paid. The job processes allocations individually, so successful and failed payouts can coexist.

The posting service uses the current business date and credits each share account's linked savings account through `handleDividendPayout`, creating a savings `DIVIDEND_PAYOUT` transaction. It does not currently accept a selected savings product, intended payout date, reference, comment, password, or percentage rate.

| Event | Debit | Credit |
|---|---|---|
| Share purchase application | Share reference | Share suspense |
| Share purchase approval | Share suspense | Share equity |
| Share purchase rejection | Share suspense | Share reference |
| Redemption | Share equity | Share reference |
| Dividend payout to savings | Payable dividends financial activity | Savings control |

Finance must define how payable dividends are funded before any change to dividend posting. The inspected create/approve flow does not establish that funding entry. A linked savings account also does not prove share purchases/redemptions transfer money through savings.

### Validation, permissions, maker-checker, and migrations

Existing serializers validate required/unsupported fields, share limits, positive quantities, holdings, chronology, lock-in, charge currency, and issued-share limits. Account creation validates that the selected savings account belongs to the client and has the required currency/type.

| Area | Existing permission codes |
|---|---|
| Product maintenance | `CREATE_SHAREPRODUCT`, `UPDATE_SHAREPRODUCT` |
| Account maintenance | `CREATE_SHAREACCOUNT`, `UPDATE_SHAREACCOUNT` |
| Lifecycle | `APPROVE_SHAREACCOUNT`, `UNDOAPPROVAL_SHAREACCOUNT`, `REJECT_SHAREACCOUNT`, `ACTIVATE_SHAREACCOUNT`, `CLOSE_SHAREACCOUNT` |
| Holdings | `APPLYADDITIONALSHARES_SHAREACCOUNT`, `APPROVEADDITIONALSHARES_SHAREACCOUNT`, `REJECTADDITIONALSHARES_SHAREACCOUNT`, `REDEEMSHARES_SHAREACCOUNT` |
| Dividends | `READ_DIVIDEND_SHAREPRODUCT`, `CREATE_DIVIDEND_SHAREPRODUCT`, `APPROVE_DIVIDEND_SHAREPRODUCT`, `DELETE_DIVIDEND_SHAREPRODUCT`, plus existing checker variants. |

The inspected share permission seed rows use `can_maker_checker=false`. Checker-named permissions alone do not activate maker-checker. Generic maker-checker support has queued commands, checker authorization, and same-user checking rules, but business dividend approval, command maker-checker, and password reauthentication are separate controls.

Existing share tables are `m_share_product`, `m_share_product_market_price`, `m_share_product_charge`, `m_share_account`, `m_share_account_transactions`, `m_share_account_charge`, `m_share_account_charge_paid_by`, `m_share_product_dividend_pay_out`, and `m_share_account_dividend_details`.

Use additive Liquibase XML changesets under `fineract-provider/src/main/resources/db/changelog/tenant/parts/`, included through the Nsimbi final changelog. Do not modify historical migrations or duplicate share tables.

## Existing frontend findings

No Nsimbi frontend source is present and frontend work is outside this phase. The backend references a separate `openmf/web-app` image.

The upstream Mifos web application already has share-account creation/editing, General/Transactions/Charges/Dividends views, lifecycle actions, share-product maintenance, dividend list/create/detail screens, client summaries, routes, and permission-gated menu actions. Its dividend form uses only start date, end date, and a fixed amount.

Some upstream action permission labels do not match this backend's seeded permission codes, and it includes at least one account deletion UI action without a matching backend DELETE endpoint. Treat these as compatibility findings, not backend requirements.

## Gap analysis

| Concern | Current behavior | Required direction |
|---|---|---|
| Products/accounts/lifecycle | Already implemented | Reuse and characterize; do not rebuild. |
| Landing-page work queue | Global list is active accounts only | Decide whether users need accounts, holdings, transactions, or approval work queues before adding filters. |
| Fixed dividend pool | Existing share-day allocation | Reuse if Finance accepts it; test dates, allocation totals, and rounding. |
| Percentage mode | Not implemented | Define denominator, rate period, annualization, eligible holdings, and rounding before backend work. |
| Date + monthly period | Explicit start/end API dates; business-date posting | Define declaration, eligibility, and payout dates and month-boundary treatment. |
| Savings-product selector | Each share account has one linked savings account | Decide whether this filters eligibility or overrides destination; define missing/inactive/multiple/currency-mismatch handling. |
| Reference/comment | Not in current dividend contract/model | Add only with requiredness, bounds, audit, uniqueness, and correction policy. |
| Password | No dividend transaction-password contract | Decide transaction reauthentication versus independent approval; never put credentials in audit payloads. |
| Maker-checker | Framework exists; share capabilities are not enabled | Decide covered operations, configuration, and financial-side-effect timing. |
| Monetary limits | Nsimbi `SHARES` policy exists but is unused | Define amount basis and actors, then reuse the policy. |
| Preview | No working verified path | Add only if needed; it must be read-only and use the same allocation rules. |
| Retry/reconciliation | Per-allocation status and transaction link exist | Test partial failure, retries, concurrency, and correction. Do not call an approved batch fully paid. |
| Reports | Source data exists | Define report/branch/status requirements before adding report SQL or permissions. |

## Proposed phased implementation plan

1. **Align baseline and settle policy.** Rebase or recreate the feature branch from current `dev`, then agree the calculation, date, destination, authorization, monetary-limit, and accounting policies.
2. **Characterize existing behavior.** Run focused share/dividend suites, add lifecycle/allocation/payout/accounting regression coverage, and fix only demonstrated defects.
3. **Apply Nsimbi authorization.** Reuse the `SHARES` policy and command framework for agreed authority controls. Add maker-checker configuration/capability only where needed through additive Liquibase migrations and tests.
4. **Extend dividend semantics.** Add approved percentage, period, destination, reference/comment, and optional preview behavior to the existing dividend resource and services. Preserve fixed-pool API compatibility.
5. **Harden posting and reconciliation.** Cover retries, duplicate execution, partial payouts, destination changes, business dates, branch closures, and payable-dividend reconciliation while retaining normal savings dividend transactions and accounting hooks.
6. **Operational APIs and rollout documentation.** Add agreed filters, allocation-status reads, and reports; verify tenant upgrades and document jobs, reconciliation, roles, and correction procedures.

## Likely files to change or create

| Path | Likely purpose |
|---|---|
| `fineract-provider/.../portfolio/shareproducts/api/ShareDividendApiResource.java` | Dividend API extensions, resource identity checks, optional preview. |
| `fineract-core/.../portfolio/shareproducts/constants/ShareProductApiConstants.java` | Approved new request fields/commands. |
| `fineract-provider/.../portfolio/shareproducts/serialization/ShareProductDataSerializer.java` | Rate, period, and metadata validation. |
| `fineract-provider/.../portfolio/shareproducts/service/ShareProductDividendAssembler.java` | Deterministic allocation and approved percentage calculation. |
| `fineract-provider/.../portfolio/shareproducts/service/ShareProductWritePlatformServiceJpaRepositoryImpl.java` | Declaration/approval policy and persistence. |
| `fineract-provider/.../portfolio/shareproducts/domain/ShareProductDividendPayOutDetails.java` and related data/read classes | Persist only required declaration metadata. |
| `fineract-provider/.../portfolio/shareaccounts/domain/ShareAccountDividendDetails.java` | Destination snapshot/retry metadata if policy requires it. |
| `fineract-provider/.../portfolio/shareaccounts/service/ShareAccountDividendReadPlatformServiceImpl.java`, `ShareAccountSchedularServiceImpl.java` | Payout selection, retry/revalidation, and operational status. |
| `fineract-provider/.../portfolio/shareaccounts/jobs/postdividentsforshares/PostDividentsForSharesTasklet.java` | Posting/retry behavior proven necessary by tests. |
| Share account/dividend handlers and configuration classes | Authority and command behavior integration. |
| `fineract-provider/.../nsimbi/userroles/service/NsimbiMonetaryAuthorityPolicyService.java` | Reuse unless its current contract cannot express the approved rules. |
| Small share authority adapter service, if needed | Translate share operation values to the existing authority policy. |
| `fineract-provider/src/main/resources/db/changelog/tenant/parts/<unused>_nsimbi_share_*.xml` and `final-changelog-tenant.xml` | Additive schema, permission capability, or configuration changes. |
| API data/Swagger classes and `docs/changes/` | Contract and rollout documentation. |

## Test plan

Extend existing suites:

- `integration-tests/.../common/shares/ShareAccountIntegrationTests.java`
- `integration-tests/.../common/shares/DividendsIntegrationTests.java`
- `integration-tests/.../ShareAccountCreationValidationTest.java`
- `integration-tests/.../ShareAccountChargeRoundingTest.java`
- `integration-tests/.../ShareProductDatatableIntegrationTest.java`

| Area | Required coverage |
|---|---|
| Dividend calculation | Purchases/redemptions at boundaries, minimum active period, closed-account option, zero eligibility, deterministic rounding, allocation sum/residual, and no negative allocations. Add rate/annualization cases only after policy approval. |
| API validation | Missing/nonpositive amounts, invalid dates, unsupported fields, invalid nested resources, metadata limits, permission denial, and fixed-pool compatibility. |
| Lifecycle | Valid/invalid transitions, repeat commands, locked/insufficient holdings, issued-share limits, savings ownership/currency. |
| Nsimbi authority | Minimum/maximum boundaries, absent authority, wrong currency, direct/import paths, queued execution, and proof that denied commands create no transaction or journal. |
| Maker-checker | Immediate versus queued execution, checker identity, self-check prevention, rejection/deletion/replay, audit context, and no early posting. |
| Payout/accounting | Approval through posted `DIVIDEND_PAYOUT`, savings balance/transaction link, rerun without double credit, partial failure/retry, business dates, GL closure, and payable-dividend/savings-control entries. |
| Migration/read behavior | New/upgraded tenants, constraints/defaults, permission capability, pagination/filter regressions, tenant/office isolation, and role visibility. |
| Reauthentication, if adopted | Missing/invalid/expired proof, correct operator, replay rules, and no secret in command/audit/error payloads. |

At the time of the original analysis, no tests had been added or executed. Phase 2 test additions and execution status are recorded below.

## Open questions

1. Is dividend value a fixed pool, per-member value, or percentage? If percentage, what is the base: paid-up nominal capital, market value, or another measure?
2. Is the video date a declaration date, eligibility end date, or payout date? How does Share Period derive dates and handle month boundaries?
3. Does Saving product select destination, eligibility filter, or both? Can a member have multiple eligible savings accounts?
4. Does Password mean same-user reauthentication, supervisor credential, or another approval workflow?
5. Which operations require maker-checker, and does this vary by amount or role?
6. Which operations use `SHARES` monetary authority: purchases, redemptions, issuance, dividend pool, individual payouts, or all?
7. How is dividend payable funded and reconciled? Are withholding tax, charges, retained residuals, or specific GL postings required?
8. Which members/accounts are eligible, including inactive clients, closed accounts after period end, multiple accounts, and pending holdings?
9. Are overlapping, supplemental, or corrected distributions allowed? Is Reference Number unique or idempotent?
10. Which reports, branch restrictions, status filters, and audit views are required?

## Risks and assumptions

| Item | Risk or assumption |
|---|---|
| Video evidence | The video proves only the empty page and password validation failure; it does not prove any successful financial rule. |
| Allocation arithmetic | Existing allocation uses `double` for amount per share-day; distribution rounding and total residual behavior need characterization before production use. |
| Partial payout | Approved and posted are different states. A batch can have both paid and unpaid allocation rows. |
| Date semantics | Current payout uses business date while the video has a selectable date and monthly period; conflating them can misstate financial dates. |
| Accounting | Payout uses Payable Dividends and Savings Control; financing the payable balance is a Finance decision. |
| Secrets and audit | Passing a password in a command body risks audit retention. Reauthentication needs a safe authentication design. |
| Maker-checker | Existing checker permission names do not prove share maker-checker capability or configuration. |
| Authority enforcement | `SHARES` authority is available but inactive in share flows. Undefined value basis could block valid transactions or miss exposure. |
| Frontend | No Nsimbi frontend is in scope; keep backend contracts backward compatible and independent of unverified upstream UI. |
| Migrations | Share tables contain core financial records. Use additive Liquibase changes only. |

## Phase 2: Nsimbi Share Dividends submission (implemented on `feat/share-module`)

This focused backend extension adds `POST /v1/shareproduct/{productId}/dividend/nsimbi`. It is scoped to the share product in the URL. The existing Fineract `POST /v1/shareproduct/{productId}/dividend` contract remains unchanged and continues to accept explicit `dividendPeriodStartDate`, `dividendPeriodEndDate`, and `dividendAmount`. The Nsimbi route checks the existing `CREATE_DIVIDEND_SHAREPRODUCT` permission before validating the body, then submits a translated payload through the existing dividend command handler. It creates a dividend declaration; approval and payout remain separate existing steps.

Example request (fixed amount only):

```json
{
  "amount": 50000,
  "usePercentages": false,
  "date": "2026-09-20",
  "sharePeriodMonths": 1,
  "method": "Savings",
  "password": "<operator-entered value>"
}
```

`date` is assumed to be the **dividend period end date**, not the accounting posting date. Subtracting `sharePeriodMonths` calendar months derives the start date; in the example, the existing command receives `dividendPeriodStartDate=2026-08-20`, `dividendPeriodEndDate=2026-09-20`, `dividendAmount=50000`, `dateFormat=yyyy-MM-dd`, and `locale=en`. This month subtraction is an explicit Phase 2 assumption, pending Finance approval of period boundaries and eligibility policy. The existing share-day allocation behavior determines the financial distribution.

Missing or blank `password` produces the validation message **“Password is required To Perform This Action.”** This is presence validation only: no password reauthentication service is integrated, and a nonblank value does **not** prove the operator's identity. The password is removed before creation of the audited Fineract command. The unchanged public Fineract route does not gain password validation. Clients must avoid logging the raw request body or treating this presence check as transaction authorization.

`usePercentages=true` is rejected. Only `method="Savings"` (or omitted method) is supported. A nonblank `savingProductId` is rejected: the existing payout destination remains each eligible share account's linked savings account. Nonblank `referenceNumber` and `comment` are rejected because neither is stored by this command; blank or omitted values are accepted. Unknown fields, invalid/nonpositive amounts, invalid ISO dates, and nonpositive/non-whole month periods are rejected. The route does not enable maker-checker, change approval or posting, add migrations, or enforce Nsimbi `SHARES` monetary limits. Existing maker-checker command behavior, if configured for the dividend command, remains in the command framework.

Focused tests were added in `NsimbiShareDividendRequestMapperTest` and `NsimbiShareDividendApiResourceTest` for field mapping, period derivation, password omission from the command, required password, unsupported percentage and metadata options, invalid amount/date/period, permission denial before validation, and reuse of the existing command. The focused command `timeout 180 ./gradlew :fineract-provider:test --tests org.apache.fineract.portfolio.shareproducts.api.NsimbiShareDividendRequestMapperTest --tests org.apache.fineract.portfolio.shareproducts.api.NsimbiShareDividendApiResourceTest --no-daemon --offline` was attempted once. It failed during `:fineract-avro-schemas:compileJava` dependency resolution because `com.google.protobuf:protobuf-java:4.34.2` was not cached for offline mode. Compilation of the changed share code and the test classes did not start; no test results are available.

Follow-up decisions remain: actual password reauthentication and its secure request/audit design; percentage basis and calculation; selected savings-product semantics; reference/comment persistence; Finance-approved month boundaries and accounting dates; dividend maker-checker activation; and whether/where `SHARES` monetary authority applies. No successful submission is shown in the reference video, so these business choices are not inferred from it.
