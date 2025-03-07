@LoanAccrualActivityFeature
Feature: LoanAccrualActivity

  @TestRailId:C3168
  Scenario: Verify accrual activity - UC1: No payment, advanced payment strategy
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"

  @TestRailId:C3169
  Scenario: Verify accrual activity - UC2: No payment, advanced payment strategy, charges added to installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 January 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "06 January 2024" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 25.33  | 0.0       | 0.33     | 10.0 | 15.0      | 0.0          |
      | 06 January 2024  | Accrual Activity | 26.64  | 0.0       | 1.64     | 10.0 | 15.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"

  @TestRailId:C3170
  Scenario: Verify accrual activity - UC3: No payment, advanced payment strategy, charges added after next installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "07 January 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "07 January 2024" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"
    When Admin sets the business date to "08 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
      | 07 January 2024  | Accrual          | 25.25  | 0.0       | 0.25     | 10.0 | 15.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "07 January 2024"

  @TestRailId:C3171
  Scenario: Verify accrual activity - UC4: No payment, cumulative payment strategy
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"

  @TestRailId:C3172
  Scenario: Verify accrual activity - UC5: No payment, cumulative payment strategy, charges added to installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 January 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "06 January 2024" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 25.33  | 0.0       | 0.33     | 10.0 | 15.0      | 0.0          |
      | 06 January 2024  | Accrual Activity | 26.64  | 0.0       | 1.64     | 10.0 | 15.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"

  @TestRailId:C3173
  Scenario: Verify accrual activity - UC6: No payment, cumulative payment strategy, charges added after next installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "07 January 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "07 January 2024" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"
    When Admin sets the business date to "08 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
      | 07 January 2024  | Accrual          | 25.25  | 0.0       | 0.25     | 10.0 | 15.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "07 January 2024"

  @TestRailId:C3174
  Scenario: Verify accrual activity - UC7: Payment in time, advanced payment strategy
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 251.03 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 251.03 | 0.0        | 0.0  | 753.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 251.03 | 0.0        | 0.0  | 753.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"

  @TestRailId:C3175
  Scenario: Verify accrual activity - UC8: Payment in time, advanced payment strategy, charges added to installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 January 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "06 January 2024" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 276.03 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.61          | 249.39        | 1.64     | 10.0 | 15.0      | 276.03 | 276.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 276.03 | 0.0        | 0.0  | 753.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 276.03 | 249.39    | 1.64     | 10.0 | 15.0      | 750.61       |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.61          | 249.39        | 1.64     | 10.0 | 15.0      | 276.03 | 276.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 276.03 | 0.0        | 0.0  | 753.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 276.03 | 249.39    | 1.64     | 10.0 | 15.0      | 750.61       |
      | 06 January 2024  | Accrual          | 25.33  | 0.0       | 0.33     | 10.0 | 15.0      | 0.0          |
      | 06 January 2024  | Accrual Activity | 26.64  | 0.0       | 1.64     | 10.0 | 15.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"

  @TestRailId:C3176
  Scenario: Verify accrual activity - UC9: Payment in time, advanced payment strategy, charges added after next installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "07 January 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "07 January 2024" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 10.0 | 15.0      | 276.03 | 0.0  | 0.0        | 0.0  | 276.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 251.03 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 10.0 | 15.0      | 276.03 | 0.0    | 0.0        | 0.0  | 276.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 251.03 | 0.0        | 0.0  | 778.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 10.0 | 15.0      | 276.03 | 0.0    | 0.0        | 0.0  | 276.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 251.03 | 0.0        | 0.0  | 778.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"
    When Admin sets the business date to "08 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 10.0 | 15.0      | 276.03 | 0.0    | 0.0        | 0.0  | 276.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 251.03 | 0.0        | 0.0  | 778.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
      | 07 January 2024  | Accrual          | 25.25  | 0.0       | 0.25     | 10.0 | 15.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "07 January 2024"

  @TestRailId:C3177
  Scenario: Verify accrual activity - UC10: Payment in time, cumulative payment strategy
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 251 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 251.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |                 | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 251.0 | 0.0        | 0.0  | 753.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.0  | 249.36    | 1.64     | 0.0  | 0.0       | 750.64       |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 251.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |                 | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 251.0 | 0.0        | 0.0  | 753.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.0  | 249.36    | 1.64     | 0.0  | 0.0       | 750.64       |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"

  @TestRailId:C3178
  Scenario: Verify accrual activity - UC11: Payment in time, cumulative payment strategy, charges added to installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 January 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "06 January 2024" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 276 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 276.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |                 | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 276.0 | 0.0        | 0.0  | 753.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 276.0  | 249.36    | 1.64     | 10.0 | 15.0      | 750.64       |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 276.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |                 | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 276.0 | 0.0        | 0.0  | 753.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 276.0  | 249.36    | 1.64     | 10.0 | 15.0      | 750.64       |
      | 06 January 2024  | Accrual          | 25.33  | 0.0       | 0.33     | 10.0 | 15.0      | 0.0          |
      | 06 January 2024  | Accrual Activity | 26.64  | 0.0       | 1.64     | 10.0 | 15.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"

  @TestRailId:C3179
  Scenario: Verify accrual activity - UC12: Payment in time, cumulative payment strategy, charges added after next installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "07 January 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "07 January 2024" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2024"
    When Admin sets the business date to "06 January 2024"
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 251 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 251.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.87          | 249.77        | 1.23     | 10.0 | 15.0      | 276.0 | 0.0   | 0.0        | 0.0  | 276.0       |
      | 3  | 5    | 16 January 2024 |                 | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 251.0 | 0.0        | 0.0  | 778.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.0  | 249.36    | 1.64     | 0.0  | 0.0       | 750.64       |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "03 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "04 January 2024"
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2024"
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 251.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.87          | 249.77        | 1.23     | 10.0 | 15.0      | 276.0 | 0.0   | 0.0        | 0.0  | 276.0       |
      | 3  | 5    | 16 January 2024 |                 | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 251.0 | 0.0        | 0.0  | 778.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.0  | 249.36    | 1.64     | 0.0  | 0.0       | 750.64       |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "06 January 2024"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "06 January 2024"
    When Admin sets the business date to "08 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 | 06 January 2024 | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 251.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.87          | 249.77        | 1.23     | 10.0 | 15.0      | 276.0 | 0.0   | 0.0        | 0.0  | 276.0       |
      | 3  | 5    | 16 January 2024 |                 | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 251.0 | 0.0        | 0.0  | 778.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Repayment        | 251.0  | 249.36    | 1.64     | 0.0  | 0.0       | 750.64       |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
      | 07 January 2024  | Accrual          | 25.25  | 0.0       | 0.25     | 10.0 | 15.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "07 January 2024"

  @TestRailId:C3185
  Scenario: Verify accrual activity - UC13: Preclose, loan account fully paid before first installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 1004.1 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 04 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 | 04 January 2024 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 3  | 5    | 16 January 2024 | 04 January 2024 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 4  | 5    | 21 January 2024 | 04 January 2024 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 251.01 | 251.01     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 1004.1 | 1004.1     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2024  | Repayment        | 1004.1 | 1000.0    | 4.1      | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual Activity | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3186
  Scenario: Verify accrual activity - UC14: Preclose, loan account overpaid before first installment due date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 1100 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 95.9 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 04 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 | 04 January 2024 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 3  | 5    | 16 January 2024 | 04 January 2024 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 4  | 5    | 21 January 2024 | 04 January 2024 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 251.01 | 251.01     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 1004.1 | 1004.1     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2024  | Repayment        | 1100.0 | 1000.0    | 4.1      | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual Activity | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3187
  Scenario: Verify accrual activity - UC15: Preclose, loan account fully paid before first installment due date, reopen
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
#    --- Loan account pprepaid fully ---
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 1004.1 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 04 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 | 04 January 2024 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 3  | 5    | 16 January 2024 | 04 January 2024 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 4  | 5    | 21 January 2024 | 04 January 2024 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 251.01 | 251.01     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 1004.1 | 1004.1     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2024  | Repayment        | 1004.1 | 1000.0    | 4.1      | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual Activity | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          |
#    --- Repayment undo ---
    When Admin sets the business date to "05 January 2024"
    When Customer undo "1"th repayment on "04 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan status will be "ACTIVE"
    Then Loan has 1004.1 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 04 January 2024  | Repayment        | 1004.1 | 1000.0    | 4.1      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 04 January 2024  | Accrual          | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          | false    | false    |
#    --- Accrual activity ---
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 04 January 2024  | Accrual            | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Repayment          | 1004.1 | 1000.0    | 4.1      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 January 2024  | Accrual Adjustment | 2.79   | 0.0       | 2.79     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual            | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity   | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3188
  Scenario: Verify accrual activity - UC16: Preclose, loan account partially paid before first installment due date, fully paid after first installment date, reopen by undo 1st repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
#    --- Loan account pprepaid partially ---
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 251.03 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 04 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 251.03 | 251.03     | 0.0  | 753.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       |
    #    --- Accrual activity ---
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 04 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 |                 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0    | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 251.03 | 251.03     | 0.0  | 753.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
#   --- Fully repaid ---
    When Admin sets the business date to "08 January 2024"
    And Customer makes "AUTOPAY" repayment on "08 January 2024" with 753.07 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 5    | 06 January 2024 | 04 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 2  | 5    | 11 January 2024 | 08 January 2024 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 3  | 5    | 16 January 2024 | 08 January 2024 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0  | 0.0         |
      | 4  | 5    | 21 January 2024 | 08 January 2024 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 251.01 | 251.01     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 1004.1 | 1004.1     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
      | 08 January 2024  | Repayment        | 753.07 | 750.61    | 2.46     | 0.0  | 0.0       | 0.0          |
      | 08 January 2024  | Accrual          | 2.46   | 0.0       | 2.46     | 0.0  | 0.0       | 0.0          |
      | 08 January 2024  | Accrual Activity | 2.46   | 0.0       | 2.46     | 0.0  | 0.0       | 0.0          |
#    --- Repayment undo ---
    When Admin sets the business date to "09 January 2024"
    When Customer undo "1"th repayment on "04 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan status will be "ACTIVE"
    Then Loan has 251.03 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2024 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 5    | 06 January 2024 | 08 January 2024 | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 251.03 | 0.0        | 251.03 | 0.0         |
      | 2  | 5    | 11 January 2024 | 08 January 2024 | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 251.03 | 251.03     | 0.0    | 0.0         |
      | 3  | 5    | 16 January 2024 |                 | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 251.01 | 251.01     | 0.0    | 0.02        |
      | 4  | 5    | 21 January 2024 |                 | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0    | 0.0        | 0.0    | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 753.07 | 502.04     | 251.03 | 251.03      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Repayment        | 251.03 | 249.39    | 1.64     | 0.0  | 0.0       | 750.61       | true     | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Repayment        | 753.07 | 749.4     | 3.67     | 0.0  | 0.0       | 250.6        | false    | true     |
      | 08 January 2024  | Accrual          | 2.46   | 0.0       | 2.46     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3182
  Scenario: Verify accrual activity posting job
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "07 January 2024"
    When Admin runs the Accrual Activity Posting job
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.61          | 249.39        | 1.64     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 2  | 5    | 11 January 2024 |           | 500.81          | 249.8         | 1.23     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 3  | 5    | 16 January 2024 |           | 250.6           | 250.21        | 0.82     | 0.0  | 0.0       | 251.03 | 0.0  | 0.0        | 0.0  | 251.03      |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.6         | 0.41     | 0.0  | 0.0       | 251.01 | 0.0  | 0.0        | 0.0  | 251.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3183
  Scenario: Verify accrual activity reverse/replay - UC01: Backdated fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "03 January 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 January 2024" due date and 10 EUR transaction amount
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 0.0       | 261.0 | 0.0  | 0.0        | 0.0  | 261.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 0.0       | 1014.1 | 0.0  | 0.0        | 0.0  | 1014.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 10.33  | 0.0       | 0.33     | 10.0 | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 11.64  | 0.0       | 1.64     | 10.0 | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "08 January 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "04 January 2024" due date and 15 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 0.0  | 0.0        | 0.0  | 276.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 0.0  | 0.0        | 0.0  | 1029.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 10.33  | 0.0       | 0.33     | 10.0 | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 26.64  | 0.0       | 1.64     | 10.0 | 15.0      | 0.0          | false    | true     |

  @TestRailId:C3184
  Scenario: Verify accrual activity reverse/replay - UC02: Early payment, charge, backdated payment, backdated charge, repayment reversal
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
#    --- Charge added ---
    When Admin sets the business date to "03 January 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 January 2024" due date and 10 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 0.0       | 261.0 | 0.0  | 0.0        | 0.0  | 261.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 0.0       | 1014.1 | 0.0  | 0.0        | 0.0  | 1014.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
