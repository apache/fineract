@LoanMigration
Feature: Loan Migration

  @TestRailId:C3591
  Scenario: Verify backdated loan migration with transactions and single COB execution
    When Admin sets the business date to "07 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "10000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 7537.19         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 2  | 28   | 01 March 2025    |           | 5074.38         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 3  | 31   | 01 April 2025    |           | 2611.57         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 2611.57       | 40.89    | 0.0  | 0.0       | 2652.46 | 0.0  | 0.0        | 0.0  | 2652.46     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 340.89   | 0    | 0         | 10340.89 | 0.0  | 0.0        | 0.0  | 10340.89    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
    # Add backdated late payment fee (simulating a migrated charge)
    And Admin adds "LOAN_NSF_FEE" due date charge with "10 February 2025" due date and 50 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 February 2025 | Flat             | 50.0 | 0.0  | 0.0    | 50.0        |
    # Make backdated partial repayment
    And Customer makes "AUTOPAY" repayment on "15 February 2025" with 2500 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 |           | 7537.19         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 2500.0 | 0.0        | 2500.0 | 62.81       |
      | 2  | 28   | 01 March 2025    |           | 5062.38         | 2474.81       | 88.0     | 0.0  | 50.0      | 2612.81 | 0.0    | 0.0        | 0.0    | 2612.81     |
      | 3  | 31   | 01 April 2025    |           | 2575.57         | 2486.81       | 76.0     | 0.0  | 0.0       | 2562.81 | 0.0    | 0.0        | 0.0    | 2562.81     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 2575.57       | 35.8     | 0.0  | 0.0       | 2611.37 | 0.0    | 0.0        | 0.0    | 2611.37     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late   | Outstanding |
      | 10000.0       | 299.8    | 0.0  | 50.0      | 10349.80 | 2500.0 | 0.0        | 2500.0 | 7849.8      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
      | 15 February 2025 | Repayment        | 2500.0  | 2400.0    | 100.0    | 0.0  | 0.0       | 7600.0       | false    | false    |
    # Make backdated full repayment for the second installment
    And Customer makes "AUTOPAY" repayment on "15 March 2025" with 2612.81 EUR transaction amount
    # Verify loan transactions before COB
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late    | Outstanding |
      |    |      | 01 January 2025  |               | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0     |            |         |             |
      | 1  | 31   | 01 February 2025 | 15 March 2025 | 7537.19         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 2562.81 | 0.0        | 2562.81 | 0.0         |
      | 2  | 28   | 01 March 2025    |               | 5062.38         | 2474.81       | 88.0     | 0.0  | 50.0      | 2612.81 | 2550.0  | 0.0        | 2550.0  | 62.81       |
      | 3  | 31   | 01 April 2025    |               | 2561.72         | 2500.66       | 62.15    | 0.0  | 0.0       | 2562.81 | 0.0     | 0.0        | 0.0     | 2562.81     |
      | 4  | 30   | 01 May 2025      |               | 0.0             | 2561.72       | 30.64    | 0.0  | 0.0       | 2592.36 | 0.0     | 0.0        | 0.0     | 2592.36     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late    | Outstanding |
      | 10000.0       | 280.79   | 0.0  | 50.0      | 10330.79 | 5112.81 | 0.0        | 5112.81 | 5217.98     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
      | 15 February 2025 | Repayment        | 2500.0  | 2400.0    | 100.0    | 0.0  | 0.0       | 7600.0       | false    | false    |
      | 15 March 2025    | Repayment        | 2612.81 | 2524.81   | 88.0     | 0.0  | 0.0       | 5075.19      | false    | false    |
    When Admin runs inline COB job for Loan
    # Verify accrual entries are created correctly
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late    | Outstanding |
      |    |      | 01 January 2025  |               | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0     |            |         |             |
      | 1  | 31   | 01 February 2025 | 15 March 2025 | 7537.19         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 2562.81 | 0.0        | 2562.81 | 0.0         |
      | 2  | 28   | 01 March 2025    |               | 5062.38         | 2474.81       | 88.0     | 0.0  | 50.0      | 2612.81 | 2550.0  | 0.0        | 2550.0  | 62.81       |
      | 3  | 31   | 01 April 2025    |               | 2561.72         | 2500.66       | 62.15    | 0.0  | 0.0       | 2562.81 | 0.0     | 0.0        | 0.0     | 2562.81     |
      | 4  | 30   | 01 May 2025      |               | 0.0             | 2561.72       | 30.64    | 0.0  | 0.0       | 2592.36 | 0.0     | 0.0        | 0.0     | 2592.36     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late    | Outstanding |
      | 10000.0       | 280.79   | 0.0  | 50.0      | 10330.79 | 5112.81 | 0.0        | 5112.81 | 5217.98     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
      | 01 February 2025 | Accrual Activity | 100.0   | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Repayment        | 2500.0  | 2400.0    | 100.0    | 0.0  | 0.0       | 7600.0       | false    | false    |
      | 01 March 2025    | Accrual Activity | 138.0   | 0.0       | 88.0     | 0.0  | 50.0      | 0.0          | false    | false    |
      | 15 March 2025    | Repayment        | 2612.81 | 2524.81   | 88.0     | 0.0  | 0.0       | 5075.19      | false    | false    |
      | 01 April 2025    | Accrual Activity | 62.15   | 0.0       | 62.15    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 308.61  | 0.0       | 258.61   | 0.0  | 50.0      | 0.0          | false    | false    |
    # Verify loan charges are correctly recognized
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 February 2025 | Flat             | 50.0 | 0.0  | 0.0    | 50.0        |
    # Verify the loan is correctly marked as delinquent (overdue)
    Then Loan has 2625.62 total overdue amount
    # Verify last COB date is recorded
    Then Admin checks that last closed business date of loan is "06 April 2025"
    # Set business date forward two day to verify daily COB works correctly after migration
    When Admin sets the business date to "08 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late    | Outstanding |
      |    |      | 01 January 2025  |               | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0     |            |         |             |
      | 1  | 31   | 01 February 2025 | 15 March 2025 | 7537.19         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 2562.81 | 0.0        | 2562.81 | 0.0         |
      | 2  | 28   | 01 March 2025    |               | 5062.38         | 2474.81       | 88.0     | 0.0  | 50.0      | 2612.81 | 2550.0  | 0.0        | 2550.0  | 62.81       |
      | 3  | 31   | 01 April 2025    |               | 2561.72         | 2500.66       | 62.15    | 0.0  | 0.0       | 2562.81 | 0.0     | 0.0        | 0.0     | 2562.81     |
      | 4  | 30   | 01 May 2025      |               | 0.0             | 2561.72       | 31.48    | 0.0  | 0.0       | 2593.2  | 0.0     | 0.0        | 0.0     | 2593.2      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late    | Outstanding |
      | 10000.0       | 281.63   | 0.0  | 50.0      | 10331.63 | 5112.81 | 0.0        | 5112.81 | 5218.82     |
    # Verify new accrual entry is created for the additional day
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
      | 01 February 2025 | Accrual Activity | 100.0   | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Repayment        | 2500.0  | 2400.0    | 100.0    | 0.0  | 0.0       | 7600.0       | false    | false    |
      | 01 March 2025    | Accrual Activity | 138.0   | 0.0       | 88.0     | 0.0  | 50.0      | 0.0          | false    | false    |
      | 15 March 2025    | Repayment        | 2612.81 | 2524.81   | 88.0     | 0.0  | 0.0       | 5075.19      | false    | false    |
      | 01 April 2025    | Accrual Activity | 62.15   | 0.0       | 62.15    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 308.61  | 0.0       | 258.61   | 0.0  | 50.0      | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 1.69    | 0.0       | 1.69     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3592
  Scenario: Verify backdated loan with progressive repayment and accrual calculations
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    # Create, approve and disburse backdated loan - January 1, 2025 with 3-month term
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "100" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 28   | 01 March 2025    |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Admin successfully disburse the loan on "01 January 2025" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 28   | 01 March 2025    |           | 33.72           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 33.72         | 0.58     | 0.0  | 0.0       | 34.3  | 0.0  | 0.0        | 0.0  | 34.3        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.74     | 0.0  | 0.0       | 101.74 | 0.0  | 0.0        | 0.0  | 101.74      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    # Make first repayment backdated to Feb 1, 2025
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 33.72 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 33.72 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0   | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2025    |                  | 0.0             | 33.53         | 0.39     | 0.0  | 0.0       | 33.92 | 0.0   | 0.0        | 0.0  | 33.92       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.36     | 0.0  | 0.0       | 101.36 | 33.72 | 0.0        | 0.0  | 67.64       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2025 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        |
    # Make second repayment backdated to March 1, 2025
    And Customer makes "AUTOPAY" repayment on "01 March 2025" with 33.72 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 33.72 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 01 March 2025    | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 33.72 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    |                  | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0   | 0.0        | 0.0  | 33.73       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0    | 0         | 101.17 | 67.44 | 0.0        | 0.0  | 33.73       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2025 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        |
      | 01 March 2025    | Repayment        | 33.72  | 33.33     | 0.39     | 0.0  | 0.0       | 33.53        |
    # Run inline COB to generate accrual transactions
    When Admin sets the business date to "10 April 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 33.72 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 01 March 2025    | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 33.72 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    |                  | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0   | 0.0        | 0.0  | 33.73       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0    | 0         | 101.17 | 67.44 | 0.0        | 0.0  | 33.73       |
    # Verify that accrual transactions are created
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2025 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        |
      | 01 February 2025 | Accrual Activity | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          |
      | 01 March 2025    | Repayment        | 33.72  | 33.33     | 0.39     | 0.0  | 0.0       | 33.53        |
      | 01 March 2025    | Accrual Activity | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          |
      | 01 April 2025    | Accrual          | 1.17   | 0.0       | 1.17     | 0.0  | 0.0       | 0.0          |
      | 01 April 2025    | Accrual Activity | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          |
    # Verify the loan is correctly marked as delinquent (overdue) because last installment is due
    Then Loan has 33.73 total overdue amount
    # Verify last COB date is recorded
    Then Admin checks that last closed business date of loan is "09 April 2025"

  @TestRailId:C3593
  Scenario: Verify backdated loan migration with single disbursement and final COB execution
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    # Create, approve and disburse backdated loan
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "5000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "5000" EUR transaction amount
    # Verify initial loan schedule (should show overdue periods)
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 5000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 3768.59         | 1231.41       | 50.0     | 0.0  | 0.0       | 1281.41 | 0.0  | 0.0        | 0.0  | 1281.41     |
      | 2  | 28   | 01 March 2025    |           | 2537.18         | 1231.41       | 50.0     | 0.0  | 0.0       | 1281.41 | 0.0  | 0.0        | 0.0  | 1281.41     |
      | 3  | 31   | 01 April 2025    |           | 1305.77         | 1231.41       | 50.0     | 0.0  | 0.0       | 1281.41 | 0.0  | 0.0        | 0.0  | 1281.41     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 1305.77       | 24.14    | 0.0  | 0.0       | 1329.91 | 0.0  | 0.0        | 0.0  | 1329.91     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000.0        | 174.14   | 0.0  | 0.0       | 5174.14 | 0.0  | 0.0        | 0.0  | 5174.14     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       |
    # Run single COB (inline COB as in migration final day)
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 5000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 3768.59         | 1231.41       | 50.0     | 0.0  | 0.0       | 1281.41 | 0.0  | 0.0        | 0.0  | 1281.41     |
      | 2  | 28   | 01 March 2025    |           | 2537.18         | 1231.41       | 50.0     | 0.0  | 0.0       | 1281.41 | 0.0  | 0.0        | 0.0  | 1281.41     |
      | 3  | 31   | 01 April 2025    |           | 1305.77         | 1231.41       | 50.0     | 0.0  | 0.0       | 1281.41 | 0.0  | 0.0        | 0.0  | 1281.41     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 1305.77       | 24.14    | 0.0  | 0.0       | 1329.91 | 0.0  | 0.0        | 0.0  | 1329.91     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000.0        | 174.14   | 0.0  | 0.0       | 5174.14 | 0.0  | 0.0        | 0.0  | 5174.14     |
     # Verify accrual entries are created correctly
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
      | 01 February 2025 | Accrual Activity | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual Activity | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 163.33 | 0.0       | 163.33   | 0.0  | 0.0       | 0.0          | false    | false    |
    # Verify the loan is correctly marked as delinquent (overdue)
    Then Loan has 3844.23 total overdue amount
    And Admin checks that last closed business date of loan is "09 April 2025"
    # Set business date forward one day to verify daily COB works correctly after migration
    When Admin sets the business date to "11 April 2025"
    And Admin runs inline COB job for Loan
    # Verify new accrual entry is created for the additional day
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
      | 01 February 2025 | Accrual Activity | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual Activity | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 163.33 | 0.0       | 163.33   | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 1.67   | 0.0       | 1.67     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3594
  Scenario: Verify backdated loan migration with late payments and final COB execution
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
  # Create, approve and disburse backdated loan
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 February 2025  | 3000           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2025" with "3000" amount and expected disbursement date on "01 February 2025"
    And Admin successfully disburse the loan on "01 February 2025" with "3000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2025 |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 01 March 2025    |           | 2510.31         | 489.69        | 24.98    | 0.0  | 0.0       | 514.67 | 0.0  | 0.0        | 0.0  | 514.67      |
      | 2  | 31   | 01 April 2025    |           | 2020.62         | 489.69        | 24.98    | 0.0  | 0.0       | 514.67 | 0.0  | 0.0        | 0.0  | 514.67      |
      | 3  | 30   | 01 May 2025      |           | 1525.22         | 495.4         | 19.27    | 0.0  | 0.0       | 514.67 | 0.0  | 0.0        | 0.0  | 514.67      |
      | 4  | 31   | 01 June 2025     |           | 1023.25         | 501.97        | 12.7     | 0.0  | 0.0       | 514.67 | 0.0  | 0.0        | 0.0  | 514.67      |
      | 5  | 30   | 01 July 2025     |           | 517.1           | 506.15        | 8.52     | 0.0  | 0.0       | 514.67 | 0.0  | 0.0        | 0.0  | 514.67      |
      | 6  | 31   | 01 August 2025   |           | 0.0             | 517.1         | 4.3      | 0.0  | 0.0       | 521.4  | 0.0  | 0.0        | 0.0  | 521.4       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 3000.0        | 94.75    | 0.0  | 0.0       | 3094.75 | 0.0  | 0.0        | 0.0  | 3094.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2025 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
  # Make backdated late payment for first installment
    And Customer makes "AUTOPAY" repayment on "25 March 2025" with 514.50 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 February 2025 |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 28   | 01 March 2025    |           | 2510.31         | 489.69        | 24.98    | 0.0  | 0.0       | 514.67 | 514.5 | 0.0        | 514.5 | 0.17        |
      | 2  | 31   | 01 April 2025    |           | 2019.69         | 490.62        | 24.05    | 0.0  | 0.0       | 514.67 | 0.0   | 0.0        | 0.0   | 514.67      |
      | 3  | 30   | 01 May 2025      |           | 1523.06         | 496.63        | 18.04    | 0.0  | 0.0       | 514.67 | 0.0   | 0.0        | 0.0   | 514.67      |
      | 4  | 31   | 01 June 2025     |           | 1021.07         | 501.99        | 12.68    | 0.0  | 0.0       | 514.67 | 0.0   | 0.0        | 0.0   | 514.67      |
      | 5  | 30   | 01 July 2025     |           | 514.9           | 506.17        | 8.5      | 0.0  | 0.0       | 514.67 | 0.0   | 0.0        | 0.0   | 514.67      |
      | 6  | 31   | 01 August 2025   |           | 0.0             | 514.9         | 4.29     | 0.0  | 0.0       | 519.19 | 0.0   | 0.0        | 0.0   | 519.19      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 3000.0        | 92.54    | 0.0  | 0.0       | 3092.54 | 514.5 | 0.0        | 514.5 | 2578.04     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2025 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 25 March 2025    | Repayment        | 514.5  | 489.52    | 24.98    | 0.0  | 0.0       | 2510.48      |
  # Make backdated on-time payments for subsequent installments
    And Customer makes "AUTOPAY" repayment on "01 April 2025" with 514.50 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 February 2025 |               | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 28   | 01 March 2025    | 01 April 2025 | 2510.31         | 489.69        | 24.98    | 0.0  | 0.0       | 514.67 | 514.67 | 0.0        | 514.67 | 0.0         |
      | 2  | 31   | 01 April 2025    |               | 2019.69         | 490.62        | 24.05    | 0.0  | 0.0       | 514.67 | 514.33 | 0.0        | 0.0    | 0.34        |
      | 3  | 30   | 01 May 2025      |               | 1521.83         | 497.86        | 16.81    | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 4  | 31   | 01 June 2025     |               | 1019.83         | 502.0         | 12.67    | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 5  | 30   | 01 July 2025     |               | 513.65          | 506.18        | 8.49     | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 6  | 31   | 01 August 2025   |               | 0.0             | 513.65        | 4.28     | 0.0  | 0.0       | 517.93 | 0.0    | 0.0        | 0.0    | 517.93      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 3000.0        | 91.28    | 0.0  | 0.0       | 3091.28 | 1029.0 | 0.0        | 514.67 | 2062.28     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2025 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 25 March 2025    | Repayment        | 514.5  | 489.52    | 24.98    | 0.0  | 0.0       | 2510.48      |
      | 01 April 2025    | Repayment        | 514.5  | 490.45    | 24.05    | 0.0  | 0.0       | 2020.03      |
    When Admin sets the business date to "01 May 2025"
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 514.50 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 February 2025 |               | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 28   | 01 March 2025    | 01 April 2025 | 2510.31         | 489.69        | 24.98    | 0.0  | 0.0       | 514.67 | 514.67 | 0.0        | 514.67 | 0.0         |
      | 2  | 31   | 01 April 2025    | 01 May 2025   | 2019.69         | 490.62        | 24.05    | 0.0  | 0.0       | 514.67 | 514.67 | 0.0        | 0.34   | 0.0         |
      | 3  | 30   | 01 May 2025      |               | 1521.84         | 497.85        | 16.82    | 0.0  | 0.0       | 514.67 | 514.16 | 0.0        | 0.0    | 0.51        |
      | 4  | 31   | 01 June 2025     |               | 1019.84         | 502.0         | 12.67    | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 5  | 30   | 01 July 2025     |               | 513.66          | 506.18        | 8.49     | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 6  | 31   | 01 August 2025   |               | 0.0             | 513.66        | 4.28     | 0.0  | 0.0       | 517.94 | 0.0    | 0.0        | 0.0    | 517.94      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 3000.0        | 91.29    | 0.0  | 0.0       | 3091.29 | 1543.5 | 0.0        | 515.01 | 1547.79     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2025 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 25 March 2025    | Repayment        | 514.5  | 489.52    | 24.98    | 0.0  | 0.0       | 2510.48      |
      | 01 April 2025    | Repayment        | 514.5  | 490.45    | 24.05    | 0.0  | 0.0       | 2020.03      |
      | 01 May 2025      | Repayment        | 514.5  | 497.68    | 16.82    | 0.0  | 0.0       | 1522.35      |
  # Run single COB (inline COB as in migration final day)
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 February 2025 |               | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 28   | 01 March 2025    | 01 April 2025 | 2510.31         | 489.69        | 24.98    | 0.0  | 0.0       | 514.67 | 514.67 | 0.0        | 514.67 | 0.0         |
      | 2  | 31   | 01 April 2025    | 01 May 2025   | 2019.69         | 490.62        | 24.05    | 0.0  | 0.0       | 514.67 | 514.67 | 0.0        | 0.34   | 0.0         |
      | 3  | 30   | 01 May 2025      |               | 1521.84         | 497.85        | 16.82    | 0.0  | 0.0       | 514.67 | 514.16 | 0.0        | 0.0    | 0.51        |
      | 4  | 31   | 01 June 2025     |               | 1019.84         | 502.0         | 12.67    | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 5  | 30   | 01 July 2025     |               | 513.66          | 506.18        | 8.49     | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 6  | 31   | 01 August 2025   |               | 0.0             | 513.66        | 4.28     | 0.0  | 0.0       | 517.94 | 0.0    | 0.0        | 0.0    | 517.94      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 3000.0        | 91.29    | 0.0  | 0.0       | 3091.29 | 1543.5 | 0.0        | 515.01 | 1547.79     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 February 2025 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       | false    | false    |
      | 01 March 2025    | Accrual Activity | 24.98  | 0.0       | 24.98    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Repayment        | 514.5  | 489.52    | 24.98    | 0.0  | 0.0       | 2510.48      | false    | false    |
      | 01 April 2025    | Repayment        | 514.5  | 490.45    | 24.05    | 0.0  | 0.0       | 2020.03      | false    | false    |
      | 01 April 2025    | Accrual Activity | 24.05  | 0.0       | 24.05    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual          | 65.29  | 0.0       | 65.29    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Repayment        | 514.5  | 497.68    | 16.82    | 0.0  | 0.0       | 1522.35      | false    | false    |
  # Verify loan status and last closed business date
    Then Admin checks that last closed business date of loan is "30 April 2025"
    And Loan status will be "ACTIVE"
  # Set business date forward one day to verify daily COB works correctly after migration
    When Admin sets the business date to "02 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 February 2025 |               | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 28   | 01 March 2025    | 01 April 2025 | 2510.31         | 489.69        | 24.98    | 0.0  | 0.0       | 514.67 | 514.67 | 0.0        | 514.67 | 0.0         |
      | 2  | 31   | 01 April 2025    | 01 May 2025   | 2019.69         | 490.62        | 24.05    | 0.0  | 0.0       | 514.67 | 514.67 | 0.0        | 0.34   | 0.0         |
      | 3  | 30   | 01 May 2025      |               | 1521.84         | 497.85        | 16.82    | 0.0  | 0.0       | 514.67 | 514.16 | 0.0        | 0.0    | 0.51        |
      | 4  | 31   | 01 June 2025     |               | 1019.84         | 502.0         | 12.67    | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 5  | 30   | 01 July 2025     |               | 513.66          | 506.18        | 8.49     | 0.0  | 0.0       | 514.67 | 0.0    | 0.0        | 0.0    | 514.67      |
      | 6  | 31   | 01 August 2025   |               | 0.0             | 513.66        | 4.28     | 0.0  | 0.0       | 517.94 | 0.0    | 0.0        | 0.0    | 517.94      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 3000.0        | 91.29    | 0.0  | 0.0       | 3091.29 | 1543.5 | 0.0        | 515.01 | 1547.79     |
  # Verify new accrual entry is created for the additional day
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 February 2025 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       | false    | false    |
      | 01 March 2025    | Accrual Activity | 24.98  | 0.0       | 24.98    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Repayment        | 514.5  | 489.52    | 24.98    | 0.0  | 0.0       | 2510.48      | false    | false    |
      | 01 April 2025    | Repayment        | 514.5  | 490.45    | 24.05    | 0.0  | 0.0       | 2020.03      | false    | false    |
      | 01 April 2025    | Accrual Activity | 24.05  | 0.0       | 24.05    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual          | 65.29  | 0.0       | 65.29    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Repayment        | 514.5  | 497.68    | 16.82    | 0.0  | 0.0       | 1522.35      | false    | false    |
      | 01 May 2025      | Accrual          | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual Activity | 16.82  | 0.0       | 16.82    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3595
  Scenario: Verify backdated loan migration with early payments and final COB execution
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    # Create, approve and disburse backdated loan
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 06 February 2025  | 2600           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "06 February 2025" with "2600" amount and expected disbursement date on "06 February 2025"
    And Admin successfully disburse the loan on "06 February 2025" with "2600" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 06 February 2025 |           | 2600.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 06 March 2025    |           | 2175.59         | 424.41        | 21.64    | 0.0  | 0.0       | 446.05 | 0.0  | 0.0        | 0.0  | 446.05      |
      | 2  | 31   | 06 April 2025    |           | 1751.18         | 424.41        | 21.64    | 0.0  | 0.0       | 446.05 | 0.0  | 0.0        | 0.0  | 446.05      |
      | 3  | 30   | 06 May 2025      |           | 1320.65         | 430.53        | 15.52    | 0.0  | 0.0       | 446.05 | 0.0  | 0.0        | 0.0  | 446.05      |
      | 4  | 31   | 06 June 2025     |           | 885.59          | 435.06        | 10.99    | 0.0  | 0.0       | 446.05 | 0.0  | 0.0        | 0.0  | 446.05      |
      | 5  | 30   | 06 July 2025     |           | 446.91          | 438.68        | 7.37     | 0.0  | 0.0       | 446.05 | 0.0  | 0.0        | 0.0  | 446.05      |
      | 6  | 31   | 06 August 2025   |           | 0.0             | 446.91        | 3.72     | 0.0  | 0.0       | 450.63 | 0.0  | 0.0        | 0.0  | 450.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2600.0        | 80.88    | 0.0  | 0.0       | 2680.88 | 0.0  | 0.0        | 0.0  | 2680.88     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 06 February 2025 | Disbursement     | 2600.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2600.0       |
    # Make backdated regular payments for first few installments
    And Customer makes "AUTOPAY" repayment on "06 March 2025" with 445.81 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 06 February 2025 |           | 2600.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 28   | 06 March 2025    |           | 2175.59         | 424.41        | 21.64    | 0.0  | 0.0       | 446.05 | 445.81 | 0.0        | 0.0  | 0.24        |
      | 2  | 31   | 06 April 2025    |           | 1747.65         | 427.94        | 18.11    | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0  | 446.05      |
      | 3  | 30   | 06 May 2025      |           | 1316.62         | 431.03        | 15.02    | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0  | 446.05      |
      | 4  | 31   | 06 June 2025     |           | 881.53          | 435.09        | 10.96    | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0  | 446.05      |
      | 5  | 30   | 06 July 2025     |           | 442.82          | 438.71        | 7.34     | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0  | 446.05      |
      | 6  | 31   | 06 August 2025   |           | 0.0             | 442.82        | 3.69     | 0.0  | 0.0       | 446.51 | 0.0    | 0.0        | 0.0  | 446.51      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 2600.0        | 76.76    | 0.0  | 0.0       | 2676.76 | 445.81 | 0.0        | 0.0  | 2230.95     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 06 February 2025 | Disbursement     | 2600.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2600.0       |
      | 06 March 2025    | Repayment        | 445.81 | 424.17    | 21.64    | 0.0  | 0.0       | 2175.83      |
    And Customer makes "AUTOPAY" repayment on "06 April 2025" with 445.81 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 06 February 2025 |               | 2600.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 28   | 06 March 2025    | 06 April 2025 | 2175.59         | 424.41        | 21.64    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 0.24 | 0.0         |
      | 2  | 31   | 06 April 2025    |               | 1747.65         | 427.94        | 18.11    | 0.0  | 0.0       | 446.05 | 445.57 | 0.0        | 0.0  | 0.48        |
      | 3  | 30   | 06 May 2025      |               | 1316.15         | 431.5         | 14.55    | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0  | 446.05      |
      | 4  | 31   | 06 June 2025     |               | 881.06          | 435.09        | 10.96    | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0  | 446.05      |
      | 5  | 30   | 06 July 2025     |               | 442.34          | 438.72        | 7.33     | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0  | 446.05      |
      | 6  | 31   | 06 August 2025   |               | 0.0             | 442.34        | 3.68     | 0.0  | 0.0       | 446.02 | 0.0    | 0.0        | 0.0  | 446.02      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 2600.0        | 76.27    | 0.0  | 0.0       | 2676.27 | 891.62 | 0.0        | 0.24 | 1784.65     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 06 February 2025 | Disbursement     | 2600.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2600.0       |
      | 06 March 2025    | Repayment        | 445.81 | 424.17    | 21.64    | 0.0  | 0.0       | 2175.83      |
      | 06 April 2025    | Repayment        | 445.81 | 427.7     | 18.11    | 0.0  | 0.0       | 1748.13      |
    # Make backdated advance/early payment covering two installments
    When Admin sets the business date to "06 June 2025"
    And Customer makes "AUTOPAY" repayment on "06 June 2025" with 891.62 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 06 February 2025 |               | 2600.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 28   | 06 March 2025    | 06 April 2025 | 2175.59         | 424.41        | 21.64    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 0.24   | 0.0         |
      | 2  | 31   | 06 April 2025    | 06 June 2025  | 1747.65         | 427.94        | 18.11    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 0.48   | 0.0         |
      | 3  | 30   | 06 May 2025      | 06 June 2025  | 1316.15         | 431.5         | 14.55    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 446.05 | 0.0         |
      | 4  | 31   | 06 June 2025     |               | 884.65          | 431.5         | 14.55    | 0.0  | 0.0       | 446.05 | 445.09 | 0.0        | 0.0    | 0.96        |
      | 5  | 30   | 06 July 2025     |               | 445.96          | 438.69        | 7.36     | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0    | 446.05      |
      | 6  | 31   | 06 August 2025   |               | 0.0             | 445.96        | 3.71     | 0.0  | 0.0       | 449.67 | 0.0    | 0.0        | 0.0    | 449.67      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 2600.0        | 79.92    | 0.0  | 0.0       | 2679.92 | 1783.24 | 0.0        | 446.77 | 896.68      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 06 February 2025 | Disbursement     | 2600.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2600.0       |
      | 06 March 2025    | Repayment        | 445.81 | 424.17    | 21.64    | 0.0  | 0.0       | 2175.83      |
      | 06 April 2025    | Repayment        | 445.81 | 427.7     | 18.11    | 0.0  | 0.0       | 1748.13      |
      | 06 June 2025     | Repayment        | 891.62 | 862.52    | 29.1     | 0.0  | 0.0       | 885.61       |
    # Run single COB (inline COB as in migration final day)
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 06 February 2025 |               | 2600.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 28   | 06 March 2025    | 06 April 2025 | 2175.59         | 424.41        | 21.64    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 0.24   | 0.0         |
      | 2  | 31   | 06 April 2025    | 06 June 2025  | 1747.65         | 427.94        | 18.11    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 0.48   | 0.0         |
      | 3  | 30   | 06 May 2025      | 06 June 2025  | 1316.15         | 431.5         | 14.55    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 446.05 | 0.0         |
      | 4  | 31   | 06 June 2025     |               | 884.65          | 431.5         | 14.55    | 0.0  | 0.0       | 446.05 | 445.09 | 0.0        | 0.0    | 0.96        |
      | 5  | 30   | 06 July 2025     |               | 445.96          | 438.69        | 7.36     | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0    | 446.05      |
      | 6  | 31   | 06 August 2025   |               | 0.0             | 445.96        | 3.71     | 0.0  | 0.0       | 449.67 | 0.0    | 0.0        | 0.0    | 449.67      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 2600.0        | 79.92    | 0.0  | 0.0       | 2679.92 | 1783.24 | 0.0        | 446.77 | 896.68      |
    # Verify accrual entries are created correctly
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 06 February 2025 | Disbursement     | 2600.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2600.0       | false    | false    |
      | 06 March 2025    | Repayment        | 445.81 | 424.17    | 21.64    | 0.0  | 0.0       | 2175.83      | false    | false    |
      | 06 March 2025    | Accrual Activity | 21.64  | 0.0       | 21.64    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Repayment        | 445.81 | 427.7     | 18.11    | 0.0  | 0.0       | 1748.13      | false    | false    |
      | 06 April 2025    | Accrual Activity | 18.11  | 0.0       | 18.11    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual Activity | 14.55  | 0.0       | 14.55    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 June 2025     | Accrual          | 68.38  | 0.0       | 68.38    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 June 2025     | Repayment        | 891.62 | 862.52    | 29.1     | 0.0  | 0.0       | 885.61       | false    | false    |
    # Verify loan status and last closed business date
    Then Admin checks that last closed business date of loan is "05 June 2025"
    And Loan status will be "ACTIVE"
    And Loan has 0.0 total overdue amount
    # Set business date forward one day to verify daily COB works correctly after migration
    When Admin sets the business date to "07 June 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 06 February 2025 |               | 2600.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 28   | 06 March 2025    | 06 April 2025 | 2175.59         | 424.41        | 21.64    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 0.24   | 0.0         |
      | 2  | 31   | 06 April 2025    | 06 June 2025  | 1747.65         | 427.94        | 18.11    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 0.48   | 0.0         |
      | 3  | 30   | 06 May 2025      | 06 June 2025  | 1316.15         | 431.5         | 14.55    | 0.0  | 0.0       | 446.05 | 446.05 | 0.0        | 446.05 | 0.0         |
      | 4  | 31   | 06 June 2025     |               | 884.65          | 431.5         | 14.55    | 0.0  | 0.0       | 446.05 | 445.09 | 0.0        | 0.0    | 0.96        |
      | 5  | 30   | 06 July 2025     |               | 445.96          | 438.69        | 7.36     | 0.0  | 0.0       | 446.05 | 0.0    | 0.0        | 0.0    | 446.05      |
      | 6  | 31   | 06 August 2025   |               | 0.0             | 445.96        | 3.71     | 0.0  | 0.0       | 449.67 | 0.0    | 0.0        | 0.0    | 449.67      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 2600.0        | 79.92    | 0.0  | 0.0       | 2679.92 | 1783.24 | 0.0        | 446.77 | 896.68      |
    # Verify new accrual entry is created for the additional day
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 06 February 2025 | Disbursement     | 2600.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2600.0       | false    | false    |
      | 06 March 2025    | Repayment        | 445.81 | 424.17    | 21.64    | 0.0  | 0.0       | 2175.83      | false    | false    |
      | 06 March 2025    | Accrual Activity | 21.64  | 0.0       | 21.64    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Repayment        | 445.81 | 427.7     | 18.11    | 0.0  | 0.0       | 1748.13      | false    | false    |
      | 06 April 2025    | Accrual Activity | 18.11  | 0.0       | 18.11    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual Activity | 14.55  | 0.0       | 14.55    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 June 2025     | Accrual          | 68.38  | 0.0       | 68.38    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 June 2025     | Repayment        | 891.62 | 862.52    | 29.1     | 0.0  | 0.0       | 885.61       | false    | false    |
      | 06 June 2025     | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 June 2025     | Accrual Activity | 14.55  | 0.0       | 14.55    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3596
  Scenario: Verify backdated loan migration with month-end dates
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    # Create, approve and disburse backdated loan on month-end date
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 31 January 2025   | 4000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 January 2025" with "4000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "4000" EUR transaction amount
    # Verify initial loan schedule handles month-end dates correctly
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 4000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 3014.88         | 985.12        | 40.0     | 0.0  | 0.0       | 1025.12 | 0.0  | 0.0        | 0.0  | 1025.12     |
      | 2  | 31   | 31 March 2025    |           | 2029.76         | 985.12        | 40.0     | 0.0  | 0.0       | 1025.12 | 0.0  | 0.0        | 0.0  | 1025.12     |
      | 3  | 30   | 30 April 2025    |           | 1031.51         | 998.25        | 26.87    | 0.0  | 0.0       | 1025.12 | 0.0  | 0.0        | 0.0  | 1025.12     |
      | 4  | 31   | 31 May 2025      |           | 0.0             | 1031.51       | 10.32    | 0.0  | 0.0       | 1041.83 | 0.0  | 0.0        | 0.0  | 1041.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 4000.0        | 117.19   | 0.0  | 0.0       | 4117.19 | 0.0  | 0.0        | 0.0  | 4117.19     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 January 2025  | Disbursement     | 4000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 4000.0       | false    | false    |
    # Make backdated payment on a non-month-end date
    And Customer makes "AUTOPAY" repayment on "15 March 2025" with 1025.12 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late    | Outstanding |
      |    |      | 31 January 2025  |               | 4000.0          |               |          | 0.0  |           | 0.0     | 0.0     |            |         |             |
      | 1  | 28   | 28 February 2025 | 15 March 2025 | 3014.88         | 985.12        | 40.0     | 0.0  | 0.0       | 1025.12 | 1025.12 | 0.0        | 1025.12 | 0.0         |
      | 2  | 31   | 31 March 2025    |               | 2024.68         | 990.2         | 34.92    | 0.0  | 0.0       | 1025.12 | 0.0     | 0.0        | 0.0     | 1025.12     |
      | 3  | 30   | 30 April 2025    |               | 1023.11         | 1001.57       | 23.55    | 0.0  | 0.0       | 1025.12 | 0.0     | 0.0        | 0.0     | 1025.12     |
      | 4  | 31   | 31 May 2025      |               | 0.0             | 1023.11       | 10.23    | 0.0  | 0.0       | 1033.34 | 0.0     | 0.0        | 0.0     | 1033.34     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late    | Outstanding |
      | 4000.0        | 108.7    | 0.0  | 0.0       | 4108.7 | 1025.12 | 0.0        | 1025.12 | 3083.58     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance |
      | 31 January 2025  | Disbursement     | 4000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 4000.0       |
      | 15 March 2025    | Repayment        | 1025.12 | 985.12    | 40.0     | 0.0  | 0.0       | 3014.88      |
    # Run single COB (inline COB as in migration final day)
    When Admin runs inline COB job for Loan
    # Verify accrual entries are created correctly
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late    | Outstanding |
      |    |      | 31 January 2025  |               | 4000.0          |               |          | 0.0  |           | 0.0     | 0.0     |            |         |             |
      | 1  | 28   | 28 February 2025 | 15 March 2025 | 3014.88         | 985.12        | 40.0     | 0.0  | 0.0       | 1025.12 | 1025.12 | 0.0        | 1025.12 | 0.0         |
      | 2  | 31   | 31 March 2025    |               | 2024.68         | 990.2         | 34.92    | 0.0  | 0.0       | 1025.12 | 0.0     | 0.0        | 0.0     | 1025.12     |
      | 3  | 30   | 30 April 2025    |               | 1023.11         | 1001.57       | 23.55    | 0.0  | 0.0       | 1025.12 | 0.0     | 0.0        | 0.0     | 1025.12     |
      | 4  | 31   | 31 May 2025      |               | 0.0             | 1023.11       | 10.23    | 0.0  | 0.0       | 1033.34 | 0.0     | 0.0        | 0.0     | 1033.34     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late    | Outstanding |
      | 4000.0        | 108.7    | 0.0  | 0.0       | 4108.7 | 1025.12 | 0.0        | 1025.12 | 3083.58     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 January 2025  | Disbursement     | 4000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 4000.0       | false    | false    |
      | 28 February 2025 | Accrual Activity | 40.0    | 0.0       | 40.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2025    | Repayment        | 1025.12 | 985.12    | 40.0     | 0.0  | 0.0       | 3014.88      | false    | false    |
      | 31 March 2025    | Accrual Activity | 34.92   | 0.0       | 34.92    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 83.96   | 0.0       | 83.96    | 0.0  | 0.0       | 0.0          | false    | false    |
    # Verify loan status and last closed business date
    Then Admin checks that last closed business date of loan is "09 April 2025"
    And Loan status will be "ACTIVE"
    And Loan has 1025.12 total overdue amount

  @TestRailId:C3623
  Scenario: Verify backdated loan migration that was fully paid and closed before current date
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
  # Create, approve and disburse backdated loan
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 15 January 2025   | 1500           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "15 January 2025" with "1500" amount and expected disbursement date on "15 January 2025"
    And Admin successfully disburse the loan on "15 January 2025" with "1500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 15 January 2025  |           | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 15 February 2025 |           | 1004.97         | 495.03        | 15.0     | 0.0  | 0.0       | 510.03 | 0.0  | 0.0        | 0.0  | 510.03      |
      | 2  | 28   | 15 March 2025    |           | 509.94          | 495.03        | 15.0     | 0.0  | 0.0       | 510.03 | 0.0  | 0.0        | 0.0  | 510.03      |
      | 3  | 31   | 15 April 2025    |           | 0.0             | 509.94        | 13.4     | 0.0  | 0.0       | 523.34 | 0.0  | 0.0        | 0.0  | 523.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1500.0        | 43.4     | 0.0  | 0.0       | 1543.4 | 0.0  | 0.0        | 0.0  | 1543.4      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 15 January 2025  | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
    And Customer makes "AUTOPAY" repayment on "15 February 2025" with 510.03 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 15 January 2025  |                  | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 15 February 2025 | 15 February 2025 | 1004.97         | 495.03        | 15.0     | 0.0  | 0.0       | 510.03 | 510.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 15 March 2025    |                  | 504.99          | 499.98        | 10.05    | 0.0  | 0.0       | 510.03 | 0.0    | 0.0        | 0.0  | 510.03      |
      | 3  | 31   | 15 April 2025    |                  | 0.0             | 504.99        | 9.24     | 0.0  | 0.0       | 514.23 | 0.0    | 0.0        | 0.0  | 514.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1500.0        | 34.29    | 0.0  | 0.0       | 1534.29 | 510.03 | 0.0        | 0.0  | 1024.26     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 15 January 2025  | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
      | 15 February 2025 | Repayment        | 510.03 | 495.03    | 15.0     | 0.0  | 0.0       | 1004.97      |
   # Make second payment on time
    And Customer makes "AUTOPAY" repayment on "15 March 2025" with 510.03 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 15 January 2025  |                  | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 15 February 2025 | 15 February 2025 | 1004.97         | 495.03        | 15.0     | 0.0  | 0.0       | 510.03 | 510.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 15 March 2025    | 15 March 2025    | 504.99          | 499.98        | 10.05    | 0.0  | 0.0       | 510.03 | 510.03 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 15 April 2025    |                  | 0.0             | 504.99        | 5.05     | 0.0  | 0.0       | 510.04 | 0.0    | 0.0        | 0.0  | 510.04      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      | 1500.0        | 30.1     | 0.0  | 0.0       | 1530.1 | 1020.06 | 0.0        | 0.0  | 510.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 15 January 2025  | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
      | 15 February 2025 | Repayment        | 510.03 | 495.03    | 15.0     | 0.0  | 0.0       | 1004.97      |
      | 15 March 2025    | Repayment        | 510.03 | 499.98    | 10.05    | 0.0  | 0.0       | 504.99       |
   # Make early payment for the final installment (loan gets fully paid before current date)
    And Customer makes "AUTOPAY" repayment on "25 March 2025" with 506.62 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 15 January 2025  |                  | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 15 February 2025 | 15 February 2025 | 1004.97         | 495.03        | 15.0     | 0.0  | 0.0       | 510.03 | 510.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 15 March 2025    | 15 March 2025    | 504.99          | 499.98        | 10.05    | 0.0  | 0.0       | 510.03 | 510.03 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 15 April 2025    | 25 March 2025    | 0.0             | 504.99        | 1.63     | 0.0  | 0.0       | 506.62 | 506.62 | 506.62     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1500.0        | 26.68    | 0.0  | 0.0       | 1526.68 | 1526.68 | 506.62     | 0.0  | 0.0         |
   # Verify loan status is CLOSED and has expected accrual entries without running COB
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 15 January 2025  | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
      | 15 February 2025 | Repayment        | 510.03 | 495.03    | 15.0     | 0.0  | 0.0       | 1004.97      | false    | false    |
      | 15 February 2025 | Accrual Activity | 15.0   | 0.0       | 15.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2025    | Repayment        | 510.03 | 499.98    | 10.05    | 0.0  | 0.0       | 504.99       | false    | false    |
      | 15 March 2025    | Accrual Activity | 10.05  | 0.0       | 10.05    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Repayment        | 506.62 | 504.99    | 1.63     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Accrual Activity | 1.63   | 0.0       | 1.63     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 26.68  | 0.0       | 26.68    | 0.0  | 0.0       | 0.0          | false    | false    |
   # Verify loan has no overdue amounts and closed date is recorded
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0.0 total overdue amount

  @TestRailId:C3804
  Scenario: Verify backdated loan migration with disbursement reversal and running Loan COB afterwards
    When Admin sets the business date to "07 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "10000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 7537.19         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 2  | 28   | 01 March 2025    |           | 5074.38         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 3  | 31   | 01 April 2025    |           | 2611.57         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 2611.57       | 40.89    | 0.0  | 0.0       | 2652.46 | 0.0  | 0.0        | 0.0  | 2652.46     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 340.89   | 0    | 0         | 10340.89 | 0.0  | 0.0        | 0.0  | 10340.89    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
    When Admin runs inline COB job for Loan
    # Verify accrual entries are created correctly
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 7537.19         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 2  | 28   | 01 March 2025    |           | 5074.38         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 3  | 31   | 01 April 2025    |           | 2611.57         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 2611.57       | 40.89    | 0.0  | 0.0       | 2652.46 | 0.0  | 0.0        | 0.0  | 2652.46     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 340.89   | 0.0  | 0.0       | 10340.89 | 0.0  | 0.0        | 0.0  | 10340.89    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
      | 01 February 2025 | Accrual Activity | 100.0   | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity | 100.0   | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual Activity | 100.0   | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 316.67  | 0.0       | 316.67   | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "01 January 2025"
    When Admin successfully undo disbursal
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 7537.19         | 2462.81       | 100.0    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 2  | 28   | 01 March 2025    |           | 5049.75         | 2487.44       | 75.37    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 3  | 31   | 01 April 2025    |           | 2537.44         | 2512.31       | 50.5     | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 2537.44       | 25.37    | 0.0  | 0.0       | 2562.81 | 0.0  | 0.0        | 0.0  | 2562.81     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 251.24   | 0.0  | 0.0       | 10251.24 | 0.0  | 0.0        | 0.0  | 10251.24    |
    Then Loan Transactions tab has none transaction
    When Admin sets the business date to "02 January 2025"
    And Admin successfully disburse the loan on "02 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 02 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 7534.78         | 2465.22       | 96.77    | 0.0  | 0.0       | 2561.99 | 0.0  | 0.0        | 0.0  | 2561.99     |
      | 2  | 28   | 01 March 2025    |           | 5048.14         | 2486.64       | 75.35    | 0.0  | 0.0       | 2561.99 | 0.0  | 0.0        | 0.0  | 2561.99     |
      | 3  | 31   | 01 April 2025    |           | 2536.63         | 2511.51       | 50.48    | 0.0  | 0.0       | 2561.99 | 0.0  | 0.0        | 0.0  | 2561.99     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 2536.63       | 25.37    | 0.0  | 0.0       | 2562.0  | 0.0  | 0.0        | 0.0  | 2562.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 247.97   | 0    | 0         | 10247.97 | 0.0  | 0.0        | 0.0  | 10247.97    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 02 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
    When Admin sets the business date to "08 April 2025"
    When Admin runs inline COB job for Loan
    # Verify accrual entries are created correctly
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 02 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 7534.78         | 2465.22       | 96.77    | 0.0  | 0.0       | 2561.99 | 0.0  | 0.0        | 0.0  | 2561.99     |
      | 2  | 28   | 01 March 2025    |           | 5072.79         | 2461.99       | 100.0    | 0.0  | 0.0       | 2561.99 | 0.0  | 0.0        | 0.0  | 2561.99     |
      | 3  | 31   | 01 April 2025    |           | 2610.8          | 2461.99       | 100.0    | 0.0  | 0.0       | 2561.99 | 0.0  | 0.0        | 0.0  | 2561.99     |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 2610.8        | 43.35    | 0.0  | 0.0       | 2654.15 | 0.0  | 0.0        | 0.0  | 2654.15     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 340.12   | 0.0  | 0.0       | 10340.12 | 0.0  | 0.0        | 0.0  | 10340.12    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 02 January 2025  | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |
      | 01 February 2025 | Accrual Activity | 96.77   | 0.0       | 96.77    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity | 100.0   | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual Activity | 100.0   | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 242.6   | 0.0       | 242.6    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C4000
  Scenario: Verify backdated loan migration with multiple disbursements on same day with final COB
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 200            | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "200" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "100" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 92.85           | 7.15          | 2.75     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 2  | 28   | 01 March 2025     |           | 85.7            | 7.15          | 2.75     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 3  | 31   | 01 April 2025     |           | 78.55           | 7.15          | 2.75     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 4  | 30   | 01 May 2025       |           | 70.99           | 7.56          | 2.34     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 5  | 31   | 01 June 2025      |           | 63.04           | 7.95          | 1.95     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 6  | 30   | 01 July 2025      |           | 54.87           | 8.17          | 1.73     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 7  | 31   | 01 August 2025    |           | 46.48           | 8.39          | 1.51     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 8  | 31   | 01 September 2025 |           | 37.86           | 8.62          | 1.28     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 9  | 30   | 01 October 2025   |           | 29.0            | 8.86          | 1.04     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 10 | 31   | 01 November 2025  |           | 19.9            | 9.1           | 0.8      | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 11 | 30   | 01 December 2025  |           | 10.55           | 9.35          | 0.55     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 10.55         | 0.29     | 0.0  | 0.0       | 10.84 | 0.0  | 0.0        | 0.0  | 10.84       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 19.74    | 0.0  | 0.0       | 119.74 | 0.0  | 0.0        | 0.0  | 119.74      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Admin successfully disburse the loan on "01 January 2025" with "100" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 01 January 2025   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 185.71          | 14.29         | 5.5      | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 2  | 28   | 01 March 2025     |           | 171.42          | 14.29         | 5.5      | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 3  | 31   | 01 April 2025     |           | 157.13          | 14.29         | 5.5      | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 4  | 30   | 01 May 2025       |           | 142.01          | 15.12         | 4.67     | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 5  | 31   | 01 June 2025      |           | 126.13          | 15.88         | 3.91     | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 6  | 30   | 01 July 2025      |           | 109.81          | 16.32         | 3.47     | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 7  | 31   | 01 August 2025    |           | 93.04           | 16.77         | 3.02     | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 8  | 31   | 01 September 2025 |           | 75.81           | 17.23         | 2.56     | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 9  | 30   | 01 October 2025   |           | 58.1            | 17.71         | 2.08     | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 10 | 31   | 01 November 2025  |           | 39.91           | 18.19         | 1.6      | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 11 | 30   | 01 December 2025  |           | 21.22           | 18.69         | 1.1      | 0.0  | 0.0       | 19.79 | 0.0  | 0.0        | 0.0  | 19.79       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 21.22         | 0.58     | 0.0  | 0.0       | 21.8  | 0.0  | 0.0        | 0.0  | 21.8        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 39.49    | 0.0  | 0.0       | 239.49 | 0.0  | 0.0        | 0.0  | 239.49      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    And Customer makes "AUTOPAY" repayment on "15 January 2025" with 19.79 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2025   |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 15 January 2025 | 182.69          | 17.31         | 2.48     | 0.0  | 0.0       | 19.79 | 19.79 | 19.79      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     |                 | 170.68          | 12.01         | 7.78     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 3  | 31   | 01 April 2025     |                 | 155.91          | 14.77         | 5.02     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 4  | 30   | 01 May 2025       |                 | 140.63          | 15.28         | 4.51     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 5  | 31   | 01 June 2025      |                 | 124.71          | 15.92         | 3.87     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 6  | 30   | 01 July 2025      |                 | 108.35          | 16.36         | 3.43     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 7  | 31   | 01 August 2025    |                 | 91.54           | 16.81         | 2.98     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 8  | 31   | 01 September 2025 |                 | 74.27           | 17.27         | 2.52     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 9  | 30   | 01 October 2025   |                 | 56.52           | 17.75         | 2.04     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 10 | 31   | 01 November 2025  |                 | 38.28           | 18.24         | 1.55     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 11 | 30   | 01 December 2025  |                 | 19.54           | 18.74         | 1.05     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 12 | 31   | 01 January 2026   |                 | 0.0             | 19.54         | 0.54     | 0.0  | 0.0       | 20.08 | 0.0   | 0.0        | 0.0  | 20.08       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 37.77    | 0.0  | 0.0       | 237.77 | 19.79 | 19.79      | 0.0  | 217.98      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 15 January 2025  | Repayment        | 19.79  | 17.31     | 2.48     | 0.0  | 0.0       | 182.69       | false    | false    |
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2025   |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 15 January 2025 | 182.69          | 17.31         | 2.48     | 0.0  | 0.0       | 19.79 | 19.79 | 19.79      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025     |                 | 155.91          | 14.77         | 5.02     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 4  | 30   | 01 May 2025       |                 | 140.63          | 15.28         | 4.51     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 5  | 31   | 01 June 2025      |                 | 124.71          | 15.92         | 3.87     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 6  | 30   | 01 July 2025      |                 | 108.35          | 16.36         | 3.43     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 7  | 31   | 01 August 2025    |                 | 91.54           | 16.81         | 2.98     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 8  | 31   | 01 September 2025 |                 | 74.27           | 17.27         | 2.52     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 9  | 30   | 01 October 2025   |                 | 56.52           | 17.75         | 2.04     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 10 | 31   | 01 November 2025  |                 | 38.28           | 18.24         | 1.55     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 11 | 30   | 01 December 2025  |                 | 19.54           | 18.74         | 1.05     | 0.0  | 0.0       | 19.79 | 0.0   | 0.0        | 0.0  | 19.79       |
      | 12 | 31   | 01 January 2026   |                 | 0.0             | 19.54         | 0.54     | 0.0  | 0.0       | 20.08 | 0.0   | 0.0        | 0.0  | 20.08       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 37.77    | 0.0  | 0.0       | 237.77  | 19.79 | 19.79      | 0.0  | 217.98      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 15 January 2025  | Repayment        | 19.79  | 17.31     | 2.48     | 0.0  | 0.0       | 182.69       | false    | false    |
      | 01 February 2025 | Accrual Activity | 2.48   | 0.0       | 2.48     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity | 7.78   | 0.0       | 7.78     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual Activity | 5.02   | 0.0       | 5.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 16.62  | 0.0       | 16.62    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C4001
  Scenario: Verify backdated loan migration with multiple disbursements on different dates with final COB
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 200            | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "200" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "100" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 92.85           | 7.15          | 2.75     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 2  | 28   | 01 March 2025     |           | 85.7            | 7.15          | 2.75     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 3  | 31   | 01 April 2025     |           | 78.55           | 7.15          | 2.75     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 4  | 30   | 01 May 2025       |           | 70.99           | 7.56          | 2.34     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 5  | 31   | 01 June 2025      |           | 63.04           | 7.95          | 1.95     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 6  | 30   | 01 July 2025      |           | 54.87           | 8.17          | 1.73     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 7  | 31   | 01 August 2025    |           | 46.48           | 8.39          | 1.51     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 8  | 31   | 01 September 2025 |           | 37.86           | 8.62          | 1.28     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 9  | 30   | 01 October 2025   |           | 29.0            | 8.86          | 1.04     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 10 | 31   | 01 November 2025  |           | 19.9            | 9.1           | 0.8      | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 11 | 30   | 01 December 2025  |           | 10.55           | 9.35          | 0.55     | 0.0  | 0.0       | 9.9   | 0.0  | 0.0        | 0.0  | 9.9         |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 10.55         | 0.29     | 0.0  | 0.0       | 10.84 | 0.0  | 0.0        | 0.0  | 10.84       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 19.74    | 0.0  | 0.0       | 119.74 | 0.0  | 0.0        | 0.0  | 119.74      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Admin successfully disburse the loan on "15 January 2025" with "100" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 15 January 2025   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 184.59          | 15.41         | 4.26     | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 2  | 28   | 01 March 2025     |           | 170.42          | 14.17         | 5.5      | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 3  | 31   | 01 April 2025     |           | 156.25          | 14.17         | 5.5      | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 4  | 30   | 01 May 2025       |           | 141.24          | 15.01         | 4.66     | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 5  | 31   | 01 June 2025      |           | 125.45          | 15.79         | 3.88     | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 6  | 30   | 01 July 2025      |           | 109.23          | 16.22         | 3.45     | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 7  | 31   | 01 August 2025    |           | 92.56           | 16.67         | 3.0      | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 8  | 31   | 01 September 2025 |           | 75.44           | 17.12         | 2.55     | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 9  | 30   | 01 October 2025   |           | 57.84           | 17.6          | 2.07     | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 10 | 31   | 01 November 2025  |           | 39.76           | 18.08         | 1.59     | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 11 | 30   | 01 December 2025  |           | 21.18           | 18.58         | 1.09     | 0.0  | 0.0       | 19.67 | 0.0  | 0.0        | 0.0  | 19.67       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 21.18         | 0.58     | 0.0  | 0.0       | 21.76 | 0.0  | 0.0        | 0.0  | 21.76       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 38.13    | 0.0  | 0.0       | 238.13 | 0.0  | 0.0        | 0.0  | 238.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 19.67 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 15 January 2025   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 February 2025 | 184.59          | 15.41         | 4.26     | 0.0  | 0.0       | 19.67 | 19.67 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 170.0           | 14.59         | 5.08     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 3  | 31   | 01 April 2025     |                  | 155.41          | 14.59         | 5.08     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 4  | 30   | 01 May 2025       |                  | 140.25          | 15.16         | 4.51     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 5  | 31   | 01 June 2025      |                  | 124.44          | 15.81         | 3.86     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 6  | 30   | 01 July 2025      |                  | 108.19          | 16.25         | 3.42     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 7  | 31   | 01 August 2025    |                  | 91.5            | 16.69         | 2.98     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 8  | 31   | 01 September 2025 |                  | 74.35           | 17.15         | 2.52     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 9  | 30   | 01 October 2025   |                  | 56.72           | 17.63         | 2.04     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 10 | 31   | 01 November 2025  |                  | 38.61           | 18.11         | 1.56     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 11 | 30   | 01 December 2025  |                  | 20.0            | 18.61         | 1.06     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0  | 19.67       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 20.0          | 0.55     | 0.0  | 0.0       | 20.55 | 0.0   | 0.0        | 0.0  | 20.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 36.92    | 0.0  | 0.0       | 236.92 | 19.67 | 0.0        | 0.0  | 217.25      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 February 2025 | Repayment        | 19.67  | 15.41     | 4.26     | 0.0  | 0.0       | 184.59       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "15 March 2025" with 19.67 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      |    |      | 15 January 2025   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 01 February 2025 | 184.59          | 15.41         | 4.26     | 0.0  | 0.0       | 19.67 | 19.67 | 0.0        | 0.0   | 0.0         |
      | 2  | 28   | 01 March 2025     | 15 March 2025    | 170.0           | 14.59         | 5.08     | 0.0  | 0.0       | 19.67 | 19.67 | 0.0        | 19.67 | 0.0         |
      | 3  | 31   | 01 April 2025     |                  | 155.19          | 14.81         | 4.86     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 4  | 30   | 01 May 2025       |                  | 139.91          | 15.28         | 4.39     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 5  | 31   | 01 June 2025      |                  | 124.09          | 15.82         | 3.85     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 6  | 30   | 01 July 2025      |                  | 107.83          | 16.26         | 3.41     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 7  | 31   | 01 August 2025    |                  | 91.13           | 16.7          | 2.97     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 8  | 31   | 01 September 2025 |                  | 73.97           | 17.16         | 2.51     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 9  | 30   | 01 October 2025   |                  | 56.33           | 17.64         | 2.03     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 10 | 31   | 01 November 2025  |                  | 38.21           | 18.12         | 1.55     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 11 | 30   | 01 December 2025  |                  | 19.59           | 18.62         | 1.05     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 19.59         | 0.54     | 0.0  | 0.0       | 20.13 | 0.0   | 0.0        | 0.0   | 20.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 200.0         | 36.5     | 0.0  | 0.0       | 236.5 | 39.34 | 0.0        | 19.67 | 197.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 February 2025 | Repayment        | 19.67  | 15.41     | 4.26     | 0.0  | 0.0       | 184.59       | false    | false    |
      | 15 March 2025    | Repayment        | 19.67  | 14.59     | 5.08     | 0.0  | 0.0       | 170.0        | false    | false    |
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      |    |      | 15 January 2025   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 01 February 2025 | 184.59          | 15.41         | 4.26     | 0.0  | 0.0       | 19.67 | 19.67 | 0.0        | 0.0   | 0.0         |
      | 2  | 28   | 01 March 2025     | 15 March 2025    | 170.0           | 14.59         | 5.08     | 0.0  | 0.0       | 19.67 | 19.67 | 0.0        | 19.67 | 0.0         |
      | 3  | 31   | 01 April 2025     |                  | 155.19          | 14.81         | 4.86     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 4  | 30   | 01 May 2025       |                  | 139.91          | 15.28         | 4.39     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 5  | 31   | 01 June 2025      |                  | 124.09          | 15.82         | 3.85     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 6  | 30   | 01 July 2025      |                  | 107.83          | 16.26         | 3.41     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 7  | 31   | 01 August 2025    |                  | 91.13           | 16.7          | 2.97     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 8  | 31   | 01 September 2025 |                  | 73.97           | 17.16         | 2.51     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 9  | 30   | 01 October 2025   |                  | 56.33           | 17.64         | 2.03     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 10 | 31   | 01 November 2025  |                  | 38.21           | 18.12         | 1.55     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 11 | 30   | 01 December 2025  |                  | 19.59           | 18.62         | 1.05     | 0.0  | 0.0       | 19.67 | 0.0   | 0.0        | 0.0   | 19.67       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 19.59         | 0.54     | 0.0  | 0.0       | 20.13 | 0.0   | 0.0        | 0.0   | 20.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 200.0         | 36.5     | 0.0  | 0.0       | 236.5 | 39.34 | 0.0        | 19.67 | 197.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2025  | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 February 2025 | Repayment         | 19.67  | 15.41     | 4.26     | 0.0  | 0.0       | 184.59       | false    | false    |
      | 01 February 2025 | Accrual Activity  | 4.26   | 0.0       | 4.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity  | 5.08   | 0.0       | 5.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2025    | Repayment         | 19.67  | 14.59     | 5.08     | 0.0  | 0.0       | 170.0        | false    | false    |
      | 01 April 2025    | Accrual Activity  | 4.86   | 0.0       | 4.86     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual           | 15.45  | 0.0       | 15.45    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C4002
  Scenario: Verify backdated loan migration with partial repayment on disbursement date
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 2  | 28   | 01 March 2025     |           | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 3  | 31   | 01 April 2025     |           | 785.59          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 4  | 30   | 01 May 2025       |           | 709.99          | 75.6          | 23.37    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2025      |           | 630.54          | 79.45         | 19.52    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |           | 548.91          | 81.63         | 17.34    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |           | 465.04          | 83.87         | 15.1     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |           | 378.86          | 86.18         | 12.79    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |           | 290.31          | 88.55         | 10.42    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |           | 199.32          | 90.99         | 7.98     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |           | 105.83          | 93.49         | 5.48     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 105.83        | 2.91     | 0.0  | 0.0       | 108.74 | 0.0  | 0.0        | 0.0  | 108.74      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 197.41   | 0.0  | 0.0       | 1197.41 | 0.0  | 0.0        | 0.0  | 1197.41     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "01 January 2025" with 200 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 January 2025 | 901.03          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97 | 98.97 | 98.97      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 January 2025 | 802.06          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97 | 98.97 | 98.97      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025     |                 | 769.09          | 32.97         | 66.0     | 0.0  | 0.0       | 98.97 | 2.06  | 2.06       | 0.0  | 96.91       |
      | 4  | 30   | 01 May 2025       |                 | 691.52          | 77.57         | 21.4     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2025      |                 | 611.57          | 79.95         | 19.02    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |                 | 529.42          | 82.15         | 16.82    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |                 | 445.01          | 84.41         | 14.56    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |                 | 358.28          | 86.73         | 12.24    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |                 | 269.16          | 89.12         | 9.85     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |                 | 177.59          | 91.57         | 7.4      | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |                 | 83.5            | 94.09         | 4.88     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |                 | 0.0             | 83.5          | 2.3      | 0.0  | 0.0       | 85.8  | 0.0   | 0.0        | 0.0  | 85.8        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 174.47   | 0.0  | 0.0       | 1174.47 | 200.0 | 200.0      | 0.0  | 974.47      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2025  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Customer makes "AUTOPAY" repayment on "15 February 2025" with 98.97 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 January 2025  | 901.03          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97 | 98.97 | 98.97      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 January 2025  | 802.06          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97 | 98.97 | 98.97      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025     | 15 February 2025 | 736.09          | 65.97         | 33.0     | 0.0  | 0.0       | 98.97 | 98.97 | 98.97      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025       |                  | 687.59          | 48.5          | 50.47    | 0.0  | 0.0       | 98.97 | 2.06  | 2.06       | 0.0  | 96.91       |
      | 5  | 31   | 01 June 2025      |                  | 607.53          | 80.06         | 18.91    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |                  | 525.27          | 82.26         | 16.71    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |                  | 440.74          | 84.53         | 14.44    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |                  | 353.89          | 86.85         | 12.12    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |                  | 264.65          | 89.24         | 9.73     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |                  | 172.96          | 91.69         | 7.28     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |                  | 78.75           | 94.21         | 4.76     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 78.75         | 2.17     | 0.0  | 0.0       | 80.92 | 0.0   | 0.0        | 0.0  | 80.92       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 169.59   | 0.0  | 0.0       | 1169.59 | 298.97 | 298.97     | 0.0  | 870.62      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2025  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 15 February 2025 | Repayment        | 98.97  | 65.97     | 33.0     | 0.0  | 0.0       | 734.03       | false    | false    |
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 January 2025  | 901.03          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97 | 98.97 | 98.97      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 January 2025  | 802.06          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97 | 98.97 | 98.97      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025     | 15 February 2025 | 736.09          | 65.97         | 33.0     | 0.0  | 0.0       | 98.97 | 98.97 | 98.97      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025       |                  | 687.59          | 48.5          | 50.47    | 0.0  | 0.0       | 98.97 | 2.06  | 2.06       | 0.0  | 96.91       |
      | 5  | 31   | 01 June 2025      |                  | 607.53          | 80.06         | 18.91    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |                  | 525.27          | 82.26         | 16.71    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |                  | 440.74          | 84.53         | 14.44    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |                  | 353.89          | 86.85         | 12.12    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |                  | 264.65          | 89.24         | 9.73     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |                  | 172.96          | 91.69         | 7.28     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |                  | 78.75           | 94.21         | 4.76     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 78.75         | 2.17     | 0.0  | 0.0       | 80.92 | 0.0   | 0.0        | 0.0  | 80.92       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 169.59   | 0.0  | 0.0       | 1169.59 | 298.97 | 298.97     | 0.0  | 870.62      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2025  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 15 February 2025 | Repayment        | 98.97  | 65.97     | 33.0     | 0.0  | 0.0       | 734.03       | false    | false    |
      | 01 April 2025    | Accrual Activity | 33.0   | 0.0       | 33.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 68.66  | 0.0       | 68.66    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C4437
  Scenario: Verify backdated loan with applied charges and final COB - charge-inclusive income recognition
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 500            | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "500" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 464.27          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 2  | 28   | 01 March 2025     |           | 428.54          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 3  | 31   | 01 April 2025     |           | 392.81          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 4  | 30   | 01 May 2025       |           | 355.02          | 37.79         | 11.69    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 5  | 31   | 01 June 2025      |           | 315.3           | 39.72         | 9.76     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 6  | 30   | 01 July 2025      |           | 274.49          | 40.81         | 8.67     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 7  | 31   | 01 August 2025    |           | 232.56          | 41.93         | 7.55     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 8  | 31   | 01 September 2025 |           | 189.48          | 43.08         | 6.4      | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 9  | 30   | 01 October 2025   |           | 145.21          | 44.27         | 5.21     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 10 | 31   | 01 November 2025  |           | 99.72           | 45.49         | 3.99     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 11 | 30   | 01 December 2025  |           | 52.98           | 46.74         | 2.74     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 52.98         | 1.46     | 0.0  | 0.0       | 54.44 | 0.0  | 0.0        | 0.0  | 54.44       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 98.72    | 0.0  | 0.0       | 598.72 | 0.0  | 0.0        | 0.0  | 598.72      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
#   --- Add fee charge backdated to 15 January 2025 ---
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 January 2025" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 464.27          | 35.73         | 13.75    | 50.0 | 0.0       | 99.48 | 0.0  | 0.0        | 0.0  | 99.48       |
      | 2  | 28   | 01 March 2025     |           | 428.54          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 3  | 31   | 01 April 2025     |           | 392.81          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 4  | 30   | 01 May 2025       |           | 355.02          | 37.79         | 11.69    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 5  | 31   | 01 June 2025      |           | 315.3           | 39.72         | 9.76     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 6  | 30   | 01 July 2025      |           | 274.49          | 40.81         | 8.67     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 7  | 31   | 01 August 2025    |           | 232.56          | 41.93         | 7.55     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 8  | 31   | 01 September 2025 |           | 189.48          | 43.08         | 6.4      | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 9  | 30   | 01 October 2025   |           | 145.21          | 44.27         | 5.21     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 10 | 31   | 01 November 2025  |           | 99.72           | 45.49         | 3.99     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 11 | 30   | 01 December 2025  |           | 52.98           | 46.74         | 2.74     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 52.98         | 1.46     | 0.0  | 0.0       | 54.44 | 0.0  | 0.0        | 0.0  | 54.44       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 98.72    | 50.0  | 0.0      | 648.72 | 0.0  | 0.0        | 0.0  | 648.72      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 15 January 2025 | Flat             | 50.0 | 0.0  | 0.0    | 50.0        |
