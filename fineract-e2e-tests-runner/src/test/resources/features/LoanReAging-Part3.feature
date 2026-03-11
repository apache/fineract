@LoanReAgingFeature
Feature: LoanReAging - Part3

  @TestRailId:C4275
  Scenario: Verify Re-aging with interest pause: UC3: Interest handling: DEFAULT, interest pause overlapping re-aging
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Interest pause ---
    When Admin sets the business date to "02 February 2024"
    And Create an interest pause period with start date "10 February 2024" and end date "10 May 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.69           | 16.88         | 0.13     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.68           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 32.67           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 15.8            | 16.87         | 0.14     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 15.8          | 0.09     | 0.0  | 0.0       | 15.89 | 0.0   | 0.0        | 0.0  | 15.89       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.94     | 0.0  | 0.0       | 100.94 | 17.01 | 0.0        | 0.0  | 83.93       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.69           | 16.88         | 0.13     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.68           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 32.67           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 15.8            | 16.87         | 0.14     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 15.8          | 0.09     | 0.0  | 0.0       | 15.89 | 0.0   | 0.0        | 0.0  | 15.89       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.94     | 0.0  | 0.0       | 100.94 | 17.01 | 0.0        | 0.0  | 83.93       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.5            | 14.07         | 0.13     | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 5  | 30   | 01 May 2024       |                  | 55.3            | 14.2          | 0.0      | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 6  | 31   | 01 June 2024      |                  | 41.33           | 13.97         | 0.23     | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 7  | 30   | 01 July 2024      |                  | 27.37           | 13.96         | 0.24     | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 8  | 31   | 01 August 2024    |                  | 13.33           | 14.04         | 0.16     | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.33         | 0.08     | 0.0  | 0.0       | 13.41 | 0.0   | 0.0        | 0.0  | 13.41       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.42     | 0.0  | 0.0       | 101.42 | 17.01 | 0.0        | 0.0  | 84.41       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.7   | 83.57     | 0.13     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4276
  Scenario: Verify Re-aging with interest pause: UC4: Interest handling: DEFAULT, interest pause after re-aging
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
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
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
  #    --- Interest pause ---
    When Admin sets the business date to "02 April 2024"
    And Create an interest pause period with start date "10 April 2024" and end date "10 June 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.06           | 14.19         | 0.11     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 41.76           | 14.3          | 0.0      | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 27.63           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 13.49           | 14.14         | 0.16     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.49         | 0.08     | 0.0  | 0.0       | 13.57 | 0.0   | 0.0        | 0.0  | 13.57       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.08     | 0.0  | 0.0       | 102.08 | 17.01 | 0.0        | 0.0  | 85.07       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |

  @Skip @TestRailId:C4277
  Scenario: Verify Re-aging with interest pause: UC4.1: Interest handling: WAIVE_INTEREST, interest pause after re-aging
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
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
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | WAIVE_INTEREST        |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
  #    --- Interest pause ---
    When Admin sets the business date to "02 April 2024"
    And Create an interest pause period with start date "10 April 2024" and end date "10 June 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.08           | 14.17         | 0.13     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 14.3          | 0.0      | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 27.65           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 13.51           | 14.14         | 0.16     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.51         | 0.08     | 0.0  | 0.0       | 13.59 | 0.0   | 0.0        | 0.0  | 13.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.1      | 0.0  | 0.0       | 102.1 | 17.01 | 0.0        | 0.0  | 85.09       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4280
  Scenario: Verify Re-aging with interest pause: UC5: Interest handling: DEFAULT, backdated interest pause created after re-aging, backdated to after re-aging
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
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
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
  #    --- Interest pause ---
    When Admin sets the business date to "01 July 2024"
    And Create an interest pause period with start date "10 April 2024" and end date "10 June 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.08           | 14.17         | 0.13     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 14.3          | 0.0      | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 27.82           | 13.96         | 0.34     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 13.68           | 14.14         | 0.16     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.68         | 0.08     | 0.0  | 0.0       | 13.76 | 0.0   | 0.0        | 0.0  | 13.76       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.27     | 0.0  | 0.0       | 102.27 | 17.01 | 0.0        | 0.0  | 85.26       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "01 July 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4281
  Scenario: Verify Re-aging with interest pause: UC6: Interest handling: DEFAULT, backdated interest pause created after re-aging, overlapping (start date before re-aging, end date after re-aging)
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
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
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
  #    --- Interest pause ---
    When Admin sets the business date to "01 July 2024"
    And Create an interest pause period with start date "10 February 2024" and end date "10 June 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.5            | 14.07         | 0.13     | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 5  | 30   | 01 May 2024       |                  | 55.3            | 14.2          | 0.0      | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 6  | 31   | 01 June 2024      |                  | 41.1            | 14.2          | 0.0      | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 7  | 30   | 01 July 2024      |                  | 27.24           | 13.86         | 0.34     | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 8  | 31   | 01 August 2024    |                  | 13.2            | 14.04         | 0.16     | 0.0  | 0.0       | 14.2  | 0.0   | 0.0        | 0.0  | 14.2        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.2          | 0.08     | 0.0  | 0.0       | 13.28 | 0.0   | 0.0        | 0.0  | 13.28       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.7   | 83.57     | 0.13     | 0.0  | 0.0       | 0.0          | false    | true     |
#   --- Close loan ---
    When Loan Pay-off is made on "01 July 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4283
  Scenario: Verify Re-aging with interest pause: UC8: Interest handling: DEFAULT, interest pause, fee, payout refund, CBR
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Interest pause ---
    When Admin sets the business date to "02 February 2024"
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.57     | 0.0  | 0.0       | 101.57 | 17.01 | 0.0        | 0.0  | 84.56       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.69           | 16.88         | 0.13     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.96           | 16.73         | 0.28     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.24           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.42           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.42         | 0.1      | 0.0  | 0.0       | 16.52 | 0.0   | 0.0        | 0.0  | 16.52       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.57     | 0.0  | 0.0       | 101.57 | 17.01 | 0.0        | 0.0  | 84.56       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 56.04           | 13.8          | 0.41     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 42.16           | 13.88         | 0.33     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 28.2            | 13.96         | 0.25     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 14.15           | 14.05         | 0.16     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.15         | 0.08     | 0.0  | 0.0       | 14.23 | 0.0   | 0.0        | 0.0  | 14.23       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.29     | 0.0  | 0.0       | 102.29 | 17.01 | 0.0        | 0.0  | 85.28       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- Interest pause ---
    When Admin sets the business date to "02 April 2024"
    And Create an interest pause period with start date "10 April 2024" and end date "10 July 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 55.74           | 14.1          | 0.11     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 41.53           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 27.32           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 13.22           | 14.1          | 0.11     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.22         | 0.08     | 0.0  | 0.0       | 13.3  | 0.0   | 0.0        | 0.0  | 13.3        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.36     | 0.0  | 0.0       | 101.36 | 17.01 | 0.0        | 0.0  | 84.35       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    #  --- Multiple types of transactions ---
    When Admin sets the business date to "15 April 2024"
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 April 2024" due date and 25 EUR transaction amount
    When Admin sets the business date to "16 April 2024"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "16 April 2024" with 40 EUR transaction amount and self-generated Idempotency key
    When Admin sets the business date to "17 April 2024"
    And Customer makes "AUTOPAY" repayment on "17 April 2024" with 80 EUR transaction amount
    When Admin sets the business date to "18 April 2024"
    Then Loan status will be "OVERPAID"
    And Loan has 11.34 overpaid amount
    And Admin makes Credit Balance Refund transaction on "18 April 2024" with 11.34 EUR transaction amount
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 17   | 01 April 2024     | 16 April 2024    | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 14.21 | 0.0        | 14.21 | 0.0         |
      | 5  | 30   | 01 May 2024       | 17 April 2024    | 55.76           | 14.08         | 0.13     | 0.0  | 25.0      | 39.21 | 39.21 | 39.21      | 0.0   | 0.0         |
      | 6  | 31   | 01 June 2024      | 17 April 2024    | 41.55           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 14.21 | 14.21      | 0.0   | 0.0         |
      | 7  | 30   | 01 July 2024      | 17 April 2024    | 27.34           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 14.21 | 14.21      | 0.0   | 0.0         |
      | 8  | 31   | 01 August 2024    | 17 April 2024    | 13.13           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 14.21 | 14.21      | 0.0   | 0.0         |
      | 9  | 31   | 01 September 2024 | 17 April 2024    | 0.0             | 13.13         | 0.0      | 0.0  | 0.0       | 13.13 | 13.13 | 13.13      | 0.0   | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 100.0         | 1.19     | 0.0  | 25.0      | 126.19 | 126.19 | 94.97      | 14.21 | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement          | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment             | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 February 2024 | Accrual Activity      | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age                | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Activity      | 0.48   | 0.0       | 0.48     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2024    | Payout Refund         | 40.0   | 14.52     | 0.48     | 0.0  | 25.0      | 69.05        | false    | false    |
      | 16 April 2024    | Interest Refund       | 0.52   | 0.39      | 0.13     | 0.0  | 0.0       | 68.66        | false    | false    |
      | 17 April 2024    | Repayment             | 80.0   | 68.66     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2024    | Accrual               | 26.19  | 0.0       | 1.19     | 0.0  | 25.0      | 0.0          | false    | false    |
      | 17 April 2024    | Accrual Activity      | 25.13  | 0.0       | 0.13     | 0.0  | 25.0      | 0.0          | false    | false    |
      | 18 April 2024    | Credit Balance Refund | 11.34  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4284
  Scenario: Verify Re-aging with interest pause: UC9: Interest handling: DEFAULT, interest pause, charge-off
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Interest pause ---
    When Admin sets the business date to "02 February 2024"
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.57     | 0.0  | 0.0       | 101.57 | 17.01 | 0.0        | 0.0  | 84.56       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.69           | 16.88         | 0.13     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.96           | 16.73         | 0.28     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.24           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.42           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.42         | 0.1      | 0.0  | 0.0       | 16.52 | 0.0   | 0.0        | 0.0  | 16.52       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.57     | 0.0  | 0.0       | 101.57 | 17.01 | 0.0        | 0.0  | 84.56       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 56.04           | 13.8          | 0.41     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 42.16           | 13.88         | 0.33     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 28.2            | 13.96         | 0.25     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 14.15           | 14.05         | 0.16     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.15         | 0.08     | 0.0  | 0.0       | 14.23 | 0.0   | 0.0        | 0.0  | 14.23       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.29     | 0.0  | 0.0       | 102.29 | 17.01 | 0.0        | 0.0  | 85.28       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- Interest pause ---
    When Admin sets the business date to "02 April 2024"
    And Create an interest pause period with start date "10 April 2024" and end date "10 July 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 55.74           | 14.1          | 0.11     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 41.53           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 27.32           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 13.22           | 14.1          | 0.11     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.22         | 0.08     | 0.0  | 0.0       | 13.3  | 0.0   | 0.0        | 0.0  | 13.3        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.36     | 0.0  | 0.0       | 101.36 | 17.01 | 0.0        | 0.0  | 84.35       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    #  --- Charge-off ---
    When Admin sets the business date to "15 April 2024"
    And Admin does charge-off the loan on "15 April 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024| 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024   | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024   | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                 | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                 | 55.76           | 14.08         | 0.13     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                 | 41.55           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                 | 27.34           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                 | 13.24           | 14.1          | 0.11     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                 | 0.0             | 13.24         | 0.08     | 0.0  | 0.0       | 13.32 | 0.0   | 0.0        | 0.0  | 13.32       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.38     | 0.0  | 0.0       | 101.38 | 17.01 | 0.0        | 0.0  | 84.37       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2024    | Accrual          | 1.19   | 0.0       | 1.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2024    | Charge-off       | 84.37  | 83.57     | 0.8      | 0.0  | 0.0       | 0.0          | false    | false    |
