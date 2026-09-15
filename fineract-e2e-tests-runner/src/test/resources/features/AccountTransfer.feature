@AccountTransfer
Feature: AccountTransfer

  @TestRailId:C80937
  Scenario: Transfer from savings to linked loan then undo it
    When Admin sets the business date to "13 May 2026"
    And Admin creates a client with random data
    And Admin creates a EUR savings product
    And Client creates a new EUR savings account with "01 May 2026" submitted on date
    And Approve EUR savings account on "01 May 2026" date
    And Activate EUR savings account on "01 May 2026" date
    And Client successfully deposits 1000 EUR to the savings account on "01 May 2026" date
    Then Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  |
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 May 2026       | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 May 2026" with "1000" amount and expected disbursement date on "01 May 2026"
    When Admin successfully disburse the loan on "01 May 2026" with "1000" EUR transaction amount
    When Initiate account transfer from savings to loan on "2 May 2026" for 10
    Then Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  |
      | 02 May 2026      | Withdrawal       | 10.0   | 990.0   |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 May 2026      | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 May 2026      | Repayment        | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 990.0        | false    | false    |
    When Undo the last account transfer
    Then Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance | Reverted |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  | false    |
      | 02 May 2026      | Withdrawal       | 10.0   | 0.0     | true     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 May 2026      | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 May 2026      | Repayment        | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 990.0        | true     | false    |
    When Undo the last account transfer it fails with error: it is already reverted

  @TestRailId:C80938
  Scenario: Transfer from loan to savings then undo it
    When Admin sets the business date to "13 May 2026"
    And Admin creates a client with random data
    And Admin creates a EUR savings product
    And Client creates a new EUR savings account with "01 May 2026" submitted on date
    And Approve EUR savings account on "01 May 2026" date
    And Activate EUR savings account on "01 May 2026" date
    And Client successfully deposits 1000 EUR to the savings account on "01 May 2026" date
    Then Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  |
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 May 2026       | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 May 2026" with "1000" amount and expected disbursement date on "01 May 2026"
    When Admin successfully disburse the loan on "01 May 2026" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "02 May 2026" with 1100 EUR transaction amount
    When Initiate account transfer from loan to savings on "2 May 2026" for 10
    Then Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  |
      | 02 May 2026      | Deposit          | 10.0   | 1010.0  |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 May 2026      | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 May 2026      | Repayment        | 1100.0 | 1000.0    | 35.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2026      | Transfer Refund  | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 May 2026      | Accrual          | 35.28  | 0.0       | 35.28    | 0.0  | 0.0       | 0.0          | false    | false    |
    When Undo the last account transfer
    Then Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance | Reverted |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  | false    |
      | 02 May 2026      | Deposit          | 10.0   | 0.0     | true     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 May 2026      | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 May 2026      | Repayment        | 1100.0 | 1000.0    | 35.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2026      | Transfer Refund  | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 13 May 2026      | Accrual          | 35.28  | 0.0       | 35.28    | 0.0  | 0.0       | 0.0          | false    | false    |
    When Undo the last account transfer it fails with error: it is already reverted

  @TestRailId:C80967
  Scenario: Transfer between savings accounts
    When Admin sets the business date to "13 May 2026"
    And Admin creates a client with random data
    And Admin creates a EUR savings product
    And Client creates a "PRIMARY" new EUR savings account with "01 May 2026" submitted on date
    And Approve "PRIMARY" EUR savings account on "01 May 2026" date
    And Activate "PRIMARY" EUR savings account on "01 May 2026" date
    And Client successfully deposits 1000 EUR to the "PRIMARY" savings account on "01 May 2026" date
    Then "PRIMARY" Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  |
    And Client creates a "SECONDARY" new EUR savings account with "01 May 2026" submitted on date
    And Approve "SECONDARY" EUR savings account on "01 May 2026" date
    And Activate "SECONDARY" EUR savings account on "01 May 2026" date
    And Client successfully deposits 1000 EUR to the "SECONDARY" savings account on "01 May 2026" date
    Then "SECONDARY" Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  |
    When Initiate account transfer from savings "PRIMARY" to savings "SECONDARY" on "2 May 2026" for 10
    Then "PRIMARY" Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  |
      | 02 May 2026      | Withdrawal       | 10.0   | 990.0   |
    Then "SECONDARY" Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  |
      | 02 May 2026      | Deposit          | 10.0   | 1010.0  |
    When Undo the last account transfer
    Then "PRIMARY" Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance | Reverted |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  | false    |
      | 02 May 2026      | Withdrawal       | 10.0   | 0.0     | true     |
    Then "SECONDARY" Savings Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Balance | Reverted |
      | 01 May 2026      | Deposit          | 1000.0 | 1000.0  | false    |
      | 02 May 2026      | Deposit          | 10.0   | 0.0     | true     |
    When Undo the last account transfer it fails with error: it is already reverted