#   --- Make partial payment backdated to 01 February 2025 covering portion of principal/interest/fees ---
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 75 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 464.27          | 35.73         | 13.75    | 50.0 | 0.0       | 99.48 | 75.0 | 0.0        | 0.0  | 24.48       |
      | 2  | 28   | 01 March 2025     |           | 427.56          | 36.71         | 12.77    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 3  | 31   | 01 April 2025     |           | 390.85          | 36.71         | 12.77    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 4  | 30   | 01 May 2025       |           | 352.72          | 38.13         | 11.35    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 5  | 31   | 01 June 2025      |           | 312.94          | 39.78         | 9.7      | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 6  | 30   | 01 July 2025      |           | 272.07          | 40.87         | 8.61     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 7  | 31   | 01 August 2025    |           | 230.07          | 42.0          | 7.48     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 8  | 31   | 01 September 2025 |           | 186.92          | 43.15         | 6.33     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 9  | 30   | 01 October 2025   |           | 142.58          | 44.34         | 5.14     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 10 | 31   | 01 November 2025  |           | 97.02           | 45.56         | 3.92     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 11 | 30   | 01 December 2025  |           | 50.21           | 46.81         | 2.67     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 50.21         | 1.38     | 0.0  | 0.0       | 51.59 | 0.0  | 0.0        | 0.0  | 51.59       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 95.87    | 50.0  | 0.0      | 645.87 | 75.0 | 0.0        | 0.0  | 570.87      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0   | 0.0       | 500.0        | false    | false    |
      | 01 February 2025 | Repayment        | 75.0   | 35.73     | 13.75    | 25.52 | 0.0       | 464.27       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid   | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 15 January 2025 | Flat             | 50.0 | 25.52  | 0.0    | 24.48       |
