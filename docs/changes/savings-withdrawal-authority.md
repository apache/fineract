# Savings withdrawal authority

Implementation resumed on `feat/savings-withdrawal-authority` from
`e983ec9de80db771fdc18314c4a6e2df02b608ed`. The policy decision is approved; the
previous stop is superseded. Existing tracked and untracked work was inspected
before edits, including status, diff check, diff statistics, the complete tracked
diff and all five new Java files. Valid unfinished work was preserved.

## Review of unfinished work

The unfinished changes supplied a shared codec, typed transaction kinds/origins,
explicit execution context, deposit compatibility adapter, command dispatch and
audit display, plus API/import origin assignment. They matched the approved design
but had no enforcing withdrawal handlers and no closure origin assignment. Adjustment
and closure checks, tests and formatting were missing. The generalized codec had
changed the deposit reserved-metadata error wording; the exact old wording was restored.

## Behavior matrix

| Operation | Behavior |
| --- | --- |
| Direct or external-ID savings withdrawal | Original maker's current WITHDRAWALS; account currency; full requested amount; inclusive bounds |
| Batch withdrawal | Same handler and authority; existing batch transaction handling unchanged |
| Cashier/teller withdrawal | Same check; no status-based exemption; existing cash controls unchanged |
| Ordinary overdraft withdrawal | Full requested amount checked; existing overdraft and balance validation downstream |
| Force withdrawal | WITHDRAWALS plus existing FORCE_WITHDRAWAL_SAVINGSACCOUNT permission and exceptional balance rules |
| Spreadsheet withdrawal | Trusted SPREADSHEET_IMPORT provenance, mandatory authority for every row, original import maker, existing row success/error reporting |
| Withdrawal adjustment | Full replacement amount checked before payment-detail persistence, undo, repost or accounting |
| Deposit adjustment | Existing financial behavior retained; no WITHDRAWALS check; new adjustment commands carry trusted metadata |
| Closure withdrawing a positive balance | Actual calculated balance checked inside the transaction before payment details or withdrawal |
| Closure without a balance withdrawal | No monetary-authority invocation |
| Grouped savings closure | Distinct GSIM_CLOSE envelope; explicit original maker passed to each child closure |
| Pending legacy withdrawal-related commands | Missing, malformed, unknown, mismatched or unsupported metadata fails closed; cancel/resubmit domain error |
| Completed history and successful idempotent replay | History remains readable; replay preserves existing result/exception mechanism without new authority evaluation |
| Pure undo/reversal | Existing handlers and flat command storage; no new withdrawal-authority evaluation |
| Account transfers | Exempt from WITHDRAWALS; separate TRANSFER policy domain; existing permission, validation, accounting and maker-checker processing unchanged |
| Valid manual charge payments | Exempt; existing charge obligation, ceiling and PAY_SAVINGSACCOUNTCHARGE permission apply |
| Deposits | Stored version-1 bytes, origin values, authority/import behavior, Java compatibility adapter and audit presentation preserved |
| Other commands and internal debits | Existing handling and flat command JSON unchanged |

Approval and retry decode the persisted envelope once and use `CommandSource.maker`,
never the checker, for monetary authority. The checker continues to supply approval
permission and audit attribution. Flat payloads reach validators and financial services.
Authorization runs before AWAITING_APPROVAL and before the protected financial operation.

## Exemption evidence and backlog

`MonetaryAuthorityType.TRANSFER` exists. Direct and batch CREATE_ACCOUNTTRANSFER
remain staff-selectable operations in a separate policy domain, not system-generated
debits. Transfers use their existing withdrawal-domain call with no new WITHDRAWALS
guard in that shared domain service.

**High-priority backlog:** implement TRANSFER monetary-authority enforcement in a
separate change for staff-initiated transfers. Define original-maker handling,
currency/amount rules, approval/retry and scheduled-execution distinctions there.
This implementation is not a universal limit on every staff-initiated savings debit.

Manual savings charge exemption checks:

1. `SavingsAccountChargeDataValidator.validatePayCharge` requires a positive amount;
   `SavingsAccount.payCharge(BigDecimal, ...)` rejects payment above outstanding.
2. Supported parameters are amount, due date, date format, locale and note. No
   beneficiary, destination account or cash-payment parameter is accepted.
3. `payCharge` binds charge ID to savings ID and creates charge/fee transactions
   tied to that obligation, not customer cash withdrawals.
4. This route does not dispatch an account transfer or select a destination.
5. The API builds PAY_SAVINGSACCOUNTCHARGE and ordinary command permission checks remain.

Tests cover the outstanding ceiling, rejection of destination/cash parameters and
unintercepted dispatch of valid charge and transfer commands.

## Import identity and transaction boundaries

`BulkImportWorkbookServiceImpl` captures the request context and submitting user.
`SpringConfig.applicationEventMulticaster` uses the existing
DelegatingSecurityContextAsyncTaskExecutor. `BulkImportEventListener` restores the
Fineract event context before processing the workbook. The command pipeline obtains
the original AppUser from the propagated security context and persists it as maker.
The new asynchronous test exercises the real multicaster, listener, workbook importer,
security-context reader, command framework and authority policy with mocked persistence:
one allowed and one denied row must report one success and one error.
The workbook includes a valid Cash payment-type lookup; assertions verify both the
success marker and the explicit WITHDRAWALS denial, plus two original-maker policy
lookups, so an unrelated validation error cannot satisfy this test.
That test exposed response-only `entryType` and `isOverdraft` fields in the import
DTO serialization. Withdrawal rows now omit those fields before forming their flat
business request; deposit payloads and downstream overdraft validation are unchanged.

Closure assembly with pivot mode false loads the account and sets helpers, while
`runTheCheckForProduct` only reads datatable requirements/counts. Closure's interest
branch validates existing interest postings; it does not post interest. The check
uses the same local balance amount subsequently passed to domain withdrawal. Existing
SavingsAccount @Version optimistic concurrency control is retained. No balance
calculation is duplicated in a handler.

No new ThreadLocal, security-context impersonation, global mutable state, database
migration or rewrite of existing records was introduced in production code.

## Validation

Focused validation completed on 2026-09-26: **189 passed, 0 failed, 0 errors,
0 skipped**, across 18 suites. Gradle reported `BUILD SUCCESSFUL` in 1m 59s.
A separate fresh scoped Spotless run (`--rerun-tasks`) also reported
`BUILD SUCCESSFUL` in 51s; its task inputs were verified to include all 30 changed
Java files (10 core, 1 savings, 19 provider). `git diff --check` passed.

| Suite | Passed | Failed | Skipped |
| --- | ---: | ---: | ---: |
| `SavingsAccountWritePlatformServiceJpaRepositoryImplTest` | 14 | 0 | 0 |
| `CreateAccountTransferCommandStrategyTest` | 1 | 0 | 0 |
| `CommandSourceServiceTest` | 3 | 0 | 0 |
| `SavingsDepositAuditTest` | 6 | 0 | 0 |
| `SavingsDepositEnvelopeTest` | 11 | 0 | 0 |
| `SavingsWithdrawalEnvelopeTest` | 16 | 0 | 0 |
| `SynchronousCommandProcessingServiceTest` | 12 | 0 | 0 |
| `NsimbiUserMonetaryAuthorityTest` | 2 | 0 | 0 |
| `NsimbiMonetaryAuthorityPolicyServiceTest` | 1 | 0 | 0 |
| `SavingsAccountTransfersServiceImplTest` | 6 | 0 | 0 |
| `SavingsAnnualFeeCommandTest` | 8 | 0 | 0 |
| `SavingsDepositOriginTest` | 6 | 0 | 0 |
| `SavingsWithdrawalOriginTest` | 6 | 0 | 0 |
| `SavingsAnnualFeeMakerCheckerTest` | 8 | 0 | 0 |
| `SavingsDepositAuthorityTest` | 37 | 0 | 0 |
| `SavingsWithdrawalAuthorityTest` | 43 | 0 | 0 |
| `SavingsWithdrawalChargeExemptionTest` | 7 | 0 | 0 |
| `SavingsAnnualFeeSchedulerTest` | 2 | 0 | 0 |

Recreate the temporary scoped configuration from the repository root before running
the command below (the script includes modified and untracked Java files):

```sh
python3 - <<'PYTHON'
from pathlib import Path
import json
import subprocess

files = subprocess.check_output(
    ['git', 'ls-files', '-m', '-o', '--exclude-standard'], text=True
).splitlines()
script = ['allprojects { p -> p.afterEvaluate {',
          'p.tasks.withType(Test).configureEach { maxHeapSize = "768m"; maxParallelForks = 1 }']
for module in ['fineract-core', 'fineract-savings', 'fineract-provider']:
    targets = [str(Path(f).resolve()) for f in files
               if f.startswith(module + '/') and f.endswith('.java')]
    script.append('if (p.name == ' + json.dumps(module) + ') { '
                  'p.extensions.getByName("spotless").java { target(p.files('
                  + json.dumps(targets) + ')) } }')
script.append('} }')
Path('/tmp/savings-withdrawal-tests.gradle').write_text('\n'.join(script) + '\n')
PYTHON

./gradlew --offline --no-daemon --max-workers=1 --no-parallel \
  -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/savings-withdrawal-tests.gradle \
  :fineract-core:spotlessJavaApply :fineract-savings:spotlessJavaApply \
  :fineract-provider:spotlessJavaApply :fineract-provider:test \
  --tests '*SavingsWithdrawal*Test' --tests '*SavingsDeposit*Test' \
  --tests '*SavingsAnnualFee*Test' --tests '*NsimbiUserMonetaryAuthorityTest' \
  --tests '*NsimbiMonetaryAuthorityPolicyServiceTest' \
  --tests '*CommandSourceServiceTest' --tests '*SynchronousCommandProcessingServiceTest' \
  --tests '*SavingsAccountWritePlatformServiceJpaRepositoryImplTest' \
  --tests '*SavingsAccountTransfersServiceImplTest' \
  --tests '*CreateAccountTransferCommandStrategyTest' \
  :fineract-core:spotlessJavaCheck :fineract-savings:spotlessJavaCheck \
  :fineract-provider:spotlessJavaCheck -x :fineract-provider:resolve

git diff --check
```

