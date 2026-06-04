# Working Capital APIs

This document summarizes the existing Working Capital REST APIs implemented in the `fineract-working-capital-loan` module.

Base path examples below use `/v1/...`. In a deployed Fineract server this is usually served under the provider API base path, for example `/fineract-provider/api/v1/...`.

## Source Files

Primary implementation sources:

| Area | Source |
| --- | --- |
| Loan products | `fineract-working-capital-loan/src/main/java/org/apache/fineract/portfolio/workingcapitalloanproduct/api/WorkingCapitalLoanProductApiResource.java` |
| Loans | `fineract-working-capital-loan/src/main/java/org/apache/fineract/portfolio/workingcapitalloan/api/WorkingCapitalLoanApiResource.java` |
| Transactions | `fineract-working-capital-loan/src/main/java/org/apache/fineract/portfolio/workingcapitalloan/api/WorkingCapitalLoanTransactionsApiResource.java` |
| Charges | `fineract-working-capital-loan/src/main/java/org/apache/fineract/portfolio/workingcapitalloan/api/WorkingCapitalLoanChargesApiResource.java` |
| Delinquency actions and schedules | `fineract-working-capital-loan/src/main/java/org/apache/fineract/portfolio/workingcapitalloan/api/WorkingCapitalLoanDelinquencyActionApiResource.java` |
| Breach schedules | `fineract-working-capital-loan/src/main/java/org/apache/fineract/portfolio/workingcapitalloan/api/WorkingCapitalLoanBreachScheduleApiResource.java` |
| Breach configuration | `fineract-working-capital-loan/src/main/java/org/apache/fineract/portfolio/workingcapitalloanbreach/api/WorkingCapitalBreachApiResource.java` |
| Near breach configuration | `fineract-working-capital-loan/src/main/java/org/apache/fineract/portfolio/workingcapitalloannearbreach/api/WorkingCapitalNearBreachApiResource.java` |
| COB catch-up | `fineract-provider/src/main/java/org/apache/fineract/cob/api/WorkingCapitalLoanCOBCatchUpApiResource.java` |

## Shared Conventions

| Convention | Description |
| --- | --- |
| Authentication and permissions | APIs use the Fineract platform security context. Read/write permissions are enforced per resource. |
| Response type for write commands | Most `POST`, `PUT`, and `DELETE` commands return `CommandProcessingResult` fields such as `officeId`, `clientId`, `loanId`, `resourceId`, `resourceExternalId`, `subResourceId`, `subResourceExternalId`, and `changes`. The exact fields depend on the command. |
| Dates in requests | Write requests use string dates plus `dateFormat` and `locale` when the date is not ISO parsed. Examples use `dd MMMM yyyy` and `en_GB`. |
| Dates in responses | Read responses expose Java `LocalDate`, serialized by Fineract commonly as arrays like `[2024, 2, 1]`. |
| External IDs | Most loan and product APIs have ID-based and external-ID-based variants. External ID paths use `external-id/{...}`. |
| Pagination | List loans and list transactions use Spring Data pagination query parameters: `page`, `size`, and `sort`, for example `sort=id,asc`. |
| Enum option objects | Many response fields are `StringEnumOptionData` or `EnumOptionData` with fields like `id`, `code`, and `value` or `description`. |

Common enum values:

| Field | Values |
| --- | --- |
| `amortizationType` | `EIR`, `FLAT`; validator currently only accepts `EIR` for products. |
| `repaymentFrequencyType`, `breachFrequencyType`, `nearBreachFrequencyType`, delinquency action `frequencyType` | `DAYS`, `WEEKS`, `MONTHS`, `YEARS`. |
| `delinquencyStartType` | `LOAN_CREATION`, `DISBURSEMENT`. |
| `accountingRule` | `NONE`, `CASH_BASED`. |
| `breachAmountCalculationType` | `PERCENTAGE`, `FLAT`. |
| Delinquency action `action` | `pause`, `reschedule`. |
| Delinquency action `minimumPaymentType` | `PERCENTAGE`, `FLAT`. |

## Endpoint Inventory

### Working Capital Loan Products

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/v1/working-capital-loan-products` | Create a Working Capital loan product. |
| `GET` | `/v1/working-capital-loan-products` | List all Working Capital loan products. |
| `GET` | `/v1/working-capital-loan-products/template` | Retrieve product template defaults and option lists. |
| `GET` | `/v1/working-capital-loan-products/{productId}` | Retrieve one product by ID. Supports `template=true` and `fields=...` through standard Fineract query handling. |
| `GET` | `/v1/working-capital-loan-products/external-id/{externalProductId}` | Retrieve one product by external ID. |
| `PUT` | `/v1/working-capital-loan-products/{productId}` | Update product by ID. |
| `PUT` | `/v1/working-capital-loan-products/external-id/{externalProductId}` | Update product by external ID. |
| `DELETE` | `/v1/working-capital-loan-products/{productId}` | Delete product by ID if not in use. |
| `DELETE` | `/v1/working-capital-loan-products/external-id/{externalProductId}` | Delete product by external ID if not in use. |

### Working Capital Loans

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/v1/working-capital-loans/template` | Retrieve loan application template. Optional `productId`, `clientId`. |
| `GET` | `/v1/working-capital-loans` | List loans. Filters: `externalId`, `accountNo`, `clientId`, `status`; pagination: `page`, `size`, `sort`. |
| `GET` | `/v1/working-capital-loans/{loanId}` | Retrieve loan by ID. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}` | Retrieve loan by external ID. |
| `POST` | `/v1/working-capital-loans` | Submit a new loan application. |
| `PUT` | `/v1/working-capital-loans/{loanId}` | Modify a submitted and pending approval loan application by ID. |
| `PUT` | `/v1/working-capital-loans/external-id/{loanExternalId}` | Modify loan application by external ID. |
| `DELETE` | `/v1/working-capital-loans/{loanId}` | Delete submitted and pending approval loan application by ID. |
| `DELETE` | `/v1/working-capital-loans/external-id/{loanExternalId}` | Delete submitted and pending approval loan application by external ID. |
| `POST` | `/v1/working-capital-loans/{loanId}?command={command}` | State transition by ID. Commands: `approve`, `reject`, `undoapproval`, `disburse`, `undodisbursal`. |
| `POST` | `/v1/working-capital-loans/external-id/{loanExternalId}?command={command}` | State transition by external ID. |
| `PUT` | `/v1/working-capital-loans/{loanId}/discount` | Update discount for a disbursed loan. |
| `PUT` | `/v1/working-capital-loans/external-id/{loanExternalId}/discount` | Update discount by external ID. |
| `PUT` | `/v1/working-capital-loans/{loanId}/payment-rate` | Update period payment rate for active loan. |
| `PUT` | `/v1/working-capital-loans/external-id/{loanExternalId}/payment-rate` | Update period payment rate by external ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/rate-changes` | Retrieve period payment rate change history. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/rate-changes` | Retrieve rate changes by external ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/delinquencyrangetags` | Retrieve delinquency tag history by ID. |
| `GET` | `/v1/working-capital-loans/external-id/{externalId}/delinquencyrangetags` | Retrieve delinquency tag history by external ID. |

