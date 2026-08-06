@LoanRescheduleFeature
Feature: LoanReschedule

  @TestRailId:C2680
  Scenario: As a user I would like to see a loan changed event was triggered when the loan got rescheduled
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    When Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    When Batch API call with steps: rescheduleLoan from "1 August 2022" to "31 August 2022" submitted on date: "1 July 2022", approveReschedule on date: "1 July 2022" runs with enclosingTransaction: "true"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2022   |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 61   | 31 August 2022 |           | 0.0             | 5000.0        | 0.0      | 0.0  | 0.0       | 5000.0 | 0.0  | 0.0        | 0.0  | 5000.0      |

  @TestRailId:C2801
  Scenario: Verify that loan is rescheduled properly in case of: undo disbursement
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 July 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    When Admin sets the business date to "02 July 2023"
    When Admin successfully undo disbursal
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 31 July 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |

  @TestRailId:C2802
  Scenario: Verify that loan is rescheduled properly in case of: multiple disbursement, second disbursement placed within payment period
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "500" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023 |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 31 July 2023 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 500           | 0        | 0    | 0         | 500 | 0    | 0          | 0    | 500         |
    When Admin sets the business date to "10 July 2023"
    And Admin successfully disburse the loan on "10 July 2023" with "500" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 10 July 2023 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 July 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |

  @TestRailId:C2803
  Scenario: Verify that loan is rescheduled properly in case of: multiple disbursement, second disbursement placed after payment period
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "500" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023 |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 31 July 2023 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 500           | 0        | 0    | 0         | 500 | 0    | 0          | 0    | 500         |
    When Admin sets the business date to "10 August 2023"
    And Admin successfully disburse the loan on "10 August 2023" with "500" EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 31 July 2023   |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
      |    |      | 10 August 2023 |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 30   | 30 August 2023 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023 |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 31 July 2023 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 500           | 0        | 0    | 0         | 500 | 0    | 0          | 0    | 500         |

  @TestRailId:C2804
  Scenario: Verify that loan is rescheduled properly in case of: single installment, changing installment date
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 July 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    When Admin sets the business date to "05 July 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 31 July 2023       | 05 July 2023    | 31 August 2023  |                  |                 |            |                 |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 61   | 31 August 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |

  @TestRailId:C2805
  Scenario: Verify that loan is rescheduled properly in case of: multiple installments, changing installment date
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 01 July 2023      | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 July 2023" with "3000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "3000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 31   | 01 September 2023 |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 3  | 30   | 01 October 2023   |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 0    | 0          | 0    | 3000        |
    When Admin sets the business date to "05 July 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate   | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 September 2023  | 05 July 2023    | 15 September 2023 |                  |                 |            |                 |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 45   | 15 September 2023 |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 3  | 30   | 15 October 2023   |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 0    | 0          | 0    | 3000        |

  @TestRailId:C2806
  Scenario: Verify that loan is rescheduled properly in case of: multiple installments, extending repayment schedule
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 01 July 2023      | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 July 2023" with "3000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "3000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 31   | 01 September 2023 |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 3  | 30   | 01 October 2023   |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 0    | 0          | 0    | 3000        |
    When Admin sets the business date to "05 July 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 September 2023  | 05 July 2023    |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 31   | 01 September 2023 |           | 1500.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0  | 0.0  | 0.0        | 0.0  | 500.0       |
      | 3  | 30   | 01 October 2023   |           | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0  | 0.0  | 0.0        | 0.0  | 500.0       |
      | 4  | 31   | 01 November 2023  |           | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0  | 0.0  | 0.0        | 0.0  | 500.0       |
      | 5  | 30   | 01 December 2023  |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0  | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 0    | 0          | 0    | 3000        |

  @TestRailId:C2807
  Scenario: Verify that loan is rescheduled properly in case of: multiple installments, mid-term grace period on principal
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 01 July 2023      | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 July 2023" with "3000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "3000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 31   | 01 September 2023 |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 3  | 30   | 01 October 2023   |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 0    | 0          | 0    | 3000        |
    When Admin sets the business date to "05 July 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 September 2023  | 05 July 2023    |                 | 1                |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |              | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |              | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 31   | 01 September 2023 | 01 July 2023 | 2000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 01 October 2023   |              | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 4  | 31   | 01 November 2023  |              | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 0    | 0          | 0    | 3000        |

  @TestRailId:C2808
  Scenario: Verify that loan is rescheduled properly in case of: multiple installments, mid-term grace period on interest
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 July 2023      | 4000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 July 2023" with "4000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "4000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 4000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 3000.0          | 1000.0        | 40.0     | 0.0  | 0.0       | 1040.0 | 0.0  | 0.0        | 0.0  | 1040.0      |
      | 2  | 31   | 01 September 2023 |           | 2000.0          | 1000.0        | 40.0     | 0.0  | 0.0       | 1040.0 | 0.0  | 0.0        | 0.0  | 1040.0      |
      | 3  | 30   | 01 October 2023   |           | 1000.0          | 1000.0        | 40.0     | 0.0  | 0.0       | 1040.0 | 0.0  | 0.0        | 0.0  | 1040.0      |
      | 4  | 31   | 01 November 2023  |           | 0.0             | 1000.0        | 40.0     | 0.0  | 0.0       | 1040.0 | 0.0  | 0.0        | 0.0  | 1040.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 4000          | 160      | 0    | 0         | 4160 | 0    | 0          | 0    | 4160        |
    When Admin sets the business date to "05 July 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 September 2023  | 05 July 2023    |                 |                  | 2               |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 4000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 3000.0          | 1000.0        | 40.0     | 0.0  | 0.0       | 1040.0 | 0.0  | 0.0        | 0.0  | 1040.0      |
      | 2  | 31   | 01 September 2023 |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 3  | 30   | 01 October 2023   |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 4  | 31   | 01 November 2023  |           | 0.0             | 1000.0        | 120.0    | 0.0  | 0.0       | 1120.0 | 0.0  | 0.0        | 0.0  | 1120.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 4000          | 160      | 0    | 0         | 4160 | 0    | 0          | 0    | 4160        |

  @TestRailId:C2809
  Scenario: Verify that loan is rescheduled properly in case of: multiple installments, new interest rate
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 July 2023      | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 July 2023" with "3000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "3000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 2000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 2  | 31   | 01 September 2023 |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 30   | 01 October 2023   |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 0    | 0         | 3090 | 0    | 0          | 0    | 3090        |
    When Admin sets the business date to "05 July 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 September 2023  | 05 July 2023    |                 |                  |                 |            | 6               |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2023      |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2023    |           | 2000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 2  | 31   | 01 September 2023 |           | 1000.0          | 1000.0        | 15.0     | 0.0  | 0.0       | 1015.0 | 0.0  | 0.0        | 0.0  | 1015.0      |
      | 3  | 30   | 01 October 2023   |           | 0.0             | 1000.0        | 15.0     | 0.0  | 0.0       | 1015.0 | 0.0  | 0.0        | 0.0  | 1015.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 60       | 0    | 0         | 3060 | 0    | 0          | 0    | 3060        |
    Then LoanRescheduledDueAdjustScheduleBusinessEvent is raised on "05 July 2023"

  @TestRailId:C2996
  Scenario: Verify that reschedule: add extra terms working properly with auto downpayment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 01 October 2023 |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 600.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 3  | 15   | 31 October 2023  |                 | 450.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 4  | 15   | 15 November 2023 |                 | 300.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 5  | 15   | 30 November 2023 |                 | 150.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 6  | 15   | 15 December 2023 |                 | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C2997
  Scenario: Verify that reschedule: add extra terms working properly with auto downpayment and 2nd disbursement before reschedule
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    When Admin successfully disburse the loan on "10 October 2023" with "400" EUR transaction amount
    When Admin sets the business date to "11 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 01 October 2023 |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 10 October 2023  |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 10 October 2023  | 10 October 2023 | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 16 October 2023  |                 | 840.0           | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0  | 210.0       |
      | 4  | 15   | 31 October 2023  |                 | 630.0           | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0  | 210.0       |
      | 5  | 15   | 15 November 2023 |                 | 420.0           | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0  | 210.0       |
      | 6  | 15   | 30 November 2023 |                 | 210.0           | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0  | 210.0       |
      | 7  | 15   | 15 December 2023 |                 | 0.0             | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0  | 210.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0.0      | 0.0  | 0.0       | 1400.0 | 350.0 | 0.0        | 0.0  | 1050.0      |

  @TestRailId:C2998
  Scenario: Verify that reschedule: add extra terms working properly with auto downpayment and 2nd disbursement after reschedule and first installment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "11 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 01 October 2023 |                 |                  |                 | 2          |                 |
    When Admin sets the business date to "20 October 2023"
    When Admin successfully disburse the loan on "20 October 2023" with "400" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 540.0           | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 100.0 | 0.0        | 100.0 | 110.0       |
      |    |      | 20 October 2023  |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 3  | 0    | 20 October 2023  |                 | 840.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0   | 100.0       |
      | 4  | 15   | 31 October 2023  |                 | 630.0           | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0   | 210.0       |
      | 5  | 15   | 15 November 2023 |                 | 420.0           | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0   | 210.0       |
      | 6  | 15   | 30 November 2023 |                 | 210.0           | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0   | 210.0       |
      | 7  | 15   | 15 December 2023 |                 | 0.0             | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 0.0   | 0.0        | 0.0   | 210.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1400.0        | 0.0      | 0.0  | 0.0       | 1400.0 | 350.0 | 0.0        | 100.0 | 1050.0      |

  @TestRailId:C3022
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) works properly in case of loan schedule adjusted by 1 installment period after successful downpayment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "05 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 05 October 2023 | 31 October 2023 |                  |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 31 October 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 15 November 2023 |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 30 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3023
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) works properly in case of loan schedule adjusted by 2 installment period after successful downpayment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "05 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate  | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 05 October 2023 | 15 November 2023 |                  |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 45   | 15 November 2023 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 30 November 2023 |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 December 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3024
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) works properly in case of loan schedule adjusted by 2 installment period while downpayment is still due
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "05 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate  | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 05 October 2023 | 15 November 2023 |                  |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 45   | 15 November 2023 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 30 November 2023 |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 December 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |

  @TestRailId:C3025
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) works properly in case of loan schedule adjusted by 1 installment period after downpayment was reverted, fee added and downpayment paid by autopayment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "02 October 2023"
    When Customer undo "1"th repayment on "01 October 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "16 October 2023" due date and 15 EUR transaction amount
    When Admin sets the business date to "03 October 2023"
    And Customer makes "AUTOPAY" repayment on "03 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "05 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 05 October 2023 | 31 October 2023 |                  |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 October 2023  | 03 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 30   | 31 October 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 15.0      | 265.0 | 0.0   | 0.0        | 0.0   | 265.0       |
      | 3  | 15   | 15 November 2023 |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 15   | 30 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 15.0      | 1015.0 | 250.0 | 0.0        | 250.0 | 765.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | true     |
      | 03 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |

  @TestRailId:C3026
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) works properly in case of auto downpayment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 01 October 2023 | 31 October 2023 |                  |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 31 October 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 15 November 2023 |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 30 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3027
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) works properly in case of auto downpayment and 2nd disbursement before reschedule
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    When Admin successfully disburse the loan on "10 October 2023" with "400" EUR transaction amount
    When Admin sets the business date to "11 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 01 October 2023 | 31 October 2023 |                  |                 |            |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 10 October 2023  |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 10 October 2023  | 10 October 2023 | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 31 October 2023  |                 | 700.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 4  | 15   | 15 November 2023 |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 15   | 30 November 2023 |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0.0      | 0.0  | 0.0       | 1400.0 | 350.0 | 0.0        | 0.0  | 1050.0      |

  @TestRailId:C3028
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) works properly in case of loan schedule adjusted by 1 installment period and 2 extra terms
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "05 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 05 October 2023 | 31 October 2023 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 31 October 2023  |                 | 600.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 3  | 15   | 15 November 2023 |                 | 450.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 4  | 15   | 30 November 2023 |                 | 300.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 5  | 15   | 15 December 2023 |                 | 150.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 6  | 15   | 30 December 2023 |                 | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3029
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) works properly in case of loan schedule adjusted by 1 installment period and 1 grace on principal
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "05 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 05 October 2023 | 31 October 2023 | 1                |                 |            |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 31 October 2023  | 01 October 2023 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 15 November 2023 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 30 November 2023 |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 15   | 15 December 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3030
  Scenario: Verify that Payment Holiday (Reschedule with adjustedDueDate) gives error in case of charged-off loan
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "03 October 2023"
    And Admin does charge-off the loan on "03 October 2023"
    When Admin sets the business date to "05 October 2023"
    Then Loan reschedule with the following data results a 403 error and "LOAN_CHARGED_OFF" error message
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 05 October 2023 | 31 October 2023 | 0                | 0               | 0          | 0               |

  @TestRailId:C3033
  Scenario: Verify that reschedule keeps the N+1 installment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "17 December 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 5  | 32   | 17 December 2023 |           | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
    When Admin sets the business date to "05 October 2023"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 October 2023    | 05 October 2023 | 31 October 2023 |                  |                 |            |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 31 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 15 November 2023 |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 30 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 5  | 17   | 17 December 2023 |           | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |

  @TestRailId:C3045 @AdvancedPaymentAllocation
  Scenario: Verify that inline COB execution is taking place in case of a BatchAPI request of Loan reschedule creation and approval
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | APPROVE_RESCHEDULELOAN |
      | CREATE_RESCHEDULELOAN  |
      | READ_RESCHEDULELOAN    |
      | REJECT_RESCHEDULELOAN  |
    When Admin sets the business date to "10 January 2024"
    When Batch API call with created user and with steps: rescheduleLoan from "16 January 2024" to "31 January 2024" submitted on date: "10 January 2024", approveReschedule on date: "10 January 2024" runs with enclosingTransaction: "true"
    Then Admin checks that last closed business date of loan is "09 January 2024"

  @TestRailId:C3048 @AdvancedPaymentAllocation
  Scenario: Verify that in case of Loan is hard locked for COB execution WITH error message, BatchAPI request of Loan reschedule creation and approval can be done
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "10 January 2024"
    When Admin places a lock on loan account with an error message
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | APPROVE_RESCHEDULELOAN |
      | CREATE_RESCHEDULELOAN  |
      | READ_RESCHEDULELOAN    |
      | REJECT_RESCHEDULELOAN  |
    When Batch API call with created user and with steps: rescheduleLoan from "16 January 2024" to "31 January 2024" submitted on date: "10 January 2024", approveReschedule on date: "10 January 2024" runs with enclosingTransaction: "true"

  @TestRailId:C3049 @AdvancedPaymentAllocation
  Scenario: Verify that in case of Loan is hard locked for COB execution with error message, BatchAPI request of Loan reschedule creation and approval will result a 200 statuscode without error message
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "10 January 2024"
    When Admin places a lock on loan account with an error message
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | APPROVE_RESCHEDULELOAN |
      | CREATE_RESCHEDULELOAN  |
      | READ_RESCHEDULELOAN    |
      | REJECT_RESCHEDULELOAN  |
    When Batch API call with created user and the following data results a 200 statuscode WITHOUT error message:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | approvedOnDate  | enclosingTransaction |
      | 16 January 2024    | 10 January 2024 | 31 January 2024 | 10 January 2024 | true                 |

  @TestRailId:C3318 @AdvancedPaymentAllocation
  Scenario: Verify that in case of Loan is hard locked for COB execution WITHOUT error message, BatchAPI request of Loan reschedule creation and approval will result a 409 error and a LOAN_LOCKED_BY_COB error message
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "10 January 2024"
    When Admin places a lock on loan account WITHOUT an error message
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | APPROVE_RESCHEDULELOAN |
      | CREATE_RESCHEDULELOAN  |
      | READ_RESCHEDULELOAN    |
      | REJECT_RESCHEDULELOAN  |
    When Batch API call with created user and the following data results a 409 error and a "LOAN_LOCKED_BY_COB" error message:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | approvedOnDate  | enclosingTransaction |
      | 16 January 2024    | 10 January 2024 | 31 January 2024 | 10 January 2024 | true                 |

  @TestRailId:C3388
  Scenario: Verify that interest recalculation working in case one due date reschedule
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2024   | 01 January 2024 | 01 March 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 60   | 01 March 2024   |           | 84.16           | 15.84         | 1.17     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 31   | 01 April 2024   |           | 67.64           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 01 May 2024     |           | 51.02           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 01 June 2024    |           | 34.31           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 01 July 2024    |           | 17.5            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 01 August 2024  |           | 0.0             | 17.5          | 0.1      | 0.0  | 0.0       | 17.6  | 0.0  | 0.0        | 0.0  | 17.6        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.65     | 0.0  | 0.0       | 102.65 | 0.0  | 0.0        | 0.0  | 102.65      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

  @TestRailId:C3389
  Scenario: Verify that interest recalculation working in case two due date reschedules
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2024   | 01 January 2024 | 01 March 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 60   | 01 March 2024   |           | 84.16           | 15.84         | 1.17     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 31   | 01 April 2024   |           | 67.64           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 01 May 2024     |           | 51.02           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 01 June 2024    |           | 34.31           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 01 July 2024    |           | 17.5            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 01 August 2024  |           | 0.0             | 17.5          | 0.1      | 0.0  | 0.0       | 17.6  | 0.0  | 0.0        | 0.0  | 17.6        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.65     | 0.0  | 0.0       | 102.65 | 0.0  | 0.0        | 0.0  | 102.65      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 May 2024        | 15 April 2024   | 01 June 2024    |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 60   | 01 March 2024     |           | 84.16           | 15.84         | 1.17     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 31   | 01 April 2024     |           | 67.64           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 61   | 01 June 2024      |           | 51.42           | 16.22         | 0.79     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 July 2024      |           | 34.71           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 August 2024    |           | 17.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 01 September 2024 |           | 0.0             | 17.9          | 0.1      | 0.0  | 0.0       | 18.0  | 0.0  | 0.0        | 0.0  | 18.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 3.05     | 0.0  | 0.0       | 103.05 | 0.0  | 0.0        | 0.0  | 103.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

  @TestRailId:C3390
  Scenario: Verify that interest recalculation working in case off due date reschedule
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 March 2024      | 15 February 2024 | 15 April 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 74   | 15 April 2024    |           | 67.76           | 15.81         | 1.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 15 May 2024      |           | 51.15           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 15 June 2024     |           | 34.44           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 15 July 2024     |           | 17.63           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 15 August 2024   |           | 0.0             | 17.63         | 0.1      | 0.0  | 0.0       | 17.73 | 0.0  | 0.0        | 0.0  | 17.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0  | 0.0        | 0.0  | 102.78      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

  @TestRailId:C3426
  Scenario: Verify that interest recalculation working in case off due date reschedule, with dates for February
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2024   | 01 January 2024 | 01 March 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 60   | 01 March 2024   |           | 84.16           | 15.84         | 1.17     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 31   | 01 April 2024   |           | 67.64           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 30   | 01 May 2024     |           | 51.02           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 01 June 2024    |           | 34.31           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 01 July 2024    |           | 17.5            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 01 August 2024  |           | 0.0             | 17.5          | 0.1      | 0.0  | 0.0       | 17.6  | 0.0  | 0.0        | 0.0  | 17.6        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.65     | 0.0  | 0.0       | 102.65 | 0.0  | 0.0        | 0.0  | 102.65      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

  @TestRailId:C3882
  Scenario: Reschedule progressive loan to 0 percent interest then back to 10 percent
    When Admin sets the business date to "1 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2024    | 100            | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 29   | 01 March 2024    |           | 67.21           | 16.46         | 0.7      | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 3  | 31   | 01 April 2024    |           | 50.61           | 16.6          | 0.56     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 4  | 30   | 01 May 2024      |           | 33.87           | 16.74         | 0.42     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 5  | 31   | 01 June 2024     |           | 16.99           | 16.88         | 0.28     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.99         | 0.14     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.93     | 0.0  | 0.0       | 102.93 | 0.0  | 0.0        | 0.0  | 102.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 January 2024    | 01 January 2024 |                 |                  |                 |            | 0               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.33           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 2  | 29   | 01 March 2024    |           | 66.66           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 3  | 31   | 01 April 2024    |           | 49.99           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 4  | 30   | 01 May 2024      |           | 33.32           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 5  | 31   | 01 June 2024     |           | 16.65           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.65         | 0.0      | 0.0  | 0.0       | 16.65 | 0.0  | 0.0        | 0.0  | 16.65       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 03 January 2024    | 01 January 2024 |                 |                  |                 |            | 10              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.66           | 16.34         | 0.81     | 0.0  | 0.0       | 17.15 | 0.0  | 0.0        | 0.0  | 17.15       |
      | 2  | 29   | 01 March 2024    |           | 67.21           | 16.45         | 0.7      | 0.0  | 0.0       | 17.15 | 0.0  | 0.0        | 0.0  | 17.15       |
      | 3  | 31   | 01 April 2024    |           | 50.62           | 16.59         | 0.56     | 0.0  | 0.0       | 17.15 | 0.0  | 0.0        | 0.0  | 17.15       |
      | 4  | 30   | 01 May 2024      |           | 33.89           | 16.73         | 0.42     | 0.0  | 0.0       | 17.15 | 0.0  | 0.0        | 0.0  | 17.15       |
      | 5  | 31   | 01 June 2024     |           | 17.02           | 16.87         | 0.28     | 0.0  | 0.0       | 17.15 | 0.0  | 0.0        | 0.0  | 17.15       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 17.02         | 0.14     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.91     | 0.0  | 0.0       | 102.91 | 0.0  | 0.0        | 0.0  | 102.91      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

  @TestRailId:C3883
  Scenario: Create progressive loan with 0 percent interest then to 10 percent after first period
    When Admin sets the business date to "1 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2024    | 100            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.33           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 2  | 29   | 01 March 2024    |           | 66.66           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 3  | 31   | 01 April 2024    |           | 49.99           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 4  | 30   | 01 May 2024      |           | 33.32           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 5  | 31   | 01 June 2024     |           | 16.65           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.65         | 0.0      | 0.0  | 0.0       | 16.65 | 0.0  | 0.0        | 0.0  | 16.65       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 02 February 2024   | 01 January 2024 |                 |                  |                 |            | 10              |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.33           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 2  | 29   | 01 March 2024    |           | 66.94           | 16.39         | 0.69     | 0.0  | 0.0       | 17.08 | 0.0  | 0.0        | 0.0  | 17.08       |
      | 3  | 31   | 01 April 2024    |           | 50.42           | 16.52         | 0.56     | 0.0  | 0.0       | 17.08 | 0.0  | 0.0        | 0.0  | 17.08       |
      | 4  | 30   | 01 May 2024      |           | 33.76           | 16.66         | 0.42     | 0.0  | 0.0       | 17.08 | 0.0  | 0.0        | 0.0  | 17.08       |
      | 5  | 31   | 01 June 2024     |           | 16.96           | 16.8          | 0.28     | 0.0  | 0.0       | 17.08 | 0.0  | 0.0        | 0.0  | 17.08       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.96         | 0.14     | 0.0  | 0.0       | 17.1  | 0.0  | 0.0        | 0.0  | 17.1        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 0.0  | 0.0        | 0.0  | 102.09      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

  @TestRailId:C3885
  Scenario: Verify reschedule progressive loan interest rate to 0 after partial repayments
    When Admin sets the business date to "1 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2024    | 100            | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "1 February 2024" with 17.16 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 17.16 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.21           | 16.46         | 0.7      | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 3  | 31   | 01 April 2024    |                  | 50.61           | 16.6          | 0.56     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 4  | 30   | 01 May 2024      |                  | 33.87           | 16.74         | 0.42     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.88         | 0.28     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.14     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.93     | 0.0  | 0.0       | 102.93 | 17.16 | 0.0        | 0.0  | 85.77       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.33     | 0.83     | 0.0  | 0.0       | 83.67        | false    | false    |
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 8.58 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 17.16 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.21           | 16.46         | 0.7      | 0.0  | 0.0       | 17.16 | 8.58  | 8.58       | 0.0  | 8.58        |
      | 3  | 31   | 01 April 2024    |                  | 50.61           | 16.6          | 0.56     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 4  | 30   | 01 May 2024      |                  | 33.87           | 16.74         | 0.42     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.88         | 0.28     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.14     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.93     | 0.0  | 0.0       | 102.93 | 25.74 | 8.58       | 0.0  | 77.19       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.33     | 0.83     | 0.0  | 0.0       | 83.67        | false    | false    |
      | 15 February 2024 | Repayment        | 8.58   | 8.58      | 0.0      | 0.0  | 0.0       | 75.09        | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 16 February 2024   | 15 February 2024 |                 |                  |                 |            | 0               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 17.16 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.21           | 16.46         | 0.34     | 0.0  | 0.0       | 16.8  | 8.58  | 8.58       | 0.0  | 8.22        |
      | 3  | 31   | 01 April 2024    |                  | 50.41           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0   | 0.0        | 0.0  | 16.8        |
      | 4  | 30   | 01 May 2024      |                  | 33.61           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0   | 0.0        | 0.0  | 16.8        |
      | 5  | 31   | 01 June 2024     |                  | 16.81           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0   | 0.0        | 0.0  | 16.8        |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.81         | 0.0      | 0.0  | 0.0       | 16.81 | 0.0   | 0.0        | 0.0  | 16.81       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 25.74 | 8.58       | 0.0  | 75.43       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.33     | 0.83     | 0.0  | 0.0       | 83.67        | false    | false    |
      | 15 February 2024 | Repayment        | 8.58   | 8.58      | 0.0      | 0.0  | 0.0       | 75.09        | false    | false    |

  @TestRailId:C4126
  Scenario: Verify rescheduling of progressive loan is allowed on the first day of 1st repayment schedule
    When Admin sets the business date to "24 July 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 24 July 2025      | 500            | 35                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "24 July 2025" with "500" amount and expected disbursement date on "24 July 2025"
    When Admin successfully disburse the loan on "24 July 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 24 July 2025      |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 24 August 2025    |           | 422.54          | 77.46         | 14.58    | 0.0  | 0.0       | 92.04 | 0.0  | 0.0        | 0.0  | 92.04       |
      | 2  | 31   | 24 September 2025 |           | 342.82          | 79.72         | 12.32    | 0.0  | 0.0       | 92.04 | 0.0  | 0.0        | 0.0  | 92.04       |
      | 3  | 30   | 24 October 2025   |           | 260.78          | 82.04         | 10.0     | 0.0  | 0.0       | 92.04 | 0.0  | 0.0        | 0.0  | 92.04       |
      | 4  | 31   | 24 November 2025  |           | 176.35          | 84.43         | 7.61     | 0.0  | 0.0       | 92.04 | 0.0  | 0.0        | 0.0  | 92.04       |
      | 5  | 30   | 24 December 2025  |           | 89.45           | 86.9          | 5.14     | 0.0  | 0.0       | 92.04 | 0.0  | 0.0        | 0.0  | 92.04       |
      | 6  | 31   | 24 January 2026   |           | 0.0             | 89.45         | 2.61     | 0.0  | 0.0       | 92.06 | 0.0  | 0.0        | 0.0  | 92.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 52.26    | 0.0  | 0.0       | 552.26 | 0.0  | 0.0        | 0.0  | 552.26      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 24 July 2025     | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 24 July 2025       | 24 July 2025    |                 |                  |                 |            | 5               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 24 July 2025      |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 24 August 2025    |           | 417.53          | 82.47         | 2.08     | 0.0  | 0.0       | 84.55 | 0.0  | 0.0        | 0.0  | 84.55       |
      | 2  | 31   | 24 September 2025 |           | 334.72          | 82.81         | 1.74     | 0.0  | 0.0       | 84.55 | 0.0  | 0.0        | 0.0  | 84.55       |
      | 3  | 30   | 24 October 2025   |           | 251.56          | 83.16         | 1.39     | 0.0  | 0.0       | 84.55 | 0.0  | 0.0        | 0.0  | 84.55       |
      | 4  | 31   | 24 November 2025  |           | 168.06          | 83.5          | 1.05     | 0.0  | 0.0       | 84.55 | 0.0  | 0.0        | 0.0  | 84.55       |
      | 5  | 30   | 24 December 2025  |           | 84.21           | 83.85         | 0.7      | 0.0  | 0.0       | 84.55 | 0.0  | 0.0        | 0.0  | 84.55       |
      | 6  | 31   | 24 January 2026   |           | 0.0             | 84.21         | 0.35     | 0.0  | 0.0       | 84.56 | 0.0  | 0.0        | 0.0  | 84.56       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 7.31     | 0.0  | 0.0       | 507.31 | 0.0  | 0.0        | 0.0  | 507.31      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 24 July 2025     | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    Then LoanRescheduledDueAdjustScheduleBusinessEvent is raised on "24 July 2025"

  @TestRailId:C4225
  Scenario: Verify after backdated repayment, the last Accrual Activity transaction is reversed-replayed only once
    When Admin sets the business date to "05 September 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 31 December 2024  | 1111           | 24.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 December 2024" with "1111" amount and expected disbursement date on "31 December 2024"
    And Admin successfully disburse the loan on "31 December 2024" with "1111" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 January 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "28 February 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 March 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "30 April 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "30 May 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "29 June 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 July 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 August 2025" with 59.26 EUR transaction amount
    When Admin sets the business date to "06 September 2025"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "11 September 2025"
    When Customer undo "1"th "Repayment" transaction made on "31 August 2025"
    When Admin sets the business date to "30 September 2025"
    And Customer makes "AUTOPAY" repayment on "30 September 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "30 September 2025" with 59.26 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "10 October 2025"
    When Customer undo "1"th "Repayment" transaction made on "30 September 2025"
    When Customer undo "2"th "Repayment" transaction made on "30 September 2025"
    When Admin sets the business date to "16 October 2025"
    And Customer makes "AUTOPAY" repayment on "16 October 2025" with 60 EUR transaction amount
    When Admin sets the business date to "31 October 2025"
    And Customer makes "AUTOPAY" repayment on "31 October 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 October 2025" with 58.52 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "10 November 2025"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "29 June 2025" with 60 EUR transaction amount
    Then In Loan Transactions the "1"th Transaction of "Accrual Activity" on "31 October 2025" has "1" relationship with type="REPLAYED"

  @TestRailId:C4391
  Scenario: Verify progressive loan due date reschedule maintains EMI
    When Admin sets the business date to "05 September 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 31 December 2024  | 1111           | 24.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 December 2024" with "1111" amount and expected disbursement date on "31 December 2024"
    And Admin successfully disburse the loan on "31 December 2024" with "1111" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 January 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "28 February 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 March 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "30 April 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "30 May 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "29 June 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 July 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 August 2025" with 59.26 EUR transaction amount
    When Admin sets the business date to "06 September 2025"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "11 September 2025"
    When Customer undo "1"th "Repayment" transaction made on "31 August 2025"
    When Admin sets the business date to "30 September 2025"
    And Customer makes "AUTOPAY" repayment on "30 September 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "30 September 2025" with 59.26 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "10 October 2025"
    When Customer undo "1"th "Repayment" transaction made on "30 September 2025"
    When Customer undo "2"th "Repayment" transaction made on "30 September 2025"
    When Admin sets the business date to "16 October 2025"
    And Customer makes "AUTOPAY" repayment on "16 October 2025" with 60 EUR transaction amount
    When Admin sets the business date to "31 October 2025"
    And Customer makes "AUTOPAY" repayment on "31 October 2025" with 59.26 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "31 October 2025" with 58.52 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "10 November 2025"
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 31 December 2024  |                  | 1111.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 31 January 2025   | 31 January 2025  | 1075.32         | 35.68         | 23.58    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 2  | 28   | 28 February 2025  | 28 February 2025 | 1036.67         | 38.65         | 20.61    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 31 March 2025     | 31 March 2025    | 999.41          | 37.26         | 22.0     | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 4  | 30   | 30 April 2025     | 30 April 2025    | 960.68          | 38.73         | 20.53    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 5  | 31   | 31 May 2025       | 30 May 2025      | 921.81          | 38.87         | 20.39    | 0.0  | 0.0       | 59.26 | 59.26 | 59.26      | 0.0   | 0.0         |
      | 6  | 30   | 30 June 2025      | 29 June 2025     | 881.48          | 40.33         | 18.93    | 0.0  | 0.0       | 59.26 | 59.26 | 59.26      | 0.0   | 0.0         |
      | 7  | 31   | 31 July 2025      | 31 July 2025     | 840.93          | 40.55         | 18.71    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 8  | 31   | 31 August 2025    | 16 October 2025  | 799.52          | 41.41         | 17.85    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 59.26 | 0.0         |
      | 9  | 30   | 30 September 2025 | 31 October 2025  | 756.68          | 42.84         | 16.42    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 59.26 | 0.0         |
      | 10 | 31   | 31 October 2025   | 31 October 2025  | 713.48          | 43.2          | 16.06    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 11 | 30   | 30 November 2025  |                  | 668.87          | 44.61         | 14.65    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 12 | 31   | 31 December 2025  |                  | 623.81          | 45.06         | 14.2     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 13 | 31   | 31 January 2026   |                  | 577.79          | 46.02         | 13.24    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 14 | 28   | 28 February 2026  |                  | 529.61          | 48.18         | 11.08    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 15 | 31   | 31 March 2026     |                  | 481.59          | 48.02         | 11.24    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 16 | 30   | 30 April 2026     |                  | 432.22          | 49.37         | 9.89     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 17 | 31   | 31 May 2026       |                  | 382.13          | 50.09         | 9.17     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 18 | 30   | 30 June 2026      |                  | 330.72          | 51.41         | 7.85     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 19 | 31   | 31 July 2026      |                  | 278.48          | 52.24         | 7.02     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 20 | 31   | 31 August 2026    |                  | 225.13          | 53.35         | 5.91     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 21 | 30   | 30 September 2026 |                  | 170.49          | 54.64         | 4.62     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 22 | 31   | 31 October 2026   |                  | 114.85          | 55.64         | 3.62     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 23 | 30   | 30 November 2026  |                  | 57.95           | 56.9          | 2.36     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 24 | 31   | 31 December 2026  |                  | 0.0             | 57.95         | 1.23     | 0.0  | 0.0       | 59.18 | 0.0   | 0.0        | 0.0   | 59.18       |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 30 November 2025   | 10 November 2025 | 31 January 2026 |                  |                 |            |                 |
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 31 December 2024  |                  | 1111.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 31 January 2025   | 31 January 2025  | 1075.32         | 35.68         | 23.58    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 2  | 28   | 28 February 2025  | 28 February 2025 | 1036.67         | 38.65         | 20.61    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 31 March 2025     | 31 March 2025    | 999.41          | 37.26         | 22.0     | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 4  | 30   | 30 April 2025     | 30 April 2025    | 960.68          | 38.73         | 20.53    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 5  | 31   | 31 May 2025       | 30 May 2025      | 921.81          | 38.87         | 20.39    | 0.0  | 0.0       | 59.26 | 59.26 | 59.26      | 0.0   | 0.0         |
      | 6  | 30   | 30 June 2025      | 29 June 2025     | 881.48          | 40.33         | 18.93    | 0.0  | 0.0       | 59.26 | 59.26 | 59.26      | 0.0   | 0.0         |
      | 7  | 31   | 31 July 2025      | 31 July 2025     | 840.93          | 40.55         | 18.71    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 8  | 31   | 31 August 2025    | 16 October 2025  | 799.52          | 41.41         | 17.85    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 59.26 | 0.0         |
      | 9  | 30   | 30 September 2025 | 31 October 2025  | 756.68          | 42.84         | 16.42    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 59.26 | 0.0         |
      | 10 | 31   | 31 October 2025   | 31 October 2025  | 713.48          | 43.2          | 16.06    | 0.0  | 0.0       | 59.26 | 59.26 | 0.0        | 0.0   | 0.0         |
      | 11 | 92   | 31 January 2026   |                  | 699.16          | 14.32         | 44.94    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 12 | 28   | 28 February 2026  |                  | 653.3           | 45.86         | 13.4     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 13 | 31   | 31 March 2026     |                  | 607.91          | 45.39         | 13.87    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 14 | 30   | 30 April 2026     |                  | 561.14          | 46.77         | 12.49    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 15 | 31   | 31 May 2026       |                  | 513.79          | 47.35         | 11.91    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 16 | 30   | 30 June 2026      |                  | 465.08          | 48.71         | 10.55    | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 17 | 31   | 31 July 2026      |                  | 415.69          | 49.39         | 9.87     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 18 | 31   | 31 August 2026    |                  | 365.25          | 50.44         | 8.82     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 19 | 30   | 30 September 2026 |                  | 313.49          | 51.76         | 7.5      | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 20 | 31   | 31 October 2026   |                  | 260.88          | 52.61         | 6.65     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 21 | 30   | 30 November 2026  |                  | 206.98          | 53.9          | 5.36     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 22 | 31   | 31 December 2026  |                  | 152.11          | 54.87         | 4.39     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 23 | 31   | 31 January 2027   |                  | 96.08           | 56.03         | 3.23     | 0.0  | 0.0       | 59.26 | 0.0   | 0.0        | 0.0   | 59.26       |
      | 24 | 28   | 28 February 2027  |                  | 0.0             | 96.08         | 1.84     | 0.0  | 0.0       | 97.92 | 0.0   | 0.0        | 0.0   | 97.92       |

  @TestRailId:C4392
  Scenario: Verify basic loan rescheduling
    When Admin sets the business date to "1 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2024    | 100            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
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
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "1 February 2024" with 17.13 EUR transaction amount
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
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 1 March 2024       | 1 March 2024    | 15 March 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 43   | 15 March 2024    |                  | 67.49           | 16.17         | 0.96     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 15 April 2024    |                  | 50.89           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 15 May 2024      |                  | 34.16           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 15 June 2024     |                  | 17.3            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 15 July 2024     |                  | 0.0             | 17.3          | 0.14     | 0.0  | 0.0       | 17.44 | 0.0   | 0.0        | 0.0  | 17.44       |

  @TestRailId:C4393
  Scenario: Verify loan rescheduling with N+1 period
    When Admin sets the business date to "1 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2024    | 100            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
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
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "1 February 2024" with 17.13 EUR transaction amount
    When Admin sets the business date to "15 July 2024"
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 July 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 0.0           | 0.0      | 10.0 | 0.0       | 10.0  | 0.0   | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 10.0 | 0.0       | 112.78 | 17.13 | 0.0        | 0.0  | 95.65       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.13  | 16.34     | 0.79     | 0.0  | 0.0       | 83.66        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 1 March 2024       | 1 March 2024    | 30 March 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 17.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 58   | 30 March 2024    |                  | 67.81           | 15.85         | 1.28     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 30 April 2024    |                  | 51.22           | 16.59         | 0.54     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 30 May 2024      |                  | 34.49           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 30 June 2024     |                  | 17.63           | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 30 July 2024     |                  | 0.0             | 17.63         | 0.14     | 10.0 | 0.0       | 27.77 | 0.0   | 0.0        | 0.0  | 27.77       |

  @TestRailId:C4394
  Scenario: Verify loan rescheduling with automatic down payment
    When Admin sets the business date to "1 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 1 January 2024    | 100            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 5                 | MONTHS                | 1              | MONTHS                 | 5                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
      | 3  | 29   | 01 March 2024    |                 | 45.35           | 14.88         | 0.48     | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
      | 4  | 31   | 01 April 2024    |                 | 30.35           | 15.0          | 0.36     | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
      | 5  | 30   | 01 May 2024      |                 | 15.23           | 15.12         | 0.24     | 0.0  | 0.0       | 15.36 | 0.0  | 0.0        | 0.0  | 15.36       |
      | 6  | 31   | 01 June 2024     |                 | 0.0             | 15.23         | 0.12     | 0.0  | 0.0       | 15.35 | 0.0  | 0.0        | 0.0  | 15.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.79     | 0.0  | 0.0       | 101.79 | 25.0 | 0.0        | 0.0  | 76.79       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
    And Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "1 February 2024" with 15.36 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36 | 15.36 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    |                  | 45.35           | 14.88         | 0.48     | 0.0  | 0.0       | 15.36 | 0.0   | 0.0        | 0.0  | 15.36       |
      | 4  | 31   | 01 April 2024    |                  | 30.35           | 15.0          | 0.36     | 0.0  | 0.0       | 15.36 | 0.0   | 0.0        | 0.0  | 15.36       |
      | 5  | 30   | 01 May 2024      |                  | 15.23           | 15.12         | 0.24     | 0.0  | 0.0       | 15.36 | 0.0   | 0.0        | 0.0  | 15.36       |
      | 6  | 31   | 01 June 2024     |                  | 0.0             | 15.23         | 0.12     | 0.0  | 0.0       | 15.35 | 0.0   | 0.0        | 0.0  | 15.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.79     | 0.0  | 0.0       | 101.79 | 40.36 | 0.0        | 0.0  | 61.43       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 15.36  | 14.77     | 0.59     | 0.0  | 0.0       | 60.23        | false    | false    |
    When Admin sets the business date to "1 March 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 1 March 2024       | 1 March 2024    | 15 March 2024   |                  |                 |            |                 |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 60.23           | 14.77         | 0.59     | 0.0  | 0.0       | 15.36 | 15.36 | 0.0        | 0.0  | 0.0         |
      | 3  | 43   | 15 March 2024    |                  | 45.56           | 14.67         | 0.69     | 0.0  | 0.0       | 15.36 | 0.0   | 0.0        | 0.0  | 15.36       |
      | 4  | 31   | 15 April 2024    |                  | 30.56           | 15.0          | 0.36     | 0.0  | 0.0       | 15.36 | 0.0   | 0.0        | 0.0  | 15.36       |
      | 5  | 30   | 15 May 2024      |                  | 15.44           | 15.12         | 0.24     | 0.0  | 0.0       | 15.36 | 0.0   | 0.0        | 0.0  | 15.36       |
      | 6  | 31   | 15 June 2024     |                  | 0.0             | 15.44         | 0.12     | 0.0  | 0.0       | 15.56 | 0.0   | 0.0        | 0.0  | 15.56       |

  @TestRailId:C4395
  Scenario: Verify Progressive Loan reschedule with non-existing installment due date results in validation error
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
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
    When Admin sets the business date to "02 April 2024"
    Then Loan reschedule with the following data results a 400 error and "LOAN_RESCHEDULE_FROM_DATE_INSTALLMENT_NOT_FOUND" error message
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 August 2024     | 02 April 2024   |                 |                  |                 | 2          |                 |

  @TestRailId:C4574
  Scenario: Verify multiple interest rate changes on same day, UC1: on due date
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2025    | 100            | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "100" amount and expected disbursement date on "1 January 2025"
    And Admin successfully disburse the loan on "1 January 2025" with "100" EUR transaction amount
    And Admin sets the business date to "1 February 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 28   | 01 March 2025    |           | 67.21           | 16.46         | 0.7      | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 3  | 31   | 01 April 2025    |           | 50.61           | 16.6          | 0.56     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 4  | 30   | 01 May 2025      |           | 33.87           | 16.74         | 0.42     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 5  | 31   | 01 June 2025     |           | 16.99           | 16.88         | 0.28     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.99         | 0.14     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.93     | 0.0  | 0.0       | 102.93 | 0.0  | 0.0        | 0.0  | 102.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- first interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2025   | 01 February 2025 |                 |                  |                 |            | 5               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 83.84           | 16.16         | 0.82     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 2  | 28   | 01 March 2025    |           | 67.21           | 16.63         | 0.35     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 3  | 31   | 01 April 2025    |           | 50.51           | 16.7          | 0.28     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 4  | 30   | 01 May 2025      |           | 33.74           | 16.77         | 0.21     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 5  | 31   | 01 June 2025     |           | 16.9            | 16.84         | 0.14     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.9          | 0.07     | 0.0  | 0.0       | 16.97 | 0.0  | 0.0        | 0.0  | 16.97       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.87     | 0.0  | 0.0       | 101.87 | 0.0  | 0.0        | 0.0  | 101.87      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Reschedule tab has the following data:
      | From Date        | Reason | Status   |
      | 01 February 2025 | Test   | Approved |
  #    --- second interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2025   | 01 February 2025 |                 |                  |                 |            | 0               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 84.01           | 15.99         | 0.81     | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 2  | 28   | 01 March 2025    |           | 67.21           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 3  | 31   | 01 April 2025    |           | 50.41           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 4  | 30   | 01 May 2025      |           | 33.61           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 5  | 31   | 01 June 2025     |           | 16.81           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.81         | 0.0      | 0.0  | 0.0       | 16.81 | 0.0  | 0.0        | 0.0  | 16.81       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.81     | 0.0  | 0.0       | 100.81 | 0.0  | 0.0        | 0.0  | 100.81      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Reschedule tab has the following data:
      | From Date        | Reason | Status   |
      | 01 February 2025 | Test   | Approved |
      | 01 February 2025 | Test   | Approved |

  @TestRailId:C4575
  Scenario: Verify multiple interest rate changes on same day, UC2: on due date after repayment
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2025    | 100            | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "100" amount and expected disbursement date on "1 January 2025"
    And Admin successfully disburse the loan on "1 January 2025" with "100" EUR transaction amount
    And Admin sets the business date to "1 February 2025"
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 17.16 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 17.16 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 67.21           | 16.46         | 0.7      | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 3  | 31   | 01 April 2025    |                  | 50.61           | 16.6          | 0.56     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 4  | 30   | 01 May 2025      |                  | 33.87           | 16.74         | 0.42     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 5  | 31   | 01 June 2025     |                  | 16.99           | 16.88         | 0.28     | 0.0  | 0.0       | 17.16 | 0.0   | 0.0        | 0.0  | 17.16       |
      | 6  | 30   | 01 July 2025     |                  | 0.0             | 16.99         | 0.14     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.93     | 0.0  | 0.0       | 102.93 | 17.16 | 0.0        | 0.0  | 85.77       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2025 | Repayment        | 17.16  | 16.33     | 0.83     | 0.0  | 0.0       | 83.67        | false    | false    |
