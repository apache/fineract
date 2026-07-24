@WorkingCapital
@WorkingCapitalBreach
@WorkingCapitalBreachResetFeature
Feature: Working Capital Breach Reset and Undo Reset

  @TestRailId:C85426
  Scenario: Verify breach reset marks the action-date period only and preserves schedule fields
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | false |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | true  |
    And Working Capital loan balance has breach past due amount "0"
    And WC loan breach actions have the following data:
      | action | startDate     |
      | RESET  | 15 April 2026 |
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   | true  |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   | false |
    And Working Capital loan balance has breach past due amount "400"
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85427
  Scenario: Verify breach reset marks only the action-date period when prior period is overpaid but overdue
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 300.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 50.00             | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | false |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 50.00             | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | true  |
    And WC loan breach actions have the following data:
      | action | startDate     |
      | RESET  | 15 April 2026 |
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 50.00             | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   | true  |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   | false |
    And Working Capital loan balance has breach past due amount "400"
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85428
  Scenario: Verify backdated payment before reset action date updates schedule while reset is active
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | true  |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85429
  Scenario: Verify multiple breach resets mark their own action-date periods
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin creates WC breach reset action
    Then WC loan breach actions have the following data:
      | action | startDate     |
      | RESET  | 15 April 2026 |
      | RESET  | 04 May 2026   |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   | true  |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   | true  |
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85430
  Scenario: Verify undo breach reset lifts the action-date period flag and preserves schedule fields
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | true  |
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | false |
    And Working Capital loan balance has breach past due amount "100"
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | UNDO_RESET | 15 April 2026 |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85431
  Scenario: Verify undo reset lifts only the reset flag for its own action-date period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   | true  |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   | true  |
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   | true  |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   | false |
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | RESET      | 04 May 2026   |
      | UNDO_RESET | 04 May 2026   |
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85432
  Scenario: Verify undo reset does not revert backdated payment recalculation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | true  |
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | false |
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | UNDO_RESET | 15 April 2026 |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85433
  Scenario: Verify payments inside the reset period recalculate the flagged period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    And Customer makes repayment on "15 April 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 350.00            | null   | true  |
    And Customer makes repayment on "14 April 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 250.00            | null   | true  |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85434
  Scenario: Verify breach reset preserves an already evaluated non-breached period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 400.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | false |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | true  |
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | false |
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | UNDO_RESET | 15 April 2026 |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85435
  Scenario: Verify breach reset fails when reset already exists in current period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Admin fails to create WC breach reset action with error containing "reset.already.exists.in.current.period"
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85436
  Scenario: Verify a second reset in the same period is allowed after the first reset is undone
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | true  |
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | false |
    When Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | true  |
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | UNDO_RESET | 15 April 2026 |
      | RESET      | 15 April 2026 |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85437
  Scenario: Verify breach undo reset fails when no active reset exists
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    Then Admin fails to create WC breach undo reset action with error containing "no.breach.reset.to.undo"
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85438
  Scenario: Verify breach undo reset fails when reset was already undone
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    And Admin creates WC breach undo reset action
    Then Admin fails to create WC breach undo reset action with error containing "no.breach.reset.to.undo"
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85439
  Scenario Outline: Verify breach <action> fails on non-active loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to create WC breach "<action>" action with error containing "loan.is.not.active"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

    Examples:
      | action     |
      | reset      |
      | undo_reset |

  @TestRailId:C85440
  Scenario: Verify breach reset fails when breach schedule does not exist
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    Then Admin fails to create WC breach reset action with error containing "no.breach.schedule"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C85441
  Scenario: Verify breach reset fails when no breach evaluation period exists for business date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays | breachGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      | 30              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    Then Admin fails to create WC breach reset action with error containing "no.breach.evaluation.period"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C85442
  Scenario Outline: Verify breach <action> fails when loan product has no breach configuration
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 1                 |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin fails to create WC breach "<action>" action with error containing "no.breach.configuration"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

    Examples:
      | action     |
      | reset      |
      | undo_reset |

  @TestRailId:C85443
  Scenario: Verify breach reset preserves near breach fields and only marks the action-date period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach | reset |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | null   | false |
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach | reset |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | true   | false |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   | false |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach | reset |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | true   | false |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   | true  |
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach | reset |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | true   | false |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   | false |
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | UNDO_RESET | 15 April 2026 |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85551
  Scenario: Verify breach reset on a pause-extended schedule only marks the extended action-date period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   | false |
    When Admin sets the business date to "20 March 2026"
    And Admin initiate a Working Capital loan breach pause with startDate "20 March 2026" and endDate "29 March 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-05-10 | 70           | 400.00           | 400.00            | null   | false |
    When Admin sets the business date to "11 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-05-10 | 70           | 400.00           | 400.00            | null   | false |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-05-10 | 70           | 400.00           | 400.00            | null   | true  |
    And WC loan breach actions have the following data:
      | action | startDate     |
      | PAUSE  | 20 March 2026 |
      | RESET  | 15 April 2026 |
    When Admin sets the business date to "11 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach | reset |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   | false |
      | 2            | 2026-03-02 | 2026-05-10 | 70           | 400.00           | 400.00            | true   | true  |
      | 3            | 2026-05-11 | 2026-07-09 | 60           | 400.00           | 400.00            | null   | false |
    Then Admin closes the Working Capital loan with a full repayment on "11 May 2026"
