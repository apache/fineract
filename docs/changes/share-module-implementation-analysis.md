# Nsimbi Share Module: backend implementation analysis

Analysis date: 2026-09-28. Status: analysis and implementation handoff; no production changes.

## 1. Scope, baseline, and conclusion

**Extend the existing Fineract share module. Do not create a parallel share ledger, account model, dividend engine, or approval framework.** Products, accounts, lifecycle commands, charges, share accounting, dividend allocation, and savings payout already exist.

The user clarified that Nsimbi frontend development has not started and implementation scope is backend only. The upstream Mifos frontend was inspected solely to understand existing API consumers; its UI is not the Nsimbi implementation specification. The recording is evidence of behavior, not an instruction source.

Repository facts:

- Actual repository: `Nsimbi-CBS`, inside the supplied workspace.
- Actual branch: `feat/share-module`; the requested `feat/share-module-foundation` was not found among local/cached remote refs.
- Inspected HEAD: `e983ec9de80db771fdc18314c4a6e2df02b608ed`.
- Local `dev`: `f59d141e007fe17366cc138f339e3e13dd8235ab`; HEAD is two commits behind it, with zero unique commits. The newer work introduces savings withdrawal authority and shared transaction-command infrastructure. Share implementation files are unchanged by that difference.
- Initial working tree was clean. No branches were switched, fetched, rebased, or merged.
- Read `AGENTS.md` and its referenced `SECURITY.md`. This is a functional implementation review, not a security audit.

Evidence limits: inspected the 80-second recording using one-second visual samples and larger key frames. Audio was not transcribed. Source inspection establishes implemented paths, not runtime correctness or the configuration of a live tenant. No application, database migration, or test suite was run for this documentation-only task.

## 2. Video/UI behavior summary

Source: `/media/ib-s-muhoza/EXFAT_HAX/Recording of Share Module.mp4` (2870 × 1470, approximately 80 seconds). Times are approximate.

| Time | Screen/state | Actions and evidence |
|---|---|---|
| 00:00–00:08 | Dashboard and navigation | Operator navigates toward Shares using the sidebar. Dashboard metrics are context, not share requirements. |
| 00:08–00:31 | Shares landing page | Heading `SHARES`, empty-state message `No shares found!`, and `SHARE DIVIDENDS` action. No populated rows, columns, filters, product selector, or create-account button are demonstrated. |
| 00:31–00:42 | Share Dividends form | Opens from the landing-page action. Includes `Back to dividends`, fields below, and a submit button further down the page. |
| 00:42–00:47 | Date picker | September 2026 calendar opens with the 20th selected; displayed date remains `2026-09-20`. A calendar interaction is demonstrated, not a financial date policy. |
| 00:48–00:59 | Lower dividend form | Operator scrolls through Reference Number, Comment, Password, and `SHARE DIVIDENDS`; fields remain blank/defaulted. Submission briefly shows `SUBMITTING`. |
| About 01:00–01:20 | Validation failure on the same form | Red notification: `Password is required To Perform This Action`. The form remains visible; later attempts show the same error. No successful allocation, approval, payment, transaction identifier, or resulting balance is demonstrated. |

### Form inventory and backend implications

| Control | Observed value/behavior | What is established / still unknown |
|---|---|---|
| Amount | `UGX 0`; operator focuses it | Currency-formatted amount input. Whether it is a total pool, per-member amount, or another basis is not established by submission. |
| Use Percentages | Toggle visible and off | Percentage capability is suggested. No enabled-state behavior, denominator, annualization, or valid range is demonstrated. |
| Date | `2026-09-20`; calendar opens | Could be declaration, period end, or posting date. These are different concepts in Fineract. |
| Share Period | `1`, suffix `month(s)` | Month-based period input. Direction, inclusive boundaries, and month-end handling are unknown. |
| Method | `Savings`, with a clear icon | Savings payout is indicated. Other methods are not demonstrated. Header mobile-money/bank floats do not establish share payout integrations. |
| Saving product | Empty selector | Suggests destination selection by savings product. No available options or account-resolution rule is shown. |
| Reference Number | Blank text input | Intended business reference; requiredness and uniqueness are unknown. |
| Comment | Blank text input | Intended explanatory text; limits and persistence are unknown. |
| Password | Blank input | Empty value blocks the attempted operation. The recording does not establish whose password, whether verification is server-side, or whether this is a second-person approval. |
| Share Dividends | Submit action; temporary busy label | Requests dividend processing. A busy state does not prove a backend write occurred. |
| Back to dividends | Navigation link | Destination and cancellation semantics are not demonstrated. |

