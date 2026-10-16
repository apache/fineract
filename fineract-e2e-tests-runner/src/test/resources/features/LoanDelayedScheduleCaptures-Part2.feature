@DelayedScheduleCapturesFeature
Feature: Full Term Tranche - Schedule handling and Calculations - Part2

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

  @TestRailId:C115274
  Scenario: Verify full term tranche late disbursement on month-end anchored loan (31st) with zero interest rate merges schedule - no duplicate periods - UC1
    When Admin sets the business date to "31 August 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 31 August 2026    | 2676.62        | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 48                | MONTHS                | 1              | MONTHS                 | 48                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 August 2026" with "2676.62" amount and expected disbursement date on "31 August 2026"
    When Admin successfully disburse the loan on "31 August 2026" with "1997.62" EUR transaction amount
    Then Loan has 48 active number of terms
    Then Loan Repayment schedule has 48 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 August 2026    |           | 1997.62         |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 September 2026 |           | 1956.0          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 2  | 31   | 31 October 2026   |           | 1914.38         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 3  | 30   | 30 November 2026  |           | 1872.76         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 4  | 31   | 31 December 2026  |           | 1831.14         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 5  | 31   | 31 January 2027   |           | 1789.52         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 6  | 28   | 28 February 2027  |           | 1747.9          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 7  | 31   | 31 March 2027     |           | 1706.28         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 8  | 30   | 30 April 2027     |           | 1664.66         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 9  | 31   | 31 May 2027       |           | 1623.04         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 10 | 30   | 30 June 2027      |           | 1581.42         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 11 | 31   | 31 July 2027      |           | 1539.8          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 12 | 31   | 31 August 2027    |           | 1498.18         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 13 | 30   | 30 September 2027 |           | 1456.56         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 14 | 31   | 31 October 2027   |           | 1414.94         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 15 | 30   | 30 November 2027  |           | 1373.32         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 16 | 31   | 31 December 2027  |           | 1331.7          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 17 | 31   | 31 January 2028   |           | 1290.08         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 18 | 29   | 29 February 2028  |           | 1248.46         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 19 | 31   | 31 March 2028     |           | 1206.84         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 20 | 30   | 30 April 2028     |           | 1165.22         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 21 | 31   | 31 May 2028       |           | 1123.6          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 22 | 30   | 30 June 2028      |           | 1081.98         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 23 | 31   | 31 July 2028      |           | 1040.36         | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 24 | 31   | 31 August 2028    |           | 998.74          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 25 | 30   | 30 September 2028 |           | 957.12          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 26 | 31   | 31 October 2028   |           | 915.5           | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 27 | 30   | 30 November 2028  |           | 873.88          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 28 | 31   | 31 December 2028  |           | 832.26          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 29 | 31   | 31 January 2029   |           | 790.64          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 30 | 28   | 28 February 2029  |           | 749.02          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 31 | 31   | 31 March 2029     |           | 707.4           | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 32 | 30   | 30 April 2029     |           | 665.78          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 33 | 31   | 31 May 2029       |           | 624.16          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 34 | 30   | 30 June 2029      |           | 582.54          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 35 | 31   | 31 July 2029      |           | 540.92          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 36 | 31   | 31 August 2029    |           | 499.3           | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 37 | 30   | 30 September 2029 |           | 457.68          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 38 | 31   | 31 October 2029   |           | 416.06          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 39 | 30   | 30 November 2029  |           | 374.44          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 40 | 31   | 31 December 2029  |           | 332.82          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 41 | 31   | 31 January 2030   |           | 291.2           | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 42 | 28   | 28 February 2030  |           | 249.58          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 43 | 31   | 31 March 2030     |           | 207.96          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 44 | 30   | 30 April 2030     |           | 166.34          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 45 | 31   | 31 May 2030       |           | 124.72          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 46 | 30   | 30 June 2030      |           | 83.1            | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 47 | 31   | 31 July 2030      |           | 41.48           | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      | 48 | 31   | 31 August 2030    |           | 0.0             | 41.48         | 0.0      | 0.0  | 0.0       | 41.48 | 0.0  | 0.0        | 0.0  | 41.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1997.62       | 0.0      | 0.0  | 0.0       | 1997.62 | 0.0  | 0.0        | 0.0  | 1997.62     |
    When Admin sets the business date to "10 October 2026"
    When Admin successfully disburse the loan on "10 October 2026" with "679" EUR transaction amount
    Then Loan has 49 active number of terms
    Then Loan Repayment schedule has 49 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 August 2026    |           | 1997.62         |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 September 2026 |           | 1956.0          | 41.62         | 0.0      | 0.0  | 0.0       | 41.62 | 0.0  | 0.0        | 0.0  | 41.62       |
      |    |      | 10 October 2026   |           | 679.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 31   | 31 October 2026   |           | 2579.23         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 3  | 30   | 30 November 2026  |           | 2523.46         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 4  | 31   | 31 December 2026  |           | 2467.69         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 5  | 31   | 31 January 2027   |           | 2411.92         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 6  | 28   | 28 February 2027  |           | 2356.15         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 7  | 31   | 31 March 2027     |           | 2300.38         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 8  | 30   | 30 April 2027     |           | 2244.61         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 9  | 31   | 31 May 2027       |           | 2188.84         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 10 | 30   | 30 June 2027      |           | 2133.07         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 11 | 31   | 31 July 2027      |           | 2077.3          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 12 | 31   | 31 August 2027    |           | 2021.53         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 13 | 30   | 30 September 2027 |           | 1965.76         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 14 | 31   | 31 October 2027   |           | 1909.99         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 15 | 30   | 30 November 2027  |           | 1854.22         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 16 | 31   | 31 December 2027  |           | 1798.45         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 17 | 31   | 31 January 2028   |           | 1742.68         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 18 | 29   | 29 February 2028  |           | 1686.91         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 19 | 31   | 31 March 2028     |           | 1631.14         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 20 | 30   | 30 April 2028     |           | 1575.37         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 21 | 31   | 31 May 2028       |           | 1519.6          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 22 | 30   | 30 June 2028      |           | 1463.83         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 23 | 31   | 31 July 2028      |           | 1408.06         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 24 | 31   | 31 August 2028    |           | 1352.29         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 25 | 30   | 30 September 2028 |           | 1296.52         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 26 | 31   | 31 October 2028   |           | 1240.75         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 27 | 30   | 30 November 2028  |           | 1184.98         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 28 | 31   | 31 December 2028  |           | 1129.21         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 29 | 31   | 31 January 2029   |           | 1073.44         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 30 | 28   | 28 February 2029  |           | 1017.67         | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 31 | 31   | 31 March 2029     |           | 961.9           | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 32 | 30   | 30 April 2029     |           | 906.13          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 33 | 31   | 31 May 2029       |           | 850.36          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 34 | 30   | 30 June 2029      |           | 794.59          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 35 | 31   | 31 July 2029      |           | 738.82          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 36 | 31   | 31 August 2029    |           | 683.05          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 37 | 30   | 30 September 2029 |           | 627.28          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 38 | 31   | 31 October 2029   |           | 571.51          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 39 | 30   | 30 November 2029  |           | 515.74          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 40 | 31   | 31 December 2029  |           | 459.97          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 41 | 31   | 31 January 2030   |           | 404.2           | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 42 | 28   | 28 February 2030  |           | 348.43          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 43 | 31   | 31 March 2030     |           | 292.66          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 44 | 30   | 30 April 2030     |           | 236.89          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 45 | 31   | 31 May 2030       |           | 181.12          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 46 | 30   | 30 June 2030      |           | 125.35          | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 47 | 31   | 31 July 2030      |           | 69.58           | 55.77         | 0.0      | 0.0  | 0.0       | 55.77 | 0.0  | 0.0        | 0.0  | 55.77       |
      | 48 | 31   | 31 August 2030    |           | 13.95           | 55.63         | 0.0      | 0.0  | 0.0       | 55.63 | 0.0  | 0.0        | 0.0  | 55.63       |
      | 49 | 30   | 30 September 2030 |           | 0.0             | 13.95         | 0.0      | 0.0  | 0.0       | 13.95 | 0.0  | 0.0        | 0.0  | 13.95       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2676.62       | 0.0      | 0.0  | 0.0       | 2676.62 | 0.0  | 0.0        | 0.0  | 2676.62     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 August 2026   | Disbursement     | 1997.62 | 0.0       | 0.0      | 0.0  | 0.0       | 1997.62      | false    | false    |
      | 10 October 2026  | Disbursement     | 679.0   | 0.0       | 0.0      | 0.0  | 0.0       | 2676.62      | false    | false    |
    When Loan Pay-off is made on "10 October 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115275
  Scenario: Verify full term tranche late disbursement on month-end shortened fromDate (31st, 6 terms) with zero interest rate merges schedule - UC2
    When Admin sets the business date to "31 August 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 31 August 2026    | 600            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 August 2026" with "600" amount and expected disbursement date on "31 August 2026"
    When Admin successfully disburse the loan on "31 August 2026" with "300" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 August 2026    |           | 300.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 30   | 30 September 2026 |           | 250.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 31   | 31 October 2026   |           | 200.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 30   | 30 November 2026  |           | 150.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 4  | 31   | 31 December 2026  |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 5  | 31   | 31 January 2027   |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 6  | 28   | 28 February 2027  |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    When Admin sets the business date to "10 October 2026"
    When Admin successfully disburse the loan on "10 October 2026" with "300" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 August 2026    |           | 300.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 September 2026 |           | 250.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0  | 0.0        | 0.0  | 50.0        |
      |    |      | 10 October 2026   |           | 300.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 31   | 31 October 2026   |           | 450.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 3  | 30   | 30 November 2026  |           | 350.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 4  | 31   | 31 December 2026  |           | 250.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 5  | 31   | 31 January 2027   |           | 150.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 6  | 28   | 28 February 2027  |           | 50.0            | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 7  | 31   | 31 March 2027     |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0  | 0.0        | 0.0  | 50.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 600.0         | 0.0      | 0.0  | 0.0       | 600.0 | 0.0  | 0.0        | 0.0  | 600.0       |
    When Loan Pay-off is made on "10 October 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115276
  Scenario: Verify full term tranche late disbursement after Feb 28 shortened fromDate (29th anchor) with zero interest rate merges schedule - UC3
    When Admin sets the business date to "29 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 29 January 2026   | 600            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "29 January 2026" with "600" amount and expected disbursement date on "29 January 2026"
    When Admin successfully disburse the loan on "29 January 2026" with "300" EUR transaction amount
    When Admin sets the business date to "10 March 2026"
    When Admin successfully disburse the loan on "10 March 2026" with "300" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 29 January 2026  |           | 300.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 28 February 2026 |           | 250.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0  | 0.0        | 0.0  | 50.0        |
      |    |      | 10 March 2026    |           | 300.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 29 March 2026    |           | 450.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 3  | 31   | 29 April 2026    |           | 350.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 4  | 30   | 29 May 2026      |           | 250.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 5  | 31   | 29 June 2026     |           | 150.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 6  | 30   | 29 July 2026     |           | 50.0            | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 7  | 31   | 29 August 2026   |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0  | 0.0        | 0.0  | 50.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 600.0         | 0.0      | 0.0  | 0.0       | 600.0 | 0.0  | 0.0        | 0.0  | 600.0       |
    When Loan Pay-off is made on "10 March 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115277
  Scenario: Verify full term tranche late disbursement after Feb 28 shortened fromDate (30th anchor) with zero interest rate merges schedule - UC4
    When Admin sets the business date to "30 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 30 January 2026   | 600            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "30 January 2026" with "600" amount and expected disbursement date on "30 January 2026"
    When Admin successfully disburse the loan on "30 January 2026" with "300" EUR transaction amount
    When Admin sets the business date to "10 March 2026"
    When Admin successfully disburse the loan on "10 March 2026" with "300" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 30 January 2026  |           | 300.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 29   | 28 February 2026 |           | 250.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0  | 0.0        | 0.0  | 50.0        |
      |    |      | 10 March 2026    |           | 300.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 30   | 30 March 2026    |           | 450.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 3  | 31   | 30 April 2026    |           | 350.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 4  | 30   | 30 May 2026      |           | 250.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 5  | 31   | 30 June 2026     |           | 150.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 6  | 30   | 30 July 2026     |           | 50.0            | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 7  | 31   | 30 August 2026   |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0  | 0.0        | 0.0  | 50.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 600.0         | 0.0      | 0.0  | 0.0       | 600.0 | 0.0  | 0.0        | 0.0  | 600.0       |
    When Loan Pay-off is made on "10 March 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115278
  Scenario: Verify full term tranche late disbursement on month-end anchored loan (31st) with non-zero interest rate merges schedule - no duplicate periods - UC5
    When Admin sets the business date to "31 August 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 31 August 2026    | 2676.62        | 35.9                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 48                | MONTHS                | 1              | MONTHS                 | 48                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 August 2026" with "2676.62" amount and expected disbursement date on "31 August 2026"
    When Admin successfully disburse the loan on "31 August 2026" with "1997.62" EUR transaction amount
    Then Loan has 48 active number of terms
    Then Loan Repayment schedule has 48 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 August 2026    |           | 1997.62         |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 September 2026 |           | 1978.44         | 19.18         | 59.76    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 2  | 31   | 31 October 2026   |           | 1958.69         | 19.75         | 59.19    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 3  | 30   | 30 November 2026  |           | 1938.35         | 20.34         | 58.6     | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 4  | 31   | 31 December 2026  |           | 1917.4          | 20.95         | 57.99    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 5  | 31   | 31 January 2027   |           | 1895.82         | 21.58         | 57.36    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 6  | 28   | 28 February 2027  |           | 1873.6          | 22.22         | 56.72    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 7  | 31   | 31 March 2027     |           | 1850.71         | 22.89         | 56.05    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 8  | 30   | 30 April 2027     |           | 1827.14         | 23.57         | 55.37    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 9  | 31   | 31 May 2027       |           | 1802.86         | 24.28         | 54.66    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 10 | 30   | 30 June 2027      |           | 1777.86         | 25.0          | 53.94    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 11 | 31   | 31 July 2027      |           | 1752.11         | 25.75         | 53.19    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 12 | 31   | 31 August 2027    |           | 1725.59         | 26.52         | 52.42    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 13 | 30   | 30 September 2027 |           | 1698.27         | 27.32         | 51.62    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 14 | 31   | 31 October 2027   |           | 1670.14         | 28.13         | 50.81    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 15 | 30   | 30 November 2027  |           | 1641.17         | 28.97         | 49.97    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 16 | 31   | 31 December 2027  |           | 1611.33         | 29.84         | 49.1     | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 17 | 31   | 31 January 2028   |           | 1580.6          | 30.73         | 48.21    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 18 | 29   | 29 February 2028  |           | 1548.95         | 31.65         | 47.29    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 19 | 31   | 31 March 2028     |           | 1516.35         | 32.6          | 46.34    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 20 | 30   | 30 April 2028     |           | 1482.77         | 33.58         | 45.36    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 21 | 31   | 31 May 2028       |           | 1448.19         | 34.58         | 44.36    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 22 | 30   | 30 June 2028      |           | 1412.58         | 35.61         | 43.33    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 23 | 31   | 31 July 2028      |           | 1375.9          | 36.68         | 42.26    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 24 | 31   | 31 August 2028    |           | 1338.12         | 37.78         | 41.16    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 25 | 30   | 30 September 2028 |           | 1299.21         | 38.91         | 40.03    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 26 | 31   | 31 October 2028   |           | 1259.14         | 40.07         | 38.87    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 27 | 30   | 30 November 2028  |           | 1217.87         | 41.27         | 37.67    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 28 | 31   | 31 December 2028  |           | 1175.36         | 42.51         | 36.43    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 29 | 31   | 31 January 2029   |           | 1131.58         | 43.78         | 35.16    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 30 | 28   | 28 February 2029  |           | 1086.49         | 45.09         | 33.85    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 31 | 31   | 31 March 2029     |           | 1040.05         | 46.44         | 32.5     | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 32 | 30   | 30 April 2029     |           | 992.22          | 47.83         | 31.11    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 33 | 31   | 31 May 2029       |           | 942.96          | 49.26         | 29.68    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 34 | 30   | 30 June 2029      |           | 892.23          | 50.73         | 28.21    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 35 | 31   | 31 July 2029      |           | 839.98          | 52.25         | 26.69    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 36 | 31   | 31 August 2029    |           | 786.17          | 53.81         | 25.13    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 37 | 30   | 30 September 2029 |           | 730.75          | 55.42         | 23.52    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 38 | 31   | 31 October 2029   |           | 673.67          | 57.08         | 21.86    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 39 | 30   | 30 November 2029  |           | 614.88          | 58.79         | 20.15    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 40 | 31   | 31 December 2029  |           | 554.34          | 60.54         | 18.4     | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 41 | 31   | 31 January 2030   |           | 491.98          | 62.36         | 16.58    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 42 | 28   | 28 February 2030  |           | 427.76          | 64.22         | 14.72    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 43 | 31   | 31 March 2030     |           | 361.62          | 66.14         | 12.8     | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 44 | 30   | 30 April 2030     |           | 293.5           | 68.12         | 10.82    | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 45 | 31   | 31 May 2030       |           | 223.34          | 70.16         | 8.78     | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 46 | 30   | 30 June 2030      |           | 151.08          | 72.26         | 6.68     | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 47 | 31   | 31 July 2030      |           | 76.66           | 74.42         | 4.52     | 0.0  | 0.0       | 78.94 | 0.0  | 0.0        | 0.0  | 78.94       |
      | 48 | 31   | 31 August 2030    |           | 0.0             | 76.66         | 2.29     | 0.0  | 0.0       | 78.95 | 0.0  | 0.0        | 0.0  | 78.95       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1997.62       | 1791.51  | 0.0  | 0.0       | 3789.13 | 0.0  | 0.0        | 0.0  | 3789.13     |
    When Admin sets the business date to "10 October 2026"
    When Admin successfully disburse the loan on "10 October 2026" with "679" EUR transaction amount
    Then Loan has 49 active number of terms
    Then Loan Repayment schedule has 49 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding  |
      |    |      | 31 August 2026    |           | 1997.62         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |              |
      | 1  | 30   | 30 September 2026 |           | 1978.44         | 19.18         | 59.76    | 0.0  | 0.0       | 78.94  | 0.0  | 0.0        | 0.0  | 78.94        |
      |    |      | 10 October 2026   |           | 679.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |              |
      | 2  | 31   | 31 October 2026   |           | 2624.8          | 32.64         | 73.13    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 3  | 30   | 30 November 2026  |           | 2597.56         | 27.24         | 78.53    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 4  | 31   | 31 December 2026  |           | 2569.5          | 28.06         | 77.71    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 5  | 31   | 31 January 2027   |           | 2540.6          | 28.9          | 76.87    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 6  | 28   | 28 February 2027  |           | 2510.84         | 29.76         | 76.01    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 7  | 31   | 31 March 2027     |           | 2480.19         | 30.65         | 75.12    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 8  | 30   | 30 April 2027     |           | 2448.62         | 31.57         | 74.2     | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 9  | 31   | 31 May 2027       |           | 2416.1          | 32.52         | 73.25    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 10 | 30   | 30 June 2027      |           | 2382.61         | 33.49         | 72.28    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 11 | 31   | 31 July 2027      |           | 2348.12         | 34.49         | 71.28    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 12 | 31   | 31 August 2027    |           | 2312.6          | 35.52         | 70.25    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 13 | 30   | 30 September 2027 |           | 2276.02         | 36.58         | 69.19    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 14 | 31   | 31 October 2027   |           | 2238.34         | 37.68         | 68.09    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 15 | 30   | 30 November 2027  |           | 2199.53         | 38.81         | 66.96    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 16 | 31   | 31 December 2027  |           | 2159.56         | 39.97         | 65.8     | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 17 | 31   | 31 January 2028   |           | 2118.4          | 41.16         | 64.61    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 18 | 29   | 29 February 2028  |           | 2076.01         | 42.39         | 63.38    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 19 | 31   | 31 March 2028     |           | 2032.35         | 43.66         | 62.11    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 20 | 30   | 30 April 2028     |           | 1987.38         | 44.97         | 60.8     | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 21 | 31   | 31 May 2028       |           | 1941.07         | 46.31         | 59.46    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 22 | 30   | 30 June 2028      |           | 1893.37         | 47.7          | 58.07    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 23 | 31   | 31 July 2028      |           | 1844.24         | 49.13         | 56.64    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 24 | 31   | 31 August 2028    |           | 1793.64         | 50.6          | 55.17    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 25 | 30   | 30 September 2028 |           | 1741.53         | 52.11         | 53.66    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 26 | 31   | 31 October 2028   |           | 1687.86         | 53.67         | 52.1     | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 27 | 30   | 30 November 2028  |           | 1632.59         | 55.27         | 50.5     | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 28 | 31   | 31 December 2028  |           | 1575.66         | 56.93         | 48.84    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 29 | 31   | 31 January 2029   |           | 1517.03         | 58.63         | 47.14    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 30 | 28   | 28 February 2029  |           | 1456.64         | 60.39         | 45.38    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 31 | 31   | 31 March 2029     |           | 1394.45         | 62.19         | 43.58    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 32 | 30   | 30 April 2029     |           | 1330.4          | 64.05         | 41.72    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 33 | 31   | 31 May 2029       |           | 1264.43         | 65.97         | 39.8     | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 34 | 30   | 30 June 2029      |           | 1196.49         | 67.94         | 37.83    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 35 | 31   | 31 July 2029      |           | 1126.51         | 69.98         | 35.79    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 36 | 31   | 31 August 2029    |           | 1054.44         | 72.07         | 33.7     | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 37 | 30   | 30 September 2029 |           | 980.22          | 74.22         | 31.55    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 38 | 31   | 31 October 2029   |           | 903.77          | 76.45         | 29.32    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 39 | 30   | 30 November 2029  |           | 825.04          | 78.73         | 27.04    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 40 | 31   | 31 December 2029  |           | 743.95          | 81.09         | 24.68    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 41 | 31   | 31 January 2030   |           | 660.44          | 83.51         | 22.26    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 42 | 28   | 28 February 2030  |           | 574.43          | 86.01         | 19.76    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 43 | 31   | 31 March 2030     |           | 485.85          | 88.58         | 17.19    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 44 | 30   | 30 April 2030     |           | 394.62          | 91.23         | 14.54    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 45 | 31   | 31 May 2030       |           | 300.66          | 93.96         | 11.81    | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 46 | 30   | 30 June 2030      |           | 203.88          | 96.78         | 8.99     | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 47 | 31   | 31 July 2030      |           | 104.21          | 99.67         | 6.1      | 0.0  | 0.0       | 105.77 | 0.0  | 0.0        | 0.0  | 105.77       |
      | 48 | 31   | 31 August 2030    |           | 0.97            | 103.24        | 3.12     | 0.0  | 0.0       | 106.36 | 0.0  | 0.0        | 0.0  | 106.36       |
      | 49 | 30   | 30 September 2030 |           | 0.0             | 0.97          | 0.03     | 0.0  | 0.0       | 1.0    | 0.0  | 0.0        | 0.0  | 1.0          |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 2676.62       | 2375.1   | 0.0  | 0.0       | 5051.72 | 0.0  | 0.0        | 0.0  | 5051.72     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 31 August 2026   | Disbursement     | 1997.62 | 0.0       | 0.0      | 0.0  | 0.0       | 1997.62      | false    | false    |
      | 10 October 2026  | Disbursement     | 679.0   | 0.0       | 0.0      | 0.0  | 0.0       | 2676.62      | false    | false    |
    When Loan Pay-off is made on "10 October 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115279
  Scenario: Verify full term tranche late disbursement on month-end shortened fromDate (31st, 6 terms) with non-zero interest rate merges schedule - UC6
    When Admin sets the business date to "31 August 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_FULL_TERM_TRANCHE | 31 August 2026    | 600            | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 August 2026" with "600" amount and expected disbursement date on "31 August 2026"
    When Admin successfully disburse the loan on "31 August 2026" with "300" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 August 2026    |           | 300.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 September 2026 |           | 251.03          | 48.97         | 2.5      | 0.0  | 0.0       | 51.47 | 0.0  | 0.0        | 0.0  | 51.47       |
      | 2  | 31   | 31 October 2026   |           | 201.65          | 49.38         | 2.09     | 0.0  | 0.0       | 51.47 | 0.0  | 0.0        | 0.0  | 51.47       |
      | 3  | 30   | 30 November 2026  |           | 151.86          | 49.79         | 1.68     | 0.0  | 0.0       | 51.47 | 0.0  | 0.0        | 0.0  | 51.47       |
      | 4  | 31   | 31 December 2026  |           | 101.65          | 50.21         | 1.26     | 0.0  | 0.0       | 51.47 | 0.0  | 0.0        | 0.0  | 51.47       |
      | 5  | 31   | 31 January 2027   |           | 51.03           | 50.62         | 0.85     | 0.0  | 0.0       | 51.47 | 0.0  | 0.0        | 0.0  | 51.47       |
      | 6  | 28   | 28 February 2027  |           | 0.0             | 51.03         | 0.42     | 0.0  | 0.0       | 51.45 | 0.0  | 0.0        | 0.0  | 51.45       |
    When Admin sets the business date to "10 October 2026"
    When Admin successfully disburse the loan on "10 October 2026" with "300" EUR transaction amount
    Then Loan has 7 active number of terms
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 August 2026    |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 30 September 2026 |           | 251.03          | 48.97         | 2.5      | 0.0  | 0.0       | 51.47  | 0.0  | 0.0        | 0.0  | 51.47       |
      |    |      | 10 October 2026   |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 31   | 31 October 2026   |           | 452.12          | 98.91         | 3.91     | 0.0  | 0.0       | 102.82 | 0.0  | 0.0        | 0.0  | 102.82      |
      | 3  | 30   | 30 November 2026  |           | 353.06          | 99.06         | 3.76     | 0.0  | 0.0       | 102.82 | 0.0  | 0.0        | 0.0  | 102.82      |
      | 4  | 31   | 31 December 2026  |           | 253.18          | 99.88         | 2.94     | 0.0  | 0.0       | 102.82 | 0.0  | 0.0        | 0.0  | 102.82      |
      | 5  | 31   | 31 January 2027   |           | 152.47          | 100.71        | 2.11     | 0.0  | 0.0       | 102.82 | 0.0  | 0.0        | 0.0  | 102.82      |
      | 6  | 28   | 28 February 2027  |           | 50.79           | 101.68        | 1.27     | 0.0  | 0.0       | 102.95 | 0.0  | 0.0        | 0.0  | 102.95      |
      | 7  | 31   | 31 March 2027     |           | 0.0             | 50.79         | 0.42     | 0.0  | 0.0       | 51.21  | 0.0  | 0.0        | 0.0  | 51.21        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 600.0         | 16.91    | 0.0  | 0.0       | 616.91 | 0.0  | 0.0        | 0.0  | 616.91      |
    When Loan Pay-off is made on "10 October 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