#   --- Run inline COB job to generate aggregated accruals ---
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 464.27          | 35.73         | 13.75    | 50.0 | 0.0       | 99.48 | 75.0 | 0.0        | 0.0  | 24.48       |
      | 2  | 28   | 01 March 2025     |           | 427.56          | 36.71         | 12.77    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 3  | 31   | 01 April 2025     |           | 390.85          | 36.71         | 12.77    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 4  | 30   | 01 May 2025       |           | 352.72          | 38.13         | 11.35    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 5  | 31   | 01 June 2025      |           | 312.94          | 39.78         | 9.7      | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 6  | 30   | 01 July 2025      |           | 272.07          | 40.87         | 8.61     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 7  | 31   | 01 August 2025    |           | 230.07          | 42.0          | 7.48     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 8  | 31   | 01 September 2025 |           | 186.92          | 43.15         | 6.33     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 9  | 30   | 01 October 2025   |           | 142.58          | 44.34         | 5.14     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 10 | 31   | 01 November 2025  |           | 97.02           | 45.56         | 3.92     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 11 | 30   | 01 December 2025  |           | 50.21           | 46.81         | 2.67     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 50.21         | 1.38     | 0.0  | 0.0       | 51.59 | 0.0  | 0.0        | 0.0  | 51.59       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 95.87    | 50.0  | 0.0      | 645.87 | 75.0 | 0.0        | 0.0  | 570.87      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0   | 0.0       | 500.0        | false    | false    |
      | 01 February 2025 | Repayment        | 75.0   | 35.73     | 13.75    | 25.52 | 0.0       | 464.27       | false    | false    |
      | 01 February 2025 | Accrual Activity | 63.75  | 0.0       | 13.75    | 50.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity | 12.77  | 0.0       | 12.77    | 0.0   | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual Activity | 12.77  | 0.0       | 12.77    | 0.0   | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 92.69  | 0.0       | 42.69    | 50.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid   | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 15 January 2025 | Flat             | 50.0 | 25.52  | 0.0    | 24.48       |
    When Loan Pay-off is made on "10 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4438
  Scenario: Verify backdated loan with NSF fee after late payment - penalty income recognition
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 500            | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "500" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 464.27          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 2  | 28   | 01 March 2025     |           | 428.54          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 3  | 31   | 01 April 2025     |           | 392.81          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 4  | 30   | 01 May 2025       |           | 355.02          | 37.79         | 11.69    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 5  | 31   | 01 June 2025      |           | 315.3           | 39.72         | 9.76     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 6  | 30   | 01 July 2025      |           | 274.49          | 40.81         | 8.67     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 7  | 31   | 01 August 2025    |           | 232.56          | 41.93         | 7.55     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 8  | 31   | 01 September 2025 |           | 189.48          | 43.08         | 6.4      | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 9  | 30   | 01 October 2025   |           | 145.21          | 44.27         | 5.21     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 10 | 31   | 01 November 2025  |           | 99.72           | 45.49         | 3.99     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 11 | 30   | 01 December 2025  |           | 52.98           | 46.74         | 2.74     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 52.98         | 1.46     | 0.0  | 0.0       | 54.44 | 0.0  | 0.0        | 0.0  | 54.44       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 98.72    | 0.0  | 0.0       | 598.72 | 0.0  | 0.0        | 0.0  | 598.72      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
#   --- Make late payment backdated to 15 February 2025 (due was 01 February 2025) ---
    And Customer makes "AUTOPAY" repayment on "15 February 2025" with 49.48 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 15 February 2025 | 464.27          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 49.48 | 0.0        | 49.48 | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 428.05          | 36.22         | 13.26    | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 3  | 31   | 01 April 2025     |                  | 391.34          | 36.71         | 12.77    | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 4  | 30   | 01 May 2025       |                  | 353.22          | 38.12         | 11.36    | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 5  | 31   | 01 June 2025      |                  | 313.45          | 39.77         | 9.71     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 6  | 30   | 01 July 2025      |                  | 272.59          | 40.86         | 8.62     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 7  | 31   | 01 August 2025    |                  | 230.61          | 41.98         | 7.5      | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 8  | 31   | 01 September 2025 |                  | 187.47          | 43.14         | 6.34     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 9  | 30   | 01 October 2025   |                  | 143.15          | 44.32         | 5.16     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 10 | 31   | 01 November 2025  |                  | 97.61           | 45.54         | 3.94     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 11 | 30   | 01 December 2025  |                  | 50.81           | 46.8          | 2.68     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 50.81         | 1.4      | 0.0  | 0.0       | 52.21 | 0.0   | 0.0        | 0.0   | 52.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 500.0         | 96.49    | 0.0  | 0.0       | 596.49 | 49.48 | 0.0        | 49.48 | 547.01      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 15 February 2025 | Repayment        | 49.48  | 35.73     | 13.75    | 0.0  | 0.0       | 464.27       | false    | false    |
