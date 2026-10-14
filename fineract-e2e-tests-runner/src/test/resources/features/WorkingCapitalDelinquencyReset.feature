@WorkingCapital
@WorkingCapitalDelinquencyResetActionFeature
Feature: Working Capital Delinquency Reset Action

  @TestRailId:C89741
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC1: delinquency reset / reset undo has correct values on the delinquency range schedule and delinquency tags are lifted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
#    --- Initial state ---
    And Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 0          | 300               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 300            | 0          | 300               | false                 |
      | 6            | 16 January 2026 | 18 January 2026 | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-01-16  |              | D00            | 1              | 30             |
      | 4            | 2026-01-16  |              | D00            | 1              | 30             |
      | 3            | 2026-01-16  |              | D00            | 1              | 30             |
      | 2            | 2026-01-16  |              | D00            | 1              | 30             |
      | 1            | 2026-01-16  |              | D00            | 1              | 30             |
#    --- Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan last delinquency action has the following data:
      | action | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 16 January 2026 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 |                |            |                   |                       |
      | 5            | 13 January 2026 | 15 January 2026 |                |            |                   |                       |
      | 6            | 16 January 2026 | 18 January 2026 | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 4            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 3            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 2            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 1            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 6            | 2026-01-19  |              | D00            | 1              | 30             |
      | 5            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 4            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 3            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 2            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 1            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
#   --- Delinquency reset undo ---
    When Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 0          | 300               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 300            | 0          | 300               | false                 |
      | 6            | 16 January 2026 | 18 January 2026 | 300            | 0          | 300               | false                 |
      | 7            | 19 January 2026 | 21 January 2026 | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-01-20  |              | D00            | 1              | 30             |
      | 4            | 2026-01-20  |              | D00            | 1              | 30             |
      | 3            | 2026-01-20  |              | D00            | 1              | 30             |
      | 2            | 2026-01-20  |              | D00            | 1              | 30             |
      | 1            | 2026-01-20  |              | D00            | 1              | 30             |
      | 6            | 2026-01-19  |              | D00            | 1              | 30             |
      | 5            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 4            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 3            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 2            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
      | 1            | 2026-01-16  | 2026-01-16   | D00            | 1              | 30             |
#    --- Close loan ---
    When Admin closes the Working Capital loan with a full repayment on "20 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 300            | 300        | 0                 | true                  |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 300        | 0                 | true                  |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 300        | 0                 | true                  |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 300        | 0                 | true                  |
      | 5            | 13 January 2026 | 15 January 2026 | 300            | 300        | 0                 | true                  |
      | 6            | 16 January 2026 | 18 January 2026 | 300            | 300        | 0                 | true                  |
      | 7            | 19 January 2026 | 21 January 2026 | 300            | 8200       | 0                 | true                  |

  @TestRailId:C89742
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC2: multiple delinquency reset / reset undo
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
#   --- Initial state ---
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |
#   --- Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan last delinquency action has the following data:
      | action | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 04 January 2026 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  | 2026-01-04   | D00            | 1              | 30             |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-01-07  |              | D00            | 1              | 30             |
      | 1            | 2026-01-04  | 2026-01-04   | D00            | 1              | 30             |
#   --- 2nd Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 04 January 2026 |         |                |                    |           |               |
      | RESET  | 07 January 2026 |         |                |                    |           |               |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-01-07  | 2026-01-07   | D00            | 1              | 30             |
      | 1            | 2026-01-04  | 2026-01-04   | D00            | 1              | 30             |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 0          | 300               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               |                       |
#    --- Delinquency reset undo ---
    When Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 0          | 300               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               |                       |
#   --- 3rd Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               |                       |
#    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 10000      | 0                 | true                  |

  @TestRailId:C89743
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC3: multiple delinquency reset - and undo reset for 2nd and 4th reset
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
#   --- Initial state ---
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               |                       |
#   --- Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               |                       |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 0          | 300               |                       |
#   --- 2nd Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 | 300            | 0          | 300               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               |                       |
#   --- Delinquency reset undo ---
    When Admin creates Working Capital delinquency reset undo
