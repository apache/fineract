@BuyDownFeeFeature
Feature:Feature: Buy Down Fees

  #TODO Waiting for the implementation of the feature PS-2569
  @Skip
  @TestRailId:C3770
  Scenario: Simple loan with Buy Down fees and full payment - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 1 January 2024    | 100            | 7                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "1 January 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 29   | 01 March 2024    |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 33.53         | 0.20     | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down fees    | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEES" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name           | Debit | Credit |
      | EXPENSE   | 51001        | BuyDown Expense        | 50.0  |        |
      | LIABILITY | 22001        | Deferred Income        |       | 50.0   |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024   | Buy Down fees    | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024  | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 33.72 EUR transaction amount
    When Admin sets the business date to "1 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 33.72 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024   | Buy Down fees    | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024  | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024     | Repayment        | 33.72  | 33.33     | 0.39     | 0.0  | 0.0       | 33.53        | false    |
      | 01 April 2024     | Repayment        | 33.73  | 33.53     | 0.20     | 0.0  | 0.0       | 0.0          | false    |

  #TODO Waiting for the implementation of the feature PS-2569
  #TODO test should be extended with checks for repayment schedule and transactions list after implementation of PS-2572
  @Skip
  @TestRailId:C3771
  Scenario: Loan with Buy Down fees and early payoff - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 1 January 2024    | 100            | 7                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "1 January 2024" with "50" EUR transaction amount
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 100.58 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down fees    | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 15 January 2024  | Repayment        | 100.58 | 100.0     | 0.58     | 0.0  | 0.0       | 0.0          | false    |

  #TODO Waiting for the implementation of the feature PS-2569
  #TODO test should be extended with checks for repayment schedule and transactions list after implementation of PS-2572
  @Skip
  @TestRailId:C3772
  Scenario: Loan with Buy Down fees and charge-off - UC3
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP_PROGRESSIVE_BUYDOWN_FEES_ENABLED | 1 January 2024    | 100            | 7                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD   | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "1 January 2024" with "50" EUR transaction amount
    When Admin sets the business date to "1 May 2024"
    And Admin does charge-off the loan on "1 May 2024"
    Then Loan status will be "ACTIVE"
    And Loan marked as charged-off on "1 May 2024"
    And Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 May 2024" which has the following Journal entries:
      | Type      | Account code | Account name           | Debit  | Credit |
      | EXPENSE   | 61002        | Credit Loss-Bad Debt   | 101.17 |        |
      | ASSET     | 12002        | Loans Receivable       |        | 100.0  |
      | ASSET     | 12003        | Interest Receivable    |        | 1.17   |