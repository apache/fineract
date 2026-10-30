@LoanTransactionAdjustmentFeature
Feature: LoanTransactionAdjustment

  @TestRailId:C119578
  Scenario: Verify loan transaction adjustment - UC1: adjust Payout Refund amount down and up
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2026" with 250 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
    # --- Payout refund ---
    When Admin sets the business date to "20 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "20 June 2026" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    Then Loan has 700 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
    # --- Adjust payout refund down ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 1th "PAYOUT_REFUND" transaction made on "20 June 2026" with "AUTOPAY" payment type on "20 June 2026" with 30.0 EUR transaction amount
    Then Adjusted transaction is created with 30.0 amount and "AUTOPAY" payment type
    And LoanAdjustTransactionBusinessEvent for "Payout Refund" transaction on "20 June 2026" is created without external owner
    And Loan has 720 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | true     | false    |
      | 20 June 2026     | Payout Refund    | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 720.0        | false    | false    |
    # --- Adjust payout refund up and forward in date ---
    When Admin sets the business date to "24 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 2th "PAYOUT_REFUND" transaction made on "20 June 2026" with "AUTOPAY" payment type on "24 June 2026" with 60.0 EUR transaction amount
    And Loan has 690 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | true     | false    |
      | 20 June 2026     | Payout Refund    | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 720.0        | true     | false    |
      | 24 June 2026     | Payout Refund    | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 690.0        | false    | false    |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "24 June 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 60.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 60.0  |        |
    # Closing the loan
    When Loan Pay-off is made on "24 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119579
  Scenario: Verify loan transaction adjustment - UC2: adjust Payout Refund date backward and forward
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2026" with 250 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
    # --- Payout refund ---
    When Admin sets the business date to "20 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "20 June 2026" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    Then Loan has 700 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
    # --- Backdate payout refund before the repayment ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 1th "PAYOUT_REFUND" transaction made on "20 June 2026" with "AUTOPAY" payment type on "10 June 2026" with 50.0 EUR transaction amount
    Then Loan has 700 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 950.0        | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 20 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | true     | false    |
    # --- Move payout refund forward again ---
    When Admin sets the business date to "25 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 1th "PAYOUT_REFUND" transaction made on "10 June 2026" with "AUTOPAY" payment type on "25 June 2026" with 50.0 EUR transaction amount
    Then Loan has 700 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 950.0        | true     | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | true     | false    |
      | 25 June 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "25 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119580
  Scenario: Verify loan transaction adjustment - UC3: adjust Goodwill Credit amount and date keeps goodwill accounting
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2026" with 300 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
    # --- Goodwill credit ---
    When Admin sets the business date to "20 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "20 June 2026" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan has 600 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 20 June 2026     | Goodwill Credit  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
    # --- Adjust goodwill credit ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 1th "GOODWILL_CREDIT" transaction made on "20 June 2026" with "AUTOPAY" payment type on "22 June 2026" with 150.0 EUR transaction amount
    Then Adjusted transaction is created with 150.0 amount and "AUTOPAY" payment type
    And Loan has 550 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 20 June 2026     | Goodwill Credit  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 600.0        | true     | false    |
      | 22 June 2026     | Goodwill Credit  | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 550.0        | false    | false    |
    Then Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "22 June 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable         |       | 150.0  |
      | EXPENSE | 744003       | Goodwill Expense Account | 150.0 |        |
    # Closing the loan
    When Loan Pay-off is made on "22 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119581
  Scenario: Verify loan transaction adjustment - UC4: adjust Merchant Issued Refund on loan without interest refund support
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2026" with 250 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
    # --- Merchant issued refund ---
    When Admin sets the business date to "20 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 June 2026" with 50 EUR transaction amount and system-generated Idempotency key
    Then Loan has 700 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
    # --- Adjust merchant issued refund backdated ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 1th "MERCHANT_ISSUED_REFUND" transaction made on "20 June 2026" with "AUTOPAY" payment type on "18 June 2026" with 80.0 EUR transaction amount
    Then Adjusted transaction is created with 80.0 amount and "AUTOPAY" payment type
    And Loan has 670 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 18 June 2026     | Merchant Issued Refund | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 670.0        | false    | false    |
      | 20 June 2026     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | true     | false    |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "18 June 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 80.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 80.0  |        |
    # Closing the loan
    When Loan Pay-off is made on "22 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119582
  Scenario: Verify loan transaction adjustment - UC5: Goodwill Credit adjustment closes and reopens the loan
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2026" with 250 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
    # --- Goodwill credit closes the loan ---
    When Admin sets the business date to "20 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "20 June 2026" with 750 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Goodwill Credit  | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Adjustment overpays the loan ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 1th "GOODWILL_CREDIT" transaction made on "20 June 2026" with "AUTOPAY" payment type on "20 June 2026" with 800.0 EUR transaction amount
    Then Loan status will be "OVERPAID"
    And Loan has 50.0 overpaid amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Goodwill Credit  | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 20 June 2026     | Goodwill Credit  | 800.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Adjustment reopens the loan ---
    When Admin sets the business date to "24 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 2th "GOODWILL_CREDIT" transaction made on "20 June 2026" with "AUTOPAY" payment type on "20 June 2026" with 700.0 EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan has 50.0 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Goodwill Credit  | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 20 June 2026     | Goodwill Credit  | 800.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 20 June 2026     | Goodwill Credit  | 700.0  | 700.0     | 0.0      | 0.0  | 0.0       | 50.0         | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "24 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119583
  Scenario: Verify loan transaction adjustment - UC6: adjust Charge Adjustment keeps the link to the loan charge
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 June 2026" due date and 10 EUR transaction amount
    # --- Charge adjustment ---
    When Admin sets the business date to "20 June 2026"
    And Admin runs inline COB job for Loan
    And Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "15 June 2026" with 4 EUR transaction amount and externalId ""
    Then Loan has 1006 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 20 June 2026     | Charge Adjustment | 4.0    | 0.0       | 0.0      | 0.0  | 4.0       | 1000.0       | false    | false    |
    # --- Adjust the charge adjustment ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 1th "CHARGE_ADJUSTMENT" transaction made on "20 June 2026" with "AUTOPAY" payment type on "22 June 2026" with 6.0 EUR transaction amount
    Then Adjusted transaction is created with 6.0 amount and "AUTOPAY" payment type
    And Loan has 1004 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 20 June 2026     | Charge Adjustment | 4.0    | 0.0       | 0.0      | 0.0  | 4.0       | 1000.0       | true     | false    |
      | 22 June 2026     | Charge Adjustment | 6.0    | 0.0       | 0.0      | 0.0  | 6.0       | 1000.0       | false    | false    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "22 June 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 6.0    |
      | INCOME | 404007       | Fee Income              | 6.0   |        |
    # Closing the loan
    When Loan Pay-off is made on "22 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119584
  Scenario: Verify loan transaction adjustment - UC7: Charge Adjustment over the available charge amount is rejected
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 June 2026" due date and 10 EUR transaction amount
    # --- Charge adjustment ---
    When Admin sets the business date to "20 June 2026"
    And Admin runs inline COB job for Loan
    And Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "15 June 2026" with 4 EUR transaction amount and externalId ""
    Then Loan has 1006 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 20 June 2026     | Charge Adjustment | 4.0    | 0.0       | 0.0      | 0.0  | 4.0       | 1000.0       | false    | false    |
    # --- Adjust charge adjustment over available amount fails ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    Then Customer is forbidden to adjust "1"th "Charge Adjustment" transaction made on "20 June 2026" with 12.0 EUR transaction amount due to a validation error:
      | http code     | 403                                                                                 |
      | error message | Transaction amount cannot be higher than the charge amount                              |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 20 June 2026     | Charge Adjustment | 4.0    | 0.0       | 0.0      | 0.0  | 4.0       | 1000.0       | false    | false    |
    # --- Adjust charge adjustment up to the available amount works ---
    And Customer adjust 1th "CHARGE_ADJUSTMENT" transaction made on "20 June 2026" with "AUTOPAY" payment type on "22 June 2026" with 10.0 EUR transaction amount
    Then Loan has 1000 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 20 June 2026     | Charge Adjustment | 4.0    | 0.0       | 0.0      | 0.0  | 4.0       | 1000.0       | true     | false    |
      | 22 June 2026     | Charge Adjustment | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 1000.0       | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "22 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119585
  Scenario: Verify loan transaction adjustment - UC8: Payout Refund adjustment is rejected when interest refund is enabled
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 July 2026      | 1000           | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1000" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "10 July 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "10 July 2026" with 338.9 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2026     | Repayment        | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 09 July 2026     | Accrual          | 2.15   | 0.0       | 2.15     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Payout refund ---
    When Admin sets the business date to "15 July 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 July 2026" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2026     | Repayment        | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 09 July 2026     | Accrual          | 2.15   | 0.0       | 2.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |
      | 10 July 2026     | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 July 2026     | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 July 2026     | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 July 2026     | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 July 2026     | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Adjust payout refund fails: interest refund enabled on product ---
    Then Customer is forbidden to adjust "1"th "Payout Refund" transaction made on "15 July 2026" with 40.0 EUR transaction amount due to a validation error:
      | http code     | 403                                                                 |
      | error message | cannot be adjusted as interest refund is enabled for loanTransactionType.payoutRefund |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2026     | Repayment        | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 09 July 2026     | Accrual          | 2.15   | 0.0       | 2.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2026     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |
      | 10 July 2026     | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 July 2026     | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 July 2026     | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 July 2026     | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 July 2026     | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "15 July 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119586
  Scenario: Verify loan transaction adjustment - UC9: Merchant Issued Refund adjustment is rejected when interest refund is enabled
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 July 2026      | 1000           | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1000" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "10 July 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "10 July 2026" with 338.9 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2026     | Repayment        | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 09 July 2026     | Accrual          | 2.15   | 0.0       | 2.15     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Merchant issued refund ---
    When Admin sets the business date to "15 July 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 July 2026" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2026     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2026     | Repayment              | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 09 July 2026     | Accrual                | 2.15   | 0.0       | 2.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2026     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |
      | 10 July 2026     | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 July 2026     | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 July 2026     | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 July 2026     | Accrual                | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 July 2026     | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Adjust MIR fails: interest refund enabled on product ---
    Then Customer is forbidden to adjust "1"th "Merchant Issued Refund" transaction made on "15 July 2026" with 40.0 EUR transaction amount due to a validation error:
      | http code     | 403                                                                       |
      | error message | cannot be adjusted as interest refund is enabled for loanTransactionType.merchantIssuedRefund |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2026     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2026     | Repayment              | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 09 July 2026     | Accrual                | 2.15   | 0.0       | 2.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2026     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |
      | 10 July 2026     | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 July 2026     | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 July 2026     | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 July 2026     | Accrual                | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 July 2026     | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "15 July 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119587
  Scenario: Verify loan transaction adjustment - UC10: reversal with zero amount still works for Payout Refund, Goodwill Credit and Merchant Issued Refund
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2026" with 300 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
    # --- Payout refund / Goodwill credit / Merchant issued refund ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "20 June 2026" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "21 June 2026" with 60 EUR transaction amount and system-generated Idempotency key
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "22 June 2026" with 40 EUR transaction amount and system-generated Idempotency key
    Then Loan has 550 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment              | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 20 June 2026     | Payout Refund          | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
      | 21 June 2026     | Goodwill Credit        | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 590.0        | false    | false    |
      | 22 June 2026     | Merchant Issued Refund | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 550.0        | false    | false    |
    # --- Revert all three transaction types ---
    When Admin sets the business date to "25 June 2026"
    And Admin runs inline COB job for Loan
    And Customer undo "1"th "Merchant Issued Refund" transaction made on "22 June 2026"
    Then Loan has 590 outstanding amount
    And Customer undo "1"th "Goodwill Credit" transaction made on "21 June 2026"
    Then Loan has 650 outstanding amount
    And Customer undo "1"th "Payout Refund" transaction made on "20 June 2026"
    Then Loan has 700 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment              | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 20 June 2026     | Payout Refund          | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 650.0        | true     | false    |
      | 21 June 2026     | Goodwill Credit        | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 590.0        | true     | false    |
      | 22 June 2026     | Merchant Issued Refund | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 550.0        | true     | false    |
    # Closing the loan
    When Loan Pay-off is made on "25 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119588
  Scenario: Verify loan transaction adjustment - UC11: Merchant Issued Refund adjustment is rejected when a linked interest refund exists
    When Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2026      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "1000" amount and expected disbursement date on "01 June 2026"
    And Admin successfully disburse the loan on "01 June 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2026" with 250 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
    # --- Merchant issued refund with manual interest refund ---
    When Admin sets the business date to "20 June 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 June 2026" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    And Admin manually adds Interest Refund for "MERCHANT_ISSUED_REFUND" transaction made on "20 June 2026" with 0.1 EUR interest refund amount
    Then Loan has 699.9 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 20 June 2026     | Interest Refund        | 0.1    | 0.1       | 0.0      | 0.0  | 0.0       | 699.9        | false    | false    |
    # --- Adjust MIR fails: interest refund is enabled for MIR on the product ---
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Loan
    Then Customer is forbidden to adjust "1"th "Merchant Issued Refund" transaction made on "20 June 2026" with 40.0 EUR transaction amount due to a validation error:
      | http code     | 403                                                                       |
      | error message | cannot be adjusted as interest refund is enabled for loanTransactionType.merchantIssuedRefund |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2026     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 June 2026     | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 20 June 2026     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 20 June 2026     | Interest Refund        | 0.1    | 0.1       | 0.0      | 0.0  | 0.0       | 699.9        | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "22 June 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119589
  Scenario: Verify loan transaction adjustment - UC12: backdated Goodwill Credit adjustment on progressive loan with interest recalculation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2026   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2026" with 172.55 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2026 | Repayment        | 172.55 | 162.55    | 10.0     | 0.0  | 0.0       | 837.45       | false    | false    |
      | 31 January 2026  | Accrual          | 9.68   | 0.0       | 9.68     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Goodwill credit ---
    When Admin sets the business date to "10 February 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 February 2026" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2026 | Repayment        | 172.55 | 162.55    | 10.0     | 0.0  | 0.0       | 837.45       | false    | false    |
      | 31 January 2026  | Accrual          | 9.68   | 0.0       | 9.68     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2026 | Goodwill Credit  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 737.45       | false    | false    |
      | 01 February 2026 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2026 | Accrual          | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Backdate goodwill credit before the repayment ---
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Loan
    And Customer adjust 1th "GOODWILL_CREDIT" transaction made on "10 February 2026" with "AUTOPAY" payment type on "25 January 2026" with 100.0 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 25 January 2026  | Goodwill Credit  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 01 February 2026 | Repayment        | 172.55 | 162.78    | 9.77     | 0.0  | 0.0       | 737.22       | false    | true     |
      | 31 January 2026  | Accrual          | 9.68   | 0.0       | 9.68     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2026 | Goodwill Credit  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 737.45       | true     | false    |
      | 01 February 2026 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2026 | Accrual          | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2026 | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2026 | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2026 | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2026 | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2026 | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "15 February 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
