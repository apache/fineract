@BuyDownFeeFeature
Feature:Feature: Buy Down Fees

  @TestRailId:C3770
  Scenario: Verify loan with Buy Down fees and full payment - UC1.1
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
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Repayment                 | 33.72  | 33.33     | 0.39     | 0.0  | 0.0       | 33.53        | false    |
      | 31 March 2024    | Accrual                   | 1.16   | 0.0       | 1.16     | 0.0  | 0.0       | 0.0          | false    |
      | 31 March 2024    | Buy Down Fee Amortization | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Repayment                 | 33.73  | 33.53     | 0.2      | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C3827
  Scenario: Verify loan with Buy Down fees and full payment and daily amortization - UC1.2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 1 January 2024    | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin runs inline COB job for Loan
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
    When Admin sets the business date to "2 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 01 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "1 March 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 33.72 EUR transaction amount
    When Admin sets the business date to "1 April 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 33.73 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2024  | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 26 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 26 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 27 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 27 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 28 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 28 January 2024  | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |

      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 01 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 02 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 02 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 04 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 05 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 05 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 06 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 06 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 07 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 07 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 08 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 08 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 09 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 09 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 10 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 10 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 11 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 11 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 12 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 12 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 13 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 13 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 14 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 14 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 15 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 15 February 2024 | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 16 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 16 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 17 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 17 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 18 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 18 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 19 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 19 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 20 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 20 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 21 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 21 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 22 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 22 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 23 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 23 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 24 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 24 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 25 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 25 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 26 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 26 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 27 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 27 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 28 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 28 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |

      | 01 March 2024    | Repayment                 | 33.72  | 33.33     | 0.39     | 0.0  | 0.0       | 33.53        | false    |
      | 01 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 02 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 02 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 04 March 2024    | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 05 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 05 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 06 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 07 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 07 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 08 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 09 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 09 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 10 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 10 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 11 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 12 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 12 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 13 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 13 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 14 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 15 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 15 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 16 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 17 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 17 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 18 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 18 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 20 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 20 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 21 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 21 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 22 March 2024    | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 23 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 23 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 24 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 24 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 25 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 26 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 26 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 27 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 28 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 28 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 29 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 29 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 30 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 31 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 31 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |

      | 01 April 2024    | Repayment                 | 33.73  | 33.53     | 0.2      | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C3771
  Scenario: Verify loan with Buy Down fees and early payoff - UC2.1
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
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Repayment                 | 67.25  | 66.86     | 0.39     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Accrual                   | 0.97   | 0.0       | 0.97     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Buy Down Fee Amortization | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 50.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 50.0  |        |

  @TestRailId:C3828
  Scenario: Verify loan with Buy Down fees and early payoff and daily amortization - UC2.2
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
    When Admin sets the business date to "2 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 01 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "1 March 2024"
    When Admin runs inline COB job for Loan
    When Loan Pay-off is made on "1 March 2024"
    Then Loan's all installments have obligations met
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2024  | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 26 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 26 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 27 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 27 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 28 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 28 January 2024  | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |

      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 01 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 02 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 02 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 04 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 05 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 05 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 06 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 06 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 07 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 07 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 08 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 08 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 09 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 09 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 10 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 10 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 11 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 11 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 12 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 12 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 13 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 13 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 14 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 14 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 15 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 15 February 2024 | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 16 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 16 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 17 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 17 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 18 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 18 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 19 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 19 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 20 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 20 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 21 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 21 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 22 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 22 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 23 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 23 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 24 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 24 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 25 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 25 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 26 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 26 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 27 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 27 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 28 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 28 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |

      | 01 March 2024    | Repayment                 | 67.25  | 66.86     | 0.39     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Buy Down Fee Amortization | 17.03  | 0.0       | 17.03    | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 17.03  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 17.03 |        |

  @TestRailId:C3772
  Scenario: Verify loan with Buy Down fees and charge-off - UC3.1
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
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Buy Down Fee Amortization | 33.52  | 0.0       | 33.52    | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Accrual                   | 0.97   | 0.0       | 0.97     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Charge-off                | 67.45  | 66.86     | 0.59     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Buy Down Fee Amortization | 16.48  | 0.0       | 16.48    | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt        |       | 16.48  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 16.48 |        |

    When Loan Pay-off is made on "1 March 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3829
  Scenario: Verify loan with Buy Down fees and charge-off and daily amortization - UC3.2
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
    When Admin sets the business date to "2 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 01 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "1 March 2024"
    When Admin runs inline COB job for Loan
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan status will be "ACTIVE"
    And Loan marked as charged-off on "01 March 2024"
    And Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable           |        | 66.86  |
      | ASSET     | 112603       | Interest/Fee Receivable    |        |  0.59  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt       |  66.86 |        |
      | INCOME    | 404001       | Interest Income Charge Off |   0.59 |        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2024  | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 26 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 26 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 27 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 27 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 28 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 28 January 2024  | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2024  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2024  | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2024  | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |

      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 01 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 02 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 02 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 04 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 05 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 05 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 06 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 06 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 07 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 07 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 08 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 08 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 09 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 09 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 10 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 10 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 11 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 11 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 12 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 12 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 13 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 13 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 14 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 14 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 15 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 15 February 2024 | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 16 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 16 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 17 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 17 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 18 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 18 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 19 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 19 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 20 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 20 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 21 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 21 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 22 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 22 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 23 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 23 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 24 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 24 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 25 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 25 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 26 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 26 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 27 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 27 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 28 February 2024 | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 28 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |

      | 01 March 2024    | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Charge-off                | 67.45  | 66.86     | 0.59     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Buy Down Fee Amortization | 16.48  | 0.0       | 16.48    | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt        |       | 16.48  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 16.48 |        |

    When Loan Pay-off is made on "1 March 2024"
    Then Loan's all installments have obligations met

  @TestRailId:С3825
  Scenario: Verify loan with Buy Down Fee adjustment trn with daily amortization and repayment trns - UC4
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
      | 3  | 31   | 01 April 2024    |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
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
# --- 1st repayment on February,1 ---#
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 33.72 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0   | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0   | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 33.72 | 0.0        | 0.0  | 67.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
# --- BuyDownFee Adjustment trns on March,1 ---#
    When Admin sets the business date to "1 March 2024"
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "01 March 2024" with "10" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 33.72 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0   | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0   | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 33.72 | 0.0        | 0.0  | 67.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement            | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee            | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment               | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Buy Down Fee Adjustment | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 56.86        | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_ADJUSTMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 450280       | Buy Down Expense            |       | 10.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 10.0  |        |
# --- 2nd repayment on April,1 ---#
    When Admin sets the business date to "1 April 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 33.73 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Buy Down Fee Adjustment   | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 56.86        | false    |
      | 31 March 2024    | Accrual                   | 1.35   | 0.0       | 1.35     | 0.0  | 0.0       | 0.0          | false    |
      | 31 March 2024    | Buy Down Fee Amortization | 40.0   | 0.0       | 40.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Repayment                 | 33.73  | 33.34     | 0.39     | 0.0  | 0.0       | 23.52        | false    |

    When Loan Pay-off is made on "1 April 2024"
    Then Loan's all installments have obligations met
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement            | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee            | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment               | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Buy Down Fee Adjustment | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 56.86        | false    |
      | 31 March 2024    | Accrual                 | 1.35   | 0.0       | 1.35     | 0.0  | 0.0       | 0.0          | false    |
      | 31 March 2024    | Buy Down Fee Amortization | 40.0   | 0.0       | 40.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Repayment               | 33.73  | 33.34     | 0.39     | 0.0  | 0.0       | 23.52        | false    |
      | 01 April 2024    | Repayment               | 33.91  | 33.52     | 0.39     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Accrual                 | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:С3826
  Scenario: Verify loan with a few Buy Down Fee adjustment trns with daily amortization and repayment trns - UC5
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
# --- 1st repayment on February,1 ---#
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 33.72 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0   | 0.0        | 0.0  | 33.72       |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0   | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 33.72 | 0.0        | 0.0  | 67.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
# --- 1st BuyDownFee Adjustment trns on March,1 ---#
    When Admin sets the business date to "1 March 2024"
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "01 March 2024" with "10" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement            | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee            | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment               | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Buy Down Fee Adjustment | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 56.86        | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_ADJUSTMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 450280       | Buy Down Expense            |       | 10.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 10.0  |        |
# --- 2nd BuyDownFee Adjustment trns on March,15 ---#
    When Admin sets the business date to "15 March 2024"
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "15 March 2024" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement            | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee            | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment               | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Buy Down Fee Adjustment | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 56.86        | false    |
      | 15 March 2024    | Buy Down Fee Adjustment |  5.0   |  5.0      | 0.0      | 0.0  | 0.0       | 51.86        | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_ADJUSTMENT" transaction with date "15 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 450280       | Buy Down Expense            |       |  5.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income |  5.0  |        |