#    --- first interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2025   | 01 February 2025 |                 |                  |                 |            | 5               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 83.84           | 16.16         | 0.82     | 0.0  | 0.0       | 16.98 | 16.98 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 67.21           | 16.63         | 0.35     | 0.0  | 0.0       | 16.98 | 0.18  | 0.18       | 0.0  | 16.8        |
      | 3  | 31   | 01 April 2025    |                  | 50.51           | 16.7          | 0.28     | 0.0  | 0.0       | 16.98 | 0.0   | 0.0        | 0.0  | 16.98       |
      | 4  | 30   | 01 May 2025      |                  | 33.74           | 16.77         | 0.21     | 0.0  | 0.0       | 16.98 | 0.0   | 0.0        | 0.0  | 16.98       |
      | 5  | 31   | 01 June 2025     |                  | 16.9            | 16.84         | 0.14     | 0.0  | 0.0       | 16.98 | 0.0   | 0.0        | 0.0  | 16.98       |
      | 6  | 30   | 01 July 2025     |                  | 0.0             | 16.9          | 0.07     | 0.0  | 0.0       | 16.97 | 0.0   | 0.0        | 0.0  | 16.97       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.87     | 0.0  | 0.0       | 101.87 | 17.16 | 0.18       | 0.0  | 84.71       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2025 | Repayment        | 17.16  | 16.34     | 0.82     | 0.0  | 0.0       | 83.66        | false    | true     |
    And Loan Reschedule tab has the following data:
      | From Date        | Reason | Status   |
      | 01 February 2025 | Test   | Approved |
  #    --- second interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2025   | 01 February 2025 |                 |                  |                 |            | 0               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 84.01           | 15.99         | 0.81     | 0.0  | 0.0       | 16.8  | 16.8 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 67.21           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.36 | 0.36       | 0.0  | 16.44       |
      | 3  | 31   | 01 April 2025    |                  | 50.41           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 4  | 30   | 01 May 2025      |                  | 33.61           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 5  | 31   | 01 June 2025     |                  | 16.81           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 6  | 30   | 01 July 2025     |                  | 0.0             | 16.81         | 0.0      | 0.0  | 0.0       | 16.81 | 0.0  | 0.0        | 0.0  | 16.81       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.81     | 0.0  | 0.0       | 100.81 | 17.16 | 0.36       | 0.0  | 83.65       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2025 | Repayment        | 17.16  | 16.35     | 0.81     | 0.0  | 0.0       | 83.65        | false    | true     |
    And Loan Reschedule tab has the following data:
      | From Date        | Reason | Status   |
      | 01 February 2025 | Test   | Approved |
      | 01 February 2025 | Test   | Approved |

  @TestRailId:C4576
  Scenario: Verify multiple interest rate changes on same day, UC3: on midterm date
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2025    | 100            | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "100" amount and expected disbursement date on "1 January 2025"
    And Admin successfully disburse the loan on "1 January 2025" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 28   | 01 March 2025    |           | 67.21           | 16.46         | 0.7      | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 3  | 31   | 01 April 2025    |           | 50.61           | 16.6          | 0.56     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 4  | 30   | 01 May 2025      |           | 33.87           | 16.74         | 0.42     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 5  | 31   | 01 June 2025     |           | 16.99           | 16.88         | 0.28     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.99         | 0.14     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.93     | 0.0  | 0.0       | 102.93 | 0.0  | 0.0        | 0.0  | 102.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- first interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 15 January 2025    | 15 January 2025 |                 |                  |                 |            | 5               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 83.65           | 16.35         | 0.59     | 0.0  | 0.0       | 16.94 | 0.0  | 0.0        | 0.0  | 16.94       |
      | 2  | 28   | 01 March 2025    |           | 67.06           | 16.59         | 0.35     | 0.0  | 0.0       | 16.94 | 0.0  | 0.0        | 0.0  | 16.94       |
      | 3  | 31   | 01 April 2025    |           | 50.4            | 16.66         | 0.28     | 0.0  | 0.0       | 16.94 | 0.0  | 0.0        | 0.0  | 16.94       |
      | 4  | 30   | 01 May 2025      |           | 33.67           | 16.73         | 0.21     | 0.0  | 0.0       | 16.94 | 0.0  | 0.0        | 0.0  | 16.94       |
      | 5  | 31   | 01 June 2025     |           | 16.87           | 16.8          | 0.14     | 0.0  | 0.0       | 16.94 | 0.0  | 0.0        | 0.0  | 16.94       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.87         | 0.07     | 0.0  | 0.0       | 16.94 | 0.0  | 0.0        | 0.0  | 16.94       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.64     | 0.0  | 0.0       | 101.64 | 0.0  | 0.0        | 0.0  | 101.64      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Reschedule tab has the following data:
      | From Date       | Reason | Status   |
      | 15 January 2025 | Test   | Approved |
  #    --- second interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 15 January 2025    | 15 January 2025 |                 |                  |                 |            | 0               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 83.63           | 16.37         | 0.35     | 0.0  | 0.0       | 16.72 | 0.0  | 0.0        | 0.0  | 16.72       |
      | 2  | 28   | 01 March 2025    |           | 66.91           | 16.72         | 0.0      | 0.0  | 0.0       | 16.72 | 0.0  | 0.0        | 0.0  | 16.72       |
      | 3  | 31   | 01 April 2025    |           | 50.19           | 16.72         | 0.0      | 0.0  | 0.0       | 16.72 | 0.0  | 0.0        | 0.0  | 16.72       |
      | 4  | 30   | 01 May 2025      |           | 33.47           | 16.72         | 0.0      | 0.0  | 0.0       | 16.72 | 0.0  | 0.0        | 0.0  | 16.72       |
      | 5  | 31   | 01 June 2025     |           | 16.75           | 16.72         | 0.0      | 0.0  | 0.0       | 16.72 | 0.0  | 0.0        | 0.0  | 16.72       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.75         | 0.0      | 0.0  | 0.0       | 16.75 | 0.0  | 0.0        | 0.0  | 16.75       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.35     | 0.0  | 0.0       | 100.35 | 0.0  | 0.0        | 0.0  | 100.35      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Reschedule tab has the following data:
      | From Date       | Reason | Status   |
      | 15 January 2025 | Test   | Approved |
      | 15 January 2025 | Test   | Approved |

  @TestRailId:C4577
  Scenario: Verify multiple interest rate changes on same day, UC4: on due date with third change
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2025    | 100            | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "100" amount and expected disbursement date on "1 January 2025"
    And Admin successfully disburse the loan on "1 January 2025" with "100" EUR transaction amount
    And Admin sets the business date to "1 February 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 28   | 01 March 2025    |           | 67.21           | 16.46         | 0.7      | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 3  | 31   | 01 April 2025    |           | 50.61           | 16.6          | 0.56     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 4  | 30   | 01 May 2025      |           | 33.87           | 16.74         | 0.42     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 5  | 31   | 01 June 2025     |           | 16.99           | 16.88         | 0.28     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.99         | 0.14     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.93     | 0.0  | 0.0       | 102.93 | 0.0  | 0.0        | 0.0  | 102.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