### Transactions

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/v1/working-capital-loans/{loanId}/transactions` | List transactions by loan ID. Paginated. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/transactions` | List transactions by loan external ID. Paginated. |
| `GET` | `/v1/working-capital-loans/{loanId}/transactions/{transactionId}` | Retrieve transaction by loan ID and transaction ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/transactions/external-id/{externalTransactionId}` | Retrieve transaction by loan ID and transaction external ID. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/transactions/{transactionId}` | Retrieve transaction by loan external ID and transaction ID. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/transactions/external-id/{externalTransactionId}` | Retrieve transaction by loan external ID and transaction external ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/template?templateType={templateType}` | Retrieve action template for a loan transaction. |
| `POST` | `/v1/working-capital-loans/{loanId}/transactions?command={command}` | Execute transaction by loan ID. Commands: `repayment`, `creditBalanceRefund`, `goodwillCredit`, `discountFee`, `discountFeeAdjustment`. |
| `POST` | `/v1/working-capital-loans/external-id/{loanExternalId}/transactions?command={command}` | Execute transaction by loan external ID. |
| `POST` | `/v1/working-capital-loans/{loanId}/transactions/{transactionId}?command=undo` | Undo an existing transaction by loan ID. |
| `POST` | `/v1/working-capital-loans/external-id/{loanExternalId}/transactions/{transactionId}?command=undo` | Undo an existing transaction by loan external ID. |

### Charges

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/v1/working-capital-loans/{loanId}/charges/template` | Retrieve charge creation template by loan ID. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/charges/template` | Retrieve charge template by loan external ID. |
| `POST` | `/v1/working-capital-loans/{loanId}/charges` | Create a loan charge by loan ID. |
| `POST` | `/v1/working-capital-loans/external-id/{loanExternalId}/charges` | Create a loan charge by loan external ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/charges` | List charges by loan ID. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/charges` | List charges by loan external ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/charges/{loanChargeId}` | Retrieve charge by loan ID and charge ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/charges/external-id/{loanChargeExternalId}` | Retrieve charge by loan ID and charge external ID. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/charges/{loanChargeId}` | Retrieve charge by loan external ID and charge ID. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/charges/external-id/{loanChargeExternalId}` | Retrieve charge by loan external ID and charge external ID. |

### Delinquency, Breach, and Amortization Schedules

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/v1/working-capital-loans/{loanId}/delinquency-actions` | Create delinquency action by loan ID. |
| `POST` | `/v1/working-capital-loans/external-id/{loanExternalId}/delinquency-actions` | Create delinquency action by loan external ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/delinquency-actions` | List delinquency actions by loan ID. |
| `GET` | `/v1/working-capital-loans/external-id/{loanExternalId}/delinquency-actions` | List delinquency actions by loan external ID. |
| `GET` | `/v1/working-capital-loans/{loanId}/delinquency-range-schedule` | Retrieve delinquency range schedule periods. |
| `GET` | `/v1/working-capital-loans/{loanId}/breach-schedule` | Retrieve breach schedule periods. |
| `GET` | `/v1/working-capital-loans/{loanId}/amortization-schedule` | Retrieve projected amortization schedule. |

### COB Catch-Up

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/v1/working-capital-loans/oldest-cob-closed` | Retrieve the oldest COB processed Working Capital loan and COB business date. |
| `POST` | `/v1/working-capital-loans/catch-up` | Execute Working Capital loan COB catch-up from oldest processed loan date to current COB business date. |
| `GET` | `/v1/working-capital-loans/is-catch-up-running` | Retrieve whether Working Capital loan COB catch-up is currently running. |

### Breach and Near Breach Configuration

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/v1/working-capital/breach/template` | Retrieve breach option lists. |
| `GET` | `/v1/working-capital/breach/breaches` | List breach configurations. |
| `GET` | `/v1/working-capital/breach/breaches/{breachId}` | Retrieve one breach configuration. |
| `POST` | `/v1/working-capital/breach/breaches` | Create breach configuration. |
| `PUT` | `/v1/working-capital/breach/breaches/{breachId}` | Update breach configuration. |
| `DELETE` | `/v1/working-capital/breach/breaches/{breachId}` | Delete breach configuration. |
| `GET` | `/v1/working-capital/near-breach` | List near breach configurations. |
| `GET` | `/v1/working-capital/near-breach/{breachId}` | Retrieve one near breach configuration. |
| `POST` | `/v1/working-capital/near-breach` | Create near breach configuration. |
| `PUT` | `/v1/working-capital/near-breach/{breachId}` | Update near breach configuration. |
| `DELETE` | `/v1/working-capital/near-breach/{breachId}` | Delete near breach configuration. |

## Working Capital Loan Product Fields

### Create Product Request

Used by `POST /v1/working-capital-loan-products`.

Required on create:

| Field | Type | Description |
| --- | --- | --- |
| `name` | string | Product name. Required, max 100 characters, unique. |
| `shortName` | string | Product short name. Required, max 4 characters, unique. |
| `currencyCode` | string | ISO currency code. Required, max 3 characters. |
| `digitsAfterDecimal` | integer | Currency decimal places. Required, range 0 to 6. |
| `inMultiplesOf` | integer | Currency multiple. Required, zero or greater. |
| `amortizationType` | string | Amortization type. Required. Code currently validates only `EIR`. |
| `npvDayCount` | integer | Day count used for NPV/EIR calculation. Required, greater than zero. |
| `paymentAllocation` | array | Required non-empty payment allocation rules. |
| `principal` | decimal | Default principal amount. Required, positive. |
| `periodPaymentRate` | decimal | Default period payment rate. Required, zero or positive. |
| `repaymentEvery` | integer | Repayment frequency value. Required, greater than zero. |
| `repaymentFrequencyType` | string | Repayment frequency unit. Required. |
| `accountingRule` | string | Accounting rule. Required: `NONE` or `CASH_BASED`. |

Optional/common fields:

| Field | Type | Description |
| --- | --- | --- |
| `description` | string | Product description, max 500 characters. |
| `externalId` | string | Optional unique external product identifier. |
| `fundId` | long | Optional fund ID. Must be greater than zero when supplied. |
| `startDate` | string | Product start date. |
| `closeDate` | string | Product close date. Must be after `startDate` when both are supplied. |
| `delinquencyBucketId` | long | Optional delinquency bucket/classification ID. |
| `minPrincipal` | decimal | Minimum allowed principal. Positive when supplied. |
| `maxPrincipal` | decimal | Maximum allowed principal. Positive when supplied. |
| `minPeriodPaymentRate` | decimal | Minimum allowed period payment rate. Zero or positive. |
| `maxPeriodPaymentRate` | decimal | Maximum allowed period payment rate. Zero or positive. |
| `discount` | decimal | Default discount amount. Zero or positive. |
| `breachId` | long | Optional working capital breach configuration ID. |
| `nearBreachId` | long | Optional near breach configuration ID. Requires `breachId`; near breach frequency must be lower than breach frequency. |
| `delinquencyGraceDays` | integer | Grace days before delinquency tracking starts. Zero or greater. |
| `delinquencyStartType` | string | Whether delinquency starts at loan creation or disbursement. |
| `breachGraceDays` | integer | Days to shift first breach schedule period after disbursement. Zero or greater. |
| `allowAttributeOverrides` | object | Flags controlling which product defaults may be overridden on individual loans. |
| `locale` | string | Locale for parsing localized values. |
| `dateFormat` | string | Date format for parsing request date strings. |

`allowAttributeOverrides` fields:

| Field | Type | Description |
| --- | --- | --- |
| `delinquencyBucketClassification` | boolean | Allows loan-level override of delinquency bucket. |
| `breach` | boolean | Allows loan-level override of breach and near breach settings. |
| `discountDefault` | boolean | Allows loan-level override of discount. |
| `periodPaymentFrequency` | boolean | Allows loan-level override of `repaymentEvery`. |
| `periodPaymentFrequencyType` | boolean | Allows loan-level override of `repaymentFrequencyType`. |

