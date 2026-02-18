@DelayedScheduleCapturesFeature
Feature: Full Term Tranche - Schedule handling and Calculations

  @TestRailId:C4366
  Scenario: Verify full term tranche interest bearing progressive loan - Schedule handling and Calculations - Disbursement on Installment Date - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 0.0  | 0.0        | 0.0  | 205.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |

  @TestRailId:C4367
  Scenario: Verify full term tranche interest bearing progressive loan - Schedule handling and Calculations - Disbursement mid-period - UC2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement mid-period (Feb 15) ---
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 15 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.59          | 33.07         | 1.13     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |           | 117.58          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |           | 84.31           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |           | 50.78           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |           | 16.92           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.24     | 0.0  | 0.0       | 205.24 | 0.0  | 0.0        | 0.0  | 205.24      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |

  @TestRailId:C4368
  Scenario: Verify full term tranche interest bearing progressive loan - Schedule handling and Calculations - Both disbursements before first repayment - UC3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement before first repayment date (Jan 15) - no term extension ---
    When Admin sets the business date to "15 January 2024"
    When Admin successfully disburse the loan on "15 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 167.02          | 32.98         | 1.22     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 2  | 29   | 01 March 2024    |           | 134.14          | 32.88         | 1.32     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |           | 101.0           | 33.14         | 1.06     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |           | 67.6            | 33.4          | 0.8      | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |           | 33.93           | 33.67         | 0.53     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 33.93         | 0.27     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.2      | 0.0  | 0.0       | 205.2  | 0.0  | 0.0        | 0.0  | 205.2       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |

  @TestRailId:C4482
  Scenario: Verify full term tranche interest bearing progressive loan - payment on Installment date - UC3.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement before first repayment date (Feb 15)
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.53          | 33.13         | 1.07     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |                  | 117.52          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |                  | 84.25           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |                  | 50.72           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |                  | 16.92           | 33.8          | 0.4      | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0   | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.18     | 0.0  | 0.0       | 205.18 | 17.13 | 0.0        | 0.0  | 188.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 15 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4483
  Scenario: Verify full term tranche interest bearing progressive loan - early payment scenario - UC3.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.23           | 16.77         | 0.36     | 0.0  | 0.0       | 17.13  | 17.13 | 17.13      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                 | 67.12           | 16.11         | 1.02     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                 | 50.52           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                 | 33.79           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                 | 16.93           | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.93         | 0.13     | 0.0  | 0.0       | 17.06  | 0.0   | 0.0        | 0.0  | 17.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.71     | 0.0  | 0.0       | 102.71 | 17.13 | 17.13      | 0.0  | 85.58       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 17.13  | 16.77     | 0.36     | 0.0  | 0.0       | 83.23        | false    | false    |
#   --- 2nd disbursement before first repayment date (Feb 15)
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.23           | 16.77         | 0.36     | 0.0  | 0.0       | 17.13  | 17.13 | 17.13      | 0.0  | 0.0         |
      |    |      | 15 February 2024 |                 | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                 | 150.46          | 32.77         | 1.43     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |                 | 117.45          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |                 | 84.18           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |                 | 50.65           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |                 | 16.92           | 33.73         | 0.4      | 0.0  | 0.0       | 34.13  | 0.0   | 0.0        | 0.0  | 34.13       |
      | 7  | 31   | 01 August 2024   |                 | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0   | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.11     | 0.0  | 0.0       | 205.11 | 17.13 | 17.13      | 0.0  | 187.98      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 17.13  | 16.77     | 0.36     | 0.0  | 0.0       | 83.23        | false    | false    |
      | 15 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.23       | false    | false    |

    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4484
  Scenario: Verify full term tranche interest bearing progressive loan - advance payment adj to next - UC3.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 20.0 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.17           | 16.49         | 0.64     | 0.0  | 0.0       | 17.13  | 2.87  | 2.87       | 0.0  | 14.26       |
      | 3  | 31   | 01 April 2024    |                  | 50.57           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.84           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 16.98           | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.98         | 0.13     | 0.0  | 0.0       | 17.11  | 0.0   | 0.0        | 0.0  | 17.11       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.76     | 0.0  | 0.0       | 102.76 | 20.0 | 2.87       | 0.0  | 82.76       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
#   --- 2nd disbursement before first repayment date (Feb 15)
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.51          | 33.15         | 1.05     | 0.0  | 0.0       | 34.2   | 2.87  | 2.87       | 0.0  | 31.33       |
      | 3  | 31   | 01 April 2024    |                  | 117.5           | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |                  | 84.23           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |                  | 50.7            | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |                  | 16.92           | 33.78         | 0.4      | 0.0  | 0.0       | 34.18  | 0.0   | 0.0        | 0.0  | 34.18       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0   | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.16     | 0.0  | 0.0       | 205.16 | 20.0 | 2.87       | 0.0  | 185.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
      | 15 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 180.79       | false    | false    |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4485
  Scenario: Verify full term tranche interest bearing progressive loan - advance payment adj to last - UC3.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE" loan product "DEFAULT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 20.0 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.17           | 16.49         | 0.64     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.55           | 16.62         | 0.51     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.75         | 0.38     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 16.91           | 16.89         | 0.24     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.91         | 0.11     | 0.0  | 0.0       | 17.02  | 2.87  | 2.87       | 0.0  | 14.15       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.67     | 0.0  | 0.0       | 102.67 | 20.0 | 2.87       | 0.0  | 82.67       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
#   --- 2nd disbursement before first repayment date (Feb 15)
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.51          | 33.15         | 1.05     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |                  | 117.48          | 33.03         | 1.17     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |                  | 84.19           | 33.29         | 0.91     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |                  | 50.63           | 33.56         | 0.64     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |                  | 16.92           | 33.71         | 0.38     | 0.0  | 0.0       | 34.09  | 2.87  | 2.87       | 0.0  | 31.22       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0   | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.07     | 0.0  | 0.0       | 205.07 | 20.0 | 2.87       | 0.0  | 185.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
      | 15 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 180.79       | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 34.2 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 150.51          | 33.15         | 1.05     | 0.0  | 0.0       | 34.2   | 34.2  | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 117.48          | 33.03         | 1.17     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |                  | 84.19           | 33.29         | 0.91     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |                  | 50.63           | 33.56         | 0.64     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |                  | 16.92           | 33.71         | 0.38     | 0.0  | 0.0       | 34.09  | 2.87  | 2.87       | 0.0  | 31.22       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0   | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.07     | 0.0  | 0.0       | 205.07 | 54.2 | 2.87       | 0.0  | 150.87      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
      | 15 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 180.79       | false    | false    |
      | 01 March 2024    | Repayment        | 34.2   | 33.15     | 1.05     | 0.0  | 0.0       | 147.64       | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C4486
  Scenario: Verify full term tranche interest bearing progressive loan - payment with MIR trn - UC3.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "01 February 2024" with 20 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.17           | 16.49         | 0.64     | 0.0  | 0.0       | 17.13  | 0.57  | 0.57       | 0.0  | 16.56       |
      | 3  | 31   | 01 April 2024    |                  | 50.55           | 16.62         | 0.51     | 0.0  | 0.0       | 17.13  | 0.57  | 0.57       | 0.0  | 16.56       |
      | 4  | 30   | 01 May 2024      |                  | 33.81           | 16.74         | 0.39     | 0.0  | 0.0       | 17.13  | 0.57  | 0.57       | 0.0  | 16.56       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.87         | 0.26     | 0.0  | 0.0       | 17.13  | 0.57  | 0.57       | 0.0  | 16.56       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.13     | 0.0  | 0.0       | 17.07  | 0.59  | 0.59       | 0.0  | 16.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.72     | 0.0  | 0.0       | 102.72 | 20.0 | 2.87       | 0.0  | 82.72       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Merchant Issued Refund | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
#   --- 2nd disbursement before first repayment date (Feb 15)
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.51          | 33.15         | 1.05     | 0.0  | 0.0       | 34.2   | 0.57  | 0.57       | 0.0  | 33.63       |
      | 3  | 31   | 01 April 2024    |                  | 117.48          | 33.03         | 1.17     | 0.0  | 0.0       | 34.2   | 0.57  | 0.57       | 0.0  | 33.63       |
      | 4  | 30   | 01 May 2024      |                  | 84.19           | 33.29         | 0.91     | 0.0  | 0.0       | 34.2   | 0.57  | 0.57       | 0.0  | 33.63       |
      | 5  | 31   | 01 June 2024     |                  | 50.65           | 33.54         | 0.66     | 0.0  | 0.0       | 34.2   | 0.57  | 0.57       | 0.0  | 33.63       |
      | 6  | 30   | 01 July 2024     |                  | 16.91           | 33.74         | 0.4      | 0.0  | 0.0       | 34.14  | 0.59  | 0.59       | 0.0  | 33.55       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.91         | 0.13     | 0.0  | 0.0       | 17.04  | 0.0   | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.11     | 0.0  | 0.0       | 205.11 | 20.0 | 2.87       | 0.0  | 185.11      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Merchant Issued Refund | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
      | 15 February 2024 | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 180.79       | false    | false    |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:С4487
  Scenario: Verify full term tranche interest bearing progressive loan - DownPayment - UC6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_DOWNPAYMENT | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 5                 | MONTHS                | 1              | MONTHS                 | 5                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 5 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
      | 3  | 29   | 01 March 2024    |           | 45.35           | 14.88         | 0.48     | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
      | 4  | 31   | 01 April 2024    |           | 30.35           | 15.0          | 0.36     | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
      | 5  | 30   | 01 May 2024      |           | 15.23           | 15.12         | 0.24     | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
      | 6  | 31   | 01 June 2024     |           | 0.0             | 15.23         | 0.12     | 0.0  | 0.0       | 15.35 | 0.0  | 0.0        | 0.0  | 15.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.79     | 0.0  | 0.0       | 101.79 | 0.0  | 0.0        | 0.0  | 101.79      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2024" with 25 EUR transaction amount
    Then Loan has 5 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36  | 0.0  | 0.0        | 0.0  | 15.36       |
      | 3  | 29   | 01 March 2024    |                 | 45.35           | 14.88         | 0.48     | 0.0  | 0.0       | 15.36  | 0.0  | 0.0        | 0.0  | 15.36       |
      | 4  | 31   | 01 April 2024    |                 | 30.35           | 15.0          | 0.36     | 0.0  | 0.0       | 15.36  | 0.0  | 0.0        | 0.0  | 15.36       |
      | 5  | 30   | 01 May 2024      |                 | 15.23           | 15.12         | 0.24     | 0.0  | 0.0       | 15.36  | 0.0  | 0.0        | 0.0  | 15.36       |
      | 6  | 31   | 01 June 2024     |                 | 0.0             | 15.23         | 0.12     | 0.0  | 0.0       | 15.35  | 0.0  | 0.0        | 0.0  | 15.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.79     | 0.0  | 0.0       | 101.79 | 25.0 | 0.0        | 0.0  | 76.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment        | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 15.36 EUR transaction amount
    Then Loan has 5 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36  | 15.36 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    |                  | 45.35           | 14.88         | 0.48     | 0.0  | 0.0       | 15.36  | 0.0   | 0.0        | 0.0  | 15.36       |
      | 4  | 31   | 01 April 2024    |                  | 30.35           | 15.0          | 0.36     | 0.0  | 0.0       | 15.36  | 0.0   | 0.0        | 0.0  | 15.36       |
      | 5  | 30   | 01 May 2024      |                  | 15.23           | 15.12         | 0.24     | 0.0  | 0.0       | 15.36  | 0.0   | 0.0        | 0.0  | 15.36       |
      | 6  | 31   | 01 June 2024     |                  | 0.0             | 15.23         | 0.12     | 0.0  | 0.0       | 15.35  | 0.0   | 0.0        | 0.0  | 15.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.79     | 0.0  | 0.0       | 101.79 | 40.36 | 0.0        | 0.0  | 61.43       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment        | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 15.36  | 14.77     | 0.59     | 0.0  | 0.0       | 60.23        | false    | false    |