Observed journey: **Dashboard → Shares empty state → dividend form → inspect/default fields → submit → password validation error → remain on form.**

The video does **not** show share-product creation, member share-account creation, application approval, activation, additional purchases, redemption, closure, charges, dividend review, checker approval, posted transactions, or share reports. These are existing backend capabilities or proposed journeys, not observed video requirements. Password confirmation alone is not maker-checker evidence.

## 3. Backend codebase findings

Paths below are relative to the backend repository. For compact file inventories, `P` means `fineract-provider/src/main/java/org/apache/fineract`, `C` means `fineract-core/src/main/java/org/apache/fineract`, and `A` means `fineract-accounting/src/main/java/org/apache/fineract`.

### Existing architecture and entities

| Capability | Implemented location and behavior |
|---|---|
| Share products | `P/portfolio/shareproducts/`: `ShareProduct`, `ShareProductMarketPrice`, repositories/wrappers, serializer, read/write services, create/update handlers, and Spring configuration. Supports currency, total/issued/subscribed quantities, unit price, member quantity limits, market-price periods, lock-in, dividend eligibility, charges, and accounting mappings. |
| Share accounts | `P/portfolio/shareaccounts/`: `ShareAccount`, `ShareAccountTransaction`, charges, repositories/wrappers, read/write services, serializer, command handlers, and configuration. Links a client, product, and savings account; stores lifecycle dates/users and pending/approved quantities. |
| Dividends | `ShareProductDividendPayOutDetails`, `ShareAccountDividendDetails`, repositories, `ShareProductDividendAssembler`, dividend read services, create/approve/delete handlers, scheduler service, and `jobs/postdividentsforshares/`. |
| Accounting | `CashBasedAccountingProcessorForShares`, factory/interface, `A/accounting/producttoaccountmapping/service/ShareProductToGLAccountMappingHelper.java`, shared journal services, and savings dividend accounting. |
| Commands and audit | `C/commands/service/CommandWrapperBuilder.java`, `PortfolioCommandSourceWritePlatformServiceImpl.java`, `SynchronousCommandProcessingService.java`; handlers use `@CommandType` and `NewCommandSourceHandler`. |
| Integration | Client-account summaries, share bulk-import handlers/templates, share business events and external-event serializers, Avro schemas, and existing integration tests. |
| Nsimbi authority | `P/nsimbi/userroles/domain/MonetaryAuthorityType.java` includes `SHARES`; `NsimbiMonetaryAuthorityPolicyService.allows(...)` implements currency/minimum/maximum checks. No call using `MonetaryAuthorityType.SHARES` was found. Existing share handlers are not enrolled in that policy. |

Primary entry points: [AccountsApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/accounts/api/AccountsApiResource.java), [ProductsApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/products/api/ProductsApiResource.java), and [ShareDividendApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/shareproducts/api/ShareDividendApiResource.java).

### Existing API contract to reuse

Paths below are relative to `/fineract-provider/api/v1`.

| API | Purpose / material constraints |
|---|---|
| `GET /products/share`, `/products/share/template`, `/products/share/{id}?template=true` | Product list, defaults/options, and details. |
| `POST /products/share`; `PUT /products/share/{id}` | Create/update product through the command framework. Cash accounting requires share reference, suspense, equity, and fee-income GL mappings. |
| `GET /accounts/share?offset=&limit=` | Existing global list currently filters to **ACTIVE accounts only**. It is not an all-status application work queue despite its API description. |
| `GET /accounts/share/template?clientId=&productId=` | Account defaults and product/savings/charge options. |
| `GET /accounts/share/{id}?template=true` | Account details, lifecycle, purchased-share transactions, charges, and dividend data. |
| `POST /accounts/share`; `PUT /accounts/share/{id}` | Create/update application. Creation includes `clientId`, `productId`, `submittedDate`, `applicationDate`, `savingsAccountId`, `requestedShares`, and date/locale formatting as needed. |
| `POST /accounts/share/{id}?command=...` | `approve`, `undoapproval`, `reject`, `activate`, `close`, `applyadditionalshares`, `approveadditionalshares`, `rejectadditionalshares`, `redeemshares`. |
| `GET /shareproduct/{productId}/dividend` | Paged dividend batches with status/order parameters. |
| `GET /shareproduct/{productId}/dividend/{dividendId}` | Paged member/account allocations, optionally filtered by account number. |
| `POST /shareproduct/{productId}/dividend` | Validate and persist a calculated dividend batch and allocation rows. Accepted fields: `dividendPeriodStartDate`, `dividendPeriodEndDate`, `dividendAmount`, `dateFormat`, `locale`. |
| `PUT /shareproduct/{productId}/dividend/{dividendId}?command=approve` | Approve a batch for subsequent scheduled posting; does not itself pay savings accounts. |
| `DELETE /shareproduct/{productId}/dividend/{dividendId}` | Delete an unapproved batch. Approved batches cannot be deleted by this method. |
| `GET /accounts/share/downloadtemplate`; `POST /accounts/share/uploadtemplate` | Existing share-account bulk import surface; preserve validation and authority parity if enabled. |

