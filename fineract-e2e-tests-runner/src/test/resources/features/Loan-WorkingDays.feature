@LoanFeature
@ResetWorkingDaysAfterAll
Feature: Loan - Working Days feature test

  Scenario: Working Days feature test LP1
    When Admin sets the business date to "22 October 2026"
    When Admin sets working days to weekdays
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT | 22 October 2026   | 100            | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 October 2026  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 32   | 23 November 2026 |           | 67.05           | 32.95         | 1.05     | 0.0  | 0.0       | 34.0  | 0.0  | 0.0        | 0.0  | 34.0        |
      | 2  | 29   | 22 December 2026 |           | 33.69           | 33.36         | 0.64     | 0.0  | 0.0       | 34.0  | 0.0  | 0.0        | 0.0  | 34.0        |
      | 3  | 31   | 22 January 2027  |           | 0.0             | 33.69         | 0.34     | 0.0  | 0.0       | 34.03 | 0.0  | 0.0        | 0.0  | 34.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.03     | 0.0  | 0.0       | 102.03 | 0.0  | 0.0        | 0.0  | 102.03      |
    And Admin successfully approves the loan on "22 October 2026" with "100" amount and expected disbursement date on "22 October 2026"
    When Admin successfully disburse the loan on "22 October 2026" with "100" EUR transaction amount

  Scenario: Working Days feature test LP2 progressive - 1st period is shifted
    When Admin sets the business date to "22 October 2026"
    When Admin sets working days to weekdays
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE | 22 October 2026   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             |
      |    |      | 22 October 2026  |
      | 1  | 32   | 23 November 2026 |
      | 2  | 29   | 22 December 2026 |
      | 3  | 31   | 22 January 2027  |
    And Admin successfully approves the loan on "22 October 2026" with "100" amount and expected disbursement date on "22 October 2026"
    When Admin successfully disburse the loan on "22 October 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             |
      |    |      | 22 October 2026  |
      | 1  | 32   | 23 November 2026 |
      | 2  | 29   | 22 December 2026 |
      | 3  | 31   | 22 January 2027  |

  Scenario: Working Days feature test LP2 progressive - 1st & 2nd is shifted
    When Admin sets the business date to "15 January 2025"
    When Admin sets working days to weekdays
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE | 15 January 2025   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             |
      |    |      | 15 January 2025  |
      | 1  | 33   | 17 February 2025 |
      | 2  | 28   | 17 March 2025    |
      | 3  | 29   | 15 April 2025    |
    And Admin successfully approves the loan on "15 January 2025" with "100" amount and expected disbursement date on "15 January 2025"
    When Admin successfully disburse the loan on "15 January 2025" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             |
      |    |      | 15 January 2025  |
      | 1  | 33   | 17 February 2025 |
      | 2  | 28   | 17 March 2025    |
      | 3  | 29   | 15 April 2025    |
