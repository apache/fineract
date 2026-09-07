@WorkingCapital
@WorkingCapitalBreach
@WorkingCapitalBreachPauseFeature
Feature: Working Capital Breach Pause

  @TestRailId:C85234
  Scenario: Verify working capital loan breach pause - pause in current period extends breach schedule and does not affect delinquency schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "03 January 2026" and endDate "05 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-03 | 2026-01-05 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | null   |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85235
  Scenario: Verify working capital loan breach pause - backdated pause re-triggers evaluation of an already evaluated period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-07 | 2026-01-12 | 6            | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach pause with startDate "05 January 2026" and endDate "08 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-08 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-10 | 10           | 110.70           | 110.70            | null       | null   |
      | 2            | 2026-01-11 | 2026-01-16 | 6            | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-10 | 10           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-11 | 2026-01-16 | 6            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"

  @TestRailId:C85236
  Scenario: Verify working capital loan breach pause - backdated pause keeps breach flag when extended period still ends in the past
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-07 | 2026-01-12 | 6            | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach pause with startDate "04 January 2026" and endDate "05 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-04 | 2026-01-05 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-08 | 8            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-09 | 2026-01-14 | 6            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"

  @TestRailId:C85237
  Scenario: Verify working capital loan breach pause - multiple non-overlapping pauses are cumulative
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "03 January 2026" and endDate "04 January 2026"
    And Admin initiate a Working Capital loan breach pause with startDate "06 January 2026" and endDate "07 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-03 | 2026-01-04 |
      | PAUSE  | 2026-01-06 | 2026-01-07 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-10 | 10           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85238
  Scenario: Verify working capital loan breach pause - overlapping pauses are rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "03 January 2026" and endDate "05 January 2026"
    Then Initiating a Working Capital loan breach pause with startDate "04 January 2026" and endDate "08 January 2026" results an error with the following data:
      | httpCode | message                                                      |
      | 400      | Breach pause period cannot overlap with another pause period |
    And Initiating a Working Capital loan breach pause with startDate "02 January 2026" and endDate "08 January 2026" results an error with the following data:
      | httpCode | message                                                      |
      | 400      | Breach pause period cannot overlap with another pause period |
    And Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-03 | 2026-01-05 |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85239
  Scenario: Verify working capital loan breach pause - breach pause and delinquency pause are independent
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "03 January 2026" and endDate "05 January 2026"
    And Admin initiate a Working Capital loan breach pause with startDate "02 January 2026" and endDate "08 January 2026"
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-02 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-13 | 13           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85240
  Scenario: Verify working capital loan breach pause - next period is generated from the extended period and recorded pauses apply to it
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "03 January 2026" and endDate "05 January 2026"
    And Admin initiate a Working Capital loan breach pause with startDate "12 January 2026" and endDate "13 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-10 | 2026-01-17 | 8            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C85241
  Scenario: Verify working capital loan breach pause - future pause beyond the schedule end is preserved when a later backdated pause extends the period over its window
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "08 January 2026" and endDate "09 January 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach pause with startDate "04 January 2026" and endDate "05 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-08 | 2026-01-09 |
      | PAUSE  | 2026-01-04 | 2026-01-05 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-10 | 10           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-10 | 10           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-11 | 2026-01-16 | 6            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"

  @TestRailId:C85242
  Scenario: Verify working capital loan breach pause - pause created before the first COB run is applied to the initial period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin initiate a Working Capital loan breach pause with startDate "05 January 2026" and endDate "15 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-15 |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-11 | 70           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C85243
  Scenario: Verify working capital loan breach pause - validation errors
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach pause with startDate "25 January 2026" and endDate "15 January 2026" results an error with the following data:
      | httpCode | message                                                     |
      | 400      | End date of pause period must be on or after the start date |
    And Initiating a Working Capital loan breach pause with startDate "25 December 2025" and endDate "05 January 2026" results an error with the following data:
      | httpCode | message                                                                                  |
      | 400      | The parameter `startDate` must be greater than or equal to the provided date: 2026-01-01 |
    And Initiating a Working Capital loan breach action "invalid" with startDate "15 January 2026" and endDate "25 January 2026" results an error with the following data:
      | httpCode | message                                                                                                 |
      | 400      | The parameter `action` must be one of [ pause, reschedule, resume, reset, undo_reset, disable, enable ] |
    And Initiating a Working Capital loan breach action without "action" results an error with the following data:
      | httpCode | message                             |
      | 400      | The parameter `action` is mandatory |
    And Initiating a Working Capital loan breach action without "startDate" results an error with the following data:
      | httpCode | message                                |
      | 400      | The parameter `startDate` is mandatory |
    And Initiating a Working Capital loan breach action without "endDate" results an error with the following data:
      | httpCode | message                              |
      | 400      | The parameter `endDate` is mandatory |
    And Retrieving breach actions for a non-existent Working Capital loan results in a 404 error
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C85244
  Scenario: Verify working capital loan breach pause - pause is rejected for a loan without breach configuration
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach pause with startDate "15 January 2026" and endDate "25 January 2026" results an error with the following data:
      | httpCode | message                                                                    |
      | 400      | Breach actions require a breach configuration on the Working Capital loan. |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C85246
  Scenario: Verify working capital loan breach pause - pause is rejected for a not yet active loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Initiating a Working Capital loan breach pause with startDate "15 January 2026" and endDate "25 January 2026" results an error with the following data:
      | httpCode | message                                                              |
      | 400      | Breach actions can be created only for active Working Capital loans. |

  @TestRailId:C85247
  Scenario: Verify working capital loan breach pause - backdated payment resets breach flag of an already breached period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-07 | 2026-01-12 | 6            | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach pause with startDate "09 January 2026" and endDate "10 January 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-07 | 2026-01-14 | 8            | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "04 January 2026" with 150.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 0.00              | null       | false  |
      | 2            | 2026-01-07 | 2026-01-14 | 8            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "12 January 2026"

  @TestRailId:C85248
  Scenario: Verify working capital loan breach pause - touching pauses are rejected and contiguous pauses must not share a day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "03 January 2026" and endDate "05 January 2026"
    # start and end are inclusive, so a second pause sharing the boundary day (05 Jan) overlaps and is rejected
    Then Initiating a Working Capital loan breach pause with startDate "05 January 2026" and endDate "07 January 2026" results an error with the following data:
      | httpCode | message                                                      |
      | 400      | Breach pause period cannot overlap with another pause period |
    # a contiguous pause must start the day after the previous one ends, so the boundary day is not double-counted
    When Admin initiate a Working Capital loan breach pause with startDate "06 January 2026" and endDate "07 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-03 | 2026-01-05 |
      | PAUSE  | 2026-01-06 | 2026-01-07 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-11 | 11           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85249
  Scenario: Verify working capital loan breach pause - a pre-existing payment is not re-bucketed when a later pause shifts the period boundary across its date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # the 50.00 paid on 08 Jan falls in period 2 (07 Jan - 12 Jan) at the time it is made
    And Customer makes repayment on "08 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-07 | 2026-01-12 | 6            | 110.70           | 60.70             | null       | null   |
    # backdated pause inside period 1 pushes its end to 09 Jan, so period 1's window now covers 08 Jan,
    # but the 50.00 paid that day stays credited to period 2
    When Admin initiate a Working Capital loan breach pause with startDate "03 January 2026" and endDate "05 January 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-10 | 2026-01-15 | 6            | 110.70           | 60.70             | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C85250
  Scenario: Verify working capital loan breach pause - pause can be applied and retrieved by loan external id
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause by external ID with startDate "03 January 2026" and endDate "05 January 2026"
    Then Working Capital loan breach action by external ID has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-03 | 2026-01-05 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85350
  Scenario: Verify working capital loan breach pause - a single day pause with the same start and end date extends the schedule by one day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "04 January 2026" and endDate "04 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-04 | 2026-01-04 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-07 | 7            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85351
  Scenario: Verify working capital loan breach pause - a single day pause on the schedule start date extends the schedule by one day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |
    # the pause covers exactly the schedule start date (inclusive), so it still extends the period by one day
    When Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "01 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-01 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 60           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C85251
  Scenario: Verify working capital loan breach pause - resume shortens the active pause and recalculates the breach schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 12           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "03 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
      | RESUME | 2026-01-03 |            |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85348
  Scenario: Verify working capital loan breach pause - resume date remains included in the effective pause
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 12           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "03 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
      | RESUME | 2026-01-03 |            |
    # Resume date is still paused, so the effective pause is 01 Jan through 03 Jan inclusive.
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85349
  Scenario: Verify working capital loan breach pause - a new pause can start after the effective resumed pause ends
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "03 January 2026"
    # The first effective pause ends on 03 Jan, so 04 Jan is the first valid start date for another pause.
    When Admin initiate a Working Capital loan breach pause with startDate "04 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
      | RESUME | 2026-01-03 |            |
      | PAUSE  | 2026-01-04 | 2026-01-06 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 12           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85252
  Scenario: Verify working capital loan breach pause - resume is rejected when there is no active pause on the resume date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "02 January 2026" and endDate "03 January 2026"
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach resume with startDate "05 January 2026" results an error with the following data:
      | httpCode | message                                                         |
      | 400      | Resume breach action can only be created during an active pause |
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"

  @TestRailId:C85253
  Scenario: Verify working capital loan breach pause - resume is rejected when the resume date is not the current business date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "02 January 2026" and endDate "04 January 2026"
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach resume with startDate "04 January 2026" results an error with the following data:
      | httpCode | message                                                         |
      | 400      | Start date of a resume action must be the current business date |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85254
  Scenario: Verify working capital loan breach pause - resume with an end date is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "02 January 2026" and endDate "04 January 2026"
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach action "resume" with startDate "03 January 2026" and endDate "05 January 2026" results an error with the following data:
      | httpCode | message                                           |
      | 400      | End date must not be provided for a resume action |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85255
  Scenario: Verify working capital loan breach pause - resume is rejected for a pause that was already resumed
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "03 January 2026"
    Then Initiating a Working Capital loan breach resume with startDate "03 January 2026" results an error with the following data:
      | httpCode | message                                                         |
      | 400      | Resume breach action can only be created during an active pause |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85256
  Scenario: Verify working capital loan breach pause - resume shortens the pause so the period breaches earlier
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 12           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "03 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
      | RESUME | 2026-01-03 |            |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-10 | 2026-01-15 | 6            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C85257
  Scenario: Verify working capital loan breach pause - resume on the day before planned pause end shortens schedule by one day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 12           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "05 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
      | RESUME | 2026-01-05 |            |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-11 | 11           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"

  @TestRailId:C85258
  Scenario: Verify working capital loan breach pause - resume by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause by external ID with startDate "01 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 12           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume by external ID with startDate "03 January 2026"
    Then Working Capital loan breach action by external ID has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
      | RESUME | 2026-01-03 |            |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85259
  Scenario: Verify working capital loan breach pause - backdated resume is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach resume with startDate "02 January 2026" results an error with the following data:
      | httpCode | message                                                         |
      | 400      | Start date of a resume action must be the current business date |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85260
  Scenario: Verify working capital loan breach pause - resume without an active pause is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach resume with startDate "03 January 2026" results an error with the following data:
      | httpCode | message                                                         |
      | 400      | Resume breach action can only be created during an active pause |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85261
  Scenario: Verify working capital loan breach pause - resume keeps the PAUSE action dates unchanged and only shortens the breach schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "04 January 2026"
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "04 January 2026" and endDate "08 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-04 | 2026-01-08 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-11 | 11           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "05 January 2026"
    And Admin initiate a Working Capital loan breach resume with startDate "05 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-04 | 2026-01-08 |
      | RESUME | 2026-01-05 |            |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-08 | 8            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"

  @TestRailId:C85262
  Scenario: Verify working capital loan breach pause - COB-generated period after resume honours the shortened pause
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "04 January 2026"
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "04 January 2026" and endDate "08 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-04 | 2026-01-08 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-11 | 11           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "05 January 2026"
    And Admin initiate a Working Capital loan breach resume with startDate "05 January 2026"
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-04 | 2026-01-08 |
      | RESUME | 2026-01-05 |            |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-08 | 8            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-09 | 2026-01-14 | 6            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "09 January 2026"

  @TestRailId:C85263
  Scenario: Verify working capital loan breach pause - resume does not affect delinquency range schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 12           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "03 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
      | RESUME | 2026-01-03 |            |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | null   |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C85352
  Scenario: Verify working capital loan breach pause - resume after a reschedule keeps the rescheduled minimum payment and shortens the schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    When Admin creates WC breach reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 1              | PERCENTAGE         |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 90               | 90                | null       | null   |
    # the minimumPayment-only reschedule keeps the configured 6-day frequency, so the period extends by the inclusive 5-day pause while the rescheduled minPayment (90) is preserved
    When Admin initiate a Working Capital loan breach pause with startDate "02 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-11 | 11           | 90               | 90                | null       | null   |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "04 January 2026"
    Then Working Capital loan breach action has the following data:
      | action     | startDate  | endDate    |
      | RESCHEDULE | 2026-01-01 |            |
      | PAUSE      | 2026-01-02 | 2026-01-06 |
      | RESUME     | 2026-01-04 |            |
    # resume folds into the pause with an effective inclusive end of 04 Jan (the resume date is still a paused day), shortening the extension from 5 to 3 days
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 90               | 90                | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"