#   --- Add NSF fee backdated to 20 February 2025 ---
    When Admin adds an NSF fee because of payment bounce with "20 February 2025" transaction date
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 15 February 2025 | 464.27          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 49.48 | 0.0        | 49.48 | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 428.05          | 36.22         | 13.26    | 0.0  | 10.0      | 59.48 | 0.0   | 0.0        | 0.0   | 59.48       |
      | 3  | 31   | 01 April 2025     |                  | 391.34          | 36.71         | 12.77    | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 4  | 30   | 01 May 2025       |                  | 353.22          | 38.12         | 11.36    | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 5  | 31   | 01 June 2025      |                  | 313.45          | 39.77         | 9.71     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 6  | 30   | 01 July 2025      |                  | 272.59          | 40.86         | 8.62     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 7  | 31   | 01 August 2025    |                  | 230.61          | 41.98         | 7.5      | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 8  | 31   | 01 September 2025 |                  | 187.47          | 43.14         | 6.34     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 9  | 30   | 01 October 2025   |                  | 143.15          | 44.32         | 5.16     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 10 | 31   | 01 November 2025  |                  | 97.61           | 45.54         | 3.94     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 11 | 30   | 01 December 2025  |                  | 50.81           | 46.8          | 2.68     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 50.81         | 1.4      | 0.0  | 0.0       | 52.21 | 0.0   | 0.0        | 0.0   | 52.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 500.0         | 96.49    | 0.0  | 10.0      | 606.49 | 49.48 | 0.0        | 49.48 | 557.01      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 15 February 2025 | Repayment        | 49.48  | 35.73     | 13.75    | 0.0  | 0.0       | 464.27       | false    | false    |
    And Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 February 2025 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
#   --- Run inline COB job to generate aggregated accruals including penalty ---
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 15 February 2025 | 464.27          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 49.48 | 0.0        | 49.48 | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 428.05          | 36.22         | 13.26    | 0.0  | 10.0      | 59.48 | 0.0   | 0.0        | 0.0   | 59.48       |
      | 3  | 31   | 01 April 2025     |                  | 391.34          | 36.71         | 12.77    | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 4  | 30   | 01 May 2025       |                  | 353.22          | 38.12         | 11.36    | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 5  | 31   | 01 June 2025      |                  | 313.45          | 39.77         | 9.71     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 6  | 30   | 01 July 2025      |                  | 272.59          | 40.86         | 8.62     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 7  | 31   | 01 August 2025    |                  | 230.61          | 41.98         | 7.5      | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 8  | 31   | 01 September 2025 |                  | 187.47          | 43.14         | 6.34     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 9  | 30   | 01 October 2025   |                  | 143.15          | 44.32         | 5.16     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 10 | 31   | 01 November 2025  |                  | 97.61           | 45.54         | 3.94     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 11 | 30   | 01 December 2025  |                  | 50.81           | 46.8          | 2.68     | 0.0  | 0.0       | 49.48 | 0.0   | 0.0        | 0.0   | 49.48       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 50.81         | 1.4      | 0.0  | 0.0       | 52.21 | 0.0   | 0.0        | 0.0   | 52.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 500.0         | 96.49    | 0.0  | 10.0      | 606.49 | 49.48 | 0.0        | 49.48 | 557.01      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 February 2025 | Accrual Activity | 13.75  | 0.0       | 13.75    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Repayment        | 49.48  | 35.73     | 13.75    | 0.0  | 0.0       | 464.27       | false    | false    |
      | 01 March 2025    | Accrual Activity | 23.26  | 0.0       | 13.26    | 0.0  | 10.0      | 0.0          | false    | false    |
      | 01 April 2025    | Accrual Activity | 12.77  | 0.0       | 12.77    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 53.18  | 0.0       | 43.18    | 0.0  | 10.0      | 0.0          | false    | false    |
    When Loan Pay-off is made on "10 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4439
  Scenario: Verify backdated loan with multiple repayment reversals and refunds - schedule and balance adjustments
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 2  | 28   | 01 March 2025     |           | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 3  | 31   | 01 April 2025     |           | 785.59          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 4  | 30   | 01 May 2025       |           | 709.99          | 75.6          | 23.37    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2025      |           | 630.54          | 79.45         | 19.52    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |           | 548.91          | 81.63         | 17.34    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |           | 465.04          | 83.87         | 15.1     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |           | 378.86          | 86.18         | 12.79    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |           | 290.31          | 88.55         | 10.42    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |           | 199.32          | 90.99         | 7.98     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |           | 105.83          | 93.49         | 5.48     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 105.83        | 2.91     | 0.0  | 0.0       | 108.74 | 0.0  | 0.0        | 0.0  | 108.74      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 197.41   | 0.0  | 0.0       | 1197.41 | 0.0  | 0.0        | 0.0  | 1197.41     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
#   --- Make first payment backdated to 01 February 2025 ---
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 100 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 February 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 855.07          | 73.46         | 25.51    | 0.0  | 0.0       | 98.97  | 1.03   | 1.03       | 0.0  | 97.94       |
      | 3  | 31   | 01 April 2025     |                  | 781.61          | 73.46         | 25.51    | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 4  | 30   | 01 May 2025       |                  | 705.34          | 76.27         | 22.7     | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2025      |                  | 625.77          | 79.57         | 19.4     | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |                  | 544.01          | 81.76         | 17.21    | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |                  | 460.0           | 84.01         | 14.96    | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |                  | 373.68          | 86.32         | 12.65    | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |                  | 284.99          | 88.69         | 10.28    | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |                  | 193.86          | 91.13         | 7.84     | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |                  | 100.22          | 93.64         | 5.33     | 0.0  | 0.0       | 98.97  | 0.0    | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 100.22        | 2.76     | 0.0  | 0.0       | 102.98 | 0.0    | 0.0        | 0.0  | 102.98      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 191.65   | 0.0  | 0.0       | 1191.65 | 100.0 | 1.03       | 0.0  | 1091.65     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 100.0  | 72.5      | 27.5     | 0.0  | 0.0       | 927.5        | false    | false    |