#  --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36  | 15.36 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 3  | 0    | 01 February 2024 |                  | 135.23          | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 0.0   | 0.0        | 0.0  | 25.0        |
      | 4  | 29   | 01 March 2024    |                  | 105.58          | 29.65         | 1.07     | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 5  | 31   | 01 April 2024    |                  | 75.69           | 29.89         | 0.83     | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 6  | 30   | 01 May 2024      |                  | 45.57           | 30.12         | 0.6      | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 7  | 31   | 01 June 2024     |                  | 15.22           | 30.35         | 0.36     | 0.0  | 0.0       | 30.71  | 0.0   | 0.0        | 0.0  | 30.71       |
      | 8  | 30   | 01 July 2024     |                  | 0.0             | 15.22         | 0.12     | 0.0  | 0.0       | 15.34  | 0.0   | 0.0        | 0.0  | 15.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.57     | 0.0  | 0.0       | 203.57 | 40.36 | 0.0        | 0.0  | 163.21      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment        | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 15.36  | 14.77     | 0.59     | 0.0  | 0.0       | 60.23        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 160.23       | false    | false    |
    And Customer makes "DOWN_PAYMENT" repayment on "01 February 2024" with 25 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36  | 15.36 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 3  | 0    | 01 February 2024 | 01 February 2024 | 135.23          | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 29   | 01 March 2024    |                  | 105.58          | 29.65         | 1.07     | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 5  | 31   | 01 April 2024    |                  | 75.69           | 29.89         | 0.83     | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 6  | 30   | 01 May 2024      |                  | 45.57           | 30.12         | 0.6      | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 7  | 31   | 01 June 2024     |                  | 15.22           | 30.35         | 0.36     | 0.0  | 0.0       | 30.71  | 0.0   | 0.0        | 0.0  | 30.71       |
      | 8  | 30   | 01 July 2024     |                  | 0.0             | 15.22         | 0.12     | 0.0  | 0.0       | 15.34  | 0.0   | 0.0        | 0.0  | 15.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.57     | 0.0  | 0.0       | 203.57 | 65.36 | 0.0        | 0.0  | 138.21      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment        | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 15.36  | 14.77     | 0.59     | 0.0  | 0.0       | 60.23        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 160.23       | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 135.23       | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:С4488
  Scenario: Verify full term tranche interest bearing progressive loan - Auto DownPayment - UC6.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_DOWNPAYMENT_AUTO | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 5                 | MONTHS                | 1              | MONTHS                 | 5                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 5 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36  | 0.0  | 0.0        | 0.0  | 15.36       |
      | 3  | 29   | 01 March 2024    |                 | 45.35           | 14.88         | 0.48     | 0.0  | 0.0       | 15.36  | 0.0  | 0.0        | 0.0  | 15.36       |
      | 4  | 31   | 01 April 2024    |                 | 30.35           | 15.0          | 0.36     | 0.0  | 0.0       | 15.36  | 0.0  | 0.0        | 0.0  | 15.36       |
      | 5  | 30   | 01 May 2024      |                 | 15.23           | 15.12         | 0.24     | 0.0  | 0.0       | 15.36  | 0.0  | 0.0        | 0.0  | 15.36       |
      | 6  | 31   | 01 June 2024     |                 | 0.0             | 15.23         | 0.12     | 0.0  | 0.0       | 15.35  | 0.0  | 0.0        | 0.0  | 15.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.79     | 0.0  | 0.0       | 101.79 | 25.0 | 0.0        | 0.0  | 76.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 15.36 EUR transaction amount
    Then Loan has 5 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36  | 15.36 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    |                  | 45.35           | 14.88         | 0.48     | 0.0  | 0.0       | 15.36  | 0.0   | 0.0        | 0.0  | 15.36       |
      | 4  | 31   | 01 April 2024    |                  | 30.35           | 15.0          | 0.36     | 0.0  | 0.0       | 15.36  | 0.0   | 0.0        | 0.0  | 15.36       |
      | 5  | 30   | 01 May 2024      |                  | 15.23           | 15.12         | 0.24     | 0.0  | 0.0       | 15.36  | 0.0   | 0.0        | 0.0  | 15.36       |
      | 6  | 31   | 01 June 2024     |                  | 0.0             | 15.23         | 0.12     | 0.0  | 0.0       | 15.35  | 0.0   | 0.0        | 0.0  | 15.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.79     | 0.0  | 0.0       | 101.79 | 40.36 | 0.0        | 0.0  | 61.43       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 15.36  | 14.77     | 0.59     | 0.0  | 0.0       | 60.23        | false    | false    |
#  --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36  | 15.36 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 3  | 0    | 01 February 2024 | 01 February 2024 | 135.23          | 25.0          | 0.0      | 0.0  | 0.0       | 25.0   | 25.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 29   | 01 March 2024    |                  | 105.58          | 29.65         | 1.07     | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 5  | 31   | 01 April 2024    |                  | 75.69           | 29.89         | 0.83     | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 6  | 30   | 01 May 2024      |                  | 45.57           | 30.12         | 0.6      | 0.0  | 0.0       | 30.72  | 0.0   | 0.0        | 0.0  | 30.72       |
      | 7  | 31   | 01 June 2024     |                  | 15.22           | 30.35         | 0.36     | 0.0  | 0.0       | 30.71  | 0.0   | 0.0        | 0.0  | 30.71       |
      | 8  | 30   | 01 July 2024     |                  | 0.0             | 15.22         | 0.12     | 0.0  | 0.0       | 15.34  | 0.0   | 0.0        | 0.0  | 15.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.57     | 0.0  | 0.0       | 203.57 | 65.36 | 0.0        | 0.0  | 138.21      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 15.36  | 14.77     | 0.59     | 0.0  | 0.0       | 60.23        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 160.23       | false    | false    |
      | 01 February 2024 | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 135.23       | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4533
  Scenario: Verify full term tranche interest bearing progressive loan with last disbursement undo - UC9
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 20.0 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.17           | 16.49         | 0.64     | 0.0  | 0.0       | 17.13  | 2.87  | 2.87       | 0.0  | 14.26       |
      | 3  | 31   | 01 April 2024    |                  | 50.57           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.84           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 16.98           | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.98         | 0.13     | 0.0  | 0.0       | 17.11  | 0.0   | 0.0        | 0.0  | 17.11       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.76     | 0.0  | 0.0       | 102.76 | 20.0 | 2.87       | 0.0  | 82.76       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