#    --- Charge-off undo ---
    When Admin sets the business date to "16 April 2024"
    And Admin does a charge-off undo the loan
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 55.76           | 14.08         | 0.13     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 41.55           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 27.34           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 13.24           | 14.1          | 0.11     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.24         | 0.08     | 0.0  | 0.0       | 13.32 | 0.0   | 0.0        | 0.0  | 13.32       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.38     | 0.0  | 0.0       | 101.38 | 17.01 | 0.0        | 0.0  | 84.37       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2024    | Accrual          | 1.19   | 0.0       | 1.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2024    | Charge-off       | 84.37  | 83.57     | 0.8      | 0.0  | 0.0       | 0.0          | true     | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "16 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4285
  Scenario: Verify Re-aging with interest pause: UC10: Interest handling: DEFAULT, interest pause update, delete
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Interest pause ---
    When Admin sets the business date to "02 February 2024"
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.57     | 0.0  | 0.0       | 101.57 | 17.01 | 0.0        | 0.0  | 84.56       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.69           | 16.88         | 0.13     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.96           | 16.73         | 0.28     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.24           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.42           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.42         | 0.1      | 0.0  | 0.0       | 16.52 | 0.0   | 0.0        | 0.0  | 16.52       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.57     | 0.0  | 0.0       | 101.57 | 17.01 | 0.0        | 0.0  | 84.56       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 56.04           | 13.8          | 0.41     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 42.16           | 13.88         | 0.33     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 28.2            | 13.96         | 0.25     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 14.15           | 14.05         | 0.16     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.15         | 0.08     | 0.0  | 0.0       | 14.23 | 0.0   | 0.0        | 0.0  | 14.23       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.29     | 0.0  | 0.0       | 102.29 | 17.01 | 0.0        | 0.0  | 85.28       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- Interest pause ---
    When Admin sets the business date to "02 April 2024"
    And Create an interest pause period with start date "10 April 2024" and end date "10 July 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 55.74           | 14.1          | 0.11     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 41.53           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 27.32           | 14.21         | 0.0      | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 13.22           | 14.1          | 0.11     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.22         | 0.08     | 0.0  | 0.0       | 13.3  | 0.0   | 0.0        | 0.0  | 13.3        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.36     | 0.0  | 0.0       | 101.36 | 17.01 | 0.0        | 0.0  | 84.35       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    #  --- Interest pause update ---
    When Admin sets the business date to "15 April 2024"
    And Update the interest pause period with start date "10 April 2024" and end date "25 May 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 55.76           | 14.08         | 0.13     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 41.62           | 14.14         | 0.07     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 27.65           | 13.97         | 0.24     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 13.6            | 14.05         | 0.16     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.6          | 0.08     | 0.0  | 0.0       | 13.68 | 0.0   | 0.0        | 0.0  | 13.68       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.74     | 0.0  | 0.0       | 101.74 | 17.01 | 0.0        | 0.0  | 84.73       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
