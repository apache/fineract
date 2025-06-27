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

  @TestRailId:3709
  Scenario: Verify accrual and accrual activity after backdated payoff with overdue installments on progressive loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ACCRUAL_ACTIVITY_POSTING | 01 January 2024   | 1000           | 49.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | WEEKS                 | 1              | WEEKS                  | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "19 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type  | Amount  | Principal  | Interest  | Fees  | Penalties  | Loan Balance  | Reverted  | Replayed  |
      | 01 January 2024   | Disbursement      | 1000.0  | 0.0        | 0.0       | 0.0   | 0.0        | 1000.0        | false     | false     |
      | 02 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 03 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 04 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 05 January 2024   | Accrual           | 1.38    | 0.0        | 1.38      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 06 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 07 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 08 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 08 January 2024   | Accrual Activity  | 9.72    | 0.0        | 9.72      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 09 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 10 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 12 January 2024   | Accrual           | 1.38    | 0.0        | 1.38      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 13 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 14 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 15 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 15 January 2024   | Accrual Activity  | 9.72    | 0.0        | 9.72      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 16 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 17 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 18 January 2024   | Accrual           | 1.39    | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
    When Loan Pay-off is made on "11 January 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type    | Amount   | Principal  | Interest  | Fees  | Penalties  | Loan Balance  | Reverted  | Replayed  |
      | 01 January 2024   | Disbursement        | 1000.0   | 0.0        | 0.0       | 0.0   | 0.0        | 1000.0        | false     | false     |
      | 02 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 03 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 04 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 05 January 2024   | Accrual             | 1.38     | 0.0        | 1.38      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 06 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 07 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 08 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 08 January 2024   | Accrual Activity    | 9.72     | 0.0        | 9.72      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 09 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 10 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Accrual Activity    | 4.17     | 0.0        | 4.17      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Repayment           | 1013.89  | 1000.0     | 13.89     | 0.0   | 0.0        | 0.0           | false     | false     |
      | 12 January 2024   | Accrual             | 1.38     | 0.0        | 1.38      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 13 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 14 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 15 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 16 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 17 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 18 January 2024   | Accrual             | 1.39     | 0.0        | 1.39      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 19 January 2024   | Accrual Adjustment  | 9.72     | 0.0        | 9.72      | 0.0   | 0.0        | 0.0           | false     | false     |

  @TestRailId:3710
  Scenario: Verify accrual and accrual activity after backdated payoff with overdue installments on cumulative loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "16 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type  | Amount  | Principal  | Interest  | Fees  | Penalties  | Loan Balance  | Reverted  | Replayed  |
      | 01 January 2024   | Disbursement      | 1000.0  | 0.0        | 0.0       | 0.0   | 0.0        | 1000.0        | false     | false     |
      | 02 January 2024   | Accrual           | 0.33    | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 03 January 2024   | Accrual           | 0.33    | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 04 January 2024   | Accrual           | 0.32    | 0.0        | 0.32      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 05 January 2024   | Accrual           | 0.33    | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 06 January 2024   | Accrual           | 0.33    | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 06 January 2024   | Accrual Activity  | 1.64    | 0.0        | 1.64      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 07 January 2024   | Accrual           | 0.28    | 0.0        | 0.28      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 08 January 2024   | Accrual           | 0.31    | 0.0        | 0.31      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 09 January 2024   | Accrual           | 0.35    | 0.0        | 0.35      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 10 January 2024   | Accrual           | 0.37    | 0.0        | 0.37      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Accrual           | 0.33    | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Accrual Activity  | 1.64    | 0.0        | 1.64      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 12 January 2024   | Accrual           | 0.23    | 0.0        | 0.23      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 13 January 2024   | Accrual           | 0.3     | 0.0        | 0.3       | 0.0   | 0.0        | 0.0           | false     | false     |
      | 14 January 2024   | Accrual           | 0.36    | 0.0        | 0.36      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 15 January 2024   | Accrual           | 0.42    | 0.0        | 0.42      | 0.0   | 0.0        | 0.0           | false     | false     |
    When Loan Pay-off is made on "11 January 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type  | Amount   | Principal  | Interest  | Fees  | Penalties  | Loan Balance  | Reverted  | Replayed  |
      | 01 January 2024   | Disbursement      | 1000.0   | 0.0        | 0.0       | 0.0   | 0.0        | 1000.0        | false     | false     |
      | 02 January 2024   | Accrual           | 0.33     | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 03 January 2024   | Accrual           | 0.33     | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 04 January 2024   | Accrual           | 0.32     | 0.0        | 0.32      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 05 January 2024   | Accrual           | 0.33     | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 06 January 2024   | Accrual           | 0.33     | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 06 January 2024   | Accrual Activity  | 1.64     | 0.0        | 1.64      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 07 January 2024   | Accrual           | 0.28     | 0.0        | 0.28      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 08 January 2024   | Accrual           | 0.31     | 0.0        | 0.31      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 09 January 2024   | Accrual           | 0.35     | 0.0        | 0.35      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 10 January 2024   | Accrual           | 0.37     | 0.0        | 0.37      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Accrual           | 0.33     | 0.0        | 0.33      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Accrual Activity  | 1.64     | 0.0        | 1.64      | 0.0   | 0.0        | 0.0           | false     | false     |
      | 11 January 2024   | Repayment         | 1003.28  | 1000.0     | 3.28      | 0.0   | 0.0        | 0.0           | false     | false     |

  @TestRailId:3711
  Scenario: Verify accrual and accrual activity after backdated payoff on cumulative loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 20                | DAYS                  | 5              | DAYS                   | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "06 January 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 251.0 EUR transaction amount
    When Admin sets the business date to "11 January 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "11 January 2024" with 251.0 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024   | Accrual           | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024   | Accrual           | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024   | Accrual           | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024   | Accrual           | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024   | Repayment         | 251.0  | 249.36    | 1.64     | 0.0  | 0.0       | 750.64       | false    | false    |
      | 06 January 2024   | Accrual           | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024   | Accrual Activity  | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024   | Accrual           | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024   | Accrual           | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024   | Accrual           | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024   | Accrual           | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024   | Repayment         | 251.0  | 249.77    | 1.23     | 0.0  | 0.0       | 500.87       | false    | false    |
      | 11 January 2024   | Accrual           | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024   | Accrual Activity  | 1.23   | 0.0       | 1.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024   | Accrual           | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024   | Accrual           | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024   | Accrual           | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024   | Accrual           | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "11 January 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024   | Accrual           | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024   | Accrual           | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024   | Accrual           | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024   | Accrual           | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024   | Repayment         | 251.0  | 249.36    | 1.64     | 0.0  | 0.0       | 750.64       | false    | false    |
      | 06 January 2024   | Accrual           | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024   | Accrual Activity  | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024   | Accrual           | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024   | Accrual           | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024   | Accrual           | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024   | Accrual           | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024   | Repayment         | 251.0  | 249.77    | 1.23     | 0.0  | 0.0       | 500.87       | false    | false    |
      | 11 January 2024   | Accrual           | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024   | Accrual Activity  | 1.23   | 0.0       | 1.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024   | Repayment         | 500.87 | 500.87    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |

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
    Then Loan Product response contains interestRecognitionOnDisbursementDate flag with value "true"
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
      | 16 January 2025  | Accrual Activity | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
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
      | 16 January 2025  | Accrual Activity | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
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
      | 16 January 2025  | Accrual Activity | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
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
      | 16 January 2025  | Accrual Activity | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
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
    Then Loan Product response contains interestRecognitionOnDisbursementDate flag with value "true"
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
    Then Loan Product response contains interestRecognitionOnDisbursementDate flag with value "true"
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
    Then Loan Product response contains interestRecognitionOnDisbursementDate flag with value "true"
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
      | 01 January 2024  | Repayment        | 340.17 | 340.17    | 0.0      | 0.0  | 0.0       | 1659.83      | false    | false    |
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
      | 2000.0        | 38.92    | 0.0  | 0.0       | 2038.92 | 1360.68 | 340.17     | 0.0  | 678.24      |
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


  @TestRailId:C3525
  Scenario: Verify accrual activity behavior in case of backdated repayment - UC1
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    And Admin sets the business date to "02 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.46          | 132.14        | 3.93     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.52          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.8           | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.3           | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.3         | 0.79     | 0.0  | 0.0       | 136.09 | 0.0  | 0.0        | 0.0  | 136.09      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.44    | 0.0  | 0.0       | 816.44 | 0.0  | 0.0        | 0.0  | 816.44      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 01 February 2025 | Accrual          | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Save external ID of "Accrual Activity" transaction made on "01 February 2025" as "saved-external-id"
    When Customer makes "AUTOPAY" repayment on "31 January 2025" with 900 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 31 January 2025 | 668.45          | 131.55        | 4.52     | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 31 January 2025 | 532.38          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 31 January 2025 | 396.31          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 31 January 2025 | 260.24          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025     | 31 January 2025 | 124.17          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025     | 31 January 2025 | 0.0             | 124.17        | 0.0      | 0.0  | 0.0       | 124.17 | 124.17 | 124.17     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.00        | 4.52     | 0.0  | 0.0       | 804.52 | 804.52 | 804.52     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 31 January 2025  | Accrual Activity   | 4.52   | 0.0       | 4.52     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 31 January 2025  | Repayment          | 900.0  | 800.0     | 4.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual            | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual Adjustment | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
    And "Accrual Activity" transaction on "31 January 2025" got reverse-replayed on "02 February 2025"
    And LoanAccrualAdjustmentTransactionBusinessEvent is raised on "02 February 2025"
    And External ID of replayed "Accrual Activity" on "31 January 2025" is matching with "saved-external-id"

  @TestRailId:C3526
  Scenario: Verify accrual activity behavior in case of repayment reversal on an overpaid loan - UC2
    When Admin sets the business date to "25 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 25 February 2025  | 75.71          | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "25 February 2025" with "75.71" amount and expected disbursement date on "25 February 2025"
    And Admin successfully disburse the loan on "25 February 2025" with "75.71" EUR transaction amount
    And Admin sets the business date to "10 March 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 25 February 2025 |           | 75.71           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 28   | 25 March 2025    |           | 63.27           | 12.44         | 0.44     | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 2  | 31   | 25 April 2025    |           | 50.76           | 12.51         | 0.37     | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 3  | 30   | 25 May 2025      |           | 38.18           | 12.58         | 0.3      | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 4  | 31   | 25 June 2025     |           | 25.52           | 12.66         | 0.22     | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 5  | 30   | 25 July 2025     |           | 12.79           | 12.73         | 0.15     | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 6  | 31   | 25 August 2025   |           | 0.0             | 12.79         | 0.07     | 0.0  | 0.0       | 12.86 | 0.0  | 0.0        | 0.0  | 12.86       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 75.71         | 1.55     | 0.0  | 0.0       | 77.26 | 0.0  | 0.0        | 0.0  | 77.26       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 25 February 2025 | Disbursement     | 75.71  | 0.0       | 0.0      | 0.0  | 0.0       | 75.71        | false    | false    |
      | 09 March 2025    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer makes "AUTOPAY" repayment on "10 March 2025" with 80 EUR transaction amount
    And Admin sets the business date to "28 March 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 25 February 2025 |               | 75.71           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 28   | 25 March 2025    | 10 March 2025 | 63.04           | 12.67         | 0.21     | 0.0  | 0.0       | 12.88 | 12.88 | 12.88      | 0.0  | 0.0         |
      | 2  | 31   | 25 April 2025    | 10 March 2025 | 50.16           | 12.88         | 0.0      | 0.0  | 0.0       | 12.88 | 12.88 | 12.88      | 0.0  | 0.0         |
      | 3  | 30   | 25 May 2025      | 10 March 2025 | 37.28           | 12.88         | 0.0      | 0.0  | 0.0       | 12.88 | 12.88 | 12.88      | 0.0  | 0.0         |
      | 4  | 31   | 25 June 2025     | 10 March 2025 | 24.4            | 12.88         | 0.0      | 0.0  | 0.0       | 12.88 | 12.88 | 12.88      | 0.0  | 0.0         |
      | 5  | 30   | 25 July 2025     | 10 March 2025 | 11.52           | 12.88         | 0.0      | 0.0  | 0.0       | 12.88 | 12.88 | 12.88      | 0.0  | 0.0         |
      | 6  | 31   | 25 August 2025   | 10 March 2025 | 0.0             | 11.52         | 0.0      | 0.0  | 0.0       | 11.52 | 11.52 | 11.52      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 75.71         | 0.21     | 0.0  | 0.0       | 75.92 | 75.92 | 75.92      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 25 February 2025 | Disbursement     | 75.71  | 0.0       | 0.0      | 0.0  | 0.0       | 75.71        | false    | false    |
      | 09 March 2025    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2025    | Repayment        | 80.0   | 75.71     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2025    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2025    | Accrual Activity | 0.21   | 0.0       | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Save external ID of "Accrual Activity" transaction made on "10 March 2025" as "saved-external-id"
    When Customer undo "1"th "Repayment" transaction made on "10 March 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 25 February 2025 |           | 75.71           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 28   | 25 March 2025    |           | 63.27           | 12.44         | 0.44     | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 2  | 31   | 25 April 2025    |           | 50.77           | 12.5          | 0.38     | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 3  | 30   | 25 May 2025      |           | 38.19           | 12.58         | 0.3      | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 4  | 31   | 25 June 2025     |           | 25.53           | 12.66         | 0.22     | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 5  | 30   | 25 July 2025     |           | 12.8            | 12.73         | 0.15     | 0.0  | 0.0       | 12.88 | 0.0  | 0.0        | 0.0  | 12.88       |
      | 6  | 31   | 25 August 2025   |           | 0.0             | 12.8          | 0.07     | 0.0  | 0.0       | 12.87 | 0.0  | 0.0        | 0.0  | 12.87       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 75.71         | 1.56     | 0.0  | 0.0       | 77.27 | 0.0  | 0.0        | 0.0  | 77.27       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 25 February 2025 | Disbursement     | 75.71  | 0.0       | 0.0      | 0.0  | 0.0       | 75.71        | false    | false    |
      | 09 March 2025    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2025    | Repayment        | 80.0   | 75.71     | 0.21     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 10 March 2025    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Accrual Activity | 0.44   | 0.0       | 0.44     | 0.0  | 0.0       | 0.0          | false    | true     |
    And "Accrual Activity" transaction on "25 March 2025" got reverse-replayed on "28 March 2025"
    And External ID of replayed "Accrual Activity" on "25 March 2025" is matching with "saved-external-id"

  @TestRailId:C3527
  Scenario: Verify accrual activity behavior in case repayment reversal before the installment date - UC3
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.43          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.49          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.77          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.27          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.27        | 0.79     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.41    | 0.0  | 0.0       | 816.41 | 0.0  | 0.0        | 0.0  | 816.41      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    When Admin sets the business date to "27 January 2025"
    And Customer makes "AUTOPAY" repayment on "27 January 2025" with 803.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 27 January 2025 | 667.84          | 132.16        | 3.91     | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 27 January 2025 | 531.77          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 27 January 2025 | 395.7           | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 27 January 2025 | 259.63          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025     | 27 January 2025 | 123.56          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025     | 27 January 2025 | 0.0             | 123.56        | 0.0      | 0.0  | 0.0       | 123.56 | 123.56 | 123.56     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.00        | 3.91     | 0.0  | 0.0       | 803.91 | 803.91 | 803.91     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 803.91 | 800.0     | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual Activity | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 January 2025"
    And Customer undo "1"th "Repayment" transaction made on "27 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.43          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.49          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.77          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.27          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.27        | 0.79     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.41    | 0.0  | 0.0       | 816.41 | 0.0  | 0.0        | 0.0  | 816.41      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 803.91 | 800.0     | 3.91     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 27 January 2025  | Accrual          | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3528
  Scenario: Verify accrual activity behavior in case repayment reversal after the installment date - UC4
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.43          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.49          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.77          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.27          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.27        | 0.79     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.41    | 0.0  | 0.0       | 816.41 | 0.0  | 0.0        | 0.0  | 816.41      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    When Admin sets the business date to "27 January 2025"
    And Customer makes "AUTOPAY" repayment on "27 January 2025" with 816.46 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 27 January 2025 | 667.84          | 132.16        | 3.91     | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 27 January 2025 | 531.77          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 27 January 2025 | 395.7           | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 27 January 2025 | 259.63          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025     | 27 January 2025 | 123.56          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025     | 27 January 2025 | 0.0             | 123.56        | 0.0      | 0.0  | 0.0       | 123.56 | 123.56 | 123.56     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.00        | 3.91     | 0.0  | 0.0       | 803.91 | 803.91 | 803.91     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 816.46 | 800.0     | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual Activity | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Save external ID of "Accrual Activity" transaction made on "27 January 2025" as "saved-external-id"
    When Admin sets the business date to "03 February 2025"
    And Customer undo "1"th "Repayment" transaction made on "27 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.48          | 132.12        | 3.95     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.54          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.82          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.32          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.32        | 0.79     | 0.0  | 0.0       | 136.11 | 0.0  | 0.0        | 0.0  | 136.11      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.46    | 0.0  | 0.0       | 816.46 | 0.0  | 0.0        | 0.0  | 816.46      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 816.46 | 800.0     | 3.91     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 27 January 2025  | Accrual          | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | true     |
    And "Accrual Activity" transaction on "01 February 2025" got reverse-replayed on "03 February 2025"
    And External ID of replayed "Accrual Activity" on "01 February 2025" is matching with "saved-external-id"

  @TestRailId:C3529
  Scenario: Verify accrual activity behavior when COB runs on installment date and backdated repayment happens - UC5
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    When Admin sets the business date to "02 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.46          | 132.14        | 3.93     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.52          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.8           | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.3           | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.3         | 0.79     | 0.0  | 0.0       | 136.09 | 0.0  | 0.0        | 0.0  | 136.09      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.44    | 0.0  | 0.0       | 816.44 | 0.0  | 0.0        | 0.0  | 816.44      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 01 February 2025 | Accrual          | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Save external ID of "Accrual Activity" transaction made on "01 February 2025" as "saved-external-id"
    When Customer makes "AUTOPAY" repayment on "27 January 2025" with 800 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 27 January 2025 | 667.84          | 132.16        | 3.91     | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 27 January 2025 | 531.77          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 27 January 2025 | 395.7           | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 27 January 2025 | 259.63          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025     | 27 January 2025 | 123.56          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025     |                 | 0.0             | 123.56        | 0.11     | 0.0  | 0.0       | 123.67 | 119.65 | 119.65     | 0.0  | 4.02        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 800.00        | 4.02     | 0.0  | 0.0       | 804.02 | 800.0 | 800.0      | 0.0  | 4.02        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 800.0  | 796.09    | 3.91     | 0.0  | 0.0       | 3.91         | false    | false    |
      | 01 February 2025 | Accrual          | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | true     |
    And "Accrual Activity" transaction on "01 February 2025" got reverse-replayed on "02 February 2025"
    And External ID of replayed "Accrual Activity" on "01 February 2025" is matching with "saved-external-id"

  @TestRailId:C3530
  Scenario: Verify accrual activity behavior in case of partial payment which is reversed after the installment date - UC6
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    And Admin sets the business date to "15 January 2025"
    And Customer makes "AUTOPAY" repayment on "15 January 2025" with 100 EUR transaction amount
    And Admin sets the business date to "02 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.28          | 131.72        | 4.35     | 0.0  | 0.0       | 136.07 | 100.0 | 100.0      | 0.0  | 36.07       |
      | 2  | 28   | 01 March 2025    |           | 536.11          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0   | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.17          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0   | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.45          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0   | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 134.95          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0   | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 134.95        | 0.79     | 0.0  | 0.0       | 135.74 | 0.0   | 0.0        | 0.0  | 135.74      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 800.00        | 16.09    | 0.0  | 0.0       | 816.09 | 100.0 | 100.0      | 0.0  | 716.09      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 15 January 2025  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 February 2025 | Accrual          | 4.35   | 0.0       | 4.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.35   | 0.0       | 4.35     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Save external ID of "Accrual Activity" transaction made on "01 February 2025" as "saved-external-id"
    When Customer undo "1"th "Repayment" transaction made on "15 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.46          | 132.14        | 3.93     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.52          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.8           | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.3           | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.3         | 0.79     | 0.0  | 0.0       | 136.09 | 0.0  | 0.0        | 0.0  | 136.09      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.44    | 0.0  | 0.0       | 816.44 | 0.0  | 0.0        | 0.0  | 816.44      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 15 January 2025  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 700.0        | true     | false    |
      | 01 February 2025 | Accrual          | 4.35   | 0.0       | 4.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | true     |
    And "Accrual Activity" transaction on "01 February 2025" got reverse-replayed on "02 February 2025"
    And External ID of replayed "Accrual Activity" on "01 February 2025" is matching with "saved-external-id"

  @TestRailId:C3533
  Scenario: Logging out transaction list, excluded given transaction types
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 2000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "2000" EUR transaction amount
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 January 2025"
    And Customer makes "AUTOPAY" repayment on "05 January 2025" with 200 EUR transaction amount
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 01 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 1800.0       | false    | false    |
    Then Log out transaction list by loanId, filtered out the following transaction types: "disbursement, accrual"
    Then Log out transaction list by loanExternalId, filtered out the following transaction types: "accrual"
    Then Filtered out transactions list contains the the following entries when filtered out by loanId for transaction types: "accrual"
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 05 January 2025  | repayment        | 200.0  | 200.0     |          |      |           | 1800.0       |
    Then Filtered out transactions list contains the the following entries when filtered out by loanExternalId for transaction types: "accrual"
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 05 January 2025  | repayment        | 200.0  | 200.0     |          |      |           | 1800.0       |
    Then Filtered out transactions list contains the the following entries when filtered out by loanId for transaction types: ""
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 01 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 02 January 2025  | accrual          | 0.37   |           | 0.37     |      |           |              |
      | 03 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 04 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 05 January 2025  | repayment        | 200.0  | 200.0     |          |      |           | 1800.0       |
    Then Filtered out transactions list contains the the following entries when filtered out by loanId for transaction types: "merchant_issued_refund"
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 01 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 02 January 2025  | accrual          | 0.37   |           | 0.37     |      |           |              |
      | 03 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 04 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 05 January 2025  | repayment        | 200.0  | 200.0     |          |      |           | 1800.0       |
    Then Filtered out transactions list has 4 pages in case of size set to 1 and transactions are filtered out for transaction types: "disbursement, repayment"

  @TestRailId:C3692
  Scenario: Verify that accruals are added in case of reversed repayment made before MIR and CBR for progressive loan with downpayment - UC1
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     |               | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  |  0.0  | 0.0        | 0.0  | 60.62       |
      | 2  | 31   | 21 April 2025     |               | 168.65          | 13.19         | 4.54     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 3  | 30   | 21 May 2025       |               | 155.13          | 13.52         | 4.21     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 4  | 31   | 21 June 2025      |               | 141.28          | 13.85         | 3.88     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 5  | 30   | 21 July 2025      |               | 127.08          | 14.2          | 3.53     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 6  | 31   | 21 August 2025    |               | 112.53          | 14.55         | 3.18     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 7  | 31   | 21 September 2025 |               |  97.61          | 14.92         | 2.81     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 8  | 30   | 21 October 2025   |               |  82.32          | 15.29         | 2.44     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 9  | 31   | 21 November 2025  |               |  66.65          | 15.67         | 2.06     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 10 | 30   | 21 December 2025  |               |  50.59          | 16.06         | 1.67     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 11 | 31   | 21 January 2026   |               |  34.12          | 16.47         | 1.26     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 12 | 31   | 21 February 2026  |               |  17.24          | 16.88         | 0.85     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 13 | 28   | 21 March 2026     |               |   0.0           | 17.24         | 0.43     | 0.0  | 0.0       | 17.67  |  0.0  | 0.0        | 0.0  | 17.67       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 30.86    | 0.0  | 0.0       | 273.32 | 0.0    | 0.0        | 0.0  | 273.32      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 1.93     | 0.0  | 0.0       | 117.08 | 15.15 | 15.15      | 0.0  | 101.93      |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  90.94          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  75.79          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  60.64          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  45.49          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |  30.34          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |  15.19          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 242.46 | 181.84     | 0.0  | 101.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 14 April 2025    | Accrual                | 1.37   | 0.0       | 1.37     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true    |
      | 14 April 2025    | Accrual                | 1.37   | 0.0       | 1.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3693
  Scenario: Verify that accruals are added in case of reversed repayment made before MIR and CBR for progressive loan with auto downpayment - UC2
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  |  0.0        |
      | 2  | 31   | 21 April 2025     |               | 168.65          | 13.19         | 4.54     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 3  | 30   | 21 May 2025       |               | 155.13          | 13.52         | 4.21     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 4  | 31   | 21 June 2025      |               | 141.28          | 13.85         | 3.88     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 5  | 30   | 21 July 2025      |               | 127.08          | 14.2          | 3.53     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 6  | 31   | 21 August 2025    |               | 112.53          | 14.55         | 3.18     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 7  | 31   | 21 September 2025 |               |  97.61          | 14.92         | 2.81     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 8  | 30   | 21 October 2025   |               |  82.32          | 15.29         | 2.44     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 9  | 31   | 21 November 2025  |               |  66.65          | 15.67         | 2.06     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 10 | 30   | 21 December 2025  |               |  50.59          | 16.06         | 1.67     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 11 | 31   | 21 January 2026   |               |  34.12          | 16.47         | 1.26     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 12 | 31   | 21 February 2026  |               |  17.24          | 16.88         | 0.85     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 13 | 28   | 21 March 2026     |               |   0.0           | 17.24         | 0.43     | 0.0  | 0.0       | 17.67  |  0.0  | 0.0        | 0.0  | 17.67       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 30.86    | 0.0  | 0.0       | 273.32 | 60.62  | 0.0        | 0.0  | 212.7       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 160.62 overpaid amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 81.84        | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 81.84     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 160.62 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 81.84        | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 81.84     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Credit Balance Refund  | 160.62 | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 164.11          | 178.35        | 1.93     | 0.0  | 0.0       | 180.28 | 78.35 | 78.35      | 0.0  | 101.93        |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 403.08        | 1.93     | 0.0  | 0.0       | 405.01 | 303.08 | 242.46     | 0.0  | 101.93        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 81.84        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 181.84    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 160.62 | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 14 April 2025    | Accrual                | 1.37   | 0.0       | 1.37     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 81.84        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 181.84    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 160.62 | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 14 April 2025    | Accrual                | 1.37   | 0.0       | 1.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3694
  Scenario: Verify repayment and accruals are added after reversed repayment made before MIR and CBR for progressive loan - UC3
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 224.88          | 17.58         | 6.06     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 2  | 30   | 21 May 2025       |               | 206.86          | 18.02         | 5.62     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 3  | 31   | 21 June 2025      |               | 188.39          | 18.47         | 5.17     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 4  | 30   | 21 July 2025      |               | 169.46          | 18.93         | 4.71     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 5  | 31   | 21 August 2025    |               | 150.06          | 19.4          | 4.24     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 6  | 31   | 21 September 2025 |               | 130.17          | 19.89         | 3.75     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 7  | 30   | 21 October 2025   |               | 109.78          | 20.39         | 3.25     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 8  | 31   | 21 November 2025  |               |  88.88          | 20.9          | 2.74     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 9  | 30   | 21 December 2025  |               |  67.46          | 21.42         | 2.22     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 10 | 31   | 21 January 2026   |               |  45.51          | 21.95         | 1.69     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 11 | 31   | 21 February 2026  |               |  23.01          | 22.5          | 1.14     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 23.01         | 0.58     | 0.0  | 0.0       | 23.59  |  0.0  | 0.0        | 0.0  | 23.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 41.17    | 0.0  | 0.0       | 283.63 | 0.0    | 0.0        | 0.0  | 283.63     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 40 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 60 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 March 2025 | 218.82          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06 |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 242.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | false    | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 March 2025 | 218.82          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06 |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 242.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | false    | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 218.82          | 123.64        | 0.77     | 0.0  | 0.0       | 124.41 | 83.64 | 83.64      | 0.0  | 40.77       |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06 |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 0.77     | 0.0  | 0.0       | 343.23 | 302.46 | 302.46     | 0.0  | 40.77       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | true     | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 182.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 182.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 40.0      | 0.0      | 0.0  | 0.0       | 40.0         | false    | true     |
      | 14 April 2025    | Accrual                | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 80 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0   |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 218.82          | 123.64        | 0.77     | 0.0  | 0.0       | 124.41 | 124.41 | 83.64      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06  |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0   |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 0.77     | 0.0  | 0.0       | 343.23 | 343.23 | 302.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | true     | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 182.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 182.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 40.0      | 0.0      | 0.0  | 0.0       | 40.0         | false    | true     |
      | 14 April 2025    | Accrual                | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Repayment              | 80.0   | 40.0      | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "23 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 April 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 218.82          | 123.64        | 0.84     | 0.0  | 0.0       | 124.48 | 83.64 | 83.64      | 0.0  | 40.84       |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06 |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 0.84     | 0.0  | 0.0       | 343.3  | 302.46 | 302.46     | 0.0  | 40.84       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | true     | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 182.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 182.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 40.0      | 0.0      | 0.0  | 0.0       | 40.0         | false    | true     |
      | 14 April 2025    | Accrual                | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Repayment              | 80.0   | 40.0      | 0.77     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 21 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3695
  Scenario: Verify accrual activity of overpaid loan in case of reversed repayment made before MIR and CBR for loan with interest refund - UC4
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 224.98          | 17.48         | 6.18     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 2  | 30   | 21 May 2025       |               | 206.87          | 18.11         | 5.55     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 3  | 31   | 21 June 2025      |               | 188.48          | 18.39         | 5.27     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 4  | 30   | 21 July 2025      |               | 169.47          | 19.01         | 4.65     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 5  | 31   | 21 August 2025    |               | 150.13          | 19.34         | 4.32     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 6  | 31   | 21 September 2025 |               | 130.29          | 19.84         | 3.82     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 7  | 30   | 21 October 2025   |               | 109.84          | 20.45         | 3.21     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 8  | 31   | 21 November 2025  |               |  88.98          | 20.86         | 2.8      | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 9  | 30   | 21 December 2025  |               |  67.51          | 21.47         | 2.19     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 10 | 31   | 21 January 2026   |               |  45.57          | 21.94         | 1.72     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 11 | 31   | 21 February 2026  |               |  23.07          | 22.5          | 1.16     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 23.07         | 0.53     | 0.0  | 0.0       | 23.6   |  0.0  | 0.0        | 0.0  | 23.6        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 41.4     | 0.0  | 0.0       | 283.86 | 0.0    | 0.0        | 0.0  | 283.86      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 142.46 EUR transaction amount and system-generated Idempotency key
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 March 2025 | 218.8           | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.14          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.48          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.82          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.16          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.5           | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.84          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.18          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.52          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   5.86          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  5.86         | 0.0      | 0.0  | 0.0       |  5.86  |  5.86 |  5.86      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 242.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 21 March 2025    | Repayment              | 200.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "21 March 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 221.85          | 120.61        | 3.05     | 0.0  | 0.0       | 123.66 | 23.66 | 23.66      | 0.0  | 100.0       |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 198.19          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 174.53          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 150.87          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 127.21          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 103.55          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  79.89          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  56.23          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  |               |  41.69          | 14.54         | 9.12     | 0.0  | 0.0       | 23.66  | 10.72 | 10.72      | 0.0  | 12.94       |
      | 10 | 31   | 21 January 2026   |               |  19.09          | 22.6          | 1.06     | 0.0  | 0.0       | 23.66  |  0.0  |  0.0       | 0.0  | 23.66       |
      | 11 | 31   | 21 February 2026  |               |   0.0           | 19.09         | 0.49     | 0.0  | 0.0       | 19.58  |  0.0  |  0.0       | 0.0  | 19.58       |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  |  0.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 13.72    | 0.0  | 0.0       | 356.18 | 200.0  | 200.0      | 0.0  | 156.18      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 March 2025    | Repayment              | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | true     |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 April 2025" with 100 EUR transaction amount and system-generated Idempotency key
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 156.7 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 221.85          | 120.61        | 3.05     | 0.0  | 0.0       | 123.66 | 123.66| 23.66      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 198.19          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 174.53          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 150.87          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 127.21          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 103.55          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  79.89          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  56.23          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 April 2025 |  32.57          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 April 2025 |   8.91          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 April 2025 |   0.0           |  8.91         | 0.0      | 0.0  | 0.0       |  8.91  |  8.91 |  8.91      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 3.05     | 0.0  | 0.0       | 345.51 | 345.51 | 245.51     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 March 2025    | Repayment              | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | true     |
      | 20 April 2025    | Accrual                | 2.94   | 0.0       | 2.94     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Merchant Issued Refund | 100.0  | 96.95     | 3.05     | 0.0  | 0.0       | 45.51        | false    | false    |
      | 21 April 2025    | Interest Refund        | 2.21   | 2.21      | 0.0      | 0.0  | 0.0       | 43.3         | false    | false    |
      | 21 April 2025    | Repayment              | 200.0  | 43.3      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "23 April 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "23 April 2025" with 156.7 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0   |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 221.85          | 120.61        | 3.05     | 0.0  | 0.0       | 123.66 | 123.66 | 23.66      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 198.19          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 174.53          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 150.87          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 127.21          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 103.55          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  79.89          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  56.23          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 April 2025 |  32.57          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 April 2025 |   8.91          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 April 2025 |   0.0           |  8.91         | 0.0      | 0.0  | 0.0       |  8.91  |  8.91  |  8.91      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0   |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 3.05     | 0.0  | 0.0       | 345.51 | 345.51 | 245.51     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 March 2025    | Repayment              | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | true     |
      | 20 April 2025    | Accrual                | 2.94   | 0.0       | 2.94     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Merchant Issued Refund | 100.0  | 96.95     | 3.05     | 0.0  | 0.0       | 45.51        | false    | false    |
      | 21 April 2025    | Interest Refund        | 2.21   | 2.21      | 0.0      | 0.0  | 0.0       | 43.3         | false    | false    |
      | 21 April 2025    | Repayment              | 200.0  | 43.3      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Credit Balance Refund  | 156.7  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "25 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "21 April 2025"
    Then Loan has 104.56 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 221.85          | 120.61        | 3.05     | 0.0  | 0.0       | 123.66 | 123.66| 23.66      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       |               | 198.19          | 180.36        | 2.35     | 0.0  | 0.0       | 182.71 | 78.15 | 78.15      | 0.0  | 104.56      |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 174.53          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 150.87          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 127.21          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 103.55          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  79.89          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  56.23          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 April 2025 |  32.57          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 April 2025 |   8.91          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 April 2025 |   0.0           |  8.91         | 0.0      | 0.0  | 0.0       |  8.91  |  8.91 |  8.91      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 499.16        | 5.4      | 0.0  | 0.0       | 504.56 | 400.0  | 300.0      | 0.0  | 104.56      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 March 2025    | Repayment              | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | true     |
      | 20 April 2025    | Accrual                | 2.94   | 0.0       | 2.94     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Merchant Issued Refund | 100.0  | 96.95     | 3.05     | 0.0  | 0.0       | 45.51        | true     | false    |
      | 21 April 2025    | Interest Refund        | 2.21   | 2.21      | 0.0      | 0.0  | 0.0       | 43.3         | true     | false    |
      | 21 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Repayment              | 200.0  | 142.46    | 3.05     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 23 April 2025    | Credit Balance Refund  | 156.7  | 102.21    | 0.0      | 0.0  | 0.0       | 102.21       | false    | true     |
