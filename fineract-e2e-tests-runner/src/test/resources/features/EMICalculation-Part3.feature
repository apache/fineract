@Emi
Feature: EMI calculation and repayment schedule checks for interest bearing loans - Part3

  @TestRailId:C3302
  Scenario: UC18-2 - In case of repayment reversal the Interest Refund transaction needs to be recalculated
    # using 2021 for the test since as per UC - non-leap year with 365 days should be used
    When Admin sets the business date to "01 January 2021"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL | 01 January 2021   | 1000           | 9.9                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2021" with "1000" amount and expected disbursement date on "01 January 2021"
    When Admin successfully disburse the loan on "01 January 2021" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2021"
    And Customer makes "AUTOPAY" repayment on "10 January 2021" with 85.63 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2021  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2021 |           | 0.0             | 1000.0        | 7.9      | 0.0  | 0.0       | 1007.9 | 85.63 | 85.63      | 0.0  | 922.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 7.9      | 0.0  | 0.0       | 1007.9 | 85.63 | 85.63      | 0.0  | 922.27      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2021  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 January 2021  | Repayment        | 85.63  | 85.63     | 0.0      | 0.0  | 0.0       | 914.37       | false    | false    |
    When Admin sets the business date to "22 January 2021"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "22 January 2021" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      |    |      | 01 January 2021  |                 | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0     |            |      |             |
      | 1  | 31   | 01 February 2021 | 22 January 2021 | 0.0             | 1000.0        | 5.42     | 0.0  | 0.0       | 1005.42 | 1005.42 | 1005.42    | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 5.42     | 0.0  | 0.0       | 1005.42 | 1005.42 | 1005.42    | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2021  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 January 2021  | Repayment              | 85.63  | 85.63     | 0.0      | 0.0  | 0.0       | 914.37       | false    | false    |
      | 22 January 2021  | Merchant Issued Refund | 1000.0 | 914.37    | 5.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2021  | Interest Refund        | 5.42   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2021  | Accrual                | 5.42   | 0.0       | 5.42     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "23 January 2021"
    And Admin makes Credit Balance Refund transaction on "23 January 2021" with 85.63 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "10 January 2021"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2021  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2021 |           | 0.0             | 1085.63       | 5.9      | 0.0  | 0.0       | 1091.53 | 1005.7 | 1005.7     | 0.0  | 85.83       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1085.63       | 5.9      | 0.0  | 0.0       | 1091.53 | 1005.7 | 1005.7     | 0.0  | 85.83       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2021  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 January 2021  | Repayment              | 85.63  | 85.63     | 0.0      | 0.0  | 0.0       | 914.37       | true     | false    |
      | 22 January 2021  | Merchant Issued Refund | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 22 January 2021  | Interest Refund        | 5.7    | 0.0       | 5.7      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 23 January 2021  | Credit Balance Refund  | 85.63  | 85.63     | 0.0      | 0.0  | 0.0       | 85.63        | false    | true     |
      | 22 January 2021  | Accrual                | 5.42   | 0.0       | 5.42     | 0.0  | 0.0       | 0.0          | false    | false    |
    And In Loan Transactions the "2"th Transaction has Transaction type="Repayment" and is reverted

  @TestRailId:C3303
  Scenario: UC18-3 - In case of refund reversal the Interest Refund transaction needs to be recalculated
 # using 2021 for the test since as per UC - non-leap year with 365 days should be used
    When Admin sets the business date to "01 January 2021"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL | 01 January 2021   | 1000           | 9.9                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2021" with "1000" amount and expected disbursement date on "01 January 2021"
    When Admin successfully disburse the loan on "01 January 2021" with "1000" EUR transaction amount
    When Admin sets the business date to "22 January 2021"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "22 January 2021" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2021  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2021 | 22 January 2021 | 0.0             | 1000.0        | 5.7      | 0.0  | 0.0       | 1005.7 | 1005.7 | 1005.7     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 5.7      | 0.0  | 0.0       | 1005.7 | 1005.7 | 1005.7     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2021  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 22 January 2021  | Merchant Issued Refund | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2021  | Interest Refund        | 5.7    | 0.0       | 5.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2021  | Accrual                | 5.7    | 0.0       | 5.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "22 January 2021"
    When Admin sets the business date to "23 January 2021"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2021  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2021 |           | 0.0             | 1000.0        | 8.41     | 0.0  | 0.0       | 1008.41 | 0.0  | 0.0        | 0.0  | 1008.41     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 8.41     | 0.0  | 0.0       | 1008.41 | 0.0  | 0.0        | 0.0  | 1008.41     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2021  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 22 January 2021  | Merchant Issued Refund | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 22 January 2021  | Interest Refund        | 5.7    | 0.0       | 5.7      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 22 January 2021  | Accrual                | 5.7    | 0.0       | 5.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then In Loan Transactions the "2"th Transaction has Transaction type="Merchant Issued Refund" and is reverted
    Then In Loan Transactions the "3"th Transaction has Transaction type="Interest Refund" and is reverted

  @TestRailId:C3313
  Scenario: Verify that due date charges after maturity date is recognized on repayment schedule
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 75.21           | 24.79         | 0.58     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 2  | 29   | 01 March 2024    |           | 50.28           | 24.93         | 0.44     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 3  | 31   | 01 April 2024    |           | 25.2            | 25.08         | 0.29     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 25.2          | 0.15     | 0.0  | 0.0       | 25.35 | 0.0  | 0.0        | 0.0  | 25.35       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.46     | 0.0  | 0.0       | 101.46 | 0.0  | 0.0        | 0.0  | 101.46      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "20 April 2024"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "15 May 2024"
    And Admin runs inline COB job for Loan
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 May 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 75.21           | 24.79         | 0.58     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 2  | 29   | 01 March 2024    |           | 50.42           | 24.79         | 0.58     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 3  | 31   | 01 April 2024    |           | 25.63           | 24.79         | 0.58     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 25.63         | 0.58     | 0.0  | 0.0       | 26.21 | 0.0  | 0.0        | 0.0  | 26.21       |
      | 5  | 14   | 15 May 2024      |           | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.32     | 0.0  | 10.0      | 112.32 | 0.0  | 0.0        | 0.0  | 112.32      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 19 April 2024    | Accrual          | 1.66   | 0.0       | 1.66     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2024    | Accrual          | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2024      | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of   | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 15 May 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |

  @TestRailId:C3333
  Scenario: Verify that due date charges after maturity date with inline COB run is recognized on repayment schedule
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 100.0         | 0.58     | 0.0  | 0.0       | 100.58 | 0.0  | 0.0        | 0.0  | 100.58      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.58     | 0.0  | 0.0       | 100.58 | 0.0  | 0.0        | 0.0  | 100.58      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "15 February 2024"
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 February 2024" due date and 10 EUR transaction amount
    When Admin sets the business date to "16 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 100.0         | 0.58     | 0.0  | 0.0       | 100.58 | 0.0  | 0.0        | 0.0  | 100.58      |
      | 2  | 14   | 15 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.58     | 0.0  | 10.0      | 110.58 | 0.0  | 0.0        | 0.0  | 110.58      |
    And Loan Transactions tab has the following data:
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
      | 15 February 2024 | Accrual          | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
    And Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 15 February 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |

  @TestRailId:C3314
  Scenario: Verify that interest refund transaction won't be created and displayed when Merchant issued refund happens on disbursement date
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 75.21           | 24.79         | 0.58     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 2  | 29   | 01 March 2024    |           | 50.28           | 24.93         | 0.44     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 3  | 31   | 01 April 2024    |           | 25.2            | 25.08         | 0.29     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 25.2          | 0.15     | 0.0  | 0.0       | 25.35 | 0.0  | 0.0        | 0.0  | 25.35       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.46     | 0.0  | 0.0       | 101.46 | 0.0  | 0.0        | 0.0  | 101.46      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "01 January 2024" with 100 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 January 2024 | 74.63           | 25.37         | 0.0      | 0.0  | 0.0       | 25.37 | 25.37 | 25.37      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 January 2024 | 49.26           | 25.37         | 0.0      | 0.0  | 0.0       | 25.37 | 25.37 | 25.37      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 January 2024 | 23.89           | 25.37         | 0.0      | 0.0  | 0.0       | 25.37 | 25.37 | 25.37      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 January 2024 | 0.0             | 23.89         | 0.0      | 0.0  | 0.0       | 23.89 | 23.89 | 23.89      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 100.0      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Merchant Issued Refund | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "MERCHANT_ISSUED_REFUND" transaction type to "REAMORTIZATION" future installment allocation rule

  @TestRailId:C3322
  Scenario: Verify accrual activity with amend rate factor after calculated interest for period was rounded - UC1: Preclose, with full disbursement at first day, accrual activity after month
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
#    --- Accrual activity ---
    When Admin sets the business date to "02 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 07 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 08 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 09 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 10 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 11 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 12 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 13 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 14 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 15 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 16 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 17 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 18 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 19 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 20 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 21 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 22 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 23 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 24 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 25 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 26 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 27 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 28 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 29 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 30 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 31 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 01 February 2024 | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3323
  Scenario: Verify accrual activity with amend rate factor after calculated interest for period was rounded - UC2: Preclose, with multi disbursements, accrual activity after month
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 2000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "2000" amount and expected disbursement date on "01 January 2024"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 2000.0          |               |          | 0.0  |           | 0.0     |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 2000.0        | 11.67    | 0.0  | 0.0       | 2011.67 | 0.0  | 0.0        | 0.0  | 2011.67     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2000.0        | 11.67    | 0.0  | 0.0       | 2011.67 | 0.0  | 0.0        | 0.0  | 2011.67     |
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
#    --- Accrual activity after first disbursement ---
    When Admin sets the business date to "15 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 07 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 08 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 09 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 10 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 11 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 12 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 13 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 14 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