#   --- Delinquency reset ---
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               |                       |
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 300            | 0          | 300               |                       |
#   --- 3rd Delinquency reset ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 |                |            |                   |                       |
      | 5            | 13 January 2026 | 15 January 2026 | 300            | 0          | 300               |                       |
#   --- 2nd Delinquency reset undo ---
    When Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 0          | 300               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 300            | 0          | 300               |                       |
    #    --- Close loan ---
    When Admin closes the Working Capital loan with a full repayment on "13 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 300            | 300        | 0                 | true                  |
      | 5            | 13 January 2026 | 15 January 2026 | 300            | 9700       | 0                 | true                  |

  @TestRailId:C89744
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC4: 2 installments overdue and backdated payment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    #   --- Repayments ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    #   --- Delinquency reset ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
        #   --- Backdated repayment ---
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "06 January 2026" with 100.0 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 11 January 2026 |         |                |                    |           |               |
#    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "14 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89745
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC5: overpaid and overdue
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#   --- Repayments ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 300.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- Delinquency reset ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 11 January 2026 |         |                |                    |           |               |
    #    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "14 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89746
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC6: backdated payment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
 #   --- Repayments ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
 #   --- Backdated repayment ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "03 January 2026" with 100.0 transaction amount on Working Capital loan
#   --- Delinquency reset ---
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 11 January 2026 |         |                |                    |           |               |
    #    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89747
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC7: multiple delinquency reset
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#   --- Repayments ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- Delinquency reset ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
#   --- Delinquency reset ---
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 |                |            |                   |                       |
      | 5            | 13 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 11 January 2026 |         |                |                    |           |               |
      | RESET  | 14 January 2026 |         |                |                    |           |               |
    #    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "14 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89748
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC8: delinquency pause and reset
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#   --- Repayments ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
#   --- Delinquency pause ---
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "07 January 2026" and endDate "08 January 2026"
#   --- Delinquency reset ---
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 11 January 2026 |                |            |                   |                       |
      | 4            | 12 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
    And WC loan delinquency actions have the following data:
      | action | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE  | 07 January 2026 | 08 January 2026 |                |                    |           |               |
      | RESET  | 12 January 2026 |                 |                |                    |           |               |
    #    --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "12 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C89749
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC9: delinquency reset with start new period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
#  --- Initial state ---
    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               |                       |
#    --- Delinquency reset with start new period ---
    When Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 04 January 2026 |                |            |                   |                       |
      | 3            | 05 January 2026 | 07 January 2026 | 300            | 0          | 300               |                       |

  @TestRailId:C89750
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC10: 2 installments overdue and backdated payments with start new period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#  --- Backdated payments covering period 1 fully and period 2 partially ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 100        | 100               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
#    --- Delinquency reset with start new period at 11 January 2026 ---
    When Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 13 January 2026 | 200            | 0          | 200               | false                 |
      | 6            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               |                       |
#    --- Backdated repayment applied against the still-open period (5) created after reset ---
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "16 January 2026" with 150.0 transaction amount on Working Capital loan
    When Admin sets the business date to "17 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 13 January 2026 | 200            | 150        | 50                | false                 |
      | 6            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               | false                 |
      | 7            | 17 January 2026 | 19 January 2026 | 200            | 0          | 200               |                       |
#    --- Backdated payment into an already reset period has no effect on the delinquency schedule ---
    And Customer makes repayment on "06 January 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "18 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 13 January 2026 | 200            | 150        | 50                | false                 |
      | 6            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               | false                 |
      | 7            | 17 January 2026 | 19 January 2026 | 200            | 0          | 200               |                       |

  @TestRailId:C89751
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC11: overpaid and overdue installments with start new period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#  --- Period 1 overpaid, period 2 partially paid ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 300.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 300        | 0                 | true                  |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 50         | 150               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
#    --- Delinquency reset with start new period at 11 January 2026 ---
    When Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 13 January 2026 | 200            | 0          | 200               | false                 |
      | 6            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "18 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 13 January 2026 | 200            | 0          | 200               | false                 |
      | 6            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               | false                 |
      | 7            | 17 January 2026 | 19 January 2026 | 200            | 0          | 200               |                       |

  @TestRailId:C89752
  Scenario: Verify Working Capital delinquency reset / delinquency reset undo - UC12: delinquency pause followed by reset with start new period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#  --- Period 1 fully paid, period 2 partially paid ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 100        | 100               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 200            | 0          | 200               |                       |