#    --- Early partial payment ---
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 150 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 0.0       | 261.0 | 150.0 | 150.0      | 0.0  | 111.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 0.0       | 1014.1 | 150.0 | 150.0      | 0.0  | 864.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Repayment        | 150.0  | 138.36    | 1.64     | 10.0 | 0.0       | 861.64       | false    | false    |
#   --- Accrual activity transaction ---
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 0.0       | 261.0 | 150.0 | 150.0      | 0.0  | 111.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 0.0       | 1014.1 | 150.0 | 150.0      | 0.0  | 864.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Repayment        | 150.0  | 138.36    | 1.64     | 10.0 | 0.0       | 861.64       | false    | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 10.33  | 0.0       | 0.33     | 10.0 | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 11.64  | 0.0       | 1.64     | 10.0 | 0.0       | 0.0          | false    | false    |
#   --- Backdated payment ---
    When Admin sets the business date to "08 January 2024"
    And Customer makes "AUTOPAY" repayment on "05 January 2024" with 80 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 0.0       | 261.0 | 230.0 | 230.0      | 0.0  | 31.0        |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 0.0       | 1014.1 | 230.0 | 230.0      | 0.0  | 784.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Repayment        | 150.0  | 138.36    | 1.64     | 10.0 | 0.0       | 861.64       | false    | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Repayment        | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 781.64       | false    | false    |
      | 06 January 2024  | Accrual          | 10.33  | 0.0       | 0.33     | 10.0 | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 11.64  | 0.0       | 1.64     | 10.0 | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Backdated charge ---
    When Admin sets the business date to "09 January 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 15 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 230.0 | 230.0      | 0.0  | 46.0        |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0   | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 230.0 | 230.0      | 0.0  | 799.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Repayment        | 150.0  | 123.36    | 1.64     | 10.0 | 15.0      | 876.64       | false    | true     |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Repayment        | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 796.64       | false    | false    |
      | 06 January 2024  | Accrual          | 10.33  | 0.0       | 0.33     | 10.0 | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 26.64  | 0.0       | 1.64     | 10.0 | 15.0      | 0.0          | false    | true     |
      | 06 January 2024  | Accrual          | 15.0   | 0.0       | 0.0      | 0.0  | 15.0      | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Repayment reversal ---
    When Admin sets the business date to "10 January 2024"
    When Customer undo "1"th repayment on "04 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 10.0 | 15.0      | 276.0 | 80.0 | 80.0       | 0.0  | 196.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 10.0 | 15.0      | 1029.1 | 80.0 | 80.0       | 0.0  | 949.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Repayment        | 150.0  | 123.36    | 1.64     | 10.0 | 15.0      | 876.64       | true     | true     |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Repayment        | 80.0   | 53.36     | 1.64     | 10.0 | 15.0      | 946.64       | false    | true     |
      | 06 January 2024  | Accrual          | 10.33  | 0.0       | 0.33     | 10.0 | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 26.64  | 0.0       | 1.64     | 10.0 | 15.0      | 0.0          | false    | true     |
      | 06 January 2024  | Accrual          | 15.0   | 0.0       | 0.0      | 0.0  | 15.0      | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.49   | 0.0       | 0.49     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3189
  Scenario: Verify accrual activity reverse/replay - UC03: Backdated repayment with interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0  | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.96          | 249.68        | 1.32     | 0.0  | 0.0       | 251.0  | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.78          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0  | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.78        | 0.41     | 0.0  | 0.0       | 251.19 | 0.0  | 0.0        | 0.0  | 251.19      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.19     | 0.0  | 0.0       | 1004.19 | 0.0  | 0.0        | 0.0  | 1004.19     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "08 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 150 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.55          | 249.45        | 1.55     | 0.0  | 0.0       | 251.0  | 150.0 | 150.0      | 0.0  | 101.0       |
      | 2  | 5    | 11 January 2024 |           | 500.85          | 249.7         | 1.3      | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.67          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.67        | 0.41     | 0.0  | 0.0       | 251.08 | 0.0   | 0.0        | 0.0  | 251.08      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.08     | 0.0  | 0.0       | 1004.08 | 150.0 | 150.0      | 0.0  | 854.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Repayment        | 150.0  | 148.45    | 1.55     | 0.0  | 0.0       | 851.55       | false    | false    |
      | 05 January 2024  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 1.55   | 0.0       | 1.55     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 06 January 2024  | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3190
  Scenario: Verify accrual activity reverse/replay - UC04: Early repayment with interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "03 January 2024"
    And Customer makes "AUTOPAY" repayment on "03 January 2024" with 150 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.5           | 249.5         | 1.5      | 0.0  | 0.0       | 251.0  | 150.0 | 150.0      | 0.0  | 101.0       |
      | 2  | 5    | 11 January 2024 |           | 500.73          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.55          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.55        | 0.41     | 0.0  | 0.0       | 250.96 | 0.0   | 0.0        | 0.0  | 250.96      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 3.96     | 0.0  | 0.0       | 1003.96 | 150.0 | 150.0      | 0.0  | 853.96      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Repayment        | 150.0  | 148.5     | 1.5      | 0.0  | 0.0       | 851.5        | false    | false    |
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.5           | 249.5         | 1.5      | 0.0  | 0.0       | 251.0 | 150.0 | 150.0      | 0.0  | 101.0       |
      | 2  | 5    | 11 January 2024 |           | 500.77          | 249.73        | 1.27     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.59          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.59        | 0.41     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.0      | 0.0  | 0.0       | 1004.0 | 150.0 | 150.0      | 0.0  | 854.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Repayment        | 150.0  | 148.5     | 1.5      | 0.0  | 0.0       | 851.5        | false    | false    |
      | 03 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 1.5    | 0.0       | 1.5      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3191
  Scenario: Verify accrual activity reverse/replay - UC04: Early repayment reversed after due date with interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 500.87          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.69          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.69        | 0.41     | 0.0  | 0.0       | 251.1 | 0.0  | 0.0        | 0.0  | 251.1       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.1      | 0.0  | 0.0       | 1004.1 | 0.0  | 0.0        | 0.0  | 1004.1      |
    When Admin sets the business date to "03 January 2024"
    And Customer makes "AUTOPAY" repayment on "03 January 2024" with 150 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.5           | 249.5         | 1.5      | 0.0  | 0.0       | 251.0  | 150.0 | 150.0      | 0.0  | 101.0       |
      | 2  | 5    | 11 January 2024 |           | 500.73          | 249.77        | 1.23     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.55          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.55        | 0.41     | 0.0  | 0.0       | 250.96 | 0.0   | 0.0        | 0.0  | 250.96      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 3.96     | 0.0  | 0.0       | 1003.96 | 150.0 | 150.0      | 0.0  | 853.96      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Repayment        | 150.0  | 148.5     | 1.5      | 0.0  | 0.0       | 851.5        | false    | false    |
    When Admin sets the business date to "07 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.5           | 249.5         | 1.5      | 0.0  | 0.0       | 251.0 | 150.0 | 150.0      | 0.0  | 101.0       |
      | 2  | 5    | 11 January 2024 |           | 500.77          | 249.73        | 1.27     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.59          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.59        | 0.41     | 0.0  | 0.0       | 251.0 | 0.0   | 0.0        | 0.0  | 251.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.0      | 0.0  | 0.0       | 1004.0 | 150.0 | 150.0      | 0.0  | 854.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Repayment        | 150.0  | 148.5     | 1.5      | 0.0  | 0.0       | 851.5        | false    | false    |
      | 03 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 1.5    | 0.0       | 1.5      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "08 January 2024"
    When Customer undo "1"th repayment on "03 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.64          | 249.36        | 1.64     | 0.0  | 0.0       | 251.0  | 0.0  | 0.0        | 0.0  | 251.0       |
      | 2  | 5    | 11 January 2024 |           | 501.04          | 249.6         | 1.4      | 0.0  | 0.0       | 251.0  | 0.0  | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.86          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0  | 0.0  | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.86        | 0.41     | 0.0  | 0.0       | 251.27 | 0.0  | 0.0        | 0.0  | 251.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 4.27     | 0.0  | 0.0       | 1004.27 | 0.0  | 0.0        | 0.0  | 1004.27     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Repayment        | 150.0  | 148.5     | 1.5      | 0.0  | 0.0       | 851.5        | true     | false    |
      | 03 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual Activity | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 06 January 2024  | Accrual          | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3268
  Scenario: Verify reversal of accruals when repayment got reversed
    When Admin sets the business date to "22 April 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 22 April 2024     | 400            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "22 April 2024" with "400" amount and expected disbursement date on "12 August 2024"
    When Admin successfully disburse the loan on "22 April 2024" with "400" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 22 April 2024     |           | 400.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 30   | 22 May 2024       |           | 333.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 2  | 31   | 22 June 2024      |           | 266.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 3  | 30   | 22 July 2024      |           | 199.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 4  | 31   | 22 August 2024    |           | 132.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 5  | 31   | 22 September 2024 |           | 65.0            | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 6  | 30   | 22 October 2024   |           | 0.0             | 65.0          | 0.0      | 0.0  | 0.0       | 65.0 | 0.0  | 0.0        | 0.0  | 65.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 April 2024    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
    And Customer makes "AUTOPAY" repayment on "22 April 2024" with 600 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Admin adds "LOAN_NSF_FEE" due date charge with "22 April 2024" due date and 30 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 22 April 2024 | Flat             | 30.0 | 30.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 22 April 2024     |               | 400.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 30   | 22 May 2024       | 22 April 2024 | 333.0           | 67.0          | 0.0      | 0.0  | 30.0      | 97.0 | 97.0 | 97.0       | 0.0  | 0.0         |
      | 2  | 31   | 22 June 2024      | 22 April 2024 | 266.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 67.0 | 67.0       | 0.0  | 0.0         |
      | 3  | 30   | 22 July 2024      | 22 April 2024 | 199.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 67.0 | 67.0       | 0.0  | 0.0         |
      | 4  | 31   | 22 August 2024    | 22 April 2024 | 132.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 67.0 | 67.0       | 0.0  | 0.0         |
      | 5  | 31   | 22 September 2024 | 22 April 2024 | 65.0            | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 67.0 | 67.0       | 0.0  | 0.0         |
      | 6  | 30   | 22 October 2024   | 22 April 2024 | 0.0             | 65.0          | 0.0      | 0.0  | 0.0       | 65.0 | 65.0 | 65.0       | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 400.0         | 0.0      | 0.0  | 30.0      | 430.0 | 430.0 | 430.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 April 2024    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
      | 22 April 2024    | Accrual          | 30.0   | 0.0       | 0.0      | 0.0  | 30.0      | 0.0          | false    | false    |
      | 22 April 2024    | Repayment        | 600.0  | 400.0     | 0.0      | 0.0  | 30.0      | 0.0          | false    | true     |
    When Admin sets the business date to "10 October 2024"
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "22 April 2024" with 15 EUR transaction amount and externalId ""
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "22 April 2024" with 15 EUR transaction amount and self-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 April 2024    | Disbursement      | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
      | 22 April 2024    | Accrual           | 30.0   | 0.0       | 0.0      | 0.0  | 30.0      | 0.0          | false    | false    |
      | 22 April 2024    | Repayment         | 600.0  | 400.0     | 0.0      | 0.0  | 30.0      | 0.0          | false    | true     |
      | 22 April 2024    | Goodwill Credit   | 15.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 October 2024  | Charge Adjustment | 15.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer undo "1"th "Repayment" transaction made on "22 April 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 22 April 2024     |           | 400.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 30   | 22 May 2024       |           | 333.0           | 67.0          | 0.0      | 0.0  | 30.0      | 97.0 | 30.0 | 15.0       | 15.0 | 67.0        |
      | 2  | 31   | 22 June 2024      |           | 266.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 3  | 30   | 22 July 2024      |           | 199.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 4  | 31   | 22 August 2024    |           | 132.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 5  | 31   | 22 September 2024 |           | 65.0            | 67.0          | 0.0      | 0.0  | 0.0       | 67.0 | 0.0  | 0.0        | 0.0  | 67.0        |
      | 6  | 30   | 22 October 2024   |           | 0.0             | 65.0          | 0.0      | 0.0  | 0.0       | 65.0 | 0.0  | 0.0        | 0.0  | 65.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 400.0         | 0.0      | 0.0  | 30.0      | 430.0 | 30.0 | 15.0       | 15.0 | 400.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 April 2024    | Disbursement      | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
      | 22 April 2024    | Accrual           | 30.0   | 0.0       | 0.0      | 0.0  | 30.0      | 0.0          | false    | false    |
      | 22 April 2024    | Repayment         | 600.0  | 400.0     | 0.0      | 0.0  | 30.0      | 0.0          | true     | true     |
      | 22 April 2024    | Goodwill Credit   | 15.0   | 0.0       | 0.0      | 0.0  | 15.0      | 400.0        | false    | true     |
      | 10 October 2024  | Charge Adjustment | 15.0   | 0.0       | 0.0      | 0.0  | 15.0      | 400.0        | false    | true     |

  @TestRailId:C3274
  Scenario: Verify accrual activity for repayment reversal on the progressive loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "14 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "15 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer makes "AUTOPAY" repayment on "15 January 2024" with 263.69 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 746.09          | 253.91        | 9.78     | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 507.44          | 238.65        | 25.04    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 254.74          | 252.7         | 10.99    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 254.74        | 5.52     | 0.0  | 0.0       | 260.26 | 0.0    | 0.0        | 0.0  | 260.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 51.33    | 0.0  | 0.0       | 1051.33 | 263.69 | 263.69     | 0.0  | 787.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
    When Admin sets the business date to "21 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer undo "1"th repayment on "15 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | true     | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "23 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | true     | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 1.59   | 0.0       | 1.59     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | true     | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 1.59   | 0.0       | 1.59     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3494
  Scenario: Verify non negative amount for Accrual Activity on backdated repayment
    When Admin sets the business date to "09 December 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 09 December 2024  | 800            | 33.3                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "09 December 2024" with "800" amount and expected disbursement date on "09 December 2024"
    When Admin successfully disburse the loan on "09 December 2024" with "800" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 09 January 2025  |           | 675.62          | 124.38        | 22.2     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 2  | 31   | 09 February 2025 |           | 547.79          | 127.83        | 18.75    | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 3  | 28   | 09 March 2025    |           | 416.41          | 131.38        | 15.2     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 4  | 31   | 09 April 2025    |           | 281.39          | 135.02        | 11.56    | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 5  | 30   | 09 May 2025      |           | 142.62          | 138.77        | 7.81     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 6  | 31   | 09 June 2025     |           | 0.0             | 142.62        | 3.96     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.0         | 79.48    | 0.0  | 0.0       | 879.48 | 0.0  | 0.0        | 0.0  | 879.48      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    When Admin sets the business date to "11 December 2024"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "08 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "09 January 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "09 January 2025" with 146.58 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 675.62          | 124.38        | 22.2     | 0.0  | 0.0       | 146.58 | 146.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 |                 | 547.79          | 127.83        | 18.75    | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 3  | 28   | 09 March 2025    |                 | 416.41          | 131.38        | 15.2     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 4  | 31   | 09 April 2025    |                 | 281.39          | 135.02        | 11.56    | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 5  | 30   | 09 May 2025      |                 | 142.62          | 138.77        | 7.81     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 6  | 31   | 09 June 2025     |                 | 0.0             | 142.62        | 3.96     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 79.48    | 0.0  | 0.0       | 879.48 | 146.58 | 0.0        | 0.0  | 732.90      |
    When Admin sets the business date to "10 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 146.58 | 124.38    | 22.2     | 0.0  | 0.0       | 675.62       | false    | false    |
      | 09 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity | 22.2   | 0.0       | 22.2     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "11 January 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "10 January 2025" with 676.22 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 675.62          | 124.38        | 22.2     | 0.0  | 0.0       | 146.58 | 146.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 | 10 January 2025 | 529.64          | 145.98        | 0.6      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 3  | 28   | 09 March 2025    | 10 January 2025 | 383.06          | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 4  | 31   | 09 April 2025    | 10 January 2025 | 236.48          | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 5  | 30   | 09 May 2025      | 10 January 2025 | 89.9            | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 6  | 31   | 09 June 2025     | 10 January 2025 | 0.0             | 89.9          | 0.0      | 0.0  | 0.0       | 89.9   | 89.9   | 89.9       | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 800.0         | 22.8     | 0.0  | 0.0       | 822.8 | 822.8 | 676.22     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 146.58 | 124.38    | 22.2     | 0.0  | 0.0       | 675.62       | false    | false    |
      | 09 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity | 22.2   | 0.0       | 22.2     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Repayment        | 676.22 | 675.62    | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual Activity | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3495
  Scenario: Verify non negative amount for Accrual Activity on backdated repayment and accrual adjustment
    When Admin sets the business date to "09 December 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 09 December 2024  | 800            | 33.3                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "09 December 2024" with "800" amount and expected disbursement date on "09 December 2024"
    When Admin successfully disburse the loan on "09 December 2024" with "800" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 09 January 2025  |           | 675.62          | 124.38        | 22.2     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 2  | 31   | 09 February 2025 |           | 547.79          | 127.83        | 18.75    | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 3  | 28   | 09 March 2025    |           | 416.41          | 131.38        | 15.2     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 4  | 31   | 09 April 2025    |           | 281.39          | 135.02        | 11.56    | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 5  | 30   | 09 May 2025      |           | 142.62          | 138.77        | 7.81     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 6  | 31   | 09 June 2025     |           | 0.0             | 142.62        | 3.96     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.0         | 79.48    | 0.0  | 0.0       | 879.48 | 0.0  | 0.0        | 0.0  | 879.48      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    When Admin sets the business date to "08 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 07 January 2025  | Accrual          | 20.77  | 0.0       | 20.77    | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "09 January 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "09 January 2025" with 146.58 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 675.62          | 124.38        | 22.2     | 0.0  | 0.0       | 146.58 | 146.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 |                 | 547.79          | 127.83        | 18.75    | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 3  | 28   | 09 March 2025    |                 | 416.41          | 131.38        | 15.2     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 4  | 31   | 09 April 2025    |                 | 281.39          | 135.02        | 11.56    | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 5  | 30   | 09 May 2025      |                 | 142.62          | 138.77        | 7.81     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 6  | 31   | 09 June 2025     |                 | 0.0             | 142.62        | 3.96     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 79.48    | 0.0  | 0.0       | 879.48 | 146.58 | 0.0        | 0.0  | 732.90      |
    When Admin sets the business date to "10 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 07 January 2025  | Accrual          | 20.77  | 0.0       | 20.77    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 146.58 | 124.38    | 22.2     | 0.0  | 0.0       | 675.62       | false    | false    |
      | 09 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity | 22.2   | 0.0       | 22.2     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "11 January 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "09 January 2025" with 675.62 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 675.62          | 124.38        | 22.2     | 0.0  | 0.0       | 146.58 | 146.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 | 09 January 2025 | 529.04          | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 3  | 28   | 09 March 2025    | 09 January 2025 | 382.46          | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 4  | 31   | 09 April 2025    | 09 January 2025 | 235.88          | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 5  | 30   | 09 May 2025      | 09 January 2025 | 89.3            | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 6  | 31   | 09 June 2025     | 09 January 2025 | 0.0             | 89.3          | 0.0      | 0.0  | 0.0       | 89.3   | 89.3   | 89.3       | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 800.0         | 22.2     | 0.0  | 0.0       | 822.2 | 822.2 | 675.62     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement       | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 07 January 2025  | Accrual            | 20.77  | 0.0       | 20.77    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual            | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment          | 146.58 | 124.38    | 22.2     | 0.0  | 0.0       | 675.62       | false    | false    |
      | 09 January 2025  | Accrual            | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity   | 22.2   | 0.0       | 22.2     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment          | 675.62 | 675.62    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual            | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual Adjustment | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3496
  Scenario: Verify non negative amount for Accrual Activity on backdated repayment, accrual adjustment and revert activity
    When Admin sets the business date to "09 December 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 09 December 2024  | 800            | 33.3                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "09 December 2024" with "800" amount and expected disbursement date on "09 December 2024"
    When Admin successfully disburse the loan on "09 December 2024" with "800" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 09 January 2025  |           | 675.62          | 124.38        | 22.2     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 2  | 31   | 09 February 2025 |           | 547.79          | 127.83        | 18.75    | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 3  | 28   | 09 March 2025    |           | 416.41          | 131.38        | 15.2     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 4  | 31   | 09 April 2025    |           | 281.39          | 135.02        | 11.56    | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 5  | 30   | 09 May 2025      |           | 142.62          | 138.77        | 7.81     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
      | 6  | 31   | 09 June 2025     |           | 0.0             | 142.62        | 3.96     | 0.0  | 0.0       | 146.58 | 0.0  | 0.0        | 0.0  | 146.58      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.0         | 79.48    | 0.0  | 0.0       | 879.48 | 0.0  | 0.0        | 0.0  | 879.48      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    When Admin sets the business date to "08 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 07 January 2025  | Accrual          | 20.77  | 0.0       | 20.77    | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "09 January 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "09 January 2025" with 146.58 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 675.62          | 124.38        | 22.2     | 0.0  | 0.0       | 146.58 | 146.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 |                 | 547.79          | 127.83        | 18.75    | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 3  | 28   | 09 March 2025    |                 | 416.41          | 131.38        | 15.2     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 4  | 31   | 09 April 2025    |                 | 281.39          | 135.02        | 11.56    | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 5  | 30   | 09 May 2025      |                 | 142.62          | 138.77        | 7.81     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
      | 6  | 31   | 09 June 2025     |                 | 0.0             | 142.62        | 3.96     | 0.0  | 0.0       | 146.58 | 0.0    | 0.0        | 0.0  | 146.58      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 79.48    | 0.0  | 0.0       | 879.48 | 146.58 | 0.0        | 0.0  | 732.90      |
    When Admin sets the business date to "10 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 07 January 2025  | Accrual          | 20.77  | 0.0       | 20.77    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 146.58 | 124.38    | 22.2     | 0.0  | 0.0       | 675.62       | false    | false    |
      | 09 January 2025  | Accrual          | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity | 22.2   | 0.0       | 22.2     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "11 January 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "08 January 2025" with 675.03 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 08 January 2025 | 674.9           | 125.1         | 21.48    | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 | 08 January 2025 | 528.32          | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 3  | 28   | 09 March 2025    | 08 January 2025 | 381.74          | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 4  | 31   | 09 April 2025    | 08 January 2025 | 235.16          | 146.58        | 0.0      | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 5  | 30   | 09 May 2025      | 09 January 2025 | 88.71           | 146.45        | 0.13     | 0.0  | 0.0       | 146.58 | 146.58 | 146.58     | 0.0  | 0.0         |
      | 6  | 31   | 09 June 2025     | 09 January 2025 | 0.0             | 88.71         | 0.0      | 0.0  | 0.0       | 88.71  | 88.71  | 88.71      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 21.61    | 0.0  | 0.0       | 821.61 | 821.61 | 821.61     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement       | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 07 January 2025  | Accrual            | 20.77  | 0.0       | 20.77    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual            | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Repayment          | 675.03 | 653.55    | 21.48    | 0.0  | 0.0       | 146.45       | false    | false    |
      | 08 January 2025  | Accrual Activity   | 21.61  | 0.0       | 21.61    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment          | 146.58 | 146.45    | 0.13     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 09 January 2025  | Accrual            | 0.72   | 0.0       | 0.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual            | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual Adjustment | 1.19   | 0.0       | 1.19     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3275
  Scenario: Verify accrual activity for backdated repayment on the progressive loan, accrual adjustment needed
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "19 January 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "21 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 18 January 2024  | Accrual          | 11.88  | 0.0       | 11.88    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "21 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 263.69 EUR transaction amount
    When Admin sets the business date to "23 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 746.09          | 253.91        | 9.78     | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 507.44          | 238.65        | 25.04    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 254.74          | 252.7         | 10.99    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 254.74        | 5.52     | 0.0  | 0.0       | 260.26 | 0.0    | 0.0        | 0.0  | 260.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 51.33    | 0.0  | 0.0       | 1051.33 | 263.69 | 263.69     | 0.0  | 787.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2024  | Repayment          | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 18 January 2024  | Accrual            | 11.88  | 0.0       | 11.88    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual Adjustment | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual            | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "26 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 746.09          | 253.91        | 9.78     | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 507.44          | 238.65        | 25.04    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 254.74          | 252.7         | 10.99    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 254.74        | 5.52     | 0.0  | 0.0       | 260.26 | 0.0    | 0.0        | 0.0  | 260.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 51.33    | 0.0  | 0.0       | 1051.33 | 263.69 | 263.69     | 0.0  | 787.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2024  | Repayment          | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 18 January 2024  | Accrual            | 11.88  | 0.0       | 11.88    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual Adjustment | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual            | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual            | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual            | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual            | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3276
  Scenario: Verify accrual activity for backdated repayment on the progressive loan, accrual adjustment not needed
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "17 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 263.69 EUR transaction amount
    When Admin sets the business date to "18 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 746.09          | 253.91        | 9.78     | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 507.44          | 238.65        | 25.04    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 254.74          | 252.7         | 10.99    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 254.74        | 5.52     | 0.0  | 0.0       | 260.26 | 0.0    | 0.0        | 0.0  | 260.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 51.33    | 0.0  | 0.0       | 1051.33 | 263.69 | 263.69     | 0.0  | 787.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "26 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 746.09          | 253.91        | 9.78     | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 507.44          | 238.65        | 25.04    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 254.74          | 252.7         | 10.99    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 254.74        | 5.52     | 0.0  | 0.0       | 260.26 | 0.0    | 0.0        | 0.0  | 260.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 51.33    | 0.0  | 0.0       | 1051.33 | 263.69 | 263.69     | 0.0  | 787.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3284
  Scenario: Verify accrual activity for backdated disbursal on the progressive loan, accrual adjustment not needed
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 1500           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "20 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin successfully disburse the loan on "15 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "21 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2024  |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 1133.34         | 366.66        | 27.61    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 2  | 29   | 01 March 2024    |           | 763.63          | 369.71        | 24.56    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 3  | 31   | 01 April 2024    |           | 385.91          | 377.72        | 16.55    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 385.91        | 8.36     | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 77.08    | 0.0  | 0.0       | 1577.08 | 0.0  | 0.0        | 0.0  | 1577.08     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 2.45   | 0.0       | 2.45     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "2 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2024  |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 1133.34         | 366.66        | 27.61    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 2  | 29   | 01 March 2024    |           | 763.9           | 369.44        | 24.83    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 3  | 31   | 01 April 2024    |           | 386.18          | 377.72        | 16.55    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 386.18        | 8.37     | 0.0  | 0.0       | 394.55 | 0.0  | 0.0        | 0.0  | 394.55      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 77.36    | 0.0  | 0.0       | 1577.36 | 0.0  | 0.0        | 0.0  | 1577.36     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 2.45   | 0.0       | 2.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 1.04   | 0.0       | 1.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 1.04   | 0.0       | 1.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3285
  Scenario: Verify accrual activity for reversal of last disbursement on the progressive loan, accrual adjustment needed
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 1500           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "20 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "20 January 2024"
    When Admin successfully disburse the loan on "15 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "22 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2024  |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 1133.34         | 366.66        | 27.61    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 2  | 29   | 01 March 2024    |           | 763.63          | 369.71        | 24.56    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 3  | 31   | 01 April 2024    |           | 385.91          | 377.72        | 16.55    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 385.91        | 8.36     | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 77.08    | 0.0  | 0.0       | 1577.08 | 0.0  | 0.0        | 0.0  | 1577.08     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 2.45   | 0.0       | 2.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "31 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2024  |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 1133.34         | 366.66        | 27.61    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 2  | 29   | 01 March 2024    |           | 763.63          | 369.71        | 24.56    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 3  | 31   | 01 April 2024    |           | 385.91          | 377.72        | 16.55    | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 385.91        | 8.36     | 0.0  | 0.0       | 394.27 | 0.0  | 0.0        | 0.0  | 394.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 77.08    | 0.0  | 0.0       | 1577.08 | 0.0  | 0.0        | 0.0  | 1577.08     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 2.45   | 0.0       | 2.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 1.04   | 0.0       | 1.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 1.04   | 0.0       | 1.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Admin successfully undo last disbursal
    When Admin sets the business date to "02 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.89          | 247.09        | 16.6     | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.27          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.27        | 5.6      | 0.0  | 0.0       | 263.87 | 0.0  | 0.0        | 0.0  | 263.87      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.94    | 0.0  | 0.0       | 1054.94 | 0.0  | 0.0        | 0.0  | 1054.94     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual            | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual            | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual            | 2.45   | 0.0       | 2.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual            | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual            | 1.04   | 0.0       | 1.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual            | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual            | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual            | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual            | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual            | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual            | 1.04   | 0.0       | 1.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual            | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual            | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual Adjustment | 4.54   | 0.0       | 4.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3286
  Scenario: Verify accrual activity for reversal of last disbursement on the progressive loan, accrual adjustment not needed
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 1100           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75 | 0.0  | 0.0        | 0.0  | 1054.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "20 January 2024"
    When Admin successfully disburse the loan on "20 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "23 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 20 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 832.8           | 267.2         | 22.51    | 0.0  | 0.0       | 289.71 | 0.0  | 0.0        | 0.0  | 289.71      |
      | 2  | 29   | 01 March 2024    |           | 561.13          | 271.67        | 18.04    | 0.0  | 0.0       | 289.71 | 0.0  | 0.0        | 0.0  | 289.71      |
      | 3  | 31   | 01 April 2024    |           | 283.58          | 277.55        | 12.16    | 0.0  | 0.0       | 289.71 | 0.0  | 0.0        | 0.0  | 289.71      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 283.58        | 6.14     | 0.0  | 0.0       | 289.72 | 0.0  | 0.0        | 0.0  | 289.72      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1100.0        | 58.85    | 0.0  | 0.0       | 1158.85 | 0.0  | 0.0        | 0.0  | 1158.85     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1100.0       | false    | false    |
      | 20 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.77   | 0.0       | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.77   | 0.0       | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Admin successfully undo last disbursal
    When Admin sets the business date to "2 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.89          | 247.09        | 16.6     | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.27          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.27        | 5.6      | 0.0  | 0.0       | 263.87 | 0.0  | 0.0        | 0.0  | 263.87      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.94    | 0.0  | 0.0       | 1054.94 | 0.0  | 0.0        | 0.0  | 1054.94     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.77   | 0.0       | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.77   | 0.0       | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3393
  Scenario: Interest pause with same period - UC1
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
# --- set and check interest pause period --- #
    And Create interest pause period with start date "05 February 2024" and end date "10 February 2024"
    Then Loan term variations has 1 variation, with the following data:
      | Term Type Id | Term Type Code             | Term Type Value | Applicable From  | Decimal Value | Date Value       | Is Specific To Installment | Is Processed |
      | 11           | loanTermType.interestPause | interestPause   | 05 February 2024 | 0.0           | 10 February 2024 | false                      |              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.95           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.33           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.61           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.8            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.8          | 0.1      | 0.0  | 0.0       | 16.9  | 0.0  | 0.0        | 0.0  | 16.9        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 1.95     | 0    | 0         | 101.95 | 0.0  | 0          | 0    | 101.95      |
    When Admin sets the business date to "15 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3394
  Scenario: Interest pause between two periods - UC2
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
# --- set and check interest pause period --- #
    And Create interest pause period with start date "10 February 2024" and end date "10 March 2024"
    Then Loan term variations has 1 variation, with the following data:
      | Term Type Id | Term Type Code             | Term Type Value | Applicable From  | Decimal Value | Date Value    | Is Specific To Installment | Is Processed |
      | 11           | loanTermType.interestPause | interestPause   | 10 February 2024 | 0.0           | 10 March 2024 | false                      |              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.69           | 16.88         | 0.13     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.96           | 16.73         | 0.28     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.24           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.42           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.42         | 0.1      | 0.0  | 0.0       | 16.52 | 0.0  | 0.0        | 0.0  | 16.52       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 1.57     | 0    | 0         | 101.57 | 0.0  | 0          | 0    | 101.57      |
    When Admin sets the business date to "5 April 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3395
  Scenario: Backdated interest pause after the repayment - UC3
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- set and check interest pause period --- #
    And Create interest pause period with start date "05 February 2024" and end date "10 February 2024"
    When Admin sets the business date to "5 March 2024"
    When Admin runs inline COB job for Loan
    Then Loan term variations has 1 variation, with the following data:
      | Term Type Id | Term Type Code             | Term Type Value | Applicable From  | Decimal Value | Date Value       | Is Specific To Installment | Is Processed |
      | 11           | loanTermType.interestPause | interestPause   | 05 February 2024 | 0.0           | 10 February 2024 | false                      |              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.95           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.61           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.8            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.8          | 0.1      | 0.0  | 0.0       | 16.9  | 0.0   | 0.0        | 0.0  | 16.9        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.95     | 0    | 0         | 101.95 | 34.02 | 0          | 0    | 67.93       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 66.95        | false    | true     |
      | 01 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3396
  Scenario: Multiple interest pauses - UC4
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- set and check interest pause period --- #
    And Create interest pause period with start date "05 February 2024" and end date "10 February 2024"
    Then Loan term variations has 1 variation, with the following data:
      | Term Type Id | Term Type Code             | Term Type Value | Applicable From  | Decimal Value | Date Value       | Is Specific To Installment | Is Processed |
      | 11           | loanTermType.interestPause | interestPause   | 05 February 2024 | 0.0           | 10 February 2024 | false                      |              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.95           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.61           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.8            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.8          | 0.1      | 0.0  | 0.0       | 16.9  | 0.0   | 0.0        | 0.0  | 16.9        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.95     | 0    | 0         | 101.95 | 34.02 | 0          | 0    | 67.93       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 66.95        | false    | true     |
