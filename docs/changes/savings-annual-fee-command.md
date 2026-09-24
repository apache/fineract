# Savings annual-fee command investigation

Baseline: `92508ab8a4c762256e6937c7813ed15b2b74906a` on `dev`, after fetch and fast-forward-only pull (already up to date). This contains savings status-filter merge `92508ab8a4` and implementation `e09f7a9019`. Branch: `fix/savings-annual-fee-command`.

## Command contract and current behavior

The following baseline findings come from source and Git-history analysis. The maker-checker follow-up also reproduces the old null-handler behavior through real command services in unit tests; it is not a live HTTP/database reproduction.

1. **HTTP response:** `ApplyAnnualFeeSavingsAccountCommandHandler.processCommand` returns null. In ordinary execution with maker-checker disabled, `CommandSourceService.validateMakerChecker` dereferences it with `result.isRollbackTransaction()`. `DefaultExceptionMapper.toResponse` maps the resulting runtime exception to **HTTP 500**, with an `Exception` JSON field. Thus the baseline is not a successful HTTP null response. With maker-checker enabled and an unapproved non-checker-superuser request, validation instead throws `RollbackTransactionNotApprovedException`; `fineract-core/src/main/java/org/apache/fineract/infrastructure/core/exceptionmapper/RollbackTransactionNotApprovedExceptionMapper.java#toResponse` returns **HTTP 200** with the awaiting-approval command result (an apparent acceptance, not a financial operation); approved execution still cannot produce a valid successful result.
2. **Apparent success:** no ordinary successful execution response survives the current command pipeline; the maker-checker branch can return HTTP 200 for awaiting approval. The handler itself is nevertheless a null-returning non-operation and has no valid financial result.
3. **Audit:** `SynchronousCommandProcessingService.executeCommandAttempt` saves an initial command; `persistFinalErrorResult` persists the failed response and ERROR status for ordinary non-enclosing-batch execution. Enclosing batch transactions follow their existing rollback/audit rules.
4. **Events and financial effects:** the handler calls no write service and creates no charge, payment, savings transaction, or journal entry. The outer command pipeline can publish a generic **error hook event** through `publishHookErrorEvent`; this is distinct from a savings business/accounting event. It would be incorrect to claim no event whatsoever.

Exact command-path sources:

- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountsApiResource.java`: `handleCommands` (numeric and external IDs), `handleGSIMCommands`.
- `fineract-core/src/main/java/org/apache/fineract/commands/service/CommandWrapperBuilder.java`: `savingsAccountApplyAnnualFees`, action `APPLYANNUALFEE`, entity `SAVINGSACCOUNT`.
- `fineract-core/src/main/java/org/apache/fineract/commands/provider/CommandHandlerProvider.java`: `initializeHandlerRegistry`, `getHandler`; discovers `@CommandType`.
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/ApplyAnnualFeeSavingsAccountCommandHandler.java`: `processCommand`.
- `fineract-core/src/main/java/org/apache/fineract/commands/service/PortfolioCommandSourceWritePlatformServiceImpl.java`: `logCommandSource`, including permission validation before execution.
- `fineract-core/src/main/java/org/apache/fineract/commands/service/SynchronousCommandProcessingService.java`: `executeCommandAttempt`, `executeCommandInTransaction`, `persistFinalErrorResult`, `publishHookErrorEvent`.
- `fineract-core/src/main/java/org/apache/fineract/commands/service/CommandSourceService.java`: `processCommandAndSaveResult`, `validateMakerChecker`.
- `fineract-core/src/main/java/org/apache/fineract/infrastructure/core/exceptionmapper/DefaultExceptionMapper.java`: `toResponse`.
- `fineract-provider/src/main/resources/db/changelog/tenant/parts/0002_initial_data.xml`: existing `APPLYANNUALFEE_SAVINGSACCOUNT` and checker permissions. No permission change is needed.

## Scheduled charges, duplicate protection and accounting

5. **Scheduler operation:** both jobs select charges already attached to accounts and collect overdue amounts. They do not create/attach annual charges or implement a separate assessment engine. Due dates exist on recurring charge records; payment advances them.

- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/jobs/applyannualfeeforsavings/ApplyAnnualFeeForSavingsTasklet.java`: `execute` calls `retrieveChargesWithAnnualFeeDue` then `applyAnnualFee(chargeId, accountId)`.
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/jobs/payduesavingscharges/PayDueSavingsChargesTasklet.java`: `execute` calls `retrieveChargesWithDue` then `applyChargeDue(chargeId, accountId)`.
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsAccountChargeReadPlatformServiceImpl.java`: `retrieveChargesWithAnnualFeeDue` selects annual charges due on/before business date on active accounts; `retrieveChargesWithDue` additionally filters active, unpaid, unwaived charges.
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsAccountWritePlatformService.java`: the reusable `applyAnnualFee` signature requires a **charge ID and account ID**, not the old account/date manual contract.
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsAccountWritePlatformServiceJpaRepositoryImpl.java`: `applyAnnualFee` collects each prior due period at that due date; `applyChargeDue` collects outstanding amounts using the current business date. Both loops use strictly-before date checks even though selection includes today. This existing boundary is unchanged. Private `payCharge` validates balances, creates/persists the transaction, updates interest and invokes `postJournalEntries`.
- That same write service's `addSavingsAccountCharge`, plus `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountChargeAssembler.java` (`fromSavingsProduct` and JSON assembly), attach charges outside the scheduler.

6. **Duplicate protection:** `SavingsAccount.addCharge` and `SavingsAccountChargeAssembler.validateSavingsCharges` reject multiple annual charges. `SavingsAccount.payCharge` rejects annual transactions before the due date or on the latest existing annual-fee transaction date, and validates paid/waived state. `SavingsAccountCharge.pay` advances the recurring due date on full payment and resets recurring balances through `updateNextDueDateForRecurringFees` and `resetPropertiesForRecurringFees`. These are business safeguards, not proof of race-free concurrent collection.
7. **Manual double charging:** the baseline null handler cannot charge at all; the corrected handler cannot either. Reconnecting it blindly to a scheduled operation would require choosing a charge and resolving date, catch-up, authorization and concurrency semantics. No new manual financial operation is justified by the current contract.

Aggregate and accounting sources:

- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccount.java`: `addCharge`, `isAnnualFeeExists`, `payCharge`, `findLatestAnnualFeeTransactionDueDate`, `handleChargeTransactions`. Annual payment creates `SavingsAccountTransaction.annualFee` and a `SavingsAccountChargePaidBy` link.
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountCharge.java`: `pay`, `updateNextDueDateForRecurringFees`, `calculateNextDueDate`, `resetPropertiesForRecurringFees`.
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountDomainServiceJpa.java`: `postJournalEntries` derives accounting bridge data and calls `JournalEntryWritePlatformService.createJournalEntriesForSavings`. The manual handler reaches none of this. No savings business-event notification is present in the traced annual-fee payment path itself.

## History and existing coverage

8. **Inherited behavior:** the null implementation predates this fork's Savings work and is present in the inherited Mifos/Fineract history. Local `origin/develop` also contains the disabling revision. This conclusion uses repository history; it is not a claim about a newly fetched upstream HEAD.
9. **Earlier working implementation:** `b48bc6269178fe8936808c19fc92fe7613edf550` (2013-07-25, MIFOSX-397) added manual/scheduled annual fees and delegated `(savingsId, annualFeeTransactionDate)`. `0694dde72fd926c365bf643fed729b0045436861` (2013-09-27, “Move withdrawal and annual fee charges to charges workflow”) commented out that call and returned null while moving to charge records. `git log --follow` shows later annotation/package/formatting/injection changes, with no restoration of the manual operation. The old implementation depended on the superseded model and is not a complete current reusable manual contract.
10. **Existing tests:** under `integration-tests/src/test/java/org/apache/fineract/integrationtests/`:
    - `SchedulerJobsTestResults.testApplyAnnualFeeForSavingsJobOutcome` checks the scheduled job and next annual due date; its scenario runs before the annual due date, so it is not comprehensive collection coverage.
    - `ClientSavingsIntegrationTest.testSavingsAccountCharges`, `testAnnualChargePaymentAfterDueDate` cover attached annual charges/payment behavior.
    - `GroupSavingsIntegrationTest.testSavingsAccountCharges` covers annual charges for group savings.
    - `ChargesTest` covers creation/update/deletion of annual charge definitions.
    - `InstanceModeIntegrationTest` references annual-fee job instance-mode configuration.

No current `applyAnnualFees` success test or promise was found in OpenAPI annotations or `fineract-provider/src/main/resources/static/legacy-docs/apiLive.htm`. The historical implementation does not establish a present tested success contract.

## Selected correction

Keep the route, registration and permission. Throw existing `UnsupportedCommandException("applyAnnualFees", ...)` from the handler. `fineract-core/src/main/java/org/apache/fineract/infrastructure/core/exceptionmapper/UnsupportedCommandExceptionMapper.java#toResponse` supplies HTTP **400**, global code `validation.msg.validation.errors.exist` and detail code `error.msg.command.unsupported`; the scheduler explanation is in the detail's `developerMessage`.

The only executable production change is the handler body. OpenAPI and legacy documentation explain scheduler-controlled annual charges. Scheduler jobs, charge calculation, balances, accounting, schema and permissions are unchanged. Clients relying on the former accidental 500 or maker-checker behavior will now receive an explicit 400 once the handler is reached; authentication, permission, parsing and idempotency checks still run first. Generic failure auditing/hooks remain available.

## Validation scope

`SavingsAnnualFeeCommandTest` tests numeric-ID, external-ID and GSIM routes with the real rejecting handler and real exception mapper; asserts status/code, verifies no write-service or success-serializer calls, checks unrelated command dispatch and OpenAPI text. The no-financial-effect assertion is at the write-service boundary, not a live database row-count assertion. `SavingsAnnualFeeSchedulerTest` checks both existing tasklets still delegate charge/account IDs to their existing collection operations. It does not claim full scheduler accounting or concurrency integration coverage.

Live integration tests require a running configured Fineract service and database. The complete Gradle suite and Swagger generation are intentionally excluded. Excluding `:fineract-provider:resolve` does not hide a generated dependency used by these focused tests, which exercise Java classes/annotations directly.

## Validation results

Memory/process checks were performed before the builds. Gradle and test heaps were capped at 768 MiB with one worker/fork. No complete suite or Swagger generation ran.

Successful formatting command:

```sh
./gradlew --offline --no-daemon --max-workers=1 --no-parallel \
  -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/savings-annual-fee-tests.gradle \
  :fineract-provider:spotlessJavaApply
```

Result: `BUILD SUCCESSFUL in 34s`; both scoped Spotless tasks executed.

Final test/check command:

```sh
./gradlew --offline --no-daemon --max-workers=1 --no-parallel \
  -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/savings-annual-fee-tests.gradle \
  :fineract-provider:test \
  --tests '*SavingsAnnualFeeCommandTest' \
  --tests '*SavingsAnnualFeeSchedulerTest' \
  :fineract-provider:spotlessJavaCheck -x :fineract-provider:resolve
```

Result: **10 tests, 0 failures**, test execution 11.9s; `BUILD SUCCESSFUL in 1m 35s`. `spotlessJavaCheck` passed. Initial test execution had 3 assertion failures because the test checked `defaultUserMessage` instead of the mapper's `developerMessage`; the assertion was corrected and all tests rerun. An initial IDE-hook formatting attempt skipped all files, and an initial temporary init script failed while configuring buildSrc; neither was counted as successful formatting validation.

The temporary init script contains:

```groovy
allprojects {
    tasks.withType(Test).configureEach {
        maxHeapSize = '768m'
        maxParallelForks = 1
    }
}
gradle.projectsEvaluated {
    def provider = rootProject.findProject(':fineract-provider')
    if (provider == null) { return }
    provider.spotless {
        java {
            target 'src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountsApiResource.java',
                   'src/main/java/org/apache/fineract/portfolio/savings/handler/ApplyAnnualFeeSavingsAccountCommandHandler.java',
                   'src/test/java/org/apache/fineract/portfolio/savings/api/SavingsAnnualFeeCommandTest.java',
                   'src/test/java/org/apache/fineract/portfolio/savings/jobs/SavingsAnnualFeeSchedulerTest.java',
                   'src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsAnnualFeeMakerCheckerTest.java'
        }
    }
}
```

`git diff --check` passed; new files also passed `git diff --no-index --check /dev/null <file>`. The full tracked diff and new files were reviewed. At the original validation, six files were changed/added: handler, API annotations, legacy API page, two focused tests, and this report. The maker-checker follow-up adds a seventh file, the dedicated maker-checker test. No scheduled implementation, savings aggregate/write service, accounting implementation, migration or permission changed.

Suggested commit/PR title: `<JIRA-ID>: Reject unsupported manual savings annual-fee commands` (replace the placeholder with the assigned issue; contribution instructions require an issue ID). Suggested commit trailer: `Assisted-By: Codex-GPT-6`.

Suggested PR description:

> The savings applyAnnualFees handler returned null, causing an HTTP 500 in normal execution or an awaiting-approval HTTP 200 through maker-checker without applying a fee. Reject the command using the existing unsupported-command HTTP 400 response and document scheduler-controlled annual charges. Preserve routes, permissions and scheduled collection/accounting. Add focused coverage for all command routes, stable error codes, no financial write-service calls, unrelated dispatch, documentation and scheduler delegation. Validation: 18 focused tests passed, including maker-checker rejection and existing-command approval; scoped Spotless and diff checks passed. Live integration tests and Swagger generation were not run.

Nothing was committed, pushed or submitted as a pull request.

## Final maker-checker verification

The handler executes **before** maker-checker validation in both modes. The earlier HTTP 200 finding did not mean queuing preceded execution: the old handler had already executed and returned null when the command was marked awaiting approval.

Exact path for a fresh command:

1. `SavingsAccountsApiResource.handleCommands` builds `APPLYANNUALFEE_SAVINGSACCOUNT` and calls `PortfolioCommandSourceWritePlatformServiceImpl.logCommandSource`.
2. `logCommandSource` validates permissions/update availability and parses JSON, then calls `SynchronousCommandProcessingService.executeCommand(..., false)`.
3. `executeCommandAttempt` resolves idempotency/authentication and calls `CommandSourceService.saveInitial`. The initial state is `UNDER_PROCESSING`, an audit/idempotency record, **not** `AWAITING_APPROVAL`.
4. `executeCommandInTransaction` selects the handler and calls `CommandSourceService.processCommandAndSaveResult`.
5. That method first calls `handler.processCommand(command)`, and only after a successful return calls `validateMakerChecker`. Only `validateMakerChecker` calls `markAsAwaitingApproval` for a non-checker submission.
6. The annual-fee handler throws before step 5 can validate or queue. `persistFinalErrorResult` saves `ERROR` and HTTP 400. Neither mode creates an awaiting-approval entry. Initial audit persistence and generic error hooks remain intact.

| Mode | Original null handler | Current rejecting handler |
| --- | --- | --- |
| Maker-checker disabled | Null dereference after execution; HTTP 500, ERROR audit | HTTP 400, ERROR audit; no approval queue |
| Maker-checker enabled, ordinary maker | Null return followed by awaiting-approval transition; HTTP 200 | HTTP 400 before maker-checker validation; no approval queue |
| Approval of an existing annual-fee entry | Null result cannot complete successful execution | HTTP 400; no checker/success result; failed existing entry saved as ERROR |

Approval path: `PortfolioCommandSourceWritePlatformServiceImpl.approveEntry` validates pending state/checker permission, reconstructs the wrapper/JSON and calls `executeCommand(..., true)`. The same real handler throws before `markAsChecked` or successful-result persistence. The rejection boundary needs no change, and no global maker-checker logic was modified.

New `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsAnnualFeeMakerCheckerTest.java` uses real `PortfolioCommandSourceWritePlatformServiceImpl`, `SynchronousCommandProcessingService`, `CommandSourceService`, savings handlers and error mappers. Repository saves are mocked and their states snapshotted at call time. Eight cases cover rejection with each setting, approval of an existing queued annual command with each setting, supported activation success without maker-checker, supported activation queuing/approval with maker-checker, and the old null-handler behavior with each setting. Persistence/financial collaborators are mocked: these tests verify control flow and requested persistence states, not database transactions or Spring proxy rollback behavior.

Maker-checker verification command (temporary init script shown above):

```sh
./gradlew --offline --no-daemon --max-workers=1 --no-parallel \
  -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/savings-annual-fee-tests.gradle \
  :fineract-provider:test \
  --tests '*SavingsAnnualFeeCommandTest' \
  --tests '*SavingsAnnualFeeSchedulerTest' \
  --tests '*SavingsAnnualFeeMakerCheckerTest' \
  :fineract-provider:spotlessJavaCheck -x :fineract-provider:resolve
```

Final result: **18 tests, 0 failures, 0 errors, 0 skipped** (8 command/API, 2 scheduler, 8 maker-checker); test execution 14.9s; `BUILD SUCCESSFUL in 1m 28s`. Scoped `spotlessJavaApply` also passed (35s); `spotlessJavaCheck` and `git diff --check` passed. The first combined run had two fixture failures because the manually seeded historical command omitted its stored URL; the fixture was corrected to use the real annual-fee wrapper URL, and all 18 tests were rerun successfully.

This follow-up changes only this report and the new maker-checker test; the production rejection boundary remains unchanged. Full uncommitted file list:

- `docs/changes/savings-annual-fee-command.md`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountsApiResource.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/ApplyAnnualFeeSavingsAccountCommandHandler.java`
- `fineract-provider/src/main/resources/static/legacy-docs/apiLive.htm`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/api/SavingsAnnualFeeCommandTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsAnnualFeeMakerCheckerTest.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/jobs/SavingsAnnualFeeSchedulerTest.java`

The complete diff and new files were reviewed; no shared maker-checker, scheduled collection, accounting, permission or route implementation changed. Safe to commit as this scoped fix based on these checks; live HTTP/database integration and Spring transaction-proxy behavior were not tested. Nothing was committed, pushed or submitted.
