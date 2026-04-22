@WorkingCapitalDelinquencyRescheduleActionFeature @WCCOBFeature
Feature: Working Capital Delinquency Reschedule Action

  @TestRailId:C74495
  Scenario: Verify that reschedule changes minimumPayment only
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               |                       |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate              | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026     | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026  | 01 March 2026       | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026    | 31 March 2026       | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026    | 30 April 2026       | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026      | 30 May 2026         | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026      | 29 June 2026        | 100            | 0          | 100               | false                 |
      | 7            | 30 June 2026     | 29 July 2026        | 100            | 0          | 100               | false                 |
      | 8            | 30 July 2026     | 28 August 2026      | 100            | 0          | 100               |                       |

  @TestRailId:C74496
  Scenario: Verify that reschedule changes frequency only
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 3              | PERCENTAGE         | 15        | DAYS          |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate              | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026     | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026  | 01 March 2026       | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026    | 31 March 2026       | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026    | 30 April 2026       | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026      | 30 May 2026         | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026      | 29 June 2026        | 300            | 0          | 300               | false                 |
      | 7            | 30 June 2026     | 14 July 2026        | 300            | 0          | 300               | false                 |
      | 8            | 15 July 2026     | 29 July 2026        | 300            | 0          | 300               | false                 |
      | 9            | 30 July 2026     | 13 August 2026      | 300            | 0          | 300               | false                 |
      | 10           | 14 August 2026   | 28 August 2026      | 300            | 0          | 300               |                       |

  @TestRailId:C74497
  Scenario: Verify that reschedule changes minimumPayment and frequency
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 2              | PERCENTAGE         | 15        | DAYS          |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate              | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026     | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026  | 01 March 2026       | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026    | 31 March 2026       | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026    | 30 April 2026       | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026      | 30 May 2026         | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026      | 29 June 2026        | 200            | 0          | 200               | false                 |
      | 7            | 30 June 2026     | 14 July 2026        | 200            | 0          | 200               | false                 |
      | 8            | 15 July 2026     | 29 July 2026        | 200            | 0          | 200               | false                 |
      | 9            | 30 July 2026     | 13 August 2026      | 200            | 0          | 200               | false                 |
      | 10           | 14 August 2026   | 28 August 2026      | 200            | 0          | 200               |                       |

  @TestRailId:C74498
  Scenario: Verify that the latest reschedule action wins
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 2              | PERCENTAGE         | 30        | DAYS          |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 5              | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "15 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate            | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026   | 500            | 0          | 500               | false                 |
      | 2            | 31 January 2026  | 01 March 2026     | 500            | 0          | 500               | false                 |
      | 3            | 02 March 2026    | 31 March 2026     | 500            | 0          | 500               | false                 |
      | 4            | 01 April 2026    | 30 April 2026     | 500            | 0          | 500               |                       |
    Then WC loan delinquency actions contain 2 actions

  @TestRailId:C74499
  Scenario: Verify that reschedule on non-active loan and validation errors are rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 1 PERCENTAGE and frequency 30 DAYS with error containing "only for active Working Capital loans"
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 1 PERCENTAGE and frequency 30 DAYS with error containing "existing delinquency range schedule"
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 0 PERCENTAGE and frequency 30 DAYS with error containing "`minimumPayment` must be greater than 0"
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 1 PERCENTAGE and frequency 0 DAYS with error containing "`frequency` must be greater than 0"
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 1 PERCENTAGE and frequency 30 INVALID with error containing "Invalid frequency type: INVALID"

  @TestRailId:C74500
  Scenario: Verify that reschedule after a PAUSE extends rescheduled periods correctly
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 March 2026" and endDate "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency actions contain 2 actions
    Then WC loan has both PAUSE and RESCHEDULE delinquency actions
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate              | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026     | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026  | 15 March 2026       | 300            | 0          | 300               | false                 |
      | 3            | 16 March 2026    | 14 April 2026       | 300            | 0          | 300               | false                 |
      | 4            | 15 April 2026    | 14 May 2026         | 300            | 0          | 300               | false                 |
      | 5            | 15 May 2026      | 13 June 2026        | 100            | 0          | 100               | false                 |
      | 6            | 14 June 2026     | 13 July 2026        | 100            | 0          | 100               | false                 |
      | 7            | 14 July 2026     | 12 August 2026      | 100            | 0          | 100               | false                 |
      | 8            | 13 August 2026   | 11 September 2026   | 100            | 0          | 100               |                       |

  @TestRailId:C74501
  Scenario: Verify that PAUSE after RESCHEDULE preserves rescheduled parameters
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 2              | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "01 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 April 2026" and endDate "15 April 2026"
    When Admin sets the business date to "15 July 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency actions contain 2 actions
    Then WC loan has both PAUSE and RESCHEDULE delinquency actions
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate            | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026   | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026  | 01 March 2026     | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026    | 31 March 2026     | 200            | 0          | 200               | false                 |
      | 4            | 01 April 2026    | 14 May 2026       | 200            | 0          | 200               | false                 |
      | 5            | 15 May 2026      | 13 June 2026      | 200            | 0          | 200               | false                 |
      | 6            | 14 June 2026     | 13 July 2026      | 200            | 0          | 200               | false                 |
      | 7            | 14 July 2026     | 12 August 2026    | 200            | 0          | 200               |                       |

  @TestRailId:C74502
  Scenario: Verify that reschedule spot-check reflects evaluated vs rescheduled period amounts
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | expectedAmount | outstandingAmount | delinquentDays | delinquentAmount |
      | 1            | 300            | 300               | 197            | 300              |
      | 5            | 300            | 300               | 77             | 300              |
      | 6            | 100            | 100               | 47             | 100              |
      | 8            | 100            | 100               |                |                  |

  @TestRailId:C74503
  Scenario: Verify that reschedule on disbursement date creates a single period
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 5              | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 500            | 0          | 500               |                       |

  @TestRailId:C74504
  Scenario: Verify that reschedule on the first day of a new period updates subsequent periods
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "31 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "15 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate          | toDate            | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026   | 30 January 2026   | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026   | 01 March 2026     | 100            | 0          | 100               | false                 |
      | 3            | 02 March 2026     | 31 March 2026     | 100            | 0          | 100               | false                 |
      | 4            | 01 April 2026     | 30 April 2026     | 100            | 0          | 100               |                       |

  @TestRailId:C74505
  Scenario: Verify that retrieving delinquency actions returns RESCHEDULE action details
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 2              | PERCENTAGE         | 15        | DAYS          |
    Then WC loan delinquency actions contain 1 action
    Then WC loan last delinquency action has the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 01 January 2026 | 2              | PERCENTAGE         | 15        | DAYS          |

  @TestRailId:C74506
  Scenario: Verify that COB generates new periods using rescheduled parameters over time
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 15        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 100            | 0          | 100               |                       |
    When Admin sets the business date to "01 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate          | toDate            | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026   | 30 January 2026   | 100            | 0          | 100               | false                 |
      | 2            | 31 January 2026   | 14 February 2026  | 100            | 0          | 100               | false                 |
      | 3            | 15 February 2026  | 01 March 2026     | 100            | 0          | 100               |                       |

  @TestRailId:C74507
  Scenario: Verify that reschedule supports WEEKS frequency type
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 2              | PERCENTAGE         | 2         | WEEKS         |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate          | toDate            | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026   | 30 January 2026   | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026   | 01 March 2026     | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026     | 31 March 2026     | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026     | 30 April 2026     | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026       | 30 May 2026       | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026       | 29 June 2026      | 200            | 0          | 200               | false                 |
      | 7            | 30 June 2026      | 13 July 2026      | 200            | 0          | 200               | false                 |
      | 8            | 14 July 2026      | 27 July 2026      | 200            | 0          | 200               | false                 |
      | 9            | 28 July 2026      | 10 August 2026    | 200            | 0          | 200               | false                 |
      | 10           | 11 August 2026    | 24 August 2026    | 200            | 0          | 200               |                       |

  @TestRailId:C74508
  Scenario: Verify that reschedule supports MONTHS frequency type
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 2              | PERCENTAGE         | 1         | MONTHS        |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate          | toDate            | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026   | 30 January 2026   | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026   | 01 March 2026     | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026     | 31 March 2026     | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026     | 30 April 2026     | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026       | 30 May 2026       | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026       | 29 June 2026      | 200            | 0          | 200               | false                 |
      | 7            | 30 June 2026      | 29 July 2026      | 200            | 0          | 200               | false                 |
      | 8            | 30 July 2026      | 29 August 2026    | 200            | 0          | 200               |                       |

  @TestRailId:C74509
  Scenario: Verify that reschedule with negative minimumPayment is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with minimumPayment -5 PERCENTAGE and frequency 30 DAYS with error containing "`minimumPayment` must be greater than 0"

  @TestRailId:C74510
  Scenario: Verify that reschedule with negative frequency is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 3 PERCENTAGE and frequency -1 DAYS with error containing "`frequency` must be greater than 0"

  @TestRailId:C74511
  Scenario: Verify that reschedule supports minimumPayment over 100 percent
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 200            | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 20000          | 0          | 20000             |                       |

  @TestRailId:C74512
  Scenario: Verify that reschedule supports decimal minimumPayment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 2.5            | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 250            | 0          | 250               |                       |

  @TestRailId:C74513
  Scenario: Verify that two identical reschedules produce the same schedule as a single reschedule
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 30        | DAYS          |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 100            | 0          | 100               |                       |
    Then WC loan delinquency actions contain 2 actions

  Scenario: Verify that reschedule with no parameters is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with no parameters with error containing "At least one of payment"

  Scenario: Verify that reschedule with minimumPayment but without minimumPaymentType is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with error containing "`minimumPaymentType` is mandatory when `minimumPayment` is provided" and the following parameters:
      | minimumPayment |
      | 5              |

  Scenario: Verify that reschedule with frequency but without frequencyType is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with error containing "`frequencyType` is mandatory when `frequency` is provided" and the following parameters:
      | frequency |
      | 30        |

  Scenario: Verify that reschedule with invalid minimumPaymentType is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with error containing "Invalid minimum payment type: INVALID" and the following parameters:
      | minimumPayment | minimumPaymentType |
      | 5              | INVALID            |

  Scenario: Verify that FLAT reschedule with COB generates periods with flat amount
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 150            | FLAT               | 30        | DAYS          |
    When Admin sets the business date to "01 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate          | toDate            | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026   | 30 January 2026   | 150            | 0          | 150               | false                 |
      | 2            | 31 January 2026   | 01 March 2026     | 150            | 0          | 150               | false                 |
      | 3            | 02 March 2026     | 31 March 2026     | 150            | 0          | 150               | false                 |
      | 4            | 01 April 2026     | 30 April 2026     | 150            | 0          | 150               |                       |

  Scenario: Verify that reschedule with FLAT minimumPaymentType uses flat amount
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 150            | FLAT               | 30        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 150            | 0          | 150               |                       |
    Then WC loan last delinquency action has the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 01 January 2026 | 150            | FLAT               | 30        | DAYS          |

  Scenario: Verify that reschedule with payment group only keeps original frequency
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 1              | PERCENTAGE         |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate              | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026     | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026  | 01 March 2026       | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026    | 31 March 2026       | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026    | 30 April 2026       | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026      | 30 May 2026         | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026      | 29 June 2026        | 100            | 0          | 100               | false                 |
      | 7            | 30 June 2026     | 29 July 2026        | 100            | 0          | 100               | false                 |
      | 8            | 30 July 2026     | 28 August 2026      | 100            | 0          | 100               |                       |

  Scenario: Verify that reschedule with frequency group only keeps original payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026  | 01 January 2026          | 10000           | 10000        | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 15        | DAYS          |
    When Admin sets the business date to "15 August 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate              | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026     | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026  | 01 March 2026       | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026    | 31 March 2026       | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026    | 30 April 2026       | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026      | 30 May 2026         | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026      | 29 June 2026        | 300            | 0          | 300               | false                 |
      | 7            | 30 June 2026     | 14 July 2026        | 300            | 0          | 300               | false                 |
      | 8            | 15 July 2026     | 29 July 2026        | 300            | 0          | 300               | false                 |
      | 9            | 30 July 2026     | 13 August 2026      | 300            | 0          | 300               | false                 |
      | 10           | 14 August 2026   | 28 August 2026      | 300            | 0          | 300               |                       |