# --- set and check 2nd interest pause period --- #
    And Create interest pause period with start date "10 March 2024" and end date "20 March 2024"
    Then Loan term variations has 2 variation, with the following data:
      | Term Type Id | Term Type Code             | Term Type Value | Applicable From  | Decimal Value | Date Value       | Is Specific To Installment | Is Processed |
      | 11           | loanTermType.interestPause | interestPause   | 05 February 2024 | 0.0           | 10 February 2024 | false                      |              |
      | 11           | loanTermType.interestPause | interestPause   | 10 March 2024    | 0.0           | 20 March 2024    | false                      |              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.95           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.19           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.47           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.66           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.66         | 0.1      | 0.0  | 0.0       | 16.76 | 0.0   | 0.0        | 0.0  | 16.76       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.81     | 0    | 0         | 101.81 | 34.02 | 0          | 0    | 67.79       |
    When Admin sets the business date to "5 April 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 66.95        | false    | true     |
      | 01 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3397
  Scenario: Backdated interest pause outcomes with Accrual Adjustment - UC5
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "15 February 2024"
    When Admin runs inline COB job for Loan
# --- set and check interest pause period --- #
    And Create interest pause period with start date "05 February 2024" and end date "10 February 2024"
    Then Loan term variations has 1 variation, with the following data:
      | Term Type Id | Term Type Code             | Term Type Value | Applicable From  | Decimal Value | Date Value       | Is Specific To Installment | Is Processed |
      | 11           | loanTermType.interestPause | interestPause   | 05 February 2024 | 0.0           | 10 February 2024 | false                      |              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.97           | 16.6          | 0.41     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.35           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.63           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.82           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.82         | 0.1      | 0.0  | 0.0       | 16.92 | 0.0  | 0.0        | 0.0  | 16.92       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 1.97     | 0    | 0         | 101.97 | 0.0  | 0          | 0    | 101.97      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "16 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual Adjustment | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3402
  Scenario: Backdated interest pause after the early repayment - UC6
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 15 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.53           | 16.47         | 0.54     | 0.0  | 0.0       | 17.01 | 15.0 | 15.0       | 0.0  | 2.01        |
      | 2  | 29   | 01 March 2024    |           | 67.01           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.39           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.67           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.86           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.86         | 0.1      | 0.0  | 0.0       | 16.96 | 0.0  | 0.0        | 0.0  | 16.96       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.01     | 0.0  | 0.0       | 102.01 | 15.0 | 15.0       | 0.0  | 87.01       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 85.0         | false    | false    |
