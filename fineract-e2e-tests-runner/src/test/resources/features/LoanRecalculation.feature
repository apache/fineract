@LoanRecalculationFeature
Feature: LoanRecalculation

  @TestRailId:C127986 @AdvancedPaymentAllocation
  Scenario: Verify loan recalculation command - UC1: rescheduled and re-aged loan with down payment stays unchanged
    When Admin sets the business date to "01 June 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with auto downpayment 33.333333% and with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 June 2026      | 99             | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "99" amount and expected disbursement date on "01 June 2026"
    When Admin successfully disburse the loan on "01 June 2026" with "99" EUR transaction amount
    When Admin sets the business date to "30 June 2026"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2026       | 30 June 2026    | 01 August 2026  |                  |                 |            |                 |
    When Admin sets the business date to "29 July 2026"
    And Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments |
      | 1               | MONTHS        | 29 August 2026 | 6                    |
    Then Loan status will be "ACTIVE"
    When Admin takes a snapshot of the Loan repayment schedule and transactions
    # --- Recalculation ---
    When Admin runs loan recalculation for Loan
    Then Loan repayment schedule and transactions are the same as in the snapshot
    And LoanBalanceChangedBusinessEvent is raised for the Loan
    # Closing the loan
    When Loan Pay-off is made on "29 July 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C127987 @AdvancedPaymentAllocation
  Scenario: Verify loan recalculation command - UC2: healthy interest bearing loan with repayment and accruals stays unchanged
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    # --- Repayment ---
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "05 February 2024"
    And Admin runs inline COB job for Loan
    When Admin takes a snapshot of the Loan repayment schedule and transactions
    # --- Recalculation ---
    When Admin runs loan recalculation for Loan
    Then Loan repayment schedule and transactions are the same as in the snapshot
    # Closing the loan
    When Loan Pay-off is made on "05 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C127988 @AdvancedPaymentAllocation
  Scenario: Verify loan recalculation command - UC3: re-aged NPA loan stays unchanged
    Given Loan product "LP2_RECALCULATION_NPA_AFTER_1_DAY" dedicated to this scenario exists with interest recalculation and overdueDaysForNPA 1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_RECALCULATION_NPA_AFTER_1_DAY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin runs inline COB job for Loan
    And Admin runs the "UPDATE_NON_PERFORMING_ASSETS" job
    Then Loan is NPA
    # --- Re-age ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan is NPA
    When Admin takes a snapshot of the Loan repayment schedule and transactions
    # --- Recalculation ---
    When Admin runs loan recalculation for Loan
    Then Loan is NPA
    And Loan repayment schedule and transactions are the same as in the snapshot
    # Closing the loan
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C127989
  Scenario: Verify loan recalculation command - UC4: cumulative loan is rejected
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 01 July 2023      | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 July 2023" with "3000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "3000" EUR transaction amount
    Then Admin fails to run loan recalculation for Loan with status code 403 and error "error.msg.loan.recalculation.not.progressive"
    # Closing the loan
    When Loan Pay-off is made on "01 July 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C127990 @AdvancedPaymentAllocation
  Scenario: Verify loan recalculation command - UC5: loan that is not disbursed yet is rejected
    When Admin sets the business date to "01 June 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with auto downpayment 33.333333% and with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 June 2026      | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "100" amount and expected disbursement date on "01 June 2026"
    Then Admin fails to run loan recalculation for Loan with status code 403 and error "error.msg.loan.recalculation.invalid.status"