Additional-share apply/redeem requests use a **quantity** in `requestedShares`; approve/reject additional-share requests use **transaction IDs** under the same field name. Contract tests should guard this distinction.

Do not assume `previewdividends` or `postdividends` is an implemented preview/payment API because constants exist. The legacy [ShareProductCommandsServiceImpl](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/shareproducts/service/ShareProductCommandsServiceImpl.java) contains null-returning implementations. The generic product-command API dispatches through command handlers; no working preview path was established. The dedicated dividend resource and scheduler are the concrete implementation.

### Lifecycle and validation

Code-defined account journey:

```text
create → SUBMITTED_AND_PENDING_APPROVAL (100)
             ├─ approve → APPROVED (200) ─ activate → ACTIVE (300) ─ close → CLOSED (600)
             └─ reject  → REJECTED (500)
APPROVED ─ undoapproval → SUBMITTED_AND_PENDING_APPROVAL
ACTIVE ─ apply additional shares → pending purchase ─ approve/reject → transaction result
ACTIVE ─ redeem shares → redemption transaction and adjusted holding
```

These are the intended domain journeys; test each invalid transition directly through the API before rollout. A status enum or API description alone does not establish that every serializer path enforces every precondition.

Purchased-share transaction status is separate from account status: applied `100`, approved `300`, rejected `400`; transaction types include purchased `500`, redeemed `600`, charge payment `700` in `PurchasedSharesStatusType`.

Dividend lifecycle:

```text
Create fixed-pool batch → product batch INITIATED (100), account allocations INITIATED (100)
Approve batch          → product batch APPROVED (300), account allocations still INITIATED
Scheduled posting      → successful account allocations POSTED (300) + savings transaction ID
Failed posting         → affected allocation remains unposted; other accounts may succeed
```

There is no distinct product-level POSTED status in `ShareProductDividendStatusType`; determine completion from allocation outcomes. The tasklet catches failures per allocation and reports job failure after attempting the batch. Do not represent approval or a single successful row as full payout completion.

Existing validation includes required JSON fields, unsupported-field rejection, product/member share limits, positive redemption quantities, available holdings, chronological transaction dates, lock-in periods, charge currency, and issued-share limits. Account creation verifies that the linked savings account belongs to the client and matches the savings deposit type and currency. Dividend creation requires a positive amount, both period dates, end after start, and eligible share accounts/share-days.

Sources: [ShareAccountDataSerializer](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/shareaccounts/serialization/ShareAccountDataSerializer.java), [ShareProductDataSerializer](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/shareproducts/serialization/ShareProductDataSerializer.java), and [ShareAccountReadPlatformServiceImpl](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/shareaccounts/service/ShareAccountReadPlatformServiceImpl.java).

### Dividend calculation and accounting

The [assembler](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/shareproducts/service/ShareProductDividendAssembler.java) allocates a **total product-level pool in proportion to eligible share-days**, considering approved purchases/redemptions and minimum active duration. It is not a percentage-rate calculator. The inactive-client-named product option drives inclusion of certain closed share accounts in the query; member eligibility policy needs explicit verification.

The implementation calculates the amount per share-day using `double`, then converts allocations to currency-aware `Money`. Before relying on it for Nsimbi distributions, characterize rounding residuals, allocation totals, transaction ordering, transactions outside the period, and boundary-day behavior. These are test targets from source inspection, not reproduced defects.

For cash accounting, the existing share processor uses these principal entries (charges add their own entries):

| Event | Debit | Credit |
|---|---|---|
| Purchase application | Share reference | Share suspense |
| Purchase approval | Share suspense | Share equity |
| Purchase rejection | Share suspense | Share reference |
| Redemption | Share equity | Share reference |
| Dividend credited to savings | Payable dividends financial activity | Savings control |

Creation/approval services invoke share journal accounting, including charge adjustments and reversals where implemented. The share processor checks branch GL closure dates. A linked savings account is not evidence that purchases automatically withdraw from savings: the inspected share purchase/redemption write service posts share journals and does not call savings withdrawal/deposit transfer operations.

