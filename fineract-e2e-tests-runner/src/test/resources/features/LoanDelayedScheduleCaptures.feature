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
      | 4  | 30   | 01 May 2024       |                  | 153.05          | 30.61         | 0.84     | 0.0  | 0.0       | 31.45  | 0.0   | 0.0        | 0.0  | 31.45      |
      | 5  | 31   | 01 June 2024      |                  | 122.44          | 30.61         | 0.84     | 0.0  | 0.0       | 31.45  | 0.0   | 0.0        | 0.0  | 31.45      |
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
