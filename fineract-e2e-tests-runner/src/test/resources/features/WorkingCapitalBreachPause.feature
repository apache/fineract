@WorkingCapital
@WorkingCapitalBreachPauseFeature
Feature: Working Capital Breach Pause

  Scenario: Verify working capital loan breach pause - pause in current period extends breach schedule and does not affect delinquency schedule
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
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "15 January 2026" and endDate "25 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-15 | 2026-01-25 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-10 | 69           | 110.70           | 110.70            | null       | null   |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  Scenario: Verify working capital loan breach pause - backdated pause re-triggers evaluation of an already evaluated period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach pause with startDate "20 February 2026" and endDate "02 March 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-02-20 | 2026-03-02 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-10 | 69           | 110.70           | 110.70            | null       | null   |
      | 2            | 2026-03-11 | 2026-05-10 | 61           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "11 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-10 | 69           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-11 | 2026-05-10 | 61           | 110.70           | 110.70            | null       | null   |

  Scenario: Verify working capital loan breach pause - backdated pause keeps breach flag when extended period still ends in the past
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach pause with startDate "20 February 2026" and endDate "25 February 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-02-20 | 2026-02-25 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-05 | 64           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-06 | 2026-05-05 | 61           | 110.70           | 110.70            | null       | null   |

  Scenario: Verify working capital loan breach pause - multiple non-overlapping pauses are cumulative
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "15 January 2026" and endDate "25 January 2026"
    And Admin initiate a Working Capital loan breach pause with startDate "01 February 2026" and endDate "06 February 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-15 | 2026-01-25 |
      | PAUSE  | 2026-02-01 | 2026-02-06 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-15 | 74           | 110.70           | 110.70            | null       | null   |

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
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "15 January 2026" and endDate "25 January 2026"
    Then Initiating a Working Capital loan breach pause with startDate "20 January 2026" and endDate "30 January 2026" results an error with the following data:
      | httpCode | message                                                 |
      | 400      | Failed data validation due to: overlapping.pause.periods |
    And Initiating a Working Capital loan breach pause with startDate "10 January 2026" and endDate "30 January 2026" results an error with the following data:
      | httpCode | message                                                 |
      | 400      | Failed data validation due to: overlapping.pause.periods |
    And Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-15 | 2026-01-25 |

  Scenario: Verify working capital loan breach pause - breach pause and delinquency pause are independent
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "15 January 2026" and endDate "25 January 2026"
    And Admin initiate a Working Capital loan breach pause with startDate "10 January 2026" and endDate "30 January 2026"
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-20 | 79           | 110.70           | 110.70            | null       | null   |

  Scenario: Verify working capital loan breach pause - next period is generated from the extended period and recorded pauses apply to it
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "15 January 2026" and endDate "25 January 2026"
    And Admin initiate a Working Capital loan breach pause with startDate "20 March 2026" and endDate "25 March 2026"
    When Admin sets the business date to "11 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-10 | 69           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-11 | 2026-05-15 | 66           | 110.70           | 110.70            | null       | null   |

  Scenario: Verify working capital loan breach pause - future pause beyond the schedule end is preserved when a later backdated pause extends the period over its window
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "05 March 2026" and endDate "08 March 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach pause with startDate "01 February 2026" and endDate "10 February 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-03-05 | 2026-03-08 |
      | PAUSE  | 2026-02-01 | 2026-02-10 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-12 | 71           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "13 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-12 | 71           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-13 | 2026-05-12 | 61           | 110.70           | 110.70            | null       | null   |

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
      | 1            | 2026-01-01 | 2026-03-10 | 69           | 110.70           | 110.70            | null       | null   |

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
    Then Initiating a Working Capital loan breach pause with startDate "15 January 2026" and endDate "15 January 2026" results an error with the following data:
      | httpCode | message                                              |
      | 400      | Failed data validation due to: must.be.after.startDate |
    And Initiating a Working Capital loan breach pause with startDate "25 January 2026" and endDate "15 January 2026" results an error with the following data:
      | httpCode | message                                              |
      | 400      | Failed data validation due to: must.be.after.startDate |
    And Initiating a Working Capital loan breach pause with startDate "25 December 2025" and endDate "05 January 2026" results an error with the following data:
      | httpCode | message                                                                            |
      | 400      | The parameter `startDate` must be greater than or equal to the provided date: 2026-01-01 |
    And Initiating a Working Capital loan breach action "resume" with startDate "15 January 2026" and endDate "25 January 2026" results an error with the following data:
      | httpCode | message                                       |
      | 400      | The parameter `action` must be one of [ pause ] |
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
      | httpCode | message                                                |
      | 400      | Failed data validation due to: no.breach.configuration |

  Scenario: Verify working capital loan breach pause - pause start date is validated against the grace-shifted breach schedule start
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays |
      | 7               | DAYS                | PERCENTAGE                  | 9            | 3               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-04 | 2026-01-10 | 7            | 900.00           | 900.00            | null       | null   |
    And Initiating a Working Capital loan breach pause with startDate "01 January 2026" and endDate "10 January 2026" results an error with the following data:
      | httpCode | message                                                                                   |
      | 400      | The parameter `startDate` must be greater than or equal to the provided date: 2026-01-04 |
    When Admin initiate a Working Capital loan breach pause with startDate "04 January 2026" and endDate "08 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-04 | 2026-01-08 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-04 | 2026-01-14 | 11           | 900.00           | 900.00            | null       | null   |

  Scenario: Verify working capital loan breach pause - pause is rejected for a not yet active loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Initiating a Working Capital loan breach pause with startDate "15 January 2026" and endDate "25 January 2026" results an error with the following data:
      | httpCode | message                                        |
      | 400      | Failed data validation due to: loan.is.not.active |

  Scenario: Verify working capital loan breach pause - backdated payment resets breach flag of an already breached period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach pause with startDate "20 March 2026" and endDate "30 March 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-05-10 | 71           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "14 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin makes Internal Payment "150.0" on "2026-02-15"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 0.00              | null       | false  |
      | 2            | 2026-03-01 | 2026-05-10 | 71           | 110.70           | 110.70            | null       | null   |
