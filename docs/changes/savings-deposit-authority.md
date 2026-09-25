# Savings deposit monetary authority

## Status and scope

Implemented on `feat/savings-deposit-authority`, based on synchronized `dev` at
`cbc78c0a3cd6d7dea8d3cbee4c54ac6782bf7333`. The base contains savings status filtering
(PR #3) and unsupported annual-fee rejection (PR #5). No commit, push or PR was made.

The original investigation stopped because API deposits and spreadsheet imports
both used `DEPOSIT_SAVINGSACCOUNT` without a persisted discriminator. The subsequent
approved design resolves that stop condition using trusted server metadata in the
existing command JSON column. This report preserves the call-path and policy
findings and describes the implementation replacing that initial stop condition.

Only ordinary staff/API savings deposits, including ordinary batch deposits, are
enrolled in `MonetaryAuthorityType.DEPOSITS`. Spreadsheet imports retain their
existing validation and permissions. Transfers, internal credits, interest,
scheduled jobs, GSIM, fixed/recurring deposits, charge processing and withdrawals
are not enrolled. No financial write/domain service or authority model changed.

## Construction and trust boundary

- `SavingsAccountTransactionsApiResource.transaction`: its ordinary `deposit`
  branch assigns `SavingsDepositOrigin.STAFF_API`. Both numeric and external
  account identifiers use this branch.
- `SavingsAccountTransactionCommandStrategy.execute`: ordinary batch deposits
  call that same resource. Additional query parameters or headers are not origin
  inputs.
- `SavingsTransactionImportHandler.importEntity`: only the spreadsheet deposit
  branch assigns `SPREADSHEET_IMPORT`. The withdrawal branch is unchanged.
- `CommandWrapperBuilder.withSavingsDepositOrigin` passes the enum to an immutable
  `CommandWrapper` field. Existing constructors remain compatible and default to
  no origin; the general `savingsAccountDeposit` builder does not default to a
  trusted origin. Future server callers must explicitly choose their origin.

The reserved top-level `_serverCommand` key in any incoming deposit business JSON
is rejected before persistence, including batch requests and JSON null values for
that key. Neither JSON, query parameters nor headers set the enum. Approval accepts
an audit ID and approve/reject action, not a replacement deposit payload.

## Persistence, execution, approval and retry

`PortfolioCommandSourceWritePlatformServiceImpl.logCommandSource` preserves the
existing deposit permission check and creates the ordinary flat `JsonCommand`.
`SynchronousCommandProcessingService.executeCommandAttempt` identifies the original
maker using the authenticated submitter and obtains a `CommandSource` through
`CommandSourceService.saveInitial` (or `getInitialCommandSource` for enclosing
batch transactions).

`CommandSourceService.getInitialCommandSource` reserves the client metadata key
before audit masking and uses `SavingsDepositCommandEnvelope` to encode only
`DEPOSIT_SAVINGSACCOUNT`:

```json
{
  "_serverCommand": { "version": 1, "origin": "STAFF_API" },
  "payload": { "transactionAmount": 50, "locale": "en" }
}
```

The complete original business payload is retained, not just the illustrative
fields above. Maker remains in `CommandSource.maker`; it is not duplicated in JSON.
The existing `m_portfolio_command_source.command_as_json` column is `TEXT`
(`0001_initial_schema.xml`, line 3067). No database migration is necessary. Unrelated
commands keep their original flat storage and execution paths.

`CommandSourceService.processCommandAndSaveResult` performs the single trusted
metadata decode for each deposit execution. It reconstructs a flat `JsonCommand`
using persisted account/resource identifiers and passes a
`SavingsDepositExecutionContext(origin, maker)` through the specialized
`SavingsDepositCommandHandler` interface. Other handlers retain the existing
`NewCommandSourceHandler.processCommand` contract and behavior.

`DepositSavingsAccountCommandHandler.processDeposit`:

1. Rejects missing context, origin or maker.
2. Bypasses monetary authority only for the exact trusted `SPREADSHEET_IMPORT`
   origin; the original financial service still performs all business validation.
3. For `STAFF_API`, runs the existing flat-payload transaction validator, reads the
   savings account currency and checks the original maker's current DEPOSITS
   authority against the requested transaction amount.
4. Calls the unchanged financial deposit service only if allowed. This precedes
   payment-detail creation, transaction/account changes, journals and business events.

The context-free handler method fails closed as defense in depth. No security
impersonation, new ThreadLocal or global mutable state was added.

Maker-checker validation follows handler execution in `CommandSourceService`.
Thus unauthorized deposits never enter `AWAITING_APPROVAL`. The normal initial
idempotency/audit record may be saved as `UNDER_PROCESSING` and subsequently
`ERROR`; this is not a pending approval entry. Authorized submissions follow the
existing execution-and-rollback maker-checker mechanism.

`approveEntry` retains checker permissions and same-maker/checker validation.
It unwraps JSON for presentation/hooks only; this does not establish execution
trust. Execution independently validates the stored envelope and uses
`CommandSource.maker`, never the checker, for monetary policy. The checker retains
normal audit attribution. A checker with greater authority cannot override the
maker, and reductions/removal of maker authority before approval are re-evaluated.

Retries load the existing command source through `COMMAND_SOURCE_ID` and use its
origin, payload, account identifiers and maker. Replacement request metadata is
not policy input. Reject/delete operations keep their existing authorization and
do not require execution decoding, so legacy pending commands can be cancelled.

## Existing policy semantics

`NsimbiMonetaryAuthorityPolicyService.allows` and `NsimbiUserMonetaryAuthority`
are unchanged. Minimum and maximum are inclusive `BigDecimal` comparisons. One
null bound is open-ended; both null is unconfigured and denied. Missing authority,
null policy inputs and contradictory persisted bounds fail closed. Normal entity
construction rejects inverted bounds. There is no authority active/inactive flag.

The currency comes from the savings account. The policy performs no currency
conversion, trimming or uppercasing. Its repository query uses the original maker
ID, DEPOSITS and the currency code; database equality/collation remains unchanged.

## Compatibility and API behavior

| Situation | Behavior |
| --- | --- |
| Ordinary API deposit within maker's inclusive bounds, maker-checker off | Existing deposit executes |
| Ordinary API deposit within bounds, maker-checker on | Existing approval workflow; maker checked again at approval |
| Below minimum, above maximum, missing/unconfigured authority or currency mismatch | HTTP 403 domain error before financial writes and approval queuing |
| Trusted spreadsheet deposit | Existing behavior; no monetary-authority lookup |
| Legacy pending API/import deposit without trusted origin | HTTP 403; cancel and resubmit |
| Malformed/unknown-origin/unsupported-version envelope | Same actionable origin error; no parsing error or HTTP 500 |
| Completed legacy deposit history | Flat business JSON remains readable |
| Other command types and internal financial paths | Existing behavior and flat command storage |

Domain errors use `GeneralPlatformDomainRuleException` and the existing
`PlatformDomainRuleExceptionMapper` (HTTP 403):

- `error.msg.savings.deposit.monetary.authority.denied`
- `error.msg.savings.deposit.untrusted.origin`: cancel and resubmit the deposit.
- `error.msg.savings.deposit.reserved.metadata`: remove the reserved client field.

Malformed incoming business JSON uses the existing `InvalidJsonException`.
Historical origin is never guessed from role, URL, payload shape or audit history,
including historical spreadsheet commands. Pending commands must be resubmitted
through the proper entry point; origin is not backfilled.

`AuditReadPlatformServiceImpl.AuditMapper` uses the codec's read-only `forDisplay`
for savings deposits. Audit detail/list and maker-checker query responses retain
flat business payloads and do not expose the metadata envelope. Legacy flat
payloads are returned unchanged. Malformed envelopes without a usable payload
present `{}`; raw metadata remains available in the internal persisted command
record. Read/display unwrapping never authorizes execution.

## Excluded paths and source evidence

- `AccountTransfersWritePlatformServiceImpl` transfer/refund credits call
  `SavingsAccountDomainService.handleDeposit` directly, bypassing the ordinary
  deposit handler.
- Opening balances use the domain service from savings activation. Interest
  posting uses its own command/service methods. Scheduled jobs use their existing
  services. None calls the guarded ordinary deposit command.
- `GSIMDepositCommandHandler` uses entity `GSIMACCOUNT`; its shared financial
  service remains unchanged.
- Fixed/recurring deposits, maturity, charges and withdrawal handlers/services
  remain unchanged. No accounting or balance-calculation code changed.
- The only production `savingsAccountDeposit(...)` construction sites are the API
  and spreadsheet import paths, and both now assign origin internally. These
  callers do not request command JSON sanitization.

## Security and deployment assumptions

Trusted origin means server-constructed and database-persisted, not a cryptographic
signature. Database/JVM operators remain trusted under `SECURITY.md`. No client API
was found that edits stored command JSON. All nodes executing savings commands
must run this version before relying on enforcement; an old handler does not
implement the policy. A rollback also requires handling new enveloped pending
commands before they reach old flat-JSON readers.

Authority is evaluated at execution using the existing repository and transaction
semantics; no new locking or snapshot of authority is introduced. This change does
not claim to serialize simultaneous authority edits and financial commits.

## Validation

Final validation on 2026-09-25: **116 tests passed, zero failures/errors/skips**.
Scoped Spotless checks passed; `git diff --check` passed.
Tests use real command services, handler, validator, policy and error mappers with
mocked persistence/financial writes. They are not a running-server or database-backed
transaction/rollback test. Real database commit/rollback, journal balances, HTTP
container binding and multi-node rollout were not exercised. Excluded-path evidence
combines focused regression tests and the call-path review above.

### Exact final command and results

```sh
./gradlew --offline --no-daemon --max-workers=1 --no-parallel \
  -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/savings-deposit-tests.gradle \
  :fineract-core:spotlessJavaApply :fineract-provider:spotlessJavaApply \
  :fineract-provider:test \
  --tests '*SavingsDeposit*Test' \
  --tests '*SavingsAnnualFee*Test' \
  --tests '*NsimbiUserMonetaryAuthorityTest' \
  --tests '*NsimbiMonetaryAuthorityPolicyServiceTest' \
  --tests '*CommandSourceServiceTest' \
  --tests '*SynchronousCommandProcessingServiceTest' \
  --tests '*SavingsAccountWritePlatformServiceJpaRepositoryImplTest' \
  --tests '*SavingsAccountTransfersServiceImplTest' \
  --tests '*FixedDepositAccountInterestCalculationServiceImplTest' \
  --tests '*CreateAccountTransferCommandStrategyTest' \
  :fineract-core:spotlessJavaCheck :fineract-provider:spotlessJavaCheck \
  -x :fineract-provider:resolve

git diff --check
```

The temporary Gradle init script caps each test JVM at 768 MiB with one fork and
sets each module's Spotless Java target to the exact changed Java files listed
below. It does not change repository build configuration. The unrelated provider
Swagger resolution task is excluded; dependency compilation still runs. Final
build: **BUILD SUCCESSFUL in 1m 34s**, tests completed in 26.5s. Log:
`/tmp/deposit-verified-build.log`. Existing Gradle deprecation warnings remain.

| Test class | Passed |
| --- | ---: |
| `FixedDepositAccountInterestCalculationServiceImplTest` | 2 |
| `SavingsAccountWritePlatformServiceJpaRepositoryImplTest` | 11 |
| `CreateAccountTransferCommandStrategyTest` | 1 |
| `CommandSourceServiceTest` | 3 |
| `SavingsDepositAuditTest` | 5 |
| `SavingsDepositEnvelopeTest` | 11 |
| `SynchronousCommandProcessingServiceTest` | 12 |
| `NsimbiUserMonetaryAuthorityTest` | 2 |
| `NsimbiMonetaryAuthorityPolicyServiceTest` | 1 |
| `SavingsAccountTransfersServiceImplTest` | 6 |
| `SavingsAnnualFeeCommandTest` | 8 |
| `SavingsDepositOriginTest` | 6 |
| `SavingsAnnualFeeMakerCheckerTest` | 8 |
| `SavingsDepositAuthorityTest` | 38 |
| `SavingsAnnualFeeSchedulerTest` | 2 |

The four new test classes contain 60 cases. These cover inclusive boundaries,
missing/unconfigured/currency-mismatched/invalid authority, both maker-checker modes,
original-maker rechecks, higher-authority checker denial, authority changes,
API/batch/import construction, reserved metadata injection, query/header and approval
spoofing, trusted import approval, flat payload validation, audit compatibility,
legacy/malformed origins, retry/reject behavior, and excluded command dispatch.
The 18 annual-fee cases include the original 10 focused tests and 8 maker-checker
regressions. Remaining tests cover existing policy, command processing and savings
financial-service regressions.

Earlier validation exposed a test-fixture enum setter compilation error and a
missing tenant timezone in the spreadsheet fixture; both were corrected. No failures
remain in the final run.

Complete diff review found only the intended 12 production files, four new test
files and this report. No migrations, generated files, permissions, routes,
financial service implementations or unrelated changes are included. The branch is
ready for review/commit within the validation limits above; nothing was committed.

## Exact changed files

- `docs/changes/savings-deposit-authority.md`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/CommandWrapper.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsDepositCommandEnvelope.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsDepositExecutionContext.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsDepositOrigin.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/handler/SavingsDepositCommandHandler.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/service/CommandSourceService.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/service/CommandWrapperBuilder.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/service/PortfolioCommandSourceWritePlatformServiceImpl.java`
- `fineract-provider/src/main/java/org/apache/fineract/commands/service/AuditReadPlatformServiceImpl.java`
- `fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/savings/SavingsTransactionImportHandler.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountTransactionsApiResource.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/DepositSavingsAccountCommandHandler.java`
- `fineract-provider/src/test/java/org/apache/fineract/commands/service/SavingsDepositAuditTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/commands/service/SavingsDepositEnvelopeTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/api/SavingsDepositOriginTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsDepositAuthorityTest.java`

## Recommended commit and PR text

Commit subject: `feat(savings): enforce maker deposit authority for staff API commands`

PR title: `Enforce maker monetary authority for ordinary savings deposits`
(Add the project ticket identifier if required by the repository contribution workflow.)

PR description:

Ordinary API and batch savings deposits previously bypassed the existing DEPOSITS
monetary-authority policy. Persist server-assigned API/import origin in the existing
command JSON and enforce the original maker's current authority before financial
writes, including approval and retries. Spreadsheet imports and internal financial
paths retain their existing behavior. Legacy pending deposits without trusted origin
must be cancelled and resubmitted; audit payloads remain flat. No schema migration.

Validation: focused command, origin, authority, maker-checker, audit and existing
savings regressions; scoped Spotless and `git diff --check`. Database-backed
transaction/rollback and running-server end-to-end tests were not run.

No commit or PR has been created. For a future commit, retain the repository's
required sign-off and `Assisted-By: Codex-GPT-6` attribution.