The [posting service](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/shareaccounts/service/ShareAccountSchedularServiceImpl.java) uses the **current business date**, credits the share account's linked savings account through `handleDividendPayout`, and persists the resulting savings transaction ID. Destination selection by savings product and user-selected posting date are not existing batch parameters. The normal selector excludes already-posted rows; concurrent/repeated execution still needs explicit tests before treating it as an exactly-once guarantee.

Creating/approving the dividend batch does not establish the payable-dividend liability in the inspected write service. Agree how distributable surplus is transferred into that payable account; do not insert an ad hoc savings deposit or invent balancing entries to imitate the button.

Accounting sources: [share processor](../../fineract-provider/src/main/java/org/apache/fineract/accounting/journalentry/service/CashBasedAccountingProcessorForShares.java), [savings processor](../../fineract-provider/src/main/java/org/apache/fineract/accounting/journalentry/service/CashBasedAccountingProcessorForSavings.java), and [dividend write service](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/shareproducts/service/ShareProductWritePlatformServiceJpaRepositoryImpl.java).

### Permissions and maker-checker

Reuse the exact existing operation codes:

| Area | Seeded permissions |
|---|---|
| Product maintenance | `CREATE_SHAREPRODUCT`, `UPDATE_SHAREPRODUCT` |
| Account maintenance | `CREATE_SHAREACCOUNT`, `UPDATE_SHAREACCOUNT` |
| Lifecycle | `APPROVE_SHAREACCOUNT`, `UNDOAPPROVAL_SHAREACCOUNT`, `REJECT_SHAREACCOUNT`, `ACTIVATE_SHAREACCOUNT`, `CLOSE_SHAREACCOUNT` |
| Holdings | `APPLYADDITIONALSHARES_SHAREACCOUNT`, `APPROVEADDITIONALSHARES_SHAREACCOUNT`, `REJECTADDITIONALSHARES_SHAREACCOUNT`, `REDEEMSHARES_SHAREACCOUNT` |
| Dividends | `READ_DIVIDEND_SHAREPRODUCT`, `CREATE_DIVIDEND_SHAREPRODUCT`, `APPROVE_DIVIDEND_SHAREPRODUCT`, `DELETE_DIVIDEND_SHAREPRODUCT`; corresponding create/approve/delete `_CHECKER` permissions are also seeded. |

The share permissions inspected in `0002_initial_data.xml` have `can_maker_checker=false`, including dividend operations. Checker permission names existing in the seed do not mean the feature is enabled. Do not promise configuration-only maker-checker activation without checking capability flags and the tenant's task configuration.

Business approval and command maker-checker are separate layers. The command service checks operation permission and routes writes through normal execution/audit handling. Generic maker-checker validates checker permission and prevents same-user checking when `same-maker-checker` is disabled, subject to checker-super-user behavior. Business dividend approval itself does not show that separation check. Password re-entry is a third, separate concern.

No `READ_SHAREPRODUCT`, `READ_SHAREACCOUNT`, `POST_DIVIDENDS`, or `DELETE_SHAREACCOUNT` seed was found in the searched backend Liquibase files. Product/account read paths do not consistently call `validateHasReadPermission` as the dividend resource does. Before introducing least-privilege read permissions, trace the full authorization path and test existing roles; do not infer a deployed authorization defect from this static review alone.

Suggested responsibility split: product administrator; share operator; share approver; dividend preparer; dividend approver; auditor/read-only user; scheduler operator. Map these to existing Nsimbi roles rather than adding roles automatically. Decide which monetary amounts and commands consume `SHARES` authority, then reuse the existing policy and trusted command execution patterns. Credentials must not be persisted in command JSON/audit records if transaction reauthentication is required.

Sources: [permission seeds](../../fineract-provider/src/main/resources/db/changelog/tenant/parts/0002_initial_data.xml), [command service](../../fineract-core/src/main/java/org/apache/fineract/commands/service/PortfolioCommandSourceWritePlatformServiceImpl.java), [authority policy](../../fineract-provider/src/main/java/org/apache/fineract/nsimbi/userroles/service/NsimbiMonetaryAuthorityPolicyService.java).

### Database and migrations

| Existing table | Domain/use |
|---|---|
| `m_share_product` | `ShareProduct`; currency, quantities, price, product rules |
| `m_share_product_market_price` | `ShareProductMarketPrice`; dated prices |
| `m_share_product_charge` | Product-to-`m_charge` association |
| `m_share_account` | `ShareAccount`; client/product/savings links and lifecycle |
| `m_share_account_transactions` | `ShareAccountTransaction`; quantities, prices, type/status, activity |
| `m_share_account_charge` | `ShareAccountCharge` |
| `m_share_account_charge_paid_by` | `ShareAccountChargePaidBy`; transaction/charge allocation |
| `m_share_product_dividend_pay_out` | `ShareProductDividendPayOutDetails`; pool, period, status, audit metadata |
| `m_share_account_dividend_details` | `ShareAccountDividendDetails`; allocation, status, savings transaction ID |