#    --- Interest pause delete  ---
    When Admin sets the business date to "16 April 2024"
    And Delete the interest pause period
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.84           | 13.73         | 0.48     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 5  | 30   | 01 May 2024       |                  | 56.08           | 13.76         | 0.45     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 6  | 31   | 01 June 2024      |                  | 42.2            | 13.88         | 0.33     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 7  | 30   | 01 July 2024      |                  | 28.24           | 13.96         | 0.25     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 8  | 31   | 01 August 2024    |                  | 14.19           | 14.05         | 0.16     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.19         | 0.08     | 0.0  | 0.0       | 14.27 | 0.0   | 0.0        | 0.0  | 14.27       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.33     | 0.0  | 0.0       | 102.33 | 17.01 | 0.0        | 0.0  | 85.32       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 83.78  | 83.57     | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "16 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4286
  Scenario: Verify Re-aging with loan reschedule: UC1.1: Interest handling: DEFAULT, reschedule - extra terms
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Reschedule before re-age ---
    When Admin sets the business date to "02 February 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 02 February 2024 |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 71.84           | 11.73         | 0.49     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 3  | 31   | 01 April 2024     |                  | 60.04           | 11.8          | 0.42     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 4  | 30   | 01 May 2024       |                  | 48.17           | 11.87         | 0.35     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 5  | 31   | 01 June 2024      |                  | 36.23           | 11.94         | 0.28     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 6  | 30   | 01 July 2024      |                  | 24.22           | 12.01         | 0.21     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 7  | 31   | 01 August 2024    |                  | 12.14           | 12.08         | 0.14     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 12.14         | 0.07     | 0.0  | 0.0       | 12.21 | 0.0   | 0.0        | 0.0  | 12.21       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.54     | 0.0  | 0.0       | 102.54 | 17.01 | 0.0        | 0.0  | 85.53       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 71.84           | 11.73         | 0.49     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 3  | 31   | 01 April 2024     |                  | 60.04           | 11.8          | 0.42     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 4  | 30   | 01 May 2024       |                  | 48.17           | 11.87         | 0.35     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 5  | 31   | 01 June 2024      |                  | 36.23           | 11.94         | 0.28     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 6  | 30   | 01 July 2024      |                  | 24.22           | 12.01         | 0.21     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 7  | 31   | 01 August 2024    |                  | 12.14           | 12.08         | 0.14     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 12.14         | 0.07     | 0.0  | 0.0       | 12.21 | 0.0   | 0.0        | 0.0  | 12.21       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.54     | 0.0  | 0.0       | 102.54 | 17.01 | 0.0        | 0.0  | 85.53       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- Reschedule after re-age ---
    When Admin sets the business date to "02 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2024       | 02 April 2024   |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 11 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 34.01           | 8.38          | 0.25     | 0.0  | 0.0       | 8.63  | 0.0   | 0.0        | 0.0  | 8.63        |
      | 8  | 31   | 01 August 2024    |                  | 25.58           | 8.43          | 0.2      | 0.0  | 0.0       | 8.63  | 0.0   | 0.0        | 0.0  | 8.63        |
      | 9  | 31   | 01 September 2024 |                  | 17.1            | 8.48          | 0.15     | 0.0  | 0.0       | 8.63  | 0.0   | 0.0        | 0.0  | 8.63        |
      | 10 | 30   | 01 October 2024   |                  | 8.57            | 8.53          | 0.1      | 0.0  | 0.0       | 8.63  | 0.0   | 0.0        | 0.0  | 8.63        |
      | 11 | 31   | 01 November 2024  |                  | 0.0             | 8.57          | 0.05     | 0.0  | 0.0       | 8.62  | 0.0   | 0.0        | 0.0  | 8.62        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 3.05     | 0.0  | 0.0       | 103.05 | 17.01 | 0.0        | 0.0  | 86.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

    #    TODO check and unSkip when WAIVE_INTEREST strategy is implemented

  @Skip @TestRailId:C4287
  Scenario: Verify Re-aging with loan reschedule: UC1.2: Interest handling: WAIVE_INTEREST, reschedule - extra terms
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Reschedule ---
    When Admin sets the business date to "02 February 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 02 February 2024 |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 71.84           | 11.73         | 0.49     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 3  | 31   | 01 April 2024     |                  | 60.04           | 11.8          | 0.42     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 4  | 30   | 01 May 2024       |                  | 48.17           | 11.87         | 0.35     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 5  | 31   | 01 June 2024      |                  | 36.23           | 11.94         | 0.28     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 6  | 30   | 01 July 2024      |                  | 24.22           | 12.01         | 0.21     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 7  | 31   | 01 August 2024    |                  | 12.14           | 12.08         | 0.14     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 12.14         | 0.07     | 0.0  | 0.0       | 12.21 | 0.0   | 0.0        | 0.0  | 12.21       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.54     | 0.0  | 0.0       | 102.54 | 17.01 | 0.0        | 0.0  | 85.53       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 71.84           | 11.73         | 0.49     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 3  | 31   | 01 April 2024     |                  | 60.04           | 11.8          | 0.42     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 4  | 30   | 01 May 2024       |                  | 48.17           | 11.87         | 0.35     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 5  | 31   | 01 June 2024      |                  | 36.23           | 11.94         | 0.28     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 6  | 30   | 01 July 2024      |                  | 24.22           | 12.01         | 0.21     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 7  | 31   | 01 August 2024    |                  | 12.14           | 12.08         | 0.14     | 0.0  | 0.0       | 12.22 | 0.0   | 0.0        | 0.0  | 12.22       |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 12.14         | 0.07     | 0.0  | 0.0       | 12.21 | 0.0   | 0.0        | 0.0  | 12.21       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.54     | 0.0  | 0.0       | 102.54 | 17.01 | 0.0        | 0.0  | 85.53       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | WAIVE_INTEREST        |
    # TODO check numbers
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
#  --- Reschedule after re-age ---
    When Admin sets the business date to "02 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2024       | 02 April 2024   |                 |                  |                 | 2          |                 |
# TODO check numbers
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
      | 10 | 30   | 01 October 2024   |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
      | 11 | 31   | 01 November 2024  |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4290
  Scenario: Verify Re-aging with loan reschedule: UC2.1: Interest handling: DEFAULT, reschedule - adjustedDueDate
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Reschedule before re-age ---
    When Admin sets the business date to "02 February 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 02 February 2024 | 15 April 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 74   | 15 April 2024    |                  | 67.76           | 15.81         | 1.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 15 May 2024      |                  | 51.15           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 15 June 2024     |                  | 34.44           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 15 July 2024     |                  | 17.63           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 15 August 2024   |                  | 0.0             | 17.63         | 0.1      | 0.0  | 0.0       | 17.73 | 0.0   | 0.0        | 0.0  | 17.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.01 | 0.0        | 0.0  | 85.77       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 74   | 15 April 2024    |                  | 67.76           | 15.81         | 1.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 15 May 2024      |                  | 51.15           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 15 June 2024     |                  | 34.44           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 15 July 2024     |                  | 17.63           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 15 August 2024   |                  | 0.0             | 17.63         | 0.1      | 0.0  | 0.0       | 17.73 | 0.0   | 0.0        | 0.0  | 17.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.01 | 0.0        | 0.0  | 85.77       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 43   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.27  | 83.57     | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- Reschedule after re-age ---
    When Admin sets the business date to "02 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 May 2024        | 02 April 2024   | 15 June 2024    |                  |                 |            |                 |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 43   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 75   | 15 June 2024      |                  | 56.96           | 13.29         | 1.01     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 15 July 2024      |                  | 42.99           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 15 August 2024    |                  | 28.94           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 15 September 2024 |                  | 14.81           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 30   | 15 October 2024   |                  | 0.0             | 14.81         | 0.09     | 0.0  | 0.0       | 14.9  | 0.0   | 0.0        | 0.0  | 14.9        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 3.41     | 0.0  | 0.0       | 103.41 | 17.01 | 0.0        | 0.0  | 86.4        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.27  | 83.57     | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  #    TODO check and unSkip when WAIVE_INTEREST strategy is implemented

  @Skip @TestRailId:C4291
  Scenario: Verify Re-aging with loan reschedule: UC2.2: Interest handling: WAIVE_INTEREST, reschedule - adjustedDueDate
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Reschedule before re-age ---
    When Admin sets the business date to "02 February 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 02 February 2024 | 15 April 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 74   | 15 April 2024    |                  | 67.76           | 15.81         | 1.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 15 May 2024      |                  | 51.15           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 15 June 2024     |                  | 34.44           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 15 July 2024     |                  | 17.63           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 15 August 2024   |                  | 0.0             | 17.63         | 0.1      | 0.0  | 0.0       | 17.73 | 0.0   | 0.0        | 0.0  | 17.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.01 | 0.0        | 0.0  | 85.77       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 74   | 15 April 2024    |                  | 67.76           | 15.81         | 1.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 15 May 2024      |                  | 51.15           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 15 June 2024     |                  | 34.44           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 15 July 2024     |                  | 17.63           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 15 August 2024   |                  | 0.0             | 17.63         | 0.1      | 0.0  | 0.0       | 17.73 | 0.0   | 0.0        | 0.0  | 17.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.01 | 0.0        | 0.0  | 85.77       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | WAIVE_INTEREST        |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 43   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.27  | 83.57     | 0.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- Reschedule after re-age ---
    When Admin sets the business date to "02 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 May 2024        | 02 April 2024   | 15 June 2024    |                  |                 |            |                 |
