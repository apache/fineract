@WorkingCapitalBreachResetFeature
Feature: Working Capital Breach Reset and Undo Reset

  @TestRailId:C85426
  Scenario: Verify breach reset clears prior breach state - 2 installments overdue (simple)
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
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    And WC loan breach actions have the following data:
      | action | startDate      |
      | RESET  | 15 April 2026  |
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   |
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85427
  Scenario: Verify breach reset clears prior breach state - overpaid and overdue
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
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 50.00             | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    And WC loan breach actions have the following data:
      | action | startDate      |
      | RESET  | 15 April 2026  |
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   |
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85428
  Scenario: Verify backdated payment before reset date is ignored for breach schedule
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
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    And WC loan breach actions have the following data:
      | action | startDate      |
      | RESET  | 15 April 2026  |
    When Admin sets the business date to "15 April 2026"
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85429
  Scenario: Verify multiple breach resets maintain history
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
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin creates WC breach reset action
    Then WC loan breach actions have the following data:
      | action | startDate      |
      | RESET  | 15 April 2026  |
      | RESET  | 04 May 2026    |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 0.00              | null   |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   |
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85430
  Scenario: Verify undo breach reset restores prior breach state
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
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    And WC loan breach actions have the following data:
      | action     | startDate      |
      | RESET      | 15 April 2026  |
      | UNDO_RESET | 15 April 2026  |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85431
  Scenario: Verify multiple breach resets are undone in LIFO order, each restoring its own captured state
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
    # First reset (15 April) clears period 1, which is in breach with 100 past due
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    # Advance so period 2 ends and breaches (400 past due), then second reset (04 May) clears it
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 0.00              | null   |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   |
    And WC loan breach actions have the following data:
      | action | startDate     |
      | RESET  | 15 April 2026 |
      | RESET  | 04 May 2026   |
    # First undo lifts only the latest reset (04 May): period 2 is restored to its captured breach state; period 1 stays cleared by the first reset (re-evaluated to breach=false since its outstanding is 0)
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   |
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | RESET      | 04 May 2026   |
      | UNDO_RESET | 04 May 2026   |
    # Second undo lifts the first reset (15 April): period 1 is restored to its captured breach state (100 past due / breach)
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | true   |
      | 3            | 2026-05-01 | 2026-06-29 | 60           | 400.00           | 400.00            | null   |
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | RESET      | 04 May 2026   |
      | UNDO_RESET | 04 May 2026   |
      | UNDO_RESET | 04 May 2026   |
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85432
  Scenario: Verify undo reset does not re-apply a backdated payment that was ignored while the reset was active
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
    # Backdated payment (value date before the 15 April reset) is ignored for the breach schedule - period 1 stays cleared
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    # Undo restores the snapshot captured at reset time; the ignored backdated payment is NOT re-applied, so period 1 returns to 100 past due / breach (it does not become 0)
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    And WC loan breach actions have the following data:
      | action     | startDate     |
      | RESET      | 15 April 2026 |
      | UNDO_RESET | 15 April 2026 |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85433
  Scenario: Verify a payment on the reset date counts but a payment before the reset date is ignored
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
    # Payment dated exactly on the reset date (15 April) is applied: period 2 outstanding drops 400 -> 350
    And Customer makes repayment on "15 April 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 350.00            | null   |
    # Payment dated before the reset date (14 April) is ignored: period 2 outstanding stays 350
    And Customer makes repayment on "14 April 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 350.00            | null   |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C85434
  Scenario: Verify breach reset clears and restores an already-evaluated, non-breached prior period
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
    # Period 1 was fully paid before its end, so it is evaluated as breach=false (not in breach)
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    # Reset clears period 1 even though it was not in breach (breach flag becomes null)
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    # Undo restores period 1 to its captured non-breached state (breach=false)
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | false  |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
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
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    # Undo reopens the period's reset slot (only active resets are counted per period)
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 100.00            | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
    # A second reset in the same period now succeeds (the create step asserts HTTP 200); contrast with the "reset already exists" scenario above where no undo was performed
    When Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 400.00           | 0.00              | null   |
      | 2            | 2026-03-02 | 2026-04-30 | 60           | 400.00           | 400.00            | null   |
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
  Scenario: Verify breach reset clears near breach and undo reset restores it
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
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | null   |
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | true   |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   |
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 0.00              | null       | null   |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   |
    When Admin creates WC breach undo reset action
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | true   |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   |
    And WC loan breach actions have the following data:
      | action     | startDate      |
      | RESET      | 15 April 2026  |
      | UNDO_RESET | 15 April 2026  |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"
