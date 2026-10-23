@WorkingCapital
@WorkingCapitalNearBreachEvaluationFeature
Feature: Working Capital Near Breach Evaluation - Part2

  @TestRailId:C115189
  Scenario: Verify near breach re-evaluation - UC5: backdated repayment does not re-open the closed period's flag
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 6               | DAYS                | FLAT                        | 90           | 2                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Pay only 10 (< 30 required): evals at 01-03 and 01-05 trigger and keep nearBreach=true.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 10.0 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 80.00             | true       | null   |
    # Close period 1 with COB on 07-Jan: paid 10 < 90 -> breach=true, nearBreach stays true (final evaluated result).
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 80.00             | true       | true   |
      | 2            | 2026-01-07 | 2026-01-12 | 90.00            | 90.00             | null       | null   |
    # Backdated repayment of 80 dated 03-Jan would have met the near-breach requirement (10+80=90 >= 30),
    # but period 1 is already closed -> its nearBreach=true result is frozen and must not change.
    When Admin sets the business date to "08 January 2026"
    And Customer makes repayment on "03 January 2026" with 80.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 0.00              | true       | false  |
      | 2            | 2026-01-07 | 2026-01-12 | 90.00            | 90.00             | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C115190
  Scenario: Verify near breach re-evaluation - UC6: reprocessing resets and re-derives the flag from effective payments
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 6               | DAYS                | FLAT                        | 90           | 2                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # No payment -> eval at 01-03 triggers nearBreach=true.
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 90.00             | true       | null   |
    # Repayment of 60 on 05-Jan clears the flag.
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 60.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 30.00             | false      | null   |
    # Backdated repayment of 10 dated 02-Jan triggers reprocessing. Cumulative paid at the
    # elapsed checkpoints is now 70 >= 30 -> flag re-derived and stays false (no new change event).
    When Admin sets the business date to "06 January 2026"
    And Customer makes repayment on "02 January 2026" with 10.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 20.00             | false      | null   |
    # Undo the 05-Jan repayment -> reprocessing leaves only the 02-Jan payment (10 < 30) -> flag re-derived to true.
    When Customer undo "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 80.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "06 January 2026"

  @TestRailId:C115191
  Scenario: Verify near breach re-evaluation - UC7: disabled breach evaluation freezes nearBreach and its events
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 6               | DAYS                | FLAT                        | 90           | 2                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # No payment -> eval at 01-03 triggers nearBreach=true.
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 90.00             | true       | null   |
    # Disable breach evaluation: subsequent monetary events must not re-derive nearBreach nor raise events.
    And Admin initiate a Working Capital loan breach disable with startDate "04 January 2026"
    Then Working Capital loan breach disable action has the following data:
      | action  | startDate  | endDate |
      | DISABLE | 2026-01-04 |         |
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 90.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 90.00             | true       | null   |
    # Re-enable: evaluation resumes and re-derives nearBreach from the actual payments -> false.
    When Admin sets the business date to "06 January 2026"
    And Admin initiate a Working Capital loan breach enable with startDate "06 January 2026"
    Then Working Capital loan breach disable action has the following data:
      | action  | startDate  | endDate    |
      | DISABLE | 2026-01-04 | 2026-01-05 |
      | ENABLE  | 2026-01-06 |            |
    # Re-enable recomputes the schedule as of 06-Jan: period 1 closes (paid in full -> false/false) and period 2 opens.
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 0.00              | false      | false  |
      | 2            | 2026-01-07 | 2026-01-12 | 90.00            | 90.00             | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "06 January 2026"

  @TestRailId:C115192
  Scenario: Verify near breach re-evaluation - UC8: re-evaluation honours the loan-level RESCHEDULE override
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 6               | DAYS                | FLAT                        | 90           | 2                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # RESCHEDULE on 02-Jan: threshold=50% (=45 required), freq=3 DAYS -> eval#1 at 01-04.
    When Admin sets the business date to "02 January 2026"
    And Admin creates a near breach reschedule action with threshold "50" frequency 3 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 50        | 3         | DAYS          |
    # Pay 40 on 03-Jan: at eval#1 (01-04) cumulative paid 40 < 45 -> nearBreach=true under the override.
    # (Under the product config 40 >= 30 would have passed -> proves the override is honoured.)
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 40.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 50.00             | true       | null   |
    # Pay 10 more on 05-Jan: cumulative paid 50 >= 45 -> monetary event re-derives the flag to false.
    And Customer makes repayment on "05 January 2026" with 10.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 90.00            | 40.00             | false      | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"