# TODO check numbers
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 43   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 31   | 15 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 15 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 15 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 15 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
      | 8  | 31   | 15 October 2024   |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4294
  Scenario: Verify Re-aging with loan reschedule: UC3: Interest handling: DEFAULT, reschedule - newInterestRate
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Reschedule before re-age ---
    When Admin sets the business date to "02 February 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 02 February 2024 |                 |                  |                 |            | 10              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.97           | 16.6          | 0.49     | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
      | 3  | 31   | 01 April 2024    |                  | 50.44           | 16.53         | 0.56     | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
      | 4  | 30   | 01 May 2024      |                  | 33.77           | 16.67         | 0.42     | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
      | 5  | 31   | 01 June 2024     |                  | 16.96           | 16.81         | 0.28     | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.96         | 0.14     | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0  | 17.1        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.47     | 0.0  | 0.0       | 102.47 | 17.01 | 0.0        | 0.0  | 85.46       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.97           | 16.6          | 0.49     | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
      | 3  | 31   | 01 April 2024    |                  | 50.44           | 16.53         | 0.56     | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
      | 4  | 30   | 01 May 2024      |                  | 33.77           | 16.67         | 0.42     | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
      | 5  | 31   | 01 June 2024     |                  | 16.96           | 16.81         | 0.28     | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.96         | 0.14     | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0  | 17.1        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.47     | 0.0  | 0.0       | 102.47 | 17.01 | 0.0        | 0.0  | 85.46       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.33           | 13.24         | 1.18     | 0.0  | 0.0       | 14.42 | 0.0   | 0.0        | 0.0  | 14.42       |
      | 5  | 30   | 01 May 2024       |                  | 56.5            | 13.83         | 0.59     | 0.0  | 0.0       | 14.42 | 0.0   | 0.0        | 0.0  | 14.42       |
      | 6  | 31   | 01 June 2024      |                  | 42.55           | 13.95         | 0.47     | 0.0  | 0.0       | 14.42 | 0.0   | 0.0        | 0.0  | 14.42       |
      | 7  | 30   | 01 July 2024      |                  | 28.48           | 14.07         | 0.35     | 0.0  | 0.0       | 14.42 | 0.0   | 0.0        | 0.0  | 14.42       |
      | 8  | 31   | 01 August 2024    |                  | 14.3            | 14.18         | 0.24     | 0.0  | 0.0       | 14.42 | 0.0   | 0.0        | 0.0  | 14.42       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.3          | 0.12     | 0.0  | 0.0       | 14.42 | 0.0   | 0.0        | 0.0  | 14.42       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 3.53     | 0.0  | 0.0       | 103.53 | 17.01 | 0.0        | 0.0  | 86.52       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.37  | 83.57     | 0.8      | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- Reschedule after re-age ---
    When Admin sets the business date to "02 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 May 2024        | 02 April 2024   |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.33           | 13.24         | 1.18     | 0.0  | 0.0       | 14.42 | 0.0   | 0.0        | 0.0  | 14.42       |
      | 5  | 30   | 01 May 2024       |                  | 56.45           | 13.88         | 0.59     | 0.0  | 0.0       | 14.47 | 0.0   | 0.0        | 0.0  | 14.47       |
      | 6  | 31   | 01 June 2024      |                  | 42.54           | 13.91         | 0.56     | 0.0  | 0.0       | 14.47 | 0.0   | 0.0        | 0.0  | 14.47       |
      | 7  | 30   | 01 July 2024      |                  | 28.5            | 14.04         | 0.43     | 0.0  | 0.0       | 14.47 | 0.0   | 0.0        | 0.0  | 14.47       |
      | 8  | 31   | 01 August 2024    |                  | 14.31           | 14.19         | 0.28     | 0.0  | 0.0       | 14.47 | 0.0   | 0.0        | 0.0  | 14.47       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.31         | 0.14     | 0.0  | 0.0       | 14.45 | 0.0   | 0.0        | 0.0  | 14.45       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 3.76     | 0.0  | 0.0       | 103.76 | 17.01 | 0.0        | 0.0  | 86.75       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.37  | 83.57     | 0.8      | 0.0  | 0.0       | 0.0          | false    | false    |