#    --- first interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2025   | 01 February 2025 |                 |                  |                 |            | 5               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 83.84           | 16.16         | 0.82     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 2  | 28   | 01 March 2025    |           | 67.21           | 16.63         | 0.35     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 3  | 31   | 01 April 2025    |           | 50.51           | 16.7          | 0.28     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 4  | 30   | 01 May 2025      |           | 33.74           | 16.77         | 0.21     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 5  | 31   | 01 June 2025     |           | 16.9            | 16.84         | 0.14     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.9          | 0.07     | 0.0  | 0.0       | 16.97 | 0.0  | 0.0        | 0.0  | 16.97       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.87     | 0.0  | 0.0       | 101.87 | 0.0  | 0.0        | 0.0  | 101.87      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Reschedule tab has the following data:
      | From Date        | Reason | Status   |
      | 01 February 2025 | Test   | Approved |
#    --- second interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2025   | 01 February 2025 |                 |                  |                 |            | 0               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 84.01           | 15.99         | 0.81     | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 2  | 28   | 01 March 2025    |           | 67.21           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 3  | 31   | 01 April 2025    |           | 50.41           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 4  | 30   | 01 May 2025      |           | 33.61           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 5  | 31   | 01 June 2025     |           | 16.81           | 16.8          | 0.0      | 0.0  | 0.0       | 16.8  | 0.0  | 0.0        | 0.0  | 16.8        |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.81         | 0.0      | 0.0  | 0.0       | 16.81 | 0.0  | 0.0        | 0.0  | 16.81       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.81     | 0.0  | 0.0       | 100.81 | 0.0  | 0.0        | 0.0  | 100.81      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Reschedule tab has the following data:
      | From Date        | Reason | Status   |
      | 01 February 2025 | Test   | Approved |
      | 01 February 2025 | Test   | Approved |