#    --- 2-day delinquency pause starting on the current business date ---
    And Admin initiate a Working Capital loan delinquency pause with startDate "07 January 2026" and endDate "08 January 2026"
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 100        | 100               | false                 |
      | 3            | 07 January 2026 | 11 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 12 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
#    --- Delinquency reset with start new period at 13 January 2026 ---
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 11 January 2026 |                |            |                   |                       |
      | 4            | 12 January 2026 | 12 January 2026 |                |            |                   |                       |
      | 5            | 13 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |

  @TestRailId:C106672
  Scenario: Verify Working Capital delinquency reset - UC13: backdated delinquency pause before an active reset is rejected, pause on the reset date is accepted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
#   --- Delinquency reset on 12 January 2026 ---
    When Admin creates a Working Capital delinquency reset
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 12 January 2026 |         |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
#   --- Backdated pause starting before the reset date is rejected ---
    Then Initiating a Working Capital loan delinquency pause with startDate "10 January 2026" and endDate "11 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                                        |
      | 400      | Delinquency pause cannot start before the latest delinquency reset date: 2026-01-12 |
    And WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 12 January 2026 |         |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
#   --- Pause starting on the reset date is accepted and extends the current period ---
    When Admin initiate a Working Capital loan delinquency pause with startDate "12 January 2026" and endDate "13 January 2026"
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 12 January 2026 |                 |                |                    |           |               |
      | PAUSE  | 12 January 2026 | 13 January 2026 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "12 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C106673
  Scenario: Verify Working Capital delinquency reset - UC14: backdated delinquency pause before an UNDONE reset is accepted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- Delinquency reset on 12 January 2026, undone on 13 January 2026 ---
    And Admin creates a Working Capital delinquency reset
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
    When Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET      | 12 January 2026 | 13 January 2026 |                |                    |           |               |
      | UNDO_RESET | 13 January 2026 |                 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Backdated pause before the (undone) reset date must be accepted: no active reset exists any more ---
    When Admin initiate a Working Capital loan delinquency pause with startDate "10 January 2026" and endDate "11 January 2026"
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET      | 12 January 2026 | 13 January 2026 |                |                    |           |               |
      | UNDO_RESET | 13 January 2026 |                 |                |                    |           |               |
      | PAUSE      | 10 January 2026 | 11 January 2026 |                |                    |           |               |
#   NOTE: table below is the expected post-fix shape (periods from 10 Jan shifted by the 2-day pause);
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 10 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
      | 5            | 15 January 2026 | 17 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "13 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C106674
  Scenario: Verify Working Capital delinquency reset - UC15: multiple resets - backdated pause is validated against the latest active reset only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
#   --- First reset on 12 January 2026 ---
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
#   --- Second reset on 15 January 2026 ---
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 12 January 2026 |         |                |                    |           |               |
      | RESET  | 15 January 2026 |         |                |                    |           |               |
#   --- Pause between the two resets is rejected against the latest active reset (15 January) ---
    Then Initiating a Working Capital loan delinquency pause with startDate "13 January 2026" and endDate "14 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                                        |
      | 400      | Delinquency pause cannot start before the latest delinquency reset date: 2026-01-15 |
#   --- Undo the 15 January reset; the 12 January reset stays active ---
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET      | 12 January 2026 |                 |                |                    |           |               |
      | RESET      | 15 January 2026 | 16 January 2026 |                |                    |           |               |
      | UNDO_RESET | 16 January 2026 |                 |                |                    |           |               |