#   --- 2nd disbursement before first repayment date (Feb 15)
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.51          | 33.15         | 1.05     | 0.0  | 0.0       | 34.2   | 2.87  | 2.87       | 0.0  | 31.33       |
      | 3  | 31   | 01 April 2024    |                  | 117.5           | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |                  | 84.23           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |                  | 50.7            | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0   | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |                  | 16.92           | 33.78         | 0.4      | 0.0  | 0.0       | 34.18  | 0.0   | 0.0        | 0.0  | 34.18       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0   | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.16     | 0.0  | 0.0       | 205.16 | 20.0 | 2.87       | 0.0  | 185.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
      | 15 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 180.79       | false    | false    |
    # --- undo last disbursement --- #
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.17           | 16.49         | 0.64     | 0.0  | 0.0       | 17.13  | 2.87  | 2.87       | 0.0  | 14.26       |
      | 3  | 31   | 01 April 2024    |                  | 50.57           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.84           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 16.98           | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.98         | 0.13     | 0.0  | 0.0       | 17.11  | 0.0   | 0.0        | 0.0  | 17.11       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.76     | 0.0  | 0.0       | 102.76 | 20.0 | 2.87       | 0.0  | 82.76       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 20.0   | 19.21     | 0.79     | 0.0  | 0.0       | 80.79        | false    | false    |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4543 @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche with re-aging transaction with default behaviour - UC16
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add re-aging transaction with default behaviour --- #
    When Admin sets the business date to "01 April 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 May 2024   | 6                    |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024     | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024     | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024       |                  | 156.05          | 27.61         | 4.35     | 0.0  | 0.0       | 31.96  | 0.0   | 0.0        | 0.0  | 31.96       |
      | 5  | 31   | 01 June 2024      |                  | 125.32          | 30.73         | 1.23     | 0.0  | 0.0       | 31.96  | 0.0   | 0.0        | 0.0  | 31.96       |
      | 6  | 30   | 01 July 2024      |                  | 94.35           | 30.97         | 0.99     | 0.0  | 0.0       | 31.96  | 0.0   | 0.0        | 0.0  | 31.96       |
      | 7  | 31   | 01 August 2024    |                  | 63.14           | 31.21         | 0.75     | 0.0  | 0.0       | 31.96  | 0.0   | 0.0        | 0.0  | 31.96       |
      | 8  | 31   | 01 September 2024 |                  | 31.68           | 31.46         | 0.5      | 0.0  | 0.0       | 31.96  | 0.0   | 0.0        | 0.0  | 31.96       |
      | 9  | 30   | 01 October 2024   |                  | 0.0             | 31.68         | 0.25     | 0.0  | 0.0       | 31.93  | 0.0   | 0.0        | 0.0  | 31.93       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 8.86     | 0.0  | 0.0       | 208.86 | 17.13 | 0.0        | 0.0  | 191.73      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 April 2024    | Re-age           | 186.56 | 183.66    | 2.9      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4544 @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche with re-aging transaction with equal amortization + outstanding payable interest - UC16.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add re-aging transaction with default behaviour --- #
    When Admin sets the business date to "01 April 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 01 May 2024   | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024     | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024     | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024       |                  | 153.05          | 30.61         | 0.48     | 0.0  | 0.0       | 31.09  | 0.0   | 0.0        | 0.0  | 31.09       |
      | 5  | 31   | 01 June 2024      |                  | 122.44          | 30.61         | 0.48     | 0.0  | 0.0       | 31.09  | 0.0   | 0.0        | 0.0  | 31.09       |
      | 6  | 30   | 01 July 2024      |                  | 91.83           | 30.61         | 0.48     | 0.0  | 0.0       | 31.09  | 0.0   | 0.0        | 0.0  | 31.09       |
      | 7  | 31   | 01 August 2024    |                  | 61.22           | 30.61         | 0.48     | 0.0  | 0.0       | 31.09  | 0.0   | 0.0        | 0.0  | 31.09       |
      | 8  | 31   | 01 September 2024 |                  | 30.61           | 30.61         | 0.48     | 0.0  | 0.0       | 31.09  | 0.0   | 0.0        | 0.0  | 31.09       |
      | 9  | 30   | 01 October 2024   |                  | 0.0             | 30.61         | 0.5      | 0.0  | 0.0       | 31.11  | 0.0   | 0.0        | 0.0  | 31.11       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.69     | 0.0  | 0.0       | 203.69 | 17.13 | 0.0        | 0.0  | 186.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 April 2024    | Re-age           | 186.56 | 183.66    | 2.9      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4545 @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche with re-aging transaction with equal amortization + outstanding full interest - UC16.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add re-aging transaction with default behaviour --- #
    When Admin sets the business date to "01 April 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate   | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 01 May 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024     | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024     | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024       |                  | 153.05          | 30.61         | 0.84     | 0.0  | 0.0       | 31.45  | 0.0   | 0.0        | 0.0  | 31.45       |
      | 5  | 31   | 01 June 2024      |                  | 122.44          | 30.61         | 0.84     | 0.0  | 0.0       | 31.45  | 0.0   | 0.0        | 0.0  | 31.45       |
      | 6  | 30   | 01 July 2024      |                  | 91.83           | 30.61         | 0.84     | 0.0  | 0.0       | 31.45  | 0.0   | 0.0        | 0.0  | 31.45       |
      | 7  | 31   | 01 August 2024    |                  | 61.22           | 30.61         | 0.84     | 0.0  | 0.0       | 31.45  | 0.0   | 0.0        | 0.0  | 31.45       |
      | 8  | 31   | 01 September 2024 |                  | 30.61           | 30.61         | 0.84     | 0.0  | 0.0       | 31.45  | 0.0   | 0.0        | 0.0  | 31.45       |
      | 9  | 30   | 01 October 2024   |                  | 0.0             | 30.61         | 0.84     | 0.0  | 0.0       | 31.45  | 0.0   | 0.0        | 0.0  | 31.45       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.83     | 0.0  | 0.0       | 205.83 | 17.13 | 0.0        | 0.0  | 188.7       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 April 2024    | Re-age           | 188.7  | 183.66    | 5.04     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche with interest pause transaction S11
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    #   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    When Admin sets the business date to "02 February 2024"
    And Create an interest pause period with start date "02 April 2024" and end date "01 May 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 83.52           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 49.92           | 33.6          | 0.66     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 16.05           | 33.87         | 0.39     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.05         | 0.13     | 0.0  | 0.0       | 16.18  | 0.0   | 0.0        | 0.0  | 16.18       |

    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 4.61     | 0.0  | 0.0       | 204.61 | 17.13 | 0.0        | 0.0  | 187.48      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "02 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Customer undo "2"th repayment on "02 February 2024"
    When Admin sets the business date to "28 February 2024"
    When Loan Pay-off is made on "28 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Customer undo "3"th repayment on "28 February 2024"
    When Admin sets the business date to "01 April 2024"
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Customer undo "4"th repayment on "01 April 2024"
    When Admin sets the business date to "02 April 2024"
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Customer undo "5"th repayment on "02 April 2024"
    When Admin sets the business date to "01 May 2024"
    When Loan Pay-off is made on "01 May 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4546 @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche with BuyDownFee transaction - UC15
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_DEFERRED_INCOME | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add buy down fee transaction --- #
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 February 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 February 2024 | Buy Down Fee     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4547 @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche with chargeback transaction - UC13
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_CHARGEBACK | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add chargeback transaction --- #
    When Admin sets the business date to "01 March 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.91          | 49.28         | 2.11     | 0.0  | 0.0       | 51.39  | 0.0   | 0.0        | 0.0  | 51.39       |
      | 4  | 30   | 01 May 2024      |                  | 84.58           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.99           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.13           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.13         | 0.14     | 0.0  | 0.0       | 17.27  | 0.0   | 0.0        | 0.0  | 17.27       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 216.34        | 6.49     | 0.0  | 0.0       | 222.83 | 17.13 | 0.0        | 0.0  | 205.7       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 March 2024    | Chargeback       | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 200.0        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4556 @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche charge-off with regular behaviour - UC7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add charge-off transaction --- #
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 March 2024    | Accrual          | 2.24   | 0.0       | 2.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 188.43 | 183.66    | 4.77     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4557 @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche charge-off with zero interest behaviour - UC7.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_ZERO_INT_CHARGE_OFF | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add charge-off transaction --- #
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 116.59          | 34.26         | 0.0      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 82.33           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 48.07           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 13.81           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 13.81         | 0.0      | 0.0  | 0.0       | 13.81  | 0.0   | 0.0        | 0.0  | 13.81       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 2.24     | 0.0  | 0.0       | 202.24 | 17.13 | 0.0        | 0.0  | 185.11      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 March 2024    | Accrual          | 2.24   | 0.0       | 2.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 185.11 | 183.66    | 1.45     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4558 @AdvancedPaymentAllocation
  Scenario: Verify that Loan full term tranche charge-off with accelerate maturity behaviour - UC7.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_ACCELERATE_MATURITY | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add charge-off transaction --- #
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  |   0.0           | 183.66        | 1.45     | 0.0  | 0.0       | 185.11 | 0.0   | 0.0        | 0.0  | 185.11      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 2.24     | 0.0  | 0.0       | 202.24 | 17.13 | 0.0        | 0.0  | 185.11      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 March 2024    | Accrual          | 2.24   | 0.0       | 2.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 185.11 | 183.66    | 1.45     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4548 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche - backdated repayment triggers reverse-replay - UC10
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 0.0  | 0.0        | 0.0  | 205.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
#   --- Backdated repayment before 2nd disbursement ---
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.23           | 16.77         | 0.36     | 0.0  | 0.0       | 17.13 | 17.13 | 17.13      | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                 | 150.78          | 32.45         | 1.81     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                 | 117.71          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                 | 84.38           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                 | 50.79           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                 | 17.0            | 33.79         | 0.4      | 0.0  | 0.0       | 34.19 | 0.0   | 0.0        | 0.0  | 34.19       |
      | 7  | 31   | 01 August 2024   |                 | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.49     | 0.0  | 0.0       | 205.49 | 17.13 | 17.13      | 0.0  | 188.36      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 17.13  | 16.77     | 0.36     | 0.0  | 0.0       | 83.23        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.23       | false    | false    |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4549 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche - backdated charge triggers reverse-replay - UC11
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- Repayment on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement ---
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
#   --- Backdated NSF charge before repayment ---
    When Admin sets the business date to "15 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 83.66           | 16.34         | 0.79     | 0.0  | 10.0      | 27.13 | 17.13 | 0.0        | 0.0  | 10.0        |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.89          | 32.77         | 1.49     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.82          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.49           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.9            | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.04           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.04         | 0.13     | 0.0  | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.6      | 0.0  | 10.0      | 215.6  | 17.13 | 0.0        | 0.0  | 198.47      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 7.13      | 0.0      | 0.0  | 10.0      | 92.87        | false    | true     |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 192.87       | false    | true     |
    And Loan Charges tab has the following data:
      | Name     | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      |  NSF fee | true      | Specified due date | 20 January 2024  | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4550 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche - backdated reschedule after 2nd disbursement - UC12
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 0.0  | 0.0        | 0.0  | 205.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
#   --- Repayment ---
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 183.66       | false    | false    |
#   --- Reschedule: adjust due date ---
    When Admin sets the business date to "15 March 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 15 March 2024   | 01 April 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 60   | 01 April 2024     |                  | 152.3           | 31.36         | 2.9      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 30   | 01 May 2024       |                  | 119.24          | 33.06         | 1.2      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 31   | 01 June 2024      |                  | 85.92           | 33.32         | 0.94     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 30   | 01 July 2024      |                  | 52.34           | 33.58         | 0.68     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 31   | 01 August 2024    |                  | 18.49           | 33.85         | 0.41     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 September 2024 |                  | 0.0             | 18.49         | 0.15     | 0.0  | 0.0       | 18.64 | 0.0   | 0.0        | 0.0  | 18.64       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 7.07     | 0.0  | 0.0       | 207.07 | 17.13 | 0.0        | 0.0  | 189.94      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4551 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche - backdated interest pause triggers reverse-replay - UC13
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 0.0  | 0.0        | 0.0  | 205.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
#   --- Repayment ---
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 183.66       | false    | false    |
#   --- Backdated interest pause before 2nd disbursement ---
    When Admin sets the business date to "15 March 2024"
    And Create an interest pause period with start date "15 January 2024" and end date "25 January 2024"
    Then LoanScheduleVariationsAddedBusinessEvent is created for interest pause from "15 January 2024" to "25 January 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.38           | 16.62         | 0.51     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.57          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.62          | 32.95         | 1.31     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.29           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.7            | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.12           | 33.58         | 0.4      | 0.0  | 0.0       | 33.98 | 0.0   | 0.0        | 0.0  | 33.98       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.12         | 0.14     | 0.0  | 0.0       | 17.26 | 0.0   | 0.0        | 0.0  | 17.26       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.41     | 0.0  | 0.0       | 205.41 | 17.13 | 0.0        | 0.0  | 188.28      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.62     | 0.51     | 0.0  | 0.0       | 183.38       | false    | true     |
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4552 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche - repayment reversal after 2nd disbursement - UC14
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- Repayment ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement ---
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
#   --- Reversal of repayment ---
    When Admin sets the business date to "15 February 2024"
    When Customer undo "1"th "Repayment" transaction made on "01 February 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.91          | 32.75         | 1.51     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.84          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.51           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.92           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.06           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.06         | 0.13     | 0.0  | 0.0       | 17.19 | 0.0  | 0.0        | 0.0  | 17.19       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.62     | 0.0  | 0.0       | 205.62 | 0.0  | 0.0        | 0.0  | 205.62      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | true     | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4553 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche - charge waive and undo waive after 2nd disbursement - UC15
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- Add charge ---
    When Admin sets the business date to "15 January 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 10.0      | 27.13 | 0.0  | 0.0        | 0.0  | 27.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 10.0      | 112.78 | 0.0  | 0.0        | 0.0  | 112.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Charges tab has the following data:
      | Name     | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      |  NSF fee | true      | Specified due date | 15 January 2024  | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