# --- set and check interest pause period --- #
    And Create interest pause period with start date "14 January 2024" and end date "20 February 2024"
    When Admin sets the business date to "5 March 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.49           | 16.51         | 0.5      | 0.0  | 0.0       | 17.01 | 15.0 | 15.0       | 0.0  | 2.01        |
      | 2  | 29   | 01 March 2024    |           | 66.65           | 16.84         | 0.17     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.04           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.32           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.5            | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.5          | 0.1      | 0.0  | 0.0       | 16.6  | 0.0  | 0.0        | 0.0  | 16.6        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.65     | 0.0  | 0.0       | 101.65 | 15.0 | 15.0       | 0.0  | 86.65       |
    When Admin sets the business date to "5 March 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment          | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 85.0         | false    | false    |
      | 15 January 2024  | Accrual Adjustment | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3403
  Scenario: Early repayment before interest pause - UC7
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "14 January 2024"
    And Customer makes "AUTOPAY" repayment on "14 January 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 14 January 2024 | 83.23           | 16.77         | 0.24     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 67.0            | 16.23         | 0.78     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                 | 50.38           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                 | 33.66           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.85           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.85         | 0.1      | 0.0  | 0.0       | 16.95 | 0.0   | 0.0        | 0.0  | 16.95       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100           | 2.0      | 0    | 0         | 102.0 | 17.01 | 17.01      | 0    | 84.99       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 14 January 2024  | Repayment        | 17.01  | 16.77     | 0.24     | 0.0  | 0.0       | 83.23        | false    | false    |
    And Create interest pause period with start date "15 January 2024" and end date "25 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 14 January 2024 | 83.23           | 16.77         | 0.24     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 66.82           | 16.41         | 0.6      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                 | 50.2            | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                 | 33.48           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.67           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.67         | 0.1      | 0.0  | 0.0       | 16.77 | 0.0   | 0.0        | 0.0  | 16.77       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.82     | 0    | 0         | 101.82 | 17.01 | 17.01      | 0    | 84.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 14 January 2024  | Repayment        | 17.01  | 16.77     | 0.24     | 0.0  | 0.0       | 83.23        | false    | false    |

  @TestRailId:C3404
  Scenario: Interest pause that overlaps a few installments - UC8
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
    And Create interest pause period with start date "01 February 2024" and end date "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.55           | 16.45         | 0.56     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.54           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.92           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.2            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.38           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.38         | 0.1      | 0.0  | 0.0       | 16.48 | 0.0  | 0.0        | 0.0  | 16.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 1.53     | 0    | 0         | 101.53 | 0.0  | 0.0        | 0    | 101.53      |
    When Admin sets the business date to "5 March 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3386
  Scenario: Accrual activity transaction reversed without update external-id to null when an accrual activity is reversed
    When Admin sets the business date to "12 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ACCRUAL_ACTIVITY_POSTING | 12 January 2025   | 430            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 12 January 2025  |           | 430.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 12 February 2025 |           | 323.44          | 106.56        | 2.51     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 2  | 28   | 12 March 2025    |           | 216.26          | 107.18        | 1.89     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 3  | 31   | 12 April 2025    |           | 108.45          | 107.81        | 1.26     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 4  | 30   | 12 May 2025      |           | 0.0             | 108.45        | 0.63     | 0.0  | 0.0       | 109.08 | 0.0  | 0.0        | 0.0  | 109.08      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 430           | 6.29     | 0    | 0         | 436.29 | 0    | 0          | 0    | 436.29      |
    And Admin successfully approves the loan on "12 January 2025" with "430" amount and expected disbursement date on "12 January 2025"
    And Admin successfully disburse the loan on "12 January 2025" with "430" EUR transaction amount
    When Admin sets the business date to "16 January 2025"
    And Customer makes "AUTOPAY" repayment on "16 January 2025" with 430 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 12 January 2025  | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 16 January 2025  | Repayment        | 430.0  | 429.68    | 0.32     | 0.0  | 0.0       | 0.32         | false    | false    |
    And Customer makes "AUTOPAY" repayment on "16 January 2025" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 12 January 2025  |                 | 430.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 12 February 2025 | 16 January 2025 | 321.25          | 108.75        | 0.32     | 0.0  | 0.0       | 109.07 | 109.07 | 109.07     | 0.0  | 0.0         |
      | 2  | 28   | 12 March 2025    | 16 January 2025 | 212.18          | 109.07        | 0.0      | 0.0  | 0.0       | 109.07 | 109.07 | 109.07     | 0.0  | 0.0         |
      | 3  | 31   | 12 April 2025    | 16 January 2025 | 103.11          | 109.07        | 0.0      | 0.0  | 0.0       | 109.07 | 109.07 | 109.07     | 0.0  | 0.0         |
      | 4  | 30   | 12 May 2025      | 16 January 2025 | 0.0             | 103.11        | 0.0      | 0.0  | 0.0       | 103.11 | 103.11 | 103.11     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 430           | 0.32     | 0    | 0         | 430.32 | 430.32 | 430.32     | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 12 January 2025  | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 16 January 2025  | Repayment        | 430.0  | 429.68    | 0.32     | 0.0  | 0.0       | 0.32         | false    | false    |
      | 16 January 2025  | Repayment        | 10.0   | 0.32      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual Activity | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- check if 'Accrual Activity' transaction has non-null external-id and remember transaction
    Then In Loan Transactions the "1"th Transaction with type="Accrual Activity" and date "16 January 2025" has non-null external-id