# --- 2nd repayment on April,1 ---#
    When Admin sets the business date to "1 April 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 33.73 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Buy Down Fee Adjustment   | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 56.86        | false    |
      | 15 March 2024    | Buy Down Fee Adjustment   |  5.0   |  5.0      | 0.0      | 0.0  | 0.0       | 51.86        | false    |
      | 31 March 2024    | Accrual                   | 1.35   | 0.0       | 1.35     | 0.0  | 0.0       | 0.0          | false    |
      | 31 March 2024    | Buy Down Fee Amortization | 35.0   | 0.0       | 35.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Repayment                 | 33.73  | 33.34     | 0.39     | 0.0  | 0.0       | 18.52        | false    |

    When Loan Pay-off is made on "1 April 2024"
    Then Loan's all installments have obligations met
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Buy Down Fee Adjustment   | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 56.86        | false    |
      | 15 March 2024    | Buy Down Fee Adjustment   |  5.0   |  5.0      | 0.0      | 0.0  | 0.0       | 51.86        | false    |
      | 31 March 2024    | Accrual                   | 1.35   | 0.0       | 1.35     | 0.0  | 0.0       | 0.0          | false    |
      | 31 March 2024    | Buy Down Fee Amortization | 35.0   | 0.0       | 35.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Repayment                 | 33.73  | 33.34     | 0.39     | 0.0  | 0.0       | 18.52        | false    |
      | 01 April 2024    | Repayment                 | 33.91  | 33.52     | 0.39     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