`paymentAllocation` item fields:

| Field | Type | Description |
| --- | --- | --- |
| `transactionType` | string | Transaction type to which this allocation order applies, for example `DEFAULT` or `REPAYMENT`. |
| `paymentAllocationOrder` | array | Ordered allocation rules. |
| `paymentAllocationOrder[].paymentAllocationRule` | string | Allocation target, for example `PENALTY`, `FEE`, or principal-related rules depending on configured allocation support. |
| `paymentAllocationOrder[].order` | integer | Execution order for the allocation rule. |

Accounting fields for `CASH_BASED` products:

| Field | Type | Description |
| --- | --- | --- |
| `fundSourceAccountId` | long | Required for `CASH_BASED`. Fund source GL account. |
| `loanPortfolioAccountId` | long | Required for `CASH_BASED`. Loan portfolio GL account. |
| `transfersInSuspenseAccountId` | long | Required for `CASH_BASED`. Transfers in suspense GL account. |
| `deferredIncomeLiabilityAccountId` | long | Required for `CASH_BASED`. Deferred income liability GL account. |
| `incomeFromDiscountFeeAccountId` | long | Required for `CASH_BASED`. Discount fee income GL account. |
| `receivableFeeAccountId` | long | Required for `CASH_BASED`. Fee receivable GL account. |
| `receivablePenaltyAccountId` | long | Required for `CASH_BASED`. Penalty receivable GL account. |
| `incomeFromFeeAccountId` | long | Required for `CASH_BASED`. Fee income GL account. |
| `incomeFromPenaltyAccountId` | long | Required for `CASH_BASED`. Penalty income GL account. |
| `incomeFromRecoveryAccountId` | long | Required for `CASH_BASED`. Recovery income GL account. |
| `writeOffAccountId` | long | Required for `CASH_BASED`. Write-off GL account. |
| `overpaymentLiabilityAccountId` | long | Required for `CASH_BASED`. Overpayment liability GL account. |
| `incomeFromChargeOffFeesAccountId` | long | Optional charge-off fee income GL account. |
| `incomeFromChargeOffPenaltyAccountId` | long | Optional charge-off penalty income GL account. |
| `incomeFromGoodwillCreditFeesAccountId` | long | Optional goodwill credit fee income GL account. |
| `incomeFromGoodwillCreditPenaltyAccountId` | long | Optional goodwill credit penalty income GL account. |
| `goodwillCreditAccountId` | long | Optional goodwill credit GL account. |
| `chargeOffExpenseAccountId` | long | Optional charge-off expense GL account. |
| `chargeOffFraudExpenseAccountId` | long | Optional charge-off fraud expense GL account. |
| `paymentChannelToFundSourceMappings` | array | Optional mapping of payment type to fund source account. |
| `feeToIncomeAccountMappings` | array | Optional mapping of charge ID to fee income account. |
| `penaltyToIncomeAccountMappings` | array | Optional mapping of charge ID to penalty income account. |
| `chargeOffReasonToExpenseAccountMappings` | array | Optional mapping of charge-off reason code value to expense account. |
| `writeOffReasonsToExpenseMappings` | array | Optional mapping of write-off reason code value to expense account. |

### Product Update Request

Used by `PUT /v1/working-capital-loan-products/{productId}` and external-ID variant.

The request supports the same fields as create, but update is partial: only fields present are validated and updated. Product uniqueness rules still apply for fields like name, short name, and external ID.

### Product Response Fields

Returned by product list/retrieve APIs.

| Field | Type | Description |
| --- | --- | --- |
| `id` | long | Product ID. |
| `name` | string | Product name. |
| `shortName` | string | Product short name. |
| `description` | string | Product description. |
| `fundId` | long | Fund ID. |
| `fundName` | string | Fund name. |
| `startDate` | date | Product start date. |
| `closeDate` | date | Product close date. |
| `externalId` | string | External product ID. |
| `status` | string | Product status. |
| `currency` | object | Currency metadata. |
| `amortizationType` | enum object | Amortization type. |
| `delinquencyBucket` | object | Delinquency bucket with ranges. |
| `npvDayCount` | integer | NPV day count. |
| `paymentAllocation` | array | Payment allocation configuration. |
| `minPrincipal`, `principal`, `maxPrincipal` | decimal | Principal constraints and default. |
| `minPeriodPaymentRate`, `periodPaymentRate`, `maxPeriodPaymentRate` | decimal | Period payment rate constraints and default. |
| `discount` | decimal | Default discount. |
| `repaymentEvery` | integer | Default repayment interval. |
| `repaymentFrequencyType` | enum object | Default repayment interval unit. |
| `breach` | object | Linked breach configuration. |
| `nearBreach` | object | Linked near breach configuration. |
| `delinquencyGraceDays` | integer | Grace days before delinquency tracking. |
| `delinquencyStartType` | enum object | Delinquency start mode. |
| `breachGraceDays` | integer | First breach schedule shift in days. |
| `allowAttributeOverrides` | object | Configurable override flags. |
| `accountingRule` | enum object | Accounting rule. |
| `accountingMappings` | object | GL account mappings keyed by account role. |
| `paymentChannelToFundSourceMappings` | array | Payment type to fund source mappings. |
| `feeToIncomeAccountMappings` | array | Fee charge to income account mappings. |
| `penaltyToIncomeAccountMappings` | array | Penalty charge to income account mappings. |
| `chargeOffReasonToExpenseAccountMappings` | array | Charge-off reason to expense mappings. |
| `writeOffReasonsToExpenseMappings` | array | Write-off reason to expense mappings. |

### Product Template Response Fields

Returned by `GET /v1/working-capital-loan-products/template`.

| Field | Description |
| --- | --- |
| `fundOptions` | Available funds. |
| `paymentTypeOptions` | Available payment types. |
| `chargeOptions` | Available fee charges. |
| `penaltyOptions` | Available penalty charges. |
| `currencyOptions` | Available currencies. |
| `amortizationTypeOptions` | Available amortization types. |
| `periodFrequencyTypeOptions` | Available period frequency types. |
| `breachOptions` | Available breach configurations. |
| `nearBreachOptions` | Available near breach configurations. |
| `advancedPaymentAllocationTypes` | Available allocation target types. |
| `advancedPaymentAllocationTransactionTypes` | Available allocation transaction types. |
| `delinquencyStartTypeOptions` | Available delinquency start types. |
| `delinquencyMinimumPaymentTypeOptions` | Available delinquency minimum payment types. |
| `delinquencyBucketOptions` | Available delinquency buckets. |
| `accountingRuleOptions` | Available accounting rule options. |
| `accountingMappingOptions` | Available GL account options. |
| `chargeOffReasonOptions` | Available charge-off reason code values. |
| `writeOffReasonOptions` | Available write-off reason code values. |

## Working Capital Loan Fields

### Submit Loan Application Request

Used by `POST /v1/working-capital-loans`.

Required on create:

| Field | Type | Description |
| --- | --- | --- |
| `clientId` | long | Active client ID. Required, greater than zero. |
| `productId` | long | Working Capital loan product ID. Required, greater than zero. |
| `principalAmount` | decimal | Requested principal/disbursement amount. Required, positive, validated against product min/max. |
| `periodPaymentRate` | decimal | Loan-level payment rate. Required, zero or positive, validated against product min/max. |
| `totalPaymentVolume` | decimal | Total expected payment volume. Required, zero or positive. |
| `expectedDisbursementDate` | string | Expected disbursement date. Required; validated as a valid business/working date. |