#    --- third interest rate change ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate  | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2025   | 01 February 2025 |                 |                  |                 |            | 5               |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 83.84           | 16.16         | 0.82     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 2  | 28   | 01 March 2025    |           | 67.21           | 16.63         | 0.35     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 3  | 31   | 01 April 2025    |           | 50.51           | 16.7          | 0.28     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 4  | 30   | 01 May 2025      |           | 33.74           | 16.77         | 0.21     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 5  | 31   | 01 June 2025     |           | 16.9            | 16.84         | 0.14     | 0.0  | 0.0       | 16.98 | 0.0  | 0.0        | 0.0  | 16.98       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 16.9          | 0.07     | 0.0  | 0.0       | 16.97 | 0.0  | 0.0        | 0.0  | 16.97       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.87     | 0.0  | 0.0       | 101.87 | 0.0  | 0.0        | 0.0  | 101.87      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    And Loan Reschedule tab has the following data:
      | From Date        | Reason | Status   |
      | 01 February 2025 | Test   | Approved |
      | 01 February 2025 | Test   | Approved |
      | 01 February 2025 | Test   | Approved |

  @TestRailId:C78849
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for downpayment product
    When Admin sets the business date to "21 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 21 January 2026   | 480            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 January 2026" with "480" amount and expected disbursement date on "21 January 2026"
    When Admin successfully disburse the loan on "21 January 2026" with "480" EUR transaction amount
    Then Loan has 360.0 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026  |                 | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026  | 21 January 2026 | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 February 2026 |                 | 240.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 0.0   | 0.0        | 0.0  | 120.0       |
      | 3  | 28   | 21 March 2026    |                 | 120.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 0.0   | 0.0        | 0.0  | 120.0       |
      | 4  | 31   | 21 April 2026    |                 | 0.0             | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 0.0   | 0.0        | 0.0  | 120.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 120.0 | 0.0        | 0.0  | 360.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 480.0  | 0.0       | 0.0      | 0.0  | 0.0       | 480.0        | false    |
      | 21 January 2026  | Down Payment     | 120.0  | 120.0     | 0.0      | 0.0  | 0.0       | 360.0        | false    |
    # --- Step 1: Reschedule (1 month payment holiday: push 21 Feb installment to 21 Mar) ---
    When Admin sets the business date to "23 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 21 February 2026   | 23 January 2026 | 21 March 2026   |                  |                 |            |                 |
    Then Loan has 360.0 outstanding amount
    And Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026 |                 | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026 | 21 January 2026 | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 59   | 21 March 2026   |                 | 240.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 0.0   | 0.0        | 0.0  | 120.0       |
      | 3  | 31   | 21 April 2026   |                 | 120.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 0.0   | 0.0        | 0.0  | 120.0       |
      | 4  | 30   | 21 May 2026     |                 | 0.0             | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 0.0   | 0.0        | 0.0  | 120.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 120.0 | 0.0        | 0.0  | 360.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 480.0  | 0.0       | 0.0      | 0.0  | 0.0       | 480.0        | false    |
      | 21 January 2026  | Down Payment     | 120.0  | 120.0     | 0.0      | 0.0  | 0.0       | 360.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 21 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 21 March 2026 | 15                   | DEFAULT               |
    Then Loan has 360.0 outstanding amount
    And Loan Repayment schedule has 17 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026   | 21 January 2026 | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 23 January 2026   | 23 January 2026 | 360.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 57   | 21 March 2026     |                 | 336.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 4  | 31   | 21 April 2026     |                 | 312.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 5  | 30   | 21 May 2026       |                 | 288.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 6  | 31   | 21 June 2026      |                 | 264.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 7  | 30   | 21 July 2026      |                 | 240.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 8  | 31   | 21 August 2026    |                 | 216.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 9  | 31   | 21 September 2026 |                 | 192.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 10 | 30   | 21 October 2026   |                 | 168.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 11 | 31   | 21 November 2026  |                 | 144.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 12 | 30   | 21 December 2026  |                 | 120.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 13 | 31   | 21 January 2027   |                 | 96.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 14 | 31   | 21 February 2027  |                 | 72.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 15 | 28   | 21 March 2027     |                 | 48.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 16 | 31   | 21 April 2027     |                 | 24.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 17 | 30   | 21 May 2027       |                 | 0.0             | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 120.0 | 0.0        | 0.0  | 360.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 480.0  | 0.0       | 0.0      | 0.0  | 0.0       | 480.0        | false    |
      | 21 January 2026  | Down Payment     | 120.0  | 120.0     | 0.0      | 0.0  | 0.0       | 360.0        | false    |
      | 23 January 2026  | Re-age           | 360.0  | 360.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: BUG TRIGGER: Reschedule again (push schedule 2 more months) ---
    When Admin sets the business date to "28 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 21 March 2026      | 28 January 2026 | 21 May 2026     |                  |                 |            |                 |
    # After reschedule 2 the 15 ReAge installments must cascade by 2 months: first reAge installment
    # moves from 21 March 2026 to 21 May 2026 and the last one moves from 21 May 2027 to 21 July 2027.
    # The bug : the second reschedule silently leaves the schedule unchanged - i.e. 21 July 2027
    # does not exist in the schedule.
    Then Loan has 360.0 outstanding amount
    Then Loan Repayment schedule has 17 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026   | 21 January 2026 | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 23 January 2026   | 23 January 2026 | 360.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 118  | 21 May 2026       |                 | 336.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 4  | 31   | 21 June 2026      |                 | 312.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 5  | 30   | 21 July 2026      |                 | 288.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 6  | 31   | 21 August 2026    |                 | 264.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 7  | 31   | 21 September 2026 |                 | 240.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 8  | 30   | 21 October 2026   |                 | 216.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 9  | 31   | 21 November 2026  |                 | 192.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 10 | 30   | 21 December 2026  |                 | 168.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 11 | 31   | 21 January 2027   |                 | 144.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 12 | 31   | 21 February 2027  |                 | 120.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 13 | 28   | 21 March 2027     |                 | 96.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 14 | 31   | 21 April 2027     |                 | 72.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 15 | 30   | 21 May 2027       |                 | 48.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 16 | 31   | 21 June 2027      |                 | 24.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 17 | 30   | 21 July 2027      |                 | 0.0             | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 120.0 | 0.0        | 0.0  | 360.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 480.0  | 0.0       | 0.0      | 0.0  | 0.0       | 480.0        | false    |
      | 21 January 2026  | Down Payment     | 120.0  | 120.0     | 0.0      | 0.0  | 0.0       | 360.0        | false    |
      | 23 January 2026  | Re-age           | 360.0  | 360.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C78850
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC1
    When Admin sets the business date to "21 January 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 21 January 2026   | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 January 2026" with "150" amount and expected disbursement date on "21 January 2026"
    When Admin successfully disburse the loan on "21 January 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 20 February 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule (push 20 Feb installment to 22 Mar) ---
    When Admin sets the business date to "23 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 20 February 2026   | 23 January 2026 | 22 March 2026   |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026 |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 60   | 22 March 2026   |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 22 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 22 March 2026 | 15                   | DEFAULT               |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 58   | 22 March 2026     |                 | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 22 April 2026     |                 | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 30   | 22 May 2026       |                 | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 22 June 2026      |                 | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 22 July 2026      |                 | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 22 August 2026    |                 | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 31   | 22 September 2026 |                 | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 30   | 22 October 2026   |                 | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 22 November 2026  |                 | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 30   | 22 December 2026  |                 | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 22 January 2027   |                 | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 31   | 22 February 2027  |                 | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 28   | 22 March 2027     |                 | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 31   | 22 April 2027     |                 | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 30   | 22 May 2027       |                 | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: BUG TRIGGER: Reschedule again (push schedule 2 more months: 22 Mar -> 22 May) ---
    When Admin sets the business date to "28 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 22 March 2026      | 28 January 2026 | 22 May 2026     |                  |                 |            |                 |
