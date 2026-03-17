@InterestRateChangeFeature
Feature: Loan interest rate change on repayment schedule

  @TestRailId:C3217
  Scenario: Verify Interest rate change - backdated, modification on installment due date - UC1
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.31           | 16.66         | 0.22     | 0.0  | 0.0       | 16.88 | 0.13  | 0.13       | 0.0  | 16.75       |
      | 4  | 30   | 01 May 2024      |                  | 33.6            | 16.71         | 0.17     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 5  | 31   | 01 June 2024     |                  | 16.83           | 16.77         | 0.11     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.83         | 0.06     | 0.0  | 0.0       | 16.89 | 0.0   | 0.0        | 0.0  | 16.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.42     | 0.0  | 0.0       | 101.42 | 34.02 | 0.13       | 0.0  | 67.4        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | false    | true     |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3916
  Scenario: Verify Interest rate change - backdated, modification in middle of installment - UC2
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 February 2024   | 16 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.38     | 0.0  | 0.0       | 16.9  | 16.9  | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.37           | 16.68         | 0.22     | 0.0  | 0.0       | 16.9  | 0.11  | 0.11       | 0.0  | 16.79       |
      | 4  | 30   | 01 May 2024      |                  | 33.64           | 16.73         | 0.17     | 0.0  | 0.0       | 16.9  | 0.0   | 0.0        | 0.0  | 16.9        |
      | 5  | 31   | 01 June 2024     |                  | 16.85           | 16.79         | 0.11     | 0.0  | 0.0       | 16.9  | 0.0   | 0.0        | 0.0  | 16.9        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.85         | 0.06     | 0.0  | 0.0       | 16.91 | 0.0   | 0.0        | 0.0  | 16.91       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.52     | 0.0  | 0.0       | 101.52 | 34.02 | 0.11       | 0.0  | 67.5        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.63     | 0.38     | 0.0  | 0.0       | 66.94        | false    | true     |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3917
  Scenario: Verify Interest rate change - backdated, modification in middle of installment, no reverse-replay - UC3
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 11 March 2024      | 11 March 2024   |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.41           | 16.64         | 0.27     | 0.0  | 0.0       | 16.91 | 0.0   | 0.0        | 0.0  | 16.91       |
      | 4  | 30   | 01 May 2024      |                  | 33.67           | 16.74         | 0.17     | 0.0  | 0.0       | 16.91 | 0.0   | 0.0        | 0.0  | 16.91       |
      | 5  | 31   | 01 June 2024     |                  | 16.87           | 16.8          | 0.11     | 0.0  | 0.0       | 16.91 | 0.0   | 0.0        | 0.0  | 16.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.87         | 0.06     | 0.0  | 0.0       | 16.93 | 0.0   | 0.0        | 0.0  | 16.93       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.68     | 0.0  | 0.0       | 101.68 | 34.02 | 0.0        | 0.0  | 67.66       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3918
  Scenario: Verify Interest rate change - backdated, modification on installment due date, with repayment undo - UC1.2
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.31           | 16.66         | 0.22     | 0.0  | 0.0       | 16.88 | 0.13  | 0.13       | 0.0  | 16.75       |
      | 4  | 30   | 01 May 2024      |                  | 33.6            | 16.71         | 0.17     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 5  | 31   | 01 June 2024     |                  | 16.83           | 16.77         | 0.11     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.83         | 0.06     | 0.0  | 0.0       | 16.89 | 0.0   | 0.0        | 0.0  | 16.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.42     | 0.0  | 0.0       | 101.42 | 34.02 | 0.13       | 0.0  | 67.4        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | false    | true     |

    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 March 2024      | 02 March 2024   |                 |                  |                 |            | 1.15            |
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 16.65 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.19           | 16.78         | 0.0      | 0.0  | 0.0       | 16.78 | 16.78 | 0.13       | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                  | 33.68           | 16.51         | 0.27     | 0.0  | 0.0       | 16.78 | 0.0   | 0.0        | 0.0  | 16.78       |
      | 5  | 31   | 01 June 2024     |                  | 16.93           | 16.75         | 0.03     | 0.0  | 0.0       | 16.78 | 0.0   | 0.0        | 0.0  | 16.78       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.93         | 0.02     | 0.0  | 0.0       | 16.95 | 0.0   | 0.0        | 0.0  | 16.95       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.18     | 0.0  | 0.0       | 101.18 | 50.67 | 0.13       | 0.0  | 50.51       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | false    | true     |
      | 01 April 2024    | Repayment        | 16.65  | 16.65     | 0.0      | 0.0  | 0.0       | 50.19        | false    | false    |
# --- undo 2nd repayment (while interest rate change to 4% have happened)---
    When Customer undo "1"th "Repayment" transaction made on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.65 | 0.0        | 16.65 | 0.23        |
      | 3  | 31   | 01 April 2024    |                  | 50.27           | 16.7          | 0.08     | 0.0  | 0.0       | 16.78 | 0.0   | 0.0        | 0.0   | 16.78       |
      | 4  | 30   | 01 May 2024      |                  | 33.54           | 16.73         | 0.05     | 0.0  | 0.0       | 16.78 | 0.0   | 0.0        | 0.0   | 16.78       |
      | 5  | 31   | 01 June 2024     |                  | 16.79           | 16.75         | 0.03     | 0.0  | 0.0       | 16.78 | 0.0   | 0.0        | 0.0   | 16.78       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.79         | 0.02     | 0.0  | 0.0       | 16.81 | 0.0   | 0.0        | 0.0   | 16.81       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 1.04     | 0.0  | 0.0       | 101.04 | 33.66 | 0.0        | 16.65 | 67.38       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | true     | true     |
      | 01 April 2024    | Repayment        | 16.65  | 16.6      | 0.05     | 0.0  | 0.0       | 66.97        | false    | true     |