#   --- 2nd disbursement ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 10.0      | 27.13 | 0.0  | 0.0        | 0.0  | 27.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 10.0      | 215.56 | 0.0  | 0.0        | 0.0  | 215.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    And Loan Charges tab has the following data:
      | Name     | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      |  NSF fee | true      | Specified due date | 15 January 2024  | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
#   --- Waive charge ---
    When Admin sets the business date to "15 February 2024"
    And Admin waives due date charge
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Waived | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |        |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 10.0      | 27.13 | 0.0  | 0.0        | 0.0  | 10.0   | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |        |             |
      | 2  | 29   | 01 March 2024    |           | 150.91          | 32.75         | 1.51     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 0.0    | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.84          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 0.0    | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.51           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 0.0    | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.92           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 0.0    | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.06           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 0.0    | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.06         | 0.13     | 0.0  | 0.0       | 17.19 | 0.0  | 0.0        | 0.0  | 0.0    | 17.19       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late |  Waived | Outstanding |
      | 200.0         | 5.62     | 0.0  | 10.0      | 215.62 | 0.0  | 0.0        | 0.0  |  10.0   | 205.62      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    And Loan Charges tab has the following data:
      | Name     | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      |  NSF fee | true      | Specified due date | 15 January 2024  | Flat             | 10.0 | 0.0  | 10.0   | 0.0        |