#    --- After 2nd reschedule Repayment schedule should reflect the last reAge data (eg: frequencyType and numberOfInstallments) ---
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 119  | 22 May 2026       |                 | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 22 June 2026      |                 | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 30   | 22 July 2026      |                 | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 22 August 2026    |                 | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 31   | 22 September 2026 |                 | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 30   | 22 October 2026   |                 | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 31   | 22 November 2026  |                 | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 30   | 22 December 2026  |                 | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 22 January 2027   |                 | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 31   | 22 February 2027  |                 | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 28   | 22 March 2027     |                 | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 31   | 22 April 2027     |                 | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 30   | 22 May 2027       |                 | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 31   | 22 June 2027      |                 | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 30   | 22 July 2027      |                 | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
#    --- Extra step: reschedule again after business date change ---
    When Admin sets the business date to "28 April 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 22 May 2026        | 28 April 2026   | 11 June 2026    |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 139  | 11 June 2026      |                 | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 11 July 2026      |                 | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 11 August 2026    |                 | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 11 September 2026 |                 | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 11 October 2026   |                 | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 11 November 2026  |                 | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 11 December 2026  |                 | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 11 January 2027   |                 | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 11 February 2027  |                 | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 11 March 2027     |                 | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 11 April 2027     |                 | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 11 May 2027       |                 | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 11 June 2027      |                 | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 11 July 2027      |                 | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 11 August 2027    |                 | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C85190
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC2: business date change before 2nd reschedule
    When Admin sets the business date to "21 January 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 21 January 2026   | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 January 2026" with "150" amount and expected disbursement date on "21 January 2026"
    When Admin successfully disburse the loan on "21 January 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 20 February 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule (push 20 Feb installment to 22 Mar) ---
    When Admin sets the business date to "23 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 20 February 2026   | 23 January 2026 | 22 March 2026   |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026 |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 60   | 22 March 2026   |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 22 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 22 March 2026 | 15                   | DEFAULT               |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 58   | 22 March 2026     |                 | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 22 April 2026     |                 | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 30   | 22 May 2026       |                 | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 22 June 2026      |                 | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 22 July 2026      |                 | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 22 August 2026    |                 | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 31   | 22 September 2026 |                 | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 30   | 22 October 2026   |                 | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 22 November 2026  |                 | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 30   | 22 December 2026  |                 | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 22 January 2027   |                 | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 31   | 22 February 2027  |                 | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 28   | 22 March 2027     |                 | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 31   | 22 April 2027     |                 | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 30   | 22 May 2027       |                 | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: BUG TRIGGER: Reschedule again (push schedule 2 more months: 22 Mar -> 11 June) ---
    When Admin sets the business date to "28 April 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 22 May 2026        | 28 April 2026   | 11 June 2026    |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 58   | 22 March 2026     |                 | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 22 April 2026     |                 | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 50   | 11 June 2026      |                 | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 30   | 11 July 2026      |                 | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 31   | 11 August 2026    |                 | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 11 September 2026 |                 | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 11 October 2026   |                 | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 11 November 2026  |                 | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 30   | 11 December 2026  |                 | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 31   | 11 January 2027   |                 | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 11 February 2027  |                 | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 28   | 11 March 2027     |                 | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 11 April 2027     |                 | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 11 May 2027       |                 | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 11 June 2027      |                 | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C85191
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC3: 2nd reAge + 3rd reSchedule added
    When Admin sets the business date to "21 January 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 21 January 2026   | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 January 2026" with "150" amount and expected disbursement date on "21 January 2026"
    When Admin successfully disburse the loan on "21 January 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 20 February 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule (push 20 Feb installment to 22 Mar) ---
    When Admin sets the business date to "23 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 20 February 2026   | 23 January 2026 | 22 March 2026   |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026 |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 60   | 22 March 2026   |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 22 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 22 March 2026 | 15                   | DEFAULT               |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 58   | 22 March 2026     |                 | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 22 April 2026     |                 | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 30   | 22 May 2026       |                 | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 22 June 2026      |                 | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 22 July 2026      |                 | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 22 August 2026    |                 | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 31   | 22 September 2026 |                 | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 30   | 22 October 2026   |                 | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 22 November 2026  |                 | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 30   | 22 December 2026  |                 | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 22 January 2027   |                 | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 31   | 22 February 2027  |                 | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 28   | 22 March 2027     |                 | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 31   | 22 April 2027     |                 | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 30   | 22 May 2027       |                 | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: BUG TRIGGER: Reschedule again (push schedule 2 more months: 22 Mar -> 11 June) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 22 March 2026      | 23 January 2026 | 11 June 2026    |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 139  | 11 June 2026      |                 | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 11 July 2026      |                 | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 11 August 2026    |                 | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 11 September 2026 |                 | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 11 October 2026   |                 | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 11 November 2026  |                 | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 11 December 2026  |                 | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 11 January 2027   |                 | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 11 February 2027  |                 | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 11 March 2027     |                 | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 11 April 2027     |                 | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 11 May 2027       |                 | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 11 June 2027      |                 | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 11 July 2027      |                 | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 11 August 2027    |                 | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
#   --- After 2nd reAge + 3rd reSchedule added Repayment Schedule should follow the last reAge data (eg: frequencyType and numberOfInstallments) ---
    When Admin sets the business date to "24 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments | reAgeInterestHandling |
      | 1               | WEEKS         | 11 June 2026 | 10                   | DEFAULT               |
    Then Loan Repayment schedule has 11 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026 |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 3    | 24 January 2026 | 24 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 138  | 11 June 2026    |                 | 135.0           | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 3  | 7    | 18 June 2026    |                 | 120.0           | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 4  | 7    | 25 June 2026    |                 | 105.0           | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 5  | 7    | 02 July 2026    |                 | 90.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 6  | 7    | 09 July 2026    |                 | 75.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 7  | 7    | 16 July 2026    |                 | 60.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 8  | 7    | 23 July 2026    |                 | 45.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 9  | 7    | 30 July 2026    |                 | 30.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 10 | 7    | 06 August 2026  |                 | 15.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 11 | 7    | 13 August 2026  |                 | 0.0             | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 11 June 2026       | 24 January 2026 | 13 June 2026    |                  |                 |            |                 |
    Then Loan Repayment schedule has 11 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026 |                 | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 3    | 24 January 2026 | 24 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 140  | 13 June 2026    |                 | 135.0           | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 3  | 7    | 20 June 2026    |                 | 120.0           | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 4  | 7    | 27 June 2026    |                 | 105.0           | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 5  | 7    | 04 July 2026    |                 | 90.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 6  | 7    | 11 July 2026    |                 | 75.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 7  | 7    | 18 July 2026    |                 | 60.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 8  | 7    | 25 July 2026    |                 | 45.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 9  | 7    | 01 August 2026  |                 | 30.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 10 | 7    | 08 August 2026  |                 | 15.0            | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
      | 11 | 7    | 15 August 2026  |                 | 0.0             | 15.0          | 0.0      | 0.0  | 0.0       | 15.0 | 0.0  | 0.0        | 0.0  | 15.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2026  | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C85192
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for INTEREST bearing progressive loan product
    When Admin sets the business date to "21 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 21 January 2026   | 150            | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 January 2026" with "150" amount and expected disbursement date on "21 January 2026"
    When Admin successfully disburse the loan on "21 January 2026" with "150" EUR transaction amount
    Then Loan has 151.5 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 20 February 2026 |           | 0.0             | 150.0         | 1.5      | 0.0  | 0.0       | 151.5 | 0.0  | 0.0        | 0.0  | 151.5       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 1.5      | 0.0  | 0.0       | 151.5 | 0.0  | 0.0        | 0.0  | 151.5       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule (push 20 Feb installment to 22 Mar) ---
    When Admin sets the business date to "23 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 20 February 2026   | 23 January 2026 | 22 March 2026   |                  |                 |            |                 |
    Then Loan has 153.0 outstanding amount
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026 |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 60   | 22 March 2026   |           | 0.0             | 150.0         | 3.0      | 0.0  | 0.0       | 153.0 | 0.0  | 0.0        | 0.0  | 153.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 3.0      | 0.0  | 0.0       | 153.0 | 0.0  | 0.0        | 0.0  | 153.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 22 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 22 March 2026 | 15                   | DEFAULT               |
    Then Loan has 164.09 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 58   | 22 March 2026     |                 | 142.06          | 7.94          | 3.0      | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 3  | 31   | 22 April 2026     |                 | 132.59          | 9.47          | 1.47     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 4  | 30   | 22 May 2026       |                 | 122.98          | 9.61          | 1.33     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 5  | 31   | 22 June 2026      |                 | 113.31          | 9.67          | 1.27     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 6  | 30   | 22 July 2026      |                 | 103.5           | 9.81          | 1.13     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 7  | 31   | 22 August 2026    |                 | 93.63           | 9.87          | 1.07     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 8  | 31   | 22 September 2026 |                 | 83.66           | 9.97          | 0.97     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 9  | 30   | 22 October 2026   |                 | 73.56           | 10.1          | 0.84     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 10 | 31   | 22 November 2026  |                 | 63.38           | 10.18         | 0.76     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 11 | 30   | 22 December 2026  |                 | 53.07           | 10.31         | 0.63     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 12 | 31   | 22 January 2027   |                 | 42.68           | 10.39         | 0.55     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 13 | 31   | 22 February 2027  |                 | 32.18           | 10.5          | 0.44     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 14 | 28   | 22 March 2027     |                 | 21.54           | 10.64         | 0.3      | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 15 | 31   | 22 April 2027     |                 | 10.82           | 10.72         | 0.22     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 16 | 30   | 22 May 2027       |                 | 0.0             | 10.82         | 0.11     | 0.0  | 0.0       | 10.93 | 0.0  | 0.0        | 0.0  | 10.93       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 14.09    | 0.0  | 0.0       | 164.09 | 0.0  | 0.0        | 0.0  | 164.09      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.1  | 150.0     | 0.1      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: BUG TRIGGER: Reschedule again (push schedule 2 more months: 22 Mar -> 11 June) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 22 March 2026      | 23 January 2026 | 11 June 2026    |                  |                 |            |                 |
    Then Loan has 168.69 outstanding amount
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 2    | 23 January 2026   | 23 January 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 139  | 11 June 2026      |                 | 146.11          | 3.89          | 7.05     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 3  | 30   | 11 July 2026      |                 | 136.63          | 9.48          | 1.46     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 4  | 31   | 11 August 2026    |                 | 127.1           | 9.53          | 1.41     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 5  | 31   | 11 September 2026 |                 | 117.47          | 9.63          | 1.31     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 6  | 30   | 11 October 2026   |                 | 107.7           | 9.77          | 1.17     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 7  | 31   | 11 November 2026  |                 | 97.87           | 9.83          | 1.11     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 8  | 30   | 11 December 2026  |                 | 87.91           | 9.96          | 0.98     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 9  | 31   | 11 January 2027   |                 | 77.88           | 10.03         | 0.91     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 10 | 31   | 11 February 2027  |                 | 67.74           | 10.14         | 0.8      | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 11 | 28   | 11 March 2027     |                 | 57.43           | 10.31         | 0.63     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 12 | 31   | 11 April 2027     |                 | 47.08           | 10.35         | 0.59     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 13 | 30   | 11 May 2027       |                 | 36.61           | 10.47         | 0.47     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 14 | 31   | 11 June 2027      |                 | 26.05           | 10.56         | 0.38     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 15 | 30   | 11 July 2027      |                 | 15.37           | 10.68         | 0.26     | 0.0  | 0.0       | 10.94 | 0.0  | 0.0        | 0.0  | 10.94       |
      | 16 | 31   | 11 August 2027    |                 | 0.0             | 15.37         | 0.16     | 0.0  | 0.0       | 15.53 | 0.0  | 0.0        | 0.0  | 15.53       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 18.69    | 0.0  | 0.0       | 168.69 | 0.0  | 0.0        | 0.0  | 168.69      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 21 January 2026  | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 23 January 2026  | Re-age           | 150.1  | 150.0     | 0.1      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C85269
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC4: 2nd reAge DEFAULT
    When Admin sets the business date to "10 June 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 31 May 2026       | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 May 2026" with "150" amount and expected disbursement date on "31 May 2026"
    When Admin successfully disburse the loan on "31 May 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 June 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule #1 date  : 2026-06-30  (from: 2026-06-30 → adjusted: 2026-09-30) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate   | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 30 June 2026       | 10 June 2026    | 30 September 2026 |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 122  | 30 September 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 22 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 12 June 2026 | 15                   | DEFAULT               |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 12 June 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 12 July 2026      |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 12 August 2026    |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 12 September 2026 |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 12 October 2026   |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 12 November 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 12 December 2026  |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 12 January 2027   |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 12 February 2027  |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 12 March 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 12 April 2027     |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 12 May 2027       |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 12 June 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 12 July 2027      |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 12 August 2027    |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: Reschedule again (push schedule 2 more months: 12 June -> 23 June) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 12 June 2026       | 10 June 2026    | 23 June 2026    |                  |                 |            |                 |
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 13   | 23 June 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 23 July 2026      |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 23 August 2026    |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 23 September 2026 |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 23 October 2026   |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 23 November 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 23 December 2026  |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 23 January 2027   |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 23 February 2027  |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 23 March 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 23 April 2027     |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 23 May 2027       |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 23 June 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 23 July 2027      |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 23 August 2027    |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C85270
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC5: 2nd reAge EQUAL_AMORTIZATION_PAYABLE_INTEREST
    When Admin sets the business date to "10 June 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 31 May 2026       | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 May 2026" with "150" amount and expected disbursement date on "31 May 2026"
    When Admin successfully disburse the loan on "31 May 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 June 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule #1 date  : 2026-06-30  (from: 2026-06-30 → adjusted: 2026-09-30) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate   | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 30 June 2026       | 10 June 2026    | 30 September 2026 |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 122  | 30 September 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 12 June 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 12 June 2026 | 15                   | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 12 June 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 12 July 2026      |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 12 August 2026    |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 12 September 2026 |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 12 October 2026   |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 12 November 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 12 December 2026  |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 12 January 2027   |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 12 February 2027  |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 12 March 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 12 April 2027     |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 12 May 2027       |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 12 June 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 12 July 2027      |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 12 August 2027    |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
# --- Step 3: Reschedule again (push schedule 2 more months: 12 June -> 23 June) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 12 June 2026       | 10 June 2026    | 23 June 2026    |                  |                 |            |                 |
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 13   | 23 June 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 23 July 2026      |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 23 August 2026    |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 23 September 2026 |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 23 October 2026   |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 23 November 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 23 December 2026  |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 23 January 2027   |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 23 February 2027  |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 23 March 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 23 April 2027     |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 23 May 2027       |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 23 June 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 23 July 2027      |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 23 August 2027    |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C85271
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC5: 2nd reAge DEFAULT end of month
    When Admin sets the business date to "10 June 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 31 May 2026       | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 May 2026" with "150" amount and expected disbursement date on "31 May 2026"
    When Admin successfully disburse the loan on "31 May 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 June 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule #1 date  : 2026-06-30  (from: 2026-06-30 → adjusted: 2026-09-30) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate   | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 30 June 2026       | 10 June 2026    | 30 September 2026 |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 122  | 30 September 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 12 July 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 12 July 2026 | 15                   | DEFAULT               |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 32   | 12 July 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 12 August 2026    |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 12 September 2026 |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 30   | 12 October 2026   |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 31   | 12 November 2026  |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 30   | 12 December 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 31   | 12 January 2027   |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 12 February 2027  |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 28   | 12 March 2027     |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 31   | 12 April 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 30   | 12 May 2027       |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 31   | 12 June 2027      |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 30   | 12 July 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 31   | 12 August 2027    |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 12 September 2027 |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: Reschedule again (push schedule 2 more months: 12 July -> 12 December) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate  | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 12 July 2026       | 10 June 2026    | 12 December 2026 |                  |                 |            |                 |
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 185  | 12 December 2026  |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 12 January 2027   |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 12 February 2027  |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 28   | 12 March 2027     |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 31   | 12 April 2027     |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 30   | 12 May 2027       |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 31   | 12 June 2027      |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 30   | 12 July 2027      |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 12 August 2027    |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 31   | 12 September 2027 |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 30   | 12 October 2027   |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 31   | 12 November 2027  |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 30   | 12 December 2027  |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 31   | 12 January 2028   |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 12 February 2028  |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C89778
  Scenario: Verify no duplicate installment periods after advance repayment and due date reschedule
    When Admin sets the business date to "20 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 20 January 2026   | 741.99         | 24.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "20 January 2026" with "741.99" amount and expected disbursement date on "20 January 2026"
    And Admin successfully disburse the loan on "20 January 2026" with "741.99" EUR transaction amount
    Then Loan has 949.82 outstanding amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 20 January 2026   |           | 741.99          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 20 February 2026  |           | 718.16          | 23.83         | 15.75    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 2  | 28   | 20 March 2026     |           | 692.35          | 25.81         | 13.77    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 3  | 31   | 20 April 2026     |           | 667.46          | 24.89         | 14.69    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 4  | 30   | 20 May 2026       |           | 641.59          | 25.87         | 13.71    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 5  | 31   | 20 June 2026      |           | 615.63          | 25.96         | 13.62    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 6  | 30   | 20 July 2026      |           | 588.69          | 26.94         | 12.64    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 7  | 31   | 20 August 2026    |           | 561.6           | 27.09         | 12.49    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 8  | 31   | 20 September 2026 |           | 533.94          | 27.66         | 11.92    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 9  | 30   | 20 October 2026   |           | 505.33          | 28.61         | 10.97    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 10 | 31   | 20 November 2026  |           | 476.48          | 28.85         | 10.73    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 11 | 30   | 20 December 2026  |           | 446.69          | 29.79         | 9.79     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 12 | 31   | 20 January 2027   |           | 416.59          | 30.1          | 9.48     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 13 | 31   | 20 February 2027  |           | 385.85          | 30.74         | 8.84     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 14 | 28   | 20 March 2027     |           | 353.67          | 32.18         | 7.4      | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 15 | 31   | 20 April 2027     |           | 321.6           | 32.07         | 7.51     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 16 | 30   | 20 May 2027       |           | 288.63          | 32.97         | 6.61     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 17 | 31   | 20 June 2027      |           | 255.18          | 33.45         | 6.13     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 18 | 30   | 20 July 2027      |           | 220.84          | 34.34         | 5.24     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 19 | 31   | 20 August 2027    |           | 185.95          | 34.89         | 4.69     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 20 | 31   | 20 September 2027 |           | 150.32          | 35.63         | 3.95     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 21 | 30   | 20 October 2027   |           | 113.83          | 36.49         | 3.09     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 22 | 31   | 20 November 2027  |           | 76.67           | 37.16         | 2.42     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 23 | 30   | 20 December 2027  |           | 38.66           | 38.01         | 1.57     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 24 | 31   | 20 January 2028   |           | 0.0             | 38.66         | 0.82     | 0.0  | 0.0       | 39.48 | 0.0  | 0.0        | 0.0  | 39.48       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 741.99        | 207.83   | 0.0  | 0.0       | 949.82 | 0.0  | 0.0        | 0.0  | 949.82      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 20 January 2026  | Disbursement     | 741.99 | 0.0       | 0.0      | 0.0  | 0.0       | 741.99       | false    |
    And Customer makes "REAL_TIME" repayment on "20 January 2026" with 300 EUR transaction amount
    Then Loan has 597.69 outstanding amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 20 January 2026   |                 | 741.99          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 20 February 2026  | 20 January 2026 | 702.41          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 2  | 28   | 20 March 2026     | 20 January 2026 | 662.83          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 3  | 31   | 20 April 2026     | 20 January 2026 | 623.25          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 4  | 30   | 20 May 2026       | 20 January 2026 | 583.67          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 5  | 31   | 20 June 2026      | 20 January 2026 | 544.09          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 6  | 30   | 20 July 2026      | 20 January 2026 | 504.51          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 7  | 31   | 20 August 2026    | 20 January 2026 | 464.93          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 8  | 31   | 20 September 2026 |                 | 425.35          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 22.94 | 22.94      | 0.0  | 16.64       |
      | 9  | 30   | 20 October 2026   |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 10 | 31   | 20 November 2026  |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 11 | 30   | 20 December 2026  |                 | 406.65          | 18.7          | 20.88    | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 12 | 31   | 20 January 2027   |                 | 375.7           | 30.95         | 8.63     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 13 | 31   | 20 February 2027  |                 | 344.09          | 31.61         | 7.97     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 14 | 28   | 20 March 2027     |                 | 311.11          | 32.98         | 6.6      | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 15 | 31   | 20 April 2027     |                 | 278.13          | 32.98         | 6.6      | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 16 | 30   | 20 May 2027       |                 | 244.26          | 33.87         | 5.71     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 17 | 31   | 20 June 2027      |                 | 209.86          | 34.4          | 5.18     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 18 | 30   | 20 July 2027      |                 | 174.59          | 35.27         | 4.31     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 19 | 31   | 20 August 2027    |                 | 138.72          | 35.87         | 3.71     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 20 | 31   | 20 September 2027 |                 | 102.08          | 36.64         | 2.94     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 21 | 30   | 20 October 2027   |                 | 64.6            | 37.48         | 2.1      | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 22 | 31   | 20 November 2027  |                 | 26.39           | 38.21         | 1.37     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 23 | 30   | 20 December 2027  |                 | 0.0             | 26.39         | 0.54     | 0.0  | 0.0       | 26.93 | 0.0   | 0.0        | 0.0  | 26.93       |
      | 24 | 31   | 20 January 2028   | 20 January 2026 | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 741.99        | 155.7    | 0.0  | 0.0       | 897.69 | 300.0 | 300.0      | 0.0  | 597.69      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 20 January 2026  | Disbursement     | 741.99 | 0.0       | 0.0      | 0.0  | 0.0       | 741.99       | false    |
      | 20 January 2026  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 441.99       | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 20 February 2026   | 20 January 2026 | 20 October 2026 |                  |                 |            |                 |
    Then Loan has 690.03 outstanding amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 20 January 2026   |                 | 741.99          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 273  | 20 October 2026   | 20 January 2026 | 702.41          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 2  | 31   | 20 November 2026  | 20 January 2026 | 662.83          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 3  | 30   | 20 December 2026  | 20 January 2026 | 623.25          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 4  | 31   | 20 January 2027   | 20 January 2026 | 583.67          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 5  | 31   | 20 February 2027  | 20 January 2026 | 544.09          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 6  | 28   | 20 March 2027     | 20 January 2026 | 504.51          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 7  | 31   | 20 April 2027     | 20 January 2026 | 464.93          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 8  | 30   | 20 May 2027       |                 | 425.35          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 22.94 | 22.94      | 0.0  | 16.64       |
      | 9  | 31   | 20 June 2027      |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 10 | 30   | 20 July 2027      |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 11 | 31   | 20 August 2027    |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 12 | 31   | 20 September 2027 |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 13 | 30   | 20 October 2027   |                 | 418.78          | 6.57          | 33.01    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 14 | 31   | 20 November 2027  |                 | 388.09          | 30.69         | 8.89     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 15 | 30   | 20 December 2027  |                 | 356.48          | 31.61         | 7.97     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 16 | 31   | 20 January 2028   |                 | 324.45          | 32.03         | 7.55     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 17 | 31   | 20 February 2028  |                 | 291.74          | 32.71         | 6.87     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 18 | 29   | 20 March 2028     |                 | 257.94          | 33.8          | 5.78     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 19 | 31   | 20 April 2028     |                 | 223.82          | 34.12         | 5.46     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 20 | 30   | 20 May 2028       |                 | 188.82          | 35.0          | 4.58     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 21 | 31   | 20 June 2028      |                 | 153.24          | 35.58         | 4.0      | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 22 | 30   | 20 July 2028      |                 | 116.8           | 36.44         | 3.14     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 23 | 31   | 20 August 2028    |                 | 0.0             | 116.8         | 2.47     | 0.0  | 0.0       | 119.27 | 0.0   | 0.0        | 0.0  | 119.27      |
      | 24 | 31   | 20 September 2028 | 20 January 2026 | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 741.99        | 248.04   | 0.0  | 0.0       | 990.03 | 300.0 | 300.0      | 0.0  | 690.03      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 20 January 2026  | Disbursement     | 741.99 | 0.0       | 0.0      | 0.0  | 0.0       | 741.99       | false    |
      | 20 January 2026  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 441.99       | false    |
    When Admin sets the business date to "20 September 2028"
    When Loan Pay-off is made on "20 September 2028"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89779
  Scenario: Verify reschedule after MIR, CBR and down payment reversal keeps unique due dates, correct balance and delinquency
    When Admin sets the business date to "18 March 2026"
    And Admin creates a client with random data
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "DEFAULT" transaction type to "REAMORTIZATION" future installment allocation rule
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "MERCHANT_ISSUED_REFUND" transaction type to "REAMORTIZATION" future installment allocation rule
    And Admin creates a fully customized loan with auto downpayment 33.33% and with the following data:
      | LoanProduct                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF | 18 March 2026     | 131.7          | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "18 March 2026" with "131.7" amount and expected disbursement date on "18 March 2026"
    And Admin successfully disburse the loan on "18 March 2026" with "131.7" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 18 April 2026 |               | 43.9            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 0.0   | 0.0        | 0.0  | 43.9        |
      | 3  | 30   | 18 May 2026   |               | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 0.0   | 0.0        | 0.0  | 43.9        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement     | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment     | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 43.9 | 0.0        | 0.0  | 87.8        |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "18 March 2026" with 131.7 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 18 April 2026 | 18 March 2026 | 43.9            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 43.9       | 0.0  | 0.0         |
      | 3  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 87.8      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 131.7 | 87.8       | 0.0  | 0.0         |
    Then Loan has 43.9 overpaid amount
    When Admin sets the business date to "19 March 2026"
    And Admin makes Credit Balance Refund transaction on "19 March 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 18 April 2026 | 18 March 2026 | 43.9            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 43.9       | 0.0  | 0.0         |
      | 3  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 87.8      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 131.7 | 87.8       | 0.0  | 0.0         |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    Then Loan has 0 overpaid amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "24 March 2026"
    And Customer undo "1"th "Down Payment" transaction made on "18 March 2026"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 18 April 2026 |               | 43.9            | 87.8          | 0.0      | 0.0  | 0.0       | 87.8 | 43.9 | 43.9       | 0.0  | 43.9        |
      | 3  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 131.7 | 87.8       | 0.0  | 43.9        |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    When Admin sets the business date to "15 April 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 18 April 2026      | 15 April 2026   | 18 May 2026     |                  |                 |            |                 |
    Then Loan has 43.9 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 61   | 18 May 2026   |               | 43.9            | 87.8          | 0.0      | 0.0  | 0.0       | 87.8 | 43.9 | 43.9       | 0.0  | 43.9        |
      | 3  | 31   | 18 June 2026  | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 131.7 | 87.8       | 0.0  | 43.9        |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    When Admin sets the business date to "1 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan has 43.9 outstanding amount
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | RANGE_30       | 43.9             | 21 May 2026    | 18 May 2026 | 41             | 44          |
    When Admin sets the business date to "2 July 2026"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | RANGE_30       | 43.9             | 21 May 2026    | 18 May 2026 | 42             | 45          |
    And Customer makes "AUTOPAY" repayment on "2 July 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 61   | 18 May 2026   | 02 July 2026  | 43.9            | 87.8          | 0.0      | 0.0  | 0.0       | 87.8 | 87.8 | 43.9       | 43.9 | 0.0         |
      | 3  | 31   | 18 June 2026  | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
      | 02 July 2026     | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 175.6 | 87.8       | 43.9 | 0.0         |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C89780
  Scenario: Verify reschedule after MIR, CBR and repayment reversal keeps unique due dates, correct balance and delinquency
    When Admin sets the business date to "18 March 2026"
    And Admin creates a client with random data
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with forced disabled downpayment with the following data:
      | LoanProduct                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF | 18 March 2026     | 131.7          | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "18 March 2026" with "131.7" amount and expected disbursement date on "18 March 2026"
    And Admin successfully disburse the loan on "18 March 2026" with "131.7" EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |           | 131.7           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 18 April 2026 |           | 65.85           | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 0.0  | 0.0        | 0.0  | 65.85       |
      | 2  | 30   | 18 May 2026   |           | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 0.0  | 0.0        | 0.0  | 65.85       |
    And Customer makes "AUTOPAY" repayment on "18 March 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |           | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 18 April 2026 |           | 65.85           | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 43.9  | 43.9       | 0.0  | 21.95       |
      | 2  | 30   | 18 May 2026   |           | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 0.0   | 0.0        | 0.0  | 65.85       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement     | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment        | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 43.9 | 43.9       | 0.0  | 87.8        |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "18 March 2026" with 131.7 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 18 April 2026 | 18 March 2026 | 65.85           | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 65.85 | 65.85      | 0.0  | 0.0         |
      | 2  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 65.85 | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 87.8      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 131.7 | 131.7      | 0.0  | 0.0         |
    Then Loan has 43.9 overpaid amount
    When Admin sets the business date to "19 March 2026"
    And Admin makes Credit Balance Refund transaction on "19 March 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 18 April 2026 | 18 March 2026 | 65.85           | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 65.85 | 65.85      | 0.0  | 0.0         |
      | 2  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 65.85 | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 87.8      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 131.7 | 131.7      | 0.0  | 0.0         |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    Then Loan has 0 overpaid amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "24 March 2026"
    And Customer undo "1"th "Repayment" transaction made on "18 March 2026"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 18 April 2026 |               | 65.85           | 109.75        | 0.0      | 0.0  | 0.0       | 109.75 | 65.85 | 65.85      | 0.0  | 43.9        |
      | 2  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85  | 65.85 | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 131.7 | 131.7      | 0.0  | 43.9        |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    When Admin sets the business date to "15 April 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 18 April 2026      | 15 April 2026   | 18 May 2026     |                  |                 |            |                 |
    Then Loan has 43.9 outstanding amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 61   | 18 May 2026   |               | 65.85           | 109.75        | 0.0      | 0.0  | 0.0       | 109.75 | 65.85 | 65.85      | 0.0  | 43.9        |
      | 2  | 31   | 18 June 2026  | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85  | 65.85 | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 131.7 | 131.7      | 0.0  | 43.9        |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    When Admin sets the business date to "1 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan has 43.9 outstanding amount
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | RANGE_30       | 43.9             | 21 May 2026    | 18 May 2026 | 41             | 44          |
    When Admin sets the business date to "2 July 2026"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | RANGE_30       | 43.9             | 21 May 2026    | 18 May 2026 | 42             | 45          |
    And Customer makes "AUTOPAY" repayment on "2 July 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 61   | 18 May 2026   | 02 July 2026  | 65.85           | 109.75        | 0.0      | 0.0  | 0.0       | 109.75 | 109.75 | 65.85      | 43.9 | 0.0         |
      | 2  | 31   | 18 June 2026  | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85  | 65.85  | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
      | 02 July 2026     | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 175.6 | 131.7      | 43.9 | 0.0         |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89781
  Scenario: Verify reschedule after ReAge keeps unique due dates when a re-aged installment is fully paid in advance
    When Admin sets the business date to "21 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 21 January 2026   | 480            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 January 2026" with "480" amount and expected disbursement date on "21 January 2026"
    When Admin successfully disburse the loan on "21 January 2026" with "480" EUR transaction amount
    Then Loan has 360.0 outstanding amount
    # --- Step 1: payment holiday: push 21 Feb installment to 21 Mar ---
    When Admin sets the business date to "23 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 21 February 2026   | 23 January 2026 | 21 March 2026   |                  |                 |            |                 |
    # --- Step 2: ReAge into 15 monthly installments starting 21 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 21 March 2026 | 15                   | DEFAULT               |
    Then Loan has 360.0 outstanding amount
    # --- Step 3: advance repayment fully pays the LAST re-aged installment (future allocation: LAST_INSTALLMENT) ---
    When Admin sets the business date to "24 January 2026"
    And Customer makes "AUTOPAY" repayment on "24 January 2026" with 24 EUR transaction amount
    Then Loan has 336.0 outstanding amount
    And Loan Repayment schedule has 17 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026   | 21 January 2026 | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 23 January 2026   | 23 January 2026 | 360.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 57   | 21 March 2026     |                 | 336.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 4  | 31   | 21 April 2026     |                 | 312.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 5  | 30   | 21 May 2026       |                 | 288.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 6  | 31   | 21 June 2026      |                 | 264.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 7  | 30   | 21 July 2026      |                 | 240.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 8  | 31   | 21 August 2026    |                 | 216.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 9  | 31   | 21 September 2026 |                 | 192.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 10 | 30   | 21 October 2026   |                 | 168.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 11 | 31   | 21 November 2026  |                 | 144.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 12 | 30   | 21 December 2026  |                 | 120.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 13 | 31   | 21 January 2027   |                 | 96.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 14 | 31   | 21 February 2027  |                 | 72.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 15 | 28   | 21 March 2027     |                 | 48.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 16 | 31   | 21 April 2027     |                 | 24.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 17 | 30   | 21 May 2027       | 24 January 2026 | 0.0             | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 24.0  | 24.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 144.0 | 24.0       | 0.0  | 336.0       |
    # --- Step 4: reschedule first re-aged installment 21 Mar -> 21 May: ALL re-aged installments incl. the paid one must cascade by 2 months ---
    When Admin sets the business date to "28 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 21 March 2026      | 28 January 2026 | 21 May 2026     |                  |                 |            |                 |
    Then Loan has 336.0 outstanding amount
    Then Loan Repayment schedule has 17 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026   | 21 January 2026 | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 23 January 2026   | 23 January 2026 | 360.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 118  | 21 May 2026       |                 | 336.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 4  | 31   | 21 June 2026      |                 | 312.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 5  | 30   | 21 July 2026      |                 | 288.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 6  | 31   | 21 August 2026    |                 | 264.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 7  | 31   | 21 September 2026 |                 | 240.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 8  | 30   | 21 October 2026   |                 | 216.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 9  | 31   | 21 November 2026  |                 | 192.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 10 | 30   | 21 December 2026  |                 | 168.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 11 | 31   | 21 January 2027   |                 | 144.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 12 | 31   | 21 February 2027  |                 | 120.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 13 | 28   | 21 March 2027     |                 | 96.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 14 | 31   | 21 April 2027     |                 | 72.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 15 | 30   | 21 May 2027       |                 | 48.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 16 | 31   | 21 June 2027      |                 | 24.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 17 | 30   | 21 July 2027      | 24 January 2026 | 0.0             | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 24.0  | 24.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 144.0 | 24.0       | 0.0  | 336.0       |
    # --- Step 5: backdated repayment triggers full reprocessing replay; schedule must keep unique cascaded dates ---
    When Admin sets the business date to "5 February 2026"
    And Customer makes "AUTOPAY" repayment on "1 February 2026" with 24 EUR transaction amount
    Then Loan has 312.0 outstanding amount
    Then Loan Repayment schedule has 17 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                  | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026   | 21 January 2026  | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 23 January 2026   | 23 January 2026  | 360.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 118  | 21 May 2026       |                  | 336.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 4  | 31   | 21 June 2026      |                  | 312.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 5  | 30   | 21 July 2026      |                  | 288.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 6  | 31   | 21 August 2026    |                  | 264.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 7  | 31   | 21 September 2026 |                  | 240.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 8  | 30   | 21 October 2026   |                  | 216.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 9  | 31   | 21 November 2026  |                  | 192.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 10 | 30   | 21 December 2026  |                  | 168.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 11 | 31   | 21 January 2027   |                  | 144.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 12 | 31   | 21 February 2027  |                  | 120.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 13 | 28   | 21 March 2027     |                  | 96.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 14 | 31   | 21 April 2027     |                  | 72.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 15 | 30   | 21 May 2027       |                  | 48.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 16 | 31   | 21 June 2027      | 01 February 2026 | 24.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 24.0  | 24.0       | 0.0  | 0.0         |
      | 17 | 30   | 21 July 2027      | 24 January 2026  | 0.0             | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 24.0  | 24.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 168.0 | 48.0       | 0.0  | 312.0       |
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