#   --- Make second payment backdated to 01 March 2025 ---
    And Customer makes "AUTOPAY" repayment on "01 March 2025" with 150 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 February 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97 | 98.97 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 March 2025    | 855.07          | 73.46         | 25.51    | 0.0  | 0.0       | 98.97 | 98.97 | 1.03       | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025     |                  | 778.18          | 76.89         | 22.08    | 0.0  | 0.0       | 98.97 | 52.06 | 52.06      | 0.0  | 46.91       |
      | 4  | 30   | 01 May 2025       |                  | 700.81          | 77.37         | 21.6     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2025      |                  | 621.11          | 79.7          | 19.27    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |                  | 539.22          | 81.89         | 17.08    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |                  | 455.08          | 84.14         | 14.83    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |                  | 368.62          | 86.46         | 12.51    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |                  | 279.79          | 88.83         | 10.14    | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |                  | 188.51          | 91.28         | 7.69     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |                  | 94.72           | 93.79         | 5.18     | 0.0  | 0.0       | 98.97 | 0.0   | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 94.72         | 2.6      | 0.0  | 0.0       | 97.32 | 0.0   | 0.0        | 0.0  | 97.32       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 185.99   | 0.0  | 0.0       | 1185.99 | 250.0 | 53.09      | 0.0  | 935.99     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 100.0  | 72.5      | 27.5     | 0.0  | 0.0       | 927.5        | false    | false    |
      | 01 March 2025    | Repayment        | 150.0  | 124.49    | 25.51    | 0.0  | 0.0       | 803.01       | false    | false    |
#   --- Reverse/refund the first payment from 01 February 2025 ---
    When Customer undo "1"th repayment on "01 February 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 01 March 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 98.97 | 0.0         |
      | 2  | 28   | 01 March 2025     |               | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 51.03 | 0.0        | 0.0   | 47.94       |
      | 3  | 31   | 01 April 2025     |               | 782.98          | 74.08         | 24.89    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 4  | 30   | 01 May 2025       |               | 706.55          | 76.43         | 22.54    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 5  | 31   | 01 June 2025      |               | 627.01          | 79.54         | 19.43    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 6  | 30   | 01 July 2025      |               | 545.28          | 81.73         | 17.24    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 7  | 31   | 01 August 2025    |               | 461.31          | 83.97         | 15.0     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 8  | 31   | 01 September 2025 |               | 375.03          | 86.28         | 12.69    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 9  | 30   | 01 October 2025   |               | 286.37          | 88.66         | 10.31    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 10 | 31   | 01 November 2025  |               | 195.28          | 91.09         | 7.88     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 11 | 30   | 01 December 2025  |               | 101.68          | 93.6          | 5.37     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 12 | 31   | 01 January 2026   |               | 0.0             | 101.68        | 2.8      | 0.0  | 0.0       | 104.48 | 0.0   | 0.0        | 0.0   | 104.48      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 193.15   | 0.0  | 0.0       | 1193.15 | 150.0 | 0.0        | 98.97 | 1043.15     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 100.0  | 72.5      | 27.5     | 0.0  | 0.0       | 927.5        | true     | false    |
      | 01 March 2025    | Repayment        | 150.0  | 95.0      | 55.0     | 0.0  | 0.0       | 905.0        | false    | true     |
#   --- Make third payment backdated to 01 April 2025 ---
    And Customer makes "AUTOPAY" repayment on "01 April 2025" with 200 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 01 March 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 98.97 | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 April 2025 | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 47.94 | 0.0         |
      | 3  | 31   | 01 April 2025     | 01 April 2025 | 782.98          | 74.08         | 24.89    | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 0.0   | 0.0         |
      | 4  | 30   | 01 May 2025       |               | 704.08          | 78.9          | 20.07    | 0.0  | 0.0       | 98.97  | 53.09 | 53.09      | 0.0   | 45.88       |
      | 5  | 31   | 01 June 2025      |               | 624.47          | 79.61         | 19.36    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 6  | 30   | 01 July 2025      |               | 542.67          | 81.8          | 17.17    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 7  | 31   | 01 August 2025    |               | 458.62          | 84.05         | 14.92    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 8  | 31   | 01 September 2025 |               | 372.26          | 86.36         | 12.61    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 9  | 30   | 01 October 2025   |               | 283.53          | 88.73         | 10.24    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 10 | 31   | 01 November 2025  |               | 192.36          | 91.17         | 7.8      | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 11 | 30   | 01 December 2025  |               | 98.68           | 93.68         | 5.29     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 12 | 31   | 01 January 2026   |               | 0.0             | 98.68         | 2.71     | 0.0  | 0.0       | 101.39 | 0.0   | 0.0        | 0.0   | 101.39      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late   | Outstanding |
      | 1000.0        | 190.06   | 0.0  | 0.0       | 1190.06 | 350.0 | 53.09      | 146.91 | 840.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 100.0  | 72.5      | 27.5     | 0.0  | 0.0       | 927.5        | true     | false    |
      | 01 March 2025    | Repayment        | 150.0  | 95.0      | 55.0     | 0.0  | 0.0       | 905.0        | false    | true     |
      | 01 April 2025    | Repayment        | 200.0  | 175.11    | 24.89    | 0.0  | 0.0       | 729.89       | false    | false    |