#   --- Undo waive charge ---
    When Admin sets the business date to "20 February 2024"
    And Admin makes waive undone for charge
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 10.0      | 27.13 | 0.0  | 0.0        | 0.0  | 27.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.94          | 32.72         | 1.54     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.87          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.54           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.95           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.09           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.09         | 0.14     | 0.0  | 0.0       | 17.23 | 0.0  | 0.0        | 0.0  | 17.23       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late  | Outstanding |
      | 200.0         | 5.66     | 0.0  | 10.0      | 215.66 | 0.0  | 0.0        | 0.0   | 215.66      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 01 February 2024 | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    And Loan Charges tab has the following data:
      | Name     | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      |  NSF fee | true      | Specified due date | 15 January 2024  | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Loan Pay-off is made on "20 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4555 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche - undo last disbursement reverts schedule extension - UC16
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 0.0  | 0.0        | 0.0  | 205.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
#   --- Undo last disbursement ---
    When Admin sets the business date to "15 February 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.25           | 16.41         | 0.72     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.65           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.92           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.06           | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.06         | 0.13     | 0.0  | 0.0       | 17.19 | 0.0  | 0.0        | 0.0  | 17.19       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.84     | 0.0  | 0.0       | 102.84 | 0.0  | 0.0        | 0.0  | 102.84      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4559 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement on installment date - write-off - UC12
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does write-off the loan on "01 March 2024"
    Then Loan status will be "CLOSED_WRITTEN_OFF"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 March 2024    | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 March 2024    | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 March 2024    | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 March 2024    | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2024   | 01 March 2024    | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment              | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 March 2024    | Close (as written-off) | 188.43 | 183.66    | 4.77     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C4560 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement mid-period - write-off - UC12.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement mid-period (Feb 15) ---
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 15 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.59          | 33.07         | 1.13     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |           | 117.58          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |           | 84.31           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |           | 50.78           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |           | 16.92           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.24     | 0.0  | 0.0       | 205.24 | 0.0  | 0.0        | 0.0  | 205.24      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does write-off the loan on "01 March 2024"
    Then Loan status will be "CLOSED_WRITTEN_OFF"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2024 |               | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 March 2024 | 150.66          | 33.0          | 1.2      | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 March 2024 | 117.65          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 March 2024 | 84.38           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 March 2024 | 50.85           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 March 2024 | 16.99           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2024   | 01 March 2024 | 0.0             | 16.99         | 0.13     | 0.0  | 0.0       | 17.12  | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.31     | 0.0  | 0.0       | 205.31 | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024 | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 March 2024    | Close (as written-off) | 205.31 | 200.0     | 5.31     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C4561 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - re-amortization - default behaviour - UC17
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Admin sets the business date to "01 April 2024"
    When Admin creates a Loan re-amortization transaction on current business date
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                  | 140.45          | 43.21         | 4.35     | 0.0  | 0.0       | 47.56 | 0.0   | 0.0        | 0.0  | 47.56       |
      | 5  | 31   | 01 June 2024     |                  | 94.0            | 46.45         | 1.11     | 0.0  | 0.0       | 47.56 | 0.0   | 0.0        | 0.0  | 47.56       |
      | 6  | 30   | 01 July 2024     |                  | 47.18           | 46.82         | 0.74     | 0.0  | 0.0       | 47.56 | 0.0   | 0.0        | 0.0  | 47.56       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 47.18         | 0.37     | 0.0  | 0.0       | 47.55 | 0.0   | 0.0        | 0.0  | 47.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 7.36     | 0.0  | 0.0       | 207.36 | 17.13 | 0.0        | 0.0  | 190.23      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 April 2024    | Re-amortize      | 68.52  | 65.62     | 2.9      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

    #    TODO check and unSkip when WAIVE_INTEREST strategy is implemented
  @Skip
  @TestRailId:C4562 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - re-amortization - waive interest behaviour - UC17.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Admin sets the business date to "01 April 2024"
    When Admin creates a Loan re-amortization transaction on current business date
    # TODO check numbers
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                  | 110.2           | 36.15         | 1.16     | 0.0  | 0.0       | 37.31 | 0.0   | 0.0        | 0.0  | 37.31       |
      | 5  | 31   | 01 June 2024     |                  | 73.76           | 36.44         | 0.87     | 0.0  | 0.0       | 37.31 | 0.0   | 0.0        | 0.0  | 37.31       |
      | 6  | 30   | 01 July 2024     |                  | 37.03           | 36.73         | 0.58     | 0.0  | 0.0       | 37.31 | 0.0   | 0.0        | 0.0  | 37.31       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 37.03         | 0.29     | 0.0  | 0.0       | 37.32 | 0.0   | 0.0        | 0.0  | 37.32       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 6.63     | 0.0  | 0.0       | 206.63 | 17.13 | 0.0        | 0.0  | 189.5       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 April 2024    | Re-amortize      | 34.26  | 32.81     | 1.45     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4563 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - re-amortization - equal interest split behaviour - UC17.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin creates a Loan re-amortization transaction on current business date with reAmortizationInterestHandling "EQUAL_AMORTIZATION_INTEREST_SPLIT"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 183.66          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                  | 138.28          | 45.38         | 2.17     | 0.0  | 0.0       | 47.55 | 0.0   | 0.0        | 0.0  | 47.55       |
      | 5  | 31   | 01 June 2024     |                  | 92.54           | 45.74         | 1.81     | 0.0  | 0.0       | 47.55 | 0.0   | 0.0        | 0.0  | 47.55       |
      | 6  | 30   | 01 July 2024     |                  | 46.44           | 46.1          | 1.45     | 0.0  | 0.0       | 47.55 | 0.0   | 0.0        | 0.0  | 47.55       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 46.44         | 1.09     | 0.0  | 0.0       | 47.53 | 0.0   | 0.0        | 0.0  | 47.53       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 7.31     | 0.0  | 0.0       | 207.31 | 17.13 | 0.0        | 0.0  | 190.18      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 April 2024    | Re-amortize      | 68.52  | 65.62     | 2.9      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4564 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement on installment date - contract termination - UC8
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin successfully terminates loan contract
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 183.66        | 1.45     | 0.0  | 0.0       | 185.11 | 0.0   | 0.0        | 0.0  | 185.11      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 2.24     | 0.0  | 0.0       | 202.24 | 17.13 | 0.0        | 0.0  | 185.11      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment            | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 March 2024    | Accrual              | 2.24   | 0.0       | 2.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Contract Termination | 185.11 | 183.66    | 1.45     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4565 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement mid-period - contract termination - UC8.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement mid-period (Feb 15) ---
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 15 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.59          | 33.07         | 1.13     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |           | 117.58          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |           | 84.31           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |           | 50.78           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |           | 16.92           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.24     | 0.0  | 0.0       | 205.24 | 0.0  | 0.0        | 0.0  | 205.24      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin successfully terminates loan contract
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 15 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 0.0             | 183.66        | 1.2      | 0.0  | 0.0       | 184.86 | 0.0  | 0.0        | 0.0  | 184.86      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 1.99     | 0.0  | 0.0       | 201.99 | 0.0  | 0.0        | 0.0  | 201.99      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024 | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 March 2024    | Accrual              | 1.99   | 0.0       | 1.99     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Contract Termination | 201.99 | 200.0     | 1.99     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4566 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement on installment date - payment holiday - reschedule - UC10
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Admin sets the business date to "01 March 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 May 2024        | 01 May 2024     | 15 May 2024     |                  |                 |            |                 |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 44   | 15 May 2024      |                  | 84.87           | 32.91         | 1.35     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 15 June 2024     |                  | 51.28           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 15 July 2024     |                  | 17.43           | 33.85         | 0.41     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 15 August 2024   |                  | 0.0             | 17.43         | 0.14     | 0.0  | 0.0       | 17.57 | 0.0   | 0.0        | 0.0  | 17.57       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 200.0         | 6.0      | 0.0  | 0.0       | 206.0 | 17.13 | 0.0        | 0.0  | 188.87      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4567 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement mid-period - payment holiday - reschedule - UC10.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement mid-period (Feb 15) ---
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 15 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.59          | 33.07         | 1.13     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |           | 117.58          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |           | 84.31           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |           | 50.78           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |           | 16.92           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.24     | 0.0  | 0.0       | 205.24 | 0.0  | 0.0        | 0.0  | 205.24      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    When Admin sets the business date to "01 March 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 May 2024        | 01 May 2024     | 15 May 2024     |                  |                 |            |                 |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 15 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.66          | 33.0          | 1.2      | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |           | 117.65          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 4  | 44   | 15 May 2024      |           | 84.8            | 32.85         | 1.35     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 15 June 2024     |           | 51.27           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 15 July 2024     |           | 17.42           | 33.85         | 0.41     | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 15 August 2024   |           | 0.0             | 17.42         | 0.14     | 0.0  | 0.0       | 17.56  | 0.0  | 0.0        | 0.0  | 17.56       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.75     | 0.0  | 0.0       | 205.75 | 0.0  | 0.0        | 0.0  | 205.75      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024  | Disbursement    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4568 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement on installment date - interest rate change - reschedule - UC10.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Admin sets the business date to "02 February 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 02 February 2024 |                 |                  |                 |            | 10              |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 153.61          | 30.05         | 1.45     | 0.0  | 0.0       | 31.5  | 0.0   | 0.0        | 0.0  | 31.5        |
      | 3  | 31   | 01 April 2024    |                  | 123.39          | 30.22         | 1.28     | 0.0  | 0.0       | 31.5  | 0.0   | 0.0        | 0.0  | 31.5        |
      | 4  | 30   | 01 May 2024      |                  | 92.92           | 30.47         | 1.03     | 0.0  | 0.0       | 31.5  | 0.0   | 0.0        | 0.0  | 31.5        |
      | 5  | 31   | 01 June 2024     |                  | 62.19           | 30.73         | 0.77     | 0.0  | 0.0       | 31.5  | 0.0   | 0.0        | 0.0  | 31.5        |
      | 6  | 30   | 01 July 2024     |                  | 31.21           | 30.98         | 0.52     | 0.0  | 0.0       | 31.5  | 0.0   | 0.0        | 0.0  | 31.5        |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 31.21         | 0.26     | 0.0  | 0.0       | 31.47 | 0.0   | 0.0        | 0.0  | 31.47       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 200.0         | 6.1      | 0.0  | 0.0       | 206.1 | 17.13 | 0.0        | 0.0  | 188.97      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "02 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4569 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement on installment date - extra terms - reschedule - UC10.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on installment date ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Admin sets the business date to "02 February 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 02 February 2024 |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024     |                  | 161.33          | 22.33         | 1.45     | 0.0  | 0.0       | 23.78 | 0.0   | 0.0        | 0.0  | 23.78       |
      | 3  | 31   | 01 April 2024     |                  | 138.82          | 22.51         | 1.27     | 0.0  | 0.0       | 23.78 | 0.0   | 0.0        | 0.0  | 23.78       |
      | 4  | 30   | 01 May 2024       |                  | 116.14          | 22.68         | 1.1      | 0.0  | 0.0       | 23.78 | 0.0   | 0.0        | 0.0  | 23.78       |
      | 5  | 31   | 01 June 2024      |                  | 93.28           | 22.86         | 0.92     | 0.0  | 0.0       | 23.78 | 0.0   | 0.0        | 0.0  | 23.78       |
      | 6  | 30   | 01 July 2024      |                  | 70.24           | 23.04         | 0.74     | 0.0  | 0.0       | 23.78 | 0.0   | 0.0        | 0.0  | 23.78       |
      | 7  | 31   | 01 August 2024    |                  | 47.02           | 23.22         | 0.56     | 0.0  | 0.0       | 23.78 | 0.0   | 0.0        | 0.0  | 23.78       |
      | 8  | 31   | 01 September 2024 |                  | 23.61           | 23.41         | 0.37     | 0.0  | 0.0       | 23.78 | 0.0   | 0.0        | 0.0  | 23.78       |
      | 9  | 30   | 01 October 2024   |                  | 0.0             | 23.61         | 0.19     | 0.0  | 0.0       | 23.8  | 0.0   | 0.0        | 0.0  | 23.8        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 7.39     | 0.0  | 0.0       | 207.39 | 17.13 | 0.0        | 0.0  | 190.26      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "02 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4572 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement on installment date - capitalized income - UC14
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_DEFERRED_INCOME | 01 January 2024   | 250            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "250" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
#   --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- capitalized income transaction --- #
    When Admin sets the business date to "01 March 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 March 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      |    |      | 01 March 2024    |                  | 50.0            |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 161.31          | 39.54         | 1.59     | 0.0  | 0.0       | 41.13  | 0.0   | 0.0        | 0.0  | 41.13       |
      | 4  | 30   | 01 May 2024      |                  | 121.45          | 39.86         | 1.27     | 0.0  | 0.0       | 41.13  | 0.0   | 0.0        | 0.0  | 41.13       |
      | 5  | 31   | 01 June 2024     |                  | 81.28           | 40.17         | 0.96     | 0.0  | 0.0       | 41.13  | 0.0   | 0.0        | 0.0  | 41.13       |
      | 6  | 30   | 01 July 2024     |                  | 40.79           | 40.49         | 0.64     | 0.0  | 0.0       | 41.13  | 0.0   | 0.0        | 0.0  | 41.13       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 40.79         | 0.32     | 0.0  | 0.0       | 41.11  | 0.0   | 0.0        | 0.0  | 41.11       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 250.0         | 7.02     | 0.0  | 0.0       | 257.02 | 17.13 | 0.0        | 0.0  | 239.89      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 March 2024    | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 233.66       | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4573 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche interest bearing progressive loan - second disbursement mid-period - capitalized income - UC14.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_DEFERRED_INCOME | 01 January 2024   | 250            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "250" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement mid-period (Feb 15) ---
    When Admin sets the business date to "15 February 2024"
    When Admin successfully disburse the loan on "15 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 15 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.59          | 33.07         | 1.13     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |           | 117.58          | 33.01         | 1.19     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |           | 84.31           | 33.27         | 0.93     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |           | 50.78           | 33.53         | 0.67     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |           | 16.92           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 16.92         | 0.13     | 0.0  | 0.0       | 17.05  | 0.0  | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.24     | 0.0  | 0.0       | 205.24 | 0.0  | 0.0        | 0.0  | 205.24      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    # --- capitalized income transaction --- #
    When Admin sets the business date to "01 March 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 March 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 15 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.66          | 33.0          | 1.2      | 0.0  | 0.0       | 34.2  | 0.0  | 0.0        | 0.0  | 34.2        |
      |    |      | 01 March 2024    |           | 50.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 3  | 31   | 01 April 2024    |           | 161.19          | 39.47         | 1.59     | 0.0  | 0.0       | 41.06 | 0.0  | 0.0        | 0.0  | 41.06       |
      | 4  | 30   | 01 May 2024      |           | 121.4           | 39.79         | 1.27     | 0.0  | 0.0       | 41.06 | 0.0  | 0.0        | 0.0  | 41.06       |
      | 5  | 31   | 01 June 2024     |           | 81.3            | 40.1          | 0.96     | 0.0  | 0.0       | 41.06 | 0.0  | 0.0        | 0.0  | 41.06       |
      | 6  | 30   | 01 July 2024     |           | 40.88           | 40.42         | 0.64     | 0.0  | 0.0       | 41.06 | 0.0  | 0.0        | 0.0  | 41.06       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 40.88         | 0.32     | 0.0  | 0.0       | 41.2  | 0.0  | 0.0        | 0.0  | 41.2        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 250.0         | 6.77     | 0.0  | 0.0       | 256.77 | 0.0  | 0.0        | 0.0  | 256.77      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 February 2024 | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 March 2024    | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4583 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche with N+1 instalment handling - charge after maturity date is placed within last installment after 2nd disb - UC4.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
# -- snooze charge on Jan 1, 2024 with due date Jul 15, 1024 --- #
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 July 2024" due date and 10 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 7  | 14   | 15 July 2024     |           | 0.0             |  0.0          | 0.0      | 10.0 | 0.0       | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 10.0 | 0.0       | 112.78 | 0.0  | 0.0        | 0.0  | 112.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at       | Due as of    | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date   | 15 July 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
# ---- repayment on Feb 1, 2024 --- #
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 7  | 14   | 15 July 2024     |                  | 0.0             |  0.0          | 0.0      | 10.0 | 0.0       | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 10.0 | 0.0       | 112.78 | 17.13 | 0.0        | 0.0  | 95.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
# --- 2nd disbursement on installment date ---#
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 10.0 | 0.0       | 27.13 | 0.0   | 0.0        | 0.0  | 27.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 10.0 | 0.0       | 215.56 | 17.13 | 0.0        | 0.0  | 198.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 February 2024 | 149.4           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 February 2024 | 115.14          | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 February 2024 | 80.88           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 February 2024 | 46.62           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 February 2024 | 12.36           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2024   | 01 February 2024 | 0.0             | 12.36         | 0.0      | 10.0 | 0.0       | 22.36 | 22.36 | 22.36      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 0.79     | 10.0 | 0.0       | 210.79 | 210.79 | 193.66     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 February 2024 | Repayment        | 193.66 | 183.66    | 0.0      | 10.0 | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 10.79  | 0.0       | 0.79     | 10.0 | 0.0       | 0.0          | false    | false    |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4584 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche with N+1 instalment handling - charge after maturity date after 2nd disb - UC4.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
# -- snooze charge on Jan 1, 2024 with due date Jul 15, 1024 --- #
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 August 2024" due date and 10 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 7  | 45   | 15 August 2024   |           | 0.0             |  0.0          | 0.0      | 0.0  | 10.0      | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 10.0      | 112.78 | 0.0  | 0.0        | 0.0  | 112.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of      | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 15 August 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
# ---- repayment on Feb 1, 2024 --- #
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 7  | 45   | 15 August 2024   |                  | 0.0             |  0.0          | 0.0      | 0.0  | 10.0      | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 10.0      | 112.78 | 17.13 | 0.0        | 0.0  | 95.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
# --- 2nd disbursement on installment date ---#
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 8  | 14   | 15 August 2024   |                  | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 10.0      | 215.56 | 17.13 | 0.0        | 0.0  | 198.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 February 2024 | 149.4           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 February 2024 | 115.14          | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 February 2024 | 80.88           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 February 2024 | 46.62           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 February 2024 | 12.36           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2024   | 01 February 2024 | 0.0             | 12.36         | 0.0      | 0.0  | 0.0       | 12.36 | 12.36 | 12.36      | 0.0  | 0.0         |
      | 8  | 14   | 15 August 2024   | 01 February 2024 | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 10.0  | 10.0       | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 0.79     | 0.0  | 10.0      | 210.79 | 210.79 | 193.66     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 February 2024 | Repayment        | 193.66 | 183.66    | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 10.79  | 0.0       | 0.79     | 0.0  | 10.0      | 0.0          | false    | false    |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4585 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche with N+1 instalment handling - multiple charges after maturity date - UC4.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