#   --- Same pause is now accepted (only the undone reset was after it) ---
    When Admin initiate a Working Capital loan delinquency pause with startDate "13 January 2026" and endDate "14 January 2026"
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET      | 12 January 2026 |                 |                |                    |           |               |
      | RESET      | 15 January 2026 | 16 January 2026 |                |                    |           |               |
      | UNDO_RESET | 16 January 2026 |                 |                |                    |           |               |
      | PAUSE      | 13 January 2026 | 14 January 2026 |                |                    |           |               |
#   --- Pause before the still-active 12 January reset is rejected ---
    Then Initiating a Working Capital loan delinquency pause with startDate "10 January 2026" and endDate "11 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                                        |
      | 400      | Delinquency pause cannot start before the latest delinquency reset date: 2026-01-12 |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "16 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C106675
  Scenario: Verify Working Capital delinquency reset - UC16: delinquency pause without startDate on a loan with a reset returns validation error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then Initiating a Working Capital loan delinquency pause without startDate and endDate "11 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                               |
      | 400      | validation.msg.workingCapitalLoanDelinquencyAction.startDate.cannot.be.blank |
    And WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 12 January 2026 |         |                |                    |           |               |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "12 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115198
  Scenario: Verify Working Capital delinquency reset - UC17: reset with start new period inside an active pause starts the new period on the reset date and keeps a repayment dated inside the pause
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 200            | 0          | 200               |                       |
#   --- 3-day delinquency pause starting on the current business date extends the current period ---
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "10 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
#   --- Reset with start new period while the pause is active: the new period starts on the reset date and only counts the pause from there ---
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE  | 08 January 2026 | 10 January 2026 |                |                    |           |               |
      | RESET  | 09 January 2026 |                 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
