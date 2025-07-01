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
