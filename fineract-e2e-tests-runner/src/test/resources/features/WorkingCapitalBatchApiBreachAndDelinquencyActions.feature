@WorkingCapital
@WorkingCapitalBatchApi
@WorkingCapitalBatchApiBreachAndDelinquencyActions
Feature: Working Capital Batch API - Breach and Delinquency Actions

  # ============================================
  # SECTION 4: Individual Batch API Action Tests
  # ============================================

  @TestRailId:C110961
  Scenario: Verify Batch API - UC18: Create Working Capital breach pause action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Batch API creates WC breach pause action from "01 January 2026" to "15 January 2026" on the working capital loan by external ID
    Then Admin checks that all steps result 200OK
    And Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-15 |
    And Working Capital loan balance has breach past due amount "0"

  @TestRailId:C110962
  Scenario: Verify Batch API - UC19: Create Working Capital breach reschedule action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | true   |
      | 3            | 2026-05-01 | 2026-06-30 | 61           | 110.70           | 110.70            | null       | null   |
    When Batch API creates WC breach reschedule action with the following parameters on the working capital loan by external ID:
      | minimumPayment | minimumPaymentType |
      | 1              | PERCENTAGE         |
    Then Admin checks that all steps result 200OK
    And WC loan breach actions have the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 01 June 2026    | 1              | PERCENTAGE         |           |               |
    When Admin sets the business date to "15 August 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | true   |
      | 3            | 2026-05-01 | 2026-06-30 | 61           | 90.00            | 90.00             | null       | true   |
      | 4            | 2026-07-01 | 2026-08-31 | 62           | 90.00            | 90.00             | null       | null   |

  @TestRailId:C110963
  Scenario: Verify Batch API - UC20: Create Working Capital breach reset action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Batch API creates WC breach reset action with restart period from reset date "true" on the working capital loan by external ID
    Then Admin checks that all steps result 200OK
    And Working Capital loan breach action has the following data:
      | action | startDate  |
      | RESET  | 2026-06-01 |

  @TestRailId:C110964
  Scenario: Verify Batch API - UC21: Create Working Capital breach undo reset action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Batch API creates WC breach reset action with restart period from reset date "true" on the working capital loan by external ID
    When Batch API creates WC breach undo reset action on the working capital loan by external ID
    Then Admin checks that all steps result 200OK
    And Working Capital loan breach action has the following data:
      | action      | startDate  |
      | RESET       | 2026-06-01 |
      | UNDO_RESET  | 2026-06-01 |

  @TestRailId:C110965
  Scenario: Verify Batch API - UC22: Create Working Capital delinquency pause action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Batch API creates WC delinquency pause action from "01 January 2026" to "15 January 2026" on the working capital loan by external ID
    Then Admin checks that all steps result 200OK
    And Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-15 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-14 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  @TestRailId:C110966
  Scenario: Verify Batch API - UC23: Create Working Capital delinquency resume action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Batch API creates WC delinquency pause action from "01 January 2026" to "15 January 2026" on the working capital loan by external ID
    When Admin sets the business date to "10 January 2026"
    And Batch API creates WC delinquency resume action from "10 January 2026" on the working capital loan by external ID
    Then Admin checks that all steps result 200OK
    And Working Capital loan delinquency action has the following data:
      | action  | startDate  | endDate    |
      | PAUSE   | 2026-01-01 | 2026-01-15 |
      | RESUME  | 2026-01-10 |            |

  @TestRailId:C110967
  Scenario: Verify Batch API - UC24: Create Working Capital delinquency reschedule action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Batch API creates WC delinquency reschedule action with the following parameters on the working capital loan by external ID:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 15        | DAYS          |
    Then Admin checks that all steps result 200OK
    And Working Capital loan delinquency action has the following data:
      | action     | startDate  |
      | RESCHEDULE | 2026-02-01 |

  @TestRailId:C110968
  Scenario: Verify Batch API - UC25: Create Working Capital delinquency reset action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Batch API creates WC delinquency reset action with start new period "true" on the working capital loan by external ID
    Then Admin checks that all steps result 200OK
    And Working Capital loan delinquency action has the following data:
      | action | startDate  |
      | RESET  | 2026-02-01 |

  @TestRailId:C110969
  Scenario: Verify Batch API - UC26: Create Working Capital delinquency undo reset action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Batch API creates WC delinquency reset action with start new period "true" on the working capital loan by external ID
    When Batch API creates WC delinquency undo reset action on the working capital loan by external ID
    Then Admin checks that all steps result 200OK
    And Working Capital loan delinquency action has the following data:
      | action      | startDate  |
      | RESET       | 2026-02-01 |
      | UNDO_RESET  | 2026-02-01 |

  # ============================================
  # SECTION 5: Full Lifecycle Batch API Tests
  # ============================================

  @TestRailId:C110970
  Scenario: Verify Batch API - UC27: Full Working Capital Loan lifecycle with delinquency pause via batch API by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API call with working capital steps by external IDs: "createWCLoan, approveWCLoan, disburseWCLoan, createWCDelinquencyPauseAction" runs with enclosingTransaction: "true" and loan data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Admin checks that all steps result 200OK
    And Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    And Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-15 |
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-14 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  # ============================================
  # SECTION 6: Negative Batch API Tests
  # ============================================

  @TestRailId:C110971
  Scenario: Verify Batch API - UC28: Batch API returns 400 for invalid Working Capital breach action by external ID (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Batch API creates WC breach action with action "invalid" on the working capital loan by external ID
    Then Verify that WCL step 1 throws an error with error code 400

  @TestRailId:C110972
  Scenario: Verify Batch API - UC29: Batch API returns 400 for delinquency pause with missing endDate by external ID (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Batch API creates WC delinquency pause action from "01 January 2026" with no end date on the working capital loan by external ID
    Then Verify that WCL step 1 throws an error with error code 400

  # ============================================
  # SECTION 7: Internal-ID and Near-Breach Batch API Tests
  # ============================================

  @TestRailId:C111029
  Scenario: Verify Batch API - UC30: Full Working Capital Loan lifecycle with delinquency pause via combined batch by internal reference
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API call with working capital steps by internal reference: "createWCLoan, approveWCLoan, disburseWCLoan, createWCDelinquencyPauseAction" runs with enclosingTransaction: "true" and loan data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Admin checks that all steps result 200OK
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-15 |

  @TestRailId:C111030
  Scenario: Verify Batch API - UC31: Create Working Capital breach pause action via batch by loan ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Batch API creates WC breach pause action from "01 January 2026" to "15 January 2026" on the working capital loan by loan ID
    Then Admin checks that all steps result 200OK
    And Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-15 |

  @TestRailId:C111031
  Scenario: Verify Batch API - UC32: Create Working Capital near breach reschedule action via batch by external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct             | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Batch API creates WC near breach reschedule action with threshold "40" frequency 4 frequencyType "DAYS" on the working capital loan by external ID
    Then Admin checks that all steps result 200OK
    And Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 40        | 4         | DAYS          |

  @TestRailId:C111032
  Scenario: Verify Batch API - UC33: Create Working Capital near breach reschedule action via batch by loan ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct             | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Batch API creates WC near breach reschedule action with threshold "40" frequency 4 frequencyType "DAYS" on the working capital loan by loan ID
    Then Admin checks that all steps result 200OK
    And Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 40        | 4         | DAYS          |