# --- close the loan --- #
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4295
  Scenario: Verify Re-aging with loan reschedule: UC4: Interest handling: DEFAULT, reschedule, N+1 installment
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- First installment paid ---
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- N+1 installment ---
    When Admin sets the business date to "02 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "25 December 2024" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 7  | 177  | 25 December 2024 |                  | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 20.0      | 122.05 | 17.01 | 0.0        | 0.0  | 105.04      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- Check before re-age ---
    When Admin sets the business date to "15 March 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 7  | 177  | 25 December 2024 |                  | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 20.0      | 122.05 | 17.01 | 0.0        | 0.0  | 105.04      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#   --- Re-age transaction ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 10 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
      | 10 | 115  | 25 December 2024  |                  | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 20.0      | 122.8 | 17.01 | 0.0        | 0.0  | 105.79      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    #    --- N+1 installment after re-age ---
    When Admin sets the business date to "02 April 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 November 2024" due date and 15 EUR transaction amount
    Then Loan Repayment schedule has 10 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
      | 10 | 115  | 25 December 2024  |                  | 0.0             | 0.0           | 0.0      | 15.0 | 20.0      | 35.0  | 0.0   | 0.0        | 0.0  | 35.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 15.0 | 20.0      | 137.8 | 17.01 | 0.0        | 0.0  | 120.79      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Close loan ---
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4296 @AdvancedPaymentAllocation
  Scenario: Verify allowing Re-aging on interest bearing loan - Interest calculation: Default Behavior - due date and frequency changed from MONTHS to DAYS - Interest Split scenario - UC13.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 15              | DAYS          | 01 April 2024| 6                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024    |                  | 70.36           | 13.21         | 0.98     | 0.0  | 0.0       | 14.19 | 0.0   | 0.0        | 0.0  | 14.19       |
      | 5  | 15   | 16 April 2024    |                  | 56.38           | 13.98         | 0.21     | 0.0  | 0.0       | 14.19 | 0.0   | 0.0        | 0.0  | 14.19       |
      | 6  | 15   | 01 May 2024      |                  | 42.35           | 14.03         | 0.16     | 0.0  | 0.0       | 14.19 | 0.0   | 0.0        | 0.0  | 14.19       |
      | 7  | 15   | 16 May 2024      |                  | 28.28           | 14.07         | 0.12     | 0.0  | 0.0       | 14.19 | 0.0   | 0.0        | 0.0  | 14.19       |
      | 8  | 15   | 31 May 2024      |                  | 14.17           | 14.11         | 0.08     | 0.0  | 0.0       | 14.19 | 0.0   | 0.0        | 0.0  | 14.19       |
      | 9  | 15   | 15 June 2024     |                  | 0.0             | 14.17         | 0.04     | 0.0  | 0.0       | 14.21 | 0.0   | 0.0        | 0.0  | 14.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.17     | 0.0  | 0.0       | 102.17 | 17.01 | 0.0        | 0.0  | 85.16       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4297 @AdvancedPaymentAllocation
  Scenario: Verify allowing Re-aging on interest bearing loan - Interest calculation: Default Behavior - due date and frequency changed from DAYS to MONTHS - Fees and Interest Split before re-aging - UC13.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 90                | DAYS                  | 15             | DAYS                   | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 15   | 16 January 2024  |           | 83.45           | 16.55         | 0.29     | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 2  | 15   | 31 January 2024  |           | 66.85           | 16.6          | 0.24     | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 3  | 15   | 15 February 2024 |           | 50.2            | 16.65         | 0.19     | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 4  | 15   | 01 March 2024    |           | 33.51           | 16.69         | 0.15     | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 5  | 15   | 16 March 2024    |           | 16.77           | 16.74         | 0.1      | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 6  | 15   | 31 March 2024    |           | 0.0             | 16.77         | 0.05     | 0.0  | 0.0       | 16.82 | 0.0  | 0.0        | 0.0  | 16.82       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.02     | 0.0  | 0.0       | 101.02 | 0.0  | 0.0        | 0.0  | 101.02      |
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 16.98 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2024" due date and 10 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date   | 20 January 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin sets the business date to "10 February 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 1               | MONTHS        | 01 March 2024| 5                    |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 15   | 16 January 2024  | 16 January 2024  | 83.45           | 16.55         | 0.29     | 0.0  | 0.0       | 16.84 | 16.84 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 31 January 2024  |                  | 83.31           | 0.14          | 0.0      | 10.0 | 0.0       | 10.14 | 0.14  | 0.14       | 0.0  | 10.0        |
      | 3  | 10   | 10 February 2024 | 10 February 2024 | 83.31           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 20   | 01 March 2024    |                  | 67.03           | 16.28         | 0.72     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 01 April 2024    |                  | 50.43           | 16.6          | 0.4      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 01 May 2024      |                  | 33.72           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 7  | 31   | 01 June 2024     |                  | 16.92           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 8  | 30   | 01 July 2024     |                  | 0.0             | 16.92         | 0.1      | 0.0  | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.0      | 10.0 | 0.0       | 112.0  | 16.98 | 0.14       | 0.0  | 95.02       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 16 January 2024  | Repayment        | 16.98  | 16.69     | 0.29     | 0.0  | 0.0       | 83.31        | false    | false    |
      | 10 February 2024 | Re-age           | 83.71  | 83.31     | 0.4      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date   | 20 January 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Loan Pay-off is made on "10 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4298 @AdvancedPaymentAllocation
  Scenario: Verify allowing Re-aging on interest bearing loan - Interest calculation: Default Behavior - due date and frequency changed from MONTHS to WEEKS - Charge-back before re-aging - UC13.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "05 February 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    When Admin sets the business date to "20 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 2               | WEEKS         | 01 April 2024| 8                    |
    Then Loan Repayment schedule has 11 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 20 March 2024    | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 19   | 20 March 2024    | 20 March 2024    | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 12   | 01 April 2024    |                  | 88.9            | 11.1          | 1.74     | 0.0  | 0.0       | 12.84 | 0.0   | 0.0        | 0.0  | 12.84       |
      | 5  | 14   | 15 April 2024    |                  | 76.3            | 12.6          | 0.24     | 0.0  | 0.0       | 12.84 | 0.0   | 0.0        | 0.0  | 12.84       |
      | 6  | 14   | 29 April 2024    |                  | 63.67           | 12.63         | 0.21     | 0.0  | 0.0       | 12.84 | 0.0   | 0.0        | 0.0  | 12.84       |
      | 7  | 14   | 13 May 2024      |                  | 51.0            | 12.67         | 0.17     | 0.0  | 0.0       | 12.84 | 0.0   | 0.0        | 0.0  | 12.84       |
      | 8  | 14   | 27 May 2024      |                  | 38.29           | 12.71         | 0.13     | 0.0  | 0.0       | 12.84 | 0.0   | 0.0        | 0.0  | 12.84       |
      | 9  | 14   | 10 June 2024     |                  | 25.55           | 12.74         | 0.1      | 0.0  | 0.0       | 12.84 | 0.0   | 0.0        | 0.0  | 12.84       |
      | 10 | 14   | 24 June 2024     |                  | 12.78           | 12.77         | 0.07     | 0.0  | 0.0       | 12.84 | 0.0   | 0.0        | 0.0  | 12.84       |
      | 11 | 14   | 08 July 2024     |                  | 0.0             | 12.78         | 0.03     | 0.0  | 0.0       | 12.81 | 0.0   | 0.0        | 0.0  | 12.81       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.43        | 3.27     | 0.0  | 0.0       | 119.7  | 17.01 | 0.0        | 0.0  | 102.69      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 05 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 100.0        | false    | false    |
      | 20 March 2024    | Re-age           | 101.51 | 100.0     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "20 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4299 @AdvancedPaymentAllocation
  Scenario:Verify allowing Re-aging on interest bearing loan - Interest calculation: Default Behavior - due date and frequency changed - N+1 Scenario - UC13.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "25 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 2               | MONTHS        | 01 May 2024  | 3                    |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 24   | 25 March 2024    | 25 March 2024    | 67.05           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 37   | 01 May 2024      |                  | 44.95           | 22.1          | 0.77     | 0.0  | 0.0       | 22.87 | 0.0   | 0.0        | 0.0  | 22.87       |
      | 5  | 61   | 01 July 2024     |                  | 22.6            | 22.35         | 0.52     | 0.0  | 0.0       | 22.87 | 0.0   | 0.0        | 0.0  | 22.87       |
      | 6  | 62   | 01 September 2024|                  | 0.0             | 22.6          | 0.26     | 0.0  | 0.0       | 22.86 | 0.0   | 0.0        | 0.0  | 22.86       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.62     | 0.0  | 0.0       | 102.62 | 34.02 | 0.0        | 0.0  | 68.6        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 25 March 2024    | Re-age           | 67.35  | 67.05     | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "25 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4300 @AdvancedPaymentAllocation
  Scenario: Verify allowing Re-aging on interest bearing loan - Interest calculation: Default Behavior - due date and frequency changed from WEEKS to DAYS - Partially paid installment - UC13.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | WEEKS                 | 2              | WEEKS                  | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 14   | 15 January 2024  |           | 83.44           | 16.56         | 0.27     | 0.0  | 0.0       | 16.83 | 0.0  | 0.0        | 0.0  | 16.83       |
      | 2  | 14   | 29 January 2024  |           | 66.84           | 16.6          | 0.23     | 0.0  | 0.0       | 16.83 | 0.0  | 0.0        | 0.0  | 16.83       |
      | 3  | 14   | 12 February 2024 |           | 50.19           | 16.65         | 0.18     | 0.0  | 0.0       | 16.83 | 0.0  | 0.0        | 0.0  | 16.83       |
      | 4  | 14   | 26 February 2024 |           | 33.5            | 16.69         | 0.14     | 0.0  | 0.0       | 16.83 | 0.0  | 0.0        | 0.0  | 16.83       |
      | 5  | 14   | 11 March 2024    |           | 16.76           | 16.74         | 0.09     | 0.0  | 0.0       | 16.83 | 0.0  | 0.0        | 0.0  | 16.83       |
      | 6  | 14   | 25 March 2024    |           | 0.0             | 16.76         | 0.05     | 0.0  | 0.0       | 16.81 | 0.0  | 0.0        | 0.0  | 16.81       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.96     | 0.0  | 0.0       | 100.96 | 0.0  | 0.0        | 0.0  | 100.96      |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 16.86 EUR transaction amount
    When Admin sets the business date to "29 January 2024"
    And Customer makes "AUTOPAY" repayment on "29 January 2024" with 10.0 EUR transaction amount
    When Admin sets the business date to "20 February 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 10              | DAYS          | 01 March 2024| 5                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 14   | 15 January 2024  | 15 January 2024  | 83.44           | 16.56         | 0.27     | 0.0  | 0.0       | 16.83 | 16.83 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 29 January 2024  | 20 February 2024 | 73.64           | 9.8           | 0.23     | 0.0  | 0.0       | 10.03 | 10.03 | 0.03       | 0.0  | 0.0         |
      | 3  | 14   | 12 February 2024 | 20 February 2024 | 73.64           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 8    | 20 February 2024 | 20 February 2024 | 73.64           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 5  | 10   | 01 March 2024    |                  | 59.21           | 14.43         | 0.45     | 0.0  | 0.0       | 14.88 | 0.0   | 0.0        | 0.0  | 14.88       |
      | 6  | 10   | 11 March 2024    |                  | 44.45           | 14.76         | 0.12     | 0.0  | 0.0       | 14.88 | 0.0   | 0.0        | 0.0  | 14.88       |
      | 7  | 10   | 21 March 2024    |                  | 29.66           | 14.79         | 0.09     | 0.0  | 0.0       | 14.88 | 0.0   | 0.0        | 0.0  | 14.88       |
      | 8  | 10   | 31 March 2024    |                  | 14.84           | 14.82         | 0.06     | 0.0  | 0.0       | 14.88 | 0.0   | 0.0        | 0.0  | 14.88       |
      | 9  | 10   | 10 April 2024    |                  | 0.0             | 14.84         | 0.03     | 0.0  | 0.0       | 14.87 | 0.0   | 0.0        | 0.0  | 14.87       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.25     | 0.0  | 0.0       | 101.25 | 26.86 | 0.03       | 0.0  | 74.39       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 16.86  | 16.59     | 0.27     | 0.0  | 0.0       | 83.41        | false    | false    |
      | 29 January 2024  | Repayment        | 10.0   | 9.77      | 0.23     | 0.0  | 0.0       | 73.64        | false    | false    |
      | 20 February 2024 | Re-age           | 73.95  | 73.64     | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "20 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4301 @AdvancedPaymentAllocation
  Scenario: Verify allowing Re-aging on interest bearing loan - Interest calculation: Default Behavior - due date and frequency changed (15 to 30 DAYS) - Multiple installments paid - UC13.6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 90                | DAYS                  | 15             | DAYS                   | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 15   | 16 January 2024  |           | 83.45           | 16.55         | 0.29     | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 2  | 15   | 31 January 2024  |           | 66.85           | 16.6          | 0.24     | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 3  | 15   | 15 February 2024 |           | 50.2            | 16.65         | 0.19     | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 4  | 15   | 01 March 2024    |           | 33.51           | 16.69         | 0.15     | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 5  | 15   | 16 March 2024    |           | 16.77           | 16.74         | 0.1      | 0.0  | 0.0       | 16.84 | 0.0  | 0.0        | 0.0  | 16.84       |
      | 6  | 15   | 31 March 2024    |           | 0.0             | 16.77         | 0.05     | 0.0  | 0.0       | 16.82 | 0.0  | 0.0        | 0.0  | 16.82       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.02     | 0.0  | 0.0       | 101.02 | 0.0  | 0.0        | 0.0  | 101.02      |
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 16.98 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 16.98 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 16.98 EUR transaction amount
    When Admin sets the business date to "10 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 30              | DAYS          | 01 April 2024| 2                    |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 15   | 16 January 2024  | 16 January 2024  | 83.45           | 16.55         | 0.29     | 0.0  | 0.0       | 16.84 | 16.84 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 31 January 2024  | 31 January 2024  | 66.85           | 16.6          | 0.24     | 0.0  | 0.0       | 16.84 | 16.84 | 0.14       | 0.0  | 0.0         |
      | 3  | 15   | 15 February 2024 | 15 February 2024 | 50.2            | 16.65         | 0.19     | 0.0  | 0.0       | 16.84 | 16.84 | 0.28       | 0.0  | 0.0         |
      | 4  | 15   | 01 March 2024    | 10 March 2024    | 49.78           | 0.42          | 0.0      | 0.0  | 0.0       | 0.42  | 0.42  | 0.42       | 0.0  | 0.0         |
      | 5  | 9    | 10 March 2024    | 10 March 2024    | 49.78           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 6  | 22   | 01 April 2024    |                  | 25.04           | 24.74         | 0.45     | 0.0  | 0.0       | 25.19 | 0.0   | 0.0        | 0.0  | 25.19       |
      | 7  | 30   | 01 May 2024      |                  | 0.0             | 25.04         | 0.15     | 0.0  | 0.0       | 25.19 | 0.0   | 0.0        | 0.0  | 25.19       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.32     | 0.0  | 0.0       | 101.32 | 50.94 | 0.84       | 0.0  | 50.38       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 16 January 2024  | Repayment        | 16.98  | 16.69     | 0.29     | 0.0  | 0.0       | 83.31        | false    | false    |
      | 31 January 2024  | Repayment        | 16.98  | 16.74     | 0.24     | 0.0  | 0.0       | 66.57        | false    | false    |
      | 15 February 2024 | Repayment        | 16.98  | 16.79     | 0.19     | 0.0  | 0.0       | 49.78        | false    | false    |
      | 10 March 2024    | Re-age           | 50.02  | 49.78     | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "10 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4302 @AdvancedPaymentAllocation
  Scenario: Verify allowing Re-aging on interest bearing loan - Interest calculation: Default Behavior - due date and frequency changed from DAYS to WEEKS with penalties - UC13.7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 60                | DAYS                  | 10             | DAYS                   | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 10   | 11 January 2024  |           | 83.41           | 16.59         | 0.19     | 0.0  | 0.0       | 16.78 | 0.0  | 0.0        | 0.0  | 16.78       |
      | 2  | 10   | 21 January 2024  |           | 66.79           | 16.62         | 0.16     | 0.0  | 0.0       | 16.78 | 0.0  | 0.0        | 0.0  | 16.78       |
      | 3  | 10   | 31 January 2024  |           | 50.14           | 16.65         | 0.13     | 0.0  | 0.0       | 16.78 | 0.0  | 0.0        | 0.0  | 16.78       |
      | 4  | 10   | 10 February 2024 |           | 33.46           | 16.68         | 0.1      | 0.0  | 0.0       | 16.78 | 0.0  | 0.0        | 0.0  | 16.78       |
      | 5  | 10   | 20 February 2024 |           | 16.75           | 16.71         | 0.07     | 0.0  | 0.0       | 16.78 | 0.0  | 0.0        | 0.0  | 16.78       |
      | 6  | 10   | 01 March 2024    |           | 0.0             | 16.75         | 0.03     | 0.0  | 0.0       | 16.78 | 0.0  | 0.0        | 0.0  | 16.78       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.68     | 0.0  | 0.0       | 100.68 | 0.0  | 0.0        | 0.0  | 100.68      |
    When Admin sets the business date to "11 January 2024"
    And Customer makes "AUTOPAY" repayment on "11 January 2024" with 16.8 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "31 January 2024" due date and 5 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 31 January 2024 | Flat             | 5.0 | 0.0  | 0.0    | 5.0         |
    When Admin sets the business date to "15 February 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | WEEKS         | 01 March 2024 | 6                    |
    Then Loan Repayment schedule has 11 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 10   | 11 January 2024  | 11 January 2024  | 83.41           | 16.59         | 0.19     | 0.0  | 0.0       | 16.78 | 16.78 | 0.0        | 0.0  | 0.0         |
      | 2  | 10   | 21 January 2024  | 15 February 2024 | 83.39           | 0.02          | 0.0      | 0.0  | 0.0       | 0.02  | 0.02  | 0.02       | 0.0  | 0.0         |
      | 3  | 10   | 31 January 2024  |                  | 83.39           | 0.0           | 0.0      | 0.0  | 5.0       | 5.0   | 0.0   | 0.0        | 0.0  | 5.0         |
      | 4  | 10   | 10 February 2024 | 15 February 2024 | 83.39           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 5  | 5    | 15 February 2024 | 15 February 2024 | 83.39           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 6  | 15   | 01 March 2024    |                  | 70.11           | 13.28         | 0.8      | 0.0  | 0.0       | 14.08 | 0.0   | 0.0        | 0.0  | 14.08       |
      | 7  | 7    | 08 March 2024    |                  | 56.13           | 13.98         | 0.1      | 0.0  | 0.0       | 14.08 | 0.0   | 0.0        | 0.0  | 14.08       |
      | 8  | 7    | 15 March 2024    |                  | 42.13           | 14.0          | 0.08     | 0.0  | 0.0       | 14.08 | 0.0   | 0.0        | 0.0  | 14.08       |
      | 9  | 7    | 22 March 2024    |                  | 28.11           | 14.02         | 0.06     | 0.0  | 0.0       | 14.08 | 0.0   | 0.0        | 0.0  | 14.08       |
      | 10 | 7    | 29 March 2024    |                  | 14.07           | 14.04         | 0.04     | 0.0  | 0.0       | 14.08 | 0.0   | 0.0        | 0.0  | 14.08       |
      | 11 | 7    | 05 April 2024    |                  | 0.0             | 14.07         | 0.02     | 0.0  | 0.0       | 14.09 | 0.0   | 0.0        | 0.0  | 14.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 5.0       | 106.29 | 16.8 | 0.02       | 0.0  | 89.49       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 11 January 2024  | Repayment        | 16.8   | 16.61     | 0.19     | 0.0  | 0.0       | 83.39        | false    | false    |
      | 15 February 2024 | Re-age           | 83.95  | 83.39     | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 31 January 2024 | Flat             | 5.0 | 0.0  | 0.0    | 5.0         |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4303 @AdvancedPaymentAllocation
  Scenario: Verify allowing Re-aging on interest bearing loan - Interest calculation: Default Behavior - due date and frequency changed - change from WEEKS to MONTHS with multiple repayment transactions - UC13.8
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 18                | WEEKS                 | 3              | WEEKS                  | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 21   | 22 January 2024  |           | 83.5            | 16.5          | 0.41     | 0.0  | 0.0       | 16.91 | 0.0  | 0.0        | 0.0  | 16.91       |
      | 2  | 21   | 12 February 2024 |           | 66.93           | 16.57         | 0.34     | 0.0  | 0.0       | 16.91 | 0.0  | 0.0        | 0.0  | 16.91       |
      | 3  | 21   | 04 March 2024    |           | 50.29           | 16.64         | 0.27     | 0.0  | 0.0       | 16.91 | 0.0  | 0.0        | 0.0  | 16.91       |
      | 4  | 21   | 25 March 2024    |           | 33.59           | 16.7          | 0.21     | 0.0  | 0.0       | 16.91 | 0.0  | 0.0        | 0.0  | 16.91       |
      | 5  | 21   | 15 April 2024    |           | 16.82           | 16.77         | 0.14     | 0.0  | 0.0       | 16.91 | 0.0  | 0.0        | 0.0  | 16.91       |
      | 6  | 21   | 06 May 2024      |           | 0.0             | 16.82         | 0.07     | 0.0  | 0.0       | 16.89 | 0.0  | 0.0        | 0.0  | 16.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.44     | 0.0  | 0.0       | 101.44 | 0.0  | 0.0        | 0.0  | 101.44      |
    When Admin sets the business date to "22 January 2024"
    And Customer makes "AUTOPAY" repayment on "22 January 2024" with 17.16 EUR transaction amount
    When Admin sets the business date to "12 February 2024"
    And Customer makes "AUTOPAY" repayment on "12 February 2024" with 17.16 EUR transaction amount
    When Admin sets the business date to "04 March 2024"
    And Customer makes "AUTOPAY" repayment on "04 March 2024" with 10.0 EUR transaction amount
    When Admin sets the business date to "20 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024| 4                    |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 21   | 22 January 2024  | 22 January 2024  | 83.5            | 16.5          | 0.41     | 0.0  | 0.0       | 16.91 | 16.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 21   | 12 February 2024 | 12 February 2024 | 66.93           | 16.57         | 0.34     | 0.0  | 0.0       | 16.91 | 16.91 | 0.25       | 0.0  | 0.0         |
      | 3  | 21   | 04 March 2024    | 20 March 2024    | 56.7            | 10.23         | 0.27     | 0.0  | 0.0       | 10.5  | 10.5  | 0.5        | 0.0  | 0.0         |
      | 4  | 16   | 20 March 2024    | 20 March 2024    | 56.7            | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 5  | 12   | 01 April 2024    |                  | 42.63           | 14.07         | 0.31     | 0.0  | 0.0       | 14.38 | 0.0   | 0.0        | 0.0  | 14.38       |
      | 6  | 30   | 01 May 2024      |                  | 28.5            | 14.13         | 0.25     | 0.0  | 0.0       | 14.38 | 0.0   | 0.0        | 0.0  | 14.38       |
      | 7  | 31   | 01 June 2024     |                  | 14.29           | 14.21         | 0.17     | 0.0  | 0.0       | 14.38 | 0.0   | 0.0        | 0.0  | 14.38       |
      | 8  | 30   | 01 July 2024     |                  | 0.0             | 14.29         | 0.08     | 0.0  | 0.0       | 14.37 | 0.0   | 0.0        | 0.0  | 14.37       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.83     | 0.0  | 0.0       | 101.83 | 44.32 | 0.75       | 0.0  | 57.51       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 22 January 2024  | Repayment        | 17.16  | 16.75     | 0.41     | 0.0  | 0.0       | 83.25        | false    | false    |
      | 12 February 2024 | Repayment        | 17.16  | 16.82     | 0.34     | 0.0  | 0.0       | 66.43        | false    | false    |
      | 04 March 2024    | Repayment        | 10.0   | 9.73      | 0.27     | 0.0  | 0.0       | 56.7         | false    | false    |
      | 20 March 2024    | Re-age           | 56.88  | 56.7      | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "20 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4369 @AdvancedPaymentAllocation
  Scenario: Verify Loan backdated re-aging during 2nd installment after chargeback on reage start date on a closed period transaction with partially paid installment; adjust to last - interest bearing loan with default behaviour; payable interest - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 25 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.0            | 16.57         | 0.44     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.57           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.71           | 16.86         | 0.15     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.71         | 0.05     | 0.0  | 0.0       | 16.76 | 7.99  | 7.99       | 0.0  | 8.77        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.81     | 0.0  | 0.0       | 101.81 | 25.0 | 7.99       | 0.0  | 76.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
    When Admin sets the business date to "1 February 2024"
    When Admin runs inline COB job for Loan
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    When Admin sets the business date to "1 April 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 15 February 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 15 February 2024 | 15 February 2024 | 92.59           | 7.99          | 0.0      | 0.0  | 0.0       | 7.99  | 7.99  | 7.99       | 0.0  | 0.0         |
      | 3  | 0    | 15 February 2024 |                  | 77.15           | 15.44         | 0.26     | 0.0  | 0.0       | 15.7  | 0.0   | 0.0        | 0.0  | 15.7        |
      | 4  | 29   | 15 March 2024    |                  | 61.99           | 15.16         | 0.54     | 0.0  | 0.0       | 15.7  | 0.0   | 0.0        | 0.0  | 15.7        |
      | 5  | 31   | 15 April 2024    |                  | 46.75           | 15.24         | 0.46     | 0.0  | 0.0       | 15.7  | 0.0   | 0.0        | 0.0  | 15.7        |
      | 6  | 30   | 15 May 2024      |                  | 31.32           | 15.43         | 0.27     | 0.0  | 0.0       | 15.7  | 0.0   | 0.0        | 0.0  | 15.7        |
      | 7  | 31   | 15 June 2024     |                  | 15.8            | 15.52         | 0.18     | 0.0  | 0.0       | 15.7  | 0.0   | 0.0        | 0.0  | 15.7        |
      | 8  | 30   | 15 July 2024     |                  | 0.0             | 15.8          | 0.09     | 0.0  | 0.0       | 15.89 | 0.0   | 0.0        | 0.0  | 15.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 2.38     | 0.0  | 0.0       | 119.39 | 25.0  | 7.99       | 0.0  | 94.39       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 31 January 2024  | Accrual          | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 92.59        | false    | false    |
      | 15 February 2024 | Re-age           | 92.85  | 92.59     | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "1 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4378 @AdvancedPaymentAllocation
  Scenario: Verify Loan backdated re-aging on 2nd due after repay and chargeback on 1st due with partially paid installment; adjust to last - interest bearing loan with default behaviour - UC2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    # When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 25 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.0            | 16.57         | 0.44     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.57           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.71           | 16.86         | 0.15     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.71         | 0.05     | 0.0  | 0.0       | 16.76 | 7.99  | 7.99       | 0.0  | 8.77        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.81     | 0.0  | 0.0       | 101.81 | 25.0 | 7.99       | 0.0  | 76.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    When Admin sets the business date to "1 April 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 1 March 2024 | 6                    | DEFAULT              |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 92.59           |  7.99         | 0.0      | 0.0  | 0.0       | 7.99  | 7.99  | 7.99       | 0.0  | 0.0         |
      | 3  |  0   | 01 March 2024    |                  | 77.38           | 15.21         | 0.54     | 0.0  | 0.0       | 15.75 | 0.0   | 0.0        | 0.0  | 15.75       |
      | 4  | 31   | 01 April 2024    |                  | 62.17           | 15.21         | 0.54     | 0.0  | 0.0       | 15.75 | 0.0   | 0.0        | 0.0  | 15.75       |
      | 5  | 30   | 01 May 2024      |                  | 46.78           | 15.39         | 0.36     | 0.0  | 0.0       | 15.75 | 0.0   | 0.0        | 0.0  | 15.75       |
      | 6  | 31   | 01 June 2024     |                  | 31.3            | 15.48         | 0.27     | 0.0  | 0.0       | 15.75 | 0.0   | 0.0        | 0.0  | 15.75       |
      | 7  | 30   | 01 July 2024     |                  | 15.73           | 15.57         | 0.18     | 0.0  | 0.0       | 15.75 | 0.0   | 0.0        | 0.0  | 15.75       |
      | 8  | 31   | 01 August 2024   |                  | 0.0             | 15.73         | 0.09     | 0.0  | 0.0       | 15.82 | 0.0   | 0.0        | 0.0  | 15.82       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 2.56     | 0.0  | 0.0       | 119.57 | 25.0  | 7.99       | 0.0  | 94.57       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 92.59        | false    | false    |
      | 01 March 2024    | Re-age           | 93.13  | 92.59     | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "1 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4379 @AdvancedPaymentAllocation
  Scenario: Verify Loan backdated re-aging on 1st installment after repay and chargeback on 1st due with partially paid installment; adjust to last - interest bearing loan with default behaviour - UC3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 25 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.0            | 16.57         | 0.44     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.57           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.71           | 16.86         | 0.15     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.71         | 0.05     | 0.0  | 0.0       | 16.76 | 7.99  | 7.99       | 0.0  | 8.77        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.81     | 0.0  | 0.0       | 101.81 | 25.0 | 7.99       | 0.0  | 76.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    When Admin sets the business date to "1 April 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 February 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 92.59           | 24.42         | 0.58     | 0.0  | 0.0       | 25.0  | 25.0  | 7.99       | 0.0  | 0.0         |
      | 2  | 0    | 01 February 2024 |                  | 76.9            | 15.69         | 0.0      | 0.0  | 0.0       | 15.69 | 0.0   | 0.0        | 0.0  | 15.69       |
      | 3  | 29   | 01 March 2024    |                  | 61.79           | 15.11         | 0.58     | 0.0  | 0.0       | 15.69 | 0.0   | 0.0        | 0.0  | 15.69       |
      | 4  | 31   | 01 April 2024    |                  | 46.68           | 15.11         | 0.58     | 0.0  | 0.0       | 15.69 | 0.0   | 0.0        | 0.0  | 15.69       |
      | 5  | 30   | 01 May 2024      |                  | 31.31           | 15.37         | 0.32     | 0.0  | 0.0       | 15.69 | 0.0   | 0.0        | 0.0  | 15.69       |
      | 6  | 31   | 01 June 2024     |                  | 15.85           | 15.46         | 0.23     | 0.0  | 0.0       | 15.69 | 0.0   | 0.0        | 0.0  | 15.69       |
      | 7  | 30   | 01 July 2024     |                  | 0.0             | 15.85         | 0.14     | 0.0  | 0.0       | 15.99 | 0.0   | 0.0        | 0.0  | 15.99       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 2.43     | 0.0  | 0.0       | 119.44 | 25.0  | 7.99       | 0.0  | 94.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 92.59        | false    | false    |
      | 01 February 2024 | Re-age           | 92.59  | 92.59     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
