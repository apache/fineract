@WorkingCapital
@WorkingCapitalDelinquencyRescheduleActionFeature @WCCOBFeature
Feature: Working Capital Delinquency Reschedule Action

  @TestRailId:C74495
  Scenario: Verify that reschedule changes minimumPayment only
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 100            | 0          | 100               | false                 |
      | 7            | 30 June 2026    | 29 July 2026    | 100            | 0          | 100               | false                 |
      | 8            | 30 July 2026    | 28 August 2026  | 100            | 0          | 100               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C74496
  Scenario: Verify that reschedule changes frequency only
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               | false                 |
      | 7            | 30 June 2026    | 14 July 2026    | 300            | 0          | 300               | false                 |
      | 8            | 15 July 2026    | 29 July 2026    | 300            | 0          | 300               | false                 |
      | 9            | 30 July 2026    | 13 August 2026  | 300            | 0          | 300               | false                 |
      | 10           | 14 August 2026  | 28 August 2026  | 300            | 0          | 300               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C74497
  Scenario: Verify that reschedule changes minimumPayment and frequency
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 200            | 0          | 200               | false                 |
      | 7            | 30 June 2026    | 14 July 2026    | 200            | 0          | 200               | false                 |
      | 8            | 15 July 2026    | 29 July 2026    | 200            | 0          | 200               | false                 |
      | 9            | 30 July 2026    | 13 August 2026  | 200            | 0          | 200               | false                 |
      | 10           | 14 August 2026  | 28 August 2026  | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C74498
  Scenario: Verify that the latest reschedule action wins
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 500            | 0          | 500               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 500            | 0          | 500               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 500            | 0          | 500               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 500            | 0          | 500               |                       |
    Then WC loan delinquency actions contain 2 actions
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C74499
  Scenario: Verify that reschedule on non-active loan and validation errors are rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 1 PERCENTAGE and frequency 30 DAYS with error containing "only for active Working Capital loans"
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 1 PERCENTAGE and frequency 30 DAYS with error containing "existing delinquency range schedule"
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 0 PERCENTAGE and frequency 30 DAYS with error containing "`minimumPayment` must be greater than 0"
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 1 PERCENTAGE and frequency 0 DAYS with error containing "`frequency` must be greater than 0"
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 1 PERCENTAGE and frequency 30 INVALID with error containing "Invalid frequency type: INVALID"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C74500
  Scenario: Verify that reschedule after a PAUSE extends rescheduled periods correctly
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate            | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026   | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 16 March 2026     | 300            | 0          | 300               | false                 |
      | 3            | 17 March 2026   | 15 April 2026     | 300            | 0          | 300               | false                 |
      | 4            | 16 April 2026   | 15 May 2026       | 300            | 0          | 300               | false                 |
      | 5            | 16 May 2026     | 14 June 2026      | 100            | 0          | 100               | false                 |
      | 6            | 15 June 2026    | 14 July 2026      | 100            | 0          | 100               | false                 |
      | 7            | 15 July 2026    | 13 August 2026    | 100            | 0          | 100               | false                 |
      | 8            | 14 August 2026  | 12 September 2026 | 100            | 0          | 100               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C74501
  Scenario: Verify that PAUSE after RESCHEDULE preserves rescheduled parameters
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 0          | 200               | false                 |
      | 4            | 01 April 2026   | 15 May 2026     | 200            | 0          | 200               | false                 |
      | 5            | 16 May 2026     | 14 June 2026    | 200            | 0          | 200               | false                 |
      | 6            | 15 June 2026    | 14 July 2026    | 200            | 0          | 200               | false                 |
      | 7            | 15 July 2026    | 13 August 2026  | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 July 2026"

  @TestRailId:C74502
  Scenario: Verify that reschedule spot-check reflects evaluated vs rescheduled period amounts
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C74503
  Scenario: Verify that reschedule on disbursement date creates a single period
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 5              | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 500            | 0          | 500               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C74504
  Scenario: Verify that reschedule on the first day of a new period updates subsequent periods
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 0          | 100               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 100            | 0          | 100               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 100            | 0          | 100               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C74505
  Scenario: Verify that retrieving delinquency actions returns RESCHEDULE action details
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C74506
  Scenario: Verify that COB generates new periods using rescheduled parameters over time
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate         | toDate           | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026  | 100            | 0          | 100               | false                 |
      | 2            | 31 January 2026  | 14 February 2026 | 100            | 0          | 100               | false                 |
      | 3            | 15 February 2026 | 01 March 2026    | 100            | 0          | 100               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "01 March 2026"

  @TestRailId:C74507
  Scenario: Verify that reschedule supports WEEKS frequency type
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 200            | 0          | 200               | false                 |
      | 7            | 30 June 2026    | 13 July 2026    | 200            | 0          | 200               | false                 |
      | 8            | 14 July 2026    | 27 July 2026    | 200            | 0          | 200               | false                 |
      | 9            | 28 July 2026    | 10 August 2026  | 200            | 0          | 200               | false                 |
      | 10           | 11 August 2026  | 24 August 2026  | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C74508
  Scenario: Verify that reschedule supports MONTHS frequency type
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 200            | 0          | 200               | false                 |
      | 7            | 30 June 2026    | 29 July 2026    | 200            | 0          | 200               | false                 |
      | 8            | 30 July 2026    | 29 August 2026  | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C74509
  Scenario: Verify that reschedule with negative minimumPayment is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with minimumPayment -5 PERCENTAGE and frequency 30 DAYS with error containing "`minimumPayment` must be greater than 0"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C74510
  Scenario: Verify that reschedule with negative frequency is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with minimumPayment 3 PERCENTAGE and frequency -1 DAYS with error containing "`frequency` must be greater than 0"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C74511
  Scenario: Verify that a reschedule minimumPayment over 100 percent is capped at the remaining balance
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 200            | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 10000          | 0          | 10000             |                       |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C74512
  Scenario: Verify that reschedule supports decimal minimumPayment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 2.5            | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 250            | 0          | 250               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C74513
  Scenario: Verify that two identical reschedules produce the same schedule as a single reschedule
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C76652
  Scenario: Verify that reschedule with no parameters is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with no parameters with error containing "At least one of payment"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C76653
  Scenario: Verify that reschedule with minimumPayment but without minimumPaymentType is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with error containing "`minimumPaymentType` is mandatory when `minimumPayment` is provided" and the following parameters:
      | minimumPayment |
      | 5              |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C76654
  Scenario: Verify that reschedule with frequency but without frequencyType is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with error containing "`frequencyType` is mandatory when `frequency` is provided" and the following parameters:
      | frequency |
      | 30        |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C76655
  Scenario: Verify that reschedule with invalid minimumPaymentType is rejected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with error containing "Invalid minimum payment type: INVALID" and the following parameters:
      | minimumPayment | minimumPaymentType |
      | 5              | INVALID            |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C76656
  Scenario: Verify that FLAT reschedule with COB generates periods with flat amount
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 150            | FLAT               | 30        | DAYS          |
    When Admin sets the business date to "01 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 150            | 0          | 150               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 150            | 0          | 150               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 150            | 0          | 150               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 150            | 0          | 150               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "01 April 2026"

  @TestRailId:C76657
  Scenario: Verify that reschedule with FLAT minimumPaymentType uses flat amount
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C85449
  Scenario: Verify delinquency and breach schedules after reschedule to 33.33 percent every 30 days, 2 Installments Overdue
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket and custom breach config:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount |
      | 60              | DAYS                | PERCENTAGE                  | 50           |
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "30 January 2026" with 200 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    And Customer makes repayment on "28 February 2026" with 100 transaction amount on Working Capital loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    When Admin creates WC breach reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 100        | 100               |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount |
      | 1            | 2026-01-01 | 2026-03-01 | 400              | 100               |
      | 2            | 2026-03-02 | 2026-04-30 | 266.64           | 266.64            |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan breach actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "04 May 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 200            | 200        | 0                 | true                  |
      | 2            | 200            | 100        | 100               | false                 |
      | 3            | 266.64         | 0          | 266.64            | false                 |
      | 4            | 266.64         | 0          | 266.64            | false                 |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | breach |
      | 1            | 2026-01-01 | 2026-03-01 | 400              | 100               | true   |
      | 2            | 2026-03-02 | 2026-04-30 | 266.64           | 266.64            | true   |
      | 3            | 2026-05-01 | 2026-05-30 | 266.64           | 266.64            | null   |
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"

  @TestRailId:C85450
  Scenario: Verify multiple delinquency and breach reschedules with history preserved
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket and custom breach config:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount |
      | 60              | DAYS                | PERCENTAGE                  | 50           |
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "30 January 2026" with 200 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    And Customer makes repayment on "28 February 2026" with 100 transaction amount on Working Capital loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    # First Reschedule
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    When Admin creates WC breach reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 100        | 100               |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount |
      | 1            | 2026-01-01 | 2026-03-01 | 400              | 100               |
      | 2            | 2026-03-02 | 2026-04-30 | 266.64           | 266.64            |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan breach actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "03 April 2026"
    When Admin runs inline COB job for Working Capital Loan
  # 2nd reschedule
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 20             | PERCENTAGE         | 30        | DAYS          |
    When Admin creates WC breach reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 20             | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 100        | 100               |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |
      | 4            | 01 April 2026   | 30 April 2026   | 160.0          | 0          | 160.0             |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount |
      | 1            | 2026-01-01 | 2026-03-01 | 400              | 100               |
      | 2            | 2026-03-02 | 2026-04-30 | 160.0            | 160.0             |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 03 April 2026 | 20             | PERCENTAGE         | 30        | DAYS          |
    Then WC loan breach actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 03 April 2026 | 20             | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "08 June 2026"
    When Admin runs inline COB job for Working Capital Loan

    # 3rd reschedule
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 10             | PERCENTAGE         | 30        | DAYS          |
    When Admin creates WC breach reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 10             | PERCENTAGE         | 30        | DAYS          |
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 100        | 100               |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |
      | 4            | 01 April 2026   | 30 April 2026   | 160.0          | 0          | 160.0             |
      | 5            | 01 May 2026     | 30 May 2026     | 160.0          | 0          | 160.0             |
      | 6            | 31 May 2026     | 29 June 2026    | 80.0           | 0          | 80.0              |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount |
      | 1            | 2026-01-01 | 2026-03-01 | 400              | 100               |
      | 2            | 2026-03-02 | 2026-04-30 | 160.0            | 160.0             |
      | 3            | 2026-05-01 | 2026-05-30 | 160.0            | 160.0             |
      | 4            | 2026-05-31 | 2026-06-29 | 80.0             | 80.0              |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 03 April 2026 | 20             | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 08 June 2026  | 10             | PERCENTAGE         | 30        | DAYS          |
    Then WC loan breach actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 03 April 2026 | 20             | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 08 June 2026  | 10             | PERCENTAGE         | 30        | DAYS          |
    Then Admin closes the Working Capital loan with a full repayment on "08 June 2026"

  @TestRailId:C85451
  Scenario: Verify multiple delinquency reschedules on the same date keep history and latest parameters
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 0          | 200               |                       |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |                       |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 20             | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 15 March 2026 | 20             | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 160            | 0          | 160               |                       |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 15             | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 15 March 2026 | 20             | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 15 March 2026 | 15             | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 120            | 0          | 120               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85452
  Scenario: Verify backdated repayment triggers delinquency schedule and evaluation reprocess
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 0          | 200               |                       |
    And Customer makes repayment on "15 March 2026" with 50 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 50         | 150               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 0          | 200               |                       |
    And Customer makes repayment on "10 March 2026" with 150 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85453
  Scenario: Verify repayment reversal triggers delinquency schedule and evaluation reprocess
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               |                       |
    And Customer makes repayment on "28 February 2026" with 200 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               |                       |
    When Customer undo "1"th working capital transaction made on "28 February 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "28 February 2026"

  @TestRailId:C85488
  Scenario: Verify reschedule right after disbursement keeps action history and does not shift period dates
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 1 MONTHS and minimumPayment 5 FLAT
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    # Reschedule on the disbursement date (after the initial COB) is allowed. The current period
    # keeps its boundaries from the original 1 MONTHS product config (01 Jan - 31 Jan) - a
    # reschedule only updates the current period's expected amount;
    # the new 30 DAYS frequency applies from the next generated period onward
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 25             | PERCENTAGE         | 30        | DAYS          |

    Then WC loan delinquency actions have the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 01 January 2026 | 25             | PERCENTAGE         | 30        | DAYS          |
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate         | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 31 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 01 February 2026 | 02 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 03 March 2026    | 01 April 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85489
  Scenario: Verify partial reschedules in 2 steps on the same day inherit parameters from each other
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 1 MONTHS and minimumPayment 5 FLAT
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 25             | PERCENTAGE         |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 30        | DAYS          |
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 01 January 2026 | 25             | PERCENTAGE         |           |               |
      | RESCHEDULE | 01 January 2026 |                |                    | 30        | DAYS          |
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate         | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 31 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 01 February 2026 | 02 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 03 March 2026    | 01 April 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85490
  Scenario: Verify partial reschedules in 2 steps on different days inherit parameters from each other
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 1 MONTHS and minimumPayment 5 FLAT
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 25             | PERCENTAGE         |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 30        | DAYS          |
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 02 January 2026 | 25             | PERCENTAGE         |           |               |
      | RESCHEDULE | 03 January 2026 |                |                    | 30        | DAYS          |
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate         | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 31 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 01 February 2026 | 02 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 03 March 2026    | 01 April 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85491
  Scenario: Verify that reschedule with payment group only keeps original frequency
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 100            | 0          | 100               | false                 |
      | 7            | 30 June 2026    | 29 July 2026    | 100            | 0          | 100               | false                 |
      | 8            | 30 July 2026    | 28 August 2026  | 100            | 0          | 100               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C85492
  Scenario: Verify that reschedule with frequency group only keeps original payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
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
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               | false                 |
      | 7            | 30 June 2026    | 14 July 2026    | 300            | 0          | 300               | false                 |
      | 8            | 15 July 2026    | 29 July 2026    | 300            | 0          | 300               | false                 |
      | 9            | 30 July 2026    | 13 August 2026  | 300            | 0          | 300               | false                 |
      | 10           | 14 August 2026  | 28 August 2026  | 300            | 0          | 300               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 August 2026"

  @TestRailId:C85574
  Scenario: Verify that three partial reschedules inherit each parameter group from the latest action that set it
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 1 MONTHS and minimumPayment 5 FLAT
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 25             | PERCENTAGE         |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 30        | DAYS          |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 33.33          | PERCENTAGE         |
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 02 January 2026 | 25             | PERCENTAGE         |           |               |
      | RESCHEDULE | 03 January 2026 |                |                    | 30        | DAYS          |
      | RESCHEDULE | 04 January 2026 | 33.33          | PERCENTAGE         |           |               |
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate         | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 31 January 2026 | 266.64         | 0          | 266.64            | false                 |
      | 2            | 01 February 2026 | 02 March 2026   | 266.64         | 0          | 266.64            | false                 |
      | 3            | 03 March 2026    | 01 April 2026   | 266.64         | 0          | 266.64            |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85493
  Scenario: Verify backdated repayment after reschedule is re-evaluated against rescheduled periods
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |                       |
    And Customer makes repayment on "15 March 2026" with 50 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 200            | 50         | 150               | false                 |
      | 2            | 200            | 0          | 200               | false                 |
      | 3            | 266.64         | 0          | 266.64            |                       |
    And Customer makes repayment on "25 February 2026" with 200 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  | 0                | 0              |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 50         | 150               | false                 | 150              | 14             |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |                       |                  |                |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85494
  Scenario: Verify repayment reversal after reschedule reprocesses against rescheduled periods
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "30 January 2026" with 200 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "28 February 2026" with 100 transaction amount on Working Capital loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 100        | 100               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |                       |
    When Customer undo "1"th working capital transaction made on "28 February 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  | 0                | 0              |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 | 200              | 14             |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |                       |                  |                |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85495
  Scenario: Verify reschedule expected amount base when loan has discount
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 100      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and "100" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount and "100" discount amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 225            | 0          | 225               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 225            | 0          | 225               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 225            | 0          | 225               |                       |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 225            | 0          | 225               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 225            | 0          | 225               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 299.97         | 0          | 299.97            |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85496
  Scenario: Verify delinquency tag history is preserved and lifted correctly across reschedule reprocess
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    # Reschedule triggers a full reset-and-replay; the existing D00 tag must not be duplicated
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    And Customer makes repayment on "15 February 2026" with 1 transaction amount on Working Capital loan
    And Customer makes repayment on "20 January 2026" with 3 transaction amount on Working Capital loan
    # Backdated repayment cures period 1 -> reprocess lifts the D00 tag as of the business date
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  | 2026-02-15   | D00            | 1              | 30             |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 3              | 3          | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 33.33          | 1          | 32.33             |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 February 2026"

  @TestRailId:C85497
  Scenario: Verify delinquency reschedule has no impact on breach schedule and breach actions
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket and custom breach config:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount |
      | 60              | DAYS                | PERCENTAGE                  | 50           |
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "30 January 2026" with 200 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    And Customer makes repayment on "28 February 2026" with 100 transaction amount on Working Capital loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount |
      | 1            | 2026-01-01 | 2026-03-01 | 400              | 100               |
      | 2            | 2026-03-02 | 2026-04-30 | 400              | 400               |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 100        | 100               |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |
    # Breach schedule and breach actions must be untouched by a delinquency-only reschedule
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount |
      | 1            | 2026-01-01 | 2026-03-01 | 400              | 100               |
      | 2            | 2026-03-02 | 2026-04-30 | 400              | 400               |
    Then WC loan breach actions have the following data:
      | action | startDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85498
  Scenario: Verify backdated goodwill credit triggers delinquency schedule and evaluation reprocess
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "15 March 2026" with 50 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 50         | 150               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 0          | 200               |                       |
    And Customer makes "GOODWILL_CREDIT" transaction on "10 March 2026" with 150 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85499
  Scenario: Verify backdated repayment crossing multiple open past periods is allocated oldest first
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "15 March 2026" with 50 transaction amount on Working Capital loan
    And Customer makes repayment on "10 March 2026" with 400 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 200        | 0                 | true                  |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 50         | 150               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C85500
  Scenario: Verify pause-shifted period boundaries survive reschedule and backdated repayment reprocess
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 March 2026" and endDate "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "20 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    And Customer makes repayment on "20 March 2026" with 50 transaction amount on Working Capital loan
    And Customer makes repayment on "10 February 2026" with 350 transaction amount on Working Capital loan
    Then WC loan has both PAUSE and RESCHEDULE delinquency actions
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 16 March 2026   | 200            | 200        | 0                 | true                  |
      | 3            | 17 March 2026   | 15 April 2026   | 266.64         | 0          | 266.64            |                       |
    Then Admin closes the Working Capital loan with a full repayment on "20 March 2026"

  @TestRailId:C85501
  Scenario: Verify same date reschedules changing frequency apply the latest frequency to new periods
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 15        | DAYS          |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 30        | DAYS          |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         | 15        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            |                       |
    When Admin sets the business date to "17 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 266.64         | 0          | 266.64            | false                 |
      | 4            | 01 April 2026   | 15 April 2026   | 266.64         | 0          | 266.64            | false                 |
      | 5            | 16 April 2026   | 30 April 2026   | 266.64         | 0          | 266.64            |                       |
    Then Admin closes the Working Capital loan with a full repayment on "17 April 2026"

  @TestRailId:C85502
  Scenario: Verify reschedule does not change expected amount of an already met current period
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "30 January 2026" with 200 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "28 February 2026" with 200 transaction amount on Working Capital loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "15 March 2026" with 200 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 200        | 0                 | true                  |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 200        | 0                 | true                  |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 33.33          | PERCENTAGE         | 30        | DAYS          |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 200        | 0                 | true                  |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 200        | 0                 | true                  |
    When Admin sets the business date to "01 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate      | toDate        | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 4            | 01 April 2026 | 30 April 2026 | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "01 April 2026"

  @TestRailId:C85575
  Scenario: Verify that a delinquency period expects only the remaining balance when it is below the minimum payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 700 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 0          | 100               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "28 February 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 100        | 0                 | true                  |

  @TestRailId:C85576
  Scenario: Verify that a reschedule-based minimum payment is capped at the remaining balance
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 700 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 500            | FLAT               |
    Then WC loan delinquency actions have the following data:
      | action     | startDate        | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 28 February 2026 | 500            | FLAT               |           |               |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 0          | 100               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "28 February 2026"

  @TestRailId:C85577
  Scenario: Verify that undoing a repayment restores the uncapped minimum payment expectation
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 700 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 0          | 100               |                       |
    When Customer undo "1"th working capital transaction made on "15 January 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "28 February 2026"

  @TestRailId:C85578
  Scenario: Verify that a past-due period beyond the remaining balance is met once the balance is fully paid
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 0          | 200               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 200            | 0          | 200               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 200            | 0          | 200               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "15 June 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 200        | 0                 | true                  |
      | 3            | 02 March 2026   | 31 March 2026   | 200            | 200        | 0                 | true                  |
      | 4            | 01 April 2026   | 30 April 2026   | 200            | 200        | 0                 | true                  |
      | 5            | 01 May 2026     | 30 May 2026     | 0              | 0          | 0                 | true                  |
      | 6            | 31 May 2026     | 29 June 2026    | 0              | 0          | 0                 | true                  |

  @TestRailId:C85579
  Scenario: Verify that the remaining balance cap on a discounted loan uses the principal plus discount base
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 100      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and "100" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount and "100" discount amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 750 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 225            | 750        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 150            | 0          | 150               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "28 February 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 225            | 750        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 150            | 150        | 0                 | true                  |

  @TestRailId:C85580
  Scenario: Verify that a backdated repayment reprocess recalculates the remaining balance cap
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "28 February 2026" with 50 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 50         | 150               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               |                       |
    And Customer makes repayment on "15 January 2026" with 700 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 50         | 50                |                       |
    Then Admin closes the Working Capital loan with a full repayment on "28 February 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 100        | 0                 | true                  |

  @TestRailId:C85581
  Scenario: Verify that adding a discount after disbursement raises the remaining balance cap
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 200            | PERCENTAGE         |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 800            | 0          | 800               |                       |
    And Admin adds Discount fee with "100" amount on Working Capital loan account for last disbursement
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 900            | 0          | 900               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 900            | 900        | 0                 | true                  |

  @TestRailId:C85582
  Scenario: Verify that a discount adjustment and its undo re-derive the remaining balance cap
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 100      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and "100" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount and "100" discount amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 700 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 225            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               |                       |
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    And Admin adds Discount fee adjustment with "100" amount on transaction date "28 February 2026" on Working Capital loan account for last discount
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 225            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 0          | 100               |                       |
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 225            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "28 February 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 225            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 200        | 0                 | true                  |

  @TestRailId:C85583
  Scenario: Verify partial reschedules inherit the payment group across a minimum payment type switch
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 33.33          | PERCENTAGE         |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | expectedAmount | minPaymentCriteriaMet |
      | 3            | 266.64         |                       |
    When Admin sets the business date to "16 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 150            | FLAT               |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | expectedAmount | minPaymentCriteriaMet |
      | 3            | 150            |                       |
    When Admin sets the business date to "17 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    # Frequency-only reschedule: the payment group must inherit the FLAT 150 from the latest action
    # that set it - not the 33.33 percent from the first action and not the product's 25 percent
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 15        | DAYS          |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         |           |               |
      | RESCHEDULE | 16 March 2026 | 150            | FLAT               |           |               |
      | RESCHEDULE | 17 March 2026 |                |                    | 15        | DAYS          |
    When Admin sets the business date to "17 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 150            | 0          | 150               | false                 |
      | 4            | 01 April 2026   | 15 April 2026   | 150            | 0          | 150               | false                 |
      | 5            | 16 April 2026   | 30 April 2026   | 150            | 0          | 150               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "17 April 2026"

  @TestRailId:C85584
  Scenario: Verify a delinquency tag is lifted when the remaining balance cap marks skipped periods as met
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # Period 1 (expected 3 = 3 percent of 100) is 1 day past due - tagged D00
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 1 transaction amount on Working Capital loan
    # The backdated repayment fully pays the loan; reprocess caps period 2 to the zero remaining
    # balance, marks it met, and the classification lifts the D00 tag from period 1
    And Customer makes repayment on "10 January 2026" with 99 transaction amount on Working Capital loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  | 2026-02-15   | D00            | 1              | 30             |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 3              | 99         | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 1              | 1          | 0                 | true                  |

  @TestRailId:C85585
  Scenario: Verify delinquent amount and days are reported from the capped expectation
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 700 transaction amount on Working Capital loan
    When Admin sets the business date to "15 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    # Remaining balance is 100, so the past-due periods expect (and report as delinquent) 100 - not
    # the uncapped 200 minimum payment
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 700        | 0                 | true                  | 0                | 0              |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 0          | 100               | false                 | 100              | 45             |
      | 3            | 02 March 2026   | 31 March 2026   | 100            | 0          | 100               | false                 | 100              | 15             |
      | 4            | 01 April 2026   | 30 April 2026   | 100            | 0          | 100               |                       |                  |                |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 200            | 700        | 0                 | true                  |
      | 2            | 100            | 100        | 0                 | true                  |
      | 3            | 0              | 0          | 0                 | true                  |
      | 4            | 0              | 0          | 0                 | true                  |

  @TestRailId:C85586
  Scenario: Verify pause-shifted period boundaries survive a partial frequency-only reschedule
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "01 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 March 2026" and endDate "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "20 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    # Frequency-only reschedule: payment group falls back to the product's 25 percent (no prior
    # reschedule); the pause-shifted boundaries of periods 2 and 3 must survive the rebuild
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 15        | DAYS          |
    Then WC loan has both PAUSE and RESCHEDULE delinquency actions
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE      | 01 March 2026 |                |                    |           |               |
      | RESCHEDULE | 20 March 2026 |                |                    | 15        | DAYS          |
    When Admin sets the business date to "20 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 16 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 17 March 2026   | 15 April 2026   | 200            | 0          | 200               | false                 |
      | 4            | 16 April 2026   | 30 April 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "20 April 2026"

  @TestRailId:C85587
  Scenario: Verify the later of two same-day payment-only reschedules wins within the payment group
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "15 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 33.33          | PERCENTAGE         |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 150            | FLAT               |
    Then WC loan delinquency actions have the following data:
      | action     | startDate     | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 15 March 2026 | 33.33          | PERCENTAGE         |           |               |
      | RESCHEDULE | 15 March 2026 | 150            | FLAT               |           |               |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 150            | 0          | 150               |                       |
    When Admin sets the business date to "17 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 4            | 01 April 2026   | 30 April 2026   | 150            | 0          | 150               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "17 April 2026"
