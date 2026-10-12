@WorkingCapital
@WorkingCapitalDelinquencyResumeFeature
Feature: Working Capital Delinquency Resume

  @TestRailId:C85172
  Scenario: Verify working capital loan delinquency resume - UC1: resume shortens active pause and delinquency range schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "16 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-16 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-15 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume with startDate "10 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-16 |
      | RESUME | 2026-01-10 |            |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C85173
  Scenario: Verify working capital loan delinquency resume - UC2: resume on the day before planned pause end shortens schedule by one day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "16 January 2026"
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-15 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume with startDate "15 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-16 |
      | RESUME | 2026-01-15 |            |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-14 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C85174
  Scenario: Verify working capital loan delinquency resume - UC3: resume by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause by external ID with startDate "01 January 2026" and endDate "16 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume by external ID with startDate "10 January 2026"
    Then Working Capital loan delinquency action by external ID has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-16 |
      | RESUME | 2026-01-10 |            |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C85175
  Scenario: Verify working capital loan delinquency resume - UC4: resume without an active pause results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan delinquency resume with startDate "10 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                         |
      | 400      | Resume Delinquency Action can only be created during an active pause |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C85176
  Scenario: Verify working capital loan delinquency resume - UC5: backdated resume results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "16 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan delinquency resume with startDate "09 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                                  |
      | 400      | Start date of the Resume Delinquency action must be the current business date |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C85177
  Scenario: Verify working capital loan delinquency resume - UC6: resume on pause start date results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "16 January 2026"
    Then Initiating a Working Capital loan delinquency resume with startDate "01 January 2026" results an error with the following data:
      | httpCode | errorMessage                                          |
      | 400      | Resume date must be after the active pause start date |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C85178
  Scenario: Verify working capital loan delinquency resume - UC7: multiple resumes on the same pause results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "16 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume with startDate "10 January 2026"
    Then Initiating a Working Capital loan delinquency resume with startDate "10 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                         |
      | 400      | Resume Delinquency Action can only be created during an active pause |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C85179
  Scenario: Verify working capital loan delinquency resume - UC8: resume recalculates delinquency immediately
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 16             |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Admin initiate a Working Capital loan delinquency pause with startDate "15 February 2026" and endDate "15 March 2026"
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 16             |
      | 2            | 2026-01-31 | 2026-03-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "20 February 2026"
    And Admin initiate a Working Capital loan delinquency resume with startDate "20 February 2026"
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 21             |
      | 2            | 2026-01-31 | 2026-03-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "20 February 2026"

  @TestRailId:C85219
  Scenario: Verify resume keeps the PAUSE action dates unchanged and only shortens the delinquency period extension
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "28 January 2026"
    And Admin initiate a Working Capital loan delinquency pause with startDate "28 January 2026" and endDate "20 February 2026"
    When Admin sets the business date to "29 January 2026"
    And Admin initiate a Working Capital loan delinquency resume with startDate "29 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-28 | 2026-02-20 |
      | RESUME | 2026-01-29 |            |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "29 January 2026"

  @TestRailId:C85220
  Scenario: Verify that COB-generated period after resume must honour the shortened (resumed) pause
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "28 January 2026"
    And Admin initiate a Working Capital loan delinquency pause with startDate "28 January 2026" and endDate "20 February 2026"
    When Admin sets the business date to "29 January 2026"
    And Admin initiate a Working Capital loan delinquency resume with startDate "29 January 2026"
    When Admin sets the business date to "02 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount |
      | 1            | 2026-01-01 | 2026-02-01 | 270.0          | 0.0        | 270.0             |
      | 2            | 2026-02-02 | 2026-03-03 | 270.0          | 0.0        | 270.0             |
    Then Admin closes the Working Capital loan with a full repayment on "02 February 2026"

  @TestRailId:C85314
  Scenario: Verify new pause is allowed after resumed pause when outside effective pause window
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "28 January 2026"
    And Admin initiate a Working Capital loan delinquency pause with startDate "28 January 2026" and endDate "20 February 2026"
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-23 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "29 January 2026"
    And Admin initiate a Working Capital loan delinquency resume with startDate "29 January 2026"
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "05 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "05 February 2026" and endDate "10 February 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-28 | 2026-02-20 |
      | RESUME | 2026-01-29 |            |
      | PAUSE  | 2026-02-05 | 2026-02-10 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-01 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 4              |
      | 2            | 2026-02-02 | 2026-03-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "05 February 2026"

  @TestRailId:C85330
  Scenario: Verify working capital loan delinquency pause - G1: a one-day pause (startDate == endDate) is accepted and extends the schedule by one day
  # both pause dates are INCLUSIVE, so startDate == endDate is a valid 1-day pause
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "10 January 2026" and endDate "10 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-10 | 2026-01-10 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-31 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C85328
  Scenario: Verify working capital loan delinquency pause - G2: an active pause suppresses delinquency on the first overdue day
  # Period 1 is due 30-Jan. A pause 30-Jan -> 10-Feb shifts its toDate to 11-Feb (+12 inclusive).
  # On 31-Jan, the first day AFTER the original due date, the loan would normally be delinquentDays = 1;
  # while paused it must stay non-delinquent (null) - i.e. delinquency does not grow across the paused period's start/due boundary.
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "30 January 2026"
    And Admin initiate a Working Capital loan delinquency pause with startDate "30 January 2026" and endDate "10 February 2026"
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-11 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-11 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "31 January 2026"

  @TestRailId:C85329
  Scenario: Verify working capital loan delinquency resume - G3: the resume date counts as paused and delinquency resumes only the next day
  # Pause 10-Jan -> 20-Feb (+42 inclusive) is resumed on 15-Jan. The resume date is STILL a paused day, so the
  # effective pause is 10..15-Jan = 6 days (inclusive of the 15th), shifting period 1 toDate to 05-Feb (due 30-Jan + 6).
  # On 05-Feb (the shifted toDate) the period is still open (null); delinquency only resumes on 06-Feb (= 1).
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "10 January 2026"
    And Admin initiate a Working Capital loan delinquency pause with startDate "10 January 2026" and endDate "20 February 2026"
    When Admin sets the business date to "15 January 2026"
    And Admin initiate a Working Capital loan delinquency resume with startDate "15 January 2026"
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-05 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "05 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-05 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "06 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-05 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-02-06 | 2026-03-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with a full repayment on "06 February 2026"
