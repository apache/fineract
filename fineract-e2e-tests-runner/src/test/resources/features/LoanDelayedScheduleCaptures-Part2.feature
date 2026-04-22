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