#    --- Accrual activity after second disbursement ---
    When Admin successfully disburse the loan on "15 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 15 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 2000.0        | 9.03     | 0.0  | 0.0       | 2009.03 | 0.0  | 0.0        | 0.0  | 2009.03     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2000.0        | 9.03     | 0.0  | 0.0       | 2009.03 | 0.0  | 0.0        | 0.0  | 2009.03     |
    When Admin sets the business date to "02 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 15 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 2000.0        | 9.03     | 0.0  | 0.0       | 2009.03 | 0.0  | 0.0        | 0.0  | 2009.03     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2000.0        | 9.03     | 0.0  | 0.0       | 2009.03 | 0.0  | 0.0        | 0.0  | 2009.03     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 03 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 04 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 05 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 06 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 07 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 08 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 09 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 10 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 11 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 12 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 13 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 14 January 2024  | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          |
      | 15 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       |
      | 15 January 2024  | Accrual          | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          |
      | 16 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 17 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 18 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          |
      | 19 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 20 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 21 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          |
      | 22 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 23 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 24 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          |
      | 25 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 26 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          |
      | 27 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 28 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 29 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          |
      | 30 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 31 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          |
      | 01 February 2024 | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3327
  Scenario: Verify accruals isn't reversed and replayed in COB for loan with disabled auto repayment for down payment
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT | 01 January 2024   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "800" amount and expected disbursement date on "01 January 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 800.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 0    | 01 January 2024  |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 31   | 01 February 2024 |           | 451.31          | 148.69        | 3.5      | 0.0  | 0.0       | 152.19 | 0.0  | 0.0        | 0.0  | 152.19      |
      | 3  | 29   | 01 March 2024    |           | 301.75          | 149.56        | 2.63     | 0.0  | 0.0       | 152.19 | 0.0  | 0.0        | 0.0  | 152.19      |
      | 4  | 31   | 01 April 2024    |           | 151.32          | 150.43        | 1.76     | 0.0  | 0.0       | 152.19 | 0.0  | 0.0        | 0.0  | 152.19      |
      | 5  | 30   | 01 May 2024      |           | 0.0             | 151.32        | 0.88     | 0.0  | 0.0       | 152.2  | 0.0  | 0.0        | 0.0  | 152.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.0         | 8.77     | 0.0  | 0.0       | 808.77 | 0.0  | 0.0        | 0.0  | 808.77      |
    When Admin sets the business date to "05 January 2024"
    And Admin successfully disburse the loan on "03 January 2024" with "800" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "03 January 2024" with 200 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 03 January 2024  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 03 January 2024  | 03 January 2024 | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 200.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 03 February 2024 |                 | 451.31          | 148.69        | 3.5      | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 3  | 29   | 03 March 2024    |                 | 301.75          | 149.56        | 2.63     | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 4  | 31   | 03 April 2024    |                 | 151.32          | 150.43        | 1.76     | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 5  | 30   | 03 May 2024      |                 | 0.0             | 151.32        | 0.88     | 0.0  | 0.0       | 152.2  | 0.0   | 0.0        | 0.0  | 152.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 800.0         | 8.77     | 0.0  | 0.0       | 808.77 | 200.0 | 0.0        | 0.0  | 608.77      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 03 January 2024  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 03 January 2024  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 03 January 2024  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 03 January 2024  | 03 January 2024 | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 200.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 03 February 2024 |                 | 451.31          | 148.69        | 3.5      | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 3  | 29   | 03 March 2024    |                 | 301.75          | 149.56        | 2.63     | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 4  | 31   | 03 April 2024    |                 | 151.32          | 150.43        | 1.76     | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 5  | 30   | 03 May 2024      |                 | 0.0             | 151.32        | 0.88     | 0.0  | 0.0       | 152.2  | 0.0   | 0.0        | 0.0  | 152.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 800.0         | 8.77     | 0.0  | 0.0       | 808.77 | 200.0 | 0.0        | 0.0  | 608.77      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 03 January 2024  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 03 January 2024  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 04 January 2024  | Accrual          | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3328
  Scenario: Verify accruals isn't reversed and replayed in COB for loan with enabled auto repayment for down payment
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 01 January 2024   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "800" amount and expected disbursement date on "01 January 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 800.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 0    | 01 January 2024  |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 31   | 01 February 2024 |           | 451.31          | 148.69        | 3.5      | 0.0  | 0.0       | 152.19 | 0.0  | 0.0        | 0.0  | 152.19      |
      | 3  | 29   | 01 March 2024    |           | 301.75          | 149.56        | 2.63     | 0.0  | 0.0       | 152.19 | 0.0  | 0.0        | 0.0  | 152.19      |
      | 4  | 31   | 01 April 2024    |           | 151.32          | 150.43        | 1.76     | 0.0  | 0.0       | 152.19 | 0.0  | 0.0        | 0.0  | 152.19      |
      | 5  | 30   | 01 May 2024      |           | 0.0             | 151.32        | 0.88     | 0.0  | 0.0       | 152.2  | 0.0  | 0.0        | 0.0  | 152.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.0         | 8.77     | 0.0  | 0.0       | 808.77 | 0.0  | 0.0        | 0.0  | 808.77      |
    When Admin sets the business date to "05 January 2024"
    And Admin successfully disburse the loan on "03 January 2024" with "800" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 03 January 2024  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 03 January 2024  | 03 January 2024 | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 200.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 03 February 2024 |                 | 451.31          | 148.69        | 3.5      | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 3  | 29   | 03 March 2024    |                 | 301.75          | 149.56        | 2.63     | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 4  | 31   | 03 April 2024    |                 | 151.32          | 150.43        | 1.76     | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 5  | 30   | 03 May 2024      |                 | 0.0             | 151.32        | 0.88     | 0.0  | 0.0       | 152.2  | 0.0   | 0.0        | 0.0  | 152.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 800.0         | 8.77     | 0.0  | 0.0       | 808.77 | 200.0 | 0.0        | 0.0  | 608.77      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 03 January 2024  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 03 January 2024  | Down Payment     | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 03 January 2024  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 03 January 2024  | 03 January 2024 | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 200.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 03 February 2024 |                 | 451.31          | 148.69        | 3.5      | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 3  | 29   | 03 March 2024    |                 | 301.75          | 149.56        | 2.63     | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 4  | 31   | 03 April 2024    |                 | 151.32          | 150.43        | 1.76     | 0.0  | 0.0       | 152.19 | 0.0   | 0.0        | 0.0  | 152.19      |
      | 5  | 30   | 03 May 2024      |                 | 0.0             | 151.32        | 0.88     | 0.0  | 0.0       | 152.2  | 0.0   | 0.0        | 0.0  | 152.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 800.0         | 8.77     | 0.0  | 0.0       | 808.77 | 200.0 | 0.0        | 0.0  | 608.77      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 03 January 2024  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 03 January 2024  | Down Payment     | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 04 January 2024  | Accrual          | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3329
  Scenario: Verify interest rate should not be calculated on past due principle amount for progressive loans - case when lesser than EMI amount was paid
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_NO_CALC_ON_PAST_DUE_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    #    --- 1st installment overdue ---
    When Admin sets the business date to "02 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Accrual          | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- late payment comes in lesser than EMI amount ---
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 15.0 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 15.0 | 0.0        | 15.0 | 2.01        |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 15.0 | 0.0        | 15.0 | 87.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Accrual          | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Repayment        | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 85.0         | false    | false    |

  @TestRailId:C3330
  Scenario: Verify interest rate should not be calculated on past due principle amount for progressive loans - case when full EMI amount was paid
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_NO_CALC_ON_PAST_DUE_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    #    --- 1st installment overdue ---
    When Admin sets the business date to "02 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Accrual          | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- late payment comes with correct EMI amount ---
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 15 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0   | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 17.01 | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Accrual          | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |

  @TestRailId:C3331
  Scenario: Verify interest rate should not be calculated on past due principle amount for progressive loans - case when excess EMI amount was paid
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_NO_CALC_ON_PAST_DUE_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    #    --- 1st installment overdue ---
    When Admin sets the business date to "02 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Accrual          | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- late payment comes in with excess EMI amount ---
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 34.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 15 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 February 2024 | 66.8            | 16.77         | 0.24     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.38           | 16.42         | 0.59     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.66           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.85           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.85         | 0.1      | 0.0  | 0.0       | 16.95 | 0.0   | 0.0        | 0.0   | 16.95       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 2.0      | 0.0  | 0.0       | 102.0 | 34.02 | 17.01      | 17.01 | 67.98       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Accrual          | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Repayment        | 34.02  | 33.2      | 0.82     | 0.0  | 0.0       | 66.8         | false    | false    |

  @TestRailId:C3332
  Scenario: Verify interest rate should not be calculated on past due principle amount for progressive loans - case when multiple disbursal occurred with full EMI amount was paid
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_NO_CALC_ON_PAST_DUE_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    #    --- 2nd disbursement ---
    When Admin sets the business date to "10 February 2024"
    And Admin successfully disburse the loan on "10 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      |    |      | 10 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 147.14          | 36.43         | 0.89     | 0.0  | 0.0       | 37.32 | 0.0  | 0.0        | 0.0  | 37.32       |
      | 3  | 31   | 01 April 2024    |           | 110.68          | 36.46         | 0.86     | 0.0  | 0.0       | 37.32 | 0.0  | 0.0        | 0.0  | 37.32       |
      | 4  | 30   | 01 May 2024      |           | 74.01           | 36.67         | 0.65     | 0.0  | 0.0       | 37.32 | 0.0  | 0.0        | 0.0  | 37.32       |
      | 5  | 31   | 01 June 2024     |           | 37.12           | 36.89         | 0.43     | 0.0  | 0.0       | 37.32 | 0.0  | 0.0        | 0.0  | 37.32       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 37.12         | 0.22     | 0.0  | 0.0       | 37.34 | 0.0  | 0.0        | 0.0  | 37.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 3.63     | 0.0  | 0.0       | 203.63 | 0.0  | 0.0        | 0.0  | 203.63      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 10 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    #    --- late payment comes with full amount ---
    When Admin sets the business date to "15 March 2024"
    And Customer makes "AUTOPAY" repayment on "15 March 2024" with 54.33 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 15 March 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      |    |      | 10 February 2024 |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 29   | 01 March 2024    | 15 March 2024 | 147.14          | 36.43         | 0.89     | 0.0  | 0.0       | 37.32 | 37.32 | 0.0        | 37.32 | 0.0         |
      | 3  | 31   | 01 April 2024    |               | 110.68          | 36.46         | 0.86     | 0.0  | 0.0       | 37.32 | 0.0   | 0.0        | 0.0   | 37.32       |
      | 4  | 30   | 01 May 2024      |               | 74.01           | 36.67         | 0.65     | 0.0  | 0.0       | 37.32 | 0.0   | 0.0        | 0.0   | 37.32       |
      | 5  | 31   | 01 June 2024     |               | 37.12           | 36.89         | 0.43     | 0.0  | 0.0       | 37.32 | 0.0   | 0.0        | 0.0   | 37.32       |
      | 6  | 30   | 01 July 2024     |               | 0.0             | 37.12         | 0.22     | 0.0  | 0.0       | 37.34 | 0.0   | 0.0        | 0.0   | 37.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 200.0         | 3.63     | 0.0  | 0.0       | 203.63 | 54.33 | 0.0        | 54.33 | 149.3       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 10 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 15 March 2024    | Repayment        | 54.33  | 52.86     | 1.47     | 0.0  | 0.0       | 147.14       | false    | false    |

  @TestRailId:C3334
  Scenario: Verify that COB works properly while creating accruals for a overpaid account (accruals created on COB not when charge is created)
    When Admin sets the business date to "20 October 2024"
    And Admin creates a client with random data
    And Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 20 October 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "20 October 2024" with "100" amount and expected disbursement date on "20 October 2024"
    And Admin successfully disburse the loan on "20 October 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "21 October 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "22 October 2024"
    And Customer makes "AUTOPAY" repayment on "22 October 2024" with 102 EUR transaction amount
    Then Loan status will be "OVERPAID"
    And Loan has 2 overpaid amount
    When Admin sets the business date to "23 October 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 20 October 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 19 November 2024 | 22 October 2024 | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 100.0      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 100.0      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 October 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 22 October 2024  | Repayment        | 102.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin adds "LOAN_NSF_FEE" due date charge with "23 October 2024" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 20 October 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 19 November 2024 |           | 0.0             | 100.0         | 0.0      | 0.0  | 20.0      | 120.0 | 102.0 | 102.0      | 0.0  | 18.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 20.0      | 120.0 | 102.0 | 102.0      | 0.0  | 18.0        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 October 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 22 October 2024  | Repayment        | 102.0  | 100.0     | 0.0      | 0.0  | 2.0       | 0.0          | false    | true     |
    And Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2024 | Flat             | 20.0 | 2.0  | 0.0    | 18.0        |
    When Admin sets the business date to "24 October 2024"
    And Admin runs inline COB job for Loan
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 20 October 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 19 November 2024 |           | 0.0             | 100.0         | 0.0      | 0.0  | 20.0      | 120.0 | 102.0 | 102.0      | 0.0  | 18.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 20.0      | 120.0 | 102.0 | 102.0      | 0.0  | 18.0        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 October 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 22 October 2024  | Repayment        | 102.0  | 100.0     | 0.0      | 0.0  | 2.0       | 0.0          | false    | true     |
      | 23 October 2024  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          | false    | false    |
    And Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2024 | Flat             | 20.0 | 2.0  | 0.0    | 18.0        |
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C3384
  Scenario: Verify the repayment schedule in case of interest bearing loan, interest recalculation enabled, 12 months loan, Merchant issued refund (next installment) on disbursement date
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1 | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 919.3           | 80.7          | 5.83     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 2  | 28   | 01 March 2025     |           | 838.13          | 81.17         | 5.36     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 3  | 31   | 01 April 2025     |           | 756.49          | 81.64         | 4.89     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 4  | 30   | 01 May 2025       |           | 674.37          | 82.12         | 4.41     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 5  | 31   | 01 June 2025      |           | 591.77          | 82.6          | 3.93     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 6  | 30   | 01 July 2025      |           | 508.69          | 83.08         | 3.45     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 7  | 31   | 01 August 2025    |           | 425.13          | 83.56         | 2.97     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 8  | 31   | 01 September 2025 |           | 341.08          | 84.05         | 2.48     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 9  | 30   | 01 October 2025   |           | 256.54          | 84.54         | 1.99     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 10 | 31   | 01 November 2025  |           | 171.51          | 85.03         | 1.5      | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 11 | 30   | 01 December 2025  |           | 85.98           | 85.53         | 1.0      | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 85.98         | 0.5      | 0.0  | 0.0       | 86.48 | 0.0  | 0.0        | 0.0  | 86.48       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 38.31    | 0.0  | 0.0       | 1038.31 | 0.0  | 0.0        | 0.0  | 1038.31     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "01 January 2025" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 January 2025 | 913.47          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 January 2025 | 826.94          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025     | 01 January 2025 | 740.41          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025       | 01 January 2025 | 653.88          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025      | 01 January 2025 | 567.35          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025      | 01 January 2025 | 480.82          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2025    | 01 January 2025 | 394.29          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 8  | 31   | 01 September 2025 | 01 January 2025 | 307.76          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 9  | 30   | 01 October 2025   | 01 January 2025 | 221.23          | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 10 | 31   | 01 November 2025  | 01 January 2025 | 134.7           | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 11 | 30   | 01 December 2025  | 01 January 2025 | 48.17           | 86.53         | 0.0      | 0.0  | 0.0       | 86.53 | 86.53 | 86.53      | 0.0  | 0.0         |
      | 12 | 31   | 01 January 2026   | 01 January 2025 | 0.0             | 48.17         | 0.0      | 0.0  | 0.0       | 48.17 | 48.17 | 48.17      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 1000.0     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2025  | Merchant Issued Refund | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1" loan product "MERCHANT_ISSUED_REFUND" transaction type to "REAMORTIZATION" future installment allocation rule

  @TestRailId:C3385
  Scenario: Verify the repayment schedule in case of interest bearing loan, interest recalculation enabled, 12 months loan, Merchant issued refund (reamortization) on disbursement date
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1" loan product "MERCHANT_ISSUED_REFUND" transaction type to "REAMORTIZATION" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1 | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025  |           | 919.3           | 80.7          | 5.83     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 2  | 28   | 01 March 2025     |           | 838.13          | 81.17         | 5.36     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 3  | 31   | 01 April 2025     |           | 756.49          | 81.64         | 4.89     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 4  | 30   | 01 May 2025       |           | 674.37          | 82.12         | 4.41     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 5  | 31   | 01 June 2025      |           | 591.77          | 82.6          | 3.93     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 6  | 30   | 01 July 2025      |           | 508.69          | 83.08         | 3.45     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 7  | 31   | 01 August 2025    |           | 425.13          | 83.56         | 2.97     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 8  | 31   | 01 September 2025 |           | 341.08          | 84.05         | 2.48     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 9  | 30   | 01 October 2025   |           | 256.54          | 84.54         | 1.99     | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 10 | 31   | 01 November 2025  |           | 171.51          | 85.03         | 1.5      | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 11 | 30   | 01 December 2025  |           | 85.98           | 85.53         | 1.0      | 0.0  | 0.0       | 86.53 | 0.0  | 0.0        | 0.0  | 86.53       |
      | 12 | 31   | 01 January 2026   |           | 0.0             | 85.98         | 0.5      | 0.0  | 0.0       | 86.48 | 0.0  | 0.0        | 0.0  | 86.48       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 38.31    | 0.0  | 0.0       | 1038.31 | 0.0  | 0.0        | 0.0  | 1038.31     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "01 January 2025" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025  | 01 January 2025 | 916.67          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025     | 01 January 2025 | 833.34          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025     | 01 January 2025 | 750.01          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025       | 01 January 2025 | 666.68          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025      | 01 January 2025 | 583.35          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025      | 01 January 2025 | 500.02          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2025    | 01 January 2025 | 416.69          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 8  | 31   | 01 September 2025 | 01 January 2025 | 333.36          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 9  | 30   | 01 October 2025   | 01 January 2025 | 250.03          | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 10 | 31   | 01 November 2025  | 01 January 2025 | 166.7           | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 11 | 30   | 01 December 2025  | 01 January 2025 | 83.37           | 83.33         | 0.0      | 0.0  | 0.0       | 83.33 | 83.33 | 83.33      | 0.0  | 0.0         |
      | 12 | 31   | 01 January 2026   | 01 January 2025 | 0.0             | 83.37         | 0.0      | 0.0  | 0.0       | 83.37 | 83.37 | 83.37      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 1000.0     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2025  | Merchant Issued Refund | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3387
  Scenario: Verify that no negative amount is calculated for Accruals
    When Admin sets the business date to "09 December 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 09 December 2024  | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "09 December 2024" with "800" amount and expected disbursement date on "09 December 2024"
    And Admin successfully disburse the loan on "09 December 2024" with "800" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "10 December 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "11 December 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 09 January 2025  |           | 668.69          | 131.31        | 4.75     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 2  | 31   | 09 February 2025 |           | 536.61          | 132.08        | 3.98     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 3  | 28   | 09 March 2025    |           | 403.43          | 133.18        | 2.88     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 4  | 31   | 09 April 2025    |           | 269.77          | 133.66        | 2.4      | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 5  | 30   | 09 May 2025      |           | 135.26          | 134.51        | 1.55     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 6  | 31   | 09 June 2025     |           | 0.0             | 135.26        | 0.8      | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.0         | 16.36    | 0.0  | 0.0       | 816.36 | 0.0  | 0.0        | 0.0  | 816.36      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "08 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 09 January 2025  |           | 668.69          | 131.31        | 4.75     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 2  | 31   | 09 February 2025 |           | 536.61          | 132.08        | 3.98     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 3  | 28   | 09 March 2025    |           | 403.43          | 133.18        | 2.88     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 4  | 31   | 09 April 2025    |           | 269.77          | 133.66        | 2.4      | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 5  | 30   | 09 May 2025      |           | 135.26          | 134.51        | 1.55     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
      | 6  | 31   | 09 June 2025     |           | 0.0             | 135.26        | 0.8      | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.0         | 16.36    | 0.0  | 0.0       | 816.36 | 0.0  | 0.0        | 0.0  | 816.36      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "09 January 2025"
    And Customer makes "AUTOPAY" repayment on "09 January 2025" with 136.06 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 668.69          | 131.31        | 4.75     | 0.0  | 0.0       | 136.06 | 136.06 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 |                 | 536.61          | 132.08        | 3.98     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 3  | 28   | 09 March 2025    |                 | 403.43          | 133.18        | 2.88     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 4  | 31   | 09 April 2025    |                 | 269.77          | 133.66        | 2.4      | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 5  | 30   | 09 May 2025      |                 | 135.26          | 134.51        | 1.55     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 6  | 31   | 09 June 2025     |                 | 0.0             | 135.26        | 0.8      | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 16.36    | 0.0  | 0.0       | 816.36 | 136.06 | 0.0        | 0.0  | 680.3       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 136.06 | 131.31    | 4.75     | 0.0  | 0.0       | 668.69       | false    | false    |
    When Admin sets the business date to "10 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 668.69          | 131.31        | 4.75     | 0.0  | 0.0       | 136.06 | 136.06 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 |                 | 536.61          | 132.08        | 3.98     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 3  | 28   | 09 March 2025    |                 | 403.43          | 133.18        | 2.88     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 4  | 31   | 09 April 2025    |                 | 269.77          | 133.66        | 2.4      | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 5  | 30   | 09 May 2025      |                 | 135.26          | 134.51        | 1.55     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 6  | 31   | 09 June 2025     |                 | 0.0             | 135.26        | 0.8      | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 16.36    | 0.0  | 0.0       | 816.36 | 136.06 | 0.0        | 0.0  | 680.3       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 136.06 | 131.31    | 4.75     | 0.0  | 0.0       | 668.69       | false    | false    |
      | 09 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity | 4.75   | 0.0       | 4.75     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "10 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 668.69          | 131.31        | 4.75     | 0.0  | 0.0       | 136.06 | 136.06 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 |                 | 536.61          | 132.08        | 3.98     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 3  | 28   | 09 March 2025    |                 | 403.43          | 133.18        | 2.88     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 4  | 31   | 09 April 2025    |                 | 269.77          | 133.66        | 2.4      | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 5  | 30   | 09 May 2025      |                 | 135.26          | 134.51        | 1.55     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 6  | 31   | 09 June 2025     |                 | 0.0             | 135.26        | 0.8      | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 16.36    | 0.0  | 0.0       | 816.36 | 136.06 | 0.0        | 0.0  | 680.3       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 136.06 | 131.31    | 4.75     | 0.0  | 0.0       | 668.69       | false    | false    |
      | 09 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity | 4.75   | 0.0       | 4.75     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "11 January 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 668.69          | 131.31        | 4.75     | 0.0  | 0.0       | 136.06 | 136.06 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 |                 | 536.61          | 132.08        | 3.98     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 3  | 28   | 09 March 2025    |                 | 403.43          | 133.18        | 2.88     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 4  | 31   | 09 April 2025    |                 | 269.77          | 133.66        | 2.4      | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 5  | 30   | 09 May 2025      |                 | 135.26          | 134.51        | 1.55     | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
      | 6  | 31   | 09 June 2025     |                 | 0.0             | 135.26        | 0.8      | 0.0  | 0.0       | 136.06 | 0.0    | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 16.36    | 0.0  | 0.0       | 816.36 | 136.06 | 0.0        | 0.0  | 680.3       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 136.06 | 131.31    | 4.75     | 0.0  | 0.0       | 668.69       | false    | false    |
      | 09 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity | 4.75   | 0.0       | 4.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer makes "AUTOPAY" repayment on "10 January 2025" with 680.3 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 09 December 2024 |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 09 January 2025  | 09 January 2025 | 668.69          | 131.31        | 4.75     | 0.0  | 0.0       | 136.06 | 136.06 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 February 2025 | 10 January 2025 | 536.61          | 132.08        | 3.98     | 0.0  | 0.0       | 136.06 | 136.06 | 136.06     | 0.0  | 0.0         |
      | 3  | 28   | 09 March 2025    | 10 January 2025 | 403.43          | 133.18        | 2.88     | 0.0  | 0.0       | 136.06 | 136.06 | 136.06     | 0.0  | 0.0         |
      | 4  | 31   | 09 April 2025    | 10 January 2025 | 269.77          | 133.66        | 2.4      | 0.0  | 0.0       | 136.06 | 136.06 | 136.06     | 0.0  | 0.0         |
      | 5  | 30   | 09 May 2025      | 10 January 2025 | 135.26          | 134.51        | 1.55     | 0.0  | 0.0       | 136.06 | 136.06 | 136.06     | 0.0  | 0.0         |
      | 6  | 31   | 09 June 2025     | 10 January 2025 | 0.0             | 135.26        | 0.8      | 0.0  | 0.0       | 136.06 | 136.06 | 136.06     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.0         | 16.36    | 0.0  | 0.0       | 816.36 | 816.36 | 680.3      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 December 2024 | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 10 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 December 2024 | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 December 2024 | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Repayment        | 136.06 | 131.31    | 4.75     | 0.0  | 0.0       | 668.69       | false    | false    |
      | 09 January 2025  | Accrual          | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual Activity | 4.75   | 0.0       | 4.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Repayment        | 680.3  | 668.69    | 11.61    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual Activity | 11.61  | 0.0       | 11.61    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 11.48  | 0.0       | 11.48    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3433
  Scenario: Verify partial interest calculated on loan with disbursement date '12 December 2023' and 10000 amount - UC1
    When Admin sets the business date to "12 December 2023"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 12 December 2023  | 10000          | 9.482                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "12 December 2023" with "10000" amount and expected disbursement date on "12 December 2023"
    And Admin successfully disburse the loan on "12 December 2023" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 12 December 2023 |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 12 January 2024  |           | 8367.33         | 1632.67       | 80.45    | 0.0  | 0.0       | 1713.12 | 0.0  | 0.0        | 0.0  | 1713.12     |
      | 2  | 31   | 12 February 2024 |           | 6721.41         | 1645.92       | 67.2     | 0.0  | 0.0       | 1713.12 | 0.0  | 0.0        | 0.0  | 1713.12     |
      | 3  | 29   | 12 March 2024    |           | 5058.79         | 1662.62       | 50.5     | 0.0  | 0.0       | 1713.12 | 0.0  | 0.0        | 0.0  | 1713.12     |
      | 4  | 31   | 12 April 2024    |           | 3386.3          | 1672.49       | 40.63    | 0.0  | 0.0       | 1713.12 | 0.0  | 0.0        | 0.0  | 1713.12     |
      | 5  | 30   | 12 May 2024      |           | 1699.5          | 1686.8        | 26.32    | 0.0  | 0.0       | 1713.12 | 0.0  | 0.0        | 0.0  | 1713.12     |
      | 6  | 31   | 12 June 2024     |           | 0.0             | 1699.5        | 13.65    | 0.0  | 0.0       | 1713.15 | 0.0  | 0.0        | 0.0  | 1713.15     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 278.75   | 0.0  | 0.0       | 10278.75 | 0.0  | 0.0        | 0.0  | 10278.75    |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 12 December 2023 | Disbursement     | 10000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 10000.0      | false    | false    |

  @TestRailId:C3434
  Scenario: Verify partial interest calculated on loan with disbursement date '12 December 2023' and 331.77 amount - UC2
    When Admin sets the business date to "12 December 2023"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 12 December 2023  | 331.77         | 10.65                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 5                 | MONTHS                | 1              | MONTHS                 | 5                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "12 December 2023" with "331.77" amount and expected disbursement date on "12 December 2023"
    And Admin successfully disburse the loan on "12 December 2023" with "331.77" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 12 December 2023 |           | 331.77          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 12 January 2024  |           | 266.63          | 65.14         | 3.0      | 0.0  | 0.0       | 68.14 | 0.0  | 0.0        | 0.0  | 68.14       |
      | 2  | 31   | 12 February 2024 |           | 200.9           | 65.73         | 2.41     | 0.0  | 0.0       | 68.14 | 0.0  | 0.0        | 0.0  | 68.14       |
      | 3  | 29   | 12 March 2024    |           | 134.46          | 66.44         | 1.7      | 0.0  | 0.0       | 68.14 | 0.0  | 0.0        | 0.0  | 68.14       |
      | 4  | 31   | 12 April 2024    |           | 67.53           | 66.93         | 1.21     | 0.0  | 0.0       | 68.14 | 0.0  | 0.0        | 0.0  | 68.14       |
      | 5  | 30   | 12 May 2024      |           | 0.0             | 67.53         | 0.59     | 0.0  | 0.0       | 68.12 | 0.0  | 0.0        | 0.0  | 68.12       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 331.77        | 8.91     | 0.0  | 0.0       | 340.68 | 0.0  | 0.0        | 0.0  | 340.68      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 12 December 2023 | Disbursement     | 331.77 | 0.0       | 0.0      | 0.0  | 0.0       | 331.77       | false    | false    |

  @TestRailId:C3435
  Scenario: Verify partial interest calculated on loan with disbursement date '23 July 2024' and 15000 amount - UC3
    When Admin sets the business date to "23 July 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 23 July 2024      | 15000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 July 2024" with "15000" amount and expected disbursement date on "23 July 2024"
    And Admin successfully disburse the loan on "23 July 2024" with "15000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 23 July 2024      |           | 15000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 23 August 2024    |           | 11307.31        | 3692.69       | 152.46   | 0.0  | 0.0       | 3845.15 | 0.0  | 0.0        | 0.0  | 3845.15     |
      | 2  | 31   | 23 September 2024 |           | 7577.09         | 3730.22       | 114.93   | 0.0  | 0.0       | 3845.15 | 0.0  | 0.0        | 0.0  | 3845.15     |
      | 3  | 30   | 23 October 2024   |           | 3806.47         | 3770.62       | 74.53    | 0.0  | 0.0       | 3845.15 | 0.0  | 0.0        | 0.0  | 3845.15     |
      | 4  | 31   | 23 November 2024  |           | 0.0             | 3806.47       | 38.69    | 0.0  | 0.0       | 3845.16 | 0.0  | 0.0        | 0.0  | 3845.16     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 15000.0       | 380.61   | 0.0  | 0.0       | 15380.61 | 0.0  | 0.0        | 0.0  | 15380.61    |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 23 July 2024     | Disbursement     | 15000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 15000.0      | false    | false    |

  @TestRailId:C3436
  Scenario: Verify interest calculated on loan that disbursed on 31 date with disbursement date '31 October 2023' and 2450 amount - UC4
    When Admin sets the business date to "31 October 2023"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 October 2023  |           | 2450.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 30 November 2023 |           | 2049.84         | 400.16        | 20.12    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 2  | 31   | 31 December 2023 |           | 1646.95         | 402.89        | 17.39    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 3  | 31   | 31 January 2024  |           | 1240.61         | 406.34        | 13.94    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 4  | 29   | 29 February 2024 |           | 830.15          | 410.46        | 9.82     | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 5  | 31   | 31 March 2024    |           | 416.89          | 413.26        | 7.02     | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 6  | 30   | 30 April 2024    |           | 0.0             | 416.89        | 3.41     | 0.0  | 0.0       | 420.3  | 0.0  | 0.0        | 0.0  | 420.3       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2450.0        | 71.7     | 0.0  | 0.0       | 2521.7 | 0.0  | 0.0        | 0.0  | 2521.7      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 2450.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2450.0       | false    | false    |

  @TestRailId:C3437
  Scenario: Verify interest calculated on loan that disbursed on 31 date with backdated disbursement date '31 October 2023' and 2450 amount - UC5
    When Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "2450" amount and expected disbursement date on "31 October 2023"
    And Admin successfully disburse the loan on "31 October 2023" with "2450" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 October 2023  |           | 2450.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 30 November 2023 |           | 2049.84         | 400.16        | 20.12    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 2  | 31   | 31 December 2023 |           | 1650.35         | 399.49        | 20.79    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 3  | 31   | 31 January 2024  |           | 1250.8          | 399.55        | 20.73    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 4  | 29   | 29 February 2024 |           | 849.91          | 400.89        | 19.39    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 5  | 31   | 31 March 2024    |           | 450.36          | 399.55        | 20.73    | 0.0  | 0.0       | 420.28 | 0.0  | 0.0        | 0.0  | 420.28      |
      | 6  | 30   | 30 April 2024    |           | 0.0             | 450.36        | 20.06    | 0.0  | 0.0       | 470.42 | 0.0  | 0.0        | 0.0  | 470.42      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2450.0        | 121.82   | 0.0  | 0.0       | 2571.82 | 0.0  | 0.0        | 0.0  | 2571.82     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 2450.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2450.0       | false    | false    |

  @TestRailId:C3438
  Scenario: Verify interest calculated on loan that disbursed on 31 date with disbursement date '31 October 2023' and 245000 amount - UC6
    When Admin sets the business date to "31 October 2023"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 245000         | 45                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "245000" amount and expected disbursement date on "31 October 2023"
    And Admin successfully disburse the loan on "31 October 2023" with "245000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 31 October 2023  |           | 245000.0        |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 30   | 30 November 2023 |           | 207718.37       | 37281.63      | 9061.64  | 0.0  | 0.0       | 46343.27 | 0.0  | 0.0        | 0.0  | 46343.27    |
      | 2  | 31   | 31 December 2023 |           | 169313.93       | 38404.44      | 7938.83  | 0.0  | 0.0       | 46343.27 | 0.0  | 0.0        | 0.0  | 46343.27    |
      | 3  | 31   | 31 January 2024  |           | 129424.02       | 39889.91      | 6453.36  | 0.0  | 0.0       | 46343.27 | 0.0  | 0.0        | 0.0  | 46343.27    |
      | 4  | 29   | 29 February 2024 |           | 87695.46        | 41728.56      | 4614.71  | 0.0  | 0.0       | 46343.27 | 0.0  | 0.0        | 0.0  | 46343.27    |
      | 5  | 31   | 31 March 2024    |           | 44694.68        | 43000.78      | 3342.49  | 0.0  | 0.0       | 46343.27 | 0.0  | 0.0        | 0.0  | 46343.27    |
      | 6  | 30   | 30 April 2024    |           | 0.0             | 44694.68      | 1648.57  | 0.0  | 0.0       | 46343.25 | 0.0  | 0.0        | 0.0  | 46343.25    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 245000.0      | 33059.6  | 0.0  | 0.0       | 278059.6 | 0.0  | 0.0        | 0.0  | 278059.6    |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount   | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 245000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 245000.0     | false    | false    |

  @TestRailId:C3439
  Scenario: Verify interest calculated on loan that disbursed on 31 date with backdated disbursement date '31 October 2023' and 5000 amount - UC7
    When Admin sets the business date to "21 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 5000           | 33.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "5000" amount and expected disbursement date on "31 October 2023"
    And Admin successfully disburse the loan on "31 October 2023" with "5000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 October 2023  |           | 5000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 30   | 30 November 2023 |           | 4222.02         | 777.98        | 139.68   | 0.0  | 0.0       | 917.66  | 0.0  | 0.0        | 0.0  | 917.66      |
      | 2  | 31   | 31 December 2023 |           | 3448.7          | 773.32        | 144.34   | 0.0  | 0.0       | 917.66  | 0.0  | 0.0        | 0.0  | 917.66      |
      | 3  | 31   | 31 January 2024  |           | 2674.99         | 773.71        | 143.95   | 0.0  | 0.0       | 917.66  | 0.0  | 0.0        | 0.0  | 917.66      |
      | 4  | 29   | 29 February 2024 |           | 1891.99         | 783.0         | 134.66   | 0.0  | 0.0       | 917.66  | 0.0  | 0.0        | 0.0  | 917.66      |
      | 5  | 31   | 31 March 2024    |           | 1118.28         | 773.71        | 143.95   | 0.0  | 0.0       | 917.66  | 0.0  | 0.0        | 0.0  | 917.66      |
      | 6  | 30   | 30 April 2024    |           | 0.0             | 1118.28       | 139.3    | 0.0  | 0.0       | 1257.58 | 0.0  | 0.0        | 0.0  | 1257.58     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000.0        | 845.88   | 0.0  | 0.0       | 5845.88 | 0.0  | 0.0        | 0.0  | 5845.88     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 October 2023  | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |

  @TestRailId:C3440
  Scenario: Verify interest calculated on loan that disbursed on 30 date with disbursement date '30 October 2021' and 1500 amount - UC8
    When Admin sets the business date to "30 October 2021"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 30 October 2021   | 1500           | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "30 October 2021" with "1500" amount and expected disbursement date on "30 October 2021"
    And Admin successfully disburse the loan on "30 October 2021" with "1500" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 30 October 2021  |           | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 30 November 2021 |           | 1255.13         | 244.87        | 12.08    | 0.0  | 0.0       | 256.95 | 0.0  | 0.0        | 0.0  | 256.95      |
      | 2  | 30   | 30 December 2021 |           | 1007.96         | 247.17        | 9.78     | 0.0  | 0.0       | 256.95 | 0.0  | 0.0        | 0.0  | 256.95      |
      | 3  | 31   | 30 January 2022  |           | 759.13          | 248.83        | 8.12     | 0.0  | 0.0       | 256.95 | 0.0  | 0.0        | 0.0  | 256.95      |
      | 4  | 29   | 28 February 2022 |           | 507.9           | 251.23        | 5.72     | 0.0  | 0.0       | 256.95 | 0.0  | 0.0        | 0.0  | 256.95      |
      | 5  | 30   | 30 March 2022    |           | 254.91          | 252.99        | 3.96     | 0.0  | 0.0       | 256.95 | 0.0  | 0.0        | 0.0  | 256.95      |
      | 6  | 31   | 30 April 2022    |           | 0.0             | 254.91        | 2.05     | 0.0  | 0.0       | 256.96 | 0.0  | 0.0        | 0.0  | 256.96      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 41.71    | 0.0  | 0.0       | 1541.71 | 0.0  | 0.0        | 0.0  | 1541.71     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 30 October 2021  | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |

  @TestRailId:C3441
  Scenario: Verify interest calculated on loan that disbursed on 29 date with disbursement date '29 October 2022' and 5000 amount - UC9
    When Admin sets the business date to "29 October 2022"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 29 October 2022   | 5000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 2              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "29 October 2022" with "5000" amount and expected disbursement date on "29 October 2022"
    And Admin successfully disburse the loan on "29 October 2022" with "5000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 29 October 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 61   | 29 December 2022 |           | 4190.81         | 809.19        | 58.49    | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 2  | 61   | 28 February 2023 |           | 3372.16         | 818.65        | 49.03    | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 3  | 60   | 29 April 2023    |           | 2543.28         | 828.88        | 38.8     | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 4  | 61   | 29 June 2023     |           | 1705.35         | 837.93        | 29.75    | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 5  | 61   | 29 August 2023   |           | 857.62          | 847.73        | 19.95    | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 6  | 61   | 29 October 2023  |           | 0.0             | 857.62        | 10.03    | 0.0  | 0.0       | 867.65 | 0.0  | 0.0        | 0.0  | 867.65      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000.0        | 206.05   | 0.0  | 0.0       | 5206.05 | 0.0  | 0.0        | 0.0  | 5206.05     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 29 October 2022  | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |

  @TestRailId:C3455
  Scenario: Verify interest calculated on backdated loan with zero interest rate  - UC1
    When Admin sets the business date to "14 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 13 January 2025   | 900            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "13 January 2025" with "900" amount and expected disbursement date on "13 January 2025"
    And Admin successfully disburse the loan on "13 January 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |           | 900.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 13 February 2025 |           | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 2  | 28   | 13 March 2025    |           | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 13 April 2025    |           | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 0.0  | 0.0        | 0.0  | 900.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |

  @TestRailId:C3456
  Scenario: Verify interest calculated on backdated loan with zero interest rate and run COB - UC2
    When Admin sets the business date to "14 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 13 January 2025   | 900            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "13 January 2025" with "900" amount and expected disbursement date on "13 January 2025"
    And Admin successfully disburse the loan on "13 January 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |           | 900.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 13 February 2025 |           | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 2  | 28   | 13 March 2025    |           | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 13 April 2025    |           | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 0.0  | 0.0        | 0.0  | 900.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "15 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |           | 900.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 13 February 2025 |           | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 2  | 28   | 13 March 2025    |           | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 13 April 2025    |           | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 0.0  | 0.0        | 0.0  | 900.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |

  @TestRailId:C3457
  Scenario: Verify interest calculated on backdated loan with zero interest rate and repayment - UC3
    When Admin sets the business date to "14 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 13 January 2025   | 900            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "13 January 2025" with "900" amount and expected disbursement date on "13 January 2025"
    And Admin successfully disburse the loan on "13 January 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |           | 900.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 13 February 2025 |           | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 2  | 28   | 13 March 2025    |           | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 13 April 2025    |           | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 0.0  | 0.0        | 0.0  | 900.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
    And Customer makes "AUTOPAY" repayment on "20 January 2025" with 300 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |                 | 900.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 13 February 2025 | 20 January 2025 | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 300.0 | 300.0      | 0.0  | 0.0         |
      | 2  | 28   | 13 March 2025    |                 | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 13 April 2025    |                 | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 300.0 | 300.0      | 0.0  | 600.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 20 January 2025  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |

  @TestRailId:C3458
  Scenario: Verify interest calculated on backdated loan with zero interest rate and repayment reversal - UC4
    When Admin sets the business date to "14 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 13 January 2025   | 900            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "13 January 2025" with "900" amount and expected disbursement date on "13 January 2025"
    And Admin successfully disburse the loan on "13 January 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |           | 900.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 13 February 2025 |           | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 2  | 28   | 13 March 2025    |           | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 13 April 2025    |           | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 0.0  | 0.0        | 0.0  | 900.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
    And Customer makes "AUTOPAY" repayment on "14 February 2025" with 300 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 13 January 2025  |                  | 900.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 13 February 2025 | 14 February 2025 | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 300.0 | 0.0        | 300.0 | 0.0         |
      | 2  | 28   | 13 March 2025    |                  | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0   | 0.0        | 0.0   | 300.0       |
      | 3  | 31   | 13 April 2025    |                  | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0   | 0.0        | 0.0   | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 300.0 | 0.0        | 300.0 | 600.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 14 February 2025 | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
    When Customer undo "1"th "Repayment" transaction made on "14 February 2025"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |           | 900.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 13 February 2025 |           | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 2  | 28   | 13 March 2025    |           | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 13 April 2025    |           | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 0.0  | 0.0        | 0.0  | 900.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 14 February 2025 | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 600.0        | true     | false    |

  @TestRailId:C3459
  Scenario: Verify interest calculated on backdated loan with zero interest rate and pay-off - UC5
    When Admin sets the business date to "14 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 13 January 2025   | 900            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "13 January 2025" with "900" amount and expected disbursement date on "13 January 2025"
    And Admin successfully disburse the loan on "13 January 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |           | 900.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 13 February 2025 |           | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 2  | 28   | 13 March 2025    |           | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 13 April 2025    |           | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 0.0  | 0.0        | 0.0  | 900.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
    When Loan Pay-off is made on "10 February 2025"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 January 2025  |                  | 900.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 13 February 2025 | 10 February 2025 | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 300.0 | 300.0      | 0.0  | 0.0         |
      | 2  | 28   | 13 March 2025    | 10 February 2025 | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 300.0 | 300.0      | 0.0  | 0.0         |
      | 3  | 31   | 13 April 2025    | 10 February 2025 | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 300.0 | 300.0      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 900.0 | 900.0      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 January 2025  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 10 February 2025 | Repayment        | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan closedon_date is "10 February 2025"

  @TestRailId:C3538
  Scenario: Verify leap year calculation with Feb month split between periods - UC1
    When Admin sets the business date to "12 December 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_LEAP_YEAR_INTEREST_RECALCULATION_DAILY | 12 December 2023  | 10000          | 9.482                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "12 December 2023" with "10000" amount and expected disbursement date on "12 December 2023"
    When Admin successfully disburse the loan on "12 December 2023" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 12 December 2023 |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 12 January 2024  |           | 8367.32         | 1632.68       | 80.53    | 0.0  | 0.0       | 1713.21 | 0.0  | 0.0        | 0.0  | 1713.21     |
      | 2  | 31   | 12 February 2024 |           | 6721.49         | 1645.83       | 67.38    | 0.0  | 0.0       | 1713.21 | 0.0  | 0.0        | 0.0  | 1713.21     |
      | 3  | 29   | 12 March 2024    |           | 5058.78         | 1662.71       | 50.5     | 0.0  | 0.0       | 1713.21 | 0.0  | 0.0        | 0.0  | 1713.21     |
      | 4  | 31   | 12 April 2024    |           | 3386.31         | 1672.47       | 40.74    | 0.0  | 0.0       | 1713.21 | 0.0  | 0.0        | 0.0  | 1713.21     |
      | 5  | 30   | 12 May 2024      |           | 1699.49         | 1686.82       | 26.39    | 0.0  | 0.0       | 1713.21 | 0.0  | 0.0        | 0.0  | 1713.21     |
      | 6  | 31   | 12 June 2024     |           | 0.0             | 1699.49       | 13.69    | 0.0  | 0.0       | 1713.18 | 0.0  | 0.0        | 0.0  | 1713.18     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 279.23   | 0.0  | 0.0       | 10279.23 | 0.0  | 0.0        | 0.0  | 10279.23    |

  @TestRailId:C3539
  Scenario: Verify leap year calculation with no February month but leap year - UC2
    When Admin sets the business date to "23 July 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_LEAP_YEAR_INTEREST_RECALCULATION_DAILY | 23 July 2024      | 15000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 July 2024" with "15000" amount and expected disbursement date on "23 July 2024"
    When Admin successfully disburse the loan on "23 July 2024" with "15000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 23 July 2024     |           | 15000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 23 August 2024   |           | 11307.47        | 3692.53       | 152.88   | 0.0  | 0.0       | 3845.41 | 0.0  | 0.0        | 0.0  | 3845.41     |
      | 2  | 31   | 23 September 2024|           | 7577.3          | 3730.17       | 115.24   | 0.0  | 0.0       | 3845.41 | 0.0  | 0.0        | 0.0  | 3845.41     |
      | 3  | 30   | 23 October 2024  |           | 3806.63         | 3770.67       | 74.74    | 0.0  | 0.0       | 3845.41 | 0.0  | 0.0        | 0.0  | 3845.41     |
      | 4  | 31   | 23 November 2024 |           | 0.0             | 3806.63       | 38.8     | 0.0  | 0.0       | 3845.43 | 0.0  | 0.0        | 0.0  | 3845.43     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 15000.0       | 381.66   | 0.0  | 0.0       | 15381.66 | 0.0  | 0.0        | 0.0  | 15381.66    |

  @TestRailId:C3540
  Scenario: Verify leap year calculation with February in one period - UC3
    When Admin sets the business date to "31 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_LEAP_YEAR_INTEREST_RECALCULATION_DAILY | 31 October 2023   | 245000         | 45                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2023" with "245000" amount and expected disbursement date on "31 October 2023"
    When Admin successfully disburse the loan on "31 October 2023" with "245000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 31 October 2023  |           | 245000.0        |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 30   | 30 November 2023 |           | 207713.25       | 37286.75      | 9061.64  | 0.0  | 0.0       | 46348.39 | 0.0  | 0.0        | 0.0  | 46348.39    |
      | 2  | 31   | 31 December 2023 |           | 169303.49       | 38409.76      | 7938.63  | 0.0  | 0.0       | 46348.39 | 0.0  | 0.0        | 0.0  | 46348.39    |
      | 3  | 31   | 31 January 2024  |           | 129425.74       | 39877.75      | 6470.64  | 0.0  | 0.0       | 46348.39 | 0.0  | 0.0        | 0.0  | 46348.39    |
      | 4  | 29   | 29 February 2024 |           | 87692.12        | 41733.62      | 4614.77  | 0.0  | 0.0       | 46348.39 | 0.0  | 0.0        | 0.0  | 46348.39    |
      | 5  | 31   | 31 March 2024    |           | 44695.25        | 42996.87      | 3351.52  | 0.0  | 0.0       | 46348.39 | 0.0  | 0.0        | 0.0  | 46348.39    |
      | 6  | 30   | 30 April 2024    |           | 0.0             | 44695.25      | 1653.11  | 0.0  | 0.0       | 46348.36 | 0.0  | 0.0        | 0.0  | 46348.36    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due       | Paid | In advance | Late | Outstanding |
      | 245000.0      | 33090.31 | 0.0  | 0.0       | 278090.31 | 0.0  | 0.0        | 0.0  | 278090.31   |

  @TestRailId:C3541
  Scenario: Verify leap year calculation with no February month - leap and non-leap year split - UC4
    When Admin sets the business date to "31 October 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_LEAP_YEAR_INTEREST_RECALCULATION_DAILY | 31 October 2024   | 2450           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 October 2024" with "2450" amount and expected disbursement date on "31 October 2024"
    When Admin successfully disburse the loan on "31 October 2024" with "2450" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 October 2024  |           | 2450.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 30 November 2024 |           | 2049.88         | 400.12        | 20.12    | 0.0  | 0.0       | 420.24 | 0.0  | 0.0        | 0.0  | 420.24      |
      | 2  | 31   | 31 December 2024 |           | 1647.03         | 402.85        | 17.39    | 0.0  | 0.0       | 420.24 | 0.0  | 0.0        | 0.0  | 420.24      |
      | 3  | 31   | 31 January 2025  |           | 1240.76         | 406.27        | 13.97    | 0.0  | 0.0       | 420.24 | 0.0  | 0.0        | 0.0  | 420.24      |
      | 4  | 28   | 28 February 2025 |           | 830.03          | 410.73        | 9.51     | 0.0  | 0.0       | 420.24 | 0.0  | 0.0        | 0.0  | 420.24      |
      | 5  | 31   | 31 March 2025    |           | 416.83          | 413.2         | 7.04     | 0.0  | 0.0       | 420.24 | 0.0  | 0.0        | 0.0  | 420.24      |
      | 6  | 30   | 30 April 2025    |           | 0.0             | 416.83        | 3.42     | 0.0  | 0.0       | 420.25 | 0.0  | 0.0        | 0.0  | 420.25      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2450.0        | 71.45    | 0.0  | 0.0       | 2521.45 | 0.0  | 0.0        | 0.0  | 2521.45     |

  @TestRailId:C3542
  Scenario: Verify leap year calculation with no leap year - UC5
    When Admin sets the business date to "29 October 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 29 October 2022   | 5000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 2              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "29 October 2022" with "5000" amount and expected disbursement date on "29 October 2022"
    When Admin successfully disburse the loan on "29 October 2022" with "5000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 29 October 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 61   | 29 December 2022 |           | 4190.81         | 809.19        | 58.49    | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 2  | 61   | 28 February 2023 |           | 3372.16         | 818.65        | 49.03    | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 3  | 60   | 29 April 2023    |           | 2543.28         | 828.88        | 38.8     | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 4  | 61   | 29 June 2023     |           | 1705.35         | 837.93        | 29.75    | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 5  | 61   | 29 August 2023   |           | 857.62          | 847.73        | 19.95    | 0.0  | 0.0       | 867.68 | 0.0  | 0.0        | 0.0  | 867.68      |
      | 6  | 61   | 29 October 2023  |           | 0.0             | 857.62        | 10.03    | 0.0  | 0.0       | 867.65 | 0.0  | 0.0        | 0.0  | 867.65      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000.0        | 206.05   | 0.0  | 0.0       | 5206.05 | 0.0  | 0.0        | 0.0  | 5206.05     |

  @TestRailId:C3622
  Scenario: Verify that RecalculationRestFrequencyType SameAsRepaymentPeriod work as intended in case of minimal amount (0.05 cent) of payments
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_SARP_TILL_PRECLOSE | 01 January 2025   | 8000           | 86.42                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "8000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "8000" EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 8000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 6887.3          | 1112.7        | 576.13   | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 2  | 28   | 01 March 2025    |           | 5694.47         | 1192.83       | 496.0    | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 3  | 31   | 01 April 2025    |           | 4415.74         | 1278.73       | 410.1    | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 4  | 30   | 01 May 2025      |           | 3044.92         | 1370.82       | 318.01   | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 5  | 31   | 01 June 2025     |           | 1575.37         | 1469.55       | 219.28   | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 1575.37       | 113.45   | 0.0  | 0.0       | 1688.82 | 0.0  | 0.0        | 0.0  | 1688.82     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 8000.0        | 2132.97  | 0.0  | 0.0       | 10132.97 | 0.0  | 0.0        | 0.0  | 10132.97    |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 8000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 8000.0       | false    | false    |
    When Admin sets the business date to "01 February 2025"
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 0.01 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "01 March 2025"
    And Customer makes "AUTOPAY" repayment on "01 March 2025" with 0.01 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "01 April 2025"
    And Customer makes "AUTOPAY" repayment on "01 April 2025" with 0.01 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "01 May 2025"
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 0.01 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "01 June 2025"
    And Customer makes "AUTOPAY" repayment on "01 June 2025" with 0.01 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "02 July 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 8000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 6887.3          | 1112.7        | 576.13   | 0.0  | 0.0       | 1688.83 | 0.05 | 0.0        | 0.04 | 1688.78     |
      | 2  | 28   | 01 March 2025    |           | 5774.6          | 1112.7        | 576.13   | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 3  | 31   | 01 April 2025    |           | 4661.9          | 1112.7        | 576.13   | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 4  | 30   | 01 May 2025      |           | 3549.2          | 1112.7        | 576.13   | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 5  | 31   | 01 June 2025     |           | 2436.5          | 1112.7        | 576.13   | 0.0  | 0.0       | 1688.83 | 0.0  | 0.0        | 0.0  | 1688.83     |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 2436.5        | 576.13   | 0.0  | 0.0       | 3012.63 | 0.0  | 0.0        | 0.0  | 3012.63     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 8000.0        | 3456.78  | 0.0  | 0.0       | 11456.78 | 0.05 | 0.0        | 0.04 | 11456.73    |

  @TestRailId:C3657
  Scenario: Verify tranche interest bearing progressive loan that expects two tranches with repayment and undo last disbursement - UC1
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 600.0                      | 01 February 2025               | 200.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 600.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 501.45          | 98.55         | 3.5      | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      |    |      | 01 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 2  | 28   | 01 March 2025    |           | 562.79          | 138.66        | 4.09     | 0.0  | 0.0       | 142.75 | 0.0  | 0.0        | 0.0  | 142.75      |
      | 3  | 31   | 01 April 2025    |           | 423.32          | 139.47        | 3.28     | 0.0  | 0.0       | 142.75 | 0.0  | 0.0        | 0.0  | 142.75      |
      | 4  | 30   | 01 May 2025      |           | 283.04          | 140.28        | 2.47     | 0.0  | 0.0       | 142.75 | 0.0  | 0.0        | 0.0  | 142.75      |
      | 5  | 31   | 01 June 2025     |           | 141.94          | 141.1         | 1.65     | 0.0  | 0.0       | 142.75 | 0.0  | 0.0        | 0.0  | 142.75      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 141.94        | 0.83     | 0.0  | 0.0       | 142.77 | 0.0  | 0.0        | 0.0  | 142.77      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 800.0         | 15.82    | 0.0  | 0.0       | 815.82  | 0.0  | 0.0        | 0.0  | 815.82      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 600.0       |                      |
      | 01 February 2025         |                 | 200.0       |                      |
