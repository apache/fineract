@WorkingCapital
@WorkingCapitalDelinquencyResetActionFeature
Feature: Working Capital Delinquency Reset Action

  @TestRailId:C89741
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC1: delinquency reset / reset undo has correct values on the delinquency range schedule and delinquency tags are lifted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
#    --- Initial state ---
    And Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-06-01  |              | D00            | 1              | 30             |
      | 4            | 2026-06-01  |              | D30            | 31             | 60             |
      | 3            | 2026-06-01  |              | D60            | 61             | 90             |
      | 2            | 2026-06-01  |              | D90            | 91             | 120            |
      | 1            | 2026-06-01  |              | D120           | 121            | 150            |
#    --- Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan last delinquency action has the following data:
      | action | startDate    | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 01 June 2026 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   |                |            |                   |                       |
      | 5            | 01 May 2026     | 30 May 2026     |                |            |                   |                       |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-06-01  | 2026-06-01   | D00            | 1              | 30             |
      | 4            | 2026-06-01  | 2026-06-01   | D30            | 31             | 60             |
      | 3            | 2026-06-01  | 2026-06-01   | D60            | 61             | 90             |
      | 2            | 2026-06-01  | 2026-06-01   | D90            | 91             | 120            |
      | 1            | 2026-06-01  | 2026-06-01   | D120           | 121            | 150            |
    When Admin sets the business date to "01 July 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 6            | 2026-06-30  |              | D00            | 1              | 30             |
      | 5            | 2026-06-01  | 2026-06-01   | D00            | 1              | 30             |
      | 4            | 2026-06-01  | 2026-06-01   | D30            | 31             | 60             |
      | 3            | 2026-06-01  | 2026-06-01   | D60            | 61             | 90             |
      | 2            | 2026-06-01  | 2026-06-01   | D90            | 91             | 120            |
      | 1            | 2026-06-01  | 2026-06-01   | D120           | 121            | 150            |
#   --- Delinquency reset undo ---
    When Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               | false                 |
      | 7            | 30 June 2026    | 29 July 2026    | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-07-01  |              | D30            | 31             | 60             |
      | 4            | 2026-07-01  |              | D60            | 61             | 90             |
      | 3            | 2026-07-01  |              | D90            | 91             | 120            |
      | 2            | 2026-07-01  |              | D120           | 121            | 150            |
      | 1            | 2026-07-01  |              | D150           | 151            | 180            |
      | 6            | 2026-06-30  |              | D00            | 1              | 30             |
      | 5            | 2026-06-01  | 2026-06-01   | D00            | 1              | 30             |
      | 4            | 2026-06-01  | 2026-06-01   | D30            | 31             | 60             |
      | 3            | 2026-06-01  | 2026-06-01   | D60            | 61             | 90             |
      | 2            | 2026-06-01  | 2026-06-01   | D90            | 91             | 120            |
      | 1            | 2026-06-01  | 2026-06-01   | D120           | 121            | 150            |
#    --- Close loan ---
    When Admin closes the Working Capital loan with a full repayment on "01 July 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 300        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 300        | 0                 | true                  |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 300        | 0                 | true                  |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 300        | 0                 | true                  |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 300        | 0                 | true                  |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 300        | 0                 | true                  |
      | 7            | 30 June 2026    | 29 July 2026    | 300            | 8200       | 0                 | true                  |

  @TestRailId:C89742
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC2: multiple delinquency reset / reset undo
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
#   --- Initial state ---
    And Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
#   --- Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan last delinquency action has the following data:
      | action | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 31 January 2026 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  | 2026-01-31   | D00            | 1              | 30             |
    When Admin sets the business date to "2 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-03-02  |              | D00            | 1              | 30             |
      | 1            | 2026-01-31  | 2026-01-31   | D00            | 1              | 30             |
#   --- 2nd Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 31 January 2026 |         |                |                    |           |               |
      | RESET  | 02 March 2026   |         |                |                    |           |               |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-03-02  | 2026-03-02   | D00            | 1              | 30             |
      | 1            | 2026-01-31  | 2026-01-31   | D00            | 1              | 30             |
    When Admin sets the business date to "2 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |
#    --- Delinquency reset undo ---
    When Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |
#   --- 3rd Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |
#    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "02 April 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 10000      | 0                 | true                  |

  @TestRailId:C89743
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC3: multiple delinquency reset - and undo reset for 2nd and 4th reset
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
#   --- Initial state ---
    And Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |
#   --- Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |
    When Admin sets the business date to "02 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               |                       |
#   --- 2nd Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    And Admin sets the business date to "02 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |
#   --- Delinquency reset undo ---
    When Admin creates Working Capital delinquency reset undo
#   --- Delinquency reset ---
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |
    When Admin sets the business date to "02 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               |                       |
#   --- 3rd Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   |                |            |                   |                       |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               |                       |
#   --- 2nd Delinquency reset undo ---
    When Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               |                       |
    #    --- Close loan ---
    When Admin closes the Working Capital loan with a full repayment on "02 May 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 300        | 0                 | true                  |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 9700       | 0                 | true                  |

  @TestRailId:C89744
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC4: 2 installments overdue and backdated payment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    #   --- Repayments ---
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    #   --- Delinquency reset ---
    When Admin sets the business date to "15 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 200            | 0          | 200               |                       |
        #   --- Backdated repayment ---
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "01 March 2026" with 100.0 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 200            | 0          | 200               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate     | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 15 April 2026 |         |                |                    |           |               |
#    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89745
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC5: overpaid and overdue
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#   --- Repayments ---
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 January 2026" with 300.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 February 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- Delinquency reset ---
    When Admin sets the business date to "15 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 200            | 0          | 200               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate     | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 15 April 2026 |         |                |                    |           |               |
    #    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89746
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC6: backdated payment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
 #   --- Repayments ---
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
 #   --- Backdated repayment ---
    When Admin sets the business date to "15 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
#   --- Delinquency reset ---
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate     | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 15 April 2026 |         |                |                    |           |               |
    #    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89747
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC7: multiple delinquency reset
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#   --- Repayments ---
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- Delinquency reset ---
    When Admin sets the business date to "15 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
#   --- Delinquency reset ---
    When Admin sets the business date to "04 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   |                |            |                   |                       |
      | 5            | 01 May 2026     | 30 May 2026     | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate     | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 15 April 2026 |         |                |                    |           |               |
      | RESET  | 04 May 2026   |         |                |                    |           |               |
    #    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "04 May 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89748
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC8: delinquency pause and reset
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#   --- Repayments ---
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 February 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- Delinquency pause ---
    When Admin sets the business date to "20 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "20 March 2026" and endDate "29 March 2026"
#   --- Delinquency reset ---
    When Admin sets the business date to "11 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 10 April 2026   |                |            |                   |                       |
      | 4            | 11 April 2026   | 10 May 2026     | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate     | endDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE  | 20 March 2026 | 29 March 2026 |                |                    |           |               |
      | RESET  | 11 April 2026 |               |                |                    |           |               |
    #    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "11 April 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:TODO_ADD_4
  Scenario: Verify multiple Reset Delinquency Action with start new period
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount

    When Admin sets the business date to "15 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |
    When Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate         | toDate           | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026  | 30 January 2026  |                |            |                   |                       |
      | 2            | 31 January 2026  | 14 February 2026 |                |            |                   |                       |
      | 3            | 15 February 2026 | 16 March 2026    | 300            | 0          | 300               |                       |
