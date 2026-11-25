@LoanProgressiveModelRebuildFeature
Feature: LoanProgressiveModelRebuild

  @TestRailId:C127991 @AdvancedPaymentAllocation
  Scenario: Verify progressive loan model rebuild after re-age - UC1: write request on a rescheduled and re-aged loan with down payment
    When Admin sets the business date to "01 June 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with auto downpayment 33.333333% and with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 June 2026      | 99             | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "99" amount and expected disbursement date on "01 June 2026"
    When Admin successfully disburse the loan on "01 June 2026" with "99" EUR transaction amount
    When Admin sets the business date to "30 June 2026"
    And Admin runs inline COB job for Loan
    # --- Reschedule ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2026       | 30 June 2026    | 01 August 2026  |                  |                 |            |                 |
    When Admin sets the business date to "29 July 2026"
    And Admin runs inline COB job for Loan
    # --- Re-age ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments |
      | 1               | MONTHS        | 29 August 2026 | 6                    |
    Then Loan has 66.0 outstanding amount
    When Admin remembers the Loan repayment schedule due dates
    # --- Goodwill credit after the model was deleted ---
    When Call Internal API to remove progressive loan model by loan Id
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "29 July 2026" with 5 EUR transaction amount
    Then Loan repayment schedule due dates are the same as remembered
    And Loan has 61.0 outstanding amount
    # Closing the loan
    When Loan Pay-off is made on "29 July 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C127992 @AdvancedPaymentAllocation
  Scenario: Verify progressive loan model rebuild after re-age - UC2: inline COB on a rescheduled and re-aged loan
    When Admin sets the business date to "01 June 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with auto downpayment 33.333333% and with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 June 2026      | 99             | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2026" with "99" amount and expected disbursement date on "01 June 2026"
    When Admin successfully disburse the loan on "01 June 2026" with "99" EUR transaction amount
    When Admin sets the business date to "30 June 2026"
    And Admin runs inline COB job for Loan
    # --- Reschedule ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2026       | 30 June 2026    | 01 August 2026  |                  |                 |            |                 |
    When Admin sets the business date to "29 July 2026"
    And Admin runs inline COB job for Loan
    # --- Re-age ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments |
      | 1               | MONTHS        | 29 August 2026 | 6                    |
    When Admin remembers the Loan repayment schedule due dates
    When Call Internal API to remove progressive loan model by loan Id
    When Admin sets the business date to "30 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan repayment schedule due dates are the same as remembered
    And Loan has 66.0 outstanding amount
    # Closing the loan
    When Loan Pay-off is made on "30 July 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C127993 @AdvancedPaymentAllocation
  Scenario: Verify progressive loan model rebuild after re-age - UC3: write request on a rescheduled and re-aged interest bearing loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "10 February 2024"
    And Admin runs inline COB job for Loan
    # --- Reschedule ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 10 February 2024 | 15 March 2024   |                  |                 |            |                 |
    When Admin sets the business date to "15 February 2024"
    And Admin runs inline COB job for Loan
    # --- Re-age ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 15 April 2024 | 6                    |
    When Admin remembers the Loan repayment schedule due dates
    # --- Goodwill credit after the model was deleted ---
    When Call Internal API to remove progressive loan model by loan Id
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 February 2024" with 5 EUR transaction amount
    Then Loan repayment schedule due dates are the same as remembered
    # Closing the loan
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
