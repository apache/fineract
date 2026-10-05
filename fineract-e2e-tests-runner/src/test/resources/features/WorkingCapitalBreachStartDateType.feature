@WorkingCapital
@WorkingCapitalBreachStartDateTypeFeature
Feature: Working Capital Breach Start Date Type

  @TestRailId:C89771
  Scenario: Verify breach start date type - UC1: LOAN_CREATION anchor with same-day disbursement matches the DISBURSEMENT baseline
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 0               | LOAN_CREATION   |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan details has the following field values:
      | breachStartType.code | LOAN_CREATION |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-03 | 3            | 100              | 100               | null       | null   |

  @TestRailId:C89772
  Scenario: Verify breach start date type - UC2: LOAN_CREATION anchor with late disbursement backfills and breaches pre-disbursement periods
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 0               | LOAN_CREATION   |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 10 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "10 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin successfully disburse the Working Capital loan on "10 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-03 | 3            | 100              | 100               | null       | true   |
      | 2            | 2026-01-04 | 2026-01-06 | 3            | 100              | 100               | null       | true   |
      | 3            | 2026-01-07 | 2026-01-09 | 3            | 100              | 100               | null       | true   |
      | 4            | 2026-01-10 | 2026-01-12 | 3            | 100              | 100               | null       | null   |
    And Working capital loan account has the correct data:
      | breachStartDate |
      | 2026-01-01      |
    And Working capital loan details has the following field values:
      | breachStartType.code | LOAN_CREATION |

  @TestRailId:C89773
  Scenario: Verify breach start date type - UC6: submitted but never disbursed LOAN_CREATION loan gets no breach schedule and never breaches
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 0               | LOAN_CREATION   |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has no data

  @TestRailId:C89774
  Scenario: Verify breach start date type - UC7: pause inside the pre-disbursement breach window is accepted when the schedule covers those dates
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 0               | LOAN_CREATION   |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 10 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "10 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin successfully disburse the Working Capital loan on "10 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-03 | 3            | 100              | 100               | null       | true   |
      | 2            | 2026-01-04 | 2026-01-06 | 3            | 100              | 100               | null       | true   |
      | 3            | 2026-01-07 | 2026-01-09 | 3            | 100              | 100               | null       | true   |
      | 4            | 2026-01-10 | 2026-01-12 | 3            | 100              | 100               | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "05 January 2026" and endDate "07 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-07 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-03 | 3            | 100              | 100               | null       | true   |
      | 2            | 2026-01-04 | 2026-01-09 | 6            | 100              | 100               | null       | true   |
      | 3            | 2026-01-10 | 2026-01-12 | 3            | 100              | 100               | null       | null   |
      | 4            | 2026-01-13 | 2026-01-15 | 3            | 100              | 100               | null       | null   |

  @TestRailId:C89775
  Scenario: Verify breach start date type - UC3: breachGraceDays shift the LOAN_CREATION anchor, not the disbursement date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 5               | LOAN_CREATION   |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 10 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "10 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin successfully disburse the Working Capital loan on "10 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Schedule starts at submittedOnDate 01 Jan + 5 grace days = 06 Jan (DISBURSEMENT anchor would start it on 15 Jan)
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-08 | 8            | 100              | 100               | null       | true   |
      | 2            | 2026-01-09 | 2026-01-11 | 3            | 100              | 100               | null       | null   |
    And Working capital loan account has the correct data:
      | breachStartDate |
      | 2026-01-01      |

  @TestRailId:C93977
  Scenario: Verify breach start date type - UC3.1: breachGraceDays shifts properly when breachStartType is DISBURSEMENT
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 5               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 10 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "10 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin successfully disburse the Working Capital loan on "10 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Schedule fromDate will be set at disbursement date 10 Jan but no breach will occur yet
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-10 | 2026-01-17 | 8            | 100              | 100               | null       | null   |
    And Working capital loan account has the correct data:
      | breachStartDate |
      | null            |
    # Schedule starts at disbursement date 10 Jan
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-10 | 2026-01-17 | 8            | 100              | 100               | null       | true   |
      | 2            | 2026-01-18 | 2026-01-20 | 3            | 100              | 100               | null       | null   |
    And Working capital loan account has the correct data:
      | breachStartDate |
      | 2026-01-10      |

  @TestRailId:C89776
  Scenario: Verify breach start date type - UC4: loan-level DISBURSEMENT override wins over LOAN_CREATION product default
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 0               | LOAN_CREATION   |
    And Admin creates a working capital loan using created product with breachStartType "DISBURSEMENT" and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 10 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "10 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin successfully disburse the Working Capital loan on "10 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # The loan-level DISBURSEMENT override anchors the schedule on 10 Jan - no retroactive periods, nothing breached
    Then Working capital loan details has the following field values:
      | breachStartType.code | DISBURSEMENT |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-10 | 2026-01-12 | 3            | 100              | 100               | null       | null   |
    And Working capital loan account has the correct data:
      | breachStartDate |
      | null            |

  @TestRailId:C89777
  Scenario: Verify breach start date type - UC5: loan-level LOAN_CREATION override wins over explicit DISBURSEMENT product default
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 0               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with breachStartType "LOAN_CREATION" and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 10 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "10 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin successfully disburse the Working Capital loan on "10 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan details has the following field values:
      | breachStartType.code | LOAN_CREATION |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-03 | 3            | 100              | 100               | null       | true   |
      | 2            | 2026-01-04 | 2026-01-06 | 3            | 100              | 100               | null       | true   |
      | 3            | 2026-01-07 | 2026-01-09 | 3            | 100              | 100               | null       | true   |
      | 4            | 2026-01-10 | 2026-01-12 | 3            | 100              | 100               | null       | null   |
    And Working capital loan account has the correct data:
      | breachStartDate |
      | 2026-01-01      |

  @TestRailId:C110950
  Scenario: Verify breachEffectiveStartDate - UC1: first breach period with grace days
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | FLAT                        | 500          | 5               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- First breach period evaluated after the grace window ---
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-01      | 2026-01-06               |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-20 | 20           | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-01-21 | 2026-02-04 | 15           | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    Then a Working Capital Loan Breach Change business event is raised with breach data:
      | breachStartDate | breachEffectiveStartDate | breachAmount | breachPastDueAmount | breachFlag |
      | 2026-01-01         | 2026-01-06               | 500.0     | 500.0              | true       |
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    Then Admin closes the Working Capital loan with a full repayment on "21 January 2026"

  @TestRailId:C110951
  Scenario: Verify breachEffectiveStartDate - UC2: LOAN_CREATION anchor
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | FLAT                        | 500          | 5               | LOAN_CREATION   |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 08 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "08 January 2026"
    # --- Disbursement happens after the LOAN_CREATION anchor date ---
    When Admin sets the business date to "08 January 2026"
    And Admin successfully disburse the Working Capital loan on "08 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan details has the following field values:
      | breachStartType.code | LOAN_CREATION |
    And Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-01      | 2026-01-06               |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-20 | 20           | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-01-21 | 2026-02-04 | 15           | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    Then a Working Capital Loan Breach Change business event is raised with breach data:
      | breachStartDate | breachEffectiveStartDate | breachAmount | breachPastDueAmount | breachFlag |
      | 2026-01-01         | 2026-01-06               | 500.0     | 500.0              | true       |
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    Then Admin closes the Working Capital loan with a full repayment on "21 January 2026"

  @TestRailId:C110952
  Scenario: Verify breachEffectiveStartDate - UC3: no grace days configured
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | FLAT                        | 500          | 0               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- First breach period without grace days ---
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-01      | null                     |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-15 | 15           | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-01-16 | 2026-01-30 | 15           | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    Then a Working Capital Loan Breach Change business event is raised with breach data:
      | breachStartDate | breachEffectiveStartDate | breachAmount | breachPastDueAmount | breachFlag |
      | 2026-01-01         | null                     | 500.0     | 500.0              | true       |
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    Then Admin closes the Working Capital loan with a full repayment on "16 January 2026"

  @TestRailId:C110953
  Scenario: Verify breachEffectiveStartDate - UC4: breach period is not the first one
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | FLAT                        | 500          | 5               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Repayment covers the first period ---
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 500.0 transaction amount on Working Capital loan
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | null            | null                     |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-20 | 20           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-01-21 | 2026-02-04 | 15           | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "0"
    # --- Second breach period is evaluated later ---
    When Admin sets the business date to "05 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-21      | null                     |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-20 | 20           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-01-21 | 2026-02-04 | 15           | 500.00           | 500.00            | null       | true   |
      | 3            | 2026-02-05 | 2026-02-19 | 15           | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    Then a Working Capital Loan Breach Change business event is raised with breach data:
      | breachStartDate | breachEffectiveStartDate | breachAmount | breachPastDueAmount | breachFlag |
      | 2026-01-21         | null                     | 500.0     | 500.0              | true       |
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    Then Admin closes the Working Capital loan with a full repayment on "05 February 2026"

  @TestRailId:C110954
  Scenario: Verify breachEffectiveStartDate - UC5: reset cuts first period inside the grace window
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | FLAT                        | 500          | 5               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Reset cuts the first period inside the grace window ---
    When Admin sets the business date to "03 January 2026"
    And Admin creates WC breach reset action with restart period from reset date
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-01      | null                     |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-02 | 2            | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-01-03 | 2026-01-17 | 15           | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "0"
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"

  @TestRailId:C110955
  Scenario: Verify breachEffectiveStartDate - UC6: business event payload exposes the effective date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | FLAT                        | 500          | 5               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Business event payload exposes breachEffectiveStartDate ---
    Then a Working Capital Loan Breach Change business event is raised with breach data:
      | breachStartDate | breachEffectiveStartDate | breachAmount | breachPastDueAmount | breachFlag |
      | 2026-01-01         | 2026-01-06               | 500.0     | 500.0              | true       |
    And Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-01      | 2026-01-06               |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-20 | 20           | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-01-21 | 2026-02-04 | 15           | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    Then Admin closes the Working Capital loan with a full repayment on "21 January 2026"

  @TestRailId:C110956
  Scenario: Verify breachEffectiveStartDate - UC7: PERCENTAGE breach amount happy path
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | PERCENTAGE                  | 2            | 3               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- PERCENTAGE breach amount with grace days ---
    When Admin sets the business date to "19 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-01      | 2026-01-04               |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-18 | 18           | 180.00           | 180.00            | null       | true   |
      | 2            | 2026-01-19 | 2026-02-02 | 15           | 180.00           | 180.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "180"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    Then a Working Capital Loan Breach Change business event is raised with breach data:
      | breachStartDate | breachEffectiveStartDate | breachAmount | breachPastDueAmount | breachFlag |
      | 2026-01-01         | 2026-01-04               | 2.0       | 180.0              | true       |
    And a Working Capital Loan Breach Past Due Change business event is raised with "180" past due amount
    Then Admin closes the Working Capital loan with a full repayment on "19 January 2026"

  @TestRailId:C110957
  Scenario: Verify breachEffectiveStartDate - UC8: undo reset restores the effective start date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | FLAT                        | 500          | 5               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Reset and immediate undo inside the grace window ---
    When Admin sets the business date to "03 January 2026"
    And Admin creates WC breach reset action with restart period from reset date
    And Admin creates WC breach undo reset action
    # --- Original first period breaches again after undo ---
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-01      | 2026-01-06               |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-20 | 20           | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-01-21 | 2026-02-04 | 15           | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    Then a Working Capital Loan Breach Change business event is raised with breach data:
      | breachStartDate | breachEffectiveStartDate | breachAmount | breachPastDueAmount | breachFlag |
      | 2026-01-01         | 2026-01-06               | 500.0     | 500.0              | true       |
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    Then Admin closes the Working Capital loan with a full repayment on "21 January 2026"

  @TestRailId:CTBD
  Scenario: Verify breachEffectiveStartDate - UC9: reschedule shortens the first period but keeps the grace window
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 15              | DAYS                | FLAT                        | 500          | 5               | DISBURSEMENT    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Reschedule to a frequency shorter than the grace window, dated inside the first period ---
    When Admin sets the business date to "04 January 2026"
    And Admin creates WC breach reschedule action with the following parameters:
      | frequency | frequencyType |
      | 2         | DAYS          |
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | null            | null                     |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-07 | 7            | 500.00           | 500.00            | null       | null   |
    # --- The shortened first period breaches and still reports the grace window ---
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | breachStartDate | breachEffectiveStartDate |
      | 2026-01-01      | 2026-01-06               |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-07 | 7            | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-01-08 | 2026-01-09 | 2            | 500.00           | 500.00            | null       | null   |
    And Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    Then a Working Capital Loan Breach Change business event is raised with breach data:
      | breachStartDate | breachEffectiveStartDate | breachAmount | breachPastDueAmount | breachFlag |
      | 2026-01-01      | 2026-01-06               | 500.0        | 500.0               | true       |
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    Then Admin closes the Working Capital loan with a full repayment on "08 January 2026"