#    When Loan Pay-off is made on "1 April 2024"
#    Then Loan is closed with zero outstanding balance and it's all installments have obligations met //cause error 500

  @TestRailId:C4380 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging trn with partial chargeback with custom payment alloc on re-age transaction date - interest bearing loan with default behaviour - UC6.2.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8  | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 10 EUR transaction amount
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.28           | 22.71         | 1.59     | 0.0  | 0.0       | 24.3  | 0.0   | 0.0        | 0.0  | 24.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.39           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.42           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.37           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.24           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 14.24         | 0.08     | 0.0  | 0.0       | 14.32 | 0.0   | 0.0        | 0.0  | 14.32       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 109.42        | 3.41     | 0.0  | 0.0       | 112.83 | 17.01 | 0.0        | 0.0  | 95.82       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false     |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Chargeback       | 10.0   | 9.42      | 0.58     | 0.0  | 0.0       | 92.99        | false    | false    |
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4381 @AdvancedPaymentAllocation
  Scenario: Verify Loan backdated re-aging post maturity date after chargeback on reage start date on a closed period transaction with partially paid installment; adjust to last - interest bearing loan with default behaviour - UC5.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 25 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.0            | 16.57         | 0.44     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.57           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.71           | 16.86         | 0.15     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.71         | 0.05     | 0.0  | 0.0       | 16.76 | 7.99  | 7.99       | 0.0  | 8.77        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.81     | 0.0  | 0.0       | 101.81 | 25.0 | 7.99       | 0.0  | 76.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
    When Admin sets the business date to "1 February 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    When Admin sets the business date to "1 April 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 August 2024 | 6                    | DEFAULT               |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 01 April 2024    | 100.58          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024     | 01 April 2024    | 92.59           | 7.99          | 0.0      | 0.0  | 0.0       | 7.99  | 7.99  | 7.99       | 0.0  | 0.0         |
      | 4  | 122  | 01 August 2024    |                  | 79.63           | 12.96         | 3.24     | 0.0  | 0.0       | 16.2  | 0.0   | 0.0        | 0.0  | 16.2        |
      | 5  | 31   | 01 September 2024 |                  | 63.89           | 15.74         | 0.46     | 0.0  | 0.0       | 16.2  | 0.0   | 0.0        | 0.0  | 16.2        |
      | 6  | 30   | 01 October 2024   |                  | 48.06           | 15.83         | 0.37     | 0.0  | 0.0       | 16.2  | 0.0   | 0.0        | 0.0  | 16.2        |
      | 7  | 31   | 01 November 2024  |                  | 32.14           | 15.92         | 0.28     | 0.0  | 0.0       | 16.2  | 0.0   | 0.0        | 0.0  | 16.2        |
      | 8  | 30   | 01 December 2024  |                  | 16.13           | 16.01         | 0.19     | 0.0  | 0.0       | 16.2  | 0.0   | 0.0        | 0.0  | 16.2        |
      | 9  | 31   | 01 January 2025   |                  | 0.0             | 16.13         | 0.09     | 0.0  | 0.0       | 16.22 | 0.0   | 0.0        | 0.0  | 16.22       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 5.21     | 0.0  | 0.0       | 122.22 | 25.0  | 7.99       | 0.0  | 97.22       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 92.59        | false    | false    |
      | 01 April 2024    | Re-age           | 93.67  | 92.59     | 1.08     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "1 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4386 @AdvancedPaymentAllocation
  Scenario: Verify that Re-aging with DEFAULT interest handling is forbidden on written-off loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does write-off the loan on "01 March 2024"
    Then Loan status will be "CLOSED_WRITTEN_OFF"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment              | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Close (as written-off) | 85.04  | 83.57     | 1.47     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "15 March 2024"
    Then Admin fails to create a Loan re-aging transaction with the following data because loan was closed:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 01 April 2024 | 6                    | DEFAULT               |

  @TestRailId:C4519 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with charge-off with zero interest after re-age - interest bearing loan with default behaviour - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  |  0.0            | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8  | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off with zero interest behaviour --- #
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 55.95           | 14.3          | 0.0      | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 41.65           | 14.3          | 0.0      | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 27.35           | 14.3          | 0.0      | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 13.05           | 14.3          | 0.0      | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  |  0.0            | 13.05         | 0.0      | 0.0  | 0.0       | 13.05 | 0.0   | 0.0        | 0.0  | 13.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.56     | 0.0  | 0.0       | 101.56 | 17.01 | 0.0        | 0.0  | 84.55       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 84.55  | 83.57     | 0.98     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4520 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with charge-off with accelerate maturity after re-age - interest bearing loan with default behaviour - UC2.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024|           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024   |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  |  0.0            | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8  | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off with accelerate maturity behaviour --- #
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024    |                  | 0.0             | 83.57         | 0.98     | 0.0  | 0.0       | 84.55 | 0.0   | 0.0        | 0.0  | 84.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.56     | 0.0  | 0.0       | 101.56 | 17.01 | 0.0        | 0.0  | 84.55       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 84.55  | 83.57     | 0.98     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4536 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with charge-off with accelerate maturity after re-age at the same date - interest bearing loan with default behaviour - UC2.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024|           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024   |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  |  0.0            | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8  | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off with accelerate maturity behaviour --- #
    And Admin does charge-off the loan on "15 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "15 March 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    |                  | 0.0             | 83.57         | 0.71     | 0.0  | 0.0       | 84.28 | 0.0   | 0.0        | 0.0  | 84.28       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
       | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Charge-off       | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4521 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging trn with charge-off after re-aging - interest bearing loan with default behaviour - UC3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  |  0.0            | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8  | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off regular --- #
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  |  0.0            | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8  | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 85.79  | 83.57     | 2.22     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4522 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with Contract Termination after re-age - interest bearing loan with default behaviour - UC4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           |  0.0          | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024       |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024      |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024      |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024    |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024 |                  |  0.0            | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8     | 0.0  | 0.0       | 102.8  | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin successfully terminates loan contract
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024    |                  | 0.0             | 83.57         | 0.98     | 0.0  | 0.0       | 84.55 | 0.0   | 0.0        | 0.0  | 84.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.56     | 0.0  | 0.0       | 101.56 | 17.01 | 0.0        | 0.0  | 84.55       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment            | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age               | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual              | 1.56   | 0.0       | 1.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Contract Termination | 84.55  | 83.57     | 0.98     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4523 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with Write-Off after re-age - interest bearing loan with default behaviour - UC5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
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
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024| 6                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024    |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 30   | 01 May 2024      |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 31   | 01 June 2024     |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 30   | 01 July 2024     |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 August 2024   |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 9  | 31   | 01 September 2024|                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin does write-off the loan on "01 April 2024"
    Then Loan status will be "CLOSED_WRITTEN_OFF"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment              | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age                 | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Close (as written-off) | 85.79  | 83.57     | 2.22     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 0 outstanding amount
    Then Loan's all installments have obligations met

  @TestRailId:C4682 @AdvancedPaymentAllocation
  Scenario: Verify Re-aging on interest bearing loan with Default Behavior - re-aging that doesn't change number of installments but changes maturity date - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
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
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments |
      | 1               | MONTHS        | 15 February 2024 | 4                    |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 14   | 15 February 2024 | 15 February 2024 | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 0    | 15 February 2024 |                  | 62.67           | 20.9          | 0.24     | 0.0  | 0.0       | 21.14 | 0.0   | 0.0        | 0.0  | 21.14       |
      | 4  | 29   | 15 March 2024    |                  | 41.9            | 20.77         | 0.37     | 0.0  | 0.0       | 21.14 | 0.0   | 0.0        | 0.0  | 21.14       |
      | 5  | 31   | 15 April 2024    |                  | 21.0            | 20.9          | 0.24     | 0.0  | 0.0       | 21.14 | 0.0   | 0.0        | 0.0  | 21.14       |
      | 6  | 30   | 15 May 2024      |                  | 0.0             | 21.0          | 0.12     | 0.0  | 0.0       | 21.12 | 0.0   | 0.0        | 0.0  | 21.12       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.55     | 0.0  | 0.0       | 101.55 | 17.01 | 0.0        | 0.0  | 84.54       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 February 2024 | Re-age           | 83.81  | 83.57     | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanReAgeTransactionBusinessEvent has changedTerms "false"
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4693 @AdvancedPaymentAllocation
  Scenario: Verify that repeated re-aging across month-end should not accumulate stub periods
    When Admin sets the business date to "28 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION | 28 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 January 2026" with "100" amount and expected disbursement date on "28 January 2026"
    When Admin successfully disburse the loan on "28 January 2026" with "100" EUR transaction amount