# -- snooze charge on Jan 1, 2024 with due date Jul 15, 1024 --- #
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 July 2024" due date and 10 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 August 2024" due date and 15 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 September 2024" due date and 18 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024  |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024     |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024     |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024       |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024      |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024      |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 7  | 76   | 15 September 2024 |           | 0.0             |  0.0          | 0.0      | 10.0 | 33.0      | 43.0   | 0.0  | 0.0        | 0.0  | 43.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 10.0 | 33.0      | 145.78 | 0.0  | 0.0        | 0.0  | 145.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of         | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 15 July 2024      | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | NSF fee    | true      | Specified due date | 15 August 2024    | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
      | NSF fee    | true      | Specified due date | 15 September 2024 | Flat             | 18.0 | 0.0  | 0.0    | 18.0        |
# ---- repayment on Feb 1, 2024 --- #
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024     |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024       |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024      |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024      |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 7  | 76   | 15 September 2024 |                  | 0.0             |  0.0          | 0.0      | 10.0 | 33.0      | 43.0   | 0.0   | 0.0        | 0.0  | 43.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 10.0 | 33.0      | 145.78 | 17.13 | 0.0        | 0.0  | 128.65      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
# --- 2nd disbursement on installment date ---#
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                   | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024  | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                   | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                   | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                   | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                   | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                   | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                   | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                   | 0.0             | 17.0          | 0.13     | 10.0 | 0.0       | 27.13 | 0.0   | 0.0        | 0.0  | 27.13       |
      | 8  | 45   | 15 September 2024 |                  | 0.0             |  0.0          | 0.0      | 0.0  | 33.0      | 33.0  | 0.0   | 0.0        | 0.0  | 33.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 10.0 | 33.0      | 248.56 | 17.13 | 0.0        | 0.0  | 231.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024     | 01 February 2024 | 149.4           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024     | 01 February 2024 | 115.14          | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024       | 01 February 2024 | 80.88           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024      | 01 February 2024 | 46.62           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024      | 01 February 2024 | 12.36           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 34.26 | 34.26      | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2024    | 01 February 2024 | 0.0             | 12.36         | 0.0      | 10.0 | 0.0       | 22.36 | 22.36 | 22.36      | 0.0  | 0.0         |
      | 8  | 45   | 15 September 2024 | 01 February 2024 | 0.0             |  0.0          | 0.0      | 0.0  | 33.0      | 33.0  | 33.0  | 33.0       | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 0.79     | 10.0 | 33.0      | 243.79 | 243.79 | 226.66     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 February 2024 | Repayment        | 226.66 | 183.66    | 0.0      | 10.0 | 33.0      | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 43.79  | 0.0       | 0.79     | 10.0 | 33.0      | 0.0          | false    | false    |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4586 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche with N+1 instalment handling - chargeback after maturity date after 2nd disb - UC4.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_CHARGEBACK | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
# --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- add chargeback transaction --- #
    When Admin sets the business date to "15 August 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.13 EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 118.04          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 85.23           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 52.42           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 19.61           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 19.61         | 1.45     | 0.0  | 0.0       | 21.06  | 0.0   | 0.0        | 0.0  | 21.06       |
      | 8  | 14   | 15 August 2024   |                  | 0.0             | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 216.34        | 10.28    | 0.0  | 0.0       | 226.62 | 17.13 | 0.0        | 0.0  | 209.49      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 15 August 2024   | Chargeback       | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 200.0        | false    | false    |
    When Loan Pay-off is made on "15 August 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4587 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche with N+1 instalment handling - chargeback after maturity date is placed within last installment after backdated 2nd disb - UC4.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_CHARGEBACK | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
# --- add chargeback transaction --- #
    When Admin sets the business date to "15 July 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.72           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 34.25           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.78           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.78         | 0.66     | 0.0  | 0.0       | 18.44  | 0.0   | 0.0        | 0.0  | 18.44       |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.34        | 4.88     | 0.0  | 0.0       | 121.22 | 17.13 | 0.0        | 0.0  | 104.09     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 15 July 2024     | Chargeback       | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 100.0        | false    | false    |
# --- 2nd disbursement  --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 118.04          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 85.23           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 52.42           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 19.61           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 35.95         | 1.6      | 0.0  | 0.0       | 37.55  | 0.0   | 0.0        | 0.0  | 37.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 216.34        | 9.64     | 0.0  | 0.0       | 225.98 | 17.13 | 0.0        | 0.0  | 208.85     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 15 July 2024     | Chargeback       | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 200.0        | false    | false    |
    When Loan Pay-off is made on "15 July 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4588 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche with N+1 instalment handling - chargeback after maturity date and backdated 2nd disb afterwards - UC4.6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_FULL_TERM_TRANCHE_CHARGEBACK | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 17.13 | 0.0        | 0.0  | 85.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
# --- add chargeback transaction --- #
    When Admin sets the business date to "15 August 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.72           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 34.25           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.78           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.78         | 0.66     | 0.0  | 0.0       | 18.44  | 0.0   | 0.0        | 0.0  | 18.44       |
      | 7  | 45   | 15 August 2024   |                  | 0.0             | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.34        | 4.88     | 0.0  | 0.0       | 121.22 | 17.13 | 0.0        | 0.0  | 104.09     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 15 August 2024   | Chargeback       | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 100.0        | false    | false    |
# --- backdated 2nd disbursement --- #
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 118.04          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 85.23           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 52.42           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 19.61           | 32.81         | 1.45     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 19.61         | 1.45     | 0.0  | 0.0       | 21.06  | 0.0   | 0.0        | 0.0  | 21.06       |
      | 8  | 14   | 15 August 2024   |                  | 0.0             | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 216.34        | 10.28    | 0.0  | 0.0       | 226.62 | 17.13 | 0.0        | 0.0  | 209.49     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 15 August 2024   | Chargeback       | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 200.0        | false    | false    |
    When Loan Pay-off is made on "15 August 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4589 @AdvancedPaymentAllocation
  Scenario: Verify full term tranche with N+1 instalment handling - multiple charges before and after 2nd disb with due date after maturity date - UC4.7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
# -- snooze charge on Jan 1, 2024 with due date Jul 15, 1024 --- #
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 August 2024" due date and 10 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 7  | 45   | 15 August 2024   |           | 0.0             |  0.0          | 0.0      | 0.0  | 10.0      | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 10.0      | 112.78 | 0.0  | 0.0        | 0.0  | 112.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of      | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 15 August 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
# ---- repayment on Feb 1, 2024 --- #
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 7  | 45   | 15 August 2024   |                  | 0.0             |  0.0          | 0.0      | 0.0  | 10.0      | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 10.0      | 112.78 | 17.13 | 0.0        | 0.0  | 95.65       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
# --- multiple charges before 2nd disb with due date after maturity date --- #
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 July 2024" due date and 10 EUR transaction amount
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 August 2024" due date and 15 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 September 2024" due date and 33.3 EUR transaction amount
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 September 2024" due date and 2.2 EUR transaction amount
    Then Loan has 6 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024     |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024       |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024      |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024      |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 7  | 81   | 20 September 2024 |                  | 0.0             |  0.0          | 0.0      | 17.2 | 53.3      | 70.5   | 0.0   | 0.0        | 0.0  | 70.5        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 17.2 | 53.3      | 173.28 | 17.13 | 0.0        | 0.0  | 156.15      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of         | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 15 August 2024    | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | NSF fee    | true      | Specified due date | 15 July 2024      | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Snooze fee | false     | Specified due date | 15 August 2024    | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
      | NSF fee    | true      | Specified due date | 15 September 2024 | Flat             | 33.3 | 0.0  | 0.0    | 33.3        |
      | Snooze fee | false     | Specified due date | 20 September 2024 | Flat             | 2.2  | 0.0  | 0.0    | 2.2         |
# --- repayment schedule before 2nd disbursement --- #
    When Admin sets the business date to "01 March 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024     |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024       |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024      |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024      |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      | 7  | 81   | 20 September 2024 |                  | 0.0             |  0.0          | 0.0      | 17.2 | 53.3      | 70.5   | 0.0   | 0.0        | 0.0  | 70.5        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 17.2 | 53.3      | 173.28 | 17.13 | 0.0        | 0.0  | 156.15       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
# --- 2nd disbursement on installment date ---#
    When Admin successfully disburse the loan on "01 March 2024" with "100" EUR transaction amount
    Then Loan has 8 active number of terms
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      |    |      | 01 March 2024     |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024     |                  | 134.25          | 32.94         | 1.32     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024       |                  | 101.05          | 33.2          | 1.06     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024      |                  | 67.59           | 33.46         | 0.8      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024      |                  | 33.86           | 33.73         | 0.53     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024    |                  | 17.0            | 16.86         | 0.27     | 0.0  | 10.0      | 27.13  | 0.0   | 0.0        | 0.0  | 27.13       |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 17.0          | 0.13     | 15.0 | 10.0      | 42.13  | 0.0   | 0.0        | 0.0  | 42.13       |
      | 9  | 19   | 20 September 2024 |                  | 0.0             |  0.0          | 0.0      | 2.2  | 33.3      | 35.5   | 0.0   | 0.0        | 0.0  | 35.5         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 17.2 | 53.3      | 276.06 | 17.13 | 0.0        | 0.0  | 258.93      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
