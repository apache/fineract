@WorkingCapital
@WorkingCapitalLoanWriteOffFeature
Feature: Working Capital Loan Write-off

  @TestRailId:temp1
  Scenario: Verify Working Capital Write-off - UC1: write-off zeroes balances and closes the loan as written-off
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"

  @TestRailId:temp2
  Scenario: Verify Working Capital Write-off - UC2: undo write-off reopens the loan and restores the balance
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"

  @TestRailId:temp3
  Scenario: Verify Working Capital Write-off - UC3: write-off on an accounting-enabled product posts and reverses cleanly
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