# - CBR on active loan - with outstanding amount is forbidden -#
    Then Credit Balance Refund transaction on active loan "25 April 2025" with 100 EUR transaction amount will result an error

  @TestRailId:C3696
  Scenario: Verify accrual activity of overpaid loan in case of reversed repayment made before MIR and CBR for progressive multidisbursal loan - UC5
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "142.46" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 132.13          | 10.33         | 3.56     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 2  | 30   | 21 May 2025       |               | 121.54          | 10.59         | 3.3      | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 3  | 31   | 21 June 2025      |               | 110.69          | 10.85         | 3.04     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 4  | 30   | 21 July 2025      |               |  99.57          | 11.12         | 2.77     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 5  | 31   | 21 August 2025    |               |  88.17          | 11.4          | 2.49     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 6  | 31   | 21 September 2025 |               |  76.48          | 11.69         | 2.2      | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 7  | 30   | 21 October 2025   |               |  64.5           | 11.98         | 1.91     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 8  | 31   | 21 November 2025  |               |  52.22          | 12.28         | 1.61     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 9  | 30   | 21 December 2025  |               |  39.64          | 12.58         | 1.31     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 10 | 31   | 21 January 2026   |               |  26.74          | 12.9          | 0.99     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 11 | 31   | 21 February 2026  |               |  13.52          | 13.22         | 0.67     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 13.52         | 0.34     | 0.0  | 0.0       | 13.86  |  0.0  | 0.0        | 0.0  | 13.86       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 142.46        | 24.19    | 0.0  | 0.0       | 166.65 | 0.0    | 0.0        | 0.0  | 166.65      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 142.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 March 2025 | 128.57          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 114.68          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 100.79          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 |  86.9           | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 |  73.01          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 |  59.12          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  45.23          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  31.34          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  17.45          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   3.56          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  3.56         | 0.0      | 0.0  | 0.0       |  3.56  |  3.56 |  3.56      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 142.46        | 0.0      | 0.0  | 0.0       | 142.46 | 142.46 | 142.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 42.46     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 130.59          | 111.87        | 1.93     | 0.0  | 0.0       | 113.8  | 11.87 | 11.87      | 0.0  | 101.93      |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 118.72          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 106.85          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 |  94.98          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 |  83.11          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 |  71.24          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  59.37          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  47.5           | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  35.63          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |  23.76          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |  11.89          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 11.89         | 0.0      | 0.0  | 0.0       | 11.89  | 11.89 | 11.89      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 1.93     | 0.0  | 0.0       | 244.39 | 142.46 | 142.46     | 0.0  | 101.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Admin successfully disburse the loan on "21 April 2025" with "100" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 100 EUR transaction amount
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "21 April 2025" with 150 EUR transaction amount and system-generated Idempotency key
    Then Loan has 48.07 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 130.59          | 111.87        | 1.93     | 0.0  | 0.0       | 113.8  | 113.8 | 11.87      | 0.0  | 0.0         |
      |    |      | 21 April 2025     |               | 100.0           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 2  | 30   | 21 May 2025       | 21 April 2025 | 208.32          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 April 2025 | 186.05          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 April 2025 | 163.78          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 April 2025 | 141.51          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 April 2025 | 119.24          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 April 2025 |  96.97          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 April 2025 |  74.7           | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 April 2025 |  52.43          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 April 2025 |  30.16          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 April 2025 |  11.89          | 18.27         | 0.0      | 0.0  | 0.0       | 18.27  | 18.27 | 18.27      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 April 2025 |   0.0           | 11.89         | 0.0      | 0.0  | 0.0       | 11.89  | 11.89 | 11.89      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 344.39 | 242.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 21 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 21 April 2025    | Payout Refund          | 150.0  | 100.0     | 1.93     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 April 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 April 2025" with 48.07 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "28 April 2025" with 100 EUR transaction amount will result an error
    When Admin sets the business date to "06 May 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 April 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 130.59          | 111.87        | 1.93     | 0.0  | 0.0       | 113.8  | 113.8 | 11.87      | 0.0  | 0.0         |
      |    |      | 21 April 2025     |               | 100.0           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 2  | 30   | 21 May 2025       |               | 210.54          | 68.12         | 2.22     | 0.0  | 0.0       | 70.34  | 22.27 | 22.27      | 0.0  | 48.07       |
      | 3  | 31   | 21 June 2025      | 21 April 2025 | 188.27          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 April 2025 | 166.0           | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 April 2025 | 143.73          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 |               | 121.46          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 18.34 | 18.34      | 0.0  | 3.93        |
      | 7  | 30   | 21 October 2025   |               | 105.85          | 15.61         | 6.66     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 8  | 31   | 21 November 2025  |               |  84.74          | 21.11         | 1.16     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 9  | 30   | 21 December 2025  |               |  63.4           | 21.34         | 0.93     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 10 | 31   | 21 January 2026   |               |  41.82          | 21.58         | 0.69     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 11 | 31   | 21 February 2026  |               |  20.0           | 21.82         | 0.45     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 20.0          | 0.2      | 0.0  | 0.0       | 20.2   | 11.89 | 11.89      | 0.0  | 8.31        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 390.53        | 14.24    | 0.0  | 0.0       | 404.77 | 292.46 | 190.53     | 0.0  | 112.31      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 21 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Payout Refund          | 150.0  | 148.07    | 1.93     | 0.0  | 0.0       | 51.93        | false    | true     |
      | 28 April 2025    | Credit Balance Refund  | 48.07  | 48.07     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "21 March 2025"
    Then Loan has 285.65 outstanding amount
 # - CBR on active loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "01 May 2025" with 100 EUR transaction amount will result an error
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 330 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 134.07          | 108.39        | 5.5      | 0.0  | 0.0       | 113.89 | 113.89| 0.0        | 0.0  | 0.0         |
      |    |      | 21 April 2025     |               | 100.0           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 2  | 30   | 21 May 2025       | 01 May 2025   | 213.58          | 68.56         | 1.77     | 0.0  | 0.0       | 70.33  | 70.33 | 70.33      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 01 May 2025   | 191.32          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 01 May 2025   | 169.06          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 01 May 2025   | 146.8           | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 01 May 2025   | 124.54          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 01 May 2025   | 102.28          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 01 May 2025   |  80.02          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 01 May 2025   |  57.76          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 01 May 2025   |  35.5           | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 01 May 2025   |  13.24          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 01 May 2025   |   0.0           | 13.24         | 0.0      | 0.0  | 0.0       | 13.24  | 13.24 | 13.24      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 390.53        | 7.27     | 0.0  | 0.0       | 397.8  | 397.8  | 283.91     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | true     | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 242.46       | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 342.46       | false    | false    |
      | 21 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Payout Refund          | 150.0  | 144.5     | 5.5      | 0.0  | 0.0       | 197.96       | false    | true     |
      | 22 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Credit Balance Refund  | 48.07  | 48.07     | 0.0      | 0.0  | 0.0       | 246.03       | false    | true     |
      | 28 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Repayment              | 330.0  | 246.03    | 1.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 4.87   | 0.0       | 4.87     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "03 May 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "01 May 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 134.07          | 108.39        | 5.5      | 0.0  | 0.0       | 113.89 | 113.89| 0.0        | 0.0  | 0.0         |
      |    |      | 21 April 2025     |               | 100.0           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 2  | 30   | 21 May 2025       |               | 217.68          | 64.46         | 5.87     | 0.0  | 0.0       | 70.33  | 22.26 | 22.26      | 0.0  | 48.07       |
      | 3  | 31   | 21 June 2025      |               | 200.51          | 17.17         | 5.09     | 0.0  | 0.0       | 22.26  | 13.85 | 13.85      | 0.0  | 8.41        |
      | 4  | 30   | 21 July 2025      |               | 183.26          | 17.25         | 5.01     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 5  | 31   | 21 August 2025    |               | 165.58          | 17.68         | 4.58     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 6  | 31   | 21 September 2025 |               | 147.46          | 18.12         | 4.14     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 7  | 30   | 21 October 2025   |               | 128.89          | 18.57         | 3.69     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 8  | 31   | 21 November 2025  |               | 109.85          | 19.04         | 3.22     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 9  | 30   | 21 December 2025  |               |  90.34          | 19.51         | 2.75     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 10 | 31   | 21 January 2026   |               |  70.34          | 20.0          | 2.26     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 11 | 31   | 21 February 2026  |               |  49.84          | 20.5          | 1.76     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 49.84         | 1.25     | 0.0  | 0.0       | 51.09  | 0.0   | 0.0        | 0.0  | 51.09       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 390.53        | 45.12    | 0.0  | 0.0       | 435.65 | 150.0  | 36.11      | 0.0  | 285.65       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | true     | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 242.46       | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 342.46       | false    | false    |
      | 21 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Payout Refund          | 150.0  | 144.5     | 5.5      | 0.0  | 0.0       | 197.96       | false    | true     |
      | 22 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Credit Balance Refund  | 48.07  | 48.07     | 0.0      | 0.0  | 0.0       | 246.03       | false    | true     |
      | 28 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Repayment              | 330.0  | 246.03    | 1.77     | 0.0  | 0.0       | 0.0          | true    | false    |
      | 01 May 2025      | Accrual                | 4.87   | 0.0       | 4.87     | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "03 May 2025" with 100 EUR transaction amount will result an error

  @TestRailId:C3697
  Scenario: Verify accrual activity of overpaid loan in case of reversed MIR made before MIR and CBR for progressive loan - UC6
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 224.88          | 17.58         | 6.06     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 2  | 30   | 21 May 2025       |               | 206.86          | 18.02         | 5.62     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 3  | 31   | 21 June 2025      |               | 188.39          | 18.47         | 5.17     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 4  | 30   | 21 July 2025      |               | 169.46          | 18.93         | 4.71     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 5  | 31   | 21 August 2025    |               | 150.06          | 19.4          | 4.24     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 6  | 31   | 21 September 2025 |               | 130.17          | 19.89         | 3.75     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 7  | 30   | 21 October 2025   |               | 109.78          | 20.39         | 3.25     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 8  | 31   | 21 November 2025  |               |  88.88          | 20.9          | 2.74     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 9  | 30   | 21 December 2025  |               |  67.46          | 21.42         | 2.22     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 10 | 31   | 21 January 2026   |               |  45.51          | 21.95         | 1.69     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 11 | 31   | 21 February 2026  |               |  23.01          | 22.5          | 1.14     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 23.01         | 0.58     | 0.0  | 0.0       | 23.59  |  0.0  | 0.0        | 0.0  | 23.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 41.17    | 0.0  | 0.0       | 283.63 | 0.0    | 0.0        | 0.0  | 283.63     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 222.26          | 120.2         | 1.93     | 0.0  | 0.0       | 122.13 | 20.2  |  20.2      | 0.0  | 101.93      |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 202.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 181.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 161.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 141.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 121.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 | 101.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  80.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  60.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |  40.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |  20.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  20.26        | 0.0      | 0.0  | 0.0       |  20.26 | 20.26 |  20.26     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 242.46 | 242.46     | 0.0  | 101.93       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Credit Balance Refund transaction on active loan "21 April 2025" with 100 EUR transaction amount will result an error
    When Admin sets the business date to "29 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "29 April 2025" with 100 EUR transaction amount
    Then Loan has 2.6 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 222.26          | 120.2         | 2.6      | 0.0  | 0.0       | 122.8  |120.2  |  20.2      | 100.0| 2.6         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 202.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 181.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 161.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 141.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 121.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 | 101.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  80.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  60.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |  40.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |  20.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  20.26        | 0.0      | 0.0  | 0.0       |  20.26 | 20.26 |  20.26     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 342.46        | 2.6      | 0.0  | 0.0       | 345.06 | 342.46 | 242.46     | 100.0 | 2.6         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on active loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "29 April 2025" with 100 EUR transaction amount will result an error
    When Admin sets the business date to "06 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "06 May 2025" with 2.6 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late   | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |        |             |
      | 1  | 31   | 21 April 2025     | 06 May 2025   | 222.26          | 120.2         | 2.6      | 0.0  | 0.0       | 122.8  |122.8  |  20.2      | 102.6  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 202.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 181.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 161.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 141.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 121.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 | 101.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  80.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  60.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |  40.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |  20.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  20.26        | 0.0      | 0.0  | 0.0       |  20.26 | 20.26 |  20.26     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 342.46        | 2.6      | 0.0  | 0.0       | 345.06 | 345.06 | 242.46     | 102.6  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Repayment              | 2.6    | 0.0       | 2.6      | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "06 May 2025" with 100 EUR transaction amount will result an error

  @TestRailId:C3698
  Scenario: Verify that interest activities are added in case of reversed repayment made before MIR and CBR for loan with accrual activity - UC7
    When Admin sets the business date to "07 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY | 07 April 2025     | 72.3           | 29.99                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "07 April 2025" with "72.3" amount and expected disbursement date on "07 April 2025"
    And Admin successfully disburse the loan on "07 April 2025" with "72.3" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 07 May 2025       |               |  67.06          | 5.24          | 1.81     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 2  | 31   | 07 June 2025      |               |  61.69          | 5.37          | 1.68     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 3  | 30   | 07 July 2025      |               |  56.18          | 5.51          | 1.54     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 4  | 31   | 07 August 2025    |               |  50.53          | 5.65          | 1.4      | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 5  | 31   | 07 September 2025 |               |  44.74          | 5.79          | 1.26     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 6  | 30   | 07 October 2025   |               |  38.81          | 5.93          | 1.12     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 7  | 31   | 07 November 2025  |               |  32.73          | 6.08          | 0.97     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 8  | 30   | 07 December 2025  |               |  26.5           | 6.23          | 0.82     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 9  | 31   | 07 January 2026   |               |  20.11          | 6.39          | 0.66     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 10 | 31   | 07 February 2026  |               |  13.56          | 6.55          | 0.5      | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 11 | 28   | 07 March 2026     |               |  6.85           | 6.71          | 0.34     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 12 | 31   | 07 April 2026     |               |  0.0            | 6.85          | 0.17     | 0.0  | 0.0       | 7.02  | 0.0  | 0.0        | 0.0  | 7.02        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 72.3          | 12.27    | 0.0  | 0.0       | 84.57  | 0.0    | 0.0        | 0.0  | 84.57       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
    When Admin sets the business date to "08 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "08 April 2025" with 72.35 EUR transaction amount
    When Admin sets the business date to "11 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "11 April 2025" with 72.3 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 72.35 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 30   | 07 May 2025       | 08 April 2025 |  65.31          | 6.99          | 0.06     | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 2  | 31   | 07 June 2025      | 08 April 2025 |  58.26          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 3  | 30   | 07 July 2025      | 08 April 2025 |  51.21          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 4  | 31   | 07 August 2025    | 08 April 2025 |  44.16          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 5  | 31   | 07 September 2025 | 08 April 2025 |  37.11          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 6  | 30   | 07 October 2025   | 08 April 2025 |  30.06          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 7  | 31   | 07 November 2025  | 08 April 2025 |  23.01          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 8  | 30   | 07 December 2025  | 08 April 2025 |  15.96          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 9  | 31   | 07 January 2026   | 08 April 2025 |   8.91          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 10 | 31   | 07 February 2026  | 08 April 2025 |   1.86          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 11 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 1.86          | 0.0      | 0.0  | 0.0       | 1.86   |  1.86 | 1.86       | 0.0  | 0.0         |
      | 12 | 31   | 07 April 2026     | 08 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    |  0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 72.3          | 0.06     | 0.0  | 0.0       | 72.36  | 72.36  | 72.36      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 08 April 2025    | Repayment              | 72.35  | 72.29     | 0.06     | 0.0  | 0.0       | 0.01         | false    | false    |
      | 08 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 0.01      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Interest Refund        | 0.06   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual Activity       | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "15 April 2025" with 72.35 EUR transaction amount
    When Admin sets the business date to "18 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "08 April 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 30   | 07 May 2025       |               |  65.49          | 79.16         | 1.57     | 0.0  | 0.0       | 80.73  |  7.05 | 7.05       | 0.0  | 73.68       |
      | 2  | 31   | 07 June 2025      | 11 April 2025 |  58.44          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 3  | 30   | 07 July 2025      | 11 April 2025 |  51.39          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 4  | 31   | 07 August 2025    | 11 April 2025 |  44.34          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 5  | 31   | 07 September 2025 | 11 April 2025 |  37.29          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 6  | 30   | 07 October 2025   | 11 April 2025 |  30.24          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 7  | 31   | 07 November 2025  | 11 April 2025 |  23.19          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 8  | 30   | 07 December 2025  | 11 April 2025 |  16.14          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 9  | 31   | 07 January 2026   | 11 April 2025 |   9.09          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 10 | 31   | 07 February 2026  | 11 April 2025 |   2.04          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 11 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 2.04          | 0.0      | 0.0  | 0.0       | 2.04   |  2.04 | 2.04       | 0.0  | 0.0         |
      | 12 | 31   | 07 April 2026     | 11 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    |  0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 144.65        | 1.57     | 0.0  | 0.0       | 146.22 | 72.54  | 72.54      | 0.0  | 73.68       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 08 April 2025    | Repayment              | 72.35  | 72.29     | 0.06     | 0.0  | 0.0       | 0.01         | true     | false    |
      | 08 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 72.06     | 0.24     | 0.0  | 0.0       | 0.24         | false    | true     |
      | 11 April 2025    | Interest Refund        | 0.24   | 0.24      | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 April 2025    | Credit Balance Refund  | 72.35  | 72.35     | 0.0      | 0.0  | 0.0       | 72.35        | false    | true     |
    When Admin sets the business date to "07 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "07 May 2025" with 73.68 EUR transaction amount
    When Admin sets the business date to "08 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 30   | 07 May 2025       | 07 May 2025   |  65.49          | 79.16         | 1.57     | 0.0  | 0.0       | 80.73  | 80.73 | 7.05       | 0.0  | 0.0         |
      | 2  | 31   | 07 June 2025      | 11 April 2025 |  58.44          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 3  | 30   | 07 July 2025      | 11 April 2025 |  51.39          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 4  | 31   | 07 August 2025    | 11 April 2025 |  44.34          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 5  | 31   | 07 September 2025 | 11 April 2025 |  37.29          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 6  | 30   | 07 October 2025   | 11 April 2025 |  30.24          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 7  | 31   | 07 November 2025  | 11 April 2025 |  23.19          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 8  | 30   | 07 December 2025  | 11 April 2025 |  16.14          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 9  | 31   | 07 January 2026   | 11 April 2025 |   9.09          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 10 | 31   | 07 February 2026  | 11 April 2025 |   2.04          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 11 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 2.04          | 0.0      | 0.0  | 0.0       | 2.04   |  2.04 | 2.04       | 0.0  | 0.0         |
      | 12 | 31   | 07 April 2026     | 11 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    |  0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 144.65        | 1.57     | 0.0  | 0.0       | 146.22 | 146.22  | 72.54     | 0.0  | 0.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 08 April 2025    | Repayment              | 72.35  | 72.29     | 0.06     | 0.0  | 0.0       | 0.01         | true     | false    |
      | 08 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 72.06     | 0.24     | 0.0  | 0.0       | 0.24         | false    | true     |
      | 11 April 2025    | Interest Refund        | 0.24   | 0.24      | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 11 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Credit Balance Refund  | 72.35  | 72.35     | 0.0      | 0.0  | 0.0       | 72.35        | false    | true     |
      | 16 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Repayment              | 73.68  | 72.35     | 1.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual Activity       | 1.57   | 0.0       | 1.57     | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "07 May 2025" with 72.35 EUR transaction amount will result an error

  @TestRailId:C3699
  Scenario: Verify that interest activities are added in case of reversed repayment made before MIR and CBR for progressive loan with auto downpayment and accrual activity - UC8
    When Admin sets the business date to "07 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 07 April 2025     | 72.3           | 29.99                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "07 April 2025" with "72.3" amount and expected disbursement date on "07 April 2025"
    And Admin successfully disburse the loan on "07 April 2025" with "72.3" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  |  0   | 07 April 2025     | 07 April 2025 |  54.22          | 18.08         | 0.0      | 0.0  | 0.0       | 18.08 | 18.08| 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 07 May 2025       |               |  50.29          | 3.93          | 1.36     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 3  | 31   | 07 June 2025      |               |  46.26          | 4.03          | 1.26     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 4  | 30   | 07 July 2025      |               |  42.13          | 4.13          | 1.16     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 5  | 31   | 07 August 2025    |               |  37.89          | 4.24          | 1.05     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 6  | 31   | 07 September 2025 |               |  33.55          | 4.34          | 0.95     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 7  | 30   | 07 October 2025   |               |  29.1           | 4.45          | 0.84     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 8  | 31   | 07 November 2025  |               |  24.54          | 4.56          | 0.73     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 9  | 30   | 07 December 2025  |               |  19.86          | 4.68          | 0.61     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 10 | 31   | 07 January 2026   |               |  15.07          | 4.79          | 0.5      | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 11 | 31   | 07 February 2026  |               |  10.16          | 4.91          | 0.38     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 12 | 28   | 07 March 2026     |               |   5.12          | 5.04          | 0.25     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 13 | 31   | 07 April 2026     |               |   0.0           | 5.12          | 0.13     | 0.0  | 0.0       | 5.25  | 0.0  | 0.0        | 0.0  | 5.25        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 72.3          | 9.22     | 0.0  | 0.0       | 81.52  | 18.08  | 0.0        | 0.0  | 63.44       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 07 April 2025    | Down Payment           | 18.08  | 18.08     | 0.0      | 0.0  | 0.0       | 54.22        | false    | false    |
    When Admin sets the business date to "08 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "08 April 2025" with 54.27 EUR transaction amount
    When Admin sets the business date to "11 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "11 April 2025" with 72.3 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 72.35 overpaid amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  |  0   | 07 April 2025     | 07 April 2025 |  54.22          | 18.08         | 0.0      | 0.0  | 0.0       | 18.08 | 18.08| 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 07 May 2025       | 08 April 2025 |  48.98          | 5.24          | 0.05     | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 3  | 31   | 07 June 2025      | 08 April 2025 |  43.69          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 4  | 30   | 07 July 2025      | 08 April 2025 |  38.4           | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 5  | 31   | 07 August 2025    | 08 April 2025 |  33.11          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 6  | 31   | 07 September 2025 | 08 April 2025 |  27.82          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 7  | 30   | 07 October 2025   | 08 April 2025 |  22.53          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 8  | 31   | 07 November 2025  | 08 April 2025 |  17.24          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 9  | 30   | 07 December 2025  | 08 April 2025 |  11.95          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 10 | 31   | 07 January 2026   | 08 April 2025 |   6.66          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 11 | 31   | 07 February 2026  | 08 April 2025 |   1.37          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 12 | 28   | 07 March 2026     | 08 April 2025 |   0.0           | 1.37          | 0.0      | 0.0  | 0.0       | 1.37  | 1.37 | 1.37       | 0.0  | 0.0         |
      | 13 | 31   | 07 April 2026     | 08 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 72.3          | 0.05     | 0.0  | 0.0       | 72.35  | 72.35  | 54.27      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 07 April 2025    | Down Payment           | 18.08  | 18.08     | 0.0      | 0.0  | 0.0       | 54.22        | false    | false    |
      | 08 April 2025    | Repayment              | 54.27  | 54.22     | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual Activity       | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Interest Refund        | 0.05   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "15 April 2025" with 72.35 EUR transaction amount
    When Admin sets the business date to "18 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "08 April 2025"
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  |  0   | 07 April 2025     | 07 April 2025 |  54.22          | 18.08         | 0.0      | 0.0  | 0.0       | 18.08 | 18.08 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 07 May 2025       |               |  49.11          | 77.46         | 1.18     | 0.0  | 0.0       | 78.64 | 23.37 | 23.37      | 0.0  | 55.27       |
      | 3  | 31   | 07 June 2025      | 11 April 2025 |  43.82          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 4  | 30   | 07 July 2025      | 11 April 2025 |  38.53          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 5  | 31   | 07 August 2025    | 11 April 2025 |  33.24          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 6  | 31   | 07 September 2025 | 11 April 2025 |  27.95          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 7  | 30   | 07 October 2025   | 11 April 2025 |  22.66          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 8  | 31   | 07 November 2025  | 11 April 2025 |  17.37          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 9  | 30   | 07 December 2025  | 11 April 2025 |  12.08          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 10 | 31   | 07 January 2026   | 11 April 2025 |   6.79          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 11 | 31   | 07 February 2026  | 11 April 2025 |   1.5           | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 12 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 1.5           | 0.0      | 0.0  | 0.0       | 1.5   | 1.5   | 1.5        | 0.0  | 0.0         |
      | 13 | 31   | 07 April 2026     | 11 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 144.65        | 1.18     | 0.0  | 0.0       | 145.83 | 90.56  | 72.48      | 0.0  | 55.27       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 07 April 2025    | Down Payment           | 18.08  | 18.08     | 0.0      | 0.0  | 0.0       | 54.22        | false    | false    |
      | 08 April 2025    | Repayment              | 54.27  | 54.22     | 0.05     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 08 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 54.22     | 0.18     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 11 April 2025    | Interest Refund        | 0.18   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 April 2025    | Credit Balance Refund  | 72.35  | 54.27     | 0.0      | 0.0  | 0.0       | 54.27        | false    | true     |
    When Admin sets the business date to "07 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "07 May 2025" with 55.27 EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  |  0   | 07 April 2025     | 07 April 2025 |  54.22          | 18.08         | 0.0      | 0.0  | 0.0       | 18.08 | 18.08 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 07 May 2025       | 07 May 2025   |  49.11          | 77.46         | 1.18     | 0.0  | 0.0       | 78.64 | 78.64 | 23.37      | 0.0  | 0.0        |
      | 3  | 31   | 07 June 2025      | 11 April 2025 |  43.82          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 4  | 30   | 07 July 2025      | 11 April 2025 |  38.53          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 5  | 31   | 07 August 2025    | 11 April 2025 |  33.24          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 6  | 31   | 07 September 2025 | 11 April 2025 |  27.95          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 7  | 30   | 07 October 2025   | 11 April 2025 |  22.66          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 8  | 31   | 07 November 2025  | 11 April 2025 |  17.37          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 9  | 30   | 07 December 2025  | 11 April 2025 |  12.08          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 10 | 31   | 07 January 2026   | 11 April 2025 |   6.79          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 11 | 31   | 07 February 2026  | 11 April 2025 |   1.5           | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 12 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 1.5           | 0.0      | 0.0  | 0.0       | 1.5   | 1.5   | 1.5        | 0.0  | 0.0         |
      | 13 | 31   | 07 April 2026     | 11 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 144.65        | 1.18     | 0.0  | 0.0       | 145.83 | 145.83 | 72.48      | 0.0  | 0.0        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 07 April 2025    | Down Payment           | 18.08  | 18.08     | 0.0      | 0.0  | 0.0       | 54.22        | false    | false    |
      | 08 April 2025    | Repayment              | 54.27  | 54.22     | 0.05     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 08 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 54.22     | 0.18     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 11 April 2025    | Interest Refund        | 0.18   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 11 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Credit Balance Refund  | 72.35  | 54.27     | 0.0      | 0.0  | 0.0       | 54.27        | false    | true     |
      | 16 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2025      | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2025      | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2025      | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2025      | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Repayment              | 55.27  | 54.27     | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual Activity       | 1.18   | 0.0       | 1.18     | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "07 May 2025" with 72.35 EUR transaction amount will result an error

  @TestRailId:C3736
  Scenario: Verify that interest is calculated after last unpaid period in case of reversed repayment made before MIR and CBR for progressive loan with downpayment
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     |               | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  |  0.0  | 0.0        | 0.0  | 60.62       |
      | 2  | 31   | 21 April 2025     |               | 168.65          | 13.19         | 4.54     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 3  | 30   | 21 May 2025       |               | 155.13          | 13.52         | 4.21     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 4  | 31   | 21 June 2025      |               | 141.28          | 13.85         | 3.88     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 5  | 30   | 21 July 2025      |               | 127.08          | 14.2          | 3.53     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 6  | 31   | 21 August 2025    |               | 112.53          | 14.55         | 3.18     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 7  | 31   | 21 September 2025 |               |  97.61          | 14.92         | 2.81     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 8  | 30   | 21 October 2025   |               |  82.32          | 15.29         | 2.44     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 9  | 31   | 21 November 2025  |               |  66.65          | 15.67         | 2.06     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 10 | 30   | 21 December 2025  |               |  50.59          | 16.06         | 1.67     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 11 | 31   | 21 January 2026   |               |  34.12          | 16.47         | 1.26     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 12 | 31   | 21 February 2026  |               |  17.24          | 16.88         | 0.85     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 13 | 28   | 21 March 2026     |               |   0.0           | 17.24         | 0.43     | 0.0  | 0.0       | 17.67  |  0.0  | 0.0        | 0.0  | 17.67       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 30.86    | 0.0  | 0.0       | 273.32 | 0.0    | 0.0        | 0.0  | 273.32      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 1.93     | 0.0  | 0.0       | 117.08 | 15.15 | 15.15      | 0.0  | 101.93      |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 | 90.94           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 | 75.79           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 | 60.64           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 | 45.49           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 | 30.34           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 | 15.19           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 | 0.0             | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 242.46 | 181.84     | 0.0  | 101.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 1.93     | 0.0  | 0.0       | 117.08 | 15.15 | 15.15      | 0.0  | 101.93      |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  90.94          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  75.79          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  60.64          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  45.49          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |  30.34          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |  15.19          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 242.46 | 181.84     | 0.0  | 101.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "22 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 2.01     | 0.0  | 0.0       | 117.16 | 15.15 | 15.15      | 0.0  | 102.01      |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  90.94          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  75.79          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  60.64          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  45.49          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |  30.34          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |  15.19          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 2.01     | 0.0  | 0.0       | 344.47 | 242.46 | 181.84     | 0.0  | 102.01      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "23 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 2.1      | 0.0  | 0.0       | 117.25 | 15.15 | 15.15      | 0.0  | 102.1       |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  90.94          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  75.79          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  60.64          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  45.49          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |  30.34          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |  15.19          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 2.1      | 0.0  | 0.0       | 344.56 | 242.46 | 181.84     | 0.0  | 102.1       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "23 April 2025" with 102.1 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Repayment              | 102.1  | 100.0     | 2.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3773
  Scenario: Verify that interest is calculated after last unpaid period in case of MIR partially covering later periods
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 21 March 2025     | 186.38         | 35.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "186.38" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "186.38" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 186.38          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 126.08          | 60.3          | 5.59     | 0.0  | 0.0       | 65.89  |  0.0  | 0.0        | 0.0  | 65.89       |
      | 2  | 30   | 21 May 2025       |               | 63.97           | 62.11         | 3.78     | 0.0  | 0.0       | 65.89  |  0.0  | 0.0        | 0.0  | 65.89       |
      | 3  | 31   | 21 June 2025      |               | 0.0             | 63.97         | 1.92     | 0.0  | 0.0       | 65.89  |  0.0  | 0.0        | 0.0  | 65.89       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 186.38        | 11.29    | 0.0  | 0.0       | 197.67 | 0.0    | 0.0        | 0.0  | 197.67      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 186.38 | 0.0       | 0.0      | 0.0  | 0.0       | 186.38       | false    | false    |
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "17 April 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "17 April 2025" with 87.33 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 186.38          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 125.74          | 60.64         | 5.25     | 0.0  | 0.0       | 65.89  |  0.0  | 0.0        | 0.0  | 65.89       |
      | 2  | 30   | 21 May 2025       |               | 65.89           | 59.85         | 1.15     | 0.0  | 0.0       | 61.0   |  21.44| 21.44      | 0.0  | 39.56       |
      | 3  | 31   | 21 June 2025      | 17 April 2025 | 0.0             | 65.89         | 0.0      | 0.0  | 0.0       | 65.89  |  65.89| 65.89      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 186.38        | 6.4      | 0.0  | 0.0       | 192.78 | 87.33  | 87.33      | 0.0  | 105.45      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 186.38 | 0.0       | 0.0      | 0.0  | 0.0       | 186.38       | false    | false    |
      | 17 April 2025    | Merchant Issued Refund | 87.33  | 87.33     | 0.0      | 0.0  | 0.0       | 99.05        | false    | false    |
    When Admin sets the business date to "21 May 2025"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 186.38 | 0.0       | 0.0      | 0.0  | 0.0       | 186.38       | false    | false    |
      | 22 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual                | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Merchant Issued Refund | 87.33  | 87.33     | 0.0      | 0.0  | 0.0       | 99.05        | false    | false    |
      | 17 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 8.22 total unpaid payable due interest
    When Admin sets the business date to "26 May 2025"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 186.38 | 0.0       | 0.0      | 0.0  | 0.0       | 186.38       | false    | false    |
      | 22 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual                | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Merchant Issued Refund | 87.33  | 87.33     | 0.0      | 0.0  | 0.0       | 99.05        | false    | false    |
      | 17 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 8.7 total unpaid payable due interest
    And Customer makes "AUTOPAY" repayment on "26 May 2025" with 107.75 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount

  @TestRailId:C3802
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a goodwill credit transaction  - UC1
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "19 May 2023" with 270.85 EUR transaction amount and system-generated Idempotency key
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement       | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment       | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Goodwill Credit    | 270.85 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual            | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3803
  Scenario: Correct Accrual Activity event publishing for backdated loans when the overpaid loan re-opens after reversing a goodwill credit transaction - UC2
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "19 May 2023" with 359.79 EUR transaction amount and system-generated Idempotency key
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement       | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment       | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Goodwill Credit    | 359.79 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual            | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3805
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a payout refund transaction - UC3
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_360_30_INTEREST_RECALC_AUTO_DOWNPAYMENT_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    And Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "19 May 2023" with 270.85 EUR transaction amount
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement     | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment     | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Payout Refund    | 270.85 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual          | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3806
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a merchant issue refund transaction  - UC4
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "19 May 2023" with 359.79 EUR transaction amount
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement           | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment           | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Merchant Issued Refund | 359.79 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 19 May 2023         | Interest Refund        | 1.01   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual                | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3807
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a interest payment waiver transaction - UC5
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "19 May 2023" with 270.85 EUR transaction amount
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement            | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment            | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Interest Payment Waiver | 270.85 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual                 | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3808
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a repayment transaction  - UC6
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Customer makes "AUTOPAY" repayment on "19 May 2023" with 359.79 EUR transaction amount
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement       | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment       | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Repayment          | 359.79 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual            | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan's all installments have obligations met