# --- multiple charges after 2nd disb with due date after maturity date --- #
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 July 2024" due date and 7 EUR transaction amount
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 August 2024" due date and 9 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 September 2024" due date and 21.1 EUR transaction amount
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 September 2024" due date and 60.4 EUR transaction amount
    Then Loan has 8 active number of terms
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0   | 0.0        | 0.0  | 17.13       |
      |    |      | 01 March 2024     |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024     |                  | 134.25          | 32.94         | 1.32     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024       |                  | 101.05          | 33.2          | 1.06     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024      |                  | 67.59           | 33.46         | 0.8      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024      |                  | 33.86           | 33.73         | 0.53     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024    |                  | 17.0            | 16.86         | 0.27     | 0.0  | 17.0      | 34.13  | 0.0   | 0.0        | 0.0  | 34.13       |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 17.0          | 0.13     | 24.0 | 10.0      | 51.13  | 0.0   | 0.0        | 0.0  | 51.13       |
      | 9  | 19   | 20 September 2024 |                  | 0.0             |  0.0          | 0.0      | 62.6 | 54.4      | 117.0  | 0.0   | 0.0        | 0.0  | 117.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 86.6  | 81.4      | 373.56 | 17.13 | 0.0        | 0.0  | 356.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of         | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 15 August 2024    | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | NSF fee    | true      | Specified due date | 15 July 2024      | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Snooze fee | false     | Specified due date | 15 August 2024    | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
      | NSF fee    | true      | Specified due date | 15 September 2024 | Flat             | 33.3 | 0.0  | 0.0    | 33.3        |
      | Snooze fee | false     | Specified due date | 20 September 2024 | Flat             | 2.2  | 0.0  | 0.0    | 2.2         |
      | NSF fee    | true      | Specified due date | 15 July 2024      | Flat             | 7.0  | 0.0  | 0.0    | 7.0         |
      | Snooze fee | false     | Specified due date | 15 August 2024    | Flat             | 9.0  | 0.0  | 0.0    | 9.0         |
      | NSF fee    | true      | Specified due date | 15 September 2024 | Flat             | 21.1 | 0.0  | 0.0    | 21.1        |
      | Snooze fee | false     | Specified due date | 20 September 2024 | Flat             | 60.4 | 0.0  | 0.0    | 60.4         |
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    Then Loan has 8 active number of terms
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 01 February 2024 | 66.53           | 17.13         | 0.0      | 0.0  | 0.0       | 17.13  | 17.13 | 17.13      | 0.0  | 0.0         |
      |    |      | 01 March 2024     |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024     |                  | 134.12          | 32.41         | 1.85     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024       |                  | 100.92          | 33.2          | 1.06     | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024      |                  | 67.46           | 33.46         | 0.8      | 0.0  | 0.0       | 34.26  | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024      |                  | 33.86           | 33.6          | 0.53     | 0.0  | 0.0       | 34.13  | 0.0   | 0.0        | 0.0  | 34.13       |
      | 7  | 31   | 01 August 2024    |                  | 17.0            | 16.86         | 0.27     | 0.0  | 17.0      | 34.13  | 0.0   | 0.0        | 0.0  | 34.13       |
      | 8  | 31   | 01 September 2024 |                  | 0.0             | 17.0          | 0.13     | 24.0 | 10.0      | 51.13  | 0.0   | 0.0        | 0.0  | 51.13       |
      | 9  | 19   | 20 September 2024 |                  | 0.0             |  0.0          | 0.0      | 62.6 | 54.4      | 117.0  | 0.0   | 0.0        | 0.0  | 117.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.43     | 86.6  | 81.4      | 373.43 | 34.26 | 17.13      | 0.0  | 339.17      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 17.13     | 0.0      | 0.0  | 0.0       | 66.53        | false    | false    |
      | 01 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 166.53       | false    | false    |
    When Admin sets the business date to "01 April 2024"
    When Loan Pay-off is made on "01 April 2024"
    Then Loan has 8 active number of terms
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 01 February 2024 | 66.53           | 17.13         | 0.0      | 0.0  | 0.0       | 17.13  | 17.13 | 17.13      | 0.0  | 0.0         |
      |    |      | 01 March 2024     |                  | 100.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024     | 01 April 2024    | 134.12          | 32.41         | 1.85     | 0.0  | 0.0       | 34.26  | 34.26 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024       | 01 April 2024    | 99.86           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26  | 34.26 | 34.26      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024      | 01 April 2024    | 65.6            | 34.26         | 0.0      | 0.0  | 0.0       | 34.26  | 34.26 | 34.26      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024      | 01 April 2024    | 31.47           | 34.13         | 0.0      | 0.0  | 0.0       | 34.13  | 34.13 | 34.13      | 0.0  | 0.0         |
      | 7  | 31   | 01 August 2024    | 01 April 2024    | 14.34           | 17.13         | 0.0      | 0.0  | 17.0      | 34.13  | 34.13 | 34.13      | 0.0  | 0.0         |
      | 8  | 31   | 01 September 2024 | 01 April 2024    | 0.0             | 14.34         | 0.0      | 24.0 | 10.0      | 48.34  | 48.34 | 48.34      | 0.0  | 0.0         |
      | 9  | 19   | 20 September 2024 | 01 April 2024    | 0.0             |  0.0          | 0.0      | 62.6 | 54.4      | 117.0  | 117.0 | 117.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 2.64     | 86.6  | 81.4      | 370.64 | 370.64 | 319.25     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 17.13     | 0.0      | 0.0  | 0.0       | 66.53        | false    | false    |
      | 01 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 166.53       | false    | false    |
      | 01 April 2024    | Repayment        | 336.38 | 166.53    | 1.85     | 86.6 | 81.4      | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 170.64 | 0.0       | 2.64     | 86.6 | 81.4      | 0.0          | false    | false    |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4590
  Scenario: Verify full term tranche interest bearing progressive loan with charges/penalties - UC1 (NSF penalty before 2nd disbursement)
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 March 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 5.0       | 22.13 | 0.0  | 0.0        | 0.0  | 22.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 5.0       | 107.78 | 0.0  | 0.0        | 0.0  | 107.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- 2nd disbursement on Feb 1 ---
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 5.0       | 39.26 | 0.0  | 0.0        | 0.0  | 39.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 5.0       | 210.56 | 0.0  | 0.0        | 0.0  | 210.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4591
  Scenario: Verify full term tranche interest bearing progressive loan with charges/penalties - UC2 (NSF penalty added between disbursements)
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    When Admin sets the business date to "10 January 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 5.0       | 22.13 | 0.0  | 0.0        | 0.0  | 22.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 5.0       | 107.78 | 0.0  | 0.0        | 0.0  | 107.78      |
    When Admin sets the business date to "01 March 2024"
    When Admin successfully disburse the loan on "01 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024  |           | 83.66           | 16.34         | 0.79     | 0.0  | 5.0       | 22.13 | 0.0  | 0.0        | 0.0  | 22.13       |
      | 2  | 29   | 01 March 2024     |           | 67.32           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 March 2024     |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 3  | 31   | 01 April 2024     |           | 134.38          | 32.94         | 1.32     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024       |           | 101.18          | 33.2          | 1.06     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024      |           | 67.72           | 33.46         | 0.8      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024      |           | 33.86           | 33.86         | 0.54     | 0.0  | 0.0       | 34.4  | 0.0  | 0.0        | 0.0  | 34.4        |
      | 7  | 31   | 01 August 2024    |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 8  | 31   | 01 September 2024 |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.7      | 0.0  | 5.0       | 210.7 | 0.0  | 0.0        | 0.0  | 210.7       |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4592
  Scenario: Verify full term tranche interest bearing progressive loan with charges/penalties - UC3 (Snooze fee + NSF penalty on different periods with repayments)
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 March 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 5.0  | 0.0       | 22.13 | 0.0  | 0.0        | 0.0  | 22.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 5.0  | 0.0       | 107.78 | 0.0  | 0.0        | 0.0  | 107.78      |
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 5.0  | 0.0       | 39.26 | 0.0  | 0.0        | 0.0  | 39.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 5.0  | 0.0       | 210.56 | 0.0  | 0.0        | 0.0  | 210.56      |
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 May 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 5.0  | 0.0       | 39.26 | 0.0  | 0.0        | 0.0  | 39.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 10.0      | 44.26 | 0.0  | 0.0        | 0.0  | 44.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 5.0  | 10.0      | 220.56 | 0.0  | 0.0        | 0.0  | 220.56      |
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 90.65 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 April 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 17.13 | 0.0         |
      |    |      | 01 February 2024 |               | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 29   | 01 March 2024    | 01 April 2024 | 150.98          | 32.68         | 1.58     | 0.0  | 0.0       | 34.26 | 34.26 | 0.0        | 34.26 | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024 | 118.3           | 32.68         | 1.58     | 5.0  | 0.0       | 39.26 | 39.26 | 0.0        | 0.0   | 0.0         |
      | 4  | 30   | 01 May 2024      |               | 84.97           | 33.33         | 0.93     | 0.0  | 10.0      | 44.26 | 0.0   | 0.0        | 0.0   | 44.26       |
      | 5  | 31   | 01 June 2024     |               | 51.38           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0   | 34.26       |
      | 6  | 30   | 01 July 2024     |               | 17.53           | 33.85         | 0.41     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0   | 34.26       |
      | 7  | 31   | 01 August 2024   |               | 0.0             | 17.53         | 0.14     | 0.0  | 0.0       | 17.67 | 0.0   | 0.0        | 0.0   | 17.67       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 200.0         | 6.1      | 5.0  | 10.0      | 221.1 | 90.65 | 0.0        | 51.39 | 130.45      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 April 2024    | Repayment        | 90.65  | 81.7      | 3.95     | 5.0  | 0.0       | 118.3        | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4593
  Scenario: Verify full term tranche interest bearing progressive loan with charges/penalties - UC4 (Both charges on same due date)
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    When Admin sets the business date to "15 January 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 April 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 10.0      | 27.13 | 0.0  | 0.0        | 0.0  | 27.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 10.0      | 112.78 | 0.0  | 0.0        | 0.0  | 112.78      |
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 0.0  | 10.0      | 44.26 | 0.0  | 0.0        | 0.0  | 44.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 10.0      | 215.56 | 0.0  | 0.0        | 0.0  | 215.56      |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 April 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |           | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |           | 84.45           | 33.33         | 0.93     | 5.0  | 10.0      | 49.26 | 0.0  | 0.0        | 0.0  | 49.26       |
      | 5  | 31   | 01 June 2024     |           | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |           | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0  | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 5.0  | 10.0      | 220.56 | 0.0  | 0.0        | 0.0  | 220.56      |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of      | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 15 April 2024  | Flat             | 5.0  | 0.0  | 0.0    | 5.0         |
      | NSF fee    | true      | Specified due date | 15 April 2024  | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4594
  Scenario: Verify full term tranche interest bearing progressive loan with charges/penalties - UC5 (Snooze fee waive after 2nd disbursement)
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    When Admin sets the business date to "15 January 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 5.0  | 0.0       | 22.13 | 0.0  | 0.0        | 0.0  | 22.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 5.0  | 0.0       | 107.78 | 0.0  | 0.0        | 0.0  | 107.78      |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 5.0  | 0.0       | 39.26 | 0.0   | 0.0        | 0.0  | 39.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 5.0  | 0.0       | 210.56 | 17.13  | 0.0        | 0.0  | 193.43      |
    When Admin sets the business date to "15 February 2024"
    And Admin waives charge
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Waived | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0    | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |        |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 5.0  | 0.0       | 39.26 | 0.0   | 0.0        | 0.0  | 5.0    | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0    | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0    | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0    | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 0.0    | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 0.0    | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Waived | Outstanding |
      | 200.0         | 5.56     | 5.0  | 0.0       | 210.56 | 17.13  | 0.0        | 0.0  | 5.0    | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 15 February 2024 | Waive loan charges | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of     | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 March 2024 | Flat             | 5.0 | 0.0  | 5.0    | 0.0         |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4595
  Scenario: Verify full term tranche interest bearing progressive loan with charges/penalties - UC6 (NSF penalty adjustment after 2nd disbursement)
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    When Admin sets the business date to "15 January 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 March 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 5.0       | 22.13 | 0.0  | 0.0        | 0.0  | 22.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 5.0       | 107.78 | 0.0  | 0.0        | 0.0  | 107.78      |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 5.0       | 39.26 | 0.0   | 0.0        | 0.0  | 39.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 5.0       | 210.56 | 17.13 | 0.0        | 0.0  | 193.43      |
    When Admin sets the business date to "15 February 2024"
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "01 March 2024" with 5 EUR transaction amount and externalId ""
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 5.0       | 39.26 | 5.0   | 5.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 5.0       | 210.56 | 22.13 | 5.0        | 0.0  | 188.43      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment         | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 15 February 2024 | Charge Adjustment | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 183.66       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of     | Calculation type | Due | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 March 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    When Loan Pay-off is made on "15 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met


  @TestRailId:C4597
  Scenario: Verify full term tranche interest bearing progressive loan - interest pause handling UC1: interest pause happy path
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- Repayment on Feb 1 + 2nd disbursement on same day ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    And Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66         | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66        | false    | false    |
#   --- Interest pause from 02 April 2024 to 01 May 2024 ---
    When Admin sets the business date to "02 February 2024"
    And Create an interest pause period with start date "02 April 2024" and end date "01 May 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 83.52           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 49.92           | 33.6          | 0.66     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 16.05           | 33.87         | 0.39     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.05         | 0.13     | 0.0  | 0.0       | 16.18 | 0.0   | 0.0        | 0.0  | 16.18       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 4.61     | 0.0  | 0.0       | 204.61 | 17.13 | 0.0        | 0.0  | 187.48      |
    When Loan Pay-off is made on "02 February 2024"
    And Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4598
  Scenario: Verify full term tranche interest bearing progressive loan - interest pause handling UC2: multiple interest pauses
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- Repayment on Feb 1 + 2nd disbursement on same day ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    And Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
#   --- First interest pause from 02 April 2024 to 01 May 2024 ---
    When Admin sets the business date to "02 February 2024"
    And Create an interest pause period with start date "02 April 2024" and end date "01 May 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 83.52           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 49.92           | 33.6          | 0.66     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 16.05           | 33.87         | 0.39     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.05         | 0.13     | 0.0  | 0.0       | 16.18 | 0.0   | 0.0        | 0.0  | 16.18       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 4.61     | 0.0  | 0.0       | 204.61 | 17.13 | 0.0        | 0.0  | 187.48      |
