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