The temporary init script limits test JVMs to 768 MiB and one fork, and scopes
Spotless Java targets to exactly the changed Java files in each module. The unrelated
Swagger resolve task is excluded. Build log: `/tmp/withdrawal-build.log`.
JUnit XML: `fineract-provider/build/test-results/test/`; HTML report:
`fineract-provider/build/reports/tests/test/index.html`.

The independent formatting verification used the same absolute targets and heap
settings, with diagnostic logging of actual Java inputs and formatter steps:

```sh
./gradlew --offline --no-daemon --max-workers=1 --no-parallel --rerun-tasks \
  -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/savings-withdrawal-final.gradle \
  :fineract-core:spotlessJavaApply :fineract-savings:spotlessJavaApply \
  :fineract-provider:spotlessJavaApply :fineract-core:spotlessJavaCheck \
  :fineract-savings:spotlessJavaCheck :fineract-provider:spotlessJavaCheck
```

Formatting log: `/tmp/withdrawal-format.log`. For reproduction, the generated
`/tmp/savings-withdrawal-tests.gradle` above supplies the equivalent scope without
the diagnostic logging.

The resumed run initially had 188 passes and one failure because the async workbook
fixture lacked a valid payment type. Correcting that fixture and asserting the exact
authority denial produced the final passing results above. No authority check or
financial validation was weakened to make the test pass.

## Limitations and risks

- Tests exercise real command orchestration with mocked persistence/financial services,
  plus selected domain methods. No live database transaction rollback, concurrent
  account mutation, real teller cash payout or full HTTP-container test was run.
- Deploy application nodes consistently: old code cannot interpret new version-2
  withdrawal metadata. Pending legacy adjustment and closure commands are also
  metadata-protected and must be cancelled/resubmitted; no origin is inferred from history.
- Context-free legacy Java adjustment/closure methods remain callable, but cannot
  authorize a new withdrawal. Custom service implementations need the new explicit-context
  overloads. Ordinary deposit APIs and stored deposit records retain compatibility.
- Grouped closure checks each child immediately before that child's withdrawal within
  the enclosing transaction; it does not aggregate child amounts into one limit.
- Transfer authority remains the separate high-priority backlog item above.

## Recommended commit message

`feat(savings): enforce original-maker withdrawal monetary authority`

## Recommended PR description

Savings withdrawals previously bypassed Nsimbi monetary limits. Enforce the original
maker's current WITHDRAWALS authority for direct, batch, spreadsheet, cashier,
overdraft and force withdrawals, full replacement withdrawal adjustments, and actual
closure balance payouts. Approval and retry retain the maker while preserving checker
permissions and audit attribution.

Generalize server command envelopes with typed version-2 withdrawal metadata while
preserving deposit version-1 storage and behavior. Pending untrusted withdrawal-related
commands require cancellation/resubmission; completed history and replay remain readable.
Transfers and constrained manual charge settlements retain their approved exemptions.

Validation: see the focused results above. No migration. Live database/concurrency and
HTTP-container behavior were not exercised. Coordinate application-node rollout for
version-2 metadata and plan legacy pending-command resubmission.

## Exact changed files

- `docs/changes/savings-withdrawal-authority.md`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/CommandWrapper.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsDepositCommandEnvelope.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionCommandEnvelope.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionExecutionContext.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionKind.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionOrigin.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/handler/SavingsTransactionCommandHandler.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/service/CommandSourceService.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/service/CommandWrapperBuilder.java`
- `fineract-core/src/main/java/org/apache/fineract/commands/service/PortfolioCommandSourceWritePlatformServiceImpl.java`
- `fineract-provider/src/main/java/org/apache/fineract/commands/service/AuditReadPlatformServiceImpl.java`
- `fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/savings/SavingsTransactionImportHandler.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountTransactionsApiResource.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountsApiResource.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/CloseGSIMCommandHandler.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/CloseSavingsAccountCommandHandler.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/ForceWithdrawalSavingsAccountCommandHandler.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/SavingsTransactionAdjustmentCommandHandler.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/WithdrawSavingsAccountCommandHandler.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsAccountWritePlatformServiceJpaRepositoryImpl.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsWithdrawalAuthorityService.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/starter/SavingsConfiguration.java`
- `fineract-provider/src/test/java/org/apache/fineract/commands/service/SavingsDepositAuditTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/commands/service/SavingsWithdrawalEnvelopeTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/api/SavingsWithdrawalOriginTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsDepositAuthorityTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsWithdrawalAuthorityTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsWithdrawalChargeExemptionTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/service/SavingsAccountWritePlatformServiceJpaRepositoryImplTest.java`
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsAccountWritePlatformService.java`

No commit, push or pull request was created.