#   --- Second interest pause from 15 May 2024 to 25 May 2024 ---
    And Create an interest pause period with start date "15 May 2024" and end date "25 May 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 83.52           | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 49.69           | 33.83         | 0.43     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 15.82           | 33.87         | 0.39     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 15.82         | 0.13     | 0.0  | 0.0       | 15.95 | 0.0   | 0.0        | 0.0  | 15.95       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 4.38     | 0.0  | 0.0       | 204.38 | 17.13 | 0.0        | 0.0  | 187.25      |
    When Loan Pay-off is made on "02 February 2024"
    And Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4599
  Scenario: Verify full term tranche interest bearing progressive loan - interest pause handling UC3: backdated interest pause after second disbursement
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- Repayment on Feb 1 + 2nd disbursement on same day ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    And Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
#   --- Repayment on March 1 ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 34.26 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 34.26 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 51.39 | 0.0        | 0.0  | 154.17      |
#   --- Backdated interest pause from 05 February 2024 to 15 February 2024 ---
    When Admin sets the business date to "15 March 2024"
    And Create an interest pause period with start date "05 February 2024" and end date "15 February 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 150.3           | 33.36         | 0.9      | 0.0  | 0.0       | 34.26 | 34.26 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 117.23          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 83.9            | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.3            | 33.6          | 0.66     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 16.44           | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.44         | 0.13     | 0.0  | 0.0       | 16.57 | 0.0   | 0.0        | 0.0  | 16.57       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.0      | 0.0  | 0.0       | 205.0 | 51.39 | 0.0        | 0.0  | 153.61      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
      | 01 March 2024    | Repayment        | 34.26  | 33.36     | 0.9      | 0.0  | 0.0       | 150.3        | false    | true     |
    When Loan Pay-off is made on "15 March 2024"
    And Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4600
  Scenario: Verify full term tranche interest bearing progressive loan - interest pause handling UC4: interest pause with partial repayments
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#   --- Repayment on Feb 1 + 2nd disbursement on same day ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.13 EUR transaction amount
    And Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 17.13 | 0.0        | 0.0  | 188.43      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 183.66       | false    | false    |
#   --- Interest pause ---
    And Create an interest pause period with start date "15 February 2024" and end date "15 April 2024"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.05          | 33.61         | 0.65     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 115.79          | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 82.02           | 33.77         | 0.49     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 48.41           | 33.61         | 0.65     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 14.53           | 33.88         | 0.38     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 14.53         | 0.11     | 0.0  | 0.0       | 14.64 | 0.0   | 0.0        | 0.0  | 14.64       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.07     | 0.0  | 0.0       | 203.07 | 17.13 | 0.0        | 0.0  | 185.94      |
#   --- Partial repayment during pause period on 01 March 2024 ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 20.0 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.05          | 33.61         | 0.65     | 0.0  | 0.0       | 34.26 | 20.0  | 0.0        | 0.0  | 14.26       |
      | 3  | 31   | 01 April 2024    |                  | 115.79          | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 82.02           | 33.77         | 0.49     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 48.41           | 33.61         | 0.65     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 14.53           | 33.88         | 0.38     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 14.53         | 0.11     | 0.0  | 0.0       | 14.64 | 0.0   | 0.0        | 0.0  | 14.64       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.07     | 0.0  | 0.0       | 203.07 | 37.13 | 0.0        | 0.0  | 165.94      |
#   --- Another partial repayment during pause period on 05 April 2024 ---
    When Admin sets the business date to "05 April 2024"
    And Customer makes "AUTOPAY" repayment on "05 April 2024" with 25.0 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0   | 0.0         |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 29   | 01 March 2024    | 05 April 2024    | 150.05          | 33.61         | 0.65     | 0.0  | 0.0       | 34.26 | 34.26 | 0.0        | 14.26 | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 115.79          | 34.26         | 0.0      | 0.0  | 0.0       | 34.26 | 10.74 | 0.0        | 10.74 | 23.52       |
      | 4  | 30   | 01 May 2024      |                  | 82.02           | 33.77         | 0.49     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0   | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 48.41           | 33.61         | 0.65     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0   | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 14.53           | 33.88         | 0.38     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0   | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 14.53         | 0.11     | 0.0  | 0.0       | 14.64 | 0.0   | 0.0        | 0.0   | 14.64       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.07     | 0.0  | 0.0       | 203.07 | 62.13 | 0.0        | 25.0 | 140.94      |
    When Loan Pay-off is made on "05 April 2024"
    And Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4644
  Scenario: Verify full term tranche interest bearing progressive loan with no new terms - Disbursement at first instalment mid-period - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |           | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |           | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |           | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |           | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13  | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
#   --- 2nd disbursement mid-period (Jan 15) ---
    When Admin sets the business date to "15 January 2024"
    When Admin disburses the loan on "15 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 167.02          | 32.98         | 1.22     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 2  | 29   | 01 March 2024    |           | 134.14          | 32.88         | 1.32     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 3  | 31   | 01 April 2024    |           | 101.0           | 33.14         | 1.06     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 4  | 30   | 01 May 2024      |           | 67.6            | 33.4          | 0.8      | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 5  | 31   | 01 June 2024     |           | 33.93           | 33.67         | 0.53     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 33.93         | 0.27     | 0.0  | 0.0       | 34.2   | 0.0  | 0.0        | 0.0  | 34.2        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 5.2      | 0.0  | 0.0       | 205.2  | 0.0  | 0.0        | 0.0  | 205.2       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024   | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    When Loan Pay-off is made on "15 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4676
  Scenario: Verify that changedTerms is true in LoanDisbursalTransactionBusinessEvent when additional disbursement adds new terms - UC2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 01 January 2024   | 200            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0   | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    When Admin sets the business date to "01 February 2024"
    When Admin disburses the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 150.85          | 32.81         | 1.45     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 3  | 31   | 01 April 2024    |                  | 117.78          | 33.07         | 1.19     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 4  | 30   | 01 May 2024      |                  | 84.45           | 33.33         | 0.93     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 5  | 31   | 01 June 2024     |                  | 50.86           | 33.59         | 0.67     | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 6  | 30   | 01 July 2024     |                  | 17.0            | 33.86         | 0.4      | 0.0  | 0.0       | 34.26 | 0.0   | 0.0        | 0.0  | 34.26       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 5.56     | 0.0  | 0.0       | 205.56 | 0.0   | 0.0        | 0.0  | 205.56      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "true"
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