Optional fields:

| Field | Type | Description |
| --- | --- | --- |
| `fundId` | long | Optional fund ID. |
| `accountNo` | string | Optional account number, max 20 characters, unique when supplied. |
| `externalId` | string | Optional loan external ID, max 100 characters, unique when supplied. |
| `submittedOnDate` | string | Submitted date. Defaults to current business date when omitted. Cannot be future or after expected disbursement. |
| `submittedOnNote` | string | Submission note, max 500 characters. |
| `repaymentEvery` | integer | Override repayment frequency value if product allows override. |
| `repaymentFrequencyType` | string | Override repayment frequency type if product allows override. |
| `discount` | decimal | Proposed discount. Zero or positive; override depends on product settings. |
| `breachId` | long | Override breach config if product allows override. |
| `nearBreachId` | long | Override near breach config if product allows override. Requires breach. |
| `delinquencyBucketId` | long | Override delinquency bucket if product allows override. |
| `delinquencyGraceDays` | integer | Grace days before delinquency tracking. |
| `delinquencyStartType` | string | Delinquency start mode. |
| `breachGraceDays` | integer | First breach schedule shift in days. |
| `paymentAllocation` | array | Optional loan-level payment allocation override. |
| `locale` | string | Locale for parsing. |
| `dateFormat` | string | Date format for parsing. |

Example:

```json
{
  "clientId": 1,
  "productId": 1,
  "principalAmount": 10000.00,
  "periodPaymentRate": 1.0,
  "totalPaymentVolume": 10500.00,
  "expectedDisbursementDate": "1 February 2024",
  "submittedOnDate": "15 January 2024",
  "locale": "en_GB",
  "dateFormat": "dd MMMM yyyy"
}
```

### Modify Loan Application Request

Used by `PUT /v1/working-capital-loans/{loanId}` and external-ID variant.

Supports the same loan application fields as create. Update is partial, but the loan must be in `Submitted and pending approval` status. At least one updatable field must be supplied.

### Loan State Transition Request

Used by `POST /v1/working-capital-loans/{loanId}?command={command}` and external-ID variant.

| Command | Required fields | Optional fields | Description |
| --- | --- | --- | --- |
| `approve` | `approvedOnDate`, `expectedDisbursementDate` | `approvedLoanAmount`, `discountAmount`, `note`, `locale`, `dateFormat` | Approves a submitted loan. Approved amount must not exceed proposed principal. Expected disbursement cannot be before approval date. |
| `reject` | `rejectedOnDate` | `note`, `locale`, `dateFormat` | Rejects a submitted loan. Rejection date cannot be future or before submission date. |
| `undoapproval` | none | `note`, `locale`, `dateFormat` | Reverts approval. |
| `disburse` | `actualDisbursementDate`, `transactionAmount` | `discountAmount`, `classificationId`, `externalId`, `discountExternalId`, `paymentDetails`, `note`, `locale`, `dateFormat` | Disburses an approved loan. Transaction amount must not exceed approved principal. |
| `undodisbursal` | none | `note`, `locale`, `dateFormat` | Reverses disbursal. |

`paymentDetails` fields:

| Field | Type | Description |
| --- | --- | --- |
| `paymentTypeId` | integer/long | Payment type ID. |
| `accountNumber` | string | Account number. |
| `checkNumber` | string | Check number. |
| `routingCode` | string | Routing code. |
| `receiptNumber` | string | Receipt number. |
| `bankNumber` | string | Bank number. |

### Discount Update Request

Used by `PUT /v1/working-capital-loans/{loanId}/discount`.

| Field | Type | Description |
| --- | --- | --- |
| `discountAmount` | decimal | Discount amount. |
| `note` | string | Discount update note. |
| `locale` | string | Locale for parsing. |
| `dateFormat` | string | Date format for parsing. |

### Period Payment Rate Update Request

Used by `PUT /v1/working-capital-loans/{loanId}/payment-rate`.

| Field | Type | Description |
| --- | --- | --- |
| `periodPaymentRate` | decimal | New period payment rate. Required, zero or positive, must differ from previous rate and fit product min/max constraints. |
| `note` | string | Rate change note. |
| `locale` | string | Locale for parsing. |

### Rate Change History Response Fields

Returned by `GET /v1/working-capital-loans/{loanId}/rate-changes`.

| Field | Type | Description |
| --- | --- | --- |
| `id` | long | Rate change record ID. |
| `loanId` | long | Loan ID. |
| `effectiveDate` | date | Date from which the new rate applies. |
| `previousRate` | decimal | Previous period payment rate. |
| `newRate` | decimal | New period payment rate. |
| `reversed` | boolean | Whether the rate change was reversed. |
| `reversedOnDate` | date | Date reversal was applied. |
| `createdDate` | datetime | Record creation timestamp. |

### Loan Response Fields

Returned by loan retrieve/list APIs.

| Field | Type | Description |
| --- | --- | --- |
| `id` | long | Loan ID. |
| `accountNo` | string | Loan account number. |
| `externalId` | string | Loan external ID. |
| `client` | object | Client summary with `id` and `displayName`. |
| `officeId` | long | Client/loan office ID. |
| `fundId` | long | Fund ID. |
| `fundName` | string | Fund name. |
| `product` | object | Working Capital loan product data. |
| `status` | object | Loan status with flags like `pendingApproval`, `active`, `closed`, `overpaid`. |
| `timeline` | object | Submission, approval, disbursement, and closure timeline. |
| `submittedOnDate`, `approvedOnDate`, `rejectedOnDate` | date | Key lifecycle dates. |
| `proposedPrincipal` | decimal | Principal requested at submission. |
| `approvedPrincipal` | decimal | Approved principal. |
| `currency` | object | Currency metadata. |
| `periodPaymentRate` | decimal | Current period payment rate. |
| `repaymentEvery` | integer | Current repayment interval. |
| `repaymentFrequencyType` | enum object | Current repayment interval unit. |
| `totalPaymentVolume` | decimal | Total expected payment volume. |
| `discount` | decimal | Discount set during disbursement. |
| `discountProposed` | decimal | Discount proposed at submission. |
| `discountApproved` | decimal | Discount approved at approval. |
| `totalNoPayments` | integer | Loan term in payments from amortization schedule. Null until schedule exists. |
| `periodPaymentAmount` | decimal | Expected periodic payment amount. Null until schedule exists. |
| `dailyEir` | decimal | Periodic/daily effective interest rate from schedule. |
| `calculatedAnnualEir` | decimal | Annualized effective interest rate. |
| `breach` | object | Linked breach config. |
| `nearBreach` | object | Linked near breach config. |
| `delinquencyBucket` | object | Linked delinquency bucket. |
| `delinquencyGraceDays` | integer | Grace days before delinquency tracking. |
| `delinquencyStartType` | enum object | Delinquency start mode. |
| `breachGraceDays` | integer | First breach schedule shift in days. |
| `lastClosedBusinessDate` | date | Last closed business date from COB. |
| `paymentAllocation` | array | Loan payment allocation rules. |
| `disbursementDetails` | array | Expected and actual disbursement details. |
| `balance` | object | Running balance summary. |
| `collectionData` | object | Delinquency collection summary. |

`balance` fields:

