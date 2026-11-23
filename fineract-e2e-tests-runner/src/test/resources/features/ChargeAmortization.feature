@ChargeAmortizationFeature
Feature: Charge amortization - deferred charge transaction types and events

  @TestRailId:C127976
  Scenario: Verify charge amortization transaction types - UC1: Excluded transaction types filter accepts deferred charge types without hiding existing transactions
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2026   | 2000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "2000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "2000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2026" with 2000 EUR transaction amount
    Then Filtered out transactions list contains the the following entries when filtered out by loanId for transaction types: "deferred_charge, deferred_charge_amortization, deferred_charge_amortization_adjustment"
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 01 January 2026  | repayment        | 2000.0 | 2000.0    |          |      |           | 0.0          |
    Then Filtered out transactions list contains the the following entries when filtered out by loanExternalId for transaction types: "deferred_charge, deferred_charge_amortization, deferred_charge_amortization_adjustment"
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 01 January 2026  | repayment        | 2000.0 | 2000.0    |          |      |           | 0.0          |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 2000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 2000.0 |        |
    Then Loan has 0.0 outstanding amount
    Then Loan has 0.0 overpaid amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C127977
  Scenario: Verify charge amortization transaction types - UC2: External asset owner excluded transaction types template and attribute accept deferred charge types
    Then External asset owner loan product attributes template attribute "EXCLUDED_TRANSACTION_TYPES" has multiValue "true", contains "DEFERRED_CHARGE,DEFERRED_CHARGE_AMORTIZATION,DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT" and does not contain "INVALID,DEFERRED,IMMEDIATE"
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" with value "DEFERRED_CHARGE,DEFERRED_CHARGE_AMORTIZATION,DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT" for the new loan product
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product has value "DEFERRED_CHARGE,DEFERRED_CHARGE_AMORTIZATION,DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT"

  @TestRailId:C127978
  Scenario: Verify charge amortization business events - UC3: External event configuration contains deferred charge business events
    Then External event configuration contains the following event types: "LoanDeferredChargeTransactionCreatedBusinessEvent, LoanDeferredChargeAmortizationTransactionCreatedBusinessEvent, LoanDeferredChargeAmortizationAdjustmentTransactionCreatedBusinessEvent"
