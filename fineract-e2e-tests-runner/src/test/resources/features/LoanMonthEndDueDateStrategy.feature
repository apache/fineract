@LoanMonthEndDueDateStrategyFeature
Feature: Loan month end due date strategy

  # When a monthly due date falls on a day that does not exist in the target month,
  # the LAST_DAY_OF_MONTH strategy keeps the date inside the target month,
  # while the FIRST_DAY_OF_NEXT_MONTH strategy rolls it to the first day of the following month.

  # --- Loan account level checks ---
  @TestRailId:C111043
  Scenario: Verify monthEndDueDateStrategy - UC1: Progressive ACTUAL/ACTUAL loan rolls missing end-of-month dates and adjusts interest
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_MONTHLY_END_DUE_DATE_ROLL       | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan has monthEndDueDateStrategy "FIRST_DAY_OF_NEXT_MONTH"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 29   | 01 March 2025    |           | 6696.78         | 3303.22       | 95.34    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 2  | 30   | 31 March 2025    |           | 3364.27         | 3332.51       | 66.05    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 3  | 31   | 01 May 2025      |           | 0.0             | 3364.27       | 34.29    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 10000.0       | 195.68   | 0.0  | 0.0       | 10195.68| 0.0  | 0.0        | 0.0  | 10195.68    |
    When Admin sets the business date to "01 May 2025"
    And Loan Pay-off is made on "01 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111044
  Scenario: Verify monthEndDueDateStrategy - UC2: Cumulative ACTUAL/ACTUAL loan rolls missing end-of-month dates and adjusts interest
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_MONTHLY_END_DUE_DATE_ROLL_CUMULATIVE       | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD           | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER                |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan has monthEndDueDateStrategy "FIRST_DAY_OF_NEXT_MONTH"
    Then Loan Repayment schedule has 3 periods, with the following due dates only:
      | Nr | Date             |
      | 1  | 01 March 2025    |
      | 2  | 31 March 2025    |
      | 3  | 01 May 2025      |
    When Admin sets the business date to "01 May 2025"
    And Loan Pay-off is made on "01 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111045
  Scenario: Verify monthEndDueDateStrategy - UC3: Progressive 30/360 loan keeps per-period interest unchanged when dates roll forward
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    # Roll strategy loan: dates move to the next month, but each period has 30 days.
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_MONTHLY_36030_END_DUE_DATE_ROLL    | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 29   | 01 March 2025    |           | 6699.78         | 3300.22       | 100.0    | 0.0  | 0.0       | 3400.22| 0.0  | 0.0        | 0.0  | 3400.22     |
      | 2  | 30   | 31 March 2025    |           | 3366.56         | 3333.22       | 67.0     | 0.0  | 0.0       | 3400.22| 0.0  | 0.0        | 0.0  | 3400.22     |
      | 3  | 31   | 01 May 2025      |           | 0.0             | 3366.56       | 33.67    | 0.0  | 0.0       | 3400.23| 0.0  | 0.0        | 0.0  | 3400.23     |
    # Clamp strategy loan: dates stay inside the target month, but period interest is the same.
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_MONTHLY_36030_END_DUE_DATE_CLAMP   | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6699.78         | 3300.22       | 100.0    | 0.0  | 0.0       | 3400.22| 0.0  | 0.0        | 0.0  | 3400.22     |
      | 2  | 31   | 31 March 2025    |           | 3366.56         | 3333.22       | 67.0     | 0.0  | 0.0       | 3400.22| 0.0  | 0.0        | 0.0  | 3400.22     |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3366.56       | 33.67    | 0.0  | 0.0       | 3400.23| 0.0  | 0.0        | 0.0  | 3400.23     |
    When Admin sets the business date to "01 May 2025"
    And Loan Pay-off is made on "01 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111046
  Scenario: Verify monthEndDueDateStrategy - UC4: Cumulative 30/360 loan keeps per-period interest unchanged when dates roll forward
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_MONTHLY_36030_END_DUE_DATE_ROLL_CUMULATIVE    | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD           | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER                |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following due dates only:
      | Nr | Date             |
      | 1  | 01 March 2025    |
      | 2  | 31 March 2025    |
      | 3  | 01 May 2025      |
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_MONTHLY_36030_END_DUE_DATE_CLAMP_CUMULATIVE   | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD           | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER                |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following due dates only:
      | Nr | Date             |
      | 1  | 28 February 2025 |
      | 2  | 31 March 2025    |
      | 3  | 30 April 2025    |
    When Admin sets the business date to "01 May 2025"
    And Loan Pay-off is made on "01 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111047
  Scenario: Verify monthEndDueDateStrategy - UC5: Default LAST_DAY_OF_MONTH strategy keeps legacy clamp behaviour
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_MONTHLY_END_DUE_DATE_CLAMP        | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan has monthEndDueDateStrategy "LAST_DAY_OF_MONTH"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6694.23         | 3305.77       | 92.05    | 0.0  | 0.0       | 3397.82| 0.0  | 0.0        | 0.0  | 3397.82     |
      | 2  | 31   | 31 March 2025    |           | 3364.64         | 3329.59       | 68.23    | 0.0  | 0.0       | 3397.82| 0.0  | 0.0        | 0.0  | 3397.82     |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3364.64       | 33.19    | 0.0  | 0.0       | 3397.83| 0.0  | 0.0        | 0.0  | 3397.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 10000.0       | 193.47   | 0.0  | 0.0       | 10193.47| 0.0  | 0.0        | 0.0  | 10193.47    |
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_MONTHLY_END_DUE_DATE_CLAMP_CUMULATIVE      | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD           | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER                |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan has monthEndDueDateStrategy "LAST_DAY_OF_MONTH"
    Then Loan Repayment schedule has 3 periods, with the following due dates only:
      | Nr | Date             |
      | 1  | 28 February 2025 |
      | 2  | 31 March 2025    |
      | 3  | 30 April 2025    |
    When Admin sets the business date to "30 April 2025"
    And Loan Pay-off is made on "30 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111048
  Scenario: Verify monthEndDueDateStrategy - UC6: Leap-day anchor rolls only when the next common-year February is reached
    When Admin sets the business date to "29 February 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_MONTHLY_END_DUE_DATE_ROLL_CUMULATIVE       | 29 February 2024  | 10000          | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD           | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER                |
    And Admin successfully approves the loan on "29 February 2024" with "10000" amount and expected disbursement date on "29 February 2024"
    And Admin successfully disburse the loan on "29 February 2024" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following due dates only:
      | Nr | Date             |
      | 1  | 29 March 2024    |
      | 2  | 29 April 2024    |
      | 3  | 29 May 2024      |
      | 4  | 29 June 2024     |
      | 5  | 29 July 2024     |
      | 6  | 29 August 2024   |
      | 7  | 29 September 2024|
      | 8  | 29 October 2024  |
      | 9  | 29 November 2024 |
      | 10 | 29 December 2024 |
      | 11 | 29 January 2025  |
      | 12 | 01 March 2025    |
    When Admin sets the business date to "01 March 2025"
    And Loan Pay-off is made on "01 March 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111049
  Scenario: Verify monthEndDueDateStrategy - UC7: Roll happens only when the seed day is missing in the target month
    When Admin sets the business date to "28 January 2025"
    And Admin creates a client with random data
    # 28 exists in every month, so the roll strategy behaves like the default.
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_MONTHLY_END_DUE_DATE_ROLL       | 28 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 January 2025" with "10000" amount and expected disbursement date on "28 January 2025"
    And Admin successfully disburse the loan on "28 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following due dates only:
      | Nr | Date             |
      | 1  | 28 February 2025 |
      | 2  | 28 March 2025    |
      | 3  | 28 April 2025    |
    And Admin creates a client with random data
    When Admin sets the business date to "30 January 2025"
    # 30 does not exist in February, so the roll strategy moves the February due date to 1 March.
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_MONTHLY_END_DUE_DATE_ROLL       | 30 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "30 January 2025" with "10000" amount and expected disbursement date on "30 January 2025"
    And Admin successfully disburse the loan on "30 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following due dates only:
      | Nr | Date             |
      | 1  | 01 March 2025    |
      | 2  | 30 March 2025    |
      | 3  | 30 April 2025    |
    When Admin sets the business date to "30 April 2025"
    And Loan Pay-off is made on "30 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111050
  Scenario: Verify monthEndDueDateStrategy - UC8: Loan inherits the product strategy and can override it at application time
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    # The product is configured with LAST_DAY_OF_MONTH; the loan overrides to FIRST_DAY_OF_NEXT_MONTH.
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | monthEndDueDateStrategy |
      | LP2_MONTHLY_END_DUE_DATE_CLAMP      | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | FIRST_DAY_OF_NEXT_MONTH |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan has monthEndDueDateStrategy "FIRST_DAY_OF_NEXT_MONTH"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 29   | 01 March 2025    |           | 6696.78         | 3303.22       | 95.34    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 2  | 30   | 31 March 2025    |           | 3364.27         | 3332.51       | 66.05    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 3  | 31   | 01 May 2025      |           | 0.0             | 3364.27       | 34.29    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
    When Admin sets the business date to "01 May 2025"
    And Loan Pay-off is made on "01 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111051
  Scenario: Verify monthEndDueDateStrategy - UC9: Product strategy change does not affect existing loans
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_MONTHLY_END_DUE_DATE_ROLL       | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan has monthEndDueDateStrategy "FIRST_DAY_OF_NEXT_MONTH"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 29   | 01 March 2025    |           | 6696.78         | 3303.22       | 95.34    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 2  | 30   | 31 March 2025    |           | 3364.27         | 3332.51       | 66.05    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 3  | 31   | 01 May 2025      |           | 0.0             | 3364.27       | 34.29    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
    When Admin updates "LP2_MONTHLY_END_DUE_DATE_ROLL" loan product monthEndDueDateStrategy to "LAST_DAY_OF_MONTH"
    Then Loan has monthEndDueDateStrategy "FIRST_DAY_OF_NEXT_MONTH"
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 29   | 01 March 2025    |           | 6696.78         | 3303.22       | 95.34    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 2  | 30   | 31 March 2025    |           | 3364.27         | 3332.51       | 66.05    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 3  | 31   | 01 May 2025      |           | 0.0             | 3364.27       | 34.29    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
    # Restore the product strategy so subsequent scenarios start from a known state.
    When Admin updates "LP2_MONTHLY_END_DUE_DATE_ROLL" loan product monthEndDueDateStrategy to "FIRST_DAY_OF_NEXT_MONTH"
    When Admin sets the business date to "01 May 2025"
    And Loan Pay-off is made on "01 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111052
  Scenario: Verify monthEndDueDateStrategy - UC10: Backdated repayment regeneration keeps the rolled due dates
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_MONTHLY_END_DUE_DATE_ROLL       | 31 January 2025 | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 29   | 01 March 2025    |           | 6696.78         | 3303.22       | 95.34    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 2  | 30   | 31 March 2025    |           | 3364.27         | 3332.51       | 66.05    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
      | 3  | 31   | 01 May 2025      |           | 0.0             | 3364.27       | 34.29    | 0.0  | 0.0       | 3398.56| 0.0  | 0.0        | 0.0  | 3398.56     |
    When Admin sets the business date to "10 March 2025"
    And Customer makes "AUTOPAY" repayment on "15 February 2025" with 1000.0 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following due dates only:
      | Nr | Date             |
      | 1  | 01 March 2025    |
      | 2  | 31 March 2025    |
      | 3  | 01 May 2025      |
    When Admin sets the business date to "01 May 2025"
    And Loan Pay-off is made on "01 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

    # --- Loan product level check ---
  @TestRailId:C111053
  Scenario: Verify monthEndDueDateStrategy - UC11: Loan product CRUD exposes the monthEndDueDateStrategy attribute
    When Admin creates a new Loan Product with monthEndDueDateStrategy "FIRST_DAY_OF_NEXT_MONTH"
    Then created Loan Product has monthEndDueDateStrategy "FIRST_DAY_OF_NEXT_MONTH"
    When Admin updates created Loan Product monthEndDueDateStrategy to "LAST_DAY_OF_MONTH"
    Then created Loan Product has monthEndDueDateStrategy "LAST_DAY_OF_MONTH"
    When Admin retrieves the Loan Product template for monthEndDueDateStrategy
    Then Loan Product template has monthEndDueDateStrategy options:
      | LAST_DAY_OF_MONTH       |
      | FIRST_DAY_OF_NEXT_MONTH |

  @TestRailId:C111054
  Scenario: Verify monthEndDueDateStrategy - UC12: Global loan product default strategies are correctly initialized
    Then Loan Product "LP1_MONTHLY_END_DUE_DATE_CLAMP_CUMULATIVE" has monthEndDueDateStrategy "LAST_DAY_OF_MONTH"
    And Loan Product "LP2_MONTHLY_END_DUE_DATE_ROLL" has monthEndDueDateStrategy "FIRST_DAY_OF_NEXT_MONTH"