#   --- Reverse/refund the second payment from 01 March 2025 ---
    When Customer undo "2"th repayment on "01 March 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 01 April 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 98.97 | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 April 2025 | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 98.97 | 0.0         |
      | 3  | 31   | 01 April 2025     |               | 785.59          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 2.06  | 0.0        | 0.0   | 96.91       |
      | 4  | 30   | 01 May 2025       |               | 708.81          | 76.78         | 22.19    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 5  | 31   | 01 June 2025      |               | 629.33          | 79.48         | 19.49    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 6  | 30   | 01 July 2025      |               | 547.67          | 81.66         | 17.31    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 7  | 31   | 01 August 2025    |               | 463.76          | 83.91         | 15.06    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 8  | 31   | 01 September 2025 |               | 377.54          | 86.22         | 12.75    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 9  | 30   | 01 October 2025   |               | 288.95          | 88.59         | 10.38    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 10 | 31   | 01 November 2025  |               | 197.93          | 91.02         | 7.95     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 11 | 30   | 01 December 2025  |               | 104.4           | 93.53         | 5.44     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 12 | 31   | 01 January 2026   |               | 0.0             | 104.4         | 2.87     | 0.0  | 0.0       | 107.27 | 0.0   | 0.0        | 0.0   | 107.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late   | Outstanding |
      | 1000.0        | 195.94   | 0.0  | 0.0       | 1195.94 | 200.0 | 0.0        | 197.94 | 995.94      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 100.0  | 72.5      | 27.5     | 0.0  | 0.0       | 927.5        | true     | false    |
      | 01 March 2025    | Repayment        | 150.0  | 95.0      | 55.0     | 0.0  | 0.0       | 905.0        | true     | true     |
      | 01 April 2025    | Repayment        | 200.0  | 142.94    | 57.06    | 0.0  | 0.0       | 857.06       | false    | true     |
