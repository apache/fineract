@BuyDownFeeFeature
Feature:Feature: Buy Down Fees

  @TestRailId:C3770
  Scenario: Simple loan with Buy Down fees and full payment - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 1 January 2024    | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "1 January 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 29   | 01 March 2024    |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 33.53         | 0.2     | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 33.72 EUR transaction amount
    When Admin sets the business date to "1 April 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 33.73 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Repayment        | 33.72  | 33.33     | 0.39     | 0.0  | 0.0       | 33.53        | false    |
      | 31 March 2024    | Accrual          | 1.16   | 0.0       | 1.16     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Repayment        | 33.73  | 33.53     | 0.2      | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C3771
  Scenario: Loan with Buy Down fees and early payoff - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 1 January 2024    | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "1 January 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 29   | 01 March 2024    |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 33.53         | 0.2     | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
    When Admin sets the business date to "1 March 2024"
    When Loan Pay-off is made on "1 March 2024"
    Then Loan's all installments have obligations met
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Repayment        | 67.25  | 66.86     | 0.39     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Accrual          | 0.97   | 0.0       | 0.97     | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C3772
  Scenario: Loan with Buy Down fees and charge-off - UC3
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 1 January 2024    | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "1 January 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 29   | 01 March 2024    |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 33.53         | 0.2     | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan status will be "ACTIVE"
    And Loan marked as charged-off on "01 March 2024"
    And Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable           |        | 66.86  |
      | ASSET     | 112603       | Interest/Fee Receivable    |        |  0.59  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt       |  66.86 |        |
      | INCOME    | 404001       | Interest Income Charge Off |   0.59 |        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Accrual          | 0.97   | 0.0       | 0.97     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Charge-off       | 67.45  | 66.86     | 0.59     | 0.0  | 0.0       | 0.0          | false    |

    When Loan Pay-off is made on "1 March 2024"
    Then Loan's all installments have obligations met
