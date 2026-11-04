@ProgressiveLoanBackdatedPeriodicAccrual
Feature: Periodic accrual for backdated month-end progressive loans

  @TestRailId:C119601
  Scenario: Periodic accrual succeeds for an unpaid backdated month-end loan
    Given Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin remembers the current loan as "backdated month-end loan"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan saved interest model has 6 complete repayment periods
    And Loan saved interest model has overdue corrections through "30 April 2024"
    And Loan has 0 "ACCRUAL" transactions on Transactions tab
    When Admin sets the business date to "03 March 2024"
    And Admin remembers the loan saved interest model
    And Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan has 1 "ACCRUAL" transactions on Transactions tab
    And Loan Transactions tab has a transaction with date: "03 March 2024", and with the following data:
      | Transaction Type | Amount | Interest | Reverted | Replayed |
      | Accrual          | 83.04  | 83.04    | false    | false    |
    And Loan interest is accrued through "03 March 2024"
    And Loan saved interest model is unchanged
    When Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan has 1 "ACCRUAL" transactions on Transactions tab
    And Loan has 83.04 total Accruals
    When Admin sets the business date to "04 March 2024"
    And Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan status will be "ACTIVE"
    And Loan principal outstanding is "2450"
    And Loan has 0 "REPAYMENT" transactions on Transactions tab
    And Loan has 2 "ACCRUAL" transactions on Transactions tab
    And Loan has 83.70 total Accruals
    And Loan Transactions tab has a transaction with date: "04 March 2024", and with the following data:
      | Transaction Type | Amount | Interest | Reverted | Replayed |
      | Accrual          | 0.66   | 0.66     | false    | false    |
    And Loan has no "Accrual" transaction with transaction date later than "04 March 2024", including reversed transactions
    And Loan interest is accrued through "04 March 2024"
    And Loan saved interest model is unchanged

  @TestRailId:C119602
  Scenario Outline: Historical periodic accrual respects repayment and maturity boundaries
    Given Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin remembers the current loan as "backdated month-end loan"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan saved interest model has 6 complete repayment periods
    And Loan saved interest model has overdue corrections through "30 April 2024"
    And Loan has 0 "ACCRUAL" transactions on Transactions tab
    When Admin sets the business date to "<businessDate>"
    And Admin remembers the loan saved interest model
    And Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan status will be "ACTIVE"
    And Loan principal outstanding is "2450"
    And Loan has 0 "REPAYMENT" transactions on Transactions tab
    And Loan has 1 "ACCRUAL" transactions on Transactions tab
    And Loan Transactions tab has a transaction with date: "<accrualDate>", and with the following data:
      | Transaction Type | Amount     | Interest   | Reverted | Replayed |
      | Accrual          | <interest> | <interest> | false    | false    |
    And Loan interest is accrued through "<accrualDate>"
    And Loan has no "Accrual" transaction with transaction date later than "<accrualDate>", including reversed transactions
    And Loan saved interest model is unchanged

    Examples:
      | businessDate     | accrualDate      | interest |
      | 29 February 2024 | 29 February 2024 | 81.03    |
      | 29 April 2024    | 29 April 2024    | 121.15   |
      | 01 May 2024      | 30 April 2024    | 121.82   |

  @TestRailId:C119603
  Scenario: Historical accrual retains credits at a repayment boundary after overdue recalculation
    Given Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin remembers the current loan as "credited boundary loan"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 1
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    When Admin sets the business date to "21 January 2025"
    And Admin regenerates the saved progressive loan interest model
    Then Loan retrieved progressive interest model matches the saved model
    And Loan saved interest model has a credited boundary from "01 March 2024" to "01 April 2024" with overdue corrections through "01 July 2024"
    And Loan saved interest model has 6 complete repayment periods
    And Loan has 0 "ACCRUAL" transactions on Transactions tab
    When Admin sets the business date to "01 March 2024"
    And Admin remembers the loan saved interest model
    And Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan status will be "ACTIVE"
    And Loan principal outstanding is "98.57"
    And Loan has 1 "ACCRUAL" transactions on Transactions tab
    And Loan Transactions tab has a transaction with date: "01 March 2024", and with the following data:
      | Transaction Type | Amount | Interest | Reverted | Replayed |
      | Accrual          | 1.07   | 1.07     | false    | false    |
    And Loan has no "Accrual" transaction with transaction date later than "01 March 2024", including reversed transactions
    And Loan interest is accrued through "01 March 2024"
    And Loan saved interest model is unchanged

  @TestRailId:C119604
  Scenario: Accrual jobs post tranche interest while an unpaid backdated month-end loan remains active
    Given Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin remembers the current loan as "backdated month-end loan"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan saved interest model has 6 complete repayment periods
    And Loan saved interest model has overdue corrections through "30 April 2024"
    And Loan has 0 "ACCRUAL" transactions on Transactions tab
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin remembers charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" for restoration
    And Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 5.0 EUR amount
    And Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                     | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 5.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin remembers the current loan as "tranche loan"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    And Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan has 0 "ACCRUAL" transactions on Transactions tab
    When Admin successfully runs the "ACCRUAL_ACTIVITY_POSTING" job
    And Admin successfully runs the "ADD_ACCRUAL_TRANSACTIONS" job
    And Admin successfully runs the "ADD_ACCRUAL_TRANSACTIONS_FOR_LOANS_WITH_INCOME_POSTED_AS_TRANSACTIONS" job
    And Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    And Admin successfully runs the "RECALCULATE_INTEREST_FOR_LOANS" job
    Then Loan has 1 "ACCRUAL" transactions on Transactions tab
    And Loan Transactions tab has a transaction with date: "03 March 2024", and with the following data:
      | Transaction Type | Amount | Interest | Reverted | Replayed |
      | Accrual          | 1.1    | 1.1      | false    | false    |
    And Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 1.1   |        |
      | INCOME | 404000       | Interest Income         |       | 1.1    |
    When Admin selects the loan named "backdated month-end loan"
    Then Loan status will be "ACTIVE"
    And Loan principal outstanding is "2450"
    And Loan has 0 "REPAYMENT" transactions on Transactions tab
    And Loan has 83.04 total Accruals
    And Loan Transactions tab has a transaction with date: "03 March 2024", and with the following data:
      | Transaction Type | Reverted | Replayed |
      | Accrual          | false    | false    |
    And Loan has no "Accrual" transaction with transaction date later than "03 March 2024", including reversed transactions
    And Loan interest is accrued through "03 March 2024"
    And Loan saved interest model has 6 complete repayment periods

  @TestRailId:C119605
  Scenario: Repayment on the historical accrual date and next-day periodic accrual succeed for an unpaid backdated month-end loan
    Given Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin remembers the current loan as "backdated month-end loan"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan saved interest model has overdue corrections through "30 April 2024"
    When Admin sets the business date to "03 March 2024"
    And Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan has 1 "ACCRUAL" transactions on Transactions tab
    When Customer makes "AUTOPAY" repayment on "03 March 2024" with 420.28 EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan principal outstanding is "2049.84"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 2450.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2450.0       | false    | false    |
      | 03 March 2024    | Accrual          | 83.04  | 0.0       | 83.04    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Repayment        | 420.28 | 400.16    | 20.12    | 0.0  | 0.0       | 2049.84      | false    | false    |
    And Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 31 October 2023  |               | 2450.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 30 November 2023 | 03 March 2024 | 2049.84         | 400.16        | 20.12    | 0.0  | 0.0       | 420.28 | 420.28 | 0.0        | 420.28 | 0.0         |
      | 2  | 31   | 31 December 2023 |               | 1650.35         | 399.49        | 20.79    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 3  | 31   | 31 January 2024  |               | 1250.8          | 399.55        | 20.73    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 4  | 29   | 29 February 2024 |               | 849.91          | 400.89        | 19.39    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 5  | 31   | 31 March 2024    |               | 438.13          | 411.78        | 8.5      | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 6  | 30   | 30 April 2024    |               | 0.0             | 438.13        | 3.59     | 0.0  | 0.0       | 441.72 | 0.0    | 0.0        | 0.0    | 441.72      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 2450.0        | 93.12    | 0.0  | 0.0       | 2543.12 | 420.28 | 0.0        | 420.28 | 2122.84     |
    And Loan saved interest model has 6 complete repayment periods
    And Loan interest is accrued through "03 March 2024"
    When Admin sets the business date to "04 March 2024"
    And Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan status will be "ACTIVE"
    And Loan has 2 "ACCRUAL" transactions on Transactions tab
    And Loan has 83.60 total Accruals
    And Loan interest is accrued through "04 March 2024"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 2450.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2450.0       | false    | false    |
      | 03 March 2024    | Accrual          | 83.04  | 0.0       | 83.04    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Repayment        | 420.28 | 400.16    | 20.12    | 0.0  | 0.0       | 2049.84      | false    | false    |
      | 04 March 2024    | Accrual          | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 31 October 2023  |               | 2450.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 30 November 2023 | 03 March 2024 | 2049.84         | 400.16        | 20.12    | 0.0  | 0.0       | 420.28 | 420.28 | 0.0        | 420.28 | 0.0         |
      | 2  | 31   | 31 December 2023 |               | 1650.35         | 399.49        | 20.79    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 3  | 31   | 31 January 2024  |               | 1250.8          | 399.55        | 20.73    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 4  | 29   | 29 February 2024 |               | 849.91          | 400.89        | 19.39    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 5  | 31   | 31 March 2024    |               | 438.46          | 411.45        | 8.83     | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 6  | 30   | 30 April 2024    |               | 0.0             | 438.46        | 3.59     | 0.0  | 0.0       | 442.05 | 0.0    | 0.0        | 0.0    | 442.05      |

  @TestRailId:C119606
  Scenario: Pay-off on the historical accrual date closes an unpaid backdated month-end loan after overdue recalculation
    Given Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin remembers the current loan as "backdated month-end loan"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan saved interest model has overdue corrections through "30 April 2024"
    When Admin sets the business date to "03 March 2024"
    And Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan has 1 "ACCRUAL" transactions on Transactions tab
    When Loan Pay-off is made on "03 March 2024"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 1 "ACCRUAL" transactions on Transactions tab
    And Loan has 83.04 total Accruals
    And Loan interest is accrued through "03 March 2024"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 2450.0  | 0.0       | 0.0      | 0.0  | 0.0       | 2450.0       | false    | false    |
      | 03 March 2024    | Accrual          | 83.04   | 0.0       | 83.04    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Repayment        | 2533.04 | 2450.0    | 83.04    | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 31 October 2023  |               | 2450.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 30 November 2023 | 03 March 2024 | 2049.84         | 400.16        | 20.12    | 0.0  | 0.0       | 420.28 | 420.28 | 0.0        | 420.28 | 0.0         |
      | 2  | 31   | 31 December 2023 | 03 March 2024 | 1650.35         | 399.49        | 20.79    | 0.0  | 0.0       | 420.28 | 420.28 | 0.0        | 420.28 | 0.0         |
      | 3  | 31   | 31 January 2024  | 03 March 2024 | 1250.8          | 399.55        | 20.73    | 0.0  | 0.0       | 420.28 | 420.28 | 0.0        | 420.28 | 0.0         |
      | 4  | 29   | 29 February 2024 | 03 March 2024 | 849.91          | 400.89        | 19.39    | 0.0  | 0.0       | 420.28 | 420.28 | 0.0        | 420.28 | 0.0         |
      | 5  | 31   | 31 March 2024    | 03 March 2024 | 431.64          | 418.27        | 2.01     | 0.0  | 0.0       | 420.28 | 420.28 | 420.28     | 0.0    | 0.0         |
      | 6  | 30   | 30 April 2024    | 03 March 2024 | 0.0             | 431.64        | 0.0      | 0.0  | 0.0       | 431.64 | 431.64 | 431.64     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late    | Outstanding |
      | 2450.0        | 83.04    | 0.0  | 0.0       | 2533.04 | 2533.04 | 851.92     | 1681.12 | 0.0         |

  @TestRailId:C119607
  Scenario: Inline COB on a historical business date posts the periodic accrual of an unpaid backdated month-end loan
    Given Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin remembers the current loan as "backdated month-end loan"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan saved interest model has overdue corrections through "30 April 2024"
    When Admin sets the business date to "04 March 2024"
    And Admin remembers the loan saved interest model
    And Admin runs inline COB job for Loan
    Then Loan status will be "ACTIVE"
    And Loan principal outstanding is "2450"
    And Loan has 1 "ACCRUAL" transactions on Transactions tab
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 2450.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2450.0       | false    | false    |
      | 03 March 2024    | Accrual          | 83.04  | 0.0       | 83.04    | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 83.04 |        |
      | INCOME | 404000       | Interest Income         |       | 83.04  |
    And Loan interest is accrued through "03 March 2024"
    And Loan saved interest model is unchanged
    And Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 October 2023  |           | 2450.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 30 November 2023 |           | 2049.84         | 400.16        | 20.12    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 2  | 31   | 31 December 2023 |           | 1650.35         | 399.49        | 20.79    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 3  | 31   | 31 January 2024  |           | 1250.8          | 399.55        | 20.73    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 4  | 29   | 29 February 2024 |           | 849.91          | 400.89        | 19.39    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 5  | 31   | 31 March 2024    |           | 450.36          | 399.55        | 20.73    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 6  | 30   | 30 April 2024    |           | 0.0             | 450.36        | 20.06    | 0.0  | 0.0       | 470.42 | 0.0  | 0.0        | 0.0  | 470.42      |

  @TestRailId:C119608
  Scenario: Backdated repayment on an unpaid backdated month-end loan replays and accrues at maturity while the business date stays after maturity
    Given Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin remembers the current loan as "backdated month-end loan"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan saved interest model has overdue corrections through "30 April 2024"
    When Customer makes "AUTOPAY" repayment on "03 March 2024" with 420.28 EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan principal outstanding is "2049.84"
    And Loan saved interest model has 6 complete repayment periods
    And Loan saved interest model has overdue corrections through "30 April 2024"
    And Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 31 October 2023  |               | 2450.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 30 November 2023 | 03 March 2024 | 2049.84         | 400.16        | 20.12    | 0.0  | 0.0       | 420.28 | 420.28 | 0.0        | 420.28 | 0.0         |
      | 2  | 31   | 31 December 2023 |               | 1650.35         | 399.49        | 20.79    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 3  | 31   | 31 January 2024  |               | 1250.8          | 399.55        | 20.73    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 4  | 29   | 29 February 2024 |               | 849.91          | 400.89        | 19.39    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 5  | 31   | 31 March 2024    |               | 447.3           | 402.61        | 17.67    | 0.0  | 0.0       | 420.28 | 0.0    | 0.0        | 0.0    | 420.28      |
      | 6  | 30   | 30 April 2024    |               | 0.0             | 447.3         | 16.79    | 0.0  | 0.0       | 464.09 | 0.0    | 0.0        | 0.0    | 464.09      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 2450.0        | 115.49   | 0.0  | 0.0       | 2565.49 | 420.28 | 0.0        | 420.28 | 2145.21     |
    When Admin successfully runs the "ADD_PERIODIC_ACCRUAL_TRANSACTIONS" job
    Then Loan status will be "ACTIVE"
    And Loan has 1 "ACCRUAL" transactions on Transactions tab
    And Loan has 115.49 total Accruals
    And Loan interest is accrued through "30 April 2024"
    And Loan has no "Accrual" transaction with transaction date later than "30 April 2024", including reversed transactions
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 2450.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2450.0       | false    | false    |
      | 03 March 2024    | Repayment        | 420.28 | 400.16    | 20.12    | 0.0  | 0.0       | 2049.84      | false    | false    |
      | 30 April 2024    | Accrual          | 115.49 | 0.0       | 115.49   | 0.0  | 0.0       | 0.0          | false    | false    |