# --- undo 1st repayment (while no interest rate change have happened)---
    When Customer undo "1"th "Repayment" transaction made on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 16.65 | 0.0        | 16.65 | 0.36        |
      | 2  | 29   | 01 March 2024    |           | 67.02           | 16.55         | 0.33     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0   | 16.88       |
      | 3  | 31   | 01 April 2024    |           | 50.34           | 16.68         | 0.1      | 0.0  | 0.0       | 16.78 | 0.0   | 0.0        | 0.0   | 16.78       |
      | 4  | 30   | 01 May 2024      |           | 33.61           | 16.73         | 0.05     | 0.0  | 0.0       | 16.78 | 0.0   | 0.0        | 0.0   | 16.78       |
      | 5  | 31   | 01 June 2024     |           | 16.86           | 16.75         | 0.03     | 0.0  | 0.0       | 16.78 | 0.0   | 0.0        | 0.0   | 16.78       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.86         | 0.02     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0   | 16.88       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 1.11     | 0.0  | 0.0       | 101.11 | 16.65 | 0.0        | 16.65 | 84.46       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | true     | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | true     | true     |
      | 01 April 2024    | Repayment        | 16.65  | 16.43     | 0.22     | 0.0  | 0.0       | 83.57        | false    | true     |

    When Loan Pay-off is made on "01 April 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3944
  Scenario: Verify Interest rate change - backdated, charged-off, regular - UC4.1
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin does charge-off the loan on "01 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.31           | 16.66         | 0.22     | 0.0  | 0.0       | 16.88 | 0.13  | 0.13       | 0.0  | 16.75       |
      | 4  | 30   | 01 May 2024      |                  | 33.6            | 16.71         | 0.17     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 5  | 31   | 01 June 2024     |                  | 16.83           | 16.77         | 0.11     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.83         | 0.06     | 0.0  | 0.0       | 16.89 | 0.0   | 0.0        | 0.0  | 16.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.42     | 0.0  | 0.0       | 101.42 | 34.02 | 0.13       | 0.0  | 67.4        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment          | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | false    | true     |
      | 01 April 2024    | Accrual            | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 67.4   | 66.84     | 0.56     | 0.0  | 0.0       | 0.0          | false    | true     |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3945
  Scenario: Verify Interest rate change - backdated, charged-off, zero interest - UC4.2
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.31           | 16.66         | 0.22     | 0.0  | 0.0       | 16.88 | 0.13  | 0.13       | 0.0  | 16.75       |
      | 4  | 30   | 01 May 2024      |                  | 33.43           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 5  | 31   | 01 June 2024     |                  | 16.55           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.55         | 0.0      | 0.0  | 0.0       | 16.55 | 0.0   | 0.0        | 0.0  | 16.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.08     | 0.0  | 0.0       | 101.08 | 34.02 | 0.13       | 0.0  | 67.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment          | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | false    | true     |
      | 01 April 2024    | Accrual            | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 67.06  | 66.84     | 0.22     | 0.0  | 0.0       | 0.0          | false    | true     |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3946
  Scenario: Verify Interest rate change - backdated, charged-off, accelerate maturity - UC4.3
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 66.97         | 0.22     | 0.0  | 0.0       | 67.19 | 0.13  | 0.13       | 0.0  | 67.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.08     | 0.0  | 0.0       | 101.08 | 34.02 | 0.13       | 0.0  | 67.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment          | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | false    | true     |
      | 01 April 2024    | Accrual            | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 67.06  | 66.84     | 0.22     | 0.0  | 0.0       | 0.0          | false    | true     |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan's all installments have obligations met

  @TestRailId:С3941
  Scenario: Verify Interest rate change - backdated, CBR - UC5
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 85.0 EUR transaction amount
    And Admin makes Credit Balance Refund transaction on "01 March 2024" with 0.94 EUR transaction amount
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 March 2024    | 50.09           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 16.88 | 16.88      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 March 2024    | 33.21           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 16.88 | 16.88      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 March 2024    | 16.33           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 16.88 | 16.88      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 March 2024    | 0.0             | 16.33         | 0.0      | 0.0  | 0.0       | 16.33 | 16.33 | 16.33      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100.0         | 0.86     | 0.0  | 0.0       | 100.86 | 100.86 | 66.97      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement          | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment             | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment             | 85.0   | 83.57     | 0.28     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 01 March 2024    | Credit Balance Refund | 0.94   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 01 April 2024    | Accrual               | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment    | 0.21   | 0.0       | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan's all installments have obligations met
    And Loan has 0.21 overpaid amount
    And Loan status will be "OVERPAID"

    And Admin makes Credit Balance Refund transaction on "01 April 2024" with 0.21 EUR transaction amount
    Then Loan's all installments have obligations met
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:С3942
  Scenario: Verify Interest rate change - backdated, closed loan - UC7
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 84.06 EUR transaction amount
    Then Loan's all installments have obligations met
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 March 2024    | 50.09           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 16.88 | 16.88      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 March 2024    | 33.21           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 16.88 | 16.88      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 March 2024    | 16.33           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 16.88 | 16.88      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 March 2024    | 0.0             | 16.33         | 0.0      | 0.0  | 0.0       | 16.33 | 16.33 | 16.33      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100.0         | 0.86     | 0.0  | 0.0       | 100.86 | 100.86 | 66.97      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment          | 84.06  | 83.57     | 0.28     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 01 April 2024    | Accrual            | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.21   | 0.0       | 0.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan's all installments have obligations met
    And Loan has 0.21 overpaid amount
    And Loan status will be "OVERPAID"

    And Admin makes Credit Balance Refund transaction on "01 April 2024" with 0.21 EUR transaction amount
    Then Loan's all installments have obligations met
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:С3943
  Scenario: Verify Interest rate change - backdated, overpaid loan - UC7.2
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 84.2 EUR transaction amount
    Then Loan's all installments have obligations met
    And Loan has 0.14 overpaid amount
    And Loan status will be "OVERPAID"
    And Loan status will be "OVERPAID"

    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 9               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.11           | 16.46         | 0.63     | 0.0  | 0.0       | 17.09 | 17.09 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 March 2024    | 50.02           | 17.09         | 0.0      | 0.0  | 0.0       | 17.09 | 17.09 | 17.09      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 March 2024    | 32.93           | 17.09         | 0.0      | 0.0  | 0.0       | 17.09 | 17.09 | 17.09      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 March 2024    | 15.84           | 17.09         | 0.0      | 0.0  | 0.0       | 17.09 | 17.09 | 17.09      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 March 2024    | 0.0             | 15.84         | 0.0      | 0.0  | 0.0       | 15.84 | 15.84 | 15.84      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100.0         | 1.21     | 0.0  | 0.0       | 101.21 | 101.21 | 67.11      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 84.2   | 83.57     | 0.63     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 01 April 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 0 outstanding amount
    And Loan's all installments have obligations met
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:С3952
  Scenario: Verify Interest rate change - backdated, closed as written-off loan - UC7.3
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
# --- make write-off --- #
    And Admin does write-off the loan on "01 March 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment              | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Close (as written-off) | 85.04  | 83.57     | 1.47     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_WRITTEN_OFF"
    And Loan's all installments have obligations met

    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 March 2024    | 50.31           | 16.66         | 0.22     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 March 2024    | 33.6            | 16.71         | 0.17     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 March 2024    | 16.83           | 16.77         | 0.11     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 March 2024    | 0.0             | 16.83         | 0.06     | 0.0  | 0.0       | 16.89 | 0.0   | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.42     | 0.0  | 0.0       | 101.42 | 17.01 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment              | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Close (as written-off) | 84.41  | 83.57     | 0.84     | 0.0  | 0.0       | 0.0          | false    | true     |
    Then Loan has 0 outstanding amount
    And Loan's all installments have obligations met
    And Loan status will be "CLOSED_WRITTEN_OFF"

  @TestRailId:С3947
  Scenario: Verify Interest rate change - backdated, external asset owner - UC6
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
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
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2024-04-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    And Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2024-04-01     | 1                  | PENDING | 2024-03-01    | 9999-12-31  | SALE             |

    When Admin sets the business date to "02 April 2024"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2024-04-01     | 1                  | PENDING | 2024-03-01    | 2024-04-01  | SALE             |
      | 2024-04-01     | 1                  | ACTIVE  | 2024-04-02    | 9999-12-31  | SALE             |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 02 February 2024 |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 16.88 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.31           | 16.66         | 0.22     | 0.0  | 0.0       | 16.88 | 0.13  | 0.13       | 0.0  | 16.75       |
      | 4  | 30   | 01 May 2024      |                  | 33.6            | 16.71         | 0.17     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 5  | 31   | 01 June 2024     |                  | 16.83           | 16.77         | 0.11     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.83         | 0.06     | 0.0  | 0.0       | 16.89 | 0.0   | 0.0        | 0.0  | 16.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.42     | 0.0  | 0.0       | 101.42 | 34.02 | 0.13       | 0.0  | 67.4        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.73     | 0.28     | 0.0  | 0.0       | 66.84        | false    | true     |
      | 01 April 2024    | Accrual          | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer makes "AUTOPAY" repayment on "01 April 2024" with 67.06 EUR transaction amount and check external owner
    Then Loan's all installments have obligations met

  @TestRailId:C3948
  Scenario: Verify backdated interest rate INCREASE on charged-off loan - backdated, charged-off, regular - UC4.4
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
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
    When Admin sets the business date to "01 March 2024"
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
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 34.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
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
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 34.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Accrual          | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 68.03  | 67.05     | 0.98     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 15 January 2024    | 01 April 2024   |                 |                  |                 |            | 12              |
      # Verify increased interest 7% to 12% causes higher charge-off amount after reversal/replay
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024 | 83.6            | 16.4          | 0.83     | 0.0  | 0.0       | 17.23 | 17.23 | 0.0        | 0.22 | 0.0         |
      | 2  | 29   | 01 March 2024    |               | 67.21           | 16.39         | 0.84     | 0.0  | 0.0       | 17.23 | 16.79 | 0.0        | 0.0  | 0.44        |
      | 3  | 31   | 01 April 2024    |               | 50.65           | 16.56         | 0.67     | 0.0  | 0.0       | 17.23 | 0.0   | 0.0        | 0.0  | 17.23       |
      | 4  | 30   | 01 May 2024      |               | 33.93           | 16.72         | 0.51     | 0.0  | 0.0       | 17.23 | 0.0   | 0.0        | 0.0  | 17.23       |
      | 5  | 31   | 01 June 2024     |               | 17.04           | 16.89         | 0.34     | 0.0  | 0.0       | 17.23 | 0.0   | 0.0        | 0.0  | 17.23       |
      | 6  | 30   | 01 July 2024     |               | 0.0             | 17.04         | 0.17     | 0.0  | 0.0       | 17.21 | 0.0   | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 3.36     | 0.0  | 0.0       | 103.36 | 34.02 | 0.0        | 0.22 | 69.34       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.4      | 0.61     | 0.0  | 0.0       | 83.6         | false    | true     |
      | 01 March 2024    | Repayment        | 17.01  | 16.39     | 0.62     | 0.0  | 0.0       | 67.21        | false    | true     |
      | 01 April 2024    | Accrual          | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.88   | 0.0       | 0.88     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 69.34  | 67.21     | 2.13     | 0.0  | 0.0       | 0.0          | false    | true     |

  @TestRailId:C3949
  Scenario: Verify backdated interest modification with PARTIAL payment before charge-off - backdated, charged-off, zero interest - UC4.5
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 10.00 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 10.0 | 0.0        | 0.0  | 7.01        |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 10.0 | 0.0        | 0.0  | 92.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 10.00 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 7.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |               | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 2.99  | 0.0        | 0.0  | 14.02       |
      | 3  | 31   | 01 April 2024    |               | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |               | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |               | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |               | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 20.0 | 0.0        | 7.01 | 82.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | false    | false    |
      | 01 March 2024    | Repayment        | 10.0   | 9.42      | 0.58     | 0.0  | 0.0       | 80.58        | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 7.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |               | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 2.99  | 0.0        | 0.0  | 14.02       |
      | 3  | 31   | 01 April 2024    |               | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |               | 33.42           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |               | 16.41           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |               | 0.0             | 16.41         | 0.0      | 0.0  | 0.0       | 16.41 | 0.0   | 0.0        | 0.0  | 16.41       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.46     | 0.0  | 0.0       | 101.46 | 20.0 | 0.0        | 7.01 | 81.46       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | false    | false    |
      | 01 March 2024    | Repayment        | 10.0   | 9.42      | 0.58     | 0.0  | 0.0       | 80.58        | false    | false    |
      | 01 April 2024    | Accrual          | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 81.46  | 80.58     | 0.88     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 01 April 2024   |                 |                  |                 |            | 4               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 7.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |               | 66.97           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 2.99  | 0.0        | 0.0  | 13.89       |
      | 3  | 31   | 01 April 2024    |               | 50.31           | 16.66         | 0.22     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 4  | 30   | 01 May 2024      |               | 33.43           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 5  | 31   | 01 June 2024     |               | 16.55           | 16.88         | 0.0      | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0  | 16.88       |
      | 6  | 30   | 01 July 2024     |               | 0.0             | 16.55         | 0.0      | 0.0  | 0.0       | 16.55 | 0.0   | 0.0        | 0.0  | 16.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.08     | 0.0  | 0.0       | 101.08 | 20.0 | 0.0        | 7.01 | 81.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | false    | false    |
      | 01 March 2024    | Repayment          | 10.0   | 9.42      | 0.58     | 0.0  | 0.0       | 80.58        | false    | false    |
      | 01 April 2024    | Accrual            | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 81.08  | 80.58     | 0.5      | 0.0  | 0.0       | 0.0          | false    | true     |

  @TestRailId:C3950
  Scenario: Verify multiple backdated interest modifications after charge-off - backdated, charged-off, accelerate maturity - UC4.6
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin runs inline COB job for Loan
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
    When Admin sets the business date to "01 March 2024"
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
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 34.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 67.05         | 0.39     | 0.0  | 0.0       | 67.44 | 0.0   | 0.0        | 0.0  | 67.44       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.46     | 0.0  | 0.0       | 101.46 | 34.02 | 0.0        | 0.0  | 67.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Accrual          | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 67.44  | 67.05     | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
  # First interest modification 7% -> 5%
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 15 January 2024    | 01 April 2024   |                 |                  |                 |            | 5               |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.49     | 0.0  | 0.0       | 16.92 | 16.92 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.0            | 16.57         | 0.35     | 0.0  | 0.0       | 16.92 | 16.92 | 0.09       | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 67.0          | 0.28     | 0.0  | 0.0       | 67.28 | 0.18  | 0.18       | 0.0  | 67.1        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.12     | 0.0  | 0.0       | 101.12 | 34.02 | 0.27       | 0.0  | 67.1        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 83.48        | false    | true     |
      | 01 March 2024    | Repayment          | 17.01  | 16.66     | 0.35     | 0.0  | 0.0       | 66.82        | false    | true     |
      | 01 April 2024    | Accrual            | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 67.1   | 66.82     | 0.28     | 0.0  | 0.0       | 0.0          | false    | true     |
  # Second interest modification 5% -> 3%
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 20 February 2024   | 01 April 2024   |                 |                  |                 |            | 3               |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.49     | 0.0  | 0.0       | 16.92 | 16.92 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.01           | 16.56         | 0.3      | 0.0  | 0.0       | 16.86 | 16.86 | 0.09       | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 67.01         | 0.17     | 0.0  | 0.0       | 67.18 | 0.24  | 0.24       | 0.0  | 66.94       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.96     | 0.0  | 0.0       | 100.96 | 34.02 | 0.33       | 0.0  | 66.94       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 83.48        | false    | true     |
      | 01 March 2024    | Repayment          | 17.01  | 16.71     | 0.3      | 0.0  | 0.0       | 66.77        | false    | true     |
      | 01 April 2024    | Accrual            | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.16   | 0.0       | 0.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 66.94  | 66.77     | 0.17     | 0.0  | 0.0       | 0.0          | false    | true     |

  @TestRailId:C3951
  Scenario: Verify backdated interest modification with fees and penalties on charged-off loan - backdated, charged-off, regular - UC4.7
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
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
    When Admin sets the business date to "10 February 2024"
    And Customer makes "AUTOPAY" repayment on "10 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 10 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.08           | 16.49         | 0.52     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.93           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.93         | 0.1      | 0.0  | 0.0       | 17.03 | 0.0   | 0.0        | 0.0   | 17.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 2.08     | 0.0  | 0.0       | 102.08 | 17.01 | 0.0        | 17.01 | 85.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 10 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 February 2024" due date and 5 EUR transaction amount
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 February 2024" due date and 8 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 10 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.08           | 16.49         | 0.52     | 8.0  | 5.0       | 30.01 | 0.0   | 0.0        | 0.0   | 30.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.46           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.93           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.93         | 0.1      | 0.0  | 0.0       | 17.03 | 0.0   | 0.0        | 0.0   | 17.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 2.08     | 8.0  | 5.0       | 115.08 | 17.01 | 0.0        | 17.01 | 98.07       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 10 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 10 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.08           | 16.49         | 0.52     | 8.0  | 5.0       | 30.01 | 0.0   | 0.0        | 0.0   | 30.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.56           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.84           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.03           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.03         | 0.1      | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0   | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 2.18     | 8.0  | 5.0       | 115.18 | 17.01 | 0.0        | 17.01 | 98.17       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 10 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 April 2024    | Accrual          | 1.59   | 0.0       | 1.59     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 98.17  | 83.57     | 1.6      | 8.0  | 5.0       | 0.0          | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 01 April 2024   |                 |                  |                 |            | 4               |
# Verify fee/penalties are correctly handled in charge-off reversal/replay
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 10 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.99           | 16.58         | 0.3      | 8.0  | 5.0       | 29.88 | 0.0   | 0.0        | 0.0   | 29.88       |
      | 3  | 31   | 01 April 2024    |                  | 50.39           | 16.6          | 0.28     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0   | 16.88       |
      | 4  | 30   | 01 May 2024      |                  | 33.68           | 16.71         | 0.17     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0   | 16.88       |
      | 5  | 31   | 01 June 2024     |                  | 16.91           | 16.77         | 0.11     | 0.0  | 0.0       | 16.88 | 0.0   | 0.0        | 0.0   | 16.88       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.91         | 0.06     | 0.0  | 0.0       | 16.97 | 0.0   | 0.0        | 0.0   | 16.97       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 1.5      | 8.0  | 5.0       | 114.5 | 17.01 | 0.0        | 17.01 | 97.49       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 10 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 April 2024    | Accrual            | 1.59   | 0.0       | 1.59     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.43   | 0.0       | 0.43     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 97.49  | 83.57     | 0.92     | 8.0  | 5.0       | 0.0          | false    | true     |

  @TestRailId:C3968
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC1: interest recalculation, no overdue installment
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 250.0 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |             | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025    | 01 May 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025   |             | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |             | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |             | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 May 2025      | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
    When Admin sets the business date to "05 May 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |             | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025    | 01 May 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025   |             | 502.49          | 247.51        | 7.5      | 0.0  | 0.0       | 255.01 | 0.0   | 0.0        | 0.0  | 255.01      |
      | 3  | 30   | 01 July 2025   |             | 252.5           | 249.99        | 5.02     | 0.0  | 0.0       | 255.01 | 0.0   | 0.0        | 0.0  | 255.01      |
      | 4  | 31   | 01 August 2025 |             | 0.0             | 252.5         | 2.52     | 0.0  | 0.0       | 255.02 | 0.0   | 0.0        | 0.0  | 255.02      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 15.04    | 0.0  | 0.0       | 1015.04 | 250.0 | 0.0        | 0.0  | 765.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 May 2025      | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |

  @TestRailId:C3969
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC2: interest recalculation, overdue installment
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 May 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 503.62          | 246.38        | 8.63     | 0.0  | 0.0       | 255.01 | 0.0  | 0.0        | 0.0  | 255.01      |
      | 3  | 30   | 01 July 2025   |           | 253.65          | 249.97        | 5.04     | 0.0  | 0.0       | 255.01 | 0.0  | 0.0        | 0.0  | 255.01      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 253.65        | 2.54     | 0.0  | 0.0       | 256.19 | 0.0  | 0.0        | 0.0  | 256.19      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 16.21    | 0.0  | 0.0       | 1016.21 | 0.0  | 0.0        | 0.0  | 1016.21     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |

  @TestRailId:C3970
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC3: interest recalculation, overdue installment, interest rate 12->0 on disbursement day
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 753.72          | 246.28        | 10.0     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 2  | 31   | 01 June 2025   |           | 504.98          | 248.74        | 7.54     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 3  | 30   | 01 July 2025   |           | 253.75          | 251.23        | 5.05     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 253.75        | 2.54     | 0.0  | 0.0       | 256.29 | 0.0  | 0.0        | 0.0  | 256.29      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 25.13    | 0.0  | 0.0       | 1025.13 | 0.0  | 0.0        | 0.0  | 1025.13     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 April 2025      | 02 April 2025   |                 |                  |                 |            | 0               |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 May 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 503.62          | 246.38        | 8.63     | 0.0  | 0.0       | 255.01 | 0.0  | 0.0        | 0.0  | 255.01      |
      | 3  | 30   | 01 July 2025   |           | 253.65          | 249.97        | 5.04     | 0.0  | 0.0       | 255.01 | 0.0  | 0.0        | 0.0  | 255.01      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 253.65        | 2.54     | 0.0  | 0.0       | 256.19 | 0.0  | 0.0        | 0.0  | 256.19      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 16.21    | 0.0  | 0.0       | 1016.21 | 0.0  | 0.0        | 0.0  | 1016.21     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |

  @TestRailId:C3971
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC4: no interest recalculation, overdue installment
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "15 May 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 502.49          | 247.51        | 7.5      | 0.0  | 0.0       | 255.01 | 0.0  | 0.0        | 0.0  | 255.01      |
      | 3  | 30   | 01 July 2025   |           | 252.5           | 249.99        | 5.02     | 0.0  | 0.0       | 255.01 | 0.0  | 0.0        | 0.0  | 255.01      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 252.5         | 2.52     | 0.0  | 0.0       | 255.02 | 0.0  | 0.0        | 0.0  | 255.02      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 15.04    | 0.0  | 0.0       | 1015.04 | 0.0  | 0.0        | 0.0  | 1015.04     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |

  @TestRailId:C3972
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC5: change interest rate fails on loan outside min and max set on loan product
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_MIN_INT_3_MAX_INT_20 | 01 April 2025     | 1000           | 6                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 April 2025    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "15 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan reschedule with the following data results a 400 error and "NEW_INTEREST_RATE_IS_1_BUT_MINIMUM_IS_3" error message
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 1               |
    Then Loan reschedule with the following data results a 400 error and "NEW_INTEREST_RATE_IS_45_BUT_MAXIMUM_IS_20" error message
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 45              |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 751.87          | 248.13        | 5.0      | 0.0  | 0.0       | 253.13 | 0.0  | 0.0        | 0.0  | 253.13      |
      | 2  | 31   | 01 June 2025   |           | 504.86          | 247.01        | 8.64     | 0.0  | 0.0       | 255.65 | 0.0  | 0.0        | 0.0  | 255.65      |
      | 3  | 30   | 01 July 2025   |           | 254.26          | 250.6         | 5.05     | 0.0  | 0.0       | 255.65 | 0.0  | 0.0        | 0.0  | 255.65      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 254.26        | 2.54     | 0.0  | 0.0       | 256.8  | 0.0  | 0.0        | 0.0  | 256.8       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 21.23    | 0.0  | 0.0       | 1021.23 | 0.0  | 0.0        | 0.0  | 1021.23     |

  @TestRailId:C3973
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC6: Minimal Interest Rate
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 May 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 0.01            |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.01          | 249.99        | 0.01     | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |           | 250.01          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.01        | 0.0      | 0.0  | 0.0       | 250.01 | 0.0  | 0.0        | 0.0  | 250.01      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.01     | 0.0  | 0.0       | 1000.01 | 0.0  | 0.0        | 0.0  | 1000.01     |

  @TestRailId:C3974
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC7: Single Digit Interest
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 May 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 1               |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 500.23          | 249.77        | 0.65     | 0.0  | 0.0       | 250.42 | 0.0  | 0.0        | 0.0  | 250.42      |
      | 3  | 30   | 01 July 2025   |           | 250.23          | 250.0         | 0.42     | 0.0  | 0.0       | 250.42 | 0.0  | 0.0        | 0.0  | 250.42      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 250.23        | 0.21     | 0.0  | 0.0       | 250.44 | 0.0  | 0.0        | 0.0  | 250.44      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 1.28     | 0.0  | 0.0       | 1001.28 | 0.0  | 0.0        | 0.0  | 1001.28     |

  @TestRailId:C3975
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC8: Immediate Reschedule
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 April 2025      | 02 April 2025   |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 753.72          | 246.28        | 10.0     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 2  | 31   | 01 June 2025   |           | 504.98          | 248.74        | 7.54     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 3  | 30   | 01 July 2025   |           | 253.75          | 251.23        | 5.05     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 253.75        | 2.54     | 0.0  | 0.0       | 256.29 | 0.0  | 0.0        | 0.0  | 256.29      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 25.13    | 0.0  | 0.0       | 1025.13 | 0.0  | 0.0        | 0.0  | 1025.13     |

  @TestRailId:C3976
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC9: Mid-Loan Reschedule with repayments
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 250.0 EUR transaction amount
    When Admin sets the business date to "01 June 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 June 2025" with 250.0 EUR transaction amount
    When Admin sets the business date to "05 June 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 June 2025       | 02 June 2025    |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025    | 01 May 2025  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025   | 01 June 2025 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 01 July 2025   |              | 251.24          | 248.76        | 5.0      | 0.0  | 0.0       | 253.76 | 0.0   | 0.0        | 0.0  | 253.76      |
      | 4  | 31   | 01 August 2025 |              | 0.0             | 251.24        | 2.51     | 0.0  | 0.0       | 253.75 | 0.0   | 0.0        | 0.0  | 253.75      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 7.51     | 0.0  | 0.0       | 1007.51 | 500.0 | 0.0        | 0.0  | 507.51      |

  @TestRailId:C3977
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC10: Late Stage Reschedule with repayment and overdue installment
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 250.0 EUR transaction amount
    When Admin sets the business date to "01 June 2025"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "01 July 2025"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 July 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 July 2025       | 02 July 2025    |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |             | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 May 2025    | 01 May 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 June 2025   |             | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 01 July 2025   |             | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 August 2025 |             | 0.0             | 250.0         | 3.15     | 0.0  | 0.0       | 253.15 | 0.0   | 0.0        | 0.0  | 253.15      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 3.15     | 0.0  | 0.0       | 1003.15 | 250.0 | 0.0        | 0.0  | 753.15      |

  @TestRailId:C3978
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC11: Sequential Rate Increases
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 May 2025"
    And Admin runs inline COB job for Loan
    # First reschedule: 0% -> 6%
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | 6               |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 501.4           | 248.6         | 3.91     | 0.0  | 0.0       | 252.51 | 0.0  | 0.0        | 0.0  | 252.51      |
      | 3  | 30   | 01 July 2025   |           | 251.4           | 250.0         | 2.51     | 0.0  | 0.0       | 252.51 | 0.0  | 0.0        | 0.0  | 252.51      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 251.4         | 1.26     | 0.0  | 0.0       | 252.66 | 0.0  | 0.0        | 0.0  | 252.66      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 7.68     | 0.0  | 0.0       | 1007.68 | 0.0  | 0.0        | 0.0  | 1007.68     |
    When Admin sets the business date to "01 June 2025"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 June 2025"
    And Admin runs inline COB job for Loan
    # Second reschedule: 6% -> 12%
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 June 2025       | 02 June 2025    |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 May 2025    |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 June 2025   |           | 502.49          | 247.51        | 5.0      | 0.0  | 0.0       | 252.51 | 0.0  | 0.0        | 0.0  | 252.51      |
      | 3  | 30   | 01 July 2025   |           | 253.79          | 248.7         | 5.69     | 0.0  | 0.0       | 254.39 | 0.0  | 0.0        | 0.0  | 254.39      |
      | 4  | 31   | 01 August 2025 |           | 0.0             | 253.79        | 2.54     | 0.0  | 0.0       | 256.33 | 0.0  | 0.0        | 0.0  | 256.33      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 13.23    | 0.0  | 0.0       | 1013.23 | 0.0  | 0.0        | 0.0  | 1013.23     |

  @TestRailId:C3979
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC12: Negative Rate Validation
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    Then Loan reschedule with the following data results a 400 error and "LOAN_INTEREST_RATE_CANNOT_BE_NEGATIVE" error message
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 May 2025        | 02 May 2025     |                 |                  |                 |            | -1              |

  @TestRailId:C3980
  Scenario: Verify repayment schedule after reschedule with interest rate change 0->12 - UC13: weekly repayments
    When Admin sets the business date to "01 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 April 2025     | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | WEEKS                 | 1              | WEEKS                  | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 April 2025" with "1000" amount and expected disbursement date on "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "08 April 2025"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "10 April 2025"
    And Admin runs inline COB job for Loan
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 09 April 2025      | 09 April 2025   |                 |                  |                 |            | 12              |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 April 2025 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 7    | 08 April 2025 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 7    | 15 April 2025 |           | 500.75          | 249.25        | 1.92     | 0.0  | 0.0       | 251.17 | 0.0  | 0.0        | 0.0  | 251.17      |
      | 3  | 7    | 22 April 2025 |           | 250.75          | 250.0         | 1.17     | 0.0  | 0.0       | 251.17 | 0.0  | 0.0        | 0.0  | 251.17      |
      | 4  | 7    | 29 April 2025 |           | 0.0             | 250.75        | 0.59     | 0.0  | 0.0       | 251.34 | 0.0  | 0.0        | 0.0  | 251.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 3.68     | 0.0  | 0.0       | 1003.68 | 0.0  | 0.0        | 0.0  | 1003.68     |

  @TestRailId:C4625
  Scenario: Verify loan closure after MIR, backdated interest rate change and repayment reversal
    When Admin sets the business date to "03 October 2025"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 03 October 2025   | 231.59         | 35.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "03 October 2025" with "231.59" amount and expected disbursement date on "03 October 2025"
    And Admin successfully disburse the loan on "03 October 2025" with "231.59" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.79          | 35.8          | 6.95     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 2  | 30   | 03 December 2025 |           | 158.91          | 36.88         | 5.87     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 3  | 31   | 03 January 2026  |           | 120.93          | 37.98         | 4.77     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 4  | 31   | 03 February 2026 |           | 81.81           | 39.12         | 3.63     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 5  | 28   | 03 March 2026    |           | 41.51           | 40.3          | 2.45     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 41.51         | 1.24     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 231.59        | 24.91    | 0.0  | 0.0       | 256.5 | 0.0  | 0.0        | 0.0  | 256.5       |
    When Admin sets the business date to "15 October 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 October 2025" with 220.83 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 213.75          | 17.84         | 2.89     | 0.0  | 0.0       | 20.73 | 9.65  | 9.65       | 0.0  | 11.08       |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 171.0           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 128.25          | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 85.5            | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 231.59        | 2.89     | 0.0  | 0.0       | 234.48 | 223.4 | 223.4      | 0.0  | 11.08       |
    When Admin sets the business date to "30 October 2025"
    And Customer makes "AUTOPAY" repayment on "30 October 2025" with 11.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 213.75          | 17.84         | 2.84     | 0.0  | 0.0       | 20.68 | 20.68 | 20.68      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 171.0           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 128.25          | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 85.5            | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.84     | 0.0  | 0.0       | 234.43 | 234.43 | 234.43     | 0.0  | 0.0         |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 04 October 2025    | 30 October 2025 |                 |                  |                 |            | 25.99           |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 207.9           | 23.69         | 2.05     | 0.0  | 0.0       | 25.74 | 25.74 | 25.74      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 166.32          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.05     | 0.0  | 0.0       | 233.64 | 233.64 | 233.64     | 0.0  | 0.0         |
    And Admin sets the business date to "06 November 2025"
    And Admin makes Credit Balance Refund transaction on "06 November 2025" with 0.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 207.9           | 23.69         | 2.05     | 0.0  | 0.0       | 25.74 | 25.74 | 25.74      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 166.32          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.05     | 0.0  | 0.0       | 233.64 | 233.64 | 233.64     | 0.0  | 0.0         |
    When Admin sets the business date to "07 November 2025"
    And Customer undo "1"th "Repayment" transaction made on "30 October 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 207.9           | 23.69         | 2.1      | 0.0  | 0.0       | 25.79 | 14.78 | 14.78      | 0.0  | 11.01       |
      | 2  | 30   | 03 December 2025 |                 | 166.32          | 41.62         | 0.03     | 0.0  | 0.0       | 41.65 | 41.58 | 41.58      | 0.0  | 0.07        |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.63        | 2.13     | 0.0  | 0.0       | 233.76 | 222.68 | 222.68     | 0.0  | 11.08       |
    When Admin sets the business date to "10 November 2025"
    And Customer makes "AUTOPAY" repayment on "10 November 2025" with 11.05 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 03 October 2025  |                  | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 03 November 2025 | 10 November 2025 | 207.9           | 23.69         | 2.1      | 0.0  | 0.0       | 25.79 | 25.79 | 14.78      | 11.01 | 0.0         |
      | 2  | 30   | 03 December 2025 | 10 November 2025 | 166.32          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 41.62 | 41.62      | 0.0   | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025  | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025  | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025  | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025  | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 231.63        | 2.1      | 0.0  | 0.0       | 233.73 | 233.73 | 222.72     | 11.01 | 0.0         |
    And Loan is closed with zero outstanding balance and it's all installments have obligations met


  @TestRailId:C4626
  Scenario: Verify loan closure after MIR, backdated interest rate change and repayment reversal, allocation rule: LAST INSTALLMENT
    When Admin sets the business date to "03 October 2025"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 03 October 2025   | 231.59         | 35.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "03 October 2025" with "231.59" amount and expected disbursement date on "03 October 2025"
    And Admin successfully disburse the loan on "03 October 2025" with "231.59" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.79          | 35.8          | 6.95     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 2  | 30   | 03 December 2025 |           | 158.91          | 36.88         | 5.87     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 3  | 31   | 03 January 2026  |           | 120.93          | 37.98         | 4.77     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 4  | 31   | 03 February 2026 |           | 81.81           | 39.12         | 3.63     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 5  | 28   | 03 March 2026    |           | 41.51           | 40.3          | 2.45     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 41.51         | 1.24     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 231.59        | 24.91    | 0.0  | 0.0       | 256.5 | 0.0  | 0.0        | 0.0  | 256.5       |
    When Admin sets the business date to "15 October 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 October 2025" with 220.83 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 213.75          | 17.84         | 2.89     | 0.0  | 0.0       | 20.73 | 9.65  | 9.65       | 0.0  | 11.08       |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 171.0           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 128.25          | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 85.5            | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 231.59        | 2.89     | 0.0  | 0.0       | 234.48 | 223.4 | 223.4      | 0.0  | 11.08       |
    When Admin sets the business date to "30 October 2025"
    And Customer makes "AUTOPAY" repayment on "30 October 2025" with 11.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 213.75          | 17.84         | 2.84     | 0.0  | 0.0       | 20.68 | 20.68 | 20.68      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 171.0           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 128.25          | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 85.5            | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.84     | 0.0  | 0.0       | 234.43 | 234.43 | 234.43     | 0.0  | 0.0         |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 04 October 2025    | 30 October 2025 |                 |                  |                 |            | 25.99           |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 207.9           | 23.69         | 2.05     | 0.0  | 0.0       | 25.74 | 25.74 | 25.74      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 166.32          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.05     | 0.0  | 0.0       | 233.64 | 233.64 | 233.64     | 0.0  | 0.0         |
    And Admin sets the business date to "06 November 2025"
    And Admin makes Credit Balance Refund transaction on "06 November 2025" with 0.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 207.9           | 23.69         | 2.05     | 0.0  | 0.0       | 25.74 | 25.74 | 25.74      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 166.32          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.05     | 0.0  | 0.0       | 233.64 | 233.64 | 233.64     | 0.0  | 0.0         |
    When Admin sets the business date to "07 November 2025"
    And Customer undo "1"th "Repayment" transaction made on "30 October 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 207.9           | 23.69         | 2.1      | 0.0  | 0.0       | 25.79 | 14.78 | 14.78      | 0.0  | 11.01       |
      | 2  | 30   | 03 December 2025 |                 | 166.32          | 41.62         | 0.03     | 0.0  | 0.0       | 41.65 | 41.58 | 41.58      | 0.0  | 0.07        |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.63        | 2.13     | 0.0  | 0.0       | 233.76 | 222.68 | 222.68     | 0.0  | 11.08       |
    When Admin sets the business date to "10 November 2025"
    And Customer makes "AUTOPAY" repayment on "10 November 2025" with 11.05 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 03 October 2025  |                  | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 03 November 2025 | 10 November 2025 | 207.9           | 23.69         | 2.1      | 0.0  | 0.0       | 25.79 | 25.79 | 14.78      | 11.01 | 0.0         |
      | 2  | 30   | 03 December 2025 | 10 November 2025 | 166.32          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 41.62 | 41.62      | 0.0   | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025  | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025  | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025  | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025  | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 231.63        | 2.1      | 0.0  | 0.0       | 233.73 | 233.73 | 222.72     | 11.01 | 0.0         |
    And Loan is closed with zero outstanding balance and it's all installments have obligations met
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C4679
  Scenario: Verify loan closure after MIR, backdated interest rate change, repayment reversal and COB recalculation
    When Admin sets the business date to "03 October 2025"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 03 October 2025   | 231.59         | 35.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "03 October 2025" with "231.59" amount and expected disbursement date on "03 October 2025"
    And Admin successfully disburse the loan on "03 October 2025" with "231.59" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.79          | 35.8          | 6.95     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 2  | 30   | 03 December 2025 |           | 158.91          | 36.88         | 5.87     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 3  | 31   | 03 January 2026  |           | 120.93          | 37.98         | 4.77     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 4  | 31   | 03 February 2026 |           | 81.81           | 39.12         | 3.63     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 5  | 28   | 03 March 2026    |           | 41.51           | 40.3          | 2.45     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 41.51         | 1.24     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 231.59        | 24.91    | 0.0  | 0.0       | 256.5 | 0.0  | 0.0        | 0.0  | 256.5       |
    When Admin sets the business date to "15 October 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 October 2025" with 220.83 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 213.75          | 17.84         | 2.89     | 0.0  | 0.0       | 20.73 | 9.65  | 9.65       | 0.0  | 11.08       |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 171.0           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 128.25          | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 85.5            | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 231.59        | 2.89     | 0.0  | 0.0       | 234.48 | 223.4 | 223.4      | 0.0  | 11.08       |
    When Admin sets the business date to "30 October 2025"
    And Customer makes "AUTOPAY" repayment on "30 October 2025" with 11.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 213.75          | 17.84         | 2.84     | 0.0  | 0.0       | 20.68 | 20.68 | 20.68      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 171.0           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 128.25          | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 85.5            | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.84     | 0.0  | 0.0       | 234.43 | 234.43 | 234.43     | 0.0  | 0.0         |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 04 October 2025    | 30 October 2025 |                 |                  |                 |            | 25.99           |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 207.9           | 23.69         | 2.05     | 0.0  | 0.0       | 25.74 | 25.74 | 25.74      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 166.32          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.05     | 0.0  | 0.0       | 233.64 | 233.64 | 233.64     | 0.0  | 0.0         |
    And Admin sets the business date to "06 November 2025"
    And Admin makes Credit Balance Refund transaction on "06 November 2025" with 0.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 207.9           | 23.69         | 2.05     | 0.0  | 0.0       | 25.74 | 25.74 | 25.74      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 166.32          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.05     | 0.0  | 0.0       | 233.64 | 233.64 | 233.64     | 0.0  | 0.0         |
    When Admin sets the business date to "07 November 2025"
    And Customer undo "1"th "Repayment" transaction made on "30 October 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 207.9           | 23.69         | 2.1      | 0.0  | 0.0       | 25.79 | 14.78 | 14.78      | 0.0  | 11.01       |
      | 2  | 30   | 03 December 2025 |                 | 166.32          | 41.62         | 0.03     | 0.0  | 0.0       | 41.65 | 41.58 | 41.58      | 0.0  | 0.07        |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.63        | 2.13     | 0.0  | 0.0       | 233.76 | 222.68 | 222.68     | 0.0  | 11.08       |
    When Admin sets the business date to "08 November 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 207.9           | 23.69         | 2.1      | 0.0  | 0.0       | 25.79 | 14.78 | 14.78      | 0.0  | 11.01       |
      | 2  | 30   | 03 December 2025 |                 | 166.32          | 41.62         | 0.04     | 0.0  | 0.0       | 41.66 | 41.58 | 41.58      | 0.0  | 0.08        |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.63        | 2.14     | 0.0  | 0.0       | 233.77 | 222.68 | 222.68     | 0.0  | 11.09       |
    When Admin sets the business date to "10 November 2025"
    And Customer makes "AUTOPAY" repayment on "10 November 2025" with 11.05 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 03 October 2025  |                  | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 03 November 2025 | 10 November 2025 | 207.9           | 23.69         | 2.1      | 0.0  | 0.0       | 25.79 | 25.79 | 14.78      | 11.01 | 0.0         |
      | 2  | 30   | 03 December 2025 | 10 November 2025 | 166.32          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 41.62 | 41.62      | 0.0   | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025  | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025  | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025  | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025  | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0   | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 231.63        | 2.1      | 0.0  | 0.0       | 233.73 | 233.73 | 222.72     | 11.01 | 0.0         |
    And Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4680
  Scenario: Verify overdueBalanceCorrectionAmount reset after MIR reversal with backdated interest rate change
    When Admin sets the business date to "03 October 2025"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 03 October 2025   | 231.59         | 35.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "03 October 2025" with "231.59" amount and expected disbursement date on "03 October 2025"
    And Admin successfully disburse the loan on "03 October 2025" with "231.59" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.79          | 35.8          | 6.95     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 2  | 30   | 03 December 2025 |           | 158.91          | 36.88         | 5.87     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 3  | 31   | 03 January 2026  |           | 120.93          | 37.98         | 4.77     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 4  | 31   | 03 February 2026 |           | 81.81           | 39.12         | 3.63     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 5  | 28   | 03 March 2026    |           | 41.51           | 40.3          | 2.45     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 41.51         | 1.24     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 231.59        | 24.91    | 0.0  | 0.0       | 256.5 | 0.0  | 0.0        | 0.0  | 256.5       |
    When Admin sets the business date to "15 October 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 October 2025" with 220.83 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 213.75          | 17.84         | 2.89     | 0.0  | 0.0       | 20.73 | 9.65  | 9.65       | 0.0  | 11.08       |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 171.0           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 128.25          | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 85.5            | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 231.59        | 2.89     | 0.0  | 0.0       | 234.48 | 223.4 | 223.4      | 0.0  | 11.08       |
    When Admin sets the business date to "30 October 2025"
    And Customer makes "AUTOPAY" repayment on "30 October 2025" with 11.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 213.75          | 17.84         | 2.84     | 0.0  | 0.0       | 20.68 | 20.68 | 20.68      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 171.0           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 128.25          | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 85.5            | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.84     | 0.0  | 0.0       | 234.43 | 234.43 | 234.43     | 0.0  | 0.0         |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 04 October 2025    | 30 October 2025 |                 |                  |                 |            | 25.99           |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 207.9           | 23.69         | 2.05     | 0.0  | 0.0       | 25.74 | 25.74 | 25.74      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 166.32          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.05     | 0.0  | 0.0       | 233.64 | 233.64 | 233.64     | 0.0  | 0.0         |
    And Admin sets the business date to "06 November 2025"
    And Admin makes Credit Balance Refund transaction on "06 November 2025" with 0.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 | 30 October 2025 | 207.9           | 23.69         | 2.05     | 0.0  | 0.0       | 25.74 | 25.74 | 25.74      | 0.0  | 0.0         |
      | 2  | 30   | 03 December 2025 | 15 October 2025 | 166.32          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 3  | 31   | 03 January 2026  | 15 October 2025 | 124.74          | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 4  | 31   | 03 February 2026 | 15 October 2025 | 83.16           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 2.05     | 0.0  | 0.0       | 233.64 | 233.64 | 233.64     | 0.0  | 0.0         |
    When Admin sets the business date to "07 November 2025"
    And Customer undo "1"th "Merchant Issued Refund" transaction made on "15 October 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.01          | 36.58         | 5.0      | 0.0  | 0.0       | 41.58 | 11.04 | 11.04      | 0.0  | 30.54       |
      | 2  | 30   | 03 December 2025 |           | 157.74          | 37.31         | 4.31     | 0.0  | 0.0       | 41.62 | 0.0   | 0.0        | 0.0  | 41.62       |
      | 3  | 31   | 03 January 2026  |           | 119.58          | 38.16         | 3.42     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 4  | 31   | 03 February 2026 |           | 80.59           | 38.99         | 2.59     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 5  | 28   | 03 March 2026    |           | 40.76           | 39.83         | 1.75     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 40.76         | 0.88     | 0.0  | 0.0       | 41.64 | 0.0   | 0.0        | 0.0  | 41.64       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 231.63        | 17.95    | 0.0  | 0.0       | 249.58 | 11.04 | 11.04      | 0.0  | 238.54      |
    # COB on Nov 8: tests overdueBalanceCorrectionAmount after massive reset + COB recalculation
    When Admin sets the business date to "08 November 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.01          | 36.58         | 5.0      | 0.0  | 0.0       | 41.58 | 11.04 | 11.04      | 0.0  | 30.54       |
      | 2  | 30   | 03 December 2025 |           | 157.76          | 37.29         | 4.33     | 0.0  | 0.0       | 41.62 | 0.0   | 0.0        | 0.0  | 41.62       |
      | 3  | 31   | 03 January 2026  |           | 119.6           | 38.16         | 3.42     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 4  | 31   | 03 February 2026 |           | 80.61           | 38.99         | 2.59     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 5  | 28   | 03 March 2026    |           | 40.78           | 39.83         | 1.75     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 40.78         | 0.88     | 0.0  | 0.0       | 41.66 | 0.0   | 0.0        | 0.0  | 41.66       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 231.63        | 17.97    | 0.0  | 0.0       | 249.6  | 11.04 | 11.04      | 0.0  | 238.56      |
    When Loan Pay-off is made on "08 November 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4681
  Scenario: Verify overdueBalanceCorrectionAmount with partial MIR and multiple overdue periods
    When Admin sets the business date to "03 October 2025"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 03 October 2025   | 231.59         | 35.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "03 October 2025" with "231.59" amount and expected disbursement date on "03 October 2025"
    And Admin successfully disburse the loan on "03 October 2025" with "231.59" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.79          | 35.8          | 6.95     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 2  | 30   | 03 December 2025 |           | 158.91          | 36.88         | 5.87     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 3  | 31   | 03 January 2026  |           | 120.93          | 37.98         | 4.77     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 4  | 31   | 03 February 2026 |           | 81.81           | 39.12         | 3.63     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 5  | 28   | 03 March 2026    |           | 41.51           | 40.3          | 2.45     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 41.51         | 1.24     | 0.0  | 0.0       | 42.75 | 0.0  | 0.0        | 0.0  | 42.75       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 231.59        | 24.91    | 0.0  | 0.0       | 256.5 | 0.0  | 0.0        | 0.0  | 256.5       |
    When Admin sets the business date to "15 October 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 October 2025" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 193.95          | 37.64         | 5.11     | 0.0  | 0.0       | 42.75 | 1.16  | 1.16       | 0.0  | 41.59       |
      | 2  | 30   | 03 December 2025 |                 | 154.02          | 39.93         | 2.82     | 0.0  | 0.0       | 42.75 | 0.0   | 0.0        | 0.0  | 42.75       |
      | 3  | 31   | 03 January 2026  |                 | 112.89          | 41.13         | 1.62     | 0.0  | 0.0       | 42.75 | 0.0   | 0.0        | 0.0  | 42.75       |
      | 4  | 31   | 03 February 2026 |                 | 85.5            | 27.39         | 0.39     | 0.0  | 0.0       | 27.78 | 14.5  | 14.5       | 0.0  | 13.28       |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 42.75           | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 42.75         | 0.0      | 0.0  | 0.0       | 42.75 | 42.75 | 42.75      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 9.94     | 0.0  | 0.0       | 241.53 | 101.16 | 101.16     | 0.0  | 140.37      |
    When Admin sets the business date to "30 October 2025"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 04 October 2025    | 30 October 2025 |                 |                  |                 |            | 25.99           |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 193.7           | 37.89         | 3.69     | 0.0  | 0.0       | 41.58 | 0.84  | 0.84       | 0.0  | 40.74       |
      | 2  | 30   | 03 December 2025 |                 | 154.15          | 39.55         | 2.03     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 3  | 31   | 03 January 2026  |                 | 113.74          | 40.41         | 1.17     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 4  | 31   | 03 February 2026 |                 | 83.16           | 30.58         | 0.3      | 0.0  | 0.0       | 30.88 | 16.84 | 16.84      | 0.0  | 14.04       |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 7.19     | 0.0  | 0.0       | 238.78 | 100.84 | 100.84     | 0.0  | 137.94      |
    # COB on Dec 4: triggers recalculateModelOverdueAmountsTillDate with 2 overdue periods
    When Admin sets the business date to "04 December 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |                 | 231.59          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 03 November 2025 |                 | 193.7           | 37.89         | 3.69     | 0.0  | 0.0       | 41.58 | 0.84  | 0.84       | 0.0  | 40.74       |
      | 2  | 30   | 03 December 2025 |                 | 154.97          | 38.73         | 2.85     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 3  | 31   | 03 January 2026  |                 | 114.63          | 40.34         | 1.24     | 0.0  | 0.0       | 41.58 | 0.0   | 0.0        | 0.0  | 41.58       |
      | 4  | 31   | 03 February 2026 |                 | 83.16           | 31.47         | 0.32     | 0.0  | 0.0       | 31.79 | 16.84 | 16.84      | 0.0  | 14.95       |
      | 5  | 28   | 03 March 2026    | 15 October 2025 | 41.58           | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
      | 6  | 31   | 03 April 2026    | 15 October 2025 | 0.0             | 41.58         | 0.0      | 0.0  | 0.0       | 41.58 | 41.58 | 41.58      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 231.59        | 8.1      | 0.0  | 0.0       | 239.69 | 100.84 | 100.84     | 0.0  | 138.85      |
    When Admin sets the business date to "05 December 2025"
    And Customer undo "1"th "Merchant Issued Refund" transaction made on "15 October 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.03          | 36.56         | 5.02     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 2  | 30   | 03 December 2025 |           | 158.47          | 36.56         | 5.02     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 3  | 31   | 03 January 2026  |           | 120.42          | 38.05         | 3.53     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 4  | 31   | 03 February 2026 |           | 81.45           | 38.97         | 2.61     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 5  | 28   | 03 March 2026    |           | 41.63           | 39.82         | 1.76     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 41.63         | 0.9      | 0.0  | 0.0       | 42.53 | 0.0  | 0.0        | 0.0  | 42.53       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 231.59        | 18.84    | 0.0  | 0.0       | 250.43 | 0.0  | 0.0        | 0.0  | 250.43      |
    # COB after reversal: tests that overdueBalanceCorrectionAmount is properly rebuilt for all overdue periods
    When Admin sets the business date to "06 December 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 03 October 2025  |           | 231.59          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 03 November 2025 |           | 195.03          | 36.56         | 5.02     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 2  | 30   | 03 December 2025 |           | 158.47          | 36.56         | 5.02     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 3  | 31   | 03 January 2026  |           | 120.48          | 37.99         | 3.59     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 4  | 31   | 03 February 2026 |           | 81.51           | 38.97         | 2.61     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 5  | 28   | 03 March 2026    |           | 41.7            | 39.81         | 1.77     | 0.0  | 0.0       | 41.58 | 0.0  | 0.0        | 0.0  | 41.58       |
      | 6  | 31   | 03 April 2026    |           | 0.0             | 41.7          | 0.9      | 0.0  | 0.0       | 42.6  | 0.0  | 0.0        | 0.0  | 42.6        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 231.59        | 18.91    | 0.0  | 0.0       | 250.5 | 0.0  | 0.0        | 0.0  | 250.5       |
    When Loan Pay-off is made on "06 December 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
