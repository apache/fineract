@WorkingCapital
@WorkingCapitalDelinquencyDisableFeature
Feature: Working Capital Delinquency Disable

  @TestRailId:C89757
  Scenario: Verify working capital loan delinquency disable/enable - UC1: Disable stops delinquency evaluation and enable re-triggers a recompute as of the enable date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation while period 1 (01-01 - 01-03) is still open
    And Admin initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action  | startDate  | endDate |
      | DISABLE | 2026-01-02 |         |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    # Period 1 ends on 01-03 while the evaluation is disabled. The close-out still records minPaymentCriteriaMet,
    # but the delinquency data stays frozen: no delinquentAmount and no delinquentDays are derived while disabled.
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | false                 | null             | null           |
      | 2            | 2026-01-04 | 2026-01-06 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    # Enable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency enable with startDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action  | startDate  | endDate    |
      | DISABLE | 2026-01-02 | 2026-01-04 |
      | ENABLE  | 2026-01-05 |            |
    # The enable re-triggers the recompute, which now fills in the frozen delinquency data;
    # delinquentDays = businessDate - toDate
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 2              |
      | 2            | 2026-01-04 | 2026-01-06 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89758
  Scenario: Verify working capital loan delinquency disable/enable - UC2: Disable is allowed only once until it is reversed (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    # 2nd attempt for Disable delinquency evaluation
    Then Initiating a Working Capital loan delinquency disable with startDate "02 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                                                                         |
      | 400      | Delinquency evaluation is already disabled for this Working Capital loan. It must be enabled before disabling again. |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89759
  Scenario: Verify working capital loan delinquency disable/enable - UC3: Enable is rejected when there is no active disable (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#    Attempt for enable
    Then Initiating a Working Capital loan delinquency enable with startDate "02 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                                    |
      | 400      | There is no active delinquency disable to enable for this Working Capital loan. |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89760
  Scenario: Verify working capital loan delinquency disable/enable - UC4: Disable and enable are rejected when backdated or given an end date (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan delinquency disable with startDate "02 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                               |
      | 400      | Start date of a disable or enable action must be the current business date |
    And Initiating a Working Capital loan delinquency enable with startDate "02 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                               |
      | 400      | Start date of a disable or enable action must be the current business date |
    And Initiating a Working Capital loan delinquency disable with startDate "03 January 2026" and endDate "04 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                 |
      | 400      | End date must not be provided for a disable or enable action |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89761
  Scenario: Verify working capital loan delinquency disable/enable - UC5: While disabled the pause, resume and reschedule actions are not allowed (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    Then Initiating a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "03 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                                                                                             |
      | 400      | Delinquency pause, resume and reschedule actions are not allowed while delinquency evaluation is disabled for this Working Capital loan. |
    And Initiating a Working Capital loan delinquency resume with startDate "02 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                                                                                             |
      | 400      | Delinquency pause, resume and reschedule actions are not allowed while delinquency evaluation is disabled for this Working Capital loan. |
    And Admin fails to create WC delinquency reschedule action with minimumPayment 2 PERCENTAGE and frequency 15 DAYS with error containing "not allowed while delinquency evaluation is disabled"
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89762
  Scenario: Verify working capital loan delinquency disable/enable - UC6: Disable and enable work through the external-id endpoints
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency disable by external ID with startDate "02 January 2026"
    Then Working Capital loan delinquency action by external ID has the following data:
      | action  | startDate  | endDate |
      | DISABLE | 2026-01-02 |         |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Enable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency enable by external ID with startDate "04 January 2026"
    Then Working Capital loan delinquency action by external ID has the following data:
      | action  | startDate  | endDate    |
      | DISABLE | 2026-01-02 | 2026-01-03 |
      | ENABLE  | 2026-01-04 |            |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89763
  Scenario: Verify working capital loan delinquency disable/enable - UC7: Disable requires the CREATE_WC_DELINQUENCY_DISABLE permission (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates new user with "test" username, "test" role name and given permissions:
      | CREATE_WC_DELINQUENCY_ACTION |
    Then Created user with no CREATE_WC_DELINQUENCY_DISABLE permission gets an error when initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89764
  Scenario: Verify working capital loan delinquency disable/enable - UC8: Retrieval of delinquency actions requires the READ_WC_DELINQUENCY_ACTION permission (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    And Admin creates new user with "test" username, "test" role name and given permissions:
      | REPAYMENT_LOAN |
    Then Created user with no READ_WC_DELINQUENCY_ACTION permission gets an error when retrieving Working Capital loan delinquency actions
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89765
  Scenario: Verify working capital loan delinquency disable/enable - UC9: Repayment during disable does not touch delinquency data and is applied by enable
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation while period 1 (01-01 - 01-03) is still open
    And Admin initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Make a repayment while the evaluation is still disabled
    And Customer makes repayment on "05 January 2026" with 300.0 transaction amount on Working Capital loan
    # Enable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency enable with startDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action  | startDate  | endDate    |
      | DISABLE | 2026-01-02 | 2026-01-04 |
      | ENABLE  | 2026-01-05 |            |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 300.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89766
  Scenario: Verify working capital loan delinquency disable/enable - UC10: Repayment undo during disable does not touch delinquency data and the reversal is honored by enable
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation while period 1 (01-01 - 01-03) is still open
    And Admin initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Make a repayment while the evaluation is still disabled
    And Customer makes repayment on "05 January 2026" with 300.0 transaction amount on Working Capital loan
    # Undo the repayment
    And Customer undo "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan
    # Enable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency enable with startDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action  | startDate  | endDate    |
      | DISABLE | 2026-01-02 | 2026-01-04 |
      | ENABLE  | 2026-01-05 |            |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 2              |
      | 2            | 2026-01-04 | 2026-01-06 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89767
  Scenario: Verify working capital loan delinquency disable/enable - UC11: Disable lifts an active delinquency classification tag as of the disable date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1 runs 01-01 - 01-03, so the first delinquent day is 01-04 and the D00 tag is added there
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency disable with startDate "05 January 2026"
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  | 2026-01-05   | D00            | 1              | 30             |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89768
  Scenario: Verify working capital loan delinquency disable/enable - UC12: Enable on the same business day as disable is accepted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    # Enable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency enable with startDate "02 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action  | startDate  | endDate    |
      | DISABLE | 2026-01-02 | 2026-01-01 |
      | ENABLE  | 2026-01-02 |            |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 2              |
      | 2            | 2026-01-04 | 2026-01-06 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89769
  Scenario: Verify working capital loan delinquency disable/enable - UC13: A loan can be disabled again after a previous disable was enabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Disable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency disable with startDate "02 January 2026"
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Enable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency enable with startDate "04 January 2026"
    # Disable delinquency evaluation
    And Admin initiate a Working Capital loan delinquency disable with startDate "04 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action  | startDate  | endDate    |
      | DISABLE | 2026-01-02 | 2026-01-03 |
      | ENABLE  | 2026-01-04 |            |
      | DISABLE | 2026-01-04 |            |
    # Close the loan
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89770
  Scenario: Verify working capital loan delinquency disable/enable - UC14: Disable is rejected for a not yet active loan (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan delinquency disable with startDate "15 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                              |
      | 400      | Delinquency actions can be created only for active Working Capital loans. |
