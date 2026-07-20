@WorkingCapital
@WorkingCapitalBreach
@WorkingCapitalBreachScheduleFeature
Feature: Working Capital Breach Schedule

  @TestRailId:C74539
  Scenario: Verify working capital loan breach schedule - no breach schedule for loan with state "Submitted and pending approval"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has no data

  @TestRailId:C74540
  Scenario: Verify working capital loan breach schedule - no breach schedule for loan with state "Approved"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has no data

  @TestRailId:C74541
  Scenario: Verify working capital loan breach schedule - breach schedule on disbursement date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |

  @TestRailId:C74542
  Scenario: Verify working capital loan breach schedule - last day of 1st period - no evaluation yet
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "28 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |

  @TestRailId:C74543
  Scenario: Verify working capital loan breach schedule - first day after 1st period - 2nd period generated, 1st evaluated
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | null   |

  @TestRailId:C74544
  Scenario: Verify working capital loan breach schedule - multiple periods
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 July 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | true   |
      | 3            | 2026-05-01 | 2026-06-30 | 61           | 110.70           | 110.70            | null       | true   |
      | 4            | 2026-07-01 | 2026-08-31 | 62           | 110.70           | 110.70            | null       | null   |

  @TestRailId:C74545
  Scenario: Verify working capital loan breach schedule - with discount affects minPayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 123.00           | 123.00            | null       | null   |

  @TestRailId:C74546
  Scenario: Verify working capital loan breach schedule - custom breach config with grace days, 10 periods
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays | breachGraceDays |
      | 7               | DAYS                | PERCENTAGE                  | 9            | 3                    | 3               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-04 | 2026-01-10 | 7            | 900.00           | 900.00            | null       | null   |
    When Admin sets the business date to "09 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-04 | 2026-01-10 | 7            | 900.00           | 900.00            | null       | true   |
      | 2            | 2026-01-11 | 2026-01-17 | 7            | 900.00           | 900.00            | null       | true   |
      | 3            | 2026-01-18 | 2026-01-24 | 7            | 900.00           | 900.00            | null       | true   |
      | 4            | 2026-01-25 | 2026-01-31 | 7            | 900.00           | 900.00            | null       | true   |
      | 5            | 2026-02-01 | 2026-02-07 | 7            | 900.00           | 900.00            | null       | true   |
      | 6            | 2026-02-08 | 2026-02-14 | 7            | 900.00           | 900.00            | null       | true   |
      | 7            | 2026-02-15 | 2026-02-21 | 7            | 900.00           | 900.00            | null       | true   |
      | 8            | 2026-02-22 | 2026-02-28 | 7            | 900.00           | 900.00            | null       | true   |
      | 9            | 2026-03-01 | 2026-03-07 | 7            | 900.00           | 900.00            | null       | true   |
      | 10           | 2026-03-08 | 2026-03-14 | 7            | 900.00           | 900.00            | null       | null   |

  @TestRailId:C74547
  Scenario: Verify working capital loan breach schedule - product without breach config - no schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has no data

  @TestRailId:C74548
  Scenario: Verify working capital loan breach schedule - WEEKS frequency, single period on disbursement day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | WEEKS               | PERCENTAGE                  | 10           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-07 | 7            | 900.00           | 900.00            | null       | null   |

  @TestRailId:C74549
  Scenario: Verify working capital loan breach schedule - WEEKS frequency, rollover into subsequent periods
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 2               | WEEKS               | PERCENTAGE                  | 10           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "29 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-14 | 14           | 900.00           | 900.00            | null       | true   |
      | 2            | 2026-01-15 | 2026-01-28 | 14           | 900.00           | 900.00            | null       | true   |
      | 3            | 2026-01-29 | 2026-02-11 | 14           | 900.00           | 900.00            | null       | null   |

  @TestRailId:C74550
  Scenario: Verify working capital loan breach schedule - YEARS frequency with FLAT calculation ignores principal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | YEARS               | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # FLAT must use breachAmount verbatim — principal and discount must not leak into minPayment.
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-12-31 | 365          | 500.00           | 500.00            | null       | null   |

  @TestRailId:C74551
  Scenario: Verify working capital loan breach schedule - MONTHS=1 sequential 4 periods across month boundaries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | PERCENTAGE                  | 2            |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 180.00           | 180.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 180.00           | 180.00            | null       | true   |
      | 3            | 2026-03-01 | 2026-03-31 | 31           | 180.00           | 180.00            | null       | true   |
      | 4            | 2026-04-01 | 2026-04-30 | 30           | 180.00           | 180.00            | null       | null   |

  @TestRailId:C74552
  Scenario: Verify working capital loan breach schedule - disburse on month-end (31st) with MONTHS=1
    When Admin sets the business date to "31 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 100          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 31 January 2026 | 31 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "31 January 2026" with "9000" amount and expected disbursement date on "31 January 2026"
    When Admin successfully disburse the Working Capital loan on "31 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-31 | 2026-02-27 | 28           | 100.00           | 100.00            | null       | null   |
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-31 | 2026-02-27 | 28           | 100.00           | 100.00            | null       | true   |
      | 2            | 2026-02-28 | 2026-03-27 | 28           | 100.00           | 100.00            | null       | true   |
      | 3            | 2026-03-28 | 2026-04-27 | 31           | 100.00           | 100.00            | null       | null   |

  @TestRailId:C74553
  Scenario: Verify working capital loan breach schedule - leap-year Feb 29 with YEARS=1
    When Admin sets the business date to "29 February 2024"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | YEARS               | FLAT                        | 100          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 29 February 2024 | 29 February 2024         | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "29 February 2024" with "9000" amount and expected disbursement date on "29 February 2024"
    When Admin successfully disburse the Working Capital loan on "29 February 2024" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2024-02-29 | 2025-02-27 | 365          | 100.00           | 100.00            | null       | null   |
    
  @TestRailId:C74554
  Scenario: Verify working capital loan breach schedule - exact period-end day must not create next period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | PERCENTAGE                  | 10           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 1 period
    When Admin sets the business date to "30 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 1 period
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 1 period
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 2 periods

  @TestRailId:C74555
  Scenario: Verify working capital loan breach schedule - idempotent COB run on the same business date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | null   |

  @TestRailId:C74556
  Scenario: Verify working capital loan breach schedule - GET for non-existent loanId returns 404
    Then Retrieving Working Capital loan breach schedule for non-existent loanId 9999999999 fails with status code 404

  @TestRailId:C74557
  Scenario: Verify working capital loan breach schedule - rejected loan has no schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin rejects the working capital loan on "01 January 2026"
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has no data

  @TestRailId:C74558
  Scenario: Verify working capital loan breach schedule - undo-approved loan has no schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin makes undo approval on the working capital loan
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has no data

  @TestRailId:C74559
  Scenario: Verify working capital loan breach schedule - grace days exceed breach frequency
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays | breachGraceDays |
      | 7               | DAYS                | PERCENTAGE                  | 10           | 45                   | 45              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-02-15 | 2026-02-21 | 7            | 900.00           | 900.00            | null       | null   |

  @TestRailId:C74560
  Scenario: Verify working capital loan breach schedule - FLAT breachAmount=0 yields breach=false on expiry
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 0            |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 0.00             | 0.00              | null       | null   |
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 0.00             | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 0.00             | 0.00              | null       | null   |

  @TestRailId:C77000
  Scenario: Verify working capital loan breach schedule - first period start shifted by breachGraceDays
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          | 5               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-06 | 2026-02-05 | 31           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C77002
  Scenario: Verify working capital loan account inherits breachGraceDays from product and breach schedule is shifted accordingly
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          | 4               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan account has breachGraceDays 4
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-05 | 2026-02-04 | 31           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C77003
  Scenario: Verify working capital loan account breachGraceDays override takes precedence over product value in breach schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          | 4               |
    And Admin creates a working capital loan using created product with breachGraceDays 9 and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan account has breachGraceDays 9
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-10 | 2026-02-09 | 31           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C77004
  Scenario: Verify breach schedule is not shifted by delinquencyGraceDays when breachGraceDays is not set
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays | breachGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          | 3                    |                 |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # delinquencyGraceDays must shift ONLY the delinquency machinery — breach schedule starts on disbursement date
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C85264
  Scenario: Verify that breachStartDate and delinquencyStartDate are populated once the loan is in breach and delinquent
    # Validates breachStartDate / delinquencyStartDate on the GET loan by response
    # breachStartDate       = fromDate of the earliest breached breach-schedule period
    # delinquencyStartDate  = fromDate of the earliest delinquent range-schedule period + delinquencyGraceDays
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays | breachGraceDays |
      | 15              | DAYS                | FLAT                        | 500          | 3                    | 5               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # Breach period 1 = [Jan 6 .. Jan 20], delinquency period 1 = [Jan 1 .. Jan 30]
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # ...and Jan 31 (= delinquency toDate + 1) flags the delinquency; the breach flag set above stays
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | delinquencyStartDate |
      | 2026-01-06      | 2026-01-04           |

  @TestRailId:C85265
  Scenario: Verify that only breachStartDate is set while the breach period has elapsed but the delinquency period has not
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays | breachGraceDays |
      | 15              | DAYS                | FLAT                        | 500          | 3                    | 5               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # Jan 21: breach period 1 ([Jan 6 .. Jan 20]) has elapsed -> breached; delinquency period 1 ([Jan 1 .. Jan 30]) has NOT -> not evaluated
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | delinquencyStartDate |
      | 2026-01-06      | null                 |

  @TestRailId:C85266
  Scenario: Verify that only delinquencyStartDate is set for a delinquency-only product with no breach configuration
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # Default WCLP has a delinquency bucket (period 1 = [Jan 1 .. Jan 30], grace 0) and no breach configuration
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | delinquencyStartDate |
      | null            | 2026-01-01           |

  @TestRailId:C85267
  Scenario: Verify that breachStartDate and delinquencyStartDate are null for a newly create loan before any period has elapsed
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays | breachGraceDays |
      | 15              | DAYS                | FLAT                        | 500          | 3                    | 5               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | delinquencyStartDate |
      | null            | null                 |

  @TestRailId:C85268
  Scenario: Verify that delinquencyStartDate is anchored on the loan creation date when delinquencyStartType is LOAN_CREATION
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 08 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "08 January 2026"
    When Admin sets the business date to "08 January 2026"
    And Admin successfully disburse the Working Capital loan on "08 January 2026" with "9000" EUR transaction amount
    # Evaluate after both the creation-anchored ([Jan 1 .. Jan 30]) and disbursement-anchored ([Jan 8 .. Feb 6])
    # first period have elapsed, so the asserted value is unambiguous
    When Admin sets the business date to "07 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | delinquencyStartDate |
      | null            | 2026-01-04           |
