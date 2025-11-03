@InterestPauseFeature
Feature: Loan interest pause on repayment schedule

  @TestRailId:C3475
  Scenario: S1 - pause calculation within same period, interestRecalculation = true
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Create an interest pause period with start date "05 February 2024" and end date "10 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.95           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.61           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.8            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.8          | 0.1      | 0.0  | 0.0       | 16.9  | 0.0   | 0.0        | 0.0  | 16.9        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.95     | 0    | 0         | 101.95 | 17.01 | 0          | 0    | 84.94       |

  @TestRailId:C3476
  Scenario: S2 - pause calculation between two periods, interestRecalculation = true
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Create an interest pause period with start date "10 February 2024" and end date "10 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.69           | 16.88         | 0.13     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.96           | 16.73         | 0.28     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.24           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.42           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.42         | 0.1      | 0.0  | 0.0       | 16.52 | 0.0   | 0.0        | 0.0  | 16.52       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.57     | 0    | 0         | 101.57 | 17.01 | 0          | 0    | 84.56       |

  @TestRailId:C3477
  Scenario: Backdated pause after the repayment
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
    And Create an interest pause period with start date "05 February 2024" and end date "10 February 2024"
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

  @TestRailId:C3478
  Scenario: Multiple pause
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
    And Create an interest pause period with start date "05 February 2024" and end date "10 February 2024"
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
    And Create an interest pause period with start date "10 March 2024" and end date "20 March 2024"
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
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 66.95        | false    | true     |

  @TestRailId:C3479
  Scenario: Interest accrual pause between two periods - UC2
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
    And Create an interest pause period with start date "10 February 2024" and end date "10 March 2024"
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
    When Admin sets the business date to "12 February 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "12 February 2024" with 0.01 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
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
      | 12 February 2024 | Repayment        | 0.01   | 0.01      | 0.0      | 0.0  | 0.0       | 99.99        | false    | false    |
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

  @TestRailId:C3480
  Scenario: Early repayment and interest pause
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
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.0      | 0    | 0         | 102.0  | 17.01 | 17.01      | 0    | 84.99       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 14 January 2024  | Repayment        | 17.01  | 16.77     | 0.24     | 0.0  | 0.0       | 83.23        | false    | false    |
    And Create an interest pause period with start date "15 January 2024" and end date "25 January 2024"
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
      | 100           | 1.82     | 0    | 0         | 101.82 | 17.01 | 17.01      | 0    | 84.81      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 14 January 2024  | Repayment        | 17.01  | 16.77     | 0.24     | 0.0  | 0.0       | 83.23        | false    | false    |

  @TestRailId:C3624
  Scenario: Verify repayment in the middle of interest pause period - UC1
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 27                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 18                | MONTHS                | 1              | MONTHS                 | 18                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Create an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    When Admin sets the business date to "20 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 946.07          | 53.93         | 14.25    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 2  | 31   | 01 June 2025      |           | 899.18          | 46.89         | 21.29    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 3  | 30   | 01 July 2025      |           | 851.23          | 47.95         | 20.23    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 4  | 31   | 01 August 2025    |           | 802.2           | 49.03         | 19.15    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 5  | 31   | 01 September 2025 |           | 752.07          | 50.13         | 18.05    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 6  | 30   | 01 October 2025   |           | 700.81          | 51.26         | 16.92    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 7  | 31   | 01 November 2025  |           | 648.4           | 52.41         | 15.77    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 8  | 30   | 01 December 2025  |           | 594.81          | 53.59         | 14.59    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 9  | 31   | 01 January 2026   |           | 540.01          | 54.8          | 13.38    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 10 | 31   | 01 February 2026  |           | 483.98          | 56.03         | 12.15    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 11 | 28   | 01 March 2026     |           | 426.69          | 57.29         | 10.89    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 12 | 31   | 01 April 2026     |           | 368.11          | 58.58         |  9.6     | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 13 | 30   | 01 May 2026       |           | 308.21          | 59.9          |  8.28    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 14 | 31   | 01 June 2026      |           | 246.96          | 61.25         |  6.93    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 15 | 30   | 01 July 2026      |           | 184.34          | 62.62         |  5.56    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 16 | 31   | 01 August 2026    |           | 120.31          | 64.03         |  4.15    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 17 | 31   | 01 September 2026 |           |  54.84          | 65.47         |  2.71    | 0.0  | 0.0       | 68.18  | 0.0  | 0.0        | 0.0  | 68.18       |
      | 18 | 30   | 01 October 2026   |           |   0.0           | 54.84         |  1.23    | 0.0  | 0.0       | 56.07  | 0.0  | 0.0        | 0.0  | 56.07       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 215.13   | 0.0  | 0.0       | 1215.13 | 0.0  | 0.0        | 0.0  | 1215.13       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "20 April 2025" with 68.18 EUR transaction amount
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025       | 20 April 2025 | 941.57          | 58.43         |  9.75    | 0.0  | 0.0       | 68.18  | 68.18 | 68.18      | 0.0  |  0.0        |
      | 2  | 31   | 01 June 2025      |               | 898.82          | 42.75         | 25.43    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 3  | 30   | 01 July 2025      |               | 850.86          | 47.96         | 20.22    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 4  | 31   | 01 August 2025    |               | 801.82          | 49.04         | 19.14    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 5  | 31   | 01 September 2025 |               | 751.68          | 50.14         | 18.04    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 6  | 30   | 01 October 2025   |               | 700.41          | 51.27         | 16.91    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 7  | 31   | 01 November 2025  |               | 647.99          | 52.42         | 15.76    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 8  | 30   | 01 December 2025  |               | 594.39          | 53.6          | 14.58    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 9  | 31   | 01 January 2026   |               | 539.58          | 54.81         | 13.37    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 10 | 31   | 01 February 2026  |               | 483.54          | 56.04         | 12.14    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 11 | 28   | 01 March 2026     |               | 426.24          | 57.3          | 10.88    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 12 | 31   | 01 April 2026     |               | 367.65          | 58.59         |  9.59    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 13 | 30   | 01 May 2026       |               | 307.74          | 59.91         |  8.27    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 14 | 31   | 01 June 2026      |               | 246.48          | 61.26         |  6.92    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 15 | 30   | 01 July 2026      |               | 183.85          | 62.63         |  5.55    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 16 | 31   | 01 August 2026    |               | 119.81          | 64.04         |  4.14    | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 17 | 31   | 01 September 2026 |               |  54.33          | 65.48         |  2.7     | 0.0  | 0.0       | 68.18  | 0.0   | 0.0        | 0.0  | 68.18       |
      | 18 | 30   | 01 October 2026   |               |   0.0           | 54.33         |  1.22    | 0.0  | 0.0       | 55.55  | 0.0   | 0.0        | 0.0  | 55.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 214.61   | 0.0  | 0.0       | 1214.61 | 68.18 | 68.18      | 0.0  | 1146.43       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Repayment        | 68.18  | 58.43     | 9.75     | 0.0  | 0.0       | 941.57       | false    | false    |
      | 26 April 2025    | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual          | 0.7    | 0.0       | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual          | 0.71   | 0.0       | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3625
  Scenario: Verify a few repayments in the middle of interest pause period - UC2
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_SARP_TILL_PRECLOSE | 01 April 2025     | 1000           | 36                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 240               | DAYS                  | 15             | DAYS                   | 16                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Create an interest pause period with start date "15 April 2025" and end date "05 May 2025"
    When Admin sets the business date to "20 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 16 April 2025     |           | 942.24          | 57.76         | 13.0     | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 2  | 15   | 01 May 2025       |           | 871.48          | 70.76         |  0.0     | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 3  | 15   | 16 May 2025       |           | 810.31          | 61.17         |  9.59    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 4  | 15   | 31 May 2025       |           | 751.7           | 58.61         | 12.15    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 5  | 15   | 15 June 2025      |           | 692.22          | 59.48         | 11.28    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 6  | 15   | 30 June 2025      |           | 631.84          | 60.38         | 10.38    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 7  | 15   | 15 July 2025      |           | 570.56          | 61.28         |  9.48    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 8  | 15   | 30 July 2025      |           | 508.36          | 62.2          |  8.56    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 9  | 15   | 14 August 2025    |           | 445.23          | 63.13         |  7.63    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 10 | 15   | 29 August 2025    |           | 381.15          | 64.08         |  6.68    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 11 | 15   | 13 September 2025 |           | 316.11          | 65.04         |  5.72    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 12 | 15   | 28 September 2025 |           | 250.09          | 66.02         |  4.74    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 13 | 15   | 13 October 2025   |           | 183.08          | 67.01         |  3.75    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 14 | 15   | 28 October 2025   |           | 115.07          | 68.01         |  2.75    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 15 | 15   | 12 November 2025  |           |  46.04          | 69.03         |  1.73    | 0.0  | 0.0       | 70.76  | 0.0  | 0.0        | 0.0  | 70.76       |
      | 16 | 15   | 27 November 2025  |           |   0.0           | 46.04         |  0.69    | 0.0  | 0.0       | 46.73  | 0.0  | 0.0        | 0.0  | 46.73       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 108.13   | 0.0  | 0.0       | 1108.13 | 0.0  | 0.0        | 0.0  | 1108.13     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "20 April 2025" with 70.76 EUR transaction amount
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 70.76 EUR transaction amount
    When Admin sets the business date to "10 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 April 2025     |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 15   | 16 April 2025     | 20 April 2025 | 942.24          | 57.76         | 13.0     | 0.0  | 0.0       | 70.76  | 70.76 | 0.0        | 70.76 | 0.0         |
      | 2  | 15   | 01 May 2025       | 01 May 2025   | 871.48          | 70.76         |  0.0     | 0.0  | 0.0       | 70.76  | 70.76 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 16 May 2025       |               | 810.31          | 61.17          |  9.59    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 4  | 15   | 31 May 2025       |               | 751.7           | 58.61         | 12.15    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 5  | 15   | 15 June 2025      |               | 692.22          | 59.48         | 11.28    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 6  | 15   | 30 June 2025      |               | 631.84          | 60.38         | 10.38    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 7  | 15   | 15 July 2025      |               | 570.56          | 61.28         |  9.48    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 8  | 15   | 30 July 2025      |               | 508.36          | 62.2          |  8.56    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 9  | 15   | 14 August 2025    |               | 445.23          | 63.13         |  7.63    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 10 | 15   | 29 August 2025    |               | 381.15          | 64.08         |  6.68    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 11 | 15   | 13 September 2025 |               | 316.11          | 65.04         |  5.72    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 12 | 15   | 28 September 2025 |               | 250.09          | 66.02         |  4.74    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 13 | 15   | 13 October 2025   |               | 183.08          | 67.01         |  3.75    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 14 | 15   | 28 October 2025   |               | 115.07          | 68.01         |  2.75    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 15 | 15   | 12 November 2025  |               |  46.04          | 69.03         |  1.73    | 0.0  | 0.0       | 70.76  | 0.0   | 0.0        | 0.0   | 70.76       |
      | 16 | 15   | 27 November 2025  |               | 0.0             | 46.04         |  0.69    | 0.0  | 0.0       | 46.73  | 0.0   | 0.0        | 0.0   | 46.73       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 108.13   | 0.0  | 0.0       | 1108.13 | 141.52 | 0.0        | 70.76 | 966.61     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 1.0    | 0.0       | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Repayment        | 70.76  | 57.76     | 13.0     | 0.0  | 0.0       | 942.24       | false    | false    |
      | 01 May 2025      | Repayment        | 70.76  | 70.76     | 0.0      | 0.0  | 0.0       | 871.48       | false    | false    |
      | 06 May 2025      | Accrual          | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual          | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2025      | Accrual          | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2025      | Accrual          | 0.88   | 0.0       | 0.88     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3626
  Scenario: Verify charge with repayment, payout refund and CBR in the middle of interest pause period - UC3
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
     When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_REST_FREQUENCY_DATE | 01 April 2025     | 1000           | 27                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Create an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    When Admin sets the business date to "20 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 834.21          | 165.79        | 14.25    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 2  | 31   | 01 June 2025      |           | 672.94          | 161.27        | 18.77    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 3  | 30   | 01 July 2025      |           | 508.04          | 164.9         | 15.14    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 4  | 31   | 01 August 2025    |           | 339.43          | 168.61        | 11.43    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 5  | 31   | 01 September 2025 |           | 167.03          | 172.4         |  7.64    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 6  | 30   | 01 October 2025   |           | 0.0             | 167.03        |  3.76    | 0.0  | 0.0       | 170.79 | 0.0  | 0.0        | 0.0  | 170.79      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 70.99    | 0.0  | 0.0       | 1070.99 | 0.0  | 0.0        | 0.0  | 1070.99     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "20 April 2025" due date and 25 EUR transaction amount
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 800 EUR transaction amount
    When Admin sets the business date to "22 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "22 April 2025" with 400 EUR transaction amount and self-generated Idempotency key
    When Admin sets the business date to "23 April 2025"
    And Admin runs inline COB job for Loan
    And Admin makes Credit Balance Refund transaction on "23 April 2025" with 164.19 EUR transaction amount
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 May 2025       | 22 April 2025 | 828.65          | 171.35        |  9.75    | 0.0  | 25.0      | 206.1  | 206.1  | 206.1      | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      | 21 April 2025 | 648.61          | 180.04        |  0.0     | 0.0  | 0.0       | 180.04 | 180.04 | 180.04     | 0.0  | 0.0         |
      | 3  | 30   | 01 July 2025      | 21 April 2025 | 468.57          | 180.04        |  0.0     | 0.0  | 0.0       | 180.04 | 180.04 | 180.04     | 0.0  | 0.0         |
      | 4  | 31   | 01 August 2025    | 21 April 2025 | 288.53          | 180.04        |  0.0     | 0.0  | 0.0       | 180.04 | 180.04 | 180.04     | 0.0  | 0.0         |
      | 5  | 31   | 01 September 2025 | 22 April 2025 | 108.49          | 180.04        |  0.0     | 0.0  | 0.0       | 180.04 | 180.04 | 180.04     | 0.0  | 0.0         |
      | 6  | 30   | 01 October 2025   | 22 April 2025 | 0.0             | 108.49        |  1.06    | 0.0  | 0.0       | 109.55 | 109.55 | 109.55     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 10.81    | 0.0  | 25.0      | 1035.81 | 1035.81 | 1035.81    | 0.0  | 0.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement          | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual               | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          | false    | false    |
      | 21 April 2025    | Repayment             | 800.0  | 765.25    | 9.75     | 0.0  | 25.0      | 234.75       | false    | false    |
      | 22 April 2025    | Payout Refund         | 400.0  | 234.75    | 1.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual               | 1.06   | 0.0       | 1.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Credit Balance Refund | 164.19 | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C3627
  Scenario: Verify repayment with reversed charge-off in the middle of interest pause period - UC4
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ACCRUAL_ACTIVITY_POSTING | 01 April 2025     | 1000           | 17                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Create an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    When Admin sets the business date to "20 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 833.94          | 166.06        |  8.97    | 0.0  | 0.0       | 175.03 | 0.0  | 0.0        | 0.0  | 175.03      |
      | 2  | 31   | 01 June 2025      |           | 670.72          | 163.22        | 11.81    | 0.0  | 0.0       | 175.03 | 0.0  | 0.0        | 0.0  | 175.03      |
      | 3  | 30   | 01 July 2025      |           | 505.19          | 165.53        |  9.5     | 0.0  | 0.0       | 175.03 | 0.0  | 0.0        | 0.0  | 175.03      |
      | 4  | 31   | 01 August 2025    |           | 337.32          | 167.87        |  7.16    | 0.0  | 0.0       | 175.03 | 0.0  | 0.0        | 0.0  | 175.03      |
      | 5  | 31   | 01 September 2025 |           | 167.07          | 170.25        |  4.78    | 0.0  | 0.0       | 175.03 | 0.0  | 0.0        | 0.0  | 175.03      |
      | 6  | 30   | 01 October 2025   |           | 0.0             | 167.07        |  2.37    | 0.0  | 0.0       | 169.44 | 0.0  | 0.0        | 0.0  | 169.44      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 44.59    | 0.0  | 0.0       | 1044.59 | 0.0  | 0.0        | 0.0  | 1044.59     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.48   | 0.0       | 0.48     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.48   | 0.0       | 0.48     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 0.48   | 0.0       | 0.48     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "20 April 2025" with 300 EUR transaction amount
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Admin does charge-off the loan on "21 April 2025"
    When Admin sets the business date to "22 April 2025"
    And Admin runs inline COB job for Loan
    And Admin does a charge-off undo the loan
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 May 2025       | 20 April 2025 | 831.11          | 168.89        |  6.14    | 0.0  | 0.0       | 175.03 | 175.03 | 175.03     | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      |               | 668.08          | 163.03        | 12.0     | 0.0  | 0.0       | 175.03 | 124.97 | 124.97     | 0.0  | 50.06       |
      | 3  | 30   | 01 July 2025      |               | 502.51          | 165.57        |  9.46    | 0.0  | 0.0       | 175.03 | 0.0    | 0.0        | 0.0  | 175.03      |
      | 4  | 31   | 01 August 2025    |               | 334.6           | 167.91        |  7.12    | 0.0  | 0.0       | 175.03 | 0.0    | 0.0        | 0.0  | 175.03      |
      | 5  | 31   | 01 September 2025 |               | 164.31          | 170.29        |  4.74    | 0.0  | 0.0       | 175.03 | 0.0    | 0.0        | 0.0  | 175.03      |
      | 6  | 30   | 01 October 2025   |               | 0.0             | 164.31        |  2.33    | 0.0  | 0.0       | 166.64 | 0.0    | 0.0        | 0.0  | 166.64      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 41.79    | 0.0  | 0.0       | 1041.79 | 300.0 | 300.0      | 0.0  | 741.79     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.48   | 0.0       | 0.48     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.48   | 0.0       | 0.48     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 0.48   | 0.0       | 0.48     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 0.47   | 0.0       | 0.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Repayment        | 300.0  | 293.86    | 6.14     | 0.0  | 0.0       | 706.14       | false    | false    |
      | 21 April 2025    | Charge-off       | 741.79 | 706.14    | 35.65    | 0.0  | 0.0       | 0.0          | true     | false    |
      | 26 April 2025    | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3628
  Scenario: Verify MIR with backdated repayment in the middle of interest pause period - UC5
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL" loan product "MERCHANT_ISSUED_REFUND" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL | 01 April 2025     | 1000           | 9.9                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Create an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    When Admin sets the business date to "20 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 749.98          | 250.02        |  5.15    | 0.0  | 0.0       | 255.17 | 0.0  | 0.0        | 0.0  | 255.17      |
      | 2  | 31   | 01 June 2025      |           | 501.12          | 248.86        |  6.31    | 0.0  | 0.0       | 255.17 | 0.0  | 0.0        | 0.0  | 255.17      |
      | 3  | 30   | 01 July 2025      |           | 250.03          | 251.09        |  4.08    | 0.0  | 0.0       | 255.17 | 0.0  | 0.0        | 0.0  | 255.17      |
      | 4  | 31   | 01 August 2025    |           |   0.0           | 250.03        |  2.1     | 0.0  | 0.0       | 252.13 | 0.0  | 0.0        | 0.0  | 252.13      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 17.64    | 0.0  | 0.0       | 1017.64 | 0.0  | 0.0        | 0.0  | 1017.64     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 April 2025" with 600 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "30 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 350 EUR transaction amount
    When Admin sets the business date to "1 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 May 2025       | 21 April 2025 | 748.36          | 251.64        |  3.53    | 0.0  | 0.0       | 255.17 | 255.17 | 255.17     | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      |               | 510.34          | 238.02        |  0.51    | 0.0  | 0.0       | 238.53 | 186.61 | 186.61     | 0.0  | 51.92       |
      | 3  | 30   | 01 July 2025      | 20 April 2025 | 255.17          | 255.17        |  0.0     | 0.0  | 0.0       | 255.17 | 255.17 | 255.17     | 0.0  | 0.0         |
      | 4  | 31   | 01 August 2025    | 20 April 2025 |   0.0           | 255.17        |  0.0     | 0.0  | 0.0       | 255.17 | 255.17 | 255.17     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 4.04     | 0.0  | 0.0       | 1004.04 | 952.12 | 952.12     | 0.0  | 51.92      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Merchant Issued Refund | 600.0  | 600.0     | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
      | 20 April 2025    | Interest Refund        | 2.12   | 2.12      | 0.0      | 0.0  | 0.0       | 397.88       | false    | false    |
      | 21 April 2025    | Repayment              | 350.0  | 346.47    | 3.53     | 0.0  | 0.0       | 51.41        | false    | false    |
      | 26 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual Adjustment     | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C3629
  Scenario: Verify repayment with Goodwill Credit the middle of interest pause period for multidisbursal loan with downpayment - UC6
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 01 April 2025     | 700            | 25                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 150               | DAYS                  | 15             | DAYS                   | 10                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "700" amount and expected disbursement date on "01 April 2025"
    And Admin successfully disburse the loan on "01 April 2025" with "700" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Create an interest pause period with start date "10 April 2025" and end date "05 May 2025"
    When Admin sets the business date to "12 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 11 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |               | 700.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 April 2025     | 01 April 2025 | 525.0           |  175.0        |  0.0     | 0.0  | 0.0       | 175.0  | 175.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 April 2025     |               | 472.36          |  52.64        |  2.92    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 3  | 15   | 01 May 2025       |               | 416.8           |  55.56        |  0.0     | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 4  | 15   | 16 May 2025       |               | 364.42          |  52.38        |  3.18    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 5  | 15   | 31 May 2025       |               | 312.66          |  51.76        |  3.8     | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 6  | 15   | 15 June 2025      |               | 260.36          |  52.3         |  3.26    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 7  | 15   | 30 June 2025      |               | 207.51          |  52.85        |  2.71    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 8  | 15   | 15 July 2025      |               | 154.11          |  53.4         |  2.16    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 9  | 15   | 30 July 2025      |               | 100.16          |  53.95        |  1.61    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 10 | 15   | 14 August 2025    |               |  45.64          |  54.52        |  1.04    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0  | 55.56       |
      | 11 | 15   | 29 August 2025    |               |   0.0           |  45.64        |  0.48    | 0.0  | 0.0       | 46.12  | 0.0   | 0.0        | 0.0  | 46.12       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 700.0         | 21.16    | 0.0  | 0.0       | 721.16  | 175.0 | 0.0        | 0.0  | 546.16     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 April 2025    | Down Payment     | 175.0  | 175.0     | 0.0      | 0.0  | 0.0       | 525.0        | false    | false    |
      | 02 April 2025    | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "12 April 2025" with 25 EUR transaction amount and self-generated Idempotency key
    When Admin sets the business date to "30 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "30 April 2025" with 170 EUR transaction amount
    When Admin sets the business date to "10 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 11 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 April 2025     |               | 700.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 0    | 01 April 2025     | 01 April 2025 | 525.0           |  175.0        |  0.0     | 0.0  | 0.0       | 175.0  | 175.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 April 2025     | 30 April 2025 | 472.36          |  52.64        |  2.92    | 0.0  | 0.0       | 55.56  | 55.56 | 0.0        | 55.56 | 0.0         |
      | 3  | 15   | 01 May 2025       | 30 April 2025 | 416.8           |  55.56        |  0.0     | 0.0  | 0.0       | 55.56  | 55.56 | 55.56      | 0.0   | 0.0         |
      | 4  | 15   | 16 May 2025       | 30 April 2025 | 361.24          |  55.56        |  0.0     | 0.0  | 0.0       | 55.56  | 55.56 | 55.56      | 0.0   | 0.0         |
      | 5  | 15   | 31 May 2025       |               | 311.69          |  49.55        |  6.01    | 0.0  | 0.0       | 55.56  |  3.32 |  3.32      | 0.0   | 52.24       |
      | 6  | 15   | 15 June 2025      |               | 259.12          |  52.57        |  2.99    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0   | 55.56       |
      | 7  | 15   | 30 June 2025      |               | 206.0           |  53.12        |  2.44    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0   | 55.56       |
      | 8  | 15   | 15 July 2025      |               | 152.33          |  53.67        |  1.89    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0   | 55.56       |
      | 9  | 15   | 30 July 2025      |               |  98.1           |  54.23        |  1.33    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0   | 55.56       |
      | 10 | 15   | 14 August 2025    |               |  43.3           |  54.8         |  0.76    | 0.0  | 0.0       | 55.56  | 0.0   | 0.0        | 0.0   | 55.56       |
      | 11 | 15   | 29 August 2025    |               |   0.0           |  43.3         |  0.19    | 0.0  | 0.0       | 43.49  | 25.0  | 25.0       | 0.0   | 18.49       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 700.0         | 18.53    | 0.0  | 0.0       | 718.53  | 370.0 | 139.44     | 55.56 | 348.53      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 April 2025    | Down Payment     | 175.0  | 175.0     | 0.0      | 0.0  | 0.0       | 525.0        | false    | false    |
      | 02 April 2025    | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Goodwill Credit  | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 30 April 2025    | Repayment        | 170.0  | 167.08    | 2.92     | 0.0  | 0.0       | 332.92       | false    | false    |
      | 06 May 2025      | Accrual          | 0.23   | 0.0       | 0.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual          | 0.23   | 0.0       | 0.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2025      | Accrual          | 0.23   | 0.0       | 0.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2025      | Accrual          | 0.23   | 0.0       | 0.23     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3630
  Scenario: Verify repayment with 2nd disbursement the middle of interest pause period for multidisbursal loan  - UC7
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 April 2025     | 1000           | 33                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "700" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Create an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    When Admin sets the business date to "20 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 584.04          | 115.96        | 12.19    | 0.0  | 0.0       | 128.15 | 0.0  | 0.0        | 0.0  | 128.15      |
      | 2  | 31   | 01 June 2025      |           | 471.95          | 112.09        | 16.06    | 0.0  | 0.0       | 128.15 | 0.0  | 0.0        | 0.0  | 128.15      |
      | 3  | 30   | 01 July 2025      |           | 356.78          | 115.17        | 12.98    | 0.0  | 0.0       | 128.15 | 0.0  | 0.0        | 0.0  | 128.15      |
      | 4  | 31   | 01 August 2025    |           | 238.44          | 118.34        | 9.81     | 0.0  | 0.0       | 128.15 | 0.0  | 0.0        | 0.0  | 128.15      |
      | 5  | 31   | 01 September 2025 |           | 116.85          | 121.59        | 6.56     | 0.0  | 0.0       | 128.15 | 0.0  | 0.0        | 0.0  | 128.15      |
      | 6  | 30   | 01 October 2025   |           | 0.0             | 116.85        | 3.21     | 0.0  | 0.0       | 120.06 | 0.0  | 0.0        | 0.0  | 120.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 700.0         | 60.81    | 0.0  | 0.0       | 760.81 | 0.0  | 0.0        | 0.0  | 760.81      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 02 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual           | 0.65   | 0.0       | 0.65     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual           | 0.65   | 0.0       | 0.65     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual           | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "20 April 2025" with 170 EUR transaction amount
    When Admin sets the business date to "22 April 2025"
    And Admin runs inline COB job for Loan
    When Admin successfully disburse the loan on "22 April 2025" with "300" EUR transaction amount
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      |    |      | 22 April 2025     |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 832.32          | 167.68        | 12.95    | 0.0  | 0.0       | 180.63 | 128.15 | 128.15     | 0.0  |  52.48      |
      | 2  | 31   | 01 June 2025      |           | 673.43          | 158.89        | 21.74    | 0.0  | 0.0       | 180.63 | 41.85  | 41.85      | 0.0  | 138.78      |
      | 3  | 30   | 01 July 2025      |           | 511.32          | 162.11        | 18.52    | 0.0  | 0.0       | 180.63 | 0.0    | 0.0        | 0.0  | 180.63      |
      | 4  | 31   | 01 August 2025    |           | 344.75          | 166.57        | 14.06    | 0.0  | 0.0       | 180.63 | 0.0    | 0.0        | 0.0  | 180.63      |
      | 5  | 31   | 01 September 2025 |           | 173.6           | 171.15        |  9.48    | 0.0  | 0.0       | 180.63 | 0.0    | 0.0        | 0.0  | 180.63      |
      | 6  | 30   | 01 October 2025   |           | 0.0             | 173.6         |  4.77    | 0.0  | 0.0       | 178.37 | 0.0    | 0.0        | 0.0  | 178.37      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 81.52    | 0.0  | 0.0       | 1081.52 | 170.0 | 170.0      | 0.0  | 911.52      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 02 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.65   | 0.0       | 0.65     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 0.65   | 0.0       | 0.65     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 0.64   | 0.0       | 0.64     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Repayment        | 170.0  | 161.66    | 8.34     | 0.0  | 0.0       | 538.34       | false    | false    |
      | 22 April 2025    | Disbursement     | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 838.34       | false    | false    |
      | 26 April 2025    | Accrual          | 0.77   | 0.0       | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual          | 0.77   | 0.0       | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual          | 0.77   | 0.0       | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual          | 0.77   | 0.0       | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual          | 0.76   | 0.0       | 0.76     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3631
  Scenario: Verify charge with repayment, payout refund and CBR in the middle of interest pause period for loan with LAST_INSTALLMENT strategy - UC8
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_REST_FREQUENCY_DATE_LAST_INSTALLMENT | 01 April 2025     | 1000           | 27                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    And Create an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    When Admin sets the business date to "20 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 834.21          | 165.79        | 14.25    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 2  | 31   | 01 June 2025      |           | 672.94          | 161.27        | 18.77    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 3  | 30   | 01 July 2025      |           | 508.04          | 164.9         | 15.14    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 4  | 31   | 01 August 2025    |           | 339.43          | 168.61        | 11.43    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 5  | 31   | 01 September 2025 |           | 167.03          | 172.4         |  7.64    | 0.0  | 0.0       | 180.04 | 0.0  | 0.0        | 0.0  | 180.04      |
      | 6  | 30   | 01 October 2025   |           | 0.0             | 167.03        |  3.76    | 0.0  | 0.0       | 170.79 | 0.0  | 0.0        | 0.0  | 170.79      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 70.99    | 0.0  | 0.0       | 1070.99 | 0.0  | 0.0        | 0.0  | 1070.99     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual          | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "20 April 2025" due date and 25 EUR transaction amount
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 800 EUR transaction amount
    When Admin sets the business date to "22 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "22 April 2025" with 400 EUR transaction amount and self-generated Idempotency key
    When Admin sets the business date to "23 April 2025"
    And Admin runs inline COB job for Loan
    And Admin makes Credit Balance Refund transaction on "23 April 2025" with 160.75 EUR transaction amount
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 May 2025       | 22 April 2025 | 830.61          | 169.39        | 10.65    | 0.0  | 25.0      | 205.04 | 205.04 | 205.04     | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      | 22 April 2025 | 720.14          | 110.47        |  0.0     | 0.0  | 0.0       | 110.47 | 110.47 | 110.47     | 0.0  | 0.0         |
      | 3  | 30   | 01 July 2025      | 21 April 2025 | 540.1           | 180.04        |  0.0     | 0.0  | 0.0       | 180.04 | 180.04 | 180.04     | 0.0  | 0.0         |
      | 4  | 31   | 01 August 2025    | 21 April 2025 | 360.06          | 180.04        |  0.0     | 0.0  | 0.0       | 180.04 | 180.04 | 180.04     | 0.0  | 0.0         |
      | 5  | 31   | 01 September 2025 | 21 April 2025 | 180.02          | 180.04        |  0.0     | 0.0  | 0.0       | 180.04 | 180.04 | 180.04     | 0.0  | 0.0         |
      | 6  | 30   | 01 October 2025   | 21 April 2025 | 0.0             | 180.02        |  0.0     | 0.0  | 0.0       | 180.02 | 180.02 | 180.02     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 10.65    | 0.0  | 25.0      | 1035.65 | 1035.65 | 1035.65    | 0.0  | 0.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement          | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual               | 0.75   | 0.0       | 0.75     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual               | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          | false    | false    |
      | 21 April 2025    | Repayment             | 800.0  | 800.0     | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 22 April 2025    | Payout Refund         | 400.0  | 200.0     | 10.65    | 0.0  | 25.0      | 0.0          | false    | false    |
      | 22 April 2025    | Accrual               | 0.9    | 0.0       | 0.9      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Credit Balance Refund | 160.75 | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"

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
    And Create an interest pause period with start date "05 February 2024" and end date "10 February 2024"
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
    And Create an interest pause period with start date "10 February 2024" and end date "10 March 2024"
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
    And Create an interest pause period with start date "05 February 2024" and end date "10 February 2024"
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
    And Create an interest pause period with start date "05 February 2024" and end date "10 February 2024"
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
    And Create an interest pause period with start date "10 March 2024" and end date "20 March 2024"
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
    And Create an interest pause period with start date "05 February 2024" and end date "10 February 2024"
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
    And Create an interest pause period with start date "14 January 2024" and end date "20 February 2024"
    When Admin sets the business date to "5 March 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.22           | 16.78         | 0.23     | 0.0  | 0.0       | 17.01 | 15.0 | 15.0       | 0.0  | 2.01        |
      | 2  | 29   | 01 March 2024    |           | 66.38           | 16.84         | 0.17     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 49.77           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.05           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.23           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.23         | 0.09     | 0.0  | 0.0       | 16.32 | 0.0  | 0.0        | 0.0  | 16.32       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.37     | 0.0  | 0.0       | 101.37 | 15.0 | 15.0       | 0.0  | 86.37       |
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
    And Create an interest pause period with start date "15 January 2024" and end date "25 January 2024"
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
    And Create an interest pause period with start date "01 February 2024" and end date "01 March 2024"
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

  @TestRailId:C3668
  Scenario: Verify interest pause period is forbidden for loan with zero interest rate- UC1
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025      |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
   # And Admin runs inline COB job for Loan
    And Admin is not able to add an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025      |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |

  @TestRailId:C3669
  Scenario: Verify interest pause period is forbidden for loan with zero interest rate with repayment trns - UC2
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025      |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 250.0 EUR transaction amount
    When Admin sets the business date to "01 June 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 June 2025" with 250.0 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025       | 01 May 2025   | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      | 01 June 2025  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 01 July 2025      |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 500.0 | 0.0        | 0.0  | 500.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 May 2025      | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 01 June 2025     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    When Admin sets the business date to "05 June 2025"
    And Admin runs inline COB job for Loan
    And Admin is not able to add an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    And Admin is not able to add an interest pause period with start date "10 May 2025" and end date "20 May 2025"
    And Admin is not able to add an interest pause period with start date "25 April 2025" and end date "05 May 2025"
    And Admin is not able to add an interest pause period with start date "25 May 2025" and end date "03 June 2025"
    And Admin is not able to add an interest pause period with start date "25 June 2025" and end date "05 July 2025"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025       | 01 May 2025   | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      | 01 June 2025  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 01 July 2025      |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 500.0 | 0.0        | 0.0  | 500.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 May 2025      | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 01 June 2025     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |

  @TestRailId:C3670
  Scenario: Verify interest pause period is forbidden for paid-off loan with zero interest rate - UC3
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025      |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 250.0 EUR transaction amount
    When Admin sets the business date to "01 June 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 June 2025" with 250.0 EUR transaction amount
    When Admin sets the business date to "01 July 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 July 2025" with 250.0 EUR transaction amount
    When Admin sets the business date to "01 August 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 August 2025" with 250.0 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025       | 01 May 2025    | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      | 01 June 2025   | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 01 July 2025      | 01 July 2025   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 01 August 2025    | 01 August 2025 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 1000.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 May 2025      | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 01 June 2025     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 July 2025     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 01 August 2025   | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "05 August 2025"
    And Admin runs inline COB job for Loan
    And Admin is not able to add an interest pause period with start date "15 April 2025" and end date "25 April 2025" due to inactive loan status
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025       | 01 May 2025    | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      | 01 June 2025   | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 01 July 2025      | 01 July 2025   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 01 August 2025    | 01 August 2025 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 1000.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 May 2025      | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 01 June 2025     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 July 2025     | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 01 August 2025   | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3671
  Scenario: Verify interest pause period is forbidden for multidisbursal loan with zero interest rate - UC4
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 31   | 01 June 2025      |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 30   | 01 July 2025      |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0  | 0.0        | 0.0  | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0   | 0.0  | 0.0        | 0.0  | 800.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 200.0 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |             | 800.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025       | 01 May 2025 | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 200.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025      |             | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0   | 0.0        | 0.0  | 200.0       |
      | 3  | 30   | 01 July 2025      |             | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0   | 0.0        | 0.0  | 200.0       |
      | 4  | 31   | 01 August 2025    |             | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 0.0   | 0.0        | 0.0  | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0   | 200.0 | 0.0        | 0.0  | 600.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 01 May 2025      | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
    And Admin is not able to add an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    And Admin is not able to add an interest pause period with start date "10 May 2025" and end date "20 May 2025"
    And Admin is not able to add an interest pause period with start date "25 April 2025" and end date "05 May 2025"
    When Admin sets the business date to "05 May 2025"
    And Admin runs inline COB job for Loan
    When Admin successfully disburse the loan on "05 May 2025" with "200" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |             | 800.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025       | 01 May 2025 | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0  | 200.0 | 0.0        | 0.0  |   0.0       |
      |    |      | 05 May 2025       |             | 200.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 31   | 01 June 2025      |             | 533.33          | 266.67        | 0.0      | 0.0  | 0.0       | 266.67 | 0.0   | 0.0        | 0.0  | 266.67      |
      | 3  | 30   | 01 July 2025      |             | 266.66          | 266.67        | 0.0      | 0.0  | 0.0       | 266.67 | 0.0   | 0.0        | 0.0  | 266.67      |
      | 4  | 31   | 01 August 2025    |             | 0.0             | 266.66        | 0.0      | 0.0  | 0.0       | 266.66 | 0.0   | 0.0        | 0.0  | 266.66      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 200.0 | 0.0        | 0.0  | 800.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 01 May 2025      | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 05 May 2025      | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin is not able to add an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    And Admin is not able to add an interest pause period with start date "10 May 2025" and end date "20 May 2025"
    And Admin is not able to add an interest pause period with start date "25 April 2025" and end date "05 May 2025"

  @TestRailId:C3672
  Scenario: Verify interest pause period is forbidden for charged-off loan with zero interest rate - UC5
    When Admin sets the business date to "01 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025      |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0  | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 May 2025" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025       |           | 750.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0  | 0.0  | 0.0        | 0.0  | 270.0       |
      | 2  | 31   | 01 June 2025      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025      |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0  | 0.0  | 0.0        | 0.0  | 1020.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    And Admin is not able to add an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    And Admin is not able to add an interest pause period with start date "10 May 2025" and end date "20 May 2025"
    And Admin is not able to add an interest pause period with start date "25 April 2025" and end date "05 May 2025"
    When Admin sets the business date to "02 May 2025"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "02 May 2025" with 125 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 May 2025       |           | 750.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0  | 125.0 | 0.0        | 125.0 | 145.0       |
      | 2  | 31   | 01 June 2025      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 30   | 01 July 2025      |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0  | 125.0 | 0.0        | 125.0 | 895.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 May 2025      | Merchant Issued Refund | 125.0  | 105.0     | 0.0      | 20.0 | 0.0       |  895.0       | false    | false    |
    And Admin is not able to add an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    And Admin is not able to add an interest pause period with start date "10 May 2025" and end date "20 May 2025"
    And Admin is not able to add an interest pause period with start date "25 April 2025" and end date "05 May 2025"
    When Admin sets the business date to "05 May 2025"
    And Admin does charge-off the loan on "05 May 2025"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 April 2025     |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 May 2025       |           | 750.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0  | 125.0 | 0.0        | 125.0 | 145.0       |
      | 2  | 31   | 01 June 2025      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 30   | 01 July 2025      |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 August 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0  | 125.0 | 0.0        | 125.0 | 895.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 May 2025      | Merchant Issued Refund | 125.0  | 105.0     | 0.0      | 20.0 | 0.0       |  895.0       | false    | false    |
      | 05 May 2025      | Charge-off             | 895.0  | 895.0     | 0.0      |  0.0 | 0.0       |    0.0       | false    | false    |
    And Admin is not able to add an interest pause period with start date "15 April 2025" and end date "25 April 2025"
    And Admin is not able to add an interest pause period with start date "10 May 2025" and end date "20 May 2025"
    And Admin is not able to add an interest pause period with start date "25 April 2025" and end date "05 May 2025"

  @TestRailId:C3722
  Scenario: Verify that the repayment schedule calculated correctly when interest pause added on the 1st day of the first installment
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "02 January 2024"
    And Create an interest pause period with start date "02 January 2024" and end date "10 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 83.42           | 16.58         | 0.43     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |                  | 66.9            | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.28           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.56           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.75           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.75         | 0.1      | 0.0  | 0.0       | 16.85 | 0.0   | 0.0        | 0.0  | 16.85       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.9      | 0    | 0         | 101.9  | 0     | 0          | 0    | 101.9       |