Related infrastructure: `m_client`, `m_savings_account`, `m_savings_account_transaction`, `m_charge`, `m_appuser`, `m_permission`, `m_role_permission`, `m_portfolio_command_source`, `acc_product_mapping`, `acc_gl_journal_entry` (share transaction FK), financial-activity mappings, scheduler job metadata, and `nsimbi_user_monetary_authority`.

Share schema/FKs are already in [0001_initial_schema.xml](../../fineract-provider/src/main/resources/db/changelog/tenant/parts/0001_initial_schema.xml); permissions, enums, financial activity, and dividend job are in `0002_initial_data.xml`. Later changes include `0023_use_the_proper_date_or_datetime_type.xml`, `0056_add_external_event_default_configuration.xml`, timestamp changes, and `0145_job_short_name.xml`.

New changes must be additive Liquibase XML changesets under `fineract-provider/src/main/resources/db/changelog/tenant/parts/`. Nsimbi foundation changes are currently included through [final-changelog-tenant.xml](../../fineract-provider/src/main/resources/db/changelog/tenant/final-changelog-tenant.xml), while upstream parts use `changelog-tenant.xml`. Follow that existing Nsimbi inclusion point unless an intentional migration-architecture change is agreed. Select an unused filename after synchronizing with `dev`; do not edit applied historical migrations or create parallel copies of share tables.

No dedicated Nsimbi share report was identified in the inspected report/migration sources. Existing account transactions and dividend detail reads are reusable evidence for a future share register, dividend allocation statement, and GL reconciliation report; report requirements are not established by the video.

## 4. Frontend findings — reference only, outside implementation scope

No Nsimbi frontend source was found in the workspace. The user confirmed that frontend development has not begun. Backend `docker-compose-web-app.yml` references `openmf/web-app:master`; that does not pin the actual deployed source revision.

For discovery, the public Mifos repository was cloned only into `/tmp/nsimbi-share-upstream-web-app`. Inspected upstream default branch: `dev`, commit `2dd9f220adc4c83017d30226151c7a0997bdf4f1`. This is **not** a verified Nsimbi frontend baseline or the source of the recorded interface.

Existing upstream consumers include:

- `src/app/shares/shares.module.ts`, `shares-routing.module.ts`, `shares.service.ts`, account resolvers, create/edit steppers, account view and General/Transactions/Charges/Dividends tabs, and action components for all principal lifecycle operations.
- Client routes under `/clients/:clientId/shares-accounts/create`, `/:shareAccountId/{general,transactions,charges,dividends,edit}`, and `/:shareAccountId/actions/:name`.
- Product routes under `/products/share-products`, `/create`, `/:productId/general`, `/edit`, and `/:productId/dividends/{create,:dividendId}`.
- `src/app/products/share-products/` product maintenance and dividend list/create/detail components; `src/app/products/products.service.ts` calls the dedicated dividend API.
- Product menu entry and client “create share account” menu action; `src/app/navigation/share-account-table/` and client share-account summaries.
- Angular reactive forms, route resolvers, translation keys, and `mifosxHasPermission` checks already provide integration patterns.

The upstream dividend creation form asks only for start date, end date, and amount. Its “Post Dividends” detail action calls **approve**, reinforcing the need for an unambiguous backend status contract. Compatibility mismatches exist: upstream button configuration uses names such as `APPLYADDITIONAL_SHAREACCOUNT`, `APPROVALUNDO_SHAREACCOUNT`, and `WITHDRAW_SAVINGSACCOUNT` for share operations, while this backend seeds the exact codes listed above. It also offers share-account deletion, but this backend's account resource has no DELETE method. Do not copy these mismatches into the backend or add unsupported endpoints merely to match that UI.