| Field | Description |
| --- | --- |
| `principal`, `principalPaid`, `principalOutstanding` | Principal totals. |
| `fee`, `feePaid`, `feeOutstanding` | Fee totals. |
| `penalty`, `penaltyPaid`, `penaltyOutstanding` | Penalty totals. |
| `realizedIncomeFromDiscountFee`, `unrealizedIncomeFromDiscountFee` | Discount fee income balances. |
| `overpaymentAmount` | Current overpayment amount. |
| `totalExpectedRepayment`, `totalRepayment`, `totalOutstanding` | Overall repayment totals. |
| `totalDisbursement` | Total disbursed amount. |
| `totalDiscountFee` | Total discount fee. |
| `totalDiscountFeeAdjustment` | Total discount fee adjustments. |

`collectionData` fields:

| Field | Description |
| --- | --- |
| `delinquentDays` | Number of delinquent days. |
| `delinquentDate` | Date when loan became delinquent. |
| `delinquentAmount` | Total delinquent amount. |
| `delinquentPrincipal`, `delinquentFee`, `delinquentPenalty` | Delinquent amount components. |
| `delinquencyPausePeriods` | Pause periods excluded from delinquency counting. |
| `rangeLevelDelinquency` | Delinquency amounts grouped by configured ranges. |

### Progressive Loan Details Response Comparison

The following table compares the current Working Capital loan details response with a progressive-loan-style loan details response. Use this table to identify whether a field is currently exposed by the Working Capital loan details API, whether it appears in the progressive loan details response shape, and whether there is a close equivalent with the same purpose.

Status values:

| Status | Meaning |
| --- | --- |
| `Current API` | Field is part of the current Working Capital loan details response. |
| `Missing from current API` | Field appears in the progressive loan details response shape but is not currently exposed by the Working Capital loan details response. |
| `Different shape` | Same or similar concept exists, but under another name, nesting structure, or endpoint. |
| `Not in progressive sample` | Field is part of the current Working Capital API documentation but was not present in the provided progressive loan details example. |

| Field path | Current Working Capital API status | Present in progressive loan details example | Similar current Working Capital field/API | Notes |
| --- | --- | --- | --- | --- |
| `id` | Current API | Yes | Same field | Loan ID. |
| `accountNo` | Current API | Yes | Same field | Loan account number. |
| `externalId` | Current API | Yes | Same field | Loan external ID. |
| `status` | Current API | Yes | Same field | Status object and status flags are aligned conceptually. |
| `client` | Current API | No | Progressive uses `clientId`, `clientName`, `clientAccountNo`, `clientOfficeId` | Working Capital nests client summary data instead of flattening all client fields. |
| `clientId` | Missing from current API as top-level field | Yes | `client.id` | Same purpose, different shape. |
| `clientAccountNo` | Missing from current API as top-level field | Yes | `client.accountNo` if included by `ClientData` | Not documented as a top-level Working Capital field. |
| `clientName` | Missing from current API as top-level field | Yes | `client.displayName` | Same purpose, different shape. |
| `clientOfficeId` | Missing from current API as top-level field | Yes | `officeId` | Working Capital exposes loan/client office as `officeId`. |
| `officeId` | Current API | No | Progressive uses `clientOfficeId` | Same purpose, different name. |
| `officeName` | Current API | No | None in example | Present in `WorkingCapitalLoanData`, not listed in the previous field table. |
| `loanProductId` | Missing from current API as top-level field | Yes | `product.id` | Same purpose, different shape. |
| `loanProductName` | Missing from current API as top-level field | Yes | `product.name` | Same purpose, different shape. |
| `product` | Current API | No | Progressive uses `loanProductId`, `loanProductName`, and top-level product attributes | Working Capital nests product details. |
| `fundId` | Current API | Yes | Same field | Fund ID. |
| `fundName` | Current API | Yes | Same field | Fund name. |
| `currency` | Current API | Yes | Same field | Currency metadata. |
| `principal` | Missing from current API as top-level field | Yes | `balance.principal`, `proposedPrincipal`, `approvedPrincipal` | Similar purpose, but current API separates proposed, approved, and balance principal. |
| `proposedPrincipal` | Current API | Yes | Same field | Requested principal. |
| `approvedPrincipal` | Current API | Yes | Same field | Approved principal. |
| `netDisbursalAmount` | Missing from current API loan details response | Yes | `GET /v1/working-capital-loans/{loanId}/amortization-schedule` field `netDisbursementAmount` | Similar value exists on amortization schedule response, not loan details. |
| `proposedDiscountFee` | Missing from current API under that name | Yes | `discountProposed` | Same purpose, different name. |
| `approvedDiscountFee` | Missing from current API under that name | Yes | `discountApproved` | Same purpose, different name. |
| `discountFee` | Missing from current API under that name | Yes | `discount`, `balance.totalDiscountFee` | `discount` is current loan-level discount; `balance.totalDiscountFee` tracks balance total. |
| `discountProposed` | Current API | No | Progressive uses `proposedDiscountFee` | Same purpose, different name. |
| `discountApproved` | Current API | No | Progressive uses `approvedDiscountFee` | Same purpose, different name. |
| `discount` | Current API | No | Progressive uses `discountFee` | Same purpose, different name depending on business interpretation. |
| `termFrequency` | Missing from current API | Yes | None exact | Working Capital uses repayment period and amortization schedule fields instead of term frequency. |
| `termPeriodFrequencyType` | Missing from current API | Yes | None exact; closest is `repaymentFrequencyType` | Term period frequency and repayment frequency are not guaranteed to be the same concept. |
| `numberOfRepayments` | Missing from current API under that name | Yes | `totalNoPayments` | Same purpose, different name. |
| `totalNoPayments` | Current API | No | Progressive uses `numberOfRepayments` | Same purpose, different name. |
| `repaymentEvery` | Current API | Yes | Same field | Repayment interval value. |
| `repaymentFrequencyType` | Current API | Yes | Same field | Repayment interval unit. |
| `periodPaymentRate` | Current API | No | Progressive uses `paymentRate` | Same purpose, different name. |
| `paymentRate` | Missing from current API under that name | Yes | `periodPaymentRate` | Same purpose, different name. |
| `totalPaymentVolume` | Current API | Yes | Same field | Total expected payment volume. |
| `amortizationType` | Different shape | Yes | `product.amortizationType` | Current loan details response exposes amortization through nested product data, not as top-level loan field. |
| `npvDayCount` | Different shape | Yes | `product.npvDayCount`, amortization schedule `npvDayCount` | Not a top-level Working Capital loan details field. |
| `transactionProcessingStrategyCode` | Missing from current API | Yes | `paymentAllocation` | Working Capital exposes allocation rules, not the standard loan transaction processing strategy fields. |
| `transactionProcessingStrategyName` | Missing from current API | Yes | `paymentAllocation` | Similar purpose at a configuration level, but not equivalent field. |
| `timeline` | Current API | Yes | Same field | Lifecycle timeline. |
| `timeline.submittedOnDate` | Current API | Yes | Same field | Also exposed as top-level `submittedOnDate` in current API. |
| `timeline.approvedOnDate` | Current API | Yes | Same field | Also exposed as top-level `approvedOnDate` in current API. |
| `timeline.expectedDisbursementDate` | Current API | Yes | Same field | Timeline field. |
| `timeline.actualDisbursementDate` | Current API | Yes | Same field | Timeline field. |
| `timeline.expectedMaturityDate` | Current API | Yes | Same field | Timeline field. |
| `timeline.actualMaturityDate` | Current API | Yes | Same field | Timeline field. |
| `submittedOnDate` | Current API | No top-level field in example | `timeline.submittedOnDate` | Current API also exposes top-level submitted date. |
| `approvedOnDate` | Current API | No top-level field in example | `timeline.approvedOnDate` | Current API also exposes top-level approved date. |
| `rejectedOnDate` | Current API | No | None in active-loan example | Relevant for rejected loans. |
| `charges` | Missing from current API loan details response | Yes | `GET /v1/working-capital-loans/{loanId}/charges` | Charges are exposed through separate Working Capital charge endpoints, not embedded in loan details. |
| `disbursementDetails` | Current API | Yes | Same field | Same array concept, but item field names differ. |
| `disbursementDetails[].loanId` | Missing from current API disbursement item | Yes | Parent loan `id` | Current item does not include `loanId`. |
| `disbursementDetails[].principal` | Missing from current API under that name | Yes | `disbursementDetails[].actualAmount` or `expectedAmount` | Same purpose depends on whether actual or expected amount is intended. |
| `disbursementDetails[].disburseChargeAmount` | Missing from current API | Yes | None exact | Current Working Capital disbursement detail does not expose this. |
| `disbursementDetails[].expectedAmount` | Current API | No | Progressive uses `principal` | Expected disbursement amount. |
| `disbursementDetails[].actualAmount` | Current API | No | Progressive uses `principal` | Actual disbursement amount. |
| `disbursementDetails[].expectedMaturityDate` | Current API | No | `timeline.expectedMaturityDate` | Present in current item DTO and timeline. |
| `originators` | Missing from current API | Yes | None | Progressive loan-specific field. |
| `loanProductCounter` | Missing from current API response | Yes | None | Exists in Working Capital domain entity but is not exposed by `WorkingCapitalLoanData`. |
| `loanTermVariations` | Missing from current API | Yes | None | Progressive loan-specific field. |
| `fraud` | Missing from current API | Yes | None | Progressive loan-specific field. |
| `chargedOff` | Missing from current API as boolean field | Yes | `status`, transaction/classification data | No direct boolean in current Working Capital loan details response. |
| `enableInstallmentLevelDelinquency` | Missing from current API | Yes | `delinquency-range-schedule` endpoint | Current API exposes delinquency range schedules, not this boolean flag. |
| `delinquent` | Missing from current API under that name | Yes | `collectionData` | Same broad purpose, different name and structure. |
| `delinquent.pastDueDays` | Missing from current API | Yes | `collectionData.delinquentDays` | Similar but not exactly the same name. |
| `delinquent.nextPaymentDueDate` | Missing from current API | Yes | `delinquency-range-schedule.toDate` or amortization `payments[].paymentDate` | Similar scheduling data exists outside loan details. |
| `delinquent.nextPaymentAmount` | Missing from current API | Yes | `periodPaymentAmount`, delinquency range schedule `expectedAmount` | Similar purpose depending on calculation context. |
| `delinquent.delinquentDays` | Different shape | Yes | `collectionData.delinquentDays` | Same purpose, different parent object. |
| `delinquent.delinquentAmount` | Different shape | Yes | `collectionData.delinquentAmount` | Same purpose, different parent object. |
| `delinquent.lastPaymentDate` | Missing from current API | Yes | transaction list endpoint | Can be derived from transactions, not exposed in loan details. |
| `delinquent.lastPaymentAmount` | Missing from current API | Yes | transaction list endpoint | Can be derived from transactions, not exposed in loan details. |
| `delinquent.delinquencyPausePeriods` | Different shape | Yes | `collectionData.delinquencyPausePeriods` | Same purpose, different parent object. |
| `delinquent.delinquentPrincipal` | Different shape | Yes | `collectionData.delinquentPrincipal` | Same purpose, different parent object. |
| `collectionData` | Current API | No | Progressive uses `delinquent` | Same broad purpose, different parent field name. |
| `balance` | Current API | No | Progressive has selected flat monetary fields such as `principal` | Current API groups running balances under `balance`. |
| `breach` | Current API | No | None | Working Capital-specific breach configuration. |
| `nearBreach` | Current API | No | None | Working Capital-specific near breach configuration. |
| `delinquencyBucket` | Current API | No | None | Current Working Capital delinquency classification configuration. |
| `delinquencyGraceDays` | Current API | No | None | Current Working Capital delinquency setting. |
| `delinquencyStartType` | Current API | No | None | Current Working Capital delinquency setting. |
| `breachGraceDays` | Current API | No | None | Current Working Capital breach setting. |
| `lastClosedBusinessDate` | Current API | No | None | COB-related Working Capital field. |
| `paymentAllocation` | Current API | No | `transactionProcessingStrategyCode`, `transactionProcessingStrategyName` | Similar configuration area, but not the same response shape. |
| `dailyEir` | Current API | No | None | Current Working Capital calculated EIR field. |
| `calculatedAnnualEir` | Current API | No | None | Current Working Capital calculated annual EIR field. |
| `periodPaymentAmount` | Current API | No | `delinquent.nextPaymentAmount` maybe similar | Current expected periodic payment amount. |

