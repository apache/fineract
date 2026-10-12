@Repayment
Feature: LoanRepayment - Part2

  @TestRailId:C2632
  Scenario: RP14 - Repayment Schedule with interest type: Declining Balance - Interest Recalculation Frequency: Same as Repayment Period - Partial payment
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE | 01 November 2022  | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "5000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "20 November 2022"
    And Customer makes "AUTOPAY" repayment on "20 November 2022" with 200 EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    And Customer makes "AUTOPAY" repayment on "04 January 2023" with 200 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 November 2022 |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 December 2022 |           | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0  | 400.0 | 200.0      | 200.0 | 462.0       |
      | 2  | 31   | 01 January 2023  |           | 3368.0          | 819.32        | 42.68    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 3  | 31   | 01 February 2023 |           | 2555.42         | 812.58        | 49.42    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 4  | 28   | 01 March 2023    |           | 1716.94         | 838.48        | 23.52    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 5  | 31   | 01 April 2023    |           | 872.44          | 844.5         | 17.5     | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 6  | 30   | 01 May 2023      |           | 0.0             | 872.44        | 8.6      | 0.0  | 0.0       | 881.04 | 0.0   | 0.0        | 0.0   | 881.04      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 5000.0        | 191.04   | 0.0  | 0.0       | 5191.04 | 400.0 | 200.0      | 200.0 | 4791.04     |

  @TestRailId:C2633
  Scenario: RP15 - Repayment Schedule with interest type: Declining Balance - Interest Recalculation Frequency: Same as Repayment Period - Late payment
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE | 01 November 2022  | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "5000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 862 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 December 2022 | 01 February 2023 | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0  | 862.0 | 0.0        | 862.0 | 0.0         |
      | 2  | 31   | 01 January 2023  |                  | 3368.0          | 819.32        | 42.68    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 3  | 31   | 01 February 2023 |                  | 2556.96         | 811.04        | 50.96    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 4  | 28   | 01 March 2023    |                  | 1718.5          | 838.46        | 23.54    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 5  | 31   | 01 April 2023    |                  | 874.01          | 844.49        | 17.51    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 874.01        | 8.62     | 0.0  | 0.0       | 882.63 | 0.0   | 0.0        | 0.0   | 882.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 5000.0        | 192.63   | 0.0  | 0.0       | 5192.63 | 862.0 | 0.0        | 862.0 | 4330.63     |

  @TestRailId:C2634
  Scenario: RP16 - Repayment Schedule with interest type: Declining Balance - Interest Recalculation Frequency: Same as Repayment Period - Multi-disbursement
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTIDISB | 01 November 2022  | 10000          | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "10000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "01 December 2022"
    And Customer makes "AUTOPAY" repayment on "01 December 2022" with 1725 EUR transaction amount
    When Admin sets the business date to "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2023" with 1725 EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1725 EUR transaction amount
    When Admin sets the business date to "01 March 2023"
    When Admin successfully disburse the loan on "01 March 2023" with "2000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2023" with 1725 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |      |             |
      | 1  | 30   | 01 December 2022 | 01 December 2022 | 3325.0          | 1675.0        | 50.0     | 0.0  | 0.0       | 1725.0  | 1725.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 January 2023  |                  | 3000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |      |             |
      | 2  | 31   | 01 January 2023  | 01 January 2023  | 4633.25         | 1691.75       | 33.25    | 0.0  | 0.0       | 1725.0  | 1725.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2023 | 01 February 2023 | 2954.58         | 1678.67       | 46.33    | 0.0  | 0.0       | 1725.0  | 1725.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 March 2023    |                  | 2000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |      |             |
      | 4  | 28   | 01 March 2023    | 01 March 2023    | 3259.13         | 1695.45       | 29.55    | 0.0  | 0.0       | 1725.0  | 1725.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 April 2023    |                  | 1566.72         | 1692.41       | 32.59    | 0.0  | 0.0       | 1725.0  | 0.0    | 0.0        | 0.0  | 1725.0      |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 1566.72       | 15.67    | 0.0  | 0.0       | 1582.39 | 0.0    | 0.0        | 0.0  | 1582.39     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      | 10000.0       | 207.39   | 0.0  | 0.0       | 10207.39 | 6900.0 | 0.0        | 0.0  | 3307.39     |

  @TestRailId:C2636 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - adding charge due in the future
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 January 2023 | Flat             | 50.0 | 0.0  | 0.0    | 50.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 500.0 | 500.0      | 0.0  | 550.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 50        | 1050 | 500  | 500        | 0    | 550         |

  @TestRailId:C2637 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - adding charge due in the future, then repayments before and after charge due date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin sets the business date to "17 January 2023"
    And Customer makes "AUTOPAY" repayment on "17 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "21 January 2023"
    And Customer makes "AUTOPAY" repayment on "21 January 2023" with 50 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 17 January 2023  | Repayment        | 450.0  | 450.0     | 0.0      | 0.0  | 0.0       | 50.0         |
      | 21 January 2023  | Repayment        | 50.0   | 0.0       | 0.0      | 0.0  | 50.0      | 50.0         |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 January 2023 | Flat             | 50.0 | 50.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 1000.0 | 1000.0     | 0.0  | 50.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 50        | 1050 | 1000 | 1000       | 0    | 50          |

  @TestRailId:C2638 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - adding charge due in the future, then repayment before due date, new charge with due date in future and repayment on first charge due date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin sets the business date to "17 January 2023"
    And Customer makes "AUTOPAY" repayment on "17 January 2023" with 100 EUR transaction amount
    When Admin sets the business date to "19 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "23 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "20 January 2023"
    And Customer makes "AUTOPAY" repayment on "20 January 2023" with 100 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 17 January 2023  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 400.0        |
      | 20 January 2023  | Repayment        | 100.0  | 50.0      | 0.0      | 0.0  | 50.0      | 350.0        |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 January 2023 | Flat             | 50.0 | 50.0 | 0.0    | 0.0         |
      | NSF fee | true      | Specified due date | 23 January 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 60.0      | 1060.0 | 700.0 | 700.0      | 0.0  | 360.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 60        | 1060 | 700  | 700        | 0    | 360         |

  @TestRailId:C2639 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - adding charge due in the future, then repayment before due date with full amount
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin sets the business date to "17 January 2023"
    And Customer makes "AUTOPAY" repayment on "17 January 2023" with 550 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 17 January 2023  | Repayment        | 550.0  | 500.0     | 0.0      | 0.0  | 50.0      | 0.0          |
      | 17 January 2023  | Accrual          | 50.0   | 0.0       | 0.0      | 0.0  | 50.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 January 2023 | Flat             | 50.0 | 50.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 17 January 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 1050.0 | 1050.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 50        | 1050 | 1050 | 1050       | 0    | 0           |

  @TestRailId:C2655 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1000 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                  | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |                  | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 1000 | 0          | 0    | 2000        |

  @TestRailId:C2656 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - inAdvance principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 10 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 10 January 2023 | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 1000.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 1000 | 1000       | 0    | 2000        |

  @TestRailId:C2657 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due + inAdvance principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 1500.0 | 1500.0    | 0.0      | 0.0  | 0.0       | 1500.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                  | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 500.0  | 500.0      | 0.0  | 500.0       |
      | 3  | 31   | 01 April 2023    |                  | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 1500 | 500        | 0    | 1500        |

  @TestRailId:C2658 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 500.0  | 450.0     | 0.0      | 0.0  | 50.0      | 2550.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 500.0 | 0.0        | 0.0  | 550.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 50        | 3050 | 500  | 0          | 0    | 2550        |

  @TestRailId:C2659 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due fee + due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 500.0  | 450.0     | 0.0      | 50.0 | 0.0       | 2550.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 50.0 | 0.0       | 1050.0 | 500.0 | 0.0        | 0.0  | 550.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 50   | 0         | 3050 | 500  | 0          | 0    | 2550        |

  @TestRailId:C2660 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due interest + due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 500.0  | 470.0     | 30.0     | 0.0  | 0.0       | 2530.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 500.0 | 0.0        | 0.0  | 530.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0   | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0   | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 0    | 0         | 3090 | 500  | 0          | 0    | 2590        |

  @TestRailId:C2661 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + due fee + due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 500.0  | 400.0     | 0.0      | 50.0 | 50.0      | 2600.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 50.0 | 50.0      | 1100.0 | 500.0 | 0.0        | 0.0  | 600.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 50   | 50        | 3100 | 500  | 0          | 0    | 2600        |

  @TestRailId:C2662 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 450.0     | 0.0      | 0.0  | 50.0      | 2550.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 500.0 | 500.0      | 0.0  | 550.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 50        | 3050 | 500  | 500        | 0    | 2550        |

  @TestRailId:C2663 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal + inAdvance penalty not effective because of partial payment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 450.0     | 0.0      | 0.0  | 50.0      | 2550.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 100.0     | 1100.0 | 500.0 | 500.0      | 0.0  | 600.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 100       | 3100 | 500  | 500        | 0    | 2600        |

  @TestRailId:C2664 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal + inAdvance penalty
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1100 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1100.0 | 1000.0    | 0.0      | 0.0  | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 0.0      | 0.0  | 100.0     | 1100.0 | 1100.0 | 1100.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 100       | 3100 | 1100 | 1100       | 0    | 2000        |

  @TestRailId:C2665 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal + inAdvance penalty + inAdvance fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1150 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1150.0 | 1000.0    | 0.0      | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 0.0      | 50.0 | 100.0     | 1150.0 | 1150.0 | 1150.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 50   | 100       | 3150 | 1150 | 1150       | 0    | 2000        |

  @TestRailId:C2666 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal + inAdvance penalty + inAdvance fee + inAdvance interest
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1180 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |

  @TestRailId:C2667 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - repayment + reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1180 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Customer undo "1"th transaction made on "15 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 0.0  | 0.0        | 0.0  | 1180.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 0    | 0          | 0    | 3240        |

  @TestRailId:C2668 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - merchant issued refund + reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2023" with 1180 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement           | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Merchant Issued Refund | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Customer undo "1"th transaction made on "15 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement           | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Merchant Issued Refund | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 0.0  | 0.0        | 0.0  | 1180.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 0    | 0          | 0    | 3240        |

  @TestRailId:C2669 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - payout refund + reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2023" with 1180 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Payout Refund    | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Customer undo "1"th transaction made on "15 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Payout Refund    | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 0.0  | 0.0        | 0.0  | 1180.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 0    | 0          | 0    | 3240        |

  @TestRailId:C2670 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - goodwill credit transaction + reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 January 2023" with 1180 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Goodwill Credit  | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Customer undo "1"th transaction made on "15 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Goodwill Credit  | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 0.0  | 0.0        | 0.0  | 1180.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 0    | 0          | 0    | 3240        |

  @TestRailId:C2671 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - repayment + charge adjustment + charge adjustment reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1180 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Admin sets the business date to "27 January 2023"
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "20 January 2023" with 50 EUR transaction amount and externalId ""
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement      | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment         | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
      | 27 January 2023  | Charge Adjustment | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1950.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 50.0   | 50.0       | 0.0  | 980.0       |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1230 | 1230       | 0    | 2010        |
    When Admin sets the business date to "30 January 2023"
    When Admin reverts the charge adjustment which was raised on "27 January 2023" with 50 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2023  | Disbursement      | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       | false    |
      | 15 January 2023  | Repayment         | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       | false    |
      | 27 January 2023  | Charge Adjustment | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1950.0       | true     |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |

  @TestRailId:C2682 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - fee - repayment - nsffee - chargeback - repayment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "18 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "18 January 2023" due date and 25 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 500 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "21 January 2023"
    And Customer makes "AUTOPAY" repayment on "21 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 490.0     | 0.0      | 10.0 | 0.0       | 510.0        |
      | 18 January 2023  | Chargeback       | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 1010.0       |
      | 21 January 2023  | Repayment        | 500.0  | 475.0     | 0.0      | 0.0  | 25.0      | 535.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1500.0        | 0.0      | 10.0 | 25.0      | 1535.0 | 1000.0 | 1000.0     | 0.0  | 535.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1500          | 0        | 10   | 25        | 1535 | 1000 | 1000       | 0    | 535         |

  @TestRailId:C2799 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy: Same day transaction
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    And Customer makes "AUTOPAY" repayment on "04 January 2023" with 200 EUR transaction amount
    When Admin sets the business date to "12 January 2023"
    And Customer makes "AUTOPAY" repayment on "12 January 2023" with 300 EUR transaction amount
    And Admin adds a 1 % Processing charge to the loan with "en" locale on date: "12 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2023  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 12 January 2023  | Repayment        | 300.0  | 290.0     | 0.0      | 10.0 | 0.0       | 510.0        |
    And Customer makes "AUTOPAY" repayment on "12 January 2023" with 510 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2023  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 12 January 2023  | Repayment        | 300.0  | 290.0     | 0.0      | 10.0 | 0.0       | 510.0        |
      | 12 January 2023  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 12 January 2023  | Repayment        | 510.0  | 510.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C2694 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC1 - no fees or penalties, due payment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1000 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 0          | 0    | 0           |

  @TestRailId:C2695 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC2 - due principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1020 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 0          | 0    | 0           |

  @TestRailId:C2696 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC3 - in advance principal, reverted, due penalty, principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |

    When Admin sets the business date to "28 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "25 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "28 January 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 28 January 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1020 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1020.0 | 1000.0    | 0.0      | 0.0  | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 28 January 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 1020.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 20        | 1020 | 1020 | 0          | 0    | 0           |

  @TestRailId:C2697 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC4 - in advance principal, fee, reverted, due penalty, principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 25 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "28 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "25 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1040 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 0          | 0    | 0           |

  @TestRailId:C2698 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC5 - in advance principal, fee, reverted, multiple due penalty, principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 25 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "28 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "25 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1040 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 0          | 0    | 0           |

    When Admin sets the business date to "05 February 2023"
    When Customer undo "1"th "Repayment" transaction made on "01 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "08 February 2023" due date and 20 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 February 2023" is reverted
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 08 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "08 February 2023"
    And Customer makes "AUTOPAY" repayment on "08 February 2023" with 1060 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 February 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 08 February 2023 | Repayment        | 1060.0 | 1000.0    | 0.0      | 20.0 | 40.0      | 0.0          |
      | 08 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 08 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2023 | 08 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 0.0        | 1040.0 | 0.0         |
      | 2  | 7    | 08 February 2023 | 08 February 2023 | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0   | 20.0   | 0.0        | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 40        | 1060 | 1060 | 0          | 1040 | 0           |

  @TestRailId:C2699 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC6 - partial payment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 500.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 500        | 0    | 0           |

  @TestRailId:C2700 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC7 - partial payment, in advance principal, due principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 500.0 | 500.0      | 0.0  | 520.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 500  | 500        | 0    | 520         |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 520 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2023 | Repayment        | 520.0  | 500.0     | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 500.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 500        | 0    | 0           |

  @TestRailId:C2701 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC8 - partial payment, in advance principal, due penalty, principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

    When Admin sets the business date to "25 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "28 January 2023"
    And Customer makes "AUTOPAY" repayment on "28 January 2023" with 520 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 520.0 | 520.0      | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 20        | 1020 | 520  | 520        | 0    | 500         |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 1020.0 | 520.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 20        | 1020 | 1020 | 520        | 0    | 0           |

  @TestRailId:C2702 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC9 - partial payment, in advance principal, fee, due penalty, principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 10 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "25 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "28 January 2023"
    And Customer makes "AUTOPAY" repayment on "28 January 2023" with 520 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 520.0 | 520.0      | 0.0  | 520.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 520  | 520        | 0    | 520         |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1020.0 | 520.0      | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1020 | 520        | 0    | 20          |

    When Admin sets the business date to "05 February 2023"
    And Customer makes "AUTOPAY" repayment on "05 February 2023" with 20 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 05 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 05 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 520.0      | 20.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 520        | 20   | 0           |

  @TestRailId:C2703 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC10 - partial payment, in advance principal, fee, due penalty
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 10 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "25 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "28 January 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "28 January 2023"
    And Customer makes "AUTOPAY" repayment on "28 January 2023" with 520 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 520.0 | 520.0      | 0.0  | 520.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 520  | 520        | 0    | 520         |

    When Admin sets the business date to "30 January 2023"
    And Customer makes "AUTOPAY" repayment on "30 January 2023" with 520 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
      | 30 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 20.0 | 0.0       | 0.0          |
      | 30 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 30 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 1040.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 1040       | 0    | 0           |

  @TestRailId:C2704 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC11 - partial payment, in advance principal, fee, due penalty, principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "05 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 10 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "25 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "28 January 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "28 January 2023"
    And Customer makes "AUTOPAY" repayment on "28 January 2023" with 1040 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 28 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 28 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 1040.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 1040       | 0    | 0           |

    When Admin sets the business date to "30 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "28 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "28 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 28 January 2023  | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 28 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 40.0      | 1060.0 | 20.0 | 0.0        | 0.0  | 1040.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 40        | 1060 | 20   | 0          | 0    | 1040        |

    When Admin sets the business date to "05 February 2023"
    And Customer makes "AUTOPAY" repayment on "05 February 2023" with 1040 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 28 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 05 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 05 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2023 | 05 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 40.0      | 1060.0 | 1060.0 | 0.0        | 1040.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 40        | 1060 | 1060 | 0          | 1040 | 0           |

  @TestRailId:C2705 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC12 - partial payment, in advance penalty, interest, principal, fee due penalty, interest, principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT | 01 January 2023   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "05 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 20 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 20.0 | 20.0       | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 20   | 20         | 0    | 1030        |

    When Admin sets the business date to "15 January 2023"
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 490.0     | 10.0     | 0.0  | 0.0       | 510.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 520.0 | 520.0      | 0.0  | 530.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 520  | 520        | 0    | 530         |

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 530 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 490.0     | 10.0     | 0.0  | 0.0       | 510.0        |
      | 25 January 2023  | Repayment        | 530.0  | 510.0     | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 25 January 2023 | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 1050.0 | 1050.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 1050 | 1050       | 0    | 0           |

    When Admin sets the business date to "30 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "15 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "25 January 2023"
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "15 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 20 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 470.0     | 10.0     | 0.0  | 20.0      | 530.0        |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
      | 25 January 2023  | Repayment        | 530.0  | 500.0     | 10.0     | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 20.0 | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 20   | 0          | 0    | 1030        |
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 470.0     | 10.0     | 0.0  | 20.0      | 530.0        |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
      | 25 January 2023  | Repayment        | 530.0  | 500.0     | 10.0     | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 01 February 2023 | Repayment        | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1000 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 470.0     | 10.0     | 0.0  | 20.0      | 530.0        |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
      | 25 January 2023  | Repayment        | 530.0  | 500.0     | 10.0     | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 01 February 2023 | Repayment        | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 20 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 470.0     | 10.0     | 0.0  | 20.0      | 530.0        |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
      | 25 January 2023  | Repayment        | 530.0  | 500.0     | 10.0     | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 01 February 2023 | Repayment        | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "15 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 1050.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 1050 | 0          | 0    | 0           |

  @TestRailId:C2800 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: Same day transaction - UC2
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT | 01 January 2023   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    And Customer makes "AUTOPAY" repayment on "04 January 2023" with 200 EUR transaction amount
    When Admin sets the business date to "12 January 2023"
    And Customer makes "AUTOPAY" repayment on "12 January 2023" with 300 EUR transaction amount
    And Admin adds a 1 % Processing charge to the loan with "en" locale on date: "12 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2023  | Repayment        | 200.0  | 190.0     | 10.0     | 0.0  | 0.0       | 810.0        |
      | 12 January 2023  | Repayment        | 300.0  | 289.9     | 0.0      | 10.1 | 0.0       | 520.1        |
    And Customer makes "AUTOPAY" repayment on "12 January 2023" with 520.10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2023  | Repayment        | 200.0  | 190.0     | 10.0     | 0.0  | 0.0       | 810.0        |
      | 12 January 2023  | Repayment        | 300.0  | 289.9     | 0.0      | 10.1 | 0.0       | 520.1        |
      | 12 January 2023  | Repayment        | 520.1  | 520.1     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 12 January 2023  | Accrual          | 20.1   | 0.0       | 10.0     | 10.1 | 0.0       | 0.0          |

  @TestRailId:C2810
  Scenario: As a user I would like to adjust an existing repayment and validate the event
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1         | 01 November 2022  | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 November 2022" with "1000" amount and expected disbursement date on "01 November 2022"
    When Admin successfully disburse the loan on "01 November 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "02 November 2022"
    And Customer makes "AUTOPAY" repayment on "02 November 2022" with 9 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "02 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 9.0    | 9.0       | 0.0      | 0.0  | 0.0       | 991.0        |
    When Customer adjust "1"th repayment on "02 November 2022" with amount "10"
    Then Loan Transactions tab has a transaction with date: "02 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 9.0    | 9.0       | 0.0      | 0.0  | 0.0       | 991.0        |
      | Repayment        | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 990.0        |

  @TestRailId:C2898
  Scenario: Verify that in case of non/disbursed loan LoanRepaymentDueBusinessEvent is not sent - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 31 October 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2899
  Scenario: Verify that in case of non/disbursed loan LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2900
  Scenario: Verify that in case of non/disbursed loan LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2901 @AdvancedPaymentAllocation
  Scenario: Verify that in case of non/disbursed loan LoanRepaymentDueBusinessEvent is not sent - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2902
  Scenario: Verify that in case of pre-payed installment LoanRepaymentDueBusinessEvent is not sent - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 October 2023 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 31 October 2023 | 01 October 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 1000.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 1000       | 0    | 0           |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2903
  Scenario: Verify that in case of pre-payed installment LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 250 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 250        | 0    | 500         |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2904
  Scenario: Verify that in case of pre-payed installment LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 250        | 0    | 500         |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2905 @AdvancedPaymentAllocation
  Scenario: Verify that in case of pre-payed installment LoanRepaymentDueBusinessEvent is not sent - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 250 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 250        | 0    | 500         |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2906
  Scenario: Verify that in case of pre-payed installments for total amount (loan balance is 0) LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 750 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 01 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 01 October 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 1000.0 | 750        | 0    | 0           |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "15 November 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2907
  Scenario: Verify that in case of pre-payed installments for total amount (loan balance is 0) LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 01 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 01 October 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 1000.0 | 750        | 0    | 0           |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "15 November 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