#    --- 1st disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "700" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
      | 01 February 2025         |                 | 200.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      |    |      | 01 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 2  | 28   | 01 March 2025    |           | 669.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 553.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 436.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 318.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 200.0           | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36     | 0.0  | 0.0      | 714.36 | 0.0  | 0.0        | 0.0  | 714.36      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
#    --- 1st repayment - 15 January, 2025  ---
    When Admin sets the business date to "15 January 2025"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 January 2025" with 119.06 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025  | 582.78          | 117.22        | 1.84     | 0.0  | 0.0       | 119.06 | 119.06 | 119.06     | 0.0  | 0.0         |
      |    |      | 01 February 2025 |                  | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 668.99          | 113.79        | 5.27     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |                  | 552.67          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |                  | 435.67          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |                  | 317.98          | 117.69        | 1.37     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |                  | 200.0           | 117.98        | 0.69     | 0.0  | 0.0       | 118.67 | 0.0    | 0.0        | 0.0  | 118.67     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 13.97    | 0.0  | 0.0       | 713.97 | 119.06 | 119.06     | 0.0  | 594.91      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 14 January 2025  | Accrual           | 1.71   | 0.0       | 1.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Repayment         | 119.06 | 117.22    | 1.84     | 0.0  | 0.0       | 582.78       | false    | false    |
#    --- 2nd disbursement - 1 February, 2025  ---
    When Admin sets the business date to "01 February 2025"
    When Admin runs inline COB job for Loan
    When Admin successfully disburse the loan on "01 February 2025" with "300" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 700.0       |                      |
      | 01 February 2025         | 01 February 2025 | 300.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025  | 582.78          | 117.22        | 1.84     | 0.0  | 0.0       | 119.06 | 119.06 | 119.06     | 0.0  | 0.0         |
      |    |      | 01 February 2025 |                  | 300.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                  | 709.69          | 173.09        | 7.02     | 0.0  | 0.0       | 180.11 | 0.0    | 0.0        | 0.0  | 180.11      |
      | 3  | 31   | 01 April 2025    |                  | 533.72          | 175.97        | 4.14     | 0.0  | 0.0       | 180.11 | 0.0    | 0.0        | 0.0  | 180.11      |
      | 4  | 30   | 01 May 2025      |                  | 356.72          | 177.0         | 3.11     | 0.0  | 0.0       | 180.11 | 0.0    | 0.0        | 0.0  | 180.11      |
      | 5  | 31   | 01 June 2025     |                  | 178.69          | 178.03        | 2.08     | 0.0  | 0.0       | 180.11 | 0.0    | 0.0        | 0.0  | 180.11      |
      | 6  | 30   | 01 July 2025     |                  | 0.0             | 178.69        | 1.04     | 0.0  | 0.0       | 179.73 | 0.0    | 0.0        | 0.0  | 179.73      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 19.23    | 0.0  | 0.0       | 1019.23 | 119.06 | 119.06     | 0.0  | 900.17    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 14 January 2025  | Accrual           | 1.71   | 0.0       | 1.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Repayment         | 119.06 | 117.22    | 1.84     | 0.0  | 0.0       | 582.78       | false    | false    |
      | 15 January 2025  | Accrual           | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Disbursement      | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 882.78       | false    | false    |
    Then Admin fails to disburse the loan on "01 February 2025" with "100" amount due to exceed approved amount
#    --- undo last disbursement --- #
    When Admin successfully undo last disbursal
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 700.0       |                      |
    When Admin sets the business date to "02 February 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025  | 582.78          | 117.22        | 1.84     | 0.0  | 0.0       | 119.06 | 119.06 | 119.06     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 468.99          | 113.79        | 5.27     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |                  | 352.67          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |                  | 235.67          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |                  | 117.98          | 117.69        | 1.37     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |                  | 0.0             | 117.98        | 0.69     | 0.0  | 0.0       | 118.67 | 0.0    | 0.0        | 0.0  | 118.67     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 13.97    | 0.0  | 0.0       | 713.97 | 119.06 | 119.06     | 0.0  | 594.91    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 14 January 2025  | Accrual           | 1.71   | 0.0       | 1.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Repayment         | 119.06 | 117.22    | 1.84     | 0.0  | 0.0       | 582.78       | false    | false    |
      | 15 January 2025  | Accrual           | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Admin fails to disburse the loan on "02 February 2025" with "200" amount

  @TestRailId:C3658
  Scenario: Verify tranche interest bearing progressive loan that expects two tranches with two repayments and undo last disbursement - UC2
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 90                | DAYS                  | 15             | DAYS                   | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 700.0                     | 16 January 2025                | 300.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 15   | 16 January 2025  |           | 584.18          | 115.82        | 2.04     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86     |
      |    |      | 16 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 2  | 15   | 31 January 2025  |           | 708.37          | 175.81        | 2.58     | 0.0  | 0.0       | 178.39 | 0.0  | 0.0        | 0.0  | 178.39      |
      | 3  | 15   | 15 February 2025 |           | 532.05          | 176.32        | 2.07     | 0.0  | 0.0       | 178.39 | 0.0  | 0.0        | 0.0  | 178.39      |
      | 4  | 15   | 02 March 2025    |           | 355.21          | 176.84        | 1.55     | 0.0  | 0.0       | 178.39 | 0.0  | 0.0        | 0.0  | 178.39      |
      | 5  | 15   | 17 March 2025    |           | 177.86          | 177.35        | 1.04     | 0.0  | 0.0       | 178.39 | 0.0  | 0.0        | 0.0  | 178.39      |
      | 6  | 15   | 01 April 2025    |           | 0.0             | 177.86        | 0.52     | 0.0  | 0.0       | 178.38 | 0.0  | 0.0        | 0.0  | 178.38      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 9.8      | 0.0  | 0.0       | 1009.8  | 0.0  | 0.0        | 0.0  | 1009.8      |
#    --- 1st disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "700" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
      | 16 January 2025          |                 | 300.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 16 January 2025  |           | 584.18          | 115.82        | 2.04     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      |    |      | 16 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 2  | 15   | 31 January 2025  |           | 768.02          | 116.16        | 1.7      | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 3  | 15   | 15 February 2025 |           | 651.53          | 116.49        | 1.37     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 4  | 15   | 02 March 2025    |           | 534.7           | 116.83        | 1.03     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 5  | 15   | 17 March 2025    |           | 417.52          | 117.18        | 0.68     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 6  | 15   | 01 April 2025    |           | 300.0           | 117.52        | 0.34     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 7.16     | 0.0  | 0.0       | 707.16 | 0.0    | 0.0        | 0.0  | 707.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
#    --- 1st repayment - 16 January, 2025  ---
    When Admin sets the business date to "16 January 2025"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "16 January 2025" with 117.86 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 15   | 16 January 2025  | 16 January 2025 | 584.18          | 115.82        | 2.04     | 0.0  | 0.0       | 117.86 | 117.86 | 0.0        | 0.0  | 0.0         |
      |    |      | 16 January 2025  |                 | 300.0           |               |          | 0.0  |           | 0.0    |        |            |      | 0.0         |
      | 2  | 15   | 31 January 2025  |                 | 768.02          | 116.16        | 1.7      | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 3  | 15   | 15 February 2025 |                 | 651.53          | 116.49        | 1.37     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 4  | 15   | 02 March 2025    |                 | 534.7           | 116.83        | 1.03     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 5  | 15   | 17 March 2025    |                 | 417.52          | 117.18        | 0.68     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 6  | 15   | 01 April 2025    |                 | 300.0           | 117.52        | 0.34     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 7.16     | 0.0  | 0.0       | 707.16 | 117.86 | 0.0        | 0.0  | 589.3       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 15 January 2025  | Accrual           | 1.91   | 0.0       | 1.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Repayment         | 117.86 | 115.82    | 2.04     | 0.0  | 0.0       | 584.18       | false    | false    |
#    --- 2nd repayment - 31 January, 2025  ---
    When Admin sets the business date to "31 January 2025"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "31 January 2025" with 117.86 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 15   | 16 January 2025  | 16 January 2025 | 584.18          | 115.82        | 2.04     | 0.0  | 0.0       | 117.86 | 117.86 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 31 January 2025  | 31 January 2025 | 468.02          | 116.16        | 1.7      | 0.0  | 0.0       | 117.86 | 117.86 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 15 February 2025 |                 | 351.53          | 116.49        | 1.37     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 4  | 15   | 02 March 2025    |                 | 234.7           | 116.83        | 1.03     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 5  | 15   | 17 March 2025    |                 | 117.52          | 117.18        | 0.68     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 6  | 15   | 01 April 2025    |                 | 0.0             | 117.52        | 0.34     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 7.16     | 0.0  | 0.0       | 707.16 | 235.72 | 0.0        | 0.0  | 471.44      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 15 January 2025  | Accrual           | 1.91   | 0.0       | 1.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Repayment         | 117.86 | 115.82    | 2.04     | 0.0  | 0.0       | 584.18       | false    | false    |
      | 16 January 2025  | Accrual           | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Repayment         | 117.86 | 116.16    | 1.7      | 0.0  | 0.0       | 468.02       | false    | false    |
#    --- 2nd disbursement - 1 February, 2025  ---
    When Admin sets the business date to "01 February 2025"
    When Admin runs inline COB job for Loan
    When Admin successfully disburse the loan on "01 February 2025" with "300" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 700.0       |                      |
      | 16 January 2025          | 01 February 2025 | 300.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 15   | 16 January 2025  | 16 January 2025 | 584.18          | 115.82        | 2.04     | 0.0  | 0.0       | 117.86 | 117.86 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 31 January 2025  | 31 January 2025 | 468.02          | 116.16        | 1.7      | 0.0  | 0.0       | 117.86 | 117.86 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2025 |                 | 300.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 3  | 15   | 15 February 2025 |                 | 576.81          | 191.21        | 2.18     | 0.0  | 0.0       | 193.39 | 0.0    | 0.0        | 0.0  | 193.39      |
      | 4  | 15   | 02 March 2025    |                 | 385.1           | 191.71        | 1.68     | 0.0  | 0.0       | 193.39 | 0.0    | 0.0        | 0.0  | 193.39      |
      | 5  | 15   | 17 March 2025    |                 | 192.83          | 192.27        | 1.12     | 0.0  | 0.0       | 193.39 | 0.0    | 0.0        | 0.0  | 193.39      |
      | 6  | 15   | 01 April 2025    |                 | 0.0             | 192.83        | 0.56     | 0.0  | 0.0       | 193.39 | 0.0    | 0.0        | 0.0  | 193.39      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 9.28     | 0.0  | 0.0       | 1009.28 | 235.72 | 0.0        | 0.0  | 773.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 15 January 2025  | Accrual           | 1.91   | 0.0       | 1.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Repayment         | 117.86 | 115.82    | 2.04     | 0.0  | 0.0       | 584.18       | false    | false    |
      | 16 January 2025  | Accrual           | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Repayment         | 117.86 | 116.16    | 1.7      | 0.0  | 0.0       | 468.02       | false    | false    |
      | 31 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Disbursement      | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 768.02       | false    | false    |
    Then Admin fails to disburse the loan on "01 February 2025" with "100" amount due to exceed approved amount
#    --- undo disbursement --- #
    When Admin sets the business date to "02 February 2025"
    When Admin runs inline COB job for Loan
    When Admin successfully undo last disbursal
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 700.0       |                      |
    When Admin sets the business date to "02 February 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 15   | 16 January 2025  | 16 January 2025 | 584.18          | 115.82        | 2.04     | 0.0  | 0.0       | 117.86 | 117.86 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 31 January 2025  | 31 January 2025 | 468.02          | 116.16        | 1.7      | 0.0  | 0.0       | 117.86 | 117.86 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 15 February 2025 |                 | 351.53          | 116.49        | 1.37     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 4  | 15   | 02 March 2025    |                 | 234.7           | 116.83        | 1.03     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 5  | 15   | 17 March 2025    |                 | 117.52          | 117.18        | 0.68     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 6  | 15   | 01 April 2025    |                 | 0.0             | 117.52        | 0.34     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 7.16     | 0.0  | 0.0       | 707.16 | 235.72 | 0.0        | 0.0  | 471.44      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 15 January 2025  | Accrual           | 1.91   | 0.0       | 1.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Repayment         | 117.86 | 115.82    | 2.04     | 0.0  | 0.0       | 584.18       | false    | false    |
      | 16 January 2025  | Accrual           | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Repayment         | 117.86 | 116.16    | 1.7      | 0.0  | 0.0       | 468.02       | false    | false    |
      | 31 January 2025  | Accrual           | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual           | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Admin fails to disburse the loan on "02 February 2025" with "200" amount

  @TestRailId:C3659
  Scenario: Verify tranche interest bearing progressive loan that expects tranche with added 2 more tranches and undo last disbursement - UC3
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 700.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36    | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    When Admin successfully disburse the loan on "01 January 2025" with "600" EUR transaction amount
  #  When Admin runs inline COB job for Loan
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 600.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 501.45          | 98.55         | 3.5      | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 2  | 28   | 01 March 2025    |           | 402.33          | 99.12         | 2.93     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 3  | 31   | 01 April 2025    |           | 302.63          | 99.7          | 2.35     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 4  | 30   | 01 May 2025      |           | 202.35          | 100.28        | 1.77     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 5  | 31   | 01 June 2025     |           | 101.48          | 100.87        | 1.18     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 101.48        | 0.59     | 0.0  | 0.0       | 102.07 | 0.0  | 0.0        | 0.0  | 102.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 600.0         | 12.32    | 0.0  | 0.0       | 612.32 | 0.0  | 0.0        | 0.0  | 612.32      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
    Then Admin fails to disburse the loan on "01 January 2025" with "200" amount
#    --- add 2nd expected disbursement details with expected disbursement date - 8 Jan, 2025 --- #
    And Admin successfully add disbursement detail to the loan on "08 January 2025" with 300 EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 600.0       |                      |
      | 08 January 2025          |                 | 300.0       | 600.0                |
    When Admin sets the business date to "08 January 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 08 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 801.45          | 98.55         | 3.5      | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 2  | 28   | 01 March 2025    |           | 702.33          | 99.12         | 2.93     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 3  | 31   | 01 April 2025    |           | 602.63          | 99.7          | 2.35     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 4  | 30   | 01 May 2025      |           | 502.35          | 100.28        | 1.77     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 5  | 31   | 01 June 2025     |           | 401.48          | 100.87        | 1.18     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
      | 6  | 30   | 01 July 2025     |           | 300.0           | 101.48        | 0.59     | 0.0  | 0.0       | 102.07 | 0.0  | 0.0        | 0.0  | 102.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 600.0         | 12.32    | 0.0  | 0.0       | 612.32 | 0.0  | 0.0        | 0.0  | 612.32      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 07 January 2025  | Accrual          | 0.68   | 0.0       | 0.68     | 0.0  | 0.0       | 0.0          | false    | false    |
#    --- 2nd disbursement partial - 8 January, 2025  --- #
    When Admin successfully disburse the loan on "08 January 2025" with "300" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 08 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 751.84          | 148.16        | 4.85     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 2  | 28   | 01 March 2025    |           | 603.22          | 148.62        | 4.39     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 3  | 31   | 01 April 2025    |           | 453.73          | 149.49        | 3.52     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 4  | 30   | 01 May 2025      |           | 303.37          | 150.36        | 2.65     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 5  | 31   | 01 June 2025     |           | 152.13          | 151.24        | 1.77     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 152.13        | 0.89     | 0.0  | 0.0       | 153.02 | 0.0  | 0.0        | 0.0  | 153.02      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.07    | 0.0  | 0.0       | 918.07 | 0.0  | 0.0        | 0.0  | 918.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 07 January 2025  | Accrual          | 0.68   | 0.0       | 0.68     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Disbursement     | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
    Then Admin fails to disburse the loan on "08 January 2025" with "100" amount
#    --- add 3rd expected disbursement details with expected disbursement date - 15 Jan, 2025 --- #
    And Admin successfully add disbursement detail to the loan on "15 January 2025" with 100 EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 600.0       |                      |
      | 08 January 2025          | 08 January 2025 | 300.0       | 600.0                |
      | 15 January 2025          |                 | 100.0       | 300.0                |
    When Admin sets the business date to "15 January 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 08 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 851.84          | 148.16        | 4.85     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 2  | 28   | 01 March 2025    |           | 703.22          | 148.62        | 4.39     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 3  | 31   | 01 April 2025    |           | 553.73          | 149.49        | 3.52     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 4  | 30   | 01 May 2025      |           | 403.37          | 150.36        | 2.65     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 5  | 31   | 01 June 2025     |           | 252.13          | 151.24        | 1.77     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 6  | 30   | 01 July 2025     |           | 100.0           | 152.13        | 0.89     | 0.0  | 0.0       | 153.02 | 0.0  | 0.0        | 0.0  | 153.02      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.07    | 0.0  | 0.0       | 918.07 | 0.0  | 0.0        | 0.0  | 918.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 07 January 2025  | Accrual          | 0.68   | 0.0       | 0.68     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Disbursement     | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 08 January 2025  | Accrual          | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
#    --- 3rd disbursement partial - 15 Jan, 2025  --- #
    When Admin successfully disburse the loan on "15 January 2025" with "50" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 08 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           | 50.0            |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 793.52          | 156.48        | 5.01     | 0.0  | 0.0       | 161.49 | 0.0  | 0.0        | 0.0  | 161.49      |
      | 2  | 28   | 01 March 2025    |           | 636.66          | 156.86        | 4.63     | 0.0  | 0.0       | 161.49 | 0.0  | 0.0        | 0.0  | 161.49      |
      | 3  | 31   | 01 April 2025    |           | 478.88          | 157.78        | 3.71     | 0.0  | 0.0       | 161.49 | 0.0  | 0.0        | 0.0  | 161.49      |
      | 4  | 30   | 01 May 2025      |           | 320.18          | 158.7         | 2.79     | 0.0  | 0.0       | 161.49 | 0.0  | 0.0        | 0.0  | 161.49      |
      | 5  | 31   | 01 June 2025     |           | 160.56          | 159.62        | 1.87     | 0.0  | 0.0       | 161.49 | 0.0  | 0.0        | 0.0  | 161.49      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 160.56        | 0.94     | 0.0  | 0.0       | 161.5  | 0.0  | 0.0        | 0.0  | 161.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 950.0         | 18.95    | 0.0  | 0.0       | 968.95 | 0.0  | 0.0        | 0.0  | 968.95      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 07 January 2025  | Accrual          | 0.68   | 0.0       | 0.68     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Disbursement     | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 08 January 2025  | Accrual          | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Disbursement     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 950.0        | false    | false    |
#    --- undo last disbursement --- #
    When Admin sets the business date to "16 January 2025"
    When Admin runs inline COB job for Loan
    When Admin successfully undo last disbursal
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 600.0       |                      |
      | 08 January 2025          | 08 January 2025 | 300.0       | 600.0                |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 08 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 751.84          | 148.16        | 4.85     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 2  | 28   | 01 March 2025    |           | 603.22          | 148.62        | 4.39     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 3  | 31   | 01 April 2025    |           | 453.73          | 149.49        | 3.52     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 4  | 30   | 01 May 2025      |           | 303.37          | 150.36        | 2.65     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 5  | 31   | 01 June 2025     |           | 152.13          | 151.24        | 1.77     | 0.0  | 0.0       | 153.01 | 0.0  | 0.0        | 0.0  | 153.01      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 152.13        | 0.89     | 0.0  | 0.0       | 153.02 | 0.0  | 0.0        | 0.0  | 153.02      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.07    | 0.0  | 0.0       | 918.07 | 0.0  | 0.0        | 0.0  | 918.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 07 January 2025  | Accrual          | 0.68   | 0.0       | 0.68     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Disbursement     | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 08 January 2025  | Accrual          | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Admin fails to disburse the loan on "16 January 2025" with "100" amount

  @TestRailId:C3660
  Scenario: Verify tranche interest bearing progressive loan that expects tranche with repayment and undo disbursement - UC4
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 700.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36    | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    When Admin successfully disburse the loan on "01 January 2025" with "700" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36     | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
#    --- 1st repayment - 1 Feb, 2025  --- #
    When Admin sets the business date to "01 February 2025"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 119.06 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 119.06 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |                  | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |                  | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |                  | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |                  | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 14.36    | 0.0  | 0.0       | 714.36 | 119.06 | 0.0        | 0.0  | 595.3      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 31 January 2025  | Accrual           | 3.95   | 0.0       | 3.95     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Repayment         | 119.06 | 114.98    | 4.08     | 0.0  | 0.0       | 585.02       | false    | false    |