#   --- check if all transactions have non-null external-id
    Then In Loan Transactions all transactions have non-null external-id
    When Customer undo "2"th repayment on "16 January 2025"
    When Admin runs inline COB job for Loan
    Then Loan status will be "ACTIVE"
    Then Loan has 0.33 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 12 January 2025  |                 | 430.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 12 February 2025 | 16 January 2025 | 321.25          | 108.75        | 0.32     | 0.0  | 0.0       | 109.07 | 109.07 | 109.07     | 0.0  | 0.0         |
      | 2  | 28   | 12 March 2025    | 16 January 2025 | 212.18          | 109.07        | 0.0      | 0.0  | 0.0       | 109.07 | 109.07 | 109.07     | 0.0  | 0.0         |
      | 3  | 31   | 12 April 2025    | 16 January 2025 | 103.11          | 109.07        | 0.0      | 0.0  | 0.0       | 109.07 | 109.07 | 109.07     | 0.0  | 0.0         |
      | 4  | 30   | 12 May 2025      |                 | 0.0             | 103.11        | 0.01     | 0.0  | 0.0       | 103.12 | 102.79 | 102.79     | 0.0  | 0.33        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 430           | 0.33     | 0    | 0         | 430.33 | 430.0 | 430.0      | 0    | 0.33        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 12 January 2025  | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 16 January 2025  | Repayment        | 430.0  | 429.68    | 0.32     | 0.0  | 0.0       | 0.32         | false    | false    |
      | 16 January 2025  | Repayment        | 10.0   | 0.32      | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- check if 'Accrual Activity' is reversed and has non-null external-id
    Then Check required transaction for non-null eternal-id
    Then In Loan Transactions all transactions have non-null external-id

  @TestRailId:C3398
  Scenario: Verify accrual activity for paid loan
    When Admin sets the business date to "20 December 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 20 December 2024  | 430            | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "20 December 2024" with "430" amount and expected disbursement date on "20 December 2024"
    When Admin successfully disburse the loan on "20 December 2024" with "430" EUR transaction amount
    When Admin sets the business date to "30 December 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 20 December 2024 |           | 430.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 20 January 2025  |           | 362.12          | 67.88         | 9.32     | 0.0  | 0.0       | 77.2 | 0.0  | 0.0        | 0.0  | 77.2        |
      | 2  | 31   | 20 February 2025 |           | 292.77          | 69.35         | 7.85     | 0.0  | 0.0       | 77.2 | 0.0  | 0.0        | 0.0  | 77.2        |
      | 3  | 28   | 20 March 2025    |           | 221.91          | 70.86         | 6.34     | 0.0  | 0.0       | 77.2 | 0.0  | 0.0        | 0.0  | 77.2        |
      | 4  | 31   | 20 April 2025    |           | 149.52          | 72.39         | 4.81     | 0.0  | 0.0       | 77.2 | 0.0  | 0.0        | 0.0  | 77.2        |
      | 5  | 30   | 20 May 2025      |           | 75.56           | 73.96         | 3.24     | 0.0  | 0.0       | 77.2 | 0.0  | 0.0        | 0.0  | 77.2        |
      | 6  | 31   | 20 June 2025     |           | 0.0             | 75.56         | 1.64     | 0.0  | 0.0       | 77.2 | 0.0  | 0.0        | 0.0  | 77.2        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 430.0         | 33.2     | 0.0  | 0.0       | 463.2 | 0.0  | 0.0        | 0.0  | 463.2       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 29 December 2024 | Accrual          | 2.7    | 0.0       | 2.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "30 December 2024" with 200 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 20 December 2024 |                  | 430.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 20 January 2025  | 30 December 2024 | 355.81          | 74.19         | 3.01     | 0.0  | 0.0       | 77.2  | 77.2 | 77.2       | 0.0  | 0.0         |
      | 2  | 31   | 20 February 2025 | 30 December 2024 | 278.61          | 77.2          | 0.0      | 0.0  | 0.0       | 77.2  | 77.2 | 77.2       | 0.0  | 0.0         |
      | 3  | 28   | 20 March 2025    |                  | 214.93          | 63.68         | 13.52    | 0.0  | 0.0       | 77.2  | 45.6 | 45.6       | 0.0  | 31.6        |
      | 4  | 31   | 20 April 2025    |                  | 142.39          | 72.54         | 4.66     | 0.0  | 0.0       | 77.2  | 0.0  | 0.0        | 0.0  | 77.2        |
      | 5  | 30   | 20 May 2025      |                  | 68.28           | 74.11         | 3.09     | 0.0  | 0.0       | 77.2  | 0.0  | 0.0        | 0.0  | 77.2        |
      | 6  | 31   | 20 June 2025     |                  | 0.0             | 68.28         | 1.48     | 0.0  | 0.0       | 69.76 | 0.0  | 0.0        | 0.0  | 69.76       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 430.0         | 25.76    | 0.0  | 0.0       | 455.76 | 200.0 | 200.0      | 0.0  | 255.76      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 29 December 2024 | Accrual          | 2.7    | 0.0       | 2.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Repayment        | 200.0  | 196.99    | 3.01     | 0.0  | 0.0       | 233.01       | false    | false    |
    When Admin sets the business date to "21 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 20 December 2024 |                  | 430.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 20 January 2025  | 30 December 2024 | 355.81          | 74.19         | 3.01     | 0.0  | 0.0       | 77.2  | 77.2 | 77.2       | 0.0  | 0.0         |
      | 2  | 31   | 20 February 2025 | 30 December 2024 | 278.61          | 77.2          | 0.0      | 0.0  | 0.0       | 77.2  | 77.2 | 77.2       | 0.0  | 0.0         |
      | 3  | 28   | 20 March 2025    |                  | 214.93          | 63.68         | 13.52    | 0.0  | 0.0       | 77.2  | 45.6 | 45.6       | 0.0  | 31.6        |
      | 4  | 31   | 20 April 2025    |                  | 142.39          | 72.54         | 4.66     | 0.0  | 0.0       | 77.2  | 0.0  | 0.0        | 0.0  | 77.2        |
      | 5  | 30   | 20 May 2025      |                  | 68.28           | 74.11         | 3.09     | 0.0  | 0.0       | 77.2  | 0.0  | 0.0        | 0.0  | 77.2        |
      | 6  | 31   | 20 June 2025     |                  | 0.0             | 68.28         | 1.48     | 0.0  | 0.0       | 69.76 | 0.0  | 0.0        | 0.0  | 69.76       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 430.0         | 25.76    | 0.0  | 0.0       | 455.76 | 200.0 | 200.0      | 0.0  | 255.76      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 29 December 2024 | Accrual          | 2.7    | 0.0       | 2.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Repayment        | 200.0  | 196.99    | 3.01     | 0.0  | 0.0       | 233.01       | false    | false    |
      | 30 December 2024 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "20 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 29 December 2024 | Accrual          | 2.7    | 0.0       | 2.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Repayment        | 200.0  | 196.99    | 3.01     | 0.0  | 0.0       | 233.01       | false    | false    |
      | 30 December 2024 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "22 March 2025"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 29 December 2024 | Accrual          | 2.7    | 0.0       | 2.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Repayment        | 200.0  | 196.99    | 3.01     | 0.0  | 0.0       | 233.01       | false    | false    |
      | 30 December 2024 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2025 | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2025 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2025 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2025 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2025 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2025 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2025 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2025 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2025 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2025 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2025    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2025    | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2025    | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3400
  Scenario: Verify Interest Payment Waiver isn't reversed and replayed after one accrual is created
    When Admin sets the business date to "12 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ACCRUAL_ACTIVITY_POSTING | 12 January 2025   | 430            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 12 January 2025  |           | 430.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 12 February 2025 |           | 323.44          | 106.56        | 2.51     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 2  | 28   | 12 March 2025    |           | 216.26          | 107.18        | 1.89     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 3  | 31   | 12 April 2025    |           | 108.45          | 107.81        | 1.26     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 4  | 30   | 12 May 2025      |           | 0.0             | 108.45        | 0.63     | 0.0  | 0.0       | 109.08 | 0.0  | 0.0        | 0.0  | 109.08      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 430           | 6.29     | 0    | 0         | 436.29 | 0    | 0          | 0    | 436.29      |
    And Admin successfully approves the loan on "12 January 2025" with "430" amount and expected disbursement date on "12 January 2025"
    And Admin successfully disburse the loan on "12 January 2025" with "430" EUR transaction amount
    When Admin sets the business date to "14 January 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 12 January 2025  |           | 430.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 12 February 2025 |           | 323.44          | 106.56        | 2.51     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 2  | 28   | 12 March 2025    |           | 216.26          | 107.18        | 1.89     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 3  | 31   | 12 April 2025    |           | 108.45          | 107.81        | 1.26     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 4  | 30   | 12 May 2025      |           | 0.0             | 108.45        | 0.63     | 0.0  | 0.0       | 109.08 | 0.0  | 0.0        | 0.0  | 109.08      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 430           | 6.29     | 0    | 0         | 436.29 | 0.0  | 0.0        | 0    | 436.29      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 12 January 2025  | Disbursement     | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 13 January 2025  | Accrual          | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "14 January 2025" with 10 EUR transaction amount
    Then In Loan Transactions all transactions have non-null external-id
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 12 January 2025  |           | 430.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 12 February 2025 |           | 323.38          | 106.62        | 2.45     | 0.0  | 0.0       | 109.07 | 10.0 | 10.0       | 0.0  | 99.07       |
      | 2  | 28   | 12 March 2025    |           | 216.2           | 107.18        | 1.89     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 3  | 31   | 12 April 2025    |           | 108.39          | 107.81        | 1.26     | 0.0  | 0.0       | 109.07 | 0.0  | 0.0        | 0.0  | 109.07      |
      | 4  | 30   | 12 May 2025      |           | 0.0             | 108.39        | 0.63     | 0.0  | 0.0       | 109.02 | 0.0  | 0.0        | 0.0  | 109.02      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 430           | 6.23     | 0    | 0         | 436.23 | 10.0 | 10.0       | 0    | 426.23      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 12 January 2025  | Disbursement            | 430.0  | 0.0       | 0.0      | 0.0  | 0.0       | 430.0        | false    | false    |
      | 13 January 2025  | Accrual                 | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Interest Payment Waiver | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 420.0        | false    | false    |

  @TestRailId:C3401
  Scenario: Verify that accrual activity is not reverse replayed when undo happens on due date repayment
    When Admin sets the business date to "08 August 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 08 August 2024    | 400            | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "08 August 2024" with "400" amount and expected disbursement date on "08 August 2024"
    When Admin successfully disburse the loan on "08 August 2024" with "400" EUR transaction amount
    When Admin sets the business date to "08 January 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 08 August 2024    |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 08 September 2024 |           | 368.53          | 31.47         | 4.07     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 2  | 30   | 08 October 2024   |           | 336.61          | 31.92         | 3.62     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 3  | 31   | 08 November 2024  |           | 304.49          | 32.12         | 3.42     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 4  | 30   | 08 December 2024  |           | 271.94          | 32.55         | 2.99     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 5  | 31   | 08 January 2025   |           | 239.17          | 32.77         | 2.77     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 6  | 31   | 08 February 2025  |           | 206.07          | 33.1          | 2.44     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 7  | 28   | 08 March 2025     |           | 172.43          | 33.64         | 1.9      | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 8  | 31   | 08 April 2025     |           | 138.65          | 33.78         | 1.76     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 9  | 30   | 08 May 2025       |           | 104.48          | 34.17         | 1.37     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 10 | 31   | 08 June 2025      |           | 70.0            | 34.48         | 1.06     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 11 | 30   | 08 July 2025      |           | 35.15           | 34.85         | 0.69     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 12 | 31   | 08 August 2025    |           | 0.0             | 35.15         | 0.36     | 0.0  | 0.0       | 35.51 | 0.0  | 0.0        | 0.0  | 35.51       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 400.0         | 26.45    | 0.0  | 0.0       | 426.45 | 0.0  | 0.0        | 0.0  | 426.45      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 08 August 2024    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 08 September 2024 | Accrual Activity | 4.07   | 0.0       | 4.07     | 0.0  | 0.0       | 0.0          |
      | 08 October 2024   | Accrual Activity | 3.62   | 0.0       | 3.62     | 0.0  | 0.0       | 0.0          |
      | 08 November 2024  | Accrual Activity | 3.42   | 0.0       | 3.42     | 0.0  | 0.0       | 0.0          |
      | 08 December 2024  | Accrual Activity | 2.99   | 0.0       | 2.99     | 0.0  | 0.0       | 0.0          |
      | 07 January 2025   | Accrual          | 16.78  | 0.0       | 16.78    | 0.0  | 0.0       | 0.0          |
    And Customer makes "AUTOPAY" repayment on "08 January 2025" with 500 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 08 August 2024    |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 08 September 2024 | 08 January 2025 | 368.53          | 31.47         | 4.07     | 0.0  | 0.0       | 35.54 | 35.54 | 0.0        | 35.54 | 0.0         |
      | 2  | 30   | 08 October 2024   | 08 January 2025 | 336.61          | 31.92         | 3.62     | 0.0  | 0.0       | 35.54 | 35.54 | 0.0        | 35.54 | 0.0         |
      | 3  | 31   | 08 November 2024  | 08 January 2025 | 304.49          | 32.12         | 3.42     | 0.0  | 0.0       | 35.54 | 35.54 | 0.0        | 35.54 | 0.0         |
      | 4  | 30   | 08 December 2024  | 08 January 2025 | 271.94          | 32.55         | 2.99     | 0.0  | 0.0       | 35.54 | 35.54 | 0.0        | 35.54 | 0.0         |
      | 5  | 31   | 08 January 2025   | 08 January 2025 | 239.17          | 32.77         | 2.77     | 0.0  | 0.0       | 35.54 | 35.54 | 0.0        | 0.0   | 0.0         |
      | 6  | 31   | 08 February 2025  | 08 January 2025 | 206.07          | 33.1          | 2.44     | 0.0  | 0.0       | 35.54 | 35.54 | 35.54      | 0.0   | 0.0         |
      | 7  | 28   | 08 March 2025     | 08 January 2025 | 172.43          | 33.64         | 1.9      | 0.0  | 0.0       | 35.54 | 35.54 | 35.54      | 0.0   | 0.0         |
      | 8  | 31   | 08 April 2025     | 08 January 2025 | 138.65          | 33.78         | 1.76     | 0.0  | 0.0       | 35.54 | 35.54 | 35.54      | 0.0   | 0.0         |
      | 9  | 30   | 08 May 2025       | 08 January 2025 | 104.48          | 34.17         | 1.37     | 0.0  | 0.0       | 35.54 | 35.54 | 35.54      | 0.0   | 0.0         |
      | 10 | 31   | 08 June 2025      | 08 January 2025 | 70.0            | 34.48         | 1.06     | 0.0  | 0.0       | 35.54 | 35.54 | 35.54      | 0.0   | 0.0         |
      | 11 | 30   | 08 July 2025      | 08 January 2025 | 35.15           | 34.85         | 0.69     | 0.0  | 0.0       | 35.54 | 35.54 | 35.54      | 0.0   | 0.0         |
      | 12 | 31   | 08 August 2025    | 08 January 2025 | 0.0             | 35.15         | 0.36     | 0.0  | 0.0       | 35.51 | 35.51 | 35.51      | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 400.0         | 26.45    | 0.0  | 0.0       | 426.45 | 426.45 | 248.75     | 142.16 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 08 August 2024    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 08 September 2024 | Accrual Activity | 4.07   | 0.0       | 4.07     | 0.0  | 0.0       | 0.0          |
      | 08 October 2024   | Accrual Activity | 3.62   | 0.0       | 3.62     | 0.0  | 0.0       | 0.0          |
      | 08 November 2024  | Accrual Activity | 3.42   | 0.0       | 3.42     | 0.0  | 0.0       | 0.0          |
      | 08 December 2024  | Accrual Activity | 2.99   | 0.0       | 2.99     | 0.0  | 0.0       | 0.0          |
      | 07 January 2025   | Accrual          | 16.78  | 0.0       | 16.78    | 0.0  | 0.0       | 0.0          |
      | 08 January 2025   | Repayment        | 500.0  | 400.0     | 26.45    | 0.0  | 0.0       | 0.0          |
      | 08 January 2025   | Accrual          | 9.67   | 0.0       | 9.67     | 0.0  | 0.0       | 0.0          |
      | 08 January 2025   | Accrual Activity | 12.35  | 0.0       | 12.35    | 0.0  | 0.0       | 0.0          |
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "08 January 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 08 August 2024    |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 08 September 2024 |           | 368.53          | 31.47         | 4.07     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 2  | 30   | 08 October 2024   |           | 336.61          | 31.92         | 3.62     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 3  | 31   | 08 November 2024  |           | 304.49          | 32.12         | 3.42     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 4  | 30   | 08 December 2024  |           | 271.94          | 32.55         | 2.99     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 5  | 31   | 08 January 2025   |           | 239.17          | 32.77         | 2.77     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 6  | 31   | 08 February 2025  |           | 206.07          | 33.1          | 2.44     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 7  | 28   | 08 March 2025     |           | 172.43          | 33.64         | 1.9      | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 8  | 31   | 08 April 2025     |           | 138.65          | 33.78         | 1.76     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 9  | 30   | 08 May 2025       |           | 104.48          | 34.17         | 1.37     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 10 | 31   | 08 June 2025      |           | 70.0            | 34.48         | 1.06     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 11 | 30   | 08 July 2025      |           | 35.15           | 34.85         | 0.69     | 0.0  | 0.0       | 35.54 | 0.0  | 0.0        | 0.0  | 35.54       |
      | 12 | 31   | 08 August 2025    |           | 0.0             | 35.15         | 0.36     | 0.0  | 0.0       | 35.51 | 0.0  | 0.0        | 0.0  | 35.51       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 400.0         | 26.45    | 0.0  | 0.0       | 426.45 | 0.0  | 0.0        | 0.0  | 426.45      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 August 2024    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
      | 08 September 2024 | Accrual Activity | 4.07   | 0.0       | 4.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 October 2024   | Accrual Activity | 3.62   | 0.0       | 3.62     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 November 2024  | Accrual Activity | 3.42   | 0.0       | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 December 2024  | Accrual Activity | 2.99   | 0.0       | 2.99     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025   | Accrual          | 16.78  | 0.0       | 16.78    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025   | Repayment        | 500.0  | 400.0     | 26.45    | 0.0  | 0.0       | 0.0          | true     | false    |
      | 08 January 2025   | Accrual          | 9.67   | 0.0       | 9.67     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3444
  Scenario: Verify that interestRecognitionOnDisbursementDate setting is taking into account by apply periodic accrual job - backdated repayment case
    When Admin sets the business date to "31 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 31 January 2024   | 2000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 January 2024" with "2000" amount and expected disbursement date on "31 January 2024"
    When Admin successfully disburse the loan on "31 January 2024" with "2000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 29   | 29 February 2024 |           | 1671.5          | 328.5         | 11.67    | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 2  | 31   | 31 March 2024    |           | 1341.08         | 330.42        | 9.75     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 3  | 30   | 30 April 2024    |           | 1008.73         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 4  | 31   | 31 May 2024      |           | 674.44          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 5  | 30   | 30 June 2024     |           | 338.2           | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 6  | 31   | 31 July 2024     |           | 0.0             | 338.2         | 1.97     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2000.0        | 41.02    | 0.0  | 0.0       | 2041.02 | 0.0  | 0.0        | 0.0  | 2041.02     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
    #run COB for 31 of Jan 2024
    And Admin runs inline COB job for Loan
    #move date to first period due - end of Feb 2024
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 29   | 29 February 2024 |           | 1671.5          | 328.5         | 11.67    | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 2  | 31   | 31 March 2024    |           | 1341.14         | 330.36        | 9.81     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 3  | 30   | 30 April 2024    |           | 1008.79         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 4  | 31   | 31 May 2024      |           | 674.5           | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 5  | 30   | 30 June 2024     |           | 338.26          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 6  | 31   | 31 July 2024     |           | 0.0             | 338.26        | 1.97     | 0.0  | 0.0       | 340.23 | 0.0  | 0.0        | 0.0  | 340.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2000.0        | 41.08    | 0.0  | 0.0       | 2041.08 | 0.0  | 0.0        | 0.0  | 2041.08     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 31 January 2024  | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual Activity | 11.67  | 0.0       | 11.67    | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |                  | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 29   | 29 February 2024 | 15 February 2024 | 1665.86         | 334.14        | 6.03     | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 31   | 31 March 2024    |                  | 1340.11         | 325.75        | 14.42    | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 3  | 30   | 30 April 2024    |                  | 1007.76         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 4  | 31   | 31 May 2024      |                  | 673.47          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 5  | 30   | 30 June 2024     |                  | 337.23          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 31   | 31 July 2024     |                  | 0.0             | 337.23        | 1.97     | 0.0  | 0.0       | 339.2  | 0.0    | 0.0        | 0.0  | 339.2       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 2000.0        | 40.05    | 0.0  | 0.0       | 2040.05 | 340.17 | 340.17     | 0.0  | 1699.88     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 31 January 2024  | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Repayment        | 340.17 | 334.14    | 6.03     | 0.0  | 0.0       | 1665.86      | false    | false    |
      | 16 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual Activity | 6.03   | 0.0       | 6.03     | 0.0  | 0.0       | 0.0          | false    | true     |
    When Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |                  | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 29   | 29 February 2024 | 15 February 2024 | 1665.86         | 334.14        | 6.03     | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 31   | 31 March 2024    |                  | 1340.11         | 325.75        | 14.42    | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 3  | 30   | 30 April 2024    |                  | 1007.82         | 332.29        | 7.88     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 4  | 31   | 31 May 2024      |                  | 673.53          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 5  | 30   | 30 June 2024     |                  | 337.29          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 31   | 31 July 2024     |                  | 0.0             | 337.29        | 1.97     | 0.0  | 0.0       | 339.26 | 0.0    | 0.0        | 0.0  | 339.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 2000.0        | 40.11    | 0.0  | 0.0       | 2040.11 | 340.17 | 340.17     | 0.0  | 1699.94     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 January 2024  | Disbursement       | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 31 January 2024  | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual            | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual            | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual            | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual            | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Repayment          | 340.17 | 334.14    | 6.03     | 0.0  | 0.0       | 1665.86      | false    | false    |
      | 16 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual            | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual            | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual            | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual            | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual Activity   | 6.03   | 0.0       | 6.03     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 01 March 2024    | Accrual Adjustment | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Accrual            | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual            | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual Activity   | 14.42  | 0.0       | 14.42    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3503
  Scenario: Verify accruals on closed or overpaid backdated loan with current date before installment date with due-date charge config option - UC1
    When Global config "charge-accrual-date" value set to "due-date"
    When Admin sets the business date to "20 January 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 800            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 28   | 01 March 2025    |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 31   | 01 April 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0 | 0.0  | 0.0        | 0.0  | 800.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "10 January 2025" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 600.0           | 200.0         | 0.0      | 0.0  | 20.0      | 220.0 | 0.0  | 0.0        | 0.0  | 220.0       |
      | 2  | 28   | 01 March 2025    |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 31   | 01 April 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 20.0      | 820.0 | 0.0  | 0.0        | 0.0  | 820.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 January 2025 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    And Customer makes "AUTOPAY" repayment on "16 January 2025" with 820 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 16 January 2025 | 600.0           | 200.0         | 0.0      | 0.0  | 20.0      | 220.0 | 220.0 | 220.0      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 16 January 2025 | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 16 January 2025 | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 16 January 2025 | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 20.0      | 820.0 | 820.0 | 820.0      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 16 January 2025  | Repayment        | 820.0  | 800.0     | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 January 2025 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C3504
  Scenario: Verify accruals on closed or overpaid backdated loan with current date after installment date with due-date charge config option - UC2
    When Global config "charge-accrual-date" value set to "due-date"
    When Admin sets the business date to "17 February 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 800            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 28   | 01 March 2025    |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 31   | 01 April 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0 | 0.0  | 0.0        | 0.0  | 800.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "10 January 2025" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 600.0           | 200.0         | 0.0      | 0.0  | 20.0      | 220.0 | 0.0  | 0.0        | 0.0  | 220.0       |
      | 2  | 28   | 01 March 2025    |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 31   | 01 April 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 20.0      | 820.0 | 0.0  | 0.0        | 0.0  | 820.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 January 2025 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    And Customer makes "AUTOPAY" repayment on "16 January 2025" with 820 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 16 January 2025 | 600.0           | 200.0         | 0.0      | 0.0  | 20.0      | 220.0 | 220.0 | 220.0      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 16 January 2025 | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 16 January 2025 | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 16 January 2025 | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 20.0      | 820.0 | 820.0 | 820.0      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 16 January 2025  | Repayment        | 820.0  | 800.0     | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
      | 17 February 2025 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 January 2025 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C3505
  Scenario: Verify accruals on closed or overpaid backdated loan with current date before installment date with submitted-date charge config option - UC3
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "20 January 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 800            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 28   | 01 March 2025    |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 31   | 01 April 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0 | 0.0  | 0.0        | 0.0  | 800.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "10 January 2025" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 600.0           | 200.0         | 0.0      | 0.0  | 20.0      | 220.0 | 0.0  | 0.0        | 0.0  | 220.0       |
      | 2  | 28   | 01 March 2025    |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 31   | 01 April 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 20.0      | 820.0 | 0.0  | 0.0        | 0.0  | 820.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 January 2025 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    And Customer makes "AUTOPAY" repayment on "16 January 2025" with 820 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 16 January 2025 | 600.0           | 200.0         | 0.0      | 0.0  | 20.0      | 220.0 | 220.0 | 220.0      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 16 January 2025 | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 16 January 2025 | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 16 January 2025 | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 20.0      | 820.0 | 820.0 | 820.0      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 16 January 2025  | Repayment        | 820.0  | 800.0     | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 January 2025 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C3506
  Scenario: Verify accruals on closed or overpaid backdated loan with current date after installment date with ігиьшееув-date charge config option - UC4
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "17 February 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 800            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 28   | 01 March 2025    |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 31   | 01 April 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0 | 0.0  | 0.0        | 0.0  | 800.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "10 January 2025" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 600.0           | 200.0         | 0.0      | 0.0  | 20.0      | 220.0 | 0.0  | 0.0        | 0.0  | 220.0       |
      | 2  | 28   | 01 March 2025    |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 31   | 01 April 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 30   | 01 May 2025      |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 20.0      | 820.0 | 0.0  | 0.0        | 0.0  | 820.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 January 2025 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    And Customer makes "AUTOPAY" repayment on "16 January 2025" with 820 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 16 January 2025 | 600.0           | 200.0         | 0.0      | 0.0  | 20.0      | 220.0 | 220.0 | 220.0      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 16 January 2025 | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 16 January 2025 | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 16 January 2025 | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 20.0      | 820.0 | 820.0 | 820.0      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 16 January 2025  | Repayment        | 820.0  | 800.0     | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
      | 17 February 2025 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 January 2025 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C3507
  Scenario: Verify calculation of fraction period in case `interestRecognitionFromDisbursementDate` is true
    When Admin sets the business date to "13 November 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 13 November 2023  | 5000           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "13 November 2023" with "5000" amount and expected disbursement date on "13 November 2023"
    When Admin successfully disburse the loan on "13 November 2023" with "5000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 13 November 2023 |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 13 December 2023 |           | 4183.33         | 816.67        | 41.05    | 0.0  | 0.0       | 857.72 | 0.0  | 0.0        | 0.0  | 857.72      |
      | 2  | 31   | 13 January 2024  |           | 3361.07         | 822.26        | 35.46    | 0.0  | 0.0       | 857.72 | 0.0  | 0.0        | 0.0  | 857.72      |
      | 3  | 31   | 13 February 2024 |           | 2531.79         | 829.28        | 28.44    | 0.0  | 0.0       | 857.72 | 0.0  | 0.0        | 0.0  | 857.72      |
      | 4  | 29   | 13 March 2024    |           | 1694.11         | 837.68        | 20.04    | 0.0  | 0.0       | 857.72 | 0.0  | 0.0        | 0.0  | 857.72      |
      | 5  | 31   | 13 April 2024    |           | 850.72          | 843.39        | 14.33    | 0.0  | 0.0       | 857.72 | 0.0  | 0.0        | 0.0  | 857.72      |
      | 6  | 30   | 13 May 2024      |           | 0.0             | 850.72        | 6.97     | 0.0  | 0.0       | 857.69 | 0.0  | 0.0        | 0.0  | 857.69      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000.0        | 146.29   | 0.0  | 0.0       | 5146.29 | 0.0  | 0.0        | 0.0  | 5146.29     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 November 2023 | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
    When Admin sets the business date to "10 December 2023"
    And Customer makes "AUTOPAY" repayment on "10 December 2023" with 857.72 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 13 November 2023 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 13 December 2023 | 10 December 2023 | 4179.23         | 820.77        | 36.95    | 0.0  | 0.0       | 857.72 | 857.72 | 857.72     | 0.0  | 0.0         |
      | 2  | 31   | 13 January 2024  |                  | 3360.36         | 818.87        | 38.85    | 0.0  | 0.0       | 857.72 | 0.0    | 0.0        | 0.0  | 857.72      |
      | 3  | 31   | 13 February 2024 |                  | 2531.07         | 829.29        | 28.43    | 0.0  | 0.0       | 857.72 | 0.0    | 0.0        | 0.0  | 857.72      |
      | 4  | 29   | 13 March 2024    |                  | 1693.38         | 837.69        | 20.03    | 0.0  | 0.0       | 857.72 | 0.0    | 0.0        | 0.0  | 857.72      |
      | 5  | 31   | 13 April 2024    |                  | 849.99          | 843.39        | 14.33    | 0.0  | 0.0       | 857.72 | 0.0    | 0.0        | 0.0  | 857.72      |
      | 6  | 30   | 13 May 2024      |                  | 0.0             | 849.99        | 6.96     | 0.0  | 0.0       | 856.95 | 0.0    | 0.0        | 0.0  | 856.95      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 5000.0        | 145.55   | 0.0  | 0.0       | 5145.55 | 857.72 | 857.72     | 0.0  | 4287.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 November 2023 | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
      | 10 December 2023 | Repayment        | 857.72 | 820.77    | 36.95    | 0.0  | 0.0       | 4179.23      | false    | false    |

  @TestRailId:C3522
  Scenario: Verify that interestRecognitionOnDisbursementDate setting is taking into account by apply periodic accrual job - payment on time for each period
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 200.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 2  | 29   | 01 March 2024    |           | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 3  | 31   | 01 April 2024    |           | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |           | 67.44           | 33.43         | 0.59     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 5  | 31   | 01 June 2024     |           | 33.81           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 33.81         | 0.2      | 0.0  | 0.0       | 34.01 | 0.0  | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11 | 0.0  | 0.0        | 0.0  | 204.11      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    #run COB for 01st of Jan 2024
    And Admin runs inline COB job for Loan
    #move date to first period due - beginning of Feb 2024
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 34.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 3  | 31   | 01 April 2024    |                  | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 67.44           | 33.43         | 0.59     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 5  | 31   | 01 June 2024     |                  | 33.81           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 33.81         | 0.2      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11 | 34.02 | 0.0        | 0.0  | 170.09      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment        | 34.02  | 32.85     | 1.17     | 0.0  | 0.0       | 167.15       | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 34.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 67.44           | 33.43         | 0.59     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 5  | 31   | 01 June 2024     |                  | 33.81           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 33.81         | 0.2      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11 | 68.04 | 0.0        | 0.0  | 136.07      |
    When Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 34.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                  | 67.44           | 33.43         | 0.59     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 5  | 31   | 01 June 2024     |                  | 33.81           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 33.81         | 0.2      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11 | 102.06 | 0.0        | 0.0  | 102.05      |
    When Admin sets the business date to "01 May 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 34.02 EUR transaction amount
    #And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 67.44           | 33.43         | 0.59     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     |                  | 33.81           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 33.81         | 0.2      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11 | 136.08 | 0.0        | 0.0  | 68.03       |
    When Admin sets the business date to "01 June 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 34.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 67.44           | 33.43         | 0.59     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 33.81           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 33.81         | 0.2      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11 | 170.1 | 0.0        | 0.0  | 34.01       |
    When Admin sets the business date to "01 July 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 34.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 67.44           | 33.43         | 0.59     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 33.81           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 34.02 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 33.81         | 0.2      | 0.0  | 0.0       | 34.01 | 34.01 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11 | 204.11 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment        | 34.02  | 32.85     | 1.17     | 0.0  | 0.0       | 167.15       | false    | false    |
      | 01 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual Activity | 1.17   | 0.0       | 1.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Repayment        | 34.02  | 33.04     | 0.98     | 0.0  | 0.0       | 134.11       | false    | false    |
      | 01 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual Activity | 0.98   | 0.0       | 0.98     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Accrual          | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Repayment        | 34.02  | 33.24     | 0.78     | 0.0  | 0.0       | 100.87       | false    | false    |
      | 01 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Activity | 0.78   | 0.0       | 0.78     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2024      | Repayment        | 34.02  | 33.43     | 0.59     | 0.0  | 0.0       | 67.44        | false    | false    |
      | 01 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2024      | Accrual Activity | 0.59   | 0.0       | 0.59     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 May 2024      | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 June 2024     | Repayment        | 34.02  | 33.63     | 0.39     | 0.0  | 0.0       | 33.81        | false    | false    |
      | 01 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 June 2024     | Accrual Activity | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 June 2024     | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Repayment        | 34.01  | 33.81     | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual Activity | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3523
  Scenario: Verify that interestRecognitionOnDisbursementDate setting is taking into account by apply periodic accrual job - early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2024   | 2000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "2000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "2000" EUR transaction amount
    #run COB for 01st of Jan 2024
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 1671.5          | 328.5         | 11.67    | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 2  | 29   | 01 March 2024    |           | 1341.08         | 330.42        | 9.75     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 3  | 31   | 01 April 2024    |           | 1008.73         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 4  | 30   | 01 May 2024      |           | 674.44          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 5  | 31   | 01 June 2024     |           | 338.2           | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 338.2         | 1.97     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2000.0        | 41.02    | 0.0  | 0.0       | 2041.02 | 0.0  | 0.0        | 0.0  | 2041.02     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
    When Admin sets the business date to "15 January 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 1665.1          | 334.9         | 5.27     | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 1339.97         | 325.13        | 15.04    | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 3  | 31   | 01 April 2024    |                 | 1007.62         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 4  | 30   | 01 May 2024      |                 | 673.33          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 5  | 31   | 01 June 2024     |                 | 337.09          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 337.09        | 1.97     | 0.0  | 0.0       | 339.06 | 0.0    | 0.0        | 0.0  | 339.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 2000.0        | 39.91    | 0.0  | 0.0       | 2039.91 | 340.17 | 340.17     | 0.0  | 1699.74     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 01 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 340.17 | 334.9     | 5.27     | 0.0  | 0.0       | 1665.1       | false    | false    |
     #move date to first period due - beginning of Feb 2024
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 1665.1          | 334.9         | 5.27     | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.97         | 325.13        | 15.04    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 1007.62         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 4  | 30   | 01 May 2024      |                 | 673.33          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 5  | 31   | 01 June 2024     |                 | 337.09          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 337.09        | 1.97     | 0.0  | 0.0       | 339.06 | 0.0    | 0.0        | 0.0  | 339.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 2000.0        | 39.91    | 0.0  | 0.0       | 2039.91 | 680.34 | 340.17     | 0.0  | 1359.57     |
    When Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 1665.1          | 334.9         | 5.27     | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.97         | 325.13        | 15.04    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024   | 1007.62         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                 | 673.33          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 5  | 31   | 01 June 2024     |                 | 337.09          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 337.09        | 1.97     | 0.0  | 0.0       | 339.06 | 0.0    | 0.0        | 0.0  | 339.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 2000.0        | 39.91    | 0.0  | 0.0       | 2039.91 | 1020.51 | 340.17     | 0.0  | 1019.4      |
    When Admin sets the business date to "01 May 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 340.17 EUR transaction amount
    #And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 1665.1          | 334.9         | 5.27     | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.97         | 325.13        | 15.04    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024   | 1007.62         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024     | 673.33          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     |                 | 337.09          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 337.09        | 1.97     | 0.0  | 0.0       | 339.06 | 0.0    | 0.0        | 0.0  | 339.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 2000.0        | 39.91    | 0.0  | 0.0       | 2039.91 | 1360.68 | 340.17     | 0.0  | 679.23      |
    When Admin sets the business date to "01 June 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 1665.1          | 334.9         | 5.27     | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.97         | 325.13        | 15.04    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024   | 1007.62         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024     | 673.33          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024    | 337.09          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 337.09        | 1.97     | 0.0  | 0.0       | 339.06 | 0.0    | 0.0        | 0.0  | 339.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 2000.0        | 39.91    | 0.0  | 0.0       | 2039.91 | 1700.85 | 340.17     | 0.0  | 339.06      |
    When Admin sets the business date to "01 July 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 1665.1          | 334.9         | 5.27     | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.97         | 325.13        | 15.04    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024   | 1007.62         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024     | 673.33          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024    | 337.09          | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024    | 0.0             | 337.09        | 1.97     | 0.0  | 0.0       | 339.06 | 339.06 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 2000.0        | 39.91    | 0.0  | 0.0       | 2039.91 | 2039.91 | 340.17     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 01 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 340.17 | 334.9     | 5.27     | 0.0  | 0.0       | 1665.1       | false    | false    |
      | 15 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual Activity | 5.27   | 0.0       | 5.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Repayment        | 340.17 | 325.13    | 15.04    | 0.0  | 0.0       | 1339.97      | false    | false    |
      | 01 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual Activity | 15.04  | 0.0       | 15.04    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Repayment        | 340.17 | 332.35    | 7.82     | 0.0  | 0.0       | 1007.62      | false    | false    |
      | 01 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Activity | 7.82   | 0.0       | 7.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2024      | Repayment        | 340.17 | 334.29    | 5.88     | 0.0  | 0.0       | 673.33       | false    | false    |
      | 01 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2024      | Accrual Activity | 5.88   | 0.0       | 5.88     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 June 2024     | Repayment        | 340.17 | 336.24    | 3.93     | 0.0  | 0.0       | 337.09       | false    | false    |
      | 01 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 June 2024     | Accrual Activity | 3.93   | 0.0       | 3.93     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Repayment        | 340.17 | 337.09    | 1.97     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual Activity | 1.97   | 0.0       | 1.97     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3524
  Scenario: Verify that interestRecognitionOnDisbursementDate setting is taking into account by apply periodic accrual job - early repayment on disbursement date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2024   | 2000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "2000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "2000" EUR transaction amount
    #run COB for 01st of Jan 2024
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 1671.5          | 328.5         | 11.67    | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 2  | 29   | 01 March 2024    |           | 1341.08         | 330.42        | 9.75     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 3  | 31   | 01 April 2024    |           | 1008.73         | 332.35        | 7.82     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 4  | 30   | 01 May 2024      |           | 674.44          | 334.29        | 5.88     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 5  | 31   | 01 June 2024     |           | 338.2           | 336.24        | 3.93     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 338.2         | 1.97     | 0.0  | 0.0       | 340.17 | 0.0  | 0.0        | 0.0  | 340.17      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2000.0        | 41.02    | 0.0  | 0.0       | 2041.02 | 0.0  | 0.0        | 0.0  | 2041.02     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 340.17 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 January 2024 | 1659.83         | 340.17        | 0.0      | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 1339.02         | 320.81        | 19.36    | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 3  | 31   | 01 April 2024    |                 | 1006.66         | 332.36        | 7.81     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 4  | 30   | 01 May 2024      |                 | 672.36          | 334.3         | 5.87     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 5  | 31   | 01 June 2024     |                 | 336.11          | 336.25        | 3.92     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 336.11        | 1.96     | 0.0  | 0.0       | 338.07 | 0.0    | 0.0        | 0.0  | 338.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 2000.0        | 38.92    | 0.0  | 0.0       | 2038.92 | 340.17 | 340.17     | 0.0  | 1698.75     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 01 January 2024  | Repayment        | 340.17 | 340.17    | 0.0      | 0.0  | 0.0       | 1659.83       | false    | false    |
     #move date to first period due - beginning of Feb 2024
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 January 2024 | 1659.83         | 340.17        | 0.0      | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.02         | 320.81        | 19.36    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 1006.66         | 332.36        | 7.81     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 4  | 30   | 01 May 2024      |                 | 672.36          | 334.3         | 5.87     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 5  | 31   | 01 June 2024     |                 | 336.11          | 336.25        | 3.92     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 336.11        | 1.96     | 0.0  | 0.0       | 338.07 | 0.0    | 0.0        | 0.0  | 338.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 2000.0        | 38.92    | 0.0  | 0.0       | 2038.92 | 680.34 | 340.17     | 0.0  | 1358.58     |
    When Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 January 2024 | 1659.83         | 340.17        | 0.0      | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.02         | 320.81        | 19.36    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024   | 1006.66         | 332.36        | 7.81     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                 | 672.36          | 334.3         | 5.87     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 5  | 31   | 01 June 2024     |                 | 336.11          | 336.25        | 3.92     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 336.11        | 1.96     | 0.0  | 0.0       | 338.07 | 0.0    | 0.0        | 0.0  | 338.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 2000.0        | 38.92    | 0.0  | 0.0       | 2038.92 | 1020.51 | 340.17     | 0.0  | 1018.41     |
    When Admin sets the business date to "01 May 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 340.17 EUR transaction amount
    #And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 January 2024 | 1659.83         | 340.17        | 0.0      | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.02         | 320.81        | 19.36    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024   | 1006.66         | 332.36        | 7.81     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024     | 672.36          | 334.3         | 5.87     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     |                 | 336.11          | 336.25        | 3.92     | 0.0  | 0.0       | 340.17 | 0.0    | 0.0        | 0.0  | 340.17      |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 336.11        | 1.96     | 0.0  | 0.0       | 338.07 | 0.0    | 0.0        | 0.0  | 338.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 2000.0        | 38.92    | 0.0  | 0.0       | 2038.92 | 1360.68 | 340.17     | 0.0  | 678.24     |
    When Admin sets the business date to "01 June 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 January 2024 | 1659.83         | 340.17        | 0.0      | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.02         | 320.81        | 19.36    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024   | 1006.66         | 332.36        | 7.81     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024     | 672.36          | 334.3         | 5.87     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024    | 336.11          | 336.25        | 3.92     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 336.11        | 1.96     | 0.0  | 0.0       | 338.07 | 0.0    | 0.0        | 0.0  | 338.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 2000.0        | 38.92    | 0.0  | 0.0       | 2038.92 | 1700.85 | 340.17     | 0.0  | 338.07      |
    When Admin sets the business date to "01 July 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 340.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 January 2024 | 1659.83         | 340.17        | 0.0      | 0.0  | 0.0       | 340.17 | 340.17 | 340.17     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024   | 1339.02         | 320.81        | 19.36    | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024   | 1006.66         | 332.36        | 7.81     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024     | 672.36          | 334.3         | 5.87     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024    | 336.11          | 336.25        | 3.92     | 0.0  | 0.0       | 340.17 | 340.17 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024    | 0.0             | 336.11        | 1.96     | 0.0  | 0.0       | 338.07 | 338.07 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 2000.0        | 38.92    | 0.0  | 0.0       | 2038.92 | 2038.92 | 340.17     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 01 January 2024  | Repayment        | 340.17 | 340.17    | 0.0      | 0.0  | 0.0       | 1659.83      | false    | false    |
      | 01 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Repayment        | 340.17 | 320.81    | 19.36    | 0.0  | 0.0       | 1339.02      | false    | false    |
      | 01 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual Activity | 19.36  | 0.0       | 19.36    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Repayment        | 340.17 | 332.36    | 7.81     | 0.0  | 0.0       | 1006.66      | false    | false    |
      | 01 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Activity | 7.81   | 0.0       | 7.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2024      | Repayment        | 340.17 | 334.3     | 5.87     | 0.0  | 0.0       | 672.36       | false    | false    |
      | 01 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2024      | Accrual Activity | 5.87   | 0.0       | 5.87     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 May 2024      | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 May 2024      | Accrual          | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 June 2024     | Repayment        | 340.17 | 336.25    | 3.92     | 0.0  | 0.0       | 336.11       | false    | false    |
      | 01 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 June 2024     | Accrual Activity | 3.92   | 0.0       | 3.92     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 June 2024     | Accrual          | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 June 2024     | Accrual          | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Repayment        | 340.17 | 336.11    | 1.96     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual Activity | 1.96   | 0.0       | 1.96     | 0.0  | 0.0       | 0.0          | false    | false    |