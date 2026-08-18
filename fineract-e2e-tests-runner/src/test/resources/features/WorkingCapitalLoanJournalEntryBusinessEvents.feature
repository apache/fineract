@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalLoanJournalEntryBusinessEventsFeature
Feature: Working Capital Loan Journal Entry Business Events

  @TestRailId:C94066
  Scenario: Working Capital loan payout refund raises a Journal Entry Created business event per ledger line
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Journal Entry Created business events are raised with balanced debits and credits
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C94067
  Scenario: Working Capital loan charge-off raises a Journal Entry Created business event per ledger line
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working Capital Loan Journal Entry Created business events are raised with balanced debits and credits
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C94068
  Scenario: Working Capital loan repayment raises a Journal Entry Created business event per ledger line
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Journal Entry Created business events are raised with balanced debits and credits
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"