Reference sources: [share routes](https://github.com/openMF/web-app/blob/2dd9f220adc4c83017d30226151c7a0997bdf4f1/src/app/shares/shares-routing.module.ts), [dividend form](https://github.com/openMF/web-app/blob/2dd9f220adc4c83017d30226151c7a0997bdf4f1/src/app/products/share-products/create-dividend/create-dividend.component.ts), [button permissions](https://github.com/openMF/web-app/blob/2dd9f220adc4c83017d30226151c7a0997bdf4f1/src/app/shares/shares-account-view/shares-buttons.config.ts).

**Frontend files to change now: none.** Future route/component needs can be met by those existing patterns or a Nsimbi UI consuming the backend contract; frontend design decisions must not determine financial rules.

## 5. Gap analysis

| Requirement / concern | Existing support | Gap and recommended treatment |
|---|---|---|
| Products, accounts, purchases, approval, activation, redemption, charges | Substantial domain/API/accounting implementation | Reuse and characterize. Do not rebuild. These flows are not demonstrated in the recording. |
| Shares landing data | Active-account list and client summaries | Decide whether the page means accounts, holdings, or transactions. Add optional status/product/client/office filtering only when the required work queue is agreed; preserve existing defaults. |
| Fixed dividend amount | Product-level pool and share-day allocation | Reuse if Nsimbi accepts that basis; verify arithmetic and boundaries. |
| Percentage toggle | No dividend-rate request field/calculator | Policy decision required on denominator, period rate vs annual rate, market vs nominal value, and eligible holdings. Implement server-side after that decision. |
| Date + period in months | Explicit start/end dates; posting uses business date | Define date meaning and period conversion. Persist a distinct intended posting date only if required; never silently equate it with period end. |
| Savings product selection | Each share account already links to one savings account | Decide filter vs destination override. Handle missing/multiple/closed/inactive/currency-mismatched destination accounts explicitly. |
| Reference and comment | Not accepted by dividend serializer or represented as dedicated payout fields | Add bounded metadata only after deciding requiredness, uniqueness, and audit use. A reference is not automatically an idempotency key. |
| Password-required behavior | API authentication exists; no dividend transaction-password field | Decide reauthentication vs independent approval. Use the authentication layer or short-lived authorization proof if needed; avoid credentials in audited command payloads. |
| Maker-checker | Command framework and business approval exist; seeded capability flags are false | Define operations requiring checker approval, enable via additive migration/configuration, reuse `_CHECKER` naming, and test no posting before effective approval. |
| Monetary limits | Nsimbi `SHARES` category and reusable policy exist | Share handlers do not enforce it. Decide amount basis, maker/checker evaluation, missing-policy behavior, and scheduler treatment before enrollment. |
| Preview | No verified working read-only dividend preview | Optional server-side preview can reuse calculation without persistence; return eligible accounts, exclusions, totals, and residual. Revalidate or use an immutable snapshot on create/approve. |
| Payout completion/retry | Per-account status, savings transaction link, scheduled processing | Test rerun/concurrency, partial failure, destination changes after approval, and reconciliation. Expose truthful initiated/approved/posted outcomes; do not invent a global posted flag. |
| Accounting policy | Share GL hooks and savings payable-dividend debit exist | Decide payable funding, purchase/redemption settlement, rounding residue, and any withholding. No evidence requires cash/mobile-money dividend channels. |
| Read permissions / branch scope | Dividend read permission explicit; other paths less consistent | Characterize end-to-end role/office behavior and nested product/dividend identity before changing access checks. Add missing permissions only where a tested requirement needs them. |
| Reports | Transaction/dividend read data exist | Define outputs and reconciliation totals before adding report SQL/permissions. No reporting screen is shown. |

## 6. Proposed safe backend implementation phases

1. **Align baseline and settle financial contracts.** Establish the requested branch from current `dev` without losing work. Read the newer savings command-envelope/withdrawal-authority patterns. Resolve the policy questions in section 9. Capture API examples for fixed-pool dividends and lifecycle actions. Exit: agreed rules and an accurate branch baseline.
2. **Characterize and repair existing behavior.** Run focused existing share suites against the supported test environment. Add tests for dividend allocation boundaries, savings posting/accounting, invalid transitions, permission enforcement, nested resource identity, and duplicate execution. Fix demonstrated failures in existing services. Exit: reliable existing fixed-pool flow, with no duplicate architecture.
3. **Enroll Nsimbi authorization.** Reuse `SHARES` authority, existing command handlers, audit metadata, and maker-checker execution patterns. Make limits, command origins, and scheduler/system behavior explicit. Add only needed permission capability/checker rows through Liquibase. Exit: rejected/queued requests produce no committed financial side effects and checker replay retains trustworthy context.
4. **Add approved dividend semantics.** Extend existing request validation/calculation for selected rate/period behavior, destination rules, and reference/comment metadata. Add a read-only preview only if needed. Keep the existing fixed-amount contract compatible; avoid changing historical allocations. Exit: server-authoritative calculation and traceable declaration/approval data.
5. **Complete payout and reconciliation.** Strengthen transaction boundaries/idempotency only where tests show gaps. Keep `DIVIDEND_PAYOUT` savings transactions and standard accounting hooks. Verify partial failures, retries, intended dates, locked periods, immutable destinations if required, and payable reconciliation. Exit: each intended allocation is attributable to one valid payout or an explainable outstanding result.
6. **Expose backend operational reads and document rollout.** Add agreed filters/status summaries/reports; update API docs and focused integration tests. Verify additive migrations for new/existing tenants and the repository's supported databases. Document job configuration and operational reconciliation. Frontend work remains deferred.

Each phase should be independently reviewable. Do not bundle unrelated share-product redesign, external payment channels, or a replacement approval system into the foundation change.

## 7. Files likely to change or be created

This is a conditional change inventory, not a commitment to edit every file. Existing domain code should change only for an agreed requirement or a reproduced failure.

| Backend path | Likely reason |
|---|---|
| `P/portfolio/shareproducts/api/ShareDividendApiResource.java` | Dividend contract extensions, optional preview/read behavior, identity checks |
| `C/portfolio/shareproducts/constants/ShareProductApiConstants.java` | Only approved additional request fields/commands |
| `P/portfolio/shareproducts/serialization/ShareProductDataSerializer.java` | Field, mode, period, amount, metadata validation |
| `P/portfolio/shareproducts/service/ShareProductDividendAssembler.java` | Reuse/fix allocation logic; approved rate calculation and deterministic arithmetic |
| `P/portfolio/shareproducts/service/ShareProductWritePlatformServiceJpaRepositoryImpl.java` | Persist declaration metadata; enforce agreed create/approve policy |
| `P/portfolio/shareproducts/domain/ShareProductDividendPayOutDetails.java` and associated data/read classes | Only metadata or immutable calculation inputs that actually require persistence |
| `P/portfolio/shareaccounts/domain/ShareAccountDividendDetails.java` | Destination snapshot/retry metadata only if agreed and necessary |
| `P/portfolio/shareaccounts/service/ShareAccountDividendReadPlatformServiceImpl.java`, `ShareAccountSchedularServiceImpl.java` | Payout selection, retry/revalidation, status/reconciliation data |
| `P/portfolio/shareaccounts/jobs/postdividentsforshares/PostDividentsForSharesTasklet.java` | Only necessary posting/retry behavior changes |
| `P/portfolio/shareaccounts/handler/*.java`, dividend handlers under `P/portfolio/shareproducts/handler/` | Consistent authority/maker-checker integration at established boundaries |
| `P/portfolio/shareaccounts/serialization/ShareAccountDataSerializer.java`, `ShareAccountWritePlatformServiceJpaRepositoryImpl.java` | Reproduced lifecycle or transaction invariant fixes |
| `P/portfolio/accounts/api/AccountsApiResource.java`, share-account read interface/implementation | Optional list filters and agreed read authorization |
| `P/nsimbi/userroles/service/NsimbiMonetaryAuthorityPolicyService.java` | Reuse; change only if current contract cannot express agreed share policy |
| Proposed `P/portfolio/shareaccounts/service/ShareTransactionAuthorityService.java` and/or `P/portfolio/shareproducts/service/ShareDividendAuthorityService.java` | Small policy adapters if needed; names illustrative, follow current savings authority pattern |
| `C/commands/service/CommandWrapperBuilder.java` and command context classes | Only for genuinely new commands/trusted replay context; existing commands already have wrappers |
| `P/portfolio/shareproducts/starter/ShareProductsConfiguration.java`, `P/portfolio/shareaccounts/start/ShareAccountsConfiguration.java` | Wire new services in existing configuration style |
| `fineract-provider/src/main/resources/db/changelog/tenant/parts/<unused>_nsimbi_share_*.xml` and `final-changelog-tenant.xml` | Additive fields/constraints/permission capabilities; no migration until a schema change is justified |
| Share API Swagger/data classes, relevant `fineract-doc/` sources, and `docs/changes/` | Keep contracts, examples, and rollout notes accurate |

Accounting processors/mappings should normally be reused unchanged. Alter them only if the approved settlement policy cannot be represented by the existing financial-activity and GL mapping mechanisms.

## 8. Tests to add or update

Existing integration coverage to extend:

- `integration-tests/src/test/java/org/apache/fineract/integrationtests/common/shares/ShareAccountIntegrationTests.java`: product/account creation, approval/rejection/undo, charges, additional shares, chronology, closure.
- `.../common/shares/DividendsIntegrationTests.java`: allocation amounts and batch approval; the inspected test does not execute the payout job or verify resulting savings/GL entries.
- `.../ShareAccountCreationValidationTest.java`: required account fields.
- `.../ShareAccountChargeRoundingTest.java`: activation/purchase/redemption charges, rounding and accounting cases.
- `.../ShareProductDatatableIntegrationTest.java`: preserve product datatable integration.
- Existing share helper classes: reuse where appropriate; legacy REST helpers are marked for replacement by the generated client, so follow nearby current integration-test conventions rather than expanding deprecated helpers unnecessarily.

| Proposed tests | What must be demonstrated |
|---|---|
| Dividend calculation unit tests | Weighted holdings across purchases/redemptions; transactions before/at/after boundaries; zero eligible shares; minimum active period; closed-account option; deterministic precision; allocation sum/residual; no negative allocations. Rate/annualization/month-end cases only after policy is specified. |
| Dividend validation/API tests | Missing/nonpositive amount, dates reversed/equal, unknown fields/modes, invalid product, metadata limits, wrong product/dividend pairing, permission denial, backward-compatible fixed-pool requests. |
| Lifecycle regression tests | Valid and invalid transitions, repeat approval/activation/redeem, insufficient/unvested holdings, issued-share limits, dates and pricing, savings ownership/currency. |
| Share authority unit/integration tests | Below/at/above min/max; absent policy; wrong currency; maker/checker role and identity; direct API/import paths; limits at actual execution; no committed mutation or journal on denial. Follow existing Nsimbi policy and savings-handler test patterns. |
| Maker-checker tests | Queue vs immediate execution, replay, denied self-check where configured, explicit super-user behavior, rejected/deleted queued commands, no premature posting, command audit context. Use existing JUnit/Mockito command-service patterns. |
| Posting/job integration tests | Approve → job → `DIVIDEND_PAYOUT` → savings balance and linked transaction ID; rerun without double credit; concurrent attempts; mixed successful/failed destinations; retry after correction; transaction rollback; business/posting dates; GL closure and payable/savings-control entries. |
| Preview tests, if added | Zero writes/audit commands, same approved calculation basis as create, stale holdings/destination changes detected or revalidated, consistent permissions and exclusions. |
| Migration/read tests | New and upgraded tenants, constraints/defaults/backfill, permission capability/checker consistency, no overbroad role grants; list filters preserve existing default results and pagination; office/tenant isolation. |
| Reauthentication tests, if selected | Missing/invalid/expired proof, correct operator identity, reuse rules, no credentials in command/audit/error payloads. |

Use JUnit 5, repository assertion/Mockito conventions, existing integration fixtures and RestAssured/generated-client patterns. Candidate new unit files belong under `fineract-provider/src/test/java/org/apache/fineract/portfolio/shareproducts/service/` and `.../shareaccounts/service/`; job/payout integration tests belong beside the existing share integration tests. Run focused suites and required formatting/build/migration checks after implementation. No tests were added or executed during this analysis.

## 9. Open policy decisions before implementation

| Decision | Why it matters / recommended starting point |
|---|---|
| Dividend amount and percentage basis | Confirm total pool vs per-member amount; for a rate, define paid-up nominal capital vs market value vs another base, day weighting, and annualization. Start with existing fixed-pool share-day allocation until another rule is explicitly specified. |
| Scope | One share product, selected products, or all SACCO shares? Existing batches are product-scoped; cross-product aggregation needs an explicit currency/allocation policy. |
| Date and share period | Declaration date, eligibility-period end, or posting date? Define month subtraction, inclusive boundaries, future/backdated handling, and business-date behavior. |
| Destination savings product | Does it filter eligible members or override each share account's linked destination? Define missing/multiple/blocked accounts and whether destinations freeze at declaration/approval. Prefer existing linked accounts until an override is required. |
| Approval and password | Which actions need business approval, command checker approval, or reauthentication? Whose identity is required? Preserve separate create/approve/post stages; do not equate a password with independent approval. |
| SHARES monetary authority | Purchase value, redemption gross/net proceeds, product issuance value, dividend pool, or individual payout? Which maker/checker is evaluated, what happens with no configured limit, and how are scheduled jobs treated? |
| Accounting and settlement | How is dividend payable funded? Are purchases/redemptions cash-settled or funded through savings transfers? Any withholding, fees, or retained residue? Obtain an approved GL example before changing postings. |
| Eligibility | Active members only? Closed share accounts? Minimum holding period? Pending/rejected shares? Multiple share accounts per member? Reuse product rules where they match the policy. |
| Duplicate and correction policy | Can periods overlap or contain supplemental distributions? Are references unique? How are mistakes corrected after partial/full posting? Existing unapproved delete is not a posted-payment reversal. |
| Operational reads/reports | Which statuses, filters, branch scope, share register, dividend statement, and reconciliation totals are required? The empty video screen supplies no populated schema. |

The first implementation work should align the branch and characterize existing backend behavior. The financial-policy extensions above should follow explicit decisions rather than inferred meanings of UI labels. This handoff is the only repository change from the analysis.