#   --- Run inline COB job to generate aggregated accruals ---
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 01 April 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 98.97 | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 April 2025 | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 98.97 | 0.0         |
      | 3  | 31   | 01 April 2025     |               | 785.59          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 2.06  | 0.0        | 0.0   | 96.91       |
      | 4  | 30   | 01 May 2025       |               | 708.81          | 76.78         | 22.19    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 5  | 31   | 01 June 2025      |               | 629.33          | 79.48         | 19.49    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 6  | 30   | 01 July 2025      |               | 547.67          | 81.66         | 17.31    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 7  | 31   | 01 August 2025    |               | 463.76          | 83.91         | 15.06    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 8  | 31   | 01 September 2025 |               | 377.54          | 86.22         | 12.75    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 9  | 30   | 01 October 2025   |               | 288.95          | 88.59         | 10.38    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 10 | 31   | 01 November 2025  |               | 197.93          | 91.02         | 7.95     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 11 | 30   | 01 December 2025  |               | 104.4           | 93.53         | 5.44     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 12 | 31   | 01 January 2026   |               | 0.0             | 104.4         | 2.87     | 0.0  | 0.0       | 107.27 | 0.0   | 0.0        | 0.0   | 107.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late   | Outstanding |
      | 1000.0        | 195.94   | 0.0  | 0.0       | 1195.94 | 200.0 | 0.0        | 197.94 | 995.94      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 100.0  | 72.5      | 27.5     | 0.0  | 0.0       | 927.5        | true     | false    |
      | 01 February 2025 | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Repayment        | 150.0  | 95.0      | 55.0     | 0.0  | 0.0       | 905.0        | true     | true     |
      | 01 March 2025    | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Repayment        | 200.0  | 142.94    | 57.06    | 0.0  | 0.0       | 857.06       | false    | true     |
      | 01 April 2025    | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 88.79  | 0.0       | 88.79    | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Verify final loan balance reflects all transactions correctly ---
    Then Loan has 995.94 outstanding amount
    When Loan Pay-off is made on "10 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
  @TestRailId:C4480
  Scenario: Verify backdated loan migration with pre-closure (early payoff)
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 2  | 28   | 01 March 2025     |           | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 3  | 31   | 01 April 2025     |           | 785.59          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 4  | 30   | 01 May 2025       |           | 709.99          | 75.6          | 23.37    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2025      |           | 630.54          | 79.45         | 19.52    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |           | 548.91          | 81.63         | 17.34    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |           | 465.04          | 83.87         | 15.1     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |           | 378.86          | 86.18         | 12.79    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |           | 290.31          | 88.55         | 10.42    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |           | 199.32          | 90.99         | 7.98     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |           | 105.83          | 93.49         | 5.48     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 105.83        | 2.91     | 0.0  | 0.0       | 108.74 | 0.0  | 0.0        | 0.0  | 108.74      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 197.41   | 0.0  | 0.0       | 1197.41 | 0.0  | 0.0        | 0.0  | 1197.41     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    # Make backdated partial repayment on 01 February 2025
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 98.97 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 February 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 855.09          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 3  | 31   | 01 April 2025     |                  | 781.65          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 4  | 30   | 01 May 2025       |                  | 705.39          | 76.26         | 22.71    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2025      |                  | 625.82          | 79.57         | 19.4     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2025      |                  | 544.06          | 81.76         | 17.21    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2025    |                  | 460.05          | 84.01         | 14.96    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2025 |                  | 373.73          | 86.32         | 12.65    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2025   |                  | 285.04          | 88.69         | 10.28    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2025  |                  | 193.91          | 91.13         | 7.84     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2025  |                  | 100.27          | 93.64         | 5.33     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 100.27        | 2.76     | 0.0  | 0.0       | 103.03 | 0.0   | 0.0        | 0.0  | 103.03      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 191.7    | 0.0  | 0.0       | 1191.7 | 98.97 | 0.0        | 0.0  | 1092.73     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 98.97  | 71.47     | 27.5     | 0.0  | 0.0       | 928.53       | false    | false    |
    # Make preclosure (Pay-off) on 01 March 2025
    When Loan Pay-off is made on "01 March 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 February 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 March 2025    | 855.09          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 98.97  | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025     | 01 March 2025    | 756.12          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97  | 98.97  | 98.97      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025       | 01 March 2025    | 657.15          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97  | 98.97  | 98.97      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025      | 01 March 2025    | 558.18          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97  | 98.97  | 98.97      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025      | 01 March 2025    | 459.21          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97  | 98.97  | 98.97      | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2025    | 01 March 2025    | 360.24          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97  | 98.97  | 98.97      | 0.0  | 0.0         |
      | 8  | 31   | 01 September 2025 | 01 March 2025    | 261.27          | 98.97         | 0.0      | 0.0  | 0.0       | 98.97  | 98.97  | 98.97      | 0.0  | 0.0         |
      | 9  | 30   | 01 October 2025   | 01 March 2025    | 162.3           | 98.97         | 0.0      | 0.0  | 0.0       | 98.97  | 98.97  | 98.97      | 0.0  | 0.0         |
      | 10 | 31   | 01 November 2025  | 01 March 2025    | 63.33           | 98.97         | 0.0      | 0.0  | 0.0       | 98.97  | 98.97  | 98.97      | 0.0  | 0.0         |
      | 11 | 30   | 01 December 2025  | 01 March 2025    | 0.0             | 63.33         | 0.0      | 0.0  | 0.0       | 63.33  | 63.33  | 63.33      | 0.0  | 0.0         |
      | 12 | 31   | 01 January 2026   | 01 March 2025    | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0    | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 53.03    | 0.0  | 0.0       | 1053.03 | 1053.03 | 855.09     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 98.97   | 71.47     | 27.5     | 0.0  | 0.0       | 928.53       | false    | false    |
      | 01 February 2025 | Accrual Activity | 27.5    | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Repayment        | 954.06  | 928.53    | 25.53    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity | 25.53   | 0.0       | 25.53    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 53.03   | 0.0       | 53.03    | 0.0  | 0.0       | 0.0          | false    | false    |
    # Verify loan is closed after preclosure
    Then Loan's all installments have obligations met
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    # Run inline COB job for loan
    When Admin runs inline COB job for Loan
    # Verify loan remains closed after COB
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    # Verify no delinquency on closed loan
    Then Loan has 0.0 total overdue amount
    # Verify final accrual entries
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment        | 98.97   | 71.47     | 27.5     | 0.0  | 0.0       | 928.53       | false    | false    |
      | 01 February 2025 | Accrual Activity | 27.5    | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Repayment        | 954.06  | 928.53    | 25.53    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual Activity | 25.53   | 0.0       | 25.53    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 53.03   | 0.0       | 53.03    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C4481
  Scenario: Verify backdated loan migration with refund transactions (merchant refund and interest refund)
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    # First disbursement - 01/01/2025 - 500 EUR
    And Admin successfully disburse the loan on "01 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 464.27          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 2  | 28   | 01 March 2025     |           | 428.54          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 3  | 31   | 01 April 2025     |           | 392.81          | 35.73         | 13.75    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 4  | 30   | 01 May 2025       |           | 355.02          | 37.79         | 11.69    | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 5  | 31   | 01 June 2025      |           | 315.3           | 39.72         | 9.76     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 6  | 30   | 01 July 2025      |           | 274.49          | 40.81         | 8.67     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 7  | 31   | 01 August 2025    |           | 232.56          | 41.93         | 7.55     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 8  | 31   | 01 September 2025 |           | 189.48          | 43.08         | 6.4      | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 9  | 30   | 01 October 2025   |           | 145.21          | 44.27         | 5.21     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 10 | 31   | 01 November 2025  |           | 99.72           | 45.49         | 3.99     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 11 | 30   | 01 December 2025  |           | 52.98           | 46.74         | 2.74     | 0.0  | 0.0       | 49.48 | 0.0  | 0.0        | 0.0  | 49.48       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 52.98         | 1.46     | 0.0  | 0.0       | 54.44 | 0.0  | 0.0        | 0.0  | 54.44       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 98.72    | 0.0  | 0.0       | 598.72 | 0.0  | 0.0        | 0.0  | 598.72      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    # Second disbursement - 15/01/2025 - 500 EUR
    And Admin successfully disburse the loan on "15 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 15 January 2025   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 922.92          | 77.08         | 21.29    | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 2  | 28   | 01 March 2025     |           | 852.05          | 70.87         | 27.5     | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 3  | 31   | 01 April 2025     |           | 781.18          | 70.87         | 27.5     | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 4  | 30   | 01 May 2025       |           | 706.1           | 75.08         | 23.29    | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 5  | 31   | 01 June 2025      |           | 627.15          | 78.95         | 19.42    | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 6  | 30   | 01 July 2025      |           | 546.03          | 81.12         | 17.25    | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 7  | 31   | 01 August 2025    |           | 462.68          | 83.35         | 15.02    | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 8  | 31   | 01 September 2025 |           | 377.03          | 85.65         | 12.72    | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 9  | 30   | 01 October 2025   |           | 289.03          | 88.0          | 10.37    | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 10 | 31   | 01 November 2025  |           | 198.61          | 90.42         | 7.95     | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 11 | 30   | 01 December 2025  |           | 105.7           | 92.91         | 5.46     | 0.0  | 0.0       | 98.37 | 0.0  | 0.0        | 0.0  | 98.37       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 105.7         | 2.91     | 0.0  | 0.0       | 108.61| 0.0  | 0.0        | 0.0  | 108.61      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 190.68   | 0.0  | 0.0       | 1190.68 | 0.0  | 0.0        | 0.0  | 1190.68     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 15 January 2025  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    # Apply merchant refund - 10/02/2025 - 100 EUR
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 February 2025" with 100 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      |    |      | 15 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 10 February 2025 | 922.92          | 77.08         | 21.29    | 0.0  | 0.0       | 98.37  | 98.37 | 0.0        | 98.37 | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 850.58          | 72.34         | 26.03    | 0.0  | 0.0       | 98.37  | 3.63  | 3.63       | 0.0   | 94.74       |
      | 3  | 31   | 01 April 2025     |                  | 777.55          | 73.03         | 25.34    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 4  | 30   | 01 May 2025       |                  | 701.72          | 75.83         | 22.54    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 5  | 31   | 01 June 2025      |                  | 622.6           | 79.12         | 19.25    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 6  | 30   | 01 July 2025      |                  | 541.31          | 81.29         | 17.08    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 7  | 31   | 01 August 2025    |                  | 457.78          | 83.53         | 14.84    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 8  | 31   | 01 September 2025 |                  | 371.95          | 85.83         | 12.54    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 9  | 30   | 01 October 2025   |                  | 283.76          | 88.19         | 10.18    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 10 | 31   | 01 November 2025  |                  | 193.15          | 90.61         | 7.76     | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 11 | 30   | 01 December 2025  |                  | 100.05          | 93.1          | 5.27     | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 100.05        | 2.71     | 0.0  | 0.0       | 102.76 | 1.63  | 1.63       | 0.0   | 101.13      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 184.83   | 0.0  | 0.0       | 1184.83 | 103.63 | 5.26       | 98.37 | 1081.2      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 15 January 2025  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 February 2025 | Merchant Issued Refund | 100.0  | 78.71     | 21.29    | 0.0  | 0.0       | 921.29       | false    | false    |
      | 10 February 2025 | Interest Refund        | 3.63   | 0.0       | 3.63     | 0.0  | 0.0       | 921.29       | false    | false    |
    # Apply interest refund - 01/03/2025
    When Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "01 March 2025" with 50 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      |    |      | 15 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 10 February 2025 | 922.92          | 77.08         | 21.29    | 0.0  | 0.0       | 98.37  | 98.37 | 0.0        | 98.37 | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 850.58          | 72.34         | 26.03    | 0.0  | 0.0       | 98.37  | 56.77 | 3.63       | 0.0   | 41.6        |
      | 3  | 31   | 01 April 2025     |                  | 776.17          | 74.41         | 23.96    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 4  | 30   | 01 May 2025       |                  | 699.9           | 76.27         | 22.1     | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 5  | 31   | 01 June 2025      |                  | 620.73          | 79.17         | 19.2     | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 6  | 30   | 01 July 2025      |                  | 539.39          | 81.34         | 17.03    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 7  | 31   | 01 August 2025    |                  | 455.81          | 83.58         | 14.79    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 8  | 31   | 01 September 2025 |                  | 369.93          | 85.88         | 12.49    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 9  | 30   | 01 October 2025   |                  | 281.69          | 88.24         | 10.13    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 10 | 31   | 01 November 2025  |                  | 191.02          | 90.67         | 7.7      | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 11 | 30   | 01 December 2025  |                  | 97.86           | 93.16         | 5.21     | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 97.86         | 2.65     | 0.0  | 0.0       | 100.51 | 1.63  | 1.63       | 0.0   | 98.88       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 182.58   | 0.0  | 0.0       | 1182.58 | 156.77 | 5.26       | 98.37 | 1025.81     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 15 January 2025  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 February 2025 | Merchant Issued Refund | 100.0  | 78.71     | 21.29    | 0.0  | 0.0       | 921.29       | false    | false    |
      | 10 February 2025 | Interest Refund        | 3.63   | 0.0       | 3.63     | 0.0  | 0.0       | 921.29       | false    | false    |
      | 01 March 2025    | Payout Refund          | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 871.29       | false    | false    |
      | 01 March 2025    | Interest Refund        | 3.14   | 0.0       | 3.14     | 0.0  | 0.0       | 871.29       | false    | false    |
    # Run inline COB job for loan
    When Admin runs inline COB job for Loan
    # Verify accrual entries adjust correctly for refunds
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      |    |      | 15 January 2025   |                  | 500.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025  | 10 February 2025 | 922.92          | 77.08         | 21.29    | 0.0  | 0.0       | 98.37  | 98.37 | 0.0        | 98.37 | 0.0         |
      | 2  | 28   | 01 March 2025     |                  | 850.58          | 72.34         | 26.03    | 0.0  | 0.0       | 98.37  | 56.77 | 3.63       | 0.0   | 41.6        |
      | 3  | 31   | 01 April 2025     |                  | 776.17          | 74.41         | 23.96    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 4  | 30   | 01 May 2025       |                  | 699.9           | 76.27         | 22.1     | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 5  | 31   | 01 June 2025      |                  | 620.73          | 79.17         | 19.2     | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 6  | 30   | 01 July 2025      |                  | 539.39          | 81.34         | 17.03    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 7  | 31   | 01 August 2025    |                  | 455.81          | 83.58         | 14.79    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 8  | 31   | 01 September 2025 |                  | 369.93          | 85.88         | 12.49    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 9  | 30   | 01 October 2025   |                  | 281.69          | 88.24         | 10.13    | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 10 | 31   | 01 November 2025  |                  | 191.02          | 90.67         | 7.7      | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 11 | 30   | 01 December 2025  |                  | 97.86           | 93.16         | 5.21     | 0.0  | 0.0       | 98.37  | 0.0   | 0.0        | 0.0   | 98.37       |
      | 12 | 31   | 01 January 2026   |                  | 0.0             | 97.86         | 2.65     | 0.0  | 0.0       | 100.51 | 1.63  | 1.63       | 0.0   | 98.88       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 182.58   | 0.0  | 0.0       | 1182.58 | 156.77 | 5.26       | 98.37 | 1025.81     |
    # Verify loan schedule reflects refunded amounts and accrual entries
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 15 January 2025  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Accrual Activity       | 21.29  | 0.0       | 21.29    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Merchant Issued Refund | 100.0  | 78.71     | 21.29    | 0.0  | 0.0       | 921.29       | false    | false    |
      | 10 February 2025 | Interest Refund        | 3.63   | 0.0       | 3.63     | 0.0  | 0.0       | 921.29       | false    | false    |
      | 01 March 2025    | Payout Refund          | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 871.29       | false    | false    |
      | 01 March 2025    | Interest Refund        | 3.14   | 0.0       | 3.14     | 0.0  | 0.0       | 871.29       | false    | false    |
      | 01 March 2025    | Accrual Activity       | 26.03  | 0.0       | 26.03    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual Activity       | 23.96  | 0.0       | 23.96    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual                | 77.67  | 0.0       | 77.67    | 0.0  | 0.0       | 0.0          | false    | false    |
    # Verify loan status remains active
    Then Loan status will be "ACTIVE"
    When Loan Pay-off is made on "10 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4482
  Scenario: Verify backdated loan migration spanning leap year boundary
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date  | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 15 December 2024   | 1000           | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "15 December 2024" with "1000" amount and expected disbursement date on "15 December 2024"
    And Admin successfully disburse the loan on "15 December 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 15 December 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 15 January 2025   |           | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 2  | 31   | 15 February 2025  |           | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 3  | 28   | 15 March 2025     |           | 785.59          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 4  | 31   | 15 April 2025     |           | 713.17          | 72.42         | 26.55    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 5  | 30   | 15 May 2025       |           | 633.81          | 79.36         | 19.61    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 6  | 31   | 15 June 2025      |           | 552.27          | 81.54         | 17.43    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 7  | 30   | 15 July 2025      |           | 468.49          | 83.78         | 15.19    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 15 August 2025    |           | 382.4           | 86.09         | 12.88    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 9  | 31   | 15 September 2025 |           | 293.95          | 88.45         | 10.52    | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 10 | 30   | 15 October 2025   |           | 203.06          | 90.89         | 8.08     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 11 | 31   | 15 November 2025  |           | 109.67          | 93.39         | 5.58     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 12 | 30   | 15 December 2025  |           | 0.0             | 109.67        | 3.02     | 0.0  | 0.0       | 112.69 | 0.0  | 0.0        | 0.0  | 112.69      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 201.36   | 0.0  | 0.0       | 1201.36 | 0.0  | 0.0        | 0.0  | 1201.36     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 15 December 2024 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    # Make backdated payment on 15 January 2025 (first installment due date)
    And Customer makes "AUTOPAY" repayment on "15 January 2025" with 98.97 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 15 December 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 15 January 2025   | 15 January 2025  | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 15 February 2025  |                  | 855.09          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 3  | 28   | 15 March 2025     |                  | 781.65          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 4  | 31   | 15 April 2025     |                  | 707.56          | 74.09         | 24.88    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 5  | 30   | 15 May 2025       |                  | 628.05          | 79.51         | 19.46    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 6  | 31   | 15 June 2025      |                  | 546.35          | 81.7          | 17.27    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 7  | 30   | 15 July 2025      |                  | 462.4           | 83.95         | 15.02    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 15 August 2025    |                  | 376.15          | 86.25         | 12.72    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 9  | 31   | 15 September 2025 |                  | 287.52          | 88.63         | 10.34    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 10 | 30   | 15 October 2025   |                  | 196.46          | 91.06         | 7.91     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 11 | 31   | 15 November 2025  |                  | 102.89          | 93.57         | 5.4      | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0  | 98.97       |
      | 12 | 30   | 15 December 2025  |                  | 0.0             | 102.89        | 2.83     | 0.0  | 0.0       | 105.72 | 0.0   | 0.0        | 0.0  | 105.72      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 194.39   | 0.0  | 0.0       | 1194.39 | 98.97 | 0.0        | 0.0  | 1095.42     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 15 December 2024 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2025  | Repayment        | 98.97  | 71.47     | 27.5     | 0.0  | 0.0       | 928.53       | false    | false    |
    # Make backdated payment on 15 March 2025 (third installment - crosses February boundary)
    And Customer makes "AUTOPAY" repayment on "15 March 2025" with 98.97 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 15 December 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 15 January 2025   | 15 January 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 15 February 2025  | 15 March 2025   | 855.09          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 98.97 | 0.0         |
      | 3  | 28   | 15 March 2025     |                 | 781.65          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 4  | 31   | 15 April 2025     |                 | 705.87          | 75.78         | 23.19    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 5  | 30   | 15 May 2025       |                 | 626.31          | 79.56         | 19.41    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 6  | 31   | 15 June 2025      |                 | 544.56          | 81.75         | 17.22    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 7  | 30   | 15 July 2025      |                 | 460.57          | 83.99         | 14.98    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 8  | 31   | 15 August 2025    |                 | 374.27          | 86.3          | 12.67    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 9  | 31   | 15 September 2025 |                 | 285.59          | 88.68         | 10.29    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 10 | 30   | 15 October 2025   |                 | 194.47          | 91.12         | 7.85     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 11 | 31   | 15 November 2025  |                 | 100.85          | 93.62         | 5.35     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 12 | 30   | 15 December 2025  |                 | 0.0             | 100.85        | 2.77     | 0.0  | 0.0       | 103.62 | 0.0   | 0.0        | 0.0   | 103.62      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 192.29   | 0.0  | 0.0       | 1192.29 | 197.94 | 0.0        | 98.97 | 994.35      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 15 December 2024 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2025  | Repayment        | 98.97  | 71.47     | 27.5     | 0.0  | 0.0       | 928.53       | false    | false    |
      | 15 March 2025    | Repayment        | 98.97  | 73.44     | 25.53    | 0.0  | 0.0       | 855.09       | false    | false    |
    # Run inline COB job for loan
    When Admin runs inline COB job for Loan
    # Verify interest calculations handle leap year boundary correctly with 360/30 day count
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 15 December 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 15 January 2025   | 15 January 2025 | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 15 February 2025  | 15 March 2025   | 855.09          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 98.97 | 0.0        | 98.97 | 0.0         |
      | 3  | 28   | 15 March 2025     |                 | 781.65          | 73.44         | 25.53    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 4  | 31   | 15 April 2025     |                 | 705.87          | 75.78         | 23.19    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 5  | 30   | 15 May 2025       |                 | 626.31          | 79.56         | 19.41    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 6  | 31   | 15 June 2025      |                 | 544.56          | 81.75         | 17.22    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 7  | 30   | 15 July 2025      |                 | 460.57          | 83.99         | 14.98    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 8  | 31   | 15 August 2025    |                 | 374.27          | 86.3          | 12.67    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 9  | 31   | 15 September 2025 |                 | 285.59          | 88.68         | 10.29    | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 10 | 30   | 15 October 2025   |                 | 194.47          | 91.12         | 7.85     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 11 | 31   | 15 November 2025  |                 | 100.85          | 93.62         | 5.35     | 0.0  | 0.0       | 98.97  | 0.0   | 0.0        | 0.0   | 98.97       |
      | 12 | 30   | 15 December 2025  |                 | 0.0             | 100.85        | 2.77     | 0.0  | 0.0       | 103.62 | 0.0   | 0.0        | 0.0   | 103.62      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 192.29   | 0.0  | 0.0       | 1192.29 | 197.94 | 0.0        | 98.97 | 994.35      |
    # Verify all accrual entries and accrual activities are generated with correct amounts
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 15 December 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2025   | Repayment        | 98.97  | 71.47     | 27.5     | 0.0  | 0.0       | 928.53       | false    | false    |
      | 15 January 2025   | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025  | Accrual Activity | 25.53  | 0.0       | 25.53    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2025     | Repayment        | 98.97  | 73.44     | 25.53    | 0.0  | 0.0       | 855.09       | false    | false    |
      | 15 March 2025     | Accrual Activity | 25.53  | 0.0       | 25.53    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025     | Accrual          | 97.52  | 0.0       | 97.52    | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "ACTIVE"
    When Loan Pay-off is made on "10 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4483
  Scenario: Verify backdated loan migration with large number of past due installments (all 12 months overdue)
    When Admin sets the business date to "10 April 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 1000           | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024  |           | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 2  | 29   | 01 March 2024     |           | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 3  | 31   | 01 April 2024     |           | 785.59          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 4  | 30   | 01 May 2024       |           | 714.12          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2024      |           | 642.65          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2024      |           | 571.18          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2024    |           | 499.71          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2024 |           | 428.24          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2024   |           | 356.77          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2024  |           | 285.3           | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2024  |           | 213.83          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2025   |           | 0.0             | 213.83        | 27.5     | 0.0  | 0.0       | 241.33 | 0.0  | 0.0        | 0.0  | 241.33      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 330.0    | 0.0  | 0.0       | 1330.0  | 0.0  | 0.0        | 0.0  | 1330.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    # No payments made - loan remains fully unpaid
    # Run inline COB job for loan to generate all past due accrual activities
    When Admin runs inline COB job for Loan
    # Verify all past periods (12+ months) have correct accrual activities
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024  |           | 928.53          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 2  | 29   | 01 March 2024     |           | 857.06          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 3  | 31   | 01 April 2024     |           | 785.59          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 4  | 30   | 01 May 2024       |           | 714.12          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 5  | 31   | 01 June 2024      |           | 642.65          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 6  | 30   | 01 July 2024      |           | 571.18          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 7  | 31   | 01 August 2024    |           | 499.71          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 8  | 31   | 01 September 2024 |           | 428.24          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 9  | 30   | 01 October 2024   |           | 356.77          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 10 | 31   | 01 November 2024  |           | 285.3           | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 11 | 30   | 01 December 2024  |           | 213.83          | 71.47         | 27.5     | 0.0  | 0.0       | 98.97  | 0.0  | 0.0        | 0.0  | 98.97       |
      | 12 | 31   | 01 January 2025   |           | 0.0             | 213.83        | 27.5     | 0.0  | 0.0       | 241.33 | 0.0  | 0.0        | 0.0  | 241.33      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 330.0    | 0.0  | 0.0       | 1330.0  | 0.0  | 0.0        | 0.0  | 1330.0      |
    # Verify aggregated accrual is accurate - all 12 accrual activities generated for each past due period
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2024  | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024     | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024     | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2024       | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 June 2024      | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024      | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 August 2024    | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 September 2024 | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 October 2024   | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 November 2024  | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 December 2024  | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025   | Accrual          | 330.0  | 0.0       | 330.0    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025   | Accrual Activity | 27.5   | 0.0       | 27.5     | 0.0  | 0.0       | 0.0          | false    | false    |
    # Verify delinquency reflects total overdue amount
    Then Loan status will be "ACTIVE"
    # All installments are past due - total overdue should be the full loan amount
    Then Loan has 1330.0 total overdue amount
    When Loan Pay-off is made on "10 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