#   --- Re-age 4 times across late-January dates ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments |
      | 1               | MONTHS        | 28 February 2026 | 6                    |
    When Admin sets the business date to "29 January 2026"
    And Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments |
      | 1               | MONTHS        | 28 February 2026 | 6                    |
    When Admin sets the business date to "30 January 2026"
    And Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments |
      | 1               | MONTHS        | 28 February 2026 | 6                    |
    When Admin sets the business date to "31 January 2026"
    And Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments |
      | 1               | MONTHS        | 28 February 2026 | 6                    |
#   --- Verify schedule: should be 7 periods (1 collapsed stub + 6 re-aged), NOT 10+ with accumulated 1-day stubs ---
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 3    | 31 January 2026   | 28 January 2026  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 28 February 2026  |                  | 83.62           | 16.38         | 0.64     | 0.0  | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 28   | 28 March 2026     |                  | 67.09           | 16.53         | 0.49     | 0.0  | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 31   | 28 April 2026     |                  | 50.46           | 16.63         | 0.39     | 0.0  | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 5  | 30   | 28 May 2026       |                  | 33.73           | 16.73         | 0.29     | 0.0  | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 6  | 31   | 28 June 2026      |                  | 16.91           | 16.82         | 0.2      | 0.0  | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 7  | 30   | 28 July 2026      |                  | 0.0             | 16.91         | 0.1      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.11     | 0.0  | 0.0       | 102.11 | 0.0  | 0.0        | 0.0  | 102.11      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 28 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2026  | Re-age           | 100.02 | 100.0     | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2026  | Re-age           | 100.04 | 100.0     | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2026  | Re-age           | 100.06 | 100.0     | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "31 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

