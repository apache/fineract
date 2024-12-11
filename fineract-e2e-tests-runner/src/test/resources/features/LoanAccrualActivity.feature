@LoanAccrualActivityFeature
Feature: LoanAccrualActivity

  @TestRailId:C3168
  Scenario: Verify accrual activity - UC1: No payment, advanced payment strategy
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 04 January 2024  | Repayment          | 1004.1 | 1000.0    | 4.1      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 04 January 2024  | Accrual            | 4.1    | 0.0       | 4.1      | 0.0  | 0.0       | 0.0          | false    | false    |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | 1000.0        | 4.19     | 0.0  | 0.0       | 1004.19 | 0.0  | 0.0        | 0.0  | 1004.19      |
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
      | LoanProduct                                                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.5           | 249.5         | 1.5      | 0.0  | 0.0       | 251.0  | 150.0 | 150.0      | 0.0  | 101.0       |
      | 2  | 5    | 11 January 2024 |           | 500.77          | 249.73        | 1.27     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.59          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.59        | 0.41     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.0      | 0.0  | 0.0       | 1004.0  | 150.0 | 150.0      | 0.0  | 854.0       |
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
      | LoanProduct                                                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 5    | 06 January 2024 |           | 750.5           | 249.5         | 1.5      | 0.0  | 0.0       | 251.0  | 150.0 | 150.0      | 0.0  | 101.0       |
      | 2  | 5    | 11 January 2024 |           | 500.77          | 249.73        | 1.27     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 3  | 5    | 16 January 2024 |           | 250.59          | 250.18        | 0.82     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
      | 4  | 5    | 21 January 2024 |           | 0.0             | 250.59        | 0.41     | 0.0  | 0.0       | 251.0  | 0.0   | 0.0        | 0.0  | 251.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 4.0      | 0.0  | 0.0       | 1004.0  | 150.0 | 150.0      | 0.0  | 854.0       |
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
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 April 2024    |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 22 May 2024      |           | 333.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 2  | 31   | 22 June 2024     |           | 266.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 3  | 30   | 22 July 2024     |           | 199.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 4  | 31   | 22 August 2024   |           | 132.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 5  | 31   | 22 September 2024|           | 65.0            | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 6  | 30   | 22 October 2024  |           | 0.0             | 65.0          | 0.0      | 0.0  | 0.0       | 65.0  | 0.0  | 0.0        | 0.0  | 65.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0  | 0.0  | 0.0        | 0.0  | 400.0     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 April 2024    | Disbursement      | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
    And Customer makes "AUTOPAY" repayment on "22 April 2024" with 600 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Admin adds "LOAN_NSF_FEE" due date charge with "22 April 2024" due date and 30 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 22 April 2024   | Flat             | 30.0 | 30.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 April 2024    |               | 400.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 22 May 2024      | 22 April 2024 | 333.0           | 67.0          | 0.0      | 0.0  | 30.0      | 97.0  | 97.0 | 97.0       | 0.0  | 0.0         |
      | 2  | 31   | 22 June 2024     | 22 April 2024 | 266.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 67.0 | 67.0       | 0.0  | 0.0         |
      | 3  | 30   | 22 July 2024     | 22 April 2024 | 199.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 67.0 | 67.0       | 0.0  | 0.0         |
      | 4  | 31   | 22 August 2024   | 22 April 2024 | 132.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 67.0 | 67.0       | 0.0  | 0.0         |
      | 5  | 31   | 22 September 2024| 22 April 2024 | 65.0            | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 67.0 | 67.0       | 0.0  | 0.0         |
      | 6  | 30   | 22 October 2024  | 22 April 2024 | 0.0             | 65.0          | 0.0      | 0.0  | 0.0       | 65.0  | 65.0 | 65.0       | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 400.0         | 0.0      | 0.0  | 30.0      | 430.0  | 430.0| 430.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 April 2024    | Disbursement      | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
      | 22 April 2024    | Accrual           | 30.0   | 0.0       | 0.0      | 0.0  | 30.0      | 0.0          | false    | false    |
      | 22 April 2024    | Repayment         | 600.0  | 400.0     | 0.0      | 0.0  | 30.0      | 0.0          | false    | true     |
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
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 April 2024    |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 22 May 2024      |           | 333.0           | 67.0          | 0.0      | 0.0  | 30.0      | 97.0  | 30.0 | 15.0       | 15.0 | 67.0        |
      | 2  | 31   | 22 June 2024     |           | 266.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 3  | 30   | 22 July 2024     |           | 199.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 4  | 31   | 22 August 2024   |           | 132.0           | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 5  | 31   | 22 September 2024|           | 65.0            | 67.0          | 0.0      | 0.0  | 0.0       | 67.0  | 0.0  | 0.0        | 0.0  | 67.0        |
      | 6  | 30   | 22 October 2024  |           | 0.0             | 65.0          | 0.0      | 0.0  | 0.0       | 65.0  | 0.0  | 0.0        | 0.0  | 65.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 400.0         | 0.0      | 0.0  | 30.0      | 430.0  | 30.0 | 15.0       | 15.0 | 400.0     |
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
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75  | 0.0  | 0.0        | 0.0   | 1054.75    |
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
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75  | 0.0  | 0.0        | 0.0   | 1054.75    |
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
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75  | 0.0  | 0.0        | 0.0   | 1054.75    |
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
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 746.1           | 253.9         | 9.79     | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 507.44          | 238.66        | 25.03    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 254.74          | 252.7         | 10.99    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 254.74        | 5.52     | 0.0  | 0.0       | 260.26 | 0.0    | 0.0        | 0.0  | 260.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 1000.0        | 51.33    | 0.0  | 0.0       | 1051.33  | 263.69  | 263.69  | 0.0  | 787.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.9     | 9.79     | 0.0  | 0.0       | 746.1        | false    | false    |
    When Admin sets the business date to "21 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.9     | 9.79     | 0.0  | 0.0       | 746.1        | false    | false    |
      | 15 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer undo "1"th repayment on "15 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75  | 0.0  | 0.0        | 0.0   | 1054.75    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.9     | 9.79     | 0.0  | 0.0       | 746.1        | true     | false    |
      | 15 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
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
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75  | 0.0  | 0.0        | 0.0   | 1054.75    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.9     | 9.79     | 0.0  | 0.0       | 746.1        | true     | false    |
      | 15 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 1.58   | 0.0       | 1.58     | 0.0  | 0.0       | 0.0          | false    | false    |
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
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 1000.0        | 54.75    | 0.0  | 0.0       | 1054.75  | 0.0  | 0.0        | 0.0   | 1054.75    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 13 January 2024  | Accrual          | 8.39   | 0.0       | 8.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.9     | 9.79     | 0.0  | 0.0       | 746.1        | true     | false    |
      | 15 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 1.58   | 0.0       | 1.58     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |