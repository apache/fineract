@ChargeOffFeature
Feature: Charge-off - Part3

  @TestRailId:C3379
  Scenario: Charge-off with FRAUD reason after installment date when loan behavior is zero-interest with interestRecalculation - UC10.1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 17.01 | 0          | 0    | 85.04       |
    When Admin sets the business date to "29 February 2024"
    And Admin does charge-off the loan with reason "FRAUD" on "29 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "29 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.03           | 16.54         | 0.47     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.02           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.01           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.0            | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.0          | 0.0      | 0.0  | 0.0       | 16.0  | 0.0   | 0.0        | 0.0  | 16.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.05     | 0    | 0         | 101.05 | 17.01 | 0          | 0    | 84.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 29 February 2024 | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Charge-off       | 84.04  | 83.57     | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "29 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 83.57  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 0.47   |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 83.57 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.47  |        |

  @TestRailId:C3380
  Scenario: Charge-off with DELINQUENT reason after installment date when loan behavior is zero-interest with interestRecalculation - UC10.2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 17.01 | 0          | 0    | 85.04       |
    When Admin sets the business date to "29 February 2024"
    And Admin does charge-off the loan with reason "DELINQUENT" on "29 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "29 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.03           | 16.54         | 0.47     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.02           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.01           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.0            | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.0          | 0.0      | 0.0  | 0.0       | 16.0  | 0.0   | 0.0        | 0.0  | 16.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.05     | 0    | 0         | 101.05 | 17.01 | 0          | 0    | 84.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 29 February 2024 | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Charge-off       | 84.04  | 83.57     | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "29 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 83.57  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 0.47   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 83.57 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.47  |        |

  @TestRailId:C3381
  Scenario: Charge-off with OTHER reason after installment date when loan behavior is zero-interest with interestRecalculation disabled - UC10.3
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 17.01 | 0          | 0    | 85.04       |
    When Admin sets the business date to "29 February 2024"
    And Admin does charge-off the loan with reason "OTHER" on "29 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "29 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.03           | 16.54         | 0.47     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.02           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.01           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.0            | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.0          | 0.0      | 0.0  | 0.0       | 16.0  | 0.0   | 0.0        | 0.0  | 16.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.05     | 0    | 0         | 101.05 | 17.01 | 0          | 0    | 84.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 29 February 2024 | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Charge-off       | 84.04  | 83.57     | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "29 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 83.57  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 0.47   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 83.57 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.47  |        |

  @TestRailId:C3460
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when couple fee charges due dates is before charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 February 2024" due date and 5 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 February 2024" due date and 3 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 8.0  | 0.0       | 25.01 | 0.0   | 0.0        | 0.0  | 25.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 8.0  | 0.0       | 110.05 | 17.01 | 0.0        | 0.0  | 93.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.57         | 0.49     | 8.0  | 0.0       | 92.06 | 0.0   | 0.0        | 0.0  | 92.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 8.0  | 0.0       | 109.07 | 17.01 | 0.0        | 0.0  | 92.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 92.06  | 83.57     | 0.49     | 8.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3461
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when fee and penalty charges due dates is before charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 February 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin adds an NSF fee because of payment bounce with "15 February 2024" transaction date
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 5.0  | 10.0      | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 10.0      | 117.05 | 17.01 | 0.0        | 0.0  | 100.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.57         | 0.49     | 5.0  | 10.0      | 99.06 | 0.0   | 0.0        | 0.0  | 99.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 10.0      | 116.07 | 17.01 | 0.0        | 0.0  | 99.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 99.06  | 83.57     | 0.49     | 5.0  | 10.0      | 0.0          | false    | false    |

  @TestRailId:C3462
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when fee and penalty charges due dates is before charge-off date, and one charge is waived after charge-off
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 February 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin adds an NSF fee because of payment bounce with "15 February 2024" transaction date
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 5.0  | 10.0      | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 10.0      | 117.05 | 17.01 | 0.0        | 0.0  | 100.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.57         | 0.49     | 5.0  | 10.0      | 99.06 | 0.0   | 0.0        | 0.0  | 99.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 10.0      | 116.07 | 17.01 | 0.0        | 0.0  | 99.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 99.06  | 83.57     | 0.49     | 5.0  | 10.0      | 0.0          | false    | false    |
    And Admin waives due date charge
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.57         | 0.49     | 5.0  | 10.0      | 99.06 | 0.0   | 0.0        | 0.0  | 94.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 10.0      | 116.07 | 17.01 | 0.0        | 0.0  | 94.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 February 2024 | Waive loan charges | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Accrual            | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off         | 94.06  | 83.57     | 0.49     | 0.0  | 10.0      | 0.0          | false    | true     |

  @TestRailId:C3463
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when one fee charge is added after the charge off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 March 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 5.0  | 0.0       | 22.01 | 0.0   | 0.0        | 0.0  | 22.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 17.01 | 0.0        | 0.0  | 90.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 March 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.57         | 0.49     | 0.0  | 0.0       | 84.06 | 0.0   | 0.0        | 0.0  | 84.06       |
      | 3  | 4    | 05 March 2024    |                  | 0.0             | 0.0           | 0.0      | 5.0  | 0.0       | 5.0   | 0.0   | 0.0        | 0.0  | 5.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 0.0       | 106.07 | 17.01 | 0.0        | 0.0  | 89.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 89.06  | 83.57     | 0.49     | 5.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3464
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when couple fee charges due dates is after charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 March 2024" due date and 5 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 April 2024" due date and 2 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 5.0  | 0.0       | 22.01 | 0.0   | 0.0        | 0.0  | 22.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 2.0  | 0.0       | 19.01 | 0.0   | 0.0        | 0.0  | 19.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 7.0  | 0.0       | 109.05 | 17.01 | 0.0        | 0.0  | 92.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 March 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.57         | 0.49     | 0.0  | 0.0       | 84.06 | 0.0   | 0.0        | 0.0  | 84.06       |
      | 3  | 45   | 15 April 2024    |                  | 0.0             | 0.0           | 0.0      | 7.0  | 0.0       | 7.0   | 0.0   | 0.0        | 0.0  | 7.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 7.0  | 0.0       | 108.07 | 17.01 | 0.0        | 0.0  | 91.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 91.06  | 83.57     | 0.49     | 7.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3465
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when fee and penalty charges due dates is after charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 March 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin adds an NSF fee because of payment bounce with "29 February 2024" transaction date
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 10.0      | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 5.0  | 0.0       | 22.01 | 0.0   | 0.0        | 0.0  | 22.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 10.0      | 117.05 | 17.01 | 0.0        | 0.0  | 100.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    And Admin does charge-off the loan on "15 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "15 February 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 15 February 2024 |                  | 0.0             | 83.57         | 0.24     | 0.0  | 10.0      | 93.81 | 0.0   | 0.0        | 0.0  | 93.81       |
      | 3  | 19   | 05 March 2024    |                  | 0.0             | 0.0           | 0.0      | 5.0  | 0.0       | 5.0   | 0.0   | 0.0        | 0.0  | 5.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.82     | 5.0  | 10.0      | 115.82 | 17.01 | 0.0        | 0.0  | 98.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 February 2024 | Accrual          | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Charge-off       | 98.81  | 83.57     | 0.24     | 5.0  | 10.0      | 0.0          | false    | false    |

  @TestRailId:C3466
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when fee and penalty charges due dates is after charge-off date, and one charge is waived after charge-off
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 March 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin adds an NSF fee because of payment bounce with "29 February 2024" transaction date
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 10.0      | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 5.0  | 0.0       | 22.01 | 0.0   | 0.0        | 0.0  | 22.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 10.0      | 117.05 | 17.01 | 0.0        | 0.0  | 100.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    And Admin does charge-off the loan on "15 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "15 February 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 15 February 2024 |                  | 0.0             | 83.57         | 0.24     | 0.0  | 10.0      | 93.81 | 0.0   | 0.0        | 0.0  | 93.81       |
      | 3  | 19   | 05 March 2024    |                  | 0.0             | 0.0           | 0.0      | 5.0  | 0.0       | 5.0   | 0.0   | 0.0        | 0.0  | 5.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.82     | 5.0  | 10.0      | 115.82 | 17.01 | 0.0        | 0.0  | 98.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 February 2024 | Accrual          | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Charge-off       | 98.81  | 83.57     | 0.24     | 5.0  | 10.0      | 0.0          | false    | false    |
    And Admin waives charge
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 15 February 2024 |                  | 0.0             | 83.57         | 0.24     | 0.0  | 10.0      | 93.81 | 0.0   | 0.0        | 0.0  | 83.81       |
      | 3  | 19   | 05 March 2024    |                  | 0.0             | 0.0           | 0.0      | 5.0  | 0.0       | 5.0   | 0.0   | 0.0        | 0.0  | 5.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.82     | 5.0  | 10.0      | 115.82 | 17.01 | 0.0        | 0.0  | 88.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 February 2024 | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 February 2024 | Accrual            | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Charge-off         | 88.81  | 83.57     | 0.24     | 5.0  | 0.0       | 0.0          | false    | true     |

  @TestRailId:C3497 @AdvancedPaymentAllocation
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when charge-off occurs with adjustment to last installment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                 | 83.52           | 16.48         | 0.53     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |                 | 66.9            | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                 | 50.18           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                 | 33.36           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                 | 17.01           | 16.35         | 0.1      | 0.0  | 0.0       | 16.45 | 0.0   | 0.0        | 0.0  | 16.45       |
      | 6  | 30   | 01 July 2024     | 15 January 2024 | 0.0             | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100           | 1.5      | 0    | 0         | 101.5 | 17.01 | 17.01      | 0    | 84.49       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 82.99        | false    | false    |
    When Admin sets the business date to "29 February 2024"
    And Admin does charge-off the loan on "29 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "29 February 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.52           | 16.48         | 0.53     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 2  | 28   | 29 February 2024 |           | 0.0             | 83.52         | 0.47     | 0.0  | 0.0       | 83.99 | 17.01 | 17.01      | 0.0  | 66.98       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.0      | 0.0  | 0.0       | 101.0 | 17.01 | 17.01      | 0.0  | 83.99       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 82.99        | false    | false    |
      | 29 February 2024 | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Charge-off       | 83.99  | 82.99     | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3498 @AdvancedPaymentAllocation
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is enabled - case when charge-off occurs with allocation to last installment with interest allocation change
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.0            | 16.57         | 0.44     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.28           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.46           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.01           | 16.45         | 0.1      | 0.0  | 0.0       | 16.55 | 0.0   | 0.0        | 0.0  | 16.55       |
      | 6  | 30   | 01 July 2024     | 15 February 2024 | 0.0             | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.6      | 0.0  | 0.0       | 101.6 | 34.02 | 17.01      | 0.0  | 67.58       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 February 2024 | Repayment        | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 66.56        | false    | false    |
    When Admin sets the business date to "29 February 2024"
    And Admin does charge-off the loan on "29 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "29 February 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 29 February 2024 |                  | 0.0             | 83.57         | 0.42     | 0.0  | 0.0       | 83.99 | 17.01 | 17.01      | 0.0  | 66.98       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.0      | 0.0  | 0.0       | 101.0 | 34.02 | 17.01      | 0.0  | 66.98       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 February 2024 | Repayment        | 17.01  | 16.77     | 0.24     | 0.0  | 0.0       | 66.8         | false    | true     |
      | 29 February 2024 | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Charge-off       | 66.98  | 66.8      | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3727
  Scenario: As a user I want to perform accelerate maturity to charge-off date with interest recalculation when backdated full repayment occurs after - S12
    Given Global configuration "is-principal-compounding-disabled-for-overdue-loans" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "31 March 2024"
    And Admin does charge-off the loan on "31 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "31 March 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 31 March 2024    |                  | 0.0             | 67.05         | 0.47     | 0.0  | 0.0       | 67.52 | 0.0   | 0.0        | 0.0  | 67.52       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.54     | 0.0  | 0.0       | 101.54 | 17.01 | 0.0        | 0.0  | 84.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 31 March 2024    | Accrual          | 1.54   | 0.0       | 1.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Charge-off       | 84.53  | 83.57     | 0.96     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 84.06 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 March 2024    | 50.04           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 March 2024    | 33.03           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 March 2024    | 16.02           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 March 2024    | 0.0             | 16.02         | 0.0      | 0.0  | 0.0       | 16.02 | 16.02 | 16.02      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 0.0  | 0.0       | 101.07 | 101.07 | 67.05      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment            | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment            | 84.06  | 83.57     | 0.49     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual              | 1.54   | 0.0       | 1.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual Adjustment   | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Global configuration "is-principal-compounding-disabled-for-overdue-loans" is disabled

  @TestRailId:C3728
  Scenario: As a user I want to perform accelerate maturity to charge-off date with interest recalculation when backdated repayment with excess amount occurs after - S13
    Given Global configuration "is-principal-compounding-disabled-for-overdue-loans" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "31 March 2024"
    And Admin does charge-off the loan on "31 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "31 March 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 31 March 2024    |                  | 0.0             | 67.05         | 0.47     | 0.0  | 0.0       | 67.52 | 0.0   | 0.0        | 0.0  | 67.52       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.54     | 0.0  | 0.0       | 101.54 | 17.01 | 0.0        | 0.0  | 84.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 31 March 2024    | Accrual          | 1.54   | 0.0       | 1.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Charge-off       | 84.53  | 83.57     | 0.96     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 31 March 2024    |                  | 0.0             | 67.05         | 0.32     | 0.0  | 0.0       | 67.37 | 10.0  | 10.0       | 0.0  | 57.37       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.39     | 0.0  | 0.0       | 101.39 | 44.02 | 10.0       | 0.0  | 57.37       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment          | 27.01  | 26.52     | 0.49     | 0.0  | 0.0       | 57.05        | false    | false    |
      | 31 March 2024    | Accrual            | 1.54   | 0.0       | 1.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual Adjustment | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Charge-off         | 57.37  | 57.05     | 0.32     | 0.0  | 0.0       | 0.0          | false    | true     |
    And Global configuration "is-principal-compounding-disabled-for-overdue-loans" is disabled

  @TestRailId:C3729
  Scenario: As a user I want to perform accelerate maturity to charge-off date with interest recalculation when backdated first repayment with excess amount occurs after
    Given Global configuration "is-principal-compounding-disabled-for-overdue-loans" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "31 March 2024"
    And Admin does charge-off the loan on "31 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "31 March 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 31 March 2024    |           | 0.0             | 67.14         | 0.56     | 0.0  | 0.0       | 67.7  | 0.0   | 0.0        | 0.0  | 67.7        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.72     | 0.0  | 0.0       | 101.72 | 0.0   | 0.0        | 0.0  | 101.72      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 31 March 2024    | Accrual          | 1.72   | 0.0       | 1.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Charge-off       | 101.72 | 100.0     | 1.72     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 70.00 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024 | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 3  | 30   | 31 March 2024    |               | 0.0             | 67.14         | 0.0      | 0.0  | 0.0       | 67.14 | 35.98 | 35.98      | 0.0   | 31.16       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 1.16     | 0.0  | 0.0       | 101.16 | 70.0  | 35.98      | 17.01 | 31.16       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 March 2024    | Repayment          | 70.0   | 68.84     | 1.16     | 0.0  | 0.0       | 31.16        | false    | false    |
      | 31 March 2024    | Accrual            | 1.72   | 0.0       | 1.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual Adjustment | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Charge-off         | 31.16  | 31.16     | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
    And Global configuration "is-principal-compounding-disabled-for-overdue-loans" is disabled

  @TestRailId:C3499 @AdvancedPaymentAllocation
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when charge-off occurs with adjustment to last installment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan with last installment strategy, without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 17.05 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |                 | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |                 | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |                 | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                 | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     | 15 January 2024 | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 17.05 | 17.05      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 17.05 | 17.05      | 0    | 85.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 17.05  | 16.95     | 0.1      | 0.0  | 0.0       | 83.05        | false    | false    |
    When Admin sets the business date to "29 February 2024"
    And Admin does charge-off the loan on "29 February 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 2  | 28   | 29 February 2024 |           | 0.0             | 83.58         | 0.47     | 0.0  | 0.0       | 84.05 | 17.05 | 17.05      | 0.0  | 67.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.05     | 0.0  | 0.0       | 101.05 | 17.05 | 17.05      | 0.0  | 84.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 17.05  | 16.58     | 0.47     | 0.0  | 0.0       | 83.42        | false    | true     |
      | 29 February 2024 | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Charge-off       | 84.0   | 83.42     | 0.58     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3500 @AdvancedPaymentAllocation
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when charge-off occurs with allocation to last installment with interest allocation change
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan with last installment strategy, without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 17.0 | 0          | 0    | 85.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 17.05 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     | 15 February 2024 | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 17.05 | 17.05      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.05 | 17.05      | 0    | 68.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 15 February 2024 | Repayment        | 17.05  | 16.95     | 0.1      | 0.0  | 0.0       | 66.63        | false    | false    |
    When Admin sets the business date to "29 February 2024"
    And Admin does charge-off the loan on "29 February 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 29 February 2024 |                  | 0.0             | 83.58         | 0.47     | 0.0  | 0.0       | 84.05 | 17.05 | 17.05      | 0.0  | 67.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.05     | 0.0  | 0.0       | 101.05 | 34.05 | 17.05      | 0.0  | 67.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 15 February 2024 | Repayment        | 17.05  | 16.58     | 0.47     | 0.0  | 0.0       | 67.0         | false    | true     |
      | 29 February 2024 | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Charge-off       | 67.0   | 67.0      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3467
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when one fee charge due date is before charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 February 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 5.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 5    | 0         | 107.05 | 17.0 | 0          | 0    | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.58         | 0.49     | 5.0  | 0.0       | 89.07 | 0.0  | 0.0        | 0.0  | 89.07       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 0.0       | 106.07 | 17.0 | 0.0        | 0.0  | 89.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 89.07  | 83.58     | 0.49     | 5.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3468
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when couple fee charges due dates is before charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 February 2024" due date and 5 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 February 2024" due date and 3 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 8.0  | 0.0       | 25.0  | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 8    | 0         | 110.05 | 17.0 | 0          | 0    | 93.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.58         | 0.49     | 8.0  | 0.0       | 92.07 | 0.0  | 0.0        | 0.0  | 92.07       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 8.0  | 0.0       | 109.07 | 17.0 | 0.0        | 0.0  | 92.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 92.07  | 83.58     | 0.49     | 8.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3469
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when fee and penalty charges due dates is before charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 February 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin adds an NSF fee because of payment bounce with "15 February 2024" transaction date
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 5.0  | 10.0      | 32.0  | 0.0  | 0.0        | 0.0  | 32.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 5    | 10        | 117.05 | 17.0 | 0          | 0    | 100.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.58         | 0.49     | 5.0  | 10.0      | 99.07 | 0.0  | 0.0        | 0.0  | 99.07       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 10.0      | 116.07 | 17.0 | 0.0        | 0.0  | 99.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 99.07  | 83.58     | 0.49     | 5.0  | 10.0      | 0.0          | false    | false    |

  @TestRailId:C3470
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when fee and penalty charges due dates is before charge-off date, and one charge is waived after charge-off
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 February 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin adds an NSF fee because of payment bounce with "15 February 2024" transaction date
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 5.0  | 10.0      | 32.0  | 0.0  | 0.0        | 0.0  | 32.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 5    | 10        | 117.05 | 17.0 | 0          | 0    | 100.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.58         | 0.49     | 5.0  | 10.0      | 99.07 | 0.0  | 0.0        | 0.0  | 99.07       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 10.0      | 116.07 | 17.0 | 0.0        | 0.0  | 99.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 99.07  | 83.58     | 0.49     | 5.0  | 10.0      | 0.0          | false    | false    |
    And Admin waives due date charge
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.58         | 0.49     | 5.0  | 10.0      | 99.07 | 0.0  | 0.0        | 0.0  | 94.07       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 10.0      | 116.07 | 17.0 | 0.0        | 0.0  | 94.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 15 February 2024 | Waive loan charges | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Accrual            | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off         | 94.07  | 83.58     | 0.49     | 0.0  | 10.0      | 0.0          | false    | true     |

  @TestRailId:C3471
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when one fee charge is added after the charge off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 March 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 5.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 5    | 0         | 107.05 | 17.0 | 0          | 0    | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.58         | 0.49     | 0.0  | 0.0       | 84.07 | 0.0  | 0.0        | 0.0  | 84.07       |
      | 3  | 4    | 05 March 2024    |                  | 0.0             | 0.0           | 0.0      | 5.0  | 0.0       | 5.0   | 0.0  | 0.0        | 0.0  | 5.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 5.0  | 0.0       | 106.07 | 17.0 | 0.0        | 0.0  | 89.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 89.07  | 83.58     | 0.49     | 5.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3472
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when couple fee charges due dates is after charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 March 2024" due date and 5 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 April 2024" due date and 2 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 5.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 2.0  | 0.0       | 19.0  | 0.0  | 0.0        | 0.0  | 19.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 7    | 0         | 109.05 | 17.0 | 0          | 0    | 92.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.58         | 0.49     | 0.0  | 0.0       | 84.07 | 0.0  | 0.0        | 0.0  | 84.07       |
      | 3  | 45   | 15 April 2024    |                  | 0.0             | 0.0           | 0.0      | 7.0  | 0.0       | 7.0   | 0.0  | 0.0        | 0.0  | 7.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 7.0  | 0.0       | 108.07 | 17.0 | 0.0        | 0.0  | 91.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 91.07  | 83.58     | 0.49     | 7.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3473
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when fee and penalty charges due dates is after charge-off date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 March 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin adds an NSF fee because of payment bounce with "29 February 2024" transaction date
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 0.0  | 10.0      | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 5.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 5    | 10        | 117.05 | 17.0 | 0          | 0    | 100.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    And Admin does charge-off the loan on "15 February 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 15 February 2024 |                  | 0.0             | 83.58         | 0.24     | 0.0  | 10.0      | 93.82 | 0.0  | 0.0        | 0.0  | 93.82       |
      | 3  | 19   | 05 March 2024    |                  | 0.0             | 0.0           | 0.0      | 5.0  | 0.0       | 5.0   | 0.0  | 0.0        | 0.0  | 5.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.82     | 5.0  | 10.0      | 115.82 | 17.0 | 0.0        | 0.0  | 98.82       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 15 February 2024 | Accrual          | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Charge-off       | 98.82  | 83.58     | 0.24     | 5.0  | 10.0      | 0.0          | false    | false    |

  @TestRailId:C3474
  Scenario: Verify accelerate maturity to charge-off date when interest recalculation is disabled - case when fee and penalty charges due dates is after charge-off date, and one charge is waived after charge-off
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: "1 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.00 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 March 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin adds an NSF fee because of payment bounce with "29 February 2024" transaction date
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.07           | 16.51         | 0.49     | 0.0  | 10.0      | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.61         | 0.39     | 5.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 4  | 30   | 01 May 2024      |                  | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 5    | 10        | 117.05 | 17.0 | 0          | 0    | 100.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
    And Admin does charge-off the loan on "15 February 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 15 February 2024 |                  | 0.0             | 83.58         | 0.24     | 0.0  | 10.0      | 93.82 | 0.0  | 0.0        | 0.0  | 93.82       |
      | 3  | 19   | 05 March 2024    |                  | 0.0             | 0.0           | 0.0      | 5.0  | 0.0       | 5.0   | 0.0  | 0.0        | 0.0  | 5.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.82     | 5.0  | 10.0      | 115.82 | 17.0 | 0.0        | 0.0  | 98.82       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 15 February 2024 | Accrual          | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Charge-off       | 98.82  | 83.58     | 0.24     | 5.0  | 10.0      | 0.0          | false    | false    |
    And Admin waives charge
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 17.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 15 February 2024 |                  | 0.0             | 83.58         | 0.24     | 0.0  | 10.0      | 93.82 | 0.0  | 0.0        | 0.0  | 83.82       |
      | 3  | 19   | 05 March 2024    |                  | 0.0             | 0.0           | 0.0      | 5.0  | 0.0       | 5.0   | 0.0  | 0.0        | 0.0  | 5.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.82     | 5.0  | 10.0      | 115.82 | 17.0 | 0.0        | 0.0  | 88.82       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.0   | 16.42     | 0.58     | 0.0  | 0.0       | 83.58        | false    | false    |
      | 15 February 2024 | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 83.58        | false    | false    |
      | 15 February 2024 | Accrual            | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Charge-off         | 88.82  | 83.58     | 0.24     | 5.0  | 0.0       | 0.0          | false    | true     |

  @TestRailId:C3508
  Scenario: Verify interest portion on Charge-off transaction in case of backdated full repayment on the same period
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2023   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 835.74          | 164.26        | 5.83     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 2  | 28   | 01 March 2023    |           | 670.53          | 165.21        | 4.88     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 3  | 31   | 01 April 2023    |           | 504.35          | 166.18        | 3.91     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 4  | 30   | 01 May 2023      |           | 337.2           | 167.15        | 2.94     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 5  | 31   | 01 June 2023     |           | 169.08          | 168.12        | 1.97     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 6  | 30   | 01 July 2023     |           | 0.0             | 169.08        | 0.99     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 20.52    | 0    | 0         | 1020.52 | 0    | 0          | 0    | 1020.52     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 January 2023"
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 170.09 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 832.54          | 167.46        | 2.63     | 0.0  | 0.0       | 170.09 | 170.09 | 170.09     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 669.98          | 162.56        | 7.53     | 0.0  | 0.0       | 170.09 | 0.0    | 0.0        | 0.0  | 170.09      |
      | 3  | 31   | 01 April 2023    |                 | 503.8           | 166.18        | 3.91     | 0.0  | 0.0       | 170.09 | 0.0    | 0.0        | 0.0  | 170.09      |
      | 4  | 30   | 01 May 2023      |                 | 336.65          | 167.15        | 2.94     | 0.0  | 0.0       | 170.09 | 0.0    | 0.0        | 0.0  | 170.09      |
      | 5  | 31   | 01 June 2023     |                 | 168.52          | 168.13        | 1.96     | 0.0  | 0.0       | 170.09 | 0.0    | 0.0        | 0.0  | 170.09      |
      | 6  | 30   | 01 July 2023     |                 | 0.0             | 168.52        | 0.98     | 0.0  | 0.0       | 169.5  | 0.0    | 0.0        | 0.0  | 169.5       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 19.95    | 0    | 0         | 1019.95 | 170.09 | 170.09     | 0    | 849.86      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2023  | Repayment        | 170.09 | 167.46    | 2.63     | 0.0  | 0.0       | 832.54       | false    | false    |
    When Admin sets the business date to "31 January 2023"
    And Admin does charge-off the loan on "31 January 2023"
    Then LoanBalanceChangedBusinessEvent is created on "31 January 2023"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 835.05          | 164.95        | 5.14     | 0.0  | 0.0       | 170.09 | 170.09 | 170.09     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 664.96          | 170.09        | 0.0      | 0.0  | 0.0       | 170.09 | 0.0    | 0.0        | 0.0  | 170.09      |
      | 3  | 31   | 01 April 2023    |                 | 494.87          | 170.09        | 0.0      | 0.0  | 0.0       | 170.09 | 0.0    | 0.0        | 0.0  | 170.09      |
      | 4  | 30   | 01 May 2023      |                 | 324.78          | 170.09        | 0.0      | 0.0  | 0.0       | 170.09 | 0.0    | 0.0        | 0.0  | 170.09      |
      | 5  | 31   | 01 June 2023     |                 | 154.69          | 170.09        | 0.0      | 0.0  | 0.0       | 170.09 | 0.0    | 0.0        | 0.0  | 170.09      |
      | 6  | 30   | 01 July 2023     |                 | 0.0             | 154.69        | 0.0      | 0.0  | 0.0       | 154.69 | 0.0    | 0.0        | 0.0  | 154.69      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 5.14     | 0    | 0         | 1005.14 | 170.09 | 170.09     | 0    | 835.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2023  | Repayment        | 170.09 | 167.46    | 2.63     | 0.0  | 0.0       | 832.54       | false    | false    |
      | 31 January 2023  | Accrual          | 5.14   | 0.0       | 5.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2023  | Charge-off       | 835.05 | 832.54    | 2.51     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3511
  Scenario: Backdate charge-off reverse accruals with isInterestRecognitionOnDisbursementDate = true
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2024   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 54.75    | 0    | 0         | 1054.75 | 0    | 0          | 0    | 1054.75     |
    Then Loan Product response contains interestRecognitionOnDisbursementDate flag with value "true"
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 263.69 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    Then Admin runs inline COB job for Loan
    And Admin does charge-off the loan on "17 January 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual            | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual            | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment          | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 15 January 2024  | Accrual            | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual            | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Charge-off         | 787.64 | 746.09    | 41.55    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3512
  Scenario: Backdate charge-off reverse accruals with isInterestRecognitionOnDisbursementDate = false
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 54.75    | 0    | 0         | 1054.75 | 0    | 0          | 0    | 1054.75     |
    Then Loan Product response contains interestRecognitionOnDisbursementDate flag with value "false"
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 263.69 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    Then Admin runs inline COB job for Loan
    And Admin does charge-off the loan on "17 January 2024"
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
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Charge-off       | 787.64 | 746.09    | 41.55    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3519
  Scenario: Charge-off on progressive loan when accounting is none
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_ACCOUNTING_RULE_NONE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.25           | 16.75         | 0.26     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 67.0            | 16.25         | 0.76     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                 | 50.38           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                 | 33.66           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.85           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.85         | 0.1      | 0.0  | 0.0       | 16.95 | 0.0   | 0.0        | 0.0  | 16.95       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment          | 17.01  | 16.75     | 0.26     | 0.0  | 0.0       | 83.25        | false    | false    |
      | 01 March 2024    | Charge-off         | 84.99  | 83.25     | 1.74     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3513
  Scenario: Accrual handling in case of charged-off loan when loan behavior is zero-interest with interestRecalculation enabled, interest recognition from disbursement date = FALSE
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0     |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72	       | 0.29     | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0   | 0.0       | 17.0 	| 0.0  | 0.0        | 0.0  | 17.0 	     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05  | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "31 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0   | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
    And Admin does charge-off the loan on "31 January 2024"
    Then LoanBalanceChangedBusinessEvent is created on "31 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 83.55           | 16.45         | 0.56     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |                  | 66.54           | 17.01         | 0.0      | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.53           | 17.01         | 0.0      | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 32.52           | 17.01	      | 0.0      | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01	    |
      | 5  | 31   | 01 June 2024     |                  | 15.51           | 17.01         | 0.0      | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01	    |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 15.51         | 0.0      | 0.0   | 0.0       | 15.51   | 0.0  | 0.0        | 0.0  | 15.51	    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 100           | 0.56     | 0    | 0         | 100.56  | 0.0  | 0          | 0    | 100.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0   | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Charge-off       | 100.56  | 100.0     | 0.56     | 0.0   | 0.0       | 0.0          | false    | false    |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "31 January 2024"

  @TestRailId:C3514
  Scenario: Accrual handling in case of charged-off loan when loan behavior is zero-interest with interestRecalculation enabled, interest recognition from disbursement date = TRUE
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0     |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72	       | 0.29     | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0   | 0.0       | 17.0 	| 0.0  | 0.0        | 0.0  | 17.0 	     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05  | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "31 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0   | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
    And Admin does charge-off the loan on "31 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 83.55           | 16.45         | 0.56     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |                  | 66.54           | 17.01         | 0.0      | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.53           | 17.01         | 0.0      | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 32.52           | 17.01	      | 0.0      | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01	    |
      | 5  | 31   | 01 June 2024     |                  | 15.51           | 17.01         | 0.0      | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01	    |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 15.51         | 0.0      | 0.0   | 0.0       | 15.51   | 0.0  | 0.0        | 0.0  | 15.51	    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 100           | 0.56     | 0    | 0         | 100.56  | 0.0  | 0          | 0    | 100.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0   | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02    | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.01    | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Charge-off       | 100.56  | 100.0     | 0.56     | 0.0   | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3515
  Scenario: Accrual handling in case of charged-off loan and backdated repayment reversal when loan behavior is zero-interest with interestRecalculation enabled, interest recognition from disbursement date = FALSE
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 54.75    | 0    | 0         | 1054.75 | 0    | 0          | 0    | 1054.75     |
    When Admin sets the business date to "15 January 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 263.69 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 746.09          | 253.91        | 9.78     | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 507.44          | 238.65        | 25.04    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 254.74          | 252.7         | 10.99    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 254.74        | 5.52     | 0.0  | 0.0       | 260.26 | 0.0    | 0.0        | 0.0  | 260.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 51.33    | 0    | 0         | 1051.33 | 263.69 | 263.69     | 0    | 787.64      |
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
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
    When Admin sets the business date to "20 January 2024"
    When Admin runs inline COB job for Loan
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
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Admin does charge-off the loan on "20 January 2024"
    Then LoanBalanceChangedBusinessEvent is created on "20 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 748.7           | 251.3         | 12.39    | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 485.01          | 263.69        | 0.0      | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 221.32          | 263.69        | 0.0      | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 221.32        | 0.0      | 0.0  | 0.0       | 221.32 | 0.0    | 0.0        | 0.0  | 221.32      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 12.39    | 0    | 0         | 1012.39 | 263.69 | 263.69     | 0    | 748.7       |
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
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 15 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Charge-off       | 748.7  | 746.09    | 2.61     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "20 January 2024"
    When Admin sets the business date to "21 January 2024"
    When Customer undo "1"th "Repayment" transaction made on "15 January 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.69    | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69  | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | true     | false    |
      | 15 January 2024  | Accrual          | 0.69    | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.53    | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52    | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52    | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52    | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.52    | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.89    | 0.0       | 0.89     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Charge-off       | 1013.28 | 1000.0    | 13.28    | 0.0  | 0.0       | 0.0          | false    | true     |

  @TestRailId:C3516
  Scenario: Accrual handling in case of charged-off loan with backdated repayment when loan behavior is zero-interest with interestRecalculation enabled, interest recognition from disbursement date = FALSE
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
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
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    When Admin sets the business date to "14 February 2024"
    When Admin runs inline COB job for Loan
    And Admin does charge-off the loan on "14 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "14 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.82           | 16.75         | 0.26     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.81           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 32.8            | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 15.79           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 15.79         | 0.0      | 0.0  | 0.0       | 15.79 | 0.0  | 0.0        | 0.0  | 15.79       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.84     | 0    | 0         | 100.84 | 0    | 0          | 0    | 100.84      |
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
      | 14 February 2024 | Charge-off       | 100.84 | 100.0     | 0.84     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "14 February 2024"
#  ----- backdated repayment  on 1 February made on 14 February ----- #
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.78           | 16.79         | 0.22     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.77           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 32.76           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 15.75           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 15.75         | 0.0      | 0.0  | 0.0       | 15.75 | 0.0   | 0.0        | 0.0  | 15.75       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100           | 0.8      | 0    | 0         | 100.8 | 17.01 | 0          | 0    | 83.79       |
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
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
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
      | 14 February 2024 | Accrual Adjustment | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Charge-off         | 83.79  | 83.57     | 0.22     | 0.0  | 0.0       | 0.0          | false    | true     |
    Then LoanAccrualAdjustmentTransactionBusinessEvent is raised on "14 February 2024"

  @TestRailId:C3517
  Scenario: Accrual handling in case of charged-off loan and backdated repayment reversal when loan behavior is zero-interest with interestRecalculation enabled, interest recognition from disbursement date = TRUE
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE | 01 January 2024   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.98          | 242.02        | 21.67    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 2  | 29   | 01 March 2024    |           | 510.71          | 247.27        | 16.42    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |           | 258.09          | 252.62        | 11.07    | 0.0  | 0.0       | 263.69 | 0.0  | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 258.09        | 5.59     | 0.0  | 0.0       | 263.68 | 0.0  | 0.0        | 0.0  | 263.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 54.75    | 0    | 0         | 1054.75 | 0    | 0          | 0    | 1054.75     |
    When Admin sets the business date to "15 January 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 263.69 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 746.09          | 253.91        | 9.78     | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 507.44          | 238.65        | 25.04    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 254.74          | 252.7         | 10.99    | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 254.74        | 5.52     | 0.0  | 0.0       | 260.26 | 0.0    | 0.0        | 0.0  | 260.26      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 51.33    | 0    | 0         | 1051.33 | 263.69 | 263.69     | 0    | 787.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
    When Admin sets the business date to "20 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 15 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Admin does charge-off the loan on "20 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 748.7           | 251.3         | 12.39    | 0.0  | 0.0       | 263.69 | 263.69 | 263.69     | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 485.01          | 263.69        | 0.0      | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 3  | 31   | 01 April 2024    |                 | 221.32          | 263.69        | 0.0      | 0.0  | 0.0       | 263.69 | 0.0    | 0.0        | 0.0  | 263.69      |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 221.32        | 0.0      | 0.0  | 0.0       | 221.32 | 0.0    | 0.0        | 0.0  | 221.32      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 12.39    | 0    | 0         | 1012.39 | 263.69 | 263.69     | 0    | 748.7       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69 | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | false    | false    |
      | 15 January 2024  | Accrual          | 0.53   | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52   | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Charge-off       | 748.7  | 746.09    | 2.61     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "21 January 2024"
    When Customer undo "1"th "Repayment" transaction made on "15 January 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.69    | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.7     | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.69    | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 263.69  | 253.91    | 9.78     | 0.0  | 0.0       | 746.09       | true     | false    |
      | 15 January 2024  | Accrual          | 0.53    | 0.0       | 0.53     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.52    | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.52    | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.52    | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.52    | 0.0       | 0.52     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.89    | 0.0       | 0.89     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Charge-off       | 1013.28 | 1000.0    | 13.28    | 0.0  | 0.0       | 0.0          | false    | true     |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "20 January 2024"

  @TestRailId:C3518
  Scenario: Accrual handling in case of charged-off loan with backdated repayment when loan behavior is zero-interest with interestRecalculation enabled, interest recognition from disbursement date = TRUE
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
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
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    When Admin sets the business date to "14 February 2024"
    When Admin runs inline COB job for Loan
    And Admin does charge-off the loan on "14 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.82           | 16.75         | 0.26     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.81           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 32.8            | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 15.79           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 15.79         | 0.0      | 0.0  | 0.0       | 15.79 | 0.0  | 0.0        | 0.0  | 15.79       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.84     | 0    | 0         | 100.84 | 0    | 0          | 0    | 100.84      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
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
      | 14 February 2024 | Charge-off       | 100.84 | 100.0     | 0.84     | 0.0  | 0.0       | 0.0          | false    | false    |
#  ----- backdated repayment  on 1 February made on 14 February ----- #
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.78           | 16.79         | 0.22     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.77           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 32.76           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 15.75           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 15.75         | 0.0      | 0.0  | 0.0       | 15.75 | 0.0   | 0.0        | 0.0  | 15.75       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100           | 0.8      | 0    | 0         | 100.8 | 17.01 | 0          | 0    | 83.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
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
      | 14 February 2024 | Accrual Adjustment | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Charge-off         | 83.79  | 83.57     | 0.22     | 0.0  | 0.0       | 0.0          | false    | true     |
    Then LoanAccrualAdjustmentTransactionBusinessEvent is raised on "14 February 2024"

  @TestRailId:C3559
  Scenario: Reverse-replay Charge-off with accrual adjustment due to backdated payment
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_360_30_DECLINING_BALANCE_PERIOD_DAILY_INT_RECALC | 01 January 2024   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.67          | 242.33        | 21.67    | 0.0  | 0.0       | 264.0  | 0.0  | 0.0        | 0.0  | 264.0       |
      | 2  | 29   | 01 March 2024    |           | 510.09          | 247.58        | 16.42    | 0.0  | 0.0       | 264.0  | 0.0  | 0.0        | 0.0  | 264.0       |
      | 3  | 31   | 01 April 2024    |           | 257.14          | 252.95        | 11.05    | 0.0  | 0.0       | 264.0  | 0.0  | 0.0        | 0.0  | 264.0       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 257.14        | 5.57     | 0.0  | 0.0       | 262.71 | 0.0  | 0.0        | 0.0  | 262.71      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 54.71    | 0    | 0         | 1054.71 | 0.0  | 0.0        | 0.0  | 1054.71     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    # Run COB for each day from Jan 1 to Jan 19 to create accrual entries
    When Admin sets the business date to "3 January 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 757.67          | 242.33        | 21.67    | 0.0  | 0.0       | 264.0  | 0.0  | 0.0        | 0.0  | 264.0       |
      | 2  | 29   | 01 March 2024    |           | 510.09          | 247.58        | 16.42    | 0.0  | 0.0       | 264.0  | 0.0  | 0.0        | 0.0  | 264.0       |
      | 3  | 31   | 01 April 2024    |           | 257.14          | 252.95        | 11.05    | 0.0  | 0.0       | 264.0  | 0.0  | 0.0        | 0.0  | 264.0       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 257.14        | 5.57     | 0.0  | 0.0       | 262.71 | 0.0  | 0.0        | 0.0  | 262.71      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 54.71    | 0    | 0         | 1054.71 | 0.0  | 0.0        | 0.0  | 1054.71     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "20 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "15 January 2024"
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 January 2024" with 264 EUR transaction amount and system-generated Idempotency key and check external owner
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 757.67          | 242.33        | 21.67    | 0.0  | 0.0       | 264.0  | 264.0 | 264.0      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 510.09          | 247.58        | 16.42    | 0.0  | 0.0       | 264.0  | 0.0   | 0.0        | 0.0  | 264.0       |
      | 3  | 31   | 01 April 2024    |                 | 257.14          | 252.95        | 11.05    | 0.0  | 0.0       | 264.0  | 0.0   | 0.0        | 0.0  | 264.0       |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 257.14        | 5.57     | 0.0  | 0.0       | 262.71 | 0.0   | 0.0        | 0.0  | 262.71      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000          | 54.71    | 0    | 0         | 1054.71 | 264.0 | 264.0      | 0.0  | 790.71     |
    When Admin sets the business date to "20 January 2024"
    And Admin does charge-off the loan on "20 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 757.67          | 242.33        | 21.67    | 0.0  | 0.0       | 264.0  | 264.0 | 264.0      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 510.09          | 247.58        | 16.42    | 0.0  | 0.0       | 264.0  | 0.0   | 0.0        | 0.0  | 264.0       |
      | 3  | 31   | 01 April 2024    |                 | 257.14          | 252.95        | 11.05    | 0.0  | 0.0       | 264.0  | 0.0   | 0.0        | 0.0  | 264.0       |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 257.14        | 5.57     | 0.0  | 0.0       | 262.71 | 0.0   | 0.0        | 0.0  | 262.71      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000          | 54.71    | 0    | 0         | 1054.71 | 264.0 | 264.0      | 0.0  | 790.71     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual            | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment          | 264.0  | 242.33    | 21.67    | 0.0  | 0.0       | 757.67       | false    | false    |
      | 16 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual            | 0.69   | 0.0       | 0.69     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual            | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual Adjustment | 12.58  | 0.0       | 12.58    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Charge-off         | 790.71 | 757.67    | 33.04    | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan marked as charged-off on "20 January 2024"
    Then LoanAccrualAdjustmentTransactionBusinessEvent is raised on "20 January 2024"

  @TestRailId:C3567
  Scenario: Charge-off reversal on a fraud loan respects GL mapping based on charge-off reason
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Admin can successfully set Fraud flag to the loan
    When Admin sets the business date to "03 February 2024"
    And Admin does charge-off the loan with reason "DELINQUENT" on "03 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "03 February 2024"
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 0.61   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 100.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.61  |        |
    Then Admin does a charge-off undo the loan
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 100.0  |
      | ASSET   | 112601       | Loans Receivable           | 100.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 0.61   |
      | ASSET   | 112603       | Interest/Fee Receivable    | 0.61  |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 100.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       |       | 100.0  |
      | INCOME  | 404001       | Interest Income Charge Off | 0.61  |        |
      | INCOME  | 404001       | Interest Income Charge Off |       | 0.61   |

  @TestRailId:C3572
  Scenario: Charge-off added before reversed charge-off when loan with interestRecalculation - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "23 January 2024"
    And Admin does charge-off the loan on "23 January 2024"
    Then LoanBalanceChangedBusinessEvent is created on "23 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.4            | 16.6          | 0.41     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.39           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.38           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 32.37           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 15.36           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 15.36         | 0.0      | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.41     | 0    | 0         | 100.41 | 0    | 0          | 0    | 100.41      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 23 January 2024  | Accrual            | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Charge-off         | 100.41 | 100.0     | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Admin does a charge-off undo the loan
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
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 23 January 2024  | Accrual          | 0.41   | 0.0       | 0.41     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Charge-off       | 100.41 | 100.0     | 0.41     | 0.0  | 0.0       | 0.0          | true     | false    |
    And Admin does charge-off the loan on "05 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.07           | 16.93         | 0.08     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.06           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.05           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 32.04           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 15.03           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 15.03         | 0.0      | 0.0  | 0.0       | 15.03 | 0.0  | 0.0        | 0.0  | 15.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.08     | 0    | 0         | 100.08 | 0    | 0          | 0    | 100.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 05 January 2024  | Accrual            | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Charge-off         | 100.08 | 100.0     | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Charge-off         | 100.41 | 100.0     | 0.41     | 0.0  | 0.0       | 0.0          | true     | false    |

  @TestRailId:C3573
  Scenario: Charge-off added before reversed repayment when loan with interestRecalculation - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "23 January 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "23 January 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 23 January 2024  | 83.4            | 16.6          | 0.41     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.03           | 16.37         | 0.64     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.41           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.69           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.88           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.88         | 0.1      | 0.0  | 0.0       | 16.98 | 0.0   | 0.0        | 0.0  | 16.98       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.03     | 0.0  | 0.0       | 102.03 | 17.01 | 17.01       | 0.0  | 85.02       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 22 January 2024  | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Repayment          | 17.01  | 16.6      | 0.41     | 0.0  | 0.0       | 83.4         | false    | false    |
    When Customer undo "1"th repayment on "23 January 2024"
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
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 22 January 2024  | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Repayment          | 17.01  | 16.6      | 0.41     | 0.0  | 0.0       | 83.4         | true     | false    |
    And Admin does charge-off the loan on "05 January 2024"
    Then LoanBalanceChangedBusinessEvent is created on "23 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.07           | 16.93         | 0.08     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.06           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.05           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 32.04           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 15.03           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 15.03         | 0.0      | 0.0  | 0.0       | 15.03 | 0.0  | 0.0        | 0.0  | 15.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.08     | 0    | 0         | 100.08 | 0    | 0          | 0    | 100.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 05 January 2024  | Accrual            | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Charge-off         | 100.08 | 100.0     | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Repayment          | 17.01  | 16.6      | 0.41     | 0.0  | 0.0       | 83.4         | true     | false    |

  @TestRailId:C3574
  Scenario: Charge-off added before reversed repayment with accruals after run COB when loan with interestRecalculation - UC3
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "23 January 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "23 January 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 23 January 2024  | 83.4            | 16.6          | 0.41     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.03           | 16.37         | 0.64     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.41           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.69           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.88           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.88         | 0.1      | 0.0  | 0.0       | 16.98 | 0.0   | 0.0        | 0.0  | 16.98       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.03     | 0.0  | 0.0       | 102.03 | 17.01 | 17.01       | 0.0  | 85.02       |
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
      | 23 January 2024  | Repayment          | 17.01  | 16.6      | 0.41     | 0.0  | 0.0       | 83.4         | false    | false    |
    When Customer undo "1"th repayment on "23 January 2024"
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
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
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
      | 23 January 2024  | Repayment          | 17.01  | 16.6      | 0.41     | 0.0  | 0.0       | 83.4         | true     | false    |
    And Admin does charge-off the loan on "05 January 2024"
    Then LoanBalanceChangedBusinessEvent is created on "23 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.07           | 16.93         | 0.08     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 66.06           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.05           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 32.04           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 15.03           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 15.03         | 0.0      | 0.0  | 0.0       | 15.03 | 0.0  | 0.0        | 0.0  | 15.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.08     | 0    | 0         | 100.08 | 0    | 0          | 0    | 100.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Charge-off         | 100.08 | 100.0     | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Repayment          | 17.01  | 16.6      | 0.41     | 0.0  | 0.0       | 83.4         | true     | false    |

  @TestRailId:C3575
  Scenario: Charge-off after repayment when loan with interestRecalculation is forbidden - UC4
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "23 January 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "23 January 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 23 January 2024  | 83.4            | 16.6          | 0.41     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.03           | 16.37         | 0.64     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.41           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.69           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.88           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.88         | 0.1      | 0.0  | 0.0       | 16.98 | 0.0   | 0.0        | 0.0  | 16.98       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.03     | 0.0  | 0.0       | 102.03 | 17.01 | 17.01       | 0.0  | 85.02       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 22 January 2024  | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Repayment          | 17.01  | 16.6      | 0.41     | 0.0  | 0.0       | 83.4         | false    | false    |
    Then Charge-off transaction is not possible on "05 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 23 January 2024  | 83.4            | 16.6          | 0.41     | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.03           | 16.37         | 0.64     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.41           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.69           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.88           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.88         | 0.1      | 0.0  | 0.0       | 16.98 | 0.0   | 0.0        | 0.0  | 16.98       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.03     | 0.0  | 0.0       | 102.03 | 17.01 | 17.01       | 0.0  | 85.02       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 22 January 2024  | Accrual            | 0.4    | 0.0       | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Repayment          | 17.01  | 16.6      | 0.41     | 0.0  | 0.0       | 83.4         | false    | false    |

  @TestRailId:C3576
  Scenario: Charge-off before reversed Goodwill Credit when loan is interestRecalculation is forbidden - UC5
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "23 January 2024"
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "23 January 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date  | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |            | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |            | 83.56           | 16.44         | 0.57     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |            | 66.98           | 16.58         | 0.43     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |            | 50.3            | 16.68         | 0.33     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |            | 33.53           | 16.77         | 0.24     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |            | 16.66           | 16.87         | 0.14     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |            | 0.0             | 16.66         | 0.04     | 0.0  | 0.0       | 16.7  | 10.0  | 10.0       | 0.0  | 6.7         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.75     | 0.0  | 0.0       | 101.75 | 10.0  | 10.0       | 0.0  | 91.75       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 23 January 2024  | Goodwill Credit    | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | false    | false    |
    Then Charge-off transaction is not possible on "05 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date  | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |            | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |            | 83.56           | 16.44         | 0.57     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |            | 66.98           | 16.58         | 0.43     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |            | 50.3            | 16.68         | 0.33     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |            | 33.53           | 16.77         | 0.24     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |            | 16.66           | 16.87         | 0.14     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |            | 0.0             | 16.66         | 0.04     | 0.0  | 0.0       | 16.7  | 10.0  | 10.0       | 0.0  | 6.7         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.75     | 0.0  | 0.0       | 101.75 | 10.0  | 10.0       | 0.0  | 91.75       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 23 January 2024  | Goodwill Credit    | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | false    | false    |

  @TestRailId:C3577
  Scenario: Charge-off after charge waived when loan with interestRecalculation is forbidden - UC6
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "23 January 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "23 January 2024" due date and 5 EUR transaction amount
    And Admin waives charge
    Then Charge-off transaction is not possible on "05 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 01 February 2024 |                  | 83.57           | 16.43         | 0.58     | 5.0  | 0.0       | 22.01 | 0.0   | 0.0        | 0.0  | 5.0    | 17.01       |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0    | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0    | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0    | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0    | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 0.0    | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Waived | Outstanding |
      | 100           | 2.05     | 5.0  | 0         | 107.05 | 0.0   | 0          | 0    | 5.0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 23 January 2024  | Waive loan charges | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 23 January 2024 | Flat             | 5.0  | 0.0  | 5.0    | 0.0         |
# --- check if it's forbidden to do charge-off before transaction with later date ---#
    Then Charge-off transaction is not possible on "05 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 01 February 2024 |                  | 83.57           | 16.43         | 0.58     | 5.0  | 0.0       | 22.01 | 0.0   | 0.0        | 0.0  | 5.0    | 17.01       |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0    | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0    | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0    | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0    | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 0.0    | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Waived | Outstanding |
      | 100           | 2.05     | 5.0  | 0         | 107.05 | 0.0   | 0          | 0    | 5.0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 23 January 2024  | Waive loan charges | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 23 January 2024 | Flat             | 5.0  | 0.0  | 5.0    | 0.0         |

  @TestRailId:C3645
  Scenario: Charge-off added before charge and charge waive transactions when loan with interestRecalculation - UC7
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "23 January 2024"
    And Admin adds "LOAN_NSF_FEE" due date charge with "23 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 5.0       | 22.01 | 0.0  | 0.0        | 0.0  | 22.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 5.0       | 107.05 | 0    | 0          | 0    | 107.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 January 2024 | Flat             | 5.0  | 0.0  | 0.0    | 5.0         |
    Then Charge-off transaction is not possible on "05 January 2024" due to monetary activity before
# --- waive charge --- #
    Then Admin waives charge
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 5.0       | 22.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 5.0       | 107.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 23 January 2024  | Waive loan charges | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 January 2024 | Flat             | 5.0  | 0.0  | 5.0    | 0.0         |
    Then Charge-off transaction is not possible on "05 January 2024"
# --- undo waive charge ---#
    Then Admin makes waive undone for charge
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 5.0       | 22.01 | 0.0  | 0.0        | 0.0  | 22.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 5.0       | 107.05 | 0    | 0          | 0    | 107.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 23 January 2024  | Waive loan charges | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
    And Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 January 2024 | Flat             | 5.0  | 0.0  | 0.0    | 5.0         |
    Then Charge-off transaction is not possible on "05 January 2024" due to monetary activity before

  @TestRailId:C3611
  Scenario: As a user I want to verify that backdated transactions are allowed after charge-off and backdated full repayment, interestRecalculation = false
    When Admin sets the business date to "11 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON | 11 March 2025     | 500            | 7                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "11 March 2025" with "500" amount and expected disbursement date on "11 March 2025"
    And Admin successfully disburse the loan on "11 March 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025    |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 11 April 2025    |                  | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 2  | 30   | 11 May 2025      |                  | 335.36          | 82.56         | 2.44     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 3  | 31   | 11 June 2025     |                  | 252.32          | 83.04         | 1.96     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 4  | 30   | 11 July 2025     |                  | 168.79          | 83.53         | 1.47     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 5  | 31   | 11 August 2025   |                  | 84.77           | 84.02         | 0.98     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 6  | 31   | 11 September 2025|                  | 0.0             | 84.77         | 0.49     | 0.0  | 0.0       | 85.26 | 0.0   | 0.0        | 0.0  | 0.0    | 85.26       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Waived | Outstanding |
      | 500.0         | 10.26    | 0.0  | 0         | 510.26 | 0.0   | 0          | 0    | 0.0    | 510.26      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    Then Admin can successfully set Fraud flag to the loan
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "14 April 2025"
    And Admin does charge-off the loan with reason "DELINQUENT" on "14 April 2025"
    Then LoanBalanceChangedBusinessEvent is created on "14 April 2025"
    Then Loan marked as charged-off on "14 April 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025     |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |        |             |
      | 1  | 31   | 11 April 2025     |           | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 2  | 30   | 11 May 2025       |           | 333.16          | 84.76         | 0.24     | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 3  | 31   | 11 June 2025      |           | 248.16          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 4  | 30   | 11 July 2025      |           | 163.16          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 5  | 31   | 11 August 2025    |           | 78.16           | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 6  | 31   | 11 September 2025 |           | 0.0             | 78.16         | 0.0      | 0.0  | 0.0       | 78.16 | 0.0  | 0.0        | 0.0  | 0.0    | 78.16       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Waived | Outstanding |
      | 500.0         | 3.16     | 0.0  | 0         | 503.16 | 0    | 0          | 0    | 0.0    | 503.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 14 April 2025    | Accrual          | 3.16   | 0.0       | 3.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Charge-off       | 503.16 | 500.0     | 3.16     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin runs inline COB job for Loan
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "13 April 2025" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025     |                | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 11 April 2025     | 13 April 2025  | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 85.0  | 0.0        | 85.0 | 0.0    | 0.0         |
      | 2  | 30   | 11 May 2025       | 13 April 2025  | 335.36          | 82.56         | 2.44     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 3  | 31   | 11 June 2025      | 13 April 2025  | 252.32          | 83.04         | 1.96     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 4  | 30   | 11 July 2025      | 13 April 2025  | 168.79          | 83.53         | 1.47     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 5  | 31   | 11 August 2025    | 13 April 2025  | 84.77           | 84.02         | 0.98     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 6  | 31   | 11 September 2025 |                | 0.0             | 84.77         | 0.0      | 0.0  | 0.0       | 84.77 | 75.0  | 75.0       | 0.0  | 0.0    | 9.77        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Waived | Outstanding |
      | 500.0         | 9.77     | 0.0  | 0         | 509.77 | 500.0 | 415.0      | 85.0 | 0.0    | 9.77        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement        | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 13 April 2025    | Repayment           | 500.0  | 490.23    | 9.77     | 0.0  | 0.0       | 9.77         | false    | false    |
      | 14 April 2025    | Accrual             | 3.16   | 0.0       | 3.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual Adjustment  | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Charge-off          | 9.77   | 9.77      | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "13 April 2025" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025     |                | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 11 April 2025     | 13 April 2025  | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 85.0  | 0.0        | 85.0 | 0.0    | 0.0         |
      | 2  | 30   | 11 May 2025       | 13 April 2025  | 335.36          | 82.56         | 2.44     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 3  | 31   | 11 June 2025      | 13 April 2025  | 252.32          | 83.04         | 1.96     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 4  | 30   | 11 July 2025      | 13 April 2025  | 168.79          | 83.53         | 1.47     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 5  | 31   | 11 August 2025    | 13 April 2025  | 84.77           | 84.02         | 0.98     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 6  | 31   | 11 September 2025 | 13 April 2025  | 0.0             | 84.77         | 0.49     | 0.0  | 0.0       | 85.26 | 85.26 | 85.26      | 0.0  | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Waived | Outstanding |
      | 500.0         | 10.26    | 0.0  | 0         | 510.26 | 510.26 | 425.26     | 85.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement            | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 13 April 2025    | Repayment               | 500.0  | 490.23    | 9.77     | 0.0  | 0.0       | 9.77         | false    | false    |
      | 13 April 2025    | Merchant Issued Refund  | 500.0  | 9.77      | 0.49     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                 | 3.16   | 0.0       | 3.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual Adjustment      | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                 | 7.34   | 0.0       | 7.34     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "13 April 2025" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025     |                | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 11 April 2025     | 13 April 2025  | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 85.0  | 0.0        | 85.0 | 0.0    | 0.0         |
      | 2  | 30   | 11 May 2025       | 13 April 2025  | 335.36          | 82.56         | 2.44     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 3  | 31   | 11 June 2025      | 13 April 2025  | 252.32          | 83.04         | 1.96     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 4  | 30   | 11 July 2025      | 13 April 2025  | 168.79          | 83.53         | 1.47     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 5  | 31   | 11 August 2025    | 13 April 2025  | 84.77           | 84.02         | 0.98     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 6  | 31   | 11 September 2025 | 13 April 2025  | 0.0             | 84.77         | 0.49     | 0.0  | 0.0       | 85.26 | 85.26 | 85.26      | 0.0  | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Waived | Outstanding |
      | 500.0         | 10.26    | 0.0  | 0         | 510.26 | 510.26 | 425.26     | 85.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement            | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 13 April 2025    | Repayment               | 500.0  | 490.23    | 9.77     | 0.0  | 0.0       | 9.77         | false    | false    |
      | 13 April 2025    | Merchant Issued Refund  | 500.0  | 9.77      | 0.49     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Goodwill Credit         | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                 | 3.16   | 0.0       | 3.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual Adjustment      | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                 | 7.34   | 0.0       | 7.34     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3612
  Scenario: As a user I want to verify that backdated transactions are allowed after charge-off and backdated full repayment, interestRecalculation = true
    When Admin sets the business date to "11 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC | 11 March 2025     | 500            | 7                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "11 March 2025" with "500" amount and expected disbursement date on "11 March 2025"
    And Admin successfully disburse the loan on "11 March 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025    |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 11 April 2025    |                  | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 2  | 30   | 11 May 2025      |                  | 335.36          | 82.56         | 2.44     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 3  | 31   | 11 June 2025     |                  | 252.32          | 83.04         | 1.96     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 4  | 30   | 11 July 2025     |                  | 168.79          | 83.53         | 1.47     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 5  | 31   | 11 August 2025   |                  | 84.77           | 84.02         | 0.98     | 0.0  | 0.0       | 85.0  | 0.0   | 0.0        | 0.0  | 0.0    | 85.0        |
      | 6  | 31   | 11 September 2025|                  | 0.0             | 84.77         | 0.49     | 0.0  | 0.0       | 85.26 | 0.0   | 0.0        | 0.0  | 0.0    | 85.26       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Waived | Outstanding |
      | 500.0         | 10.26    | 0.0  | 0         | 510.26 | 0.0   | 0          | 0    | 0.0    | 510.26      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    Then Admin can successfully set Fraud flag to the loan
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "14 April 2025"
    And Admin does charge-off the loan with reason "DELINQUENT" on "14 April 2025"
    Then LoanBalanceChangedBusinessEvent is created on "14 April 2025"
    Then Loan marked as charged-off on "14 April 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025     |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |        |             |
      | 1  | 31   | 11 April 2025     |           | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 2  | 30   | 11 May 2025       |           | 333.21          | 84.71         | 0.29     | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 3  | 31   | 11 June 2025      |           | 248.21          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 4  | 30   | 11 July 2025      |           | 163.21          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 5  | 31   | 11 August 2025    |           | 78.21           | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 0.0  | 0.0        | 0.0  | 0.0    | 85.0        |
      | 6  | 31   | 11 September 2025 |           | 0.0             | 78.21         | 0.0      | 0.0  | 0.0       | 78.21 | 0.0  | 0.0        | 0.0  | 0.0    | 78.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Waived | Outstanding |
      | 500.0         | 3.21     | 0.0  | 0         | 503.21 | 0.0  | 0          | 0    | 0.0    | 503.21      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 14 April 2025    | Accrual          | 3.21   | 0.0       | 3.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Charge-off       | 503.21 | 500.0     | 3.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin runs inline COB job for Loan
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "13 April 2025" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025     |                | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 11 April 2025     | 13 April 2025  | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 85.0  | 0.0        | 85.0 | 0.0    | 0.0         |
      | 2  | 30   | 11 May 2025       | 13 April 2025  | 333.12          | 84.8          | 0.2      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 3  | 31   | 11 June 2025      | 13 April 2025  | 248.12          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 4  | 30   | 11 July 2025      | 13 April 2025  | 163.12          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 5  | 31   | 11 August 2025    | 13 April 2025  | 78.12           | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 6  | 31   | 11 September 2025 |                | 0.0             | 78.12         | 0.0      | 0.0  | 0.0       | 78.12 | 75.0  | 75.0       | 0.0  | 0.0    | 3.12        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Waived | Outstanding |
      | 500.0         | 3.12     | 0.0  | 0         | 503.12 | 500.0 | 415.0      | 85.0  | 0.0    | 3.12        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement       | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 13 April 2025    | Repayment          | 500.0  | 496.89    | 3.11     | 0.0  | 0.0       | 3.11         | false    | false    |
      | 14 April 2025    | Accrual            | 3.21   | 0.0       | 3.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual Adjustment | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Charge-off         | 3.12   | 3.11      | 0.01     | 0.0  | 0.0       | 0.0          | false    | true     |
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "13 April 2025" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025     |                | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 11 April 2025     | 13 April 2025  | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 85.0  | 0.0        | 85.0 | 0.0    | 0.0         |
      | 2  | 30   | 11 May 2025       | 13 April 2025  | 333.11          | 84.81         | 0.19     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 3  | 31   | 11 June 2025      | 13 April 2025  | 248.11          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 4  | 30   | 11 July 2025      | 13 April 2025  | 163.11          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 5  | 31   | 11 August 2025    | 13 April 2025  | 78.11           | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 6  | 31   | 11 September 2025 | 13 April 2025  | 0.0             | 78.11         | 0.0      | 0.0  | 0.0       | 78.11 | 78.11 | 78.11      | 0.0  | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Waived | Outstanding |
      | 500.0         | 3.11     | 0.0  | 0         | 503.11 | 503.11 | 418.11     | 85.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement            | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 13 April 2025    | Repayment               | 500.0  | 496.89    | 3.11     | 0.0  | 0.0       | 3.11         | false    | false    |
      | 13 April 2025    | Merchant Issued Refund  | 500.0  | 3.11      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                 | 3.21   | 0.0       | 3.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual Adjustment      | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual Adjustment      | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "13 April 2025" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 11 March 2025     |                | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 11 April 2025     | 13 April 2025  | 417.92          | 82.08         | 2.92     | 0.0  | 0.0       | 85.0  | 85.0  | 0.0        | 85.0 | 0.0    | 0.0         |
      | 2  | 30   | 11 May 2025       | 13 April 2025  | 333.11          | 84.81         | 0.19     | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 3  | 31   | 11 June 2025      | 13 April 2025  | 248.11          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 4  | 30   | 11 July 2025      | 13 April 2025  | 163.11          | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 5  | 31   | 11 August 2025    | 13 April 2025  | 78.11           | 85.0          | 0.0      | 0.0  | 0.0       | 85.0  | 85.0  | 85.0       | 0.0  | 0.0    | 0.0         |
      | 6  | 31   | 11 September 2025 | 13 April 2025  | 0.0             | 78.11         | 0.0      | 0.0  | 0.0       | 78.11 | 78.11 | 78.11      | 0.0  | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Waived | Outstanding |
      | 500.0         | 3.11     | 0.0  | 0         | 503.11 | 503.11 | 418.11     | 85.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 11 March 2025    | Disbursement            | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 13 April 2025    | Repayment               | 500.0  | 496.89    | 3.11     | 0.0  | 0.0       | 3.11         | false    | false    |
      | 13 April 2025    | Merchant Issued Refund  | 500.0  | 3.11      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Goodwill Credit         | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                 | 3.21   | 0.0       | 3.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual Adjustment      | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual Adjustment      | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3618
  Scenario: Charge-off on a fraud loan respects GL mapping based on charge-off reason for buyback
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2024-01-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2024-01-21     | 1                  | PENDING | 2024-01-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 January 2024"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2024-01-21     | 1                  | PENDING | 2024-01-01    | 2024-01-21  | SALE             |
      | 2024-01-21     | 1                  | ACTIVE  | 2024-01-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount |
      | ASSET         | 112601        | Loans Receivable        | CREDIT    | 100.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 2.05   |
      | ASSET         | 146000        | Asset transfer          | DEBIT     | 102.05 |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 100.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 2.05   |
      | ASSET         | 146000        | Asset transfer          | CREDIT    | 102.05 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 100.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 2.05   |
    Then Admin can successfully set Fraud flag to the loan
    When Admin sets the business date to "03 February 2024"
    And Admin does charge-off the loan with reason "DELINQUENT" on "03 February 2024"
    Then LoanBalanceChangedBusinessEvent is created on "03 February 2024"
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 0.61   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 100.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.61  |        |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2024-02-03     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2024-01-21     | 1                  | PENDING | 2024-01-01    | 2024-01-21  | SALE             |
      | 2024-01-21     | 1                  | ACTIVE  | 2024-01-22    | 9999-12-31  | SALE             |
      | 2024-02-03     | 1                  | BUYBACK | 2024-02-03    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "04 May 2024"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2024-01-21     | 1                  | PENDING | 2024-01-01    | 2024-01-21  | SALE             |
      | 2024-01-21     | 1                  | ACTIVE  | 2024-01-22    | 2024-02-03  | SALE             |
      | 2024-02-03     | 1                  | BUYBACK | 2024-02-03    | 2024-02-03  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName              | entryType | amount |
      | EXPENSE       | 744007        | Credit Loss/Bad Debt       | DEBIT     | 100.00 |
      | INCOME        | 404001        | Interest Income Charge Off | DEBIT     | 0.61   |
      | ASSET         | 146000        | Asset transfer             | CREDIT    | 100.61 |
      | EXPENSE       | 744007        | Credit Loss/Bad Debt       | CREDIT    | 100.00 |
      | INCOME        | 404001        | Interest Income Charge Off | CREDIT    | 0.61   |
      | ASSET         | 146000        | Asset transfer             | DEBIT     | 100.61 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName              | entryType | amount |
      | ASSET         | 112601        | Loans Receivable           | DEBIT     | 100.00 |
      | ASSET         | 112603        | Interest/Fee Receivable    | DEBIT     | 2.05   |
      | ASSET         | 112603        | Interest/Fee Receivable    | DEBIT     | 0.23   |
      | INCOME        | 404000        | Interest Income            | CREDIT    | 0.23   |
      | ASSET         | 112601        | Loans Receivable           | CREDIT    | 100.00 |
      | ASSET         | 112603        | Interest/Fee Receivable    | CREDIT    | 0.61   |
      | EXPENSE       | 744007        | Credit Loss/Bad Debt       | DEBIT     | 100.00 |
      | INCOME        | 404001        | Interest Income Charge Off | DEBIT     | 0.61   |
      | EXPENSE       | 744007        | Credit Loss/Bad Debt       | CREDIT    | 100.00 |
      | INCOME        | 404001        | Interest Income Charge Off | CREDIT    | 0.61   |

  @TestRailId:C3619
  Scenario: Verify repayment reversal after charge-off and merchant issued refund
    When Admin sets the business date to "14 March 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL | 14 March 2025     | 900            | 9.9                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "14 March 2025" with "900" amount and expected disbursement date on "14 March 2025"
    And Admin successfully disburse the loan on "14 March 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |           | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 14 April 2025 |           | 677.88          | 222.12        | 7.57     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 2  | 30   | 14 May 2025   |           | 453.71          | 224.17        | 5.52     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 3  | 31   | 14 June 2025  |           | 227.83          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 4  | 30   | 14 July 2025  |           | 0.0             | 227.83        | 1.85     | 0.0  | 0.0       | 229.68 | 0.0  | 0.0        | 0.0  | 229.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.75    | 0.0  | 0.0       | 918.75 | 0.0  | 0.0        | 0.0  | 918.75      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
    When Admin sets the business date to "05 April 2025"
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "05 April 2025" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |              | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 14 April 2025 |              | 677.63          | 222.37        | 7.32     | 0.0  | 0.0       | 229.69 | 100.0  | 100.0      | 0.0  | 129.69      |
      | 2  | 30   | 14 May 2025   |              | 453.45          | 224.18        | 5.51     | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 3  | 31   | 14 June 2025  |              | 227.57          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 4  | 30   | 14 July 2025  |              | 0.0             | 227.57        | 1.85     | 0.0  | 0.0       | 229.42 | 0.0    | 0.0        | 0.0  | 229.42      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 18.49    | 0.0  | 0.0       | 918.49 | 100.0  | 100.0      | 0.0  | 818.49      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
    When Admin sets the business date to "14 April 2025"
    And Admin does charge-off the loan on "14 April 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |              | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 14 April 2025 |              | 677.63          | 222.37        | 7.32     | 0.0  | 0.0       | 229.69 | 100.0  | 100.0      | 0.0  | 129.69      |
      | 2  | 30   | 14 May 2025   |              | 453.45          | 224.18        | 5.51     | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 3  | 31   | 14 June 2025  |              | 227.57          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 4  | 30   | 14 July 2025  |              | 0.0             | 227.57        | 1.85     | 0.0  | 0.0       | 229.42 | 0.0    | 0.0        | 0.0  | 229.42      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 18.49    | 0.0  | 0.0       | 918.49 | 100.0  | 100.0      | 0.0  | 818.49      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 14 April 2025    | Accrual          | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          |
      | 14 April 2025    | Charge-off       | 818.49 | 800.0     | 18.49    | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "15 April 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 April 2025" with 900 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 14 March 2025 |               | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 14 April 2025 | 15 April 2025 | 677.63          | 222.37        | 7.32     | 0.0  | 0.0       | 229.69 | 229.69 | 100.0      | 129.69 | 0.0         |
      | 2  | 30   | 14 May 2025   | 15 April 2025 | 453.45          | 224.18        | 5.51     | 0.0  | 0.0       | 229.69 | 229.69 | 229.69     | 0.0    | 0.0         |
      | 3  | 31   | 14 June 2025  | 15 April 2025 | 227.57          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 229.69 | 229.69     | 0.0    | 0.0         |
      | 4  | 30   | 14 July 2025  | 15 April 2025 | 0.0             | 227.57        | 1.85     | 0.0  | 0.0       | 229.42 | 229.42 | 229.42     | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 900.0         | 18.49    | 0.0  | 0.0       | 918.49 | 918.49 | 788.8      | 129.69 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement           | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 14 April 2025    | Accrual                | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          |
      | 14 April 2025    | Charge-off             | 818.49 | 800.0     | 18.49    | 0.0  | 0.0       | 0.0          |
      | 15 April 2025    | Merchant Issued Refund | 900.0  | 800.0     | 18.49    | 0.0  | 0.0       | 0.0          |
      | 15 April 2025    | Interest Refund        | 18.49  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    When Customer undo "1"th repayment on "05 April 2025"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 14 March 2025 |               | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 14 April 2025 | 15 April 2025 | 677.88          | 222.12        | 7.57     | 0.0  | 0.0       | 229.69 | 229.69 | 0.0        | 229.69 | 0.0         |
      | 2  | 30   | 14 May 2025   | 15 April 2025 | 453.71          | 224.17        | 5.52     | 0.0  | 0.0       | 229.69 | 229.69 | 229.69     | 0.0    | 0.0         |
      | 3  | 31   | 14 June 2025  | 15 April 2025 | 227.83          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 229.69 | 229.69     | 0.0    | 0.0         |
      | 4  | 30   | 14 July 2025  | 15 April 2025 | 0.0             | 227.83        | 1.85     | 0.0  | 0.0       | 229.68 | 229.68 | 229.68     | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 900.0         | 18.75    | 0.0  | 0.0       | 918.75 | 918.75 | 689.06     | 229.69 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2025    | Disbursement           | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 05 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        | true     | false    |
      | 14 April 2025    | Accrual                | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Charge-off             | 918.75 | 900.0     | 18.75    | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 April 2025    | Merchant Issued Refund | 900.0  | 883.1     | 16.9     | 0.0  | 0.0       | 16.9         | false    | true     |
      | 15 April 2025    | Interest Refund        | 18.75  | 16.9      | 1.85     | 0.0  | 0.0       | 0.0          | false    | true     |

  @TestRailId:C3620
  Scenario: Verify repayment reversal after zero interest behaviour charge-off and merchant issued refund
    When Admin sets the business date to "14 March 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF | 14 March 2025     | 900            | 9.9                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "14 March 2025" with "900" amount and expected disbursement date on "14 March 2025"
    And Admin successfully disburse the loan on "14 March 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |           | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 14 April 2025 |           | 677.88          | 222.12        | 7.57     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 2  | 30   | 14 May 2025   |           | 453.71          | 224.17        | 5.52     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 3  | 31   | 14 June 2025  |           | 227.83          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 4  | 30   | 14 July 2025  |           | 0.0             | 227.83        | 1.85     | 0.0  | 0.0       | 229.68 | 0.0  | 0.0        | 0.0  | 229.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.75    | 0.0  | 0.0       | 918.75 | 0.0  | 0.0        | 0.0  | 918.75      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
    When Admin sets the business date to "05 April 2025"
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "05 April 2025" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |              | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 14 April 2025 |              | 677.63          | 222.37        | 7.32     | 0.0  | 0.0       | 229.69 | 100.0  | 100.0      | 0.0  | 129.69      |
      | 2  | 30   | 14 May 2025   |              | 453.45          | 224.18        | 5.51     | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 3  | 31   | 14 June 2025  |              | 227.57          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 4  | 30   | 14 July 2025  |              | 0.0             | 227.57        | 1.85     | 0.0  | 0.0       | 229.42 | 0.0    | 0.0        | 0.0  | 229.42      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 18.49    | 0.0  | 0.0       | 918.49 | 100.0  | 100.0      | 0.0  | 818.49      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
    When Admin sets the business date to "14 April 2025"
    And Admin does charge-off the loan on "14 April 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |              | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 14 April 2025 |              | 677.63          | 222.37        | 7.32     | 0.0  | 0.0       | 229.69 | 100.0  | 100.0      | 0.0  | 129.69      |
      | 2  | 30   | 14 May 2025   |              | 447.94          | 229.69        | 0.0      | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 3  | 31   | 14 June 2025  |              | 218.25          | 229.69        | 0.0      | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 4  | 30   | 14 July 2025  |              | 0.0             | 218.25        | 0.0      | 0.0  | 0.0       | 218.25 | 0.0    | 0.0        | 0.0  | 218.25      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 7.32     | 0.0  | 0.0       | 907.32 | 100.0  | 100.0      | 0.0  | 807.32      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 14 April 2025    | Accrual          | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          |
      | 14 April 2025    | Charge-off       | 807.32 | 800.0     | 7.32     | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "15 April 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 April 2025" with 900 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 14 March 2025 |               | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 14 April 2025 | 15 April 2025 | 677.63          | 222.37        | 7.32     | 0.0  | 0.0       | 229.69 | 229.69 | 100.0      | 129.69 | 0.0         |
      | 2  | 30   | 14 May 2025   | 15 April 2025 | 447.94          | 229.69        | 0.0      | 0.0  | 0.0       | 229.69 | 229.69 | 229.69     | 0.0    | 0.0         |
      | 3  | 31   | 14 June 2025  | 15 April 2025 | 218.25          | 229.69        | 0.0      | 0.0  | 0.0       | 229.69 | 229.69 | 229.69     | 0.0    | 0.0         |
      | 4  | 30   | 14 July 2025  | 15 April 2025 | 0.0             | 218.25        | 0.0      | 0.0  | 0.0       | 218.25 | 218.25 | 218.25     | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 900.0         | 7.32     | 0.0  | 0.0       | 907.32 | 907.32 | 777.63     | 129.69 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement           | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 14 April 2025    | Accrual                | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          |
      | 14 April 2025    | Charge-off             | 807.32 | 800.0     | 7.32     | 0.0  | 0.0       | 0.0          |
      | 15 April 2025    | Merchant Issued Refund | 900.0  | 800.0     | 7.32     | 0.0  | 0.0       | 0.0          |
      | 15 April 2025    | Interest Refund        | 7.32   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    When Customer undo "1"th repayment on "05 April 2025"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 14 March 2025 |               | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 14 April 2025 | 15 April 2025 | 677.88          | 222.12        | 7.57     | 0.0  | 0.0       | 229.69 | 229.69 | 0.0        | 229.69 | 0.0         |
      | 2  | 30   | 14 May 2025   | 15 April 2025 | 448.19          | 229.69        | 0.0      | 0.0  | 0.0       | 229.69 | 229.69 | 229.69     | 0.0    | 0.0         |
      | 3  | 31   | 14 June 2025  | 15 April 2025 | 218.5           | 229.69        | 0.0      | 0.0  | 0.0       | 229.69 | 229.69 | 229.69     | 0.0    | 0.0         |
      | 4  | 30   | 14 July 2025  | 15 April 2025 | 0.0             | 218.5         | 0.0      | 0.0  | 0.0       | 218.5  | 218.5  | 218.5      | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 900.0         | 7.57     | 0.0  | 0.0       | 907.57 | 907.57 | 677.88     | 229.69 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2025    | Disbursement           | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 05 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        | true     | false    |
      | 14 April 2025    | Accrual                | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Charge-off             | 907.57 | 900.0     | 7.57     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 April 2025    | Merchant Issued Refund | 900.0  | 892.43    | 7.57     | 0.0  | 0.0       | 7.57         | false    | true     |
      | 15 April 2025    | Interest Refund        | 7.57   | 7.57      | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |

  @TestRailId:C3621
  Scenario: Verify repayment reversal after accelerate maturity behaviour charge-off and merchant issued refund
    When Admin sets the business date to "14 March 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ACCELERATE_MATURITY_CHARGE_OFF | 14 March 2025     | 900            | 9.9                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "14 March 2025" with "900" amount and expected disbursement date on "14 March 2025"
    And Admin successfully disburse the loan on "14 March 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |           | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 14 April 2025 |           | 677.88          | 222.12        | 7.57     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 2  | 30   | 14 May 2025   |           | 453.71          | 224.17        | 5.52     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 3  | 31   | 14 June 2025  |           | 227.83          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 0.0  | 0.0        | 0.0  | 229.69      |
      | 4  | 30   | 14 July 2025  |           | 0.0             | 227.83        | 1.85     | 0.0  | 0.0       | 229.68 | 0.0  | 0.0        | 0.0  | 229.68      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.75    | 0.0  | 0.0       | 918.75 | 0.0  | 0.0        | 0.0  | 918.75      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
    When Admin sets the business date to "05 April 2025"
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "05 April 2025" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |              | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 14 April 2025 |              | 677.63          | 222.37        | 7.32     | 0.0  | 0.0       | 229.69 | 100.0  | 100.0      | 0.0  | 129.69      |
      | 2  | 30   | 14 May 2025   |              | 453.45          | 224.18        | 5.51     | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 3  | 31   | 14 June 2025  |              | 227.57          | 225.88        | 3.81     | 0.0  | 0.0       | 229.69 | 0.0    | 0.0        | 0.0  | 229.69      |
      | 4  | 30   | 14 July 2025  |              | 0.0             | 227.57        | 1.85     | 0.0  | 0.0       | 229.42 | 0.0    | 0.0        | 0.0  | 229.42      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 18.49    | 0.0  | 0.0       | 918.49 | 100.0  | 100.0      | 0.0  | 818.49      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
    When Admin sets the business date to "14 April 2025"
    And Admin does charge-off the loan on "14 April 2025"
    Then LoanBalanceChangedBusinessEvent is created on "14 April 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2025 |              | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 14 April 2025 |              | 0.0             | 900.0         | 7.32     | 0.0  | 0.0       | 907.32 | 100.0  | 100.0      | 0.0  | 807.32      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 7.32     | 0.0  | 0.0       | 907.32 | 100.0  | 100.0      | 0.0  | 807.32      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 14 April 2025    | Accrual          | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          |
      | 14 April 2025    | Charge-off       | 807.32 | 800.0     | 7.32     | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "15 April 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 April 2025" with 900 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 14 March 2025 |               | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 14 April 2025 | 15 April 2025 | 0.0             | 900.0         | 7.32     | 0.0  | 0.0       | 907.32 | 907.32 | 100.0      | 807.32 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 900.0         | 7.32     | 0.0  | 0.0       | 907.32 | 907.32 | 100.0      | 807.32 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 14 March 2025    | Disbursement           | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        |
      | 05 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 14 April 2025    | Accrual                | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          |
      | 14 April 2025    | Charge-off             | 807.32 | 800.0     | 7.32     | 0.0  | 0.0       | 0.0          |
      | 15 April 2025    | Merchant Issued Refund | 900.0  | 800.0     | 7.32     | 0.0  | 0.0       | 0.0          |
      | 15 April 2025    | Interest Refund        | 7.32   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    When Customer undo "1"th repayment on "05 April 2025"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 14 March 2025 |               | 900.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 14 April 2025 | 15 April 2025 | 0.0             | 900.0         | 7.57     | 0.0  | 0.0       | 907.57 | 907.57 | 0.0        | 907.57 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 900.0         | 7.57     | 0.0  | 0.0       | 907.57 | 907.57 | 0.0        | 907.57 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2025    | Disbursement           | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
      | 05 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        | true     | false    |
      | 14 April 2025    | Accrual                | 7.32   | 0.0       | 7.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Charge-off             | 907.57 | 900.0     | 7.57     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 April 2025    | Merchant Issued Refund | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 April 2025    | Interest Refund        | 7.57   | 0.0       | 7.57     | 0.0  | 0.0       | 0.0          | false    | true     |