#   --- A repayment dated inside the pause lands in the restarted period ---
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 200.0 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 200        | 0                 | true                  |
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 200        | 0                 | true                  |
      | 5            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "14 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115199
  Scenario: Verify Working Capital delinquency reset - UC18: resume after a reset with start new period inside the pause shortens the restarted period only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- 5-day delinquency pause starting on the current business date ---
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "12 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
#   --- Reset with start new period while the pause is active ---
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Resume two days early: the cut period keeps its end, the restarted period keeps its start and loses the two days from its end ---
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume with startDate "10 January 2026"
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE  | 08 January 2026 | 12 January 2026 |                |                    |           |               |
      | RESET  | 09 January 2026 |                 |                |                    |           |               |
      | RESUME | 10 January 2026 |                 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "14 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115200
  Scenario: Verify Working Capital delinquency reset - UC19: reschedule after a reset with start new period inside the pause counts the pause from the reset date only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "10 January 2026"
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
#   --- Reschedule to a 4-day frequency: 09-12 Jan extended by the pause days 09 and 10 Jan only ---
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 25             | PERCENTAGE         | 4         | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "09 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115201
  Scenario: Verify Working Capital delinquency reset - UC20: resume after an undone reset with start new period inside the pause keeps the restarted period start
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- 5-day delinquency pause starting on the current business date ---
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "12 January 2026"
#   --- Reset with start new period while the pause is active ---
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Undo the reset: the flags are cleared and the earlier periods are evaluated again, but the cut stays ---
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 08 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Resume two days early: the restarted period keeps its start on the undone reset date, no overlap with period 3 ---
    And Admin initiate a Working Capital loan delinquency resume with startDate "10 January 2026"
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE      | 08 January 2026 | 12 January 2026 |                |                    |           |               |
      | RESET      | 09 January 2026 | 10 January 2026 |                |                    |           |               |
      | UNDO_RESET | 10 January 2026 |                 |                |                    |           |               |
      | RESUME     | 10 January 2026 |                 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 08 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 08 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "14 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115202
  Scenario: Verify Working Capital delinquency reset - UC21: a later pause after a reset with start new period inside a pause extends only the restarted period and leaves the cut period and its lifted tags alone
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "10 January 2026"
#   --- Reset with start new period inside the pause ---
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
#   --- A second pause after the first one ended extends the restarted period from its end only; the cut period keeps its dates ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "11 January 2026" and endDate "12 January 2026"
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE  | 08 January 2026 | 10 January 2026 |                |                    |           |               |
      | RESET  | 09 January 2026 |                 |                |                    |           |               |
      | PAUSE  | 11 January 2026 | 12 January 2026 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- A repayment dated inside the second pause lands in the restarted period ---
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 200        | 0                 | true                  |
      | 5            | 16 January 2026 | 18 January 2026 | 200            | 0          | 200               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-01-08  | 2026-01-09   | D00            | 1              | 30             |
      | 1            | 2026-01-08  | 2026-01-09   | D00            | 1              | 30             |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "16 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115203
  Scenario: Verify Working Capital delinquency reset - UC22: reschedule and resume after an undone reset with start new period inside the pause count the pause from the reset date only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "12 January 2026"
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Undo the reset: the cut stays, the earlier periods are evaluated again ---
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates Working Capital delinquency reset undo
#   --- Reschedule to a 4-day frequency: 09-12 Jan extended by the pause days 09-12 Jan only, not by 08 Jan ---
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 25             | PERCENTAGE         | 4         | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 08 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 09 January 2026 | 16 January 2026 | 200            | 0          | 200               |                       |
#   --- Resume one day early: the restarted period keeps its start on the undone reset date and loses one day from its end ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume with startDate "11 January 2026"
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE      | 08 January 2026 | 12 January 2026 |                |                    |           |               |
      | RESET      | 09 January 2026 | 10 January 2026 |                |                    |           |               |
      | UNDO_RESET | 10 January 2026 |                 |                |                    |           |               |
      | RESCHEDULE | 10 January 2026 |                 | 25             | PERCENTAGE         | 4         | DAYS          |
      | RESUME     | 11 January 2026 |                 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 08 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115204
  Scenario: Verify Working Capital delinquency reset - UC23: a repayment dated inside the pause after a reset with start new period is kept by the restarted period and re-allocated oldest-first when the reset is undone
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "12 January 2026"
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
#   --- Repayment dated inside the pause, one day after the reset ---
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 200.0 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 200        | 0                 | true                  |
#   --- Undo the reset: the replay keeps the repayment and cascades it to the oldest open period ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 08 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 08 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 09 January 2026 | 15 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 16 January 2026 | 18 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "16 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115205
  Scenario: Verify Working Capital delinquency reset - UC24: reset with start new period on the last day of the pause counts that single pause day only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "10 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 09 January 2026 |                |            |                   |                       |
      | 4            | 10 January 2026 | 13 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 14 January 2026 | 16 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "14 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115206
  Scenario: Verify Working Capital delinquency reset - UC25: reset with start new period on the first day of the pause counts the whole pause and a resume shortens the restarted period from its end
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "10 January 2026"
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 07 January 2026 |                |            |                   |                       |
      | 4            | 08 January 2026 | 13 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume with startDate "09 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 07 January 2026 |                |            |                   |                       |
      | 4            | 08 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 07 January 2026 |                |            |                   |                       |
      | 4            | 08 January 2026 | 12 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 13 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "13 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115207
  Scenario: Verify Working Capital delinquency reset - UC26: two resets with start new period inside the same pause keep both boundaries through a resume and an undo of the second reset
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "12 January 2026"
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
#   --- Second reset with start new period two days later, still inside the pause ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Resume on the second reset date: only the latest restarted period shortens, from its end ---
    And Admin initiate a Working Capital loan delinquency resume with startDate "11 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 10 January 2026 |                |            |                   |                       |
      | 5            | 11 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