Charge fields in the progressive response example:

| Field path | Current Working Capital API status | Present in progressive loan details example | Similar current Working Capital field/API | Notes |
| --- | --- | --- | --- | --- |
| `charges[].id` | Different shape | Yes | Charge endpoint response `id` | Available from separate charge API, not embedded in loan details. |
| `charges[].chargeId` | Different shape | Yes | Charge endpoint response `chargeId` | Available from separate charge API. |
| `charges[].name` | Different shape | Yes | Charge endpoint response `name` | Available from separate charge API. |
| `charges[].chargeTimeType` | Different shape | Yes | Charge endpoint response `chargeTimeType` | Available from separate charge API. |
| `charges[].submittedOnDate` | Different shape | Yes | Charge endpoint response `submittedOnDate` | Available from separate charge API. |
| `charges[].dueDate` | Different shape | Yes | Charge endpoint response `dueDate` | Available from separate charge API. |
| `charges[].chargeCalculationType` | Different shape | Yes | Charge endpoint response `chargeCalculationType` | Available from separate charge API. |
| `charges[].currency` | Different shape | Yes | Charge endpoint response `currency` | Available from separate charge API. |
| `charges[].amount` | Different shape | Yes | Charge endpoint response `amount` | Available from separate charge API. |
| `charges[].amountPaid` | Different shape | Yes | Charge endpoint response `amountPaid` | Available from separate charge API. |
| `charges[].amountOutstanding` | Different shape | Yes | Charge endpoint response `amountOutstanding` | Available from separate charge API. |
| `charges[].penalty` | Different shape | Yes | Charge endpoint response `penalty` | Available from separate charge API. |
| `charges[].chargePaymentMode` | Different shape | Yes | Charge endpoint response `chargePaymentMode` | Available from separate charge API. |
| `charges[].paid` | Different shape | Yes | Charge endpoint response `paid` | Available from separate charge API. |
| `charges[].loanId` | Different shape | Yes | Charge endpoint response `loanId` | Available from separate charge API. |
| `charges[].percentage` | Missing from current API | Yes | None exact | Not exposed by `WorkingCapitalLoanChargeData`. |
| `charges[].amountPercentageAppliedTo` | Missing from current API | Yes | None exact | Not exposed by `WorkingCapitalLoanChargeData`. |
| `charges[].amountWaived` | Missing from current API | Yes | None exact | Working Capital charge DTO does not expose waived amount. |
| `charges[].amountWrittenOff` | Missing from current API | Yes | None exact | Working Capital charge DTO does not expose written-off amount. |
| `charges[].amountOrPercentage` | Missing from current API | Yes | `amount` | Current API exposes `amount`; not the combined standard loan field. |
| `charges[].waived` | Missing from current API | Yes | None exact | Working Capital charge DTO does not expose waived boolean. |
| `charges[].chargePayable` | Missing from current API | Yes | None exact | Working Capital charge DTO does not expose charge payable boolean. |