#    --- add 2nd expected disbursement details with expected disbursement date - 5 Feb, 2025 --- #
    And Admin successfully add disbursement detail to the loan on "05 February 2025" with 300 EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
      | 05 February 2025         |                 | 300.0       | 700.0                |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 119.06 | 0.0        | 0.0  | 0.0         |
      |    |      | 05 February 2025 |                  | 300.0           |               |          | 0.0  |           | 0.0    |        |            |      | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 769.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |                  | 653.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |                  | 536.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |                  | 418.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |                  | 300.0           | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0    | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 14.36    | 0.0  | 0.0       | 714.36 | 119.06 | 0.0        | 0.0  | 595.3      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 31 January 2025  | Accrual           | 3.95   | 0.0       | 3.95     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Repayment         | 119.06 | 114.98    | 4.08     | 0.0  | 0.0       | 585.02       | false    | false    |
#    --- 2nd disbursement - 5 February, 2025  ---
    When Admin sets the business date to "05 February 2025"
    When Admin runs inline COB job for Loan
    When Admin successfully disburse the loan on "05 February 2025" with "200" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 700.0       |                      |
      | 05 February 2025         | 05 February 2025 | 200.0       | 700.0                |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 119.06 | 0.0        | 0.0  | 0.0         |
      |    |      | 05 February 2025 |                  | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                  | 629.7           | 155.32        | 4.41     | 0.0  | 0.0       | 159.73 | 0.0    | 0.0        | 0.0  | 159.73      |
      | 3  | 31   | 01 April 2025    |                  | 473.64          | 156.06        | 3.67     | 0.0  | 0.0       | 159.73 | 0.0    | 0.0        | 0.0  | 159.73      |
      | 4  | 30   | 01 May 2025      |                  | 316.67          | 156.97        | 2.76     | 0.0  | 0.0       | 159.73 | 0.0    | 0.0        | 0.0  | 159.73      |
      | 5  | 31   | 01 June 2025     |                  | 158.79          | 157.88        | 1.85     | 0.0  | 0.0       | 159.73 | 0.0    | 0.0        | 0.0  | 159.73      |
      | 6  | 30   | 01 July 2025     |                  | 0.0             | 158.79        | 0.93     | 0.0  | 0.0       | 159.72 | 0.0    | 0.0        | 0.0  | 159.72      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 17.7     | 0.0  | 0.0       | 917.7  | 119.06 | 0.0        | 0.0  | 798.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 31 January 2025  | Accrual           | 3.95   | 0.0       | 3.95     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Repayment         | 119.06 | 114.98    | 4.08     | 0.0  | 0.0       | 585.02       | false    | false    |
      | 01 February 2025 | Accrual           | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual           | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual           | 0.13   | 0.0       | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Disbursement      | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 785.02       | false    | false    |
    Then Admin fails to disburse the loan on "05 February 2025" with "100" amount
  # -- undo disbursement ----
    When Admin sets the business date to "06 February 2025"
    When Admin runs inline COB job for Loan
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                  | 700.0       |                      |
      | 05 February 2025         |                  | 200.0       | 700.0                |
    When Admin sets the business date to "02 February 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      |    |      | 05 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 2  | 28   | 01 March 2025    |           | 629.7           | 155.32        | 4.41     | 0.0  | 0.0       | 159.73 | 0.0  | 0.0        | 0.0  | 159.73      |
      | 3  | 31   | 01 April 2025    |           | 473.64          | 156.06        | 3.67     | 0.0  | 0.0       | 159.73 | 0.0  | 0.0        | 0.0  | 159.73      |
      | 4  | 30   | 01 May 2025      |           | 316.67          | 156.97        | 2.76     | 0.0  | 0.0       | 159.73 | 0.0  | 0.0        | 0.0  | 159.73      |
      | 5  | 31   | 01 June 2025     |           | 158.79          | 157.88        | 1.85     | 0.0  | 0.0       | 159.73 | 0.0  | 0.0        | 0.0  | 159.73      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 158.79        | 0.93     | 0.0  | 0.0       | 159.72 | 0.0  | 0.0        | 0.0  | 159.72      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 17.7     | 0.0  | 0.0       | 917.7  | 0.0    | 0.0        | 0.0  | 917.7       |
    Then Loan Transactions tab has none transaction
    When Admin sets the business date to "02 March 2025"
    When Admin runs inline COB job for Loan
    When Admin successfully disburse the loan on "01 February 2025" with "750" EUR transaction amount
    When Admin successfully disburse the loan on "01 March 2025" with "200" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 February 2025 | 750.0       |                      |
      | 05 February 2025         | 01 March 2025    | 200.0       | 700.0                |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 February 2025 |             | 750.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 28   | 01 March 2025    |             | 626.81          | 123.19        | 4.37     | 0.0  | 0.0       | 127.56 | 0.0    | 0.0        | 0.0  | 127.56      |
      |    |      | 01 March 2025    |             | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 31   | 01 April 2025    |             | 663.39          | 163.42        | 4.85     | 0.0  | 0.0       | 168.27 | 0.0    | 0.0        | 0.0  | 168.27      |
      | 3  | 30   | 01 May 2025      |             | 498.99          | 164.4         | 3.87     | 0.0  | 0.0       | 168.27 | 0.0    | 0.0        | 0.0  | 168.27      |
      | 4  | 31   | 01 June 2025     |             | 333.63          | 165.36        | 2.91     | 0.0  | 0.0       | 168.27 | 0.0    | 0.0        | 0.0  | 168.27      |
      | 5  | 30   | 01 July 2025     |             | 167.31          | 166.32        | 1.95     | 0.0  | 0.0       | 168.27 | 0.0    | 0.0        | 0.0  | 168.27      |
      | 6  | 31   | 01 August 2025   |             | 0.0             | 167.31        | 0.98     | 0.0  | 0.0       | 168.29 | 0.0    | 0.0        | 0.0  | 168.29      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 950.0         | 18.93    | 0.0  | 0.0       | 968.93 | 0.0    | 0.0        | 0.0  | 968.93      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 February 2025 | Disbursement      | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 01 March 2025    | Disbursement      | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 950.0        | false    | false    |
    Then Admin fails to disburse the loan on "01 March 2025" with "50" amount

  @TestRailId:C3636
  Scenario: Verify that no negative amount interest refund created after multiple Merchant Issued Refund
    When Admin sets the business date to "05 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION_MULTIDISB | 05 April 2025     | 300            | 20.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 April 2025" with "300" amount and expected disbursement date on "05 April 2025"
    And Admin successfully disburse the loan on "05 April 2025" with "265.91" EUR transaction amount
    And Admin successfully disburse the loan on "05 April 2025" with "1.99" EUR transaction amount
    And Admin successfully disburse the loan on "05 April 2025" with "20.0" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2025     |           | 265.91          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 05 April 2025     |           | 1.99            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 05 April 2025     |           | 20.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 05 May 2025       |           | 241.9           | 46.0          | 4.97     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 2  | 31   | 05 June 2025      |           | 195.24          | 46.66         | 4.31     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 3  | 30   | 05 July 2025      |           | 147.64          | 47.6          | 3.37     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 4  | 31   | 05 August 2025    |           | 99.3            | 48.34         | 2.63     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 5  | 31   | 05 September 2025 |           | 50.1            | 49.2          | 1.77     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 6  | 30   | 05 October 2025   |           | 0.0             | 50.1          | 0.86     | 0.0  | 0.0       | 50.96 | 0.0  | 0.0        | 0.0  | 50.96       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 287.9         | 17.91    | 0.0  | 0.0       | 305.81 | 0.0  | 0.0        | 0.0  | 305.81      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 April 2025    | Disbursement     | 265.91 | 0.0       | 0.0      | 0.0  | 0.0       | 265.91       | false    | false    |
      | 05 April 2025    | Disbursement     | 1.99   | 0.0       | 0.0      | 0.0  | 0.0       | 267.9        | false    | false    |
      | 05 April 2025    | Disbursement     | 20.0   | 0.0       | 0.0      | 0.0  | 0.0       | 287.9        | false    | false    |
    When Admin sets the business date to "06 April 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "06 April 2025" with 6.29 EUR transaction amount and system-generated Idempotency key
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2025     |           | 265.91          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 05 April 2025     |           | 1.99            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 05 April 2025     |           | 20.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 05 May 2025       |           | 241.79          | 46.11         | 4.86     | 0.0  | 0.0       | 50.97 | 6.3  | 6.3        | 0.0  | 44.67       |
      | 2  | 31   | 05 June 2025      |           | 195.13          | 46.66         | 4.31     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 3  | 30   | 05 July 2025      |           | 147.53          | 47.6          | 3.37     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 4  | 31   | 05 August 2025    |           | 99.19           | 48.34         | 2.63     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 5  | 31   | 05 September 2025 |           | 49.99           | 49.2          | 1.77     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 6  | 30   | 05 October 2025   |           | 0.0             | 49.99         | 0.86     | 0.0  | 0.0       | 50.85 | 0.0  | 0.0        | 0.0  | 50.85       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 287.9         | 17.8     | 0.0  | 0.0       | 305.7 | 6.3  | 6.3        | 0.0  | 299.4       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 April 2025    | Disbursement           | 265.91 | 0.0       | 0.0      | 0.0  | 0.0       | 265.91       | false    | false    |
      | 05 April 2025    | Disbursement           | 1.99   | 0.0       | 0.0      | 0.0  | 0.0       | 267.9        | false    | false    |
      | 05 April 2025    | Disbursement           | 20.0   | 0.0       | 0.0      | 0.0  | 0.0       | 287.9        | false    | false    |
      | 06 April 2025    | Merchant Issued Refund | 6.29   | 6.29      | 0.0      | 0.0  | 0.0       | 281.61       | false    | false    |
      | 06 April 2025    | Interest Refund        | 0.01   | 0.01      | 0.0      | 0.0  | 0.0       | 281.6        | false    | false    |
    When Admin sets the business date to "07 April 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "07 April 2025" with 1.99 EUR transaction amount and system-generated Idempotency key
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2025     |           | 265.91          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 05 April 2025     |           | 1.99            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 05 April 2025     |           | 20.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 05 May 2025       |           | 241.76          | 46.14         | 4.83     | 0.0  | 0.0       | 50.97 | 8.29 | 8.29       | 0.0  | 42.68       |
      | 2  | 31   | 05 June 2025      |           | 195.1           | 46.66         | 4.31     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 3  | 30   | 05 July 2025      |           | 147.5           | 47.6          | 3.37     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 4  | 31   | 05 August 2025    |           | 99.16           | 48.34         | 2.63     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 5  | 31   | 05 September 2025 |           | 49.96           | 49.2          | 1.77     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 6  | 30   | 05 October 2025   |           | 0.0             | 49.96         | 0.86     | 0.0  | 0.0       | 50.82 | 0.0  | 0.0        | 0.0  | 50.82       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 287.9         | 17.77    | 0.0  | 0.0       | 305.67 | 8.29 | 8.29       | 0.0  | 297.38      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 April 2025    | Disbursement           | 265.91 | 0.0       | 0.0      | 0.0  | 0.0       | 265.91       | false    | false    |
      | 05 April 2025    | Disbursement           | 1.99   | 0.0       | 0.0      | 0.0  | 0.0       | 267.9        | false    | false    |
      | 05 April 2025    | Disbursement           | 20.0   | 0.0       | 0.0      | 0.0  | 0.0       | 287.9        | false    | false    |
      | 06 April 2025    | Merchant Issued Refund | 6.29   | 6.29      | 0.0      | 0.0  | 0.0       | 281.61       | false    | false    |
      | 06 April 2025    | Interest Refund        | 0.01   | 0.01      | 0.0      | 0.0  | 0.0       | 281.6        | false    | false    |
      | 06 April 2025    | Accrual                | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Merchant Issued Refund | 1.99   | 1.99      | 0.0      | 0.0  | 0.0       | 279.61       | false    | false    |
    When Admin sets the business date to "08 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2025     |           | 265.91          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 05 April 2025     |           | 1.99            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 05 April 2025     |           | 20.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 05 May 2025       |           | 241.76          | 46.14         | 4.83     | 0.0  | 0.0       | 50.97 | 8.29 | 8.29       | 0.0  | 42.68       |
      | 2  | 31   | 05 June 2025      |           | 195.1           | 46.66         | 4.31     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 3  | 30   | 05 July 2025      |           | 147.5           | 47.6          | 3.37     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 4  | 31   | 05 August 2025    |           | 99.16           | 48.34         | 2.63     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 5  | 31   | 05 September 2025 |           | 49.96           | 49.2          | 1.77     | 0.0  | 0.0       | 50.97 | 0.0  | 0.0        | 0.0  | 50.97       |
      | 6  | 30   | 05 October 2025   |           | 0.0             | 49.96         | 0.86     | 0.0  | 0.0       | 50.82 | 0.0  | 0.0        | 0.0  | 50.82       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 287.9         | 17.77    | 0.0  | 0.0       | 305.67 | 8.29 | 8.29       | 0.0  | 297.38      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 April 2025    | Disbursement           | 265.91 | 0.0       | 0.0      | 0.0  | 0.0       | 265.91       | false    | false    |
      | 05 April 2025    | Disbursement           | 1.99   | 0.0       | 0.0      | 0.0  | 0.0       | 267.9        | false    | false    |
      | 05 April 2025    | Disbursement           | 20.0   | 0.0       | 0.0      | 0.0  | 0.0       | 287.9        | false    | false    |
      | 06 April 2025    | Merchant Issued Refund | 6.29   | 6.29      | 0.0      | 0.0  | 0.0       | 281.61       | false    | false    |
      | 06 April 2025    | Interest Refund        | 0.01   | 0.01      | 0.0      | 0.0  | 0.0       | 281.6        | false    | false    |
      | 06 April 2025    | Accrual                | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Merchant Issued Refund | 1.99   | 1.99      | 0.0      | 0.0  | 0.0       | 279.61       | false    | false    |
      | 07 April 2025    | Accrual                | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3783
  Scenario: Verify that remaining repayment periods are correctly calculated when early repayment is made on till rest frequency type loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_REST_FREQUENCY_DATE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "7 January 2024"
    And Customer makes "AUTOPAY" repayment on "7 January 2024" with 17.01 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 07 January 2024 | 83.1            | 16.9          | 0.11     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 66.96           | 16.14         | 0.87     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                 | 50.34           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                 | 33.62           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.81           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.81         | 0.1      | 0.0  | 0.0       | 16.91 | 0.0   | 0.0        | 0.0  | 16.91       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 0.0  | 0.0       | 101.96 | 17.01 | 17.01      | 0.0  | 84.95       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Repayment        | 17.01  | 16.9      | 0.11     | 0.0  | 0.0       | 83.1         | false    | false    |

  @TestRailId:C3798
  Scenario: Verify prepayment on daily interest recalculation loan with preClosureInterestCalculationStrategy = till rest frequency date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_REST_FREQUENCY_DATE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "02 April 2024"
    And Customer makes "AUTOPAY" repayment on "12 March 2024" with 101.74 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 12 March 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    | 12 March 2024 | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 3  | 31   | 01 April 2024    | 12 March 2024 | 50.71           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0   | 0.0         |
      | 4  | 30   | 01 May 2024      | 12 March 2024 | 33.7            | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0   | 0.0         |
      | 5  | 31   | 01 June 2024     | 12 March 2024 | 16.69           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0   | 0.0         |
      | 6  | 30   | 01 July 2024     | 12 March 2024 | 0.0             | 16.69         | 0.0      | 0.0  | 0.0       | 16.69 | 16.69 | 16.69      | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 100.0         | 1.74     | 0.0  | 0.0       | 101.74 | 101.74 | 67.72      | 34.02 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 12 March 2024    | Repayment        | 101.74  | 100.0     | 1.74     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2024    | Accrual          | 1.74    | 0.0       | 1.74     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer undo "1"th "Repayment" transaction made on "12 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |       |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0   | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0   | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.71           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 34.01           | 16.7          | 0.31     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 17.2            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.2          | 0.1      | 0.0  | 0.0       | 17.3  | 0.0  | 0.0        | 0.0   | 17.3        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 100.0         | 2.35     | 0.0  | 0.0       | 102.35 | 0.0    | 0.0        | 0.0   | 102.35      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 12 March 2024    | Repayment        | 101.74  | 100.0     | 1.74     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 02 April 2024    | Accrual          | 1.74    | 0.0       | 1.74     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "22 March 2024" with 101.74 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount

  @TestRailId:C3830
  Scenario: Progressive loan - flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = true, actual/actual, second disbursement in the middle of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "15 July 2024"
    When Admin successfully disburse the loan on "15 July 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      |    |      | 15 July 2024      |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 31   | 01 August 2024    |           | 457.77          | 458.9         | 11.37    | 0.0  | 0.0       | 470.27 | 0.0  | 0.0        | 0.0  | 470.27      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 457.77        | 12.5     | 0.0  | 0.0       | 470.27 | 0.0  | 0.0        | 0.0  | 470.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250          | 33.87    | 0.0  | 0.0       | 1283.87 | 0.0  | 0.0        | 0.0  | 1283.87     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "15 July 2024" with 813.6 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 July 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      |    |      | 15 July 2024      |              | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 31   | 01 August 2024    | 15 July 2024 | 457.77          | 458.9         | 11.37    | 0.0  | 0.0       | 470.27 | 470.27 | 470.27     | 0.0    | 0.0         |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 457.77        | 12.5     | 0.0  | 0.0       | 470.27 | 0.0    | 0.0        | 0.0    | 470.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late   | Outstanding |
      | 1250          | 33.87    | 0.0  | 0.0       | 1283.87 | 813.6 | 470.27     | 343.33 | 470.27      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 15 July 2024     | Repayment               | 813.6  | 792.23    | 21.37    | 0.0  | 0.0       | 457.77       |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 792.23 |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 21.37  |
      | LIABILITY | 145023       | Suspense/Clearing account | 813.6 |        |
    When Customer makes a repayment undo on "15 July 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 July 2024     | Repayment               | 813.6  | 792.23    | 21.37    | 0.0  | 0.0       | 457.77       | true     | false    |

  @TestRailId:C3831
  Scenario: Progressive loan - flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = true, actual/actual, second disbursement on the due date of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 August 2024"
    When Admin successfully disburse the loan on "01 August 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      |    |      | 01 August 2024    |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 583.34        | 12.5     | 0.0  | 0.0       | 595.84 | 0.0  | 0.0        | 0.0  | 595.84      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250          | 32.5     | 0.0  | 0.0       | 1282.5  | 0.0  | 0.0        | 0.0  | 1282.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 August 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "01 August 2024" with 1282.5 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 01 August 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      | 2  | 31   | 01 August 2024    | 01 August 2024 | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 0.0    | 0.0         |
      |    |      | 01 August 2024    |                | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 3  | 31   | 01 September 2024 | 01 August 2024 | 0.0             | 583.34        | 12.5     | 0.0  | 0.0       | 595.84 | 595.84 | 595.84     | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1250          | 32.5     | 0.0  | 0.0       | 1282.5  | 1282.5 | 595.84     | 343.33 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 01 August 2024   | Repayment               | 1282.5 | 1250.0    | 32.5     | 0.0  | 0.0       | 0.0          |
      | 01 August 2024   | Accrual                 | 32.5   | 0.0       | 32.5     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 August 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1250.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 32.5   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1282.5 |        |
    When Customer makes a repayment undo on "01 August 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 August 2024   | Repayment               | 1282.5 | 1250.0    | 32.5     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 01 August 2024   | Accrual                 | 32.5   | 0.0       | 32.5     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3832
  Scenario: Progressive loan - flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = true, 360/30, second disbursement in the middle of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "15 July 2024"
    When Admin successfully disburse the loan on "15 July 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      |    |      | 15 July 2024      |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 31   | 01 August 2024    |           | 457.77          | 458.9         | 11.37    | 0.0  | 0.0       | 470.27 | 0.0  | 0.0        | 0.0  | 470.27      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 457.77        | 12.5     | 0.0  | 0.0       | 470.27 | 0.0  | 0.0        | 0.0  | 470.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250          | 33.87    | 0.0  | 0.0       | 1283.87 | 0.0  | 0.0        | 0.0  | 1283.87     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "15 July 2024" with 813.6 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 July 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      |    |      | 15 July 2024      |              | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 31   | 01 August 2024    | 15 July 2024 | 457.77          | 458.9         | 11.37    | 0.0  | 0.0       | 470.27 | 470.27 | 470.27     | 0.0    | 0.0         |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 457.77        | 12.5     | 0.0  | 0.0       | 470.27 | 0.0    | 0.0        | 0.0    | 470.27      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late   | Outstanding |
      | 1250          | 33.87    | 0.0  | 0.0       | 1283.87 | 813.6 | 470.27     | 343.33 | 470.27      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 15 July 2024     | Repayment               | 813.6  | 792.23    | 21.37    | 0.0  | 0.0       | 457.77       |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 792.23 |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 21.37  |
      | LIABILITY | 145023       | Suspense/Clearing account | 813.6 |        |
    When Customer makes a repayment undo on "15 July 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 July 2024     | Repayment               | 813.6  | 792.23    | 21.37    | 0.0  | 0.0       | 457.77       | true     | false    |

  @TestRailId:C3833
  Scenario: Progressive loan - flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = true, 360/30, second disbursement on the due date of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 August 2024"
    When Admin successfully disburse the loan on "01 August 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      |    |      | 01 August 2024    |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 583.34        | 12.5     | 0.0  | 0.0       | 595.84 | 0.0  | 0.0        | 0.0  | 595.84      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250          | 32.5     | 0.0  | 0.0       | 1282.5  | 0.0  | 0.0        | 0.0  | 1282.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 August 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "01 August 2024" with 1282.5 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 01 August 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      | 2  | 31   | 01 August 2024    | 01 August 2024 | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 0.0    | 0.0         |
      |    |      | 01 August 2024    |                | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 3  | 31   | 01 September 2024 | 01 August 2024 | 0.0             | 583.34        | 12.5     | 0.0  | 0.0       | 595.84 | 595.84 | 595.84     | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1250          | 32.5     | 0.0  | 0.0       | 1282.5  | 1282.5 | 595.84     | 343.33 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 01 August 2024   | Repayment               | 1282.5 | 1250.0    | 32.5     | 0.0  | 0.0       | 0.0          |
      | 01 August 2024   | Accrual                 | 32.5   | 0.0       | 32.5     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 August 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1250.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 32.5   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1282.5 |        |
    When Customer makes a repayment undo on "01 August 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 August 2024   | Repayment               | 1282.5 | 1250.0    | 32.5     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 01 August 2024   | Accrual                 | 32.5   | 0.0       | 32.5     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3834
  Scenario: Progressive loan - down payment, flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = true, actual/actual, second disbursement in the middle of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 500.0           | 250.0         | 7.5      | 0.0  | 0.0       | 257.5 | 0.0  | 0.0        | 0.0  | 257.5       |
      | 3  | 31   | 01 August 2024    |           | 250.0           | 250.0         | 7.5      | 0.0  | 0.0       | 257.5 | 0.0  | 0.0        | 0.0  | 257.5       |
      | 4  | 31   | 01 September 2024 |           | 0.0             | 250.0         | 7.5      | 0.0  | 0.0       | 257.5 | 0.0  | 0.0        | 0.0  | 257.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 22.5     | 0.0  | 0.0       | 1022.5 | 0.0  | 0.0        | 0.0  | 1022.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "15 July 2024"
    When Admin successfully disburse the loan on "15 July 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 500.0           | 250.0         | 7.5      | 0.0  | 0.0       | 257.5 | 0.0  | 0.0        | 0.0  | 257.5       |
      |    |      | 15 July 2024      |           | 250.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 3  | 0    | 15 July 2024      |           | 687.5           | 62.5          | 0.0      | 0.0  | 0.0       | 62.5  | 0.0  | 0.0        | 0.0  | 62.5        |
      | 4  | 31   | 01 August 2024    |           | 343.33          | 344.17        | 8.53     | 0.0  | 0.0       | 352.7 | 0.0  | 0.0        | 0.0  | 352.7       |
      | 5  | 31   | 01 September 2024 |           | 0.0             | 343.33        | 9.37     | 0.0  | 0.0       | 352.7 | 0.0  | 0.0        | 0.0  | 352.7       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1250          | 25.4     | 0.0  | 0.0       | 1275.4 | 0.0  | 0.0        | 0.0  | 1275.4      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "15 July 2024" with 922.7 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 June 2024      | 15 July 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 30   | 01 July 2024      | 15 July 2024 | 500.0           | 250.0         | 7.5      | 0.0  | 0.0       | 257.5 | 257.5 | 0.0        | 257.5 | 0.0         |
      |    |      | 15 July 2024      |              | 250.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 3  | 0    | 15 July 2024      | 15 July 2024 | 687.5           | 62.5          | 0.0      | 0.0  | 0.0       | 62.5  | 62.5  | 0.0        | 0.0   | 0.0         |
      | 4  | 31   | 01 August 2024    | 15 July 2024 | 343.33          | 344.17        | 8.53     | 0.0  | 0.0       | 352.7 | 352.7 | 352.7      | 0.0   | 0.0         |
      | 5  | 31   | 01 September 2024 |              | 0.0             | 343.33        | 9.37     | 0.0  | 0.0       | 352.7 | 0.0   | 0.0        | 0.0   | 352.7       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1250          | 25.4     | 0.0  | 0.0       | 1275.4 | 922.7 | 352.7      | 507.5 | 352.7       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 15 July 2024     | Repayment               | 922.7  | 906.67    | 16.03    | 0.0  | 0.0       | 343.33       |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 906.67 |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 16.03  |
      | LIABILITY | 145023       | Suspense/Clearing account | 922.7 |        |

  @TestRailId:C3835
  Scenario: Progressive loan - flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = false, actual/actual, second disbursement in the middle of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "15 July 2024"
    When Admin successfully disburse the loan on "15 July 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      |    |      | 15 July 2024      |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 31   | 01 August 2024    |           | 458.33          | 458.34        | 12.5     | 0.0  | 0.0       | 470.84 | 0.0  | 0.0        | 0.0  | 470.84      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 458.33        | 12.5     | 0.0  | 0.0       | 470.83 | 0.0  | 0.0        | 0.0  | 470.83      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1250          | 35.0     | 0.0  | 0.0       | 1285.0 | 0.0  | 0.0        | 0.0  | 1285.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement     | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "15 July 2024" with 814.17 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 July 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      |    |      | 15 July 2024      |              | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 31   | 01 August 2024    | 15 July 2024 | 458.33          | 458.34        | 12.5     | 0.0  | 0.0       | 470.84 | 470.84 | 470.84     | 0.0    | 0.0         |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 458.33        | 12.5     | 0.0  | 0.0       | 470.83 | 0.0    | 0.0        | 0.0    | 470.83      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 1250          | 35.0     | 0.0  | 0.0       | 1285.0 | 814.17 | 470.84     | 343.33 | 470.83      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement     | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 15 July 2024     | Repayment        | 814.17 | 791.67    | 22.5     | 0.0  | 0.0       | 458.33       |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 791.67 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 22.5   |
      | LIABILITY | 145023       | Suspense/Clearing account | 814.17 |        |
    When Customer makes a repayment undo on "15 July 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 July 2024     | Repayment        | 814.17 | 791.67    | 22.5     | 0.0  | 0.0       | 458.33       | true     | false    |