#   --- Undo the second reset only: period 4 is evaluated again, the first reset keeps periods 1-3 flagged ---
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates Working Capital delinquency reset undo
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE      | 08 January 2026 | 12 January 2026 |                |                    |           |               |
      | RESET      | 09 January 2026 |                 |                |                    |           |               |
      | RESET      | 11 January 2026 | 12 January 2026 |                |                    |           |               |
      | RESUME     | 11 January 2026 |                 |                |                    |           |               |
      | UNDO_RESET | 12 January 2026 |                 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 08 January 2026 |                |            |                   |                       |
      | 4            | 09 January 2026 | 10 January 2026 | 200            | 0          | 200               | false                 |
      | 5            | 11 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "12 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C115208
  Scenario: Verify Working Capital delinquency reset - UC27: a reset without start new period inside an active pause is not a boundary, the current period keeps its dates and a resume shortens it from its end
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "12 January 2026"
#   --- Flag-only reset inside the pause: periods 1-2 are flagged, the current period is neither cut nor moved ---
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a Working Capital delinquency reset
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 14 January 2026 | 200            | 0          | 200               |                       |
#   --- Resume two days early: the current period loses the two days from its end ---
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume with startDate "10 January 2026"
    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate         | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE  | 08 January 2026 | 12 January 2026 |                |                    |           |               |
      | RESET  | 09 January 2026 |                 |                |                    |           |               |
      | RESUME | 10 January 2026 |                 |                |                    |           |               |
    And WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 |                |            |                   |                       |
      | 2            | 04 January 2026 | 06 January 2026 |                |            |                   |                       |
      | 3            | 07 January 2026 | 12 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 13 January 2026 | 15 January 2026 | 200            | 0          | 200               |                       |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "13 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:CTBD
  Scenario: Verify Working Capital delinquency reset - UC28: a pause cannot start before an undone reset with start new period, since the undo leaves the cut in place
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#   --- Reset with start new period: period 3 is cut on 07 January and period 4 starts on the reset date ---
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          |
      | 1            | 01 January 2026 | 03 January 2026 |
      | 2            | 04 January 2026 | 06 January 2026 |
      | 3            | 07 January 2026 | 07 January 2026 |
      | 4            | 08 January 2026 | 10 January 2026 |
#   --- Undo the reset: the flags are cleared but the cut stays ---
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates Working Capital delinquency reset undo
#   --- A pause starting before the undone reset date would move the restarted period off it, so it is rejected ---
    Then Initiating a Working Capital loan delinquency pause with startDate "07 January 2026" and endDate "10 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                            |
      | 400      | Delinquency pause cannot start before the latest delinquency reset date |
#   --- A pause starting on the reset date is accepted and extends the restarted period from its end only ---
    When Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "10 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          |
      | 1            | 01 January 2026 | 03 January 2026 |
      | 2            | 04 January 2026 | 06 January 2026 |
      | 3            | 07 January 2026 | 07 January 2026 |
      | 4            | 08 January 2026 | 13 January 2026 |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "09 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:CTBD
  Scenario: Verify Working Capital delinquency reset - UC29: a reset with start new period inside a pause backdated into an earlier period cuts the period holding the reset date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 800                | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          |
      | 1            | 01 January 2026 | 03 January 2026 |
      | 2            | 04 January 2026 | 06 January 2026 |
      | 3            | 07 January 2026 | 09 January 2026 |
#   --- 5-day pause backdated into period 2: period 2 is extended to 11 January and period 3 pushed to 12-14 January ---
    And Admin initiate a Working Capital loan delinquency pause with startDate "05 January 2026" and endDate "09 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          |
      | 1            | 01 January 2026 | 03 January 2026 |
      | 2            | 04 January 2026 | 11 January 2026 |
      | 3            | 12 January 2026 | 14 January 2026 |
#   --- Reset with start new period while the pause is active: the reset date falls in period 2, not in the last period ---
#   --- Period 2 is cut on 07 January, the not yet started period 3 is replaced by the restarted one, which starts on ---
#   --- the reset date and counts only the pause days 08 and 09 January: 3 days from 08 January extended to 12 January ---
    And Admin creates WC delinquency reset action with start new period
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          |
      | 1            | 01 January 2026 | 03 January 2026 |
      | 2            | 04 January 2026 | 07 January 2026 |
      | 3            | 08 January 2026 | 12 January 2026 |
#   --- The next period follows the restarted one without overlap ---
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          |
      | 1            | 01 January 2026 | 03 January 2026 |
      | 2            | 04 January 2026 | 07 January 2026 |
      | 3            | 08 January 2026 | 12 January 2026 |
      | 4            | 13 January 2026 | 15 January 2026 |
#   --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "13 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
