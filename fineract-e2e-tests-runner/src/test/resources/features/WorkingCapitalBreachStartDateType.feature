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
      | 1            | 2026-01-06 | 2026-01-08 | 3            | 100              | 100               | null       | true   |
      | 2            | 2026-01-09 | 2026-01-11 | 3            | 100              | 100               | null       | null   |
    And Working capital loan account has the correct data:
      | breachStartDate |
      | 2026-01-06      |

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