### Loan Template Response Fields

Returned by `GET /v1/working-capital-loans/template`.

| Field | Description |
| --- | --- |
| `loanData` | Prefilled loan details when `productId` or `clientId` are supplied. |
| `productOptions` | Working Capital loan product options. |
| `fundOptions` | Fund options. |
| `delinquencyBucketOptions` | Delinquency bucket options. |
| `periodFrequencyTypeOptions` | Frequency type options. |
| `delinquencyStartTypeOptions` | Delinquency start type options. |
| `delinquencyMinimumPaymentTypeOptions` | Minimum payment type options. |
| `breachOptions` | Breach configuration options. |

## Transaction Fields

### Execute Transaction Request

Used by:

- `POST /v1/working-capital-loans/{loanId}/transactions?command={command}`
- `POST /v1/working-capital-loans/external-id/{loanExternalId}/transactions?command={command}`

Supported commands:

| Command | Main required fields | Description |
| --- | --- | --- |
| `repayment` | `transactionDate`, `transactionAmount` | Records repayment. Transaction date cannot be future or before disbursement. |
| `creditBalanceRefund` | `transactionDate`, `transactionAmount` | Refunds credit balance. Backdated credit balance refund is not allowed. |
| `goodwillCredit` | `transactionDate`, `transactionAmount` | Records goodwill credit. Uses repayment-style request validation. |
| `discountFee` | usually `transactionAmount`; may include `relatedResourceId` | Records discount fee transaction. |
| `discountFeeAdjustment` | `transactionAmount` | Adjusts an existing discount fee; `relatedResourceId` points to discount fee transaction. |

Request fields:

| Field | Type | Description |
| --- | --- | --- |
| `locale` | string | Locale for parsing. |
| `dateFormat` | string | Date format for parsing. |
| `transactionDate` | string | Transaction date. Required for repayment and credit balance refund. |
| `relatedResourceId` | long | Disbursement transaction ID for `discountFee`; discount fee transaction ID for `discountFeeAdjustment`. |
| `transactionAmount` | decimal | Transaction amount. Required for transaction commands. |
| `classificationId` | long | Optional code value ID for transaction classification. |
| `note` | string | Transaction note. |
| `externalId` | string | Optional transaction external ID. |
| `paymentDetails` | object | Optional payment detail object. |

`paymentDetails` fields are `paymentTypeId`, `accountNumber`, `checkNumber`, `routingCode`, `receiptNumber`, and `bankNumber`.

### Undo Transaction Request

Used by `POST /v1/working-capital-loans/{loanId}/transactions/{transactionId}?command=undo`.

The request body may include common command fields such as `note`, `locale`, and `dateFormat`. The target transaction is identified by the path parameter.

### Transaction Response Fields

Returned by transaction retrieve/list APIs.

| Field | Type | Description |
| --- | --- | --- |
| `id` | long | Transaction ID. |
| `type` | enum object | Transaction type, for example disbursement or repayment. |
| `transactionDate` | date | Transaction date. |
| `submittedOnDate` | date | Date transaction was submitted. |
| `transactionAmount` | decimal | Transaction amount. |
| `paymentDetailData` | object | Payment details. |
| `externalId` | string | Transaction external ID. |
| `reversed` | boolean | Whether transaction has been reversed. |
| `reversalExternalId` | string | External ID for reversal. |
| `reversedOnDate` | date | Reversal date. |
| `classification` | object | Transaction classification code value. |
| `principalPortion` | decimal | Principal amount allocated from transaction. |
| `feeChargesPortion` | decimal | Fee amount allocated from transaction. |
| `penaltyChargesPortion` | decimal | Penalty amount allocated from transaction. |

## Charge Fields

### Create Charge Request

Used by `POST /v1/working-capital-loans/{loanId}/charges`.

| Field | Type | Description |
| --- | --- | --- |
| `chargeId` | long | Charge definition ID. Required, greater than zero. |
| `amount` | decimal | Charge amount. Required, positive. |
| `dueDate` | string | Optional due date. |
| `externalId` | string | Optional charge external ID. |
| `locale` | string | Locale for parsing. |
| `dateFormat` | string | Date format for parsing `dueDate`. |

### Charge Response Fields

Returned by charge retrieve/list APIs.

| Field | Type | Description |
| --- | --- | --- |
| `id` | long | Loan charge ID. |
| `chargeId` | long | Charge definition ID. |
| `name` | string | Charge name. |
| `chargeTimeType` | enum object | Charge timing. |
| `submittedOnDate` | date | Date charge was submitted. |
| `dueDate` | date | Due date. |
| `chargeCalculationType` | enum object | Calculation mode. |
| `currency` | object | Currency metadata. |
| `amount` | decimal | Charge amount. |
| `amountPaid` | decimal | Amount paid. |
| `amountOutstanding` | decimal | Outstanding charge amount. |
| `chargeOptions` | array | Present on template response; available charge definitions. |
| `penalty` | boolean | Whether charge is a penalty. |
| `chargePaymentMode` | enum object | Payment mode. |
| `paid` | boolean | Whether charge is fully paid. |
| `loanId` | long | Working Capital loan ID. |
| `externalId` | string/object | Loan charge external ID. |
| `externalLoanId` | string/object | Loan external ID. |

## Delinquency Fields

### Create Delinquency Action Request

Used by `POST /v1/working-capital-loans/{loanId}/delinquency-actions`.

| Field | Type | Description |
| --- | --- | --- |
| `action` | string | Action type: `pause` or `reschedule`. |
| `startDate` | string | Pause start date. Required for `pause`. |
| `endDate` | string | Pause end date. Required for `pause`. |
| `minimumPayment` | decimal | Minimum payment value. Required with `minimumPaymentType` for reschedule. |
| `minimumPaymentType` | string | Minimum payment type: `PERCENTAGE` or `FLAT`. Required with `minimumPayment`. |
| `frequency` | integer | Frequency value. Required with `frequencyType` for reschedule. |
| `frequencyType` | string | Frequency unit. Required with `frequency`. |
| `dateFormat` | string | Date format. |
| `locale` | string | Locale. |

### Delinquency Action Response Fields

| Field | Type | Description |
| --- | --- | --- |
| `id` or `resourceId` | long | Delinquency action ID, depending on read vs write response. |
| `officeId` | long | Office ID in write response. |
| `clientId` | long | Client ID in write response. |
| `action` | string | Action type. |
| `startDate` | date | Action start date. |
| `endDate` | date | Action end date. |
| `minimumPayment` | decimal | Rescheduled minimum payment value. |
| `minimumPaymentType` | enum/string | Rescheduled minimum payment type. |
| `frequency` | integer | Rescheduled frequency value. |
| `frequencyType` | enum/string | Rescheduled frequency unit. |

### Delinquency Range Schedule Response Fields

Returned by `GET /v1/working-capital-loans/{loanId}/delinquency-range-schedule`.

| Field | Description |
| --- | --- |
| `id` | Schedule row ID. |
| `loanId` | Loan ID. |
| `periodNumber` | Schedule period number. |
| `fromDate` | Period start date. |
| `toDate` | Period end date. |
| `expectedAmount` | Expected payment amount for the period. |
| `paidAmount` | Amount paid for the period. |
| `outstandingAmount` | Outstanding amount for the period. |
| `minPaymentCriteriaMet` | Whether minimum payment criteria were met. |
| `delinquentDays` | Number of delinquent days for the period. |
| `delinquentAmount` | Delinquent amount for the period. |

### Delinquency Tag History Response Fields

Returned by `GET /v1/working-capital-loans/{loanId}/delinquencyrangetags`.

| Field | Description |
| --- | --- |
| `id` | Tag history ID. |
| `loanId` | Loan ID. |
| `delinquencyRange` | Delinquency range object. |
| `addedOnDate` | Date range tag was applied. |
| `liftedOnDate` | Date range tag was lifted. |
| `delinquentDays` | Delinquent days at tagging. |
| `rangeId` | Range ID. |
| `periodNumber` | Schedule period number. |
| `delinquentAmount` | Delinquent amount. |

## Breach and Near Breach Fields

### Breach Configuration Request

Used by `POST /v1/working-capital/breach/breaches` and `PUT /v1/working-capital/breach/breaches/{breachId}`.

| Field | Type | Description |
| --- | --- | --- |
| `name` | string | Breach configuration name. Required, max 100 characters. |
| `breachFrequency` | integer | Breach frequency value. Required, greater than zero. |
| `breachFrequencyType` | string | Breach frequency unit. Required. |
| `breachAmountCalculationType` | string | `PERCENTAGE` or `FLAT`. Required. |
| `breachAmount` | decimal | Breach amount or percentage. Required, zero or positive. |

### Breach Configuration Response

| Field | Description |
| --- | --- |
| `id` | Breach ID. |
| `name` | Breach name. |
| `breachFrequency` | Breach frequency value. |
| `breachFrequencyType` | Frequency type enum object. |
| `breachAmountCalculationType` | Amount calculation type enum object. |
| `breachAmount` | Breach threshold amount or percentage. |

### Near Breach Configuration Request

Used by `POST /v1/working-capital/near-breach` and `PUT /v1/working-capital/near-breach/{breachId}`.

| Field | Type | Description |
| --- | --- | --- |
| `nearBreachName` | string | Near breach name. Required. |
| `nearBreachFrequency` | integer | Near breach frequency. Required, greater than zero. |
| `nearBreachFrequencyType` | string | Near breach frequency unit. Required. |
| `nearBreachThreshold` | decimal | Near breach threshold percentage. Required and validated as percentage. |

### Near Breach Configuration Response

| Field | Description |
| --- | --- |
| `id` | Near breach ID. |
| `name` | Near breach name. |
| `frequency` | Near breach frequency. |
| `frequencyType` | Frequency type enum object. |
| `threshold` | Near breach threshold. |

### Breach Schedule Response Fields

Returned by `GET /v1/working-capital-loans/{loanId}/breach-schedule`.

| Field | Description |
| --- | --- |
| `id` | Breach schedule row ID. |
| `loanId` | Loan ID. |
| `periodNumber` | Breach period number. |
| `fromDate`, `toDate` | Breach period boundaries. |
| `numberOfDays` | Number of days in the breach period. |
| `minPaymentAmount` | Minimum payment amount expected for the period. |
| `outstandingAmount` | Outstanding amount for the period. |
| `nearBreach` | Whether the period is near breach. |
| `breach` | Whether the period is in breach. |

## Amortization Schedule Fields

Returned by `GET /v1/working-capital-loans/{loanId}/amortization-schedule`.

| Field | Type | Description |
| --- | --- | --- |
| `discountFeeAmount` | decimal | Discount fee amount. |
| `netDisbursementAmount` | decimal | Net disbursement after discount fee. |
| `totalPaymentVolume` | decimal | Total payment volume. |
| `periodPaymentRate` | decimal | Period payment rate. |
| `npvDayCount` | integer | NPV day count. |
| `expectedDisbursementDate` | date | Expected disbursement date. |
| `expectedPaymentAmount` | decimal | Expected amount per payment period. |
| `originalPaymentNumber` | integer | Original number of payments. |
| `effectiveInterestRate` | decimal | Effective interest rate. |
| `payments` | array | Per-payment amortization rows. |

`payments` item fields:

| Field | Description |
| --- | --- |
| `paymentNo` | Payment sequence number. |
| `paymentDate` | Expected payment date. |
| `expectedPaymentAmount` | Expected payment amount. |
| `expectedBalance` | Expected remaining balance. |
| `actualBalance` | Actual remaining balance. |
| `expectedAmortizationAmount` | Expected amortization amount. |
| `actualPaymentAmount` | Actual payment amount. |
| `actualAmortizationAmount` | Actual amortization amount. |
| `expectedDiscountFeeBalance` | Expected discount fee balance. |
| `actualDiscountFeeBalance` | Actual discount fee balance. |

## COB Catch-Up Fields

### Oldest COB Closed Response

Returned by `GET /v1/working-capital-loans/oldest-cob-closed`.

| Field | Description |
| --- | --- |
| `loanIds` | List of Working Capital loan IDs at the oldest processed COB point. |
| `cobProcessedDate` | Oldest COB processed date. |
| `cobBusinessDate` | COB business date. |

### Catch-Up Execution Response

Returned by `POST /v1/working-capital-loans/catch-up`.

| HTTP status | Description |
| --- | --- |
| `200` | All loans are up to date. |
| `202` | Catch-up has been started. |
| `400` | Catch-up is already running. |

### Is Catch-Up Running Response

Returned by `GET /v1/working-capital-loans/is-catch-up-running`.

| Field | Description |
| --- | --- |
| `isCatchUpRunning` | Whether catch-up is running. |
| `processingDate` | Current catch-up processing date when catch-up is running. |

## Internal and Test-Only APIs

The module also contains internal/test APIs under `/v1/internal/working-capital-loans`. These are annotated as test profile/internal APIs and should not be treated as production API surface.

Examples include:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/v1/internal/working-capital-loans/{loanId}/amortization-schedule` | Generate and save projected amortization schedule for testing. |
| `POST` | `/v1/internal/working-capital-loans/{loanId}/activate` | Activate loan for testing. |
| `POST` | `/v1/internal/working-capital-loans/{loanId}/generate-next-delinquency-period` | Generate next delinquency period for testing. |
| `POST` | `/v1/internal/working-capital-loans/{loanId}/internalMakePayment` | Make internal test payment. |
| `POST` | `/v1/internal/working-capital-loans/internal/lastCobRun` | Return captured last COB run test data. |
| `DELETE` | `/v1/internal/working-capital-loans/internal/lastCobRun` | Clear captured last COB run test data. |
| `POST` | `/v1/internal/working-capital-loans/{loanId}/place-lock/{lockOwner}` | Place a test account lock for a Working Capital loan. |

## Minimal API Flow

1. Create or retrieve breach and near breach configurations if the product needs breach tracking.
2. Create a Working Capital loan product with principal/rate defaults, repayment frequency, payment allocation, delinquency settings, and accounting rule.
3. Submit a Working Capital loan application for an active client using `POST /v1/working-capital-loans`.
4. Approve the loan using `POST /v1/working-capital-loans/{loanId}?command=approve`.
5. Disburse the loan using `POST /v1/working-capital-loans/{loanId}?command=disburse`.
6. Record repayments or other transactions through `POST /v1/working-capital-loans/{loanId}/transactions?command=...`.
7. Read balances, schedules, transactions, charges, delinquency tags, and breach schedules from the GET endpoints.
