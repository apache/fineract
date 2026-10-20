@WorkingCapital
@WorkingCapitalDelinquencyRescheduleActionFeature
@Order(1)
Feature: Working Capital Delinquency Reschedule Action - Part2

  @TestRailId:C85579
  Scenario: Verify that the remaining balance cap on a discounted loan uses the principal plus discount base
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 2 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 100      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and "100" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount and "100" discount amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 750 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 225            | 750        | 0                 | true                  |
      | 2            | 03 January 2026 | 04 January 2026 | 150            | 0          | 150               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 225            | 750        | 0                 | true                  |
      | 2            | 03 January 2026 | 04 January 2026 | 150            | 150        | 0                 | true                  |

  @TestRailId:C85580
  Scenario: Verify that a backdated repayment reprocess recalculates the remaining balance cap
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 2 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 200            | 50         | 150               | false                 |
      | 2            | 03 January 2026 | 04 January 2026 | 200            | 0          | 200               |                       |
    And Customer makes repayment on "02 January 2026" with 700 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 03 January 2026 | 04 January 2026 | 100            | 50         | 50                |                       |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 03 January 2026 | 04 January 2026 | 100            | 100        | 0                 | true                  |

  @TestRailId:C85581
  Scenario: Verify that adding a discount after disbursement raises the remaining balance cap
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
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
      | 1            | 01 January 2026 | 03 January 2026 | 800            | 0          | 800               |                       |
    And Admin adds Discount fee with "100" amount on Working Capital loan account for last disbursement
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 900            | 0          | 900               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 900            | 900        | 0                 | true                  |

  @TestRailId:C85582
  Scenario: Verify that a discount adjustment and its undo re-derive the remaining balance cap
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 2 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 100      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and "100" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount and "100" discount amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 700 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 225            | 700        | 0                 | true                  |
      | 2            | 03 January 2026 | 04 January 2026 | 200            | 0          | 200               |                       |
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    And Admin adds Discount fee adjustment with "100" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 225            | 700        | 0                 | true                  |
      | 2            | 03 January 2026 | 04 January 2026 | 100            | 0          | 100               |                       |
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 225            | 700        | 0                 | true                  |
      | 2            | 03 January 2026 | 04 January 2026 | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 225            | 700        | 0                 | true                  |
      | 2            | 03 January 2026 | 04 January 2026 | 200            | 200        | 0                 | true                  |

  @TestRailId:C85583
  Scenario: Verify partial reschedules inherit the payment group across a minimum payment type switch
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "07 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 33.33          | PERCENTAGE         |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | expectedAmount | minPaymentCriteriaMet |
      | 3            | 266.64         |                       |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 150            | FLAT               |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | expectedAmount | minPaymentCriteriaMet |
      | 3            | 150            |                       |
    When Admin sets the business date to "09 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    # Frequency-only reschedule: the payment group must inherit the FLAT 150 from the latest action
    # that set it - not the 33.33 percent from the first action and not the product's 25 percent.
    # Frequency 3 DAYS matches the current period length, so its boundary stays 09 January and the
    # assertion isolates payment group inheritance from re-dating
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 3         | DAYS          |
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 07 January 2026 | 33.33          | PERCENTAGE         |           |               |
      | RESCHEDULE | 08 January 2026 | 150            | FLAT               |           |               |
      | RESCHEDULE | 09 January 2026 |                |                    | 3         | DAYS          |
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 07 January 2026 | 09 January 2026 | 150            | 0          | 150               | false                 |
      | 4            | 10 January 2026 | 12 January 2026 | 150            | 0          | 150               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 3         | DAYS          |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # Period 1 (expected 3 = 3 percent of 100) is 1 day past due - tagged D00
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 1 transaction amount on Working Capital loan
    # The backdated repayment fully pays the loan; reprocess caps period 2 to the zero remaining
    # balance, marks it met, and the classification lifts the D00 tag from period 1
    And Customer makes repayment on "01 January 2026" with 99 transaction amount on Working Capital loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  | 2026-01-05   | D00            | 1              | 30             |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 3              | 99         | 0                 | true                  |
      | 2            | 04 January 2026 | 06 January 2026 | 1              | 1          | 0                 | true                  |

  @TestRailId:C85585
  Scenario: Verify delinquent amount and days are reported from the capped expectation
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 2 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 700 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    # Remaining balance is 100, so the past-due periods expect (and report as delinquent) 100 - not
    # the uncapped 200 minimum payment
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 01 January 2026 | 02 January 2026 | 200            | 700        | 0                 | true                  | 0                | 0              |
      | 2            | 03 January 2026 | 04 January 2026 | 100            | 0          | 100               | false                 | 100              | 4              |
      | 3            | 05 January 2026 | 06 January 2026 | 100            | 0          | 100               | false                 | 100              | 2              |
      | 4            | 07 January 2026 | 08 January 2026 | 100            | 0          | 100               |                       |                  |                |
    Then Admin closes the Working Capital loan with a full repayment on "08 January 2026"
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
    When Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin initiate a Working Capital loan delinquency pause with startDate "06 January 2026" and endDate "07 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "09 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    # Frequency-only reschedule: payment group falls back to the product's 25 percent (no prior
    # reschedule); the pause-shifted boundaries of periods 2 and 3 must survive the rebuild
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 2         | DAYS          |
    Then WC loan has both PAUSE and RESCHEDULE delinquency actions
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | PAUSE      | 06 January 2026 |                |                    |           |               |
      | RESCHEDULE | 09 January 2026 |                |                    | 2         | DAYS          |
    When Admin sets the business date to "11 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 04 January 2026 | 08 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 09 January 2026 | 10 January 2026 | 200            | 0          | 200               | false                 |
      | 4            | 11 January 2026 | 12 January 2026 | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"

  @TestRailId:C85587
  Scenario: Verify the later of two same-day payment-only reschedules wins within the payment group
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 2 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 33.33          | PERCENTAGE         |
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 150            | FLAT               |
    Then WC loan delinquency actions have the following data:
      | action     | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESCHEDULE | 05 January 2026 | 33.33          | PERCENTAGE         |           |               |
      | RESCHEDULE | 05 January 2026 | 150            | FLAT               |           |               |
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 02 January 2026 | 200            | 0          | 200               | false                 |
      | 2            | 03 January 2026 | 04 January 2026 | 200            | 0          | 200               | false                 |
      | 3            | 05 January 2026 | 06 January 2026 | 150            | 0          | 150               |                       |
    When Admin sets the business date to "07 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 4            | 07 January 2026 | 08 January 2026 | 150            | 0          | 150               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "07 January 2026"

  @TestRailId:C93974
  Scenario: Verify delinquency reschedule frequency change is rejected when resulting period end date is in the past
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Admin fails to create WC delinquency reschedule action with error containing "Frequency change results a delinquency period endDate before current businessDate is not allowed" and the following parameters:
      | frequency | frequencyType |
      | 1         | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 300            | 0          | 300               |                       |
    Then Admin fails to create WC delinquency reschedule action with error containing "Frequency change results a delinquency period endDate before current businessDate is not allowed" and the following parameters:
      | frequency | frequencyType |
      | 2         | DAYS          |
    # --- Delinquency reschedule ---
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 3         | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 03 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 04 January 2026 | 06 January 2026 | 300            | 0          | 300               |                       |
    # --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C93975
  Scenario: Verify delinquency reschedule validates the recalculated period end date against the resumed pause length
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    # --- Delinquency pause ---
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "03 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 05 January 2026 | 300            | 0          | 300               |                       |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    # --- Delinquency resume ---
    And Admin initiate a Working Capital loan delinquency resume with startDate "03 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 05 January 2026 | 300            | 0          | 300               |                       |
    When Admin sets the business date to "05 January 2026"
    Then Admin fails to create WC delinquency reschedule action with error containing "Frequency change results a delinquency period endDate before current businessDate is not allowed" and the following parameters:
      | frequency | frequencyType |
      | 2         | DAYS          |
    # --- Delinquency reschedule ---
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 3         | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 05 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 06 January 2026 | 08 January 2026 | 300            | 0          | 300               |                       |
    # --- Close loan ---
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"

  @TestRailId:94006
  Scenario: Verify delinquency reschedule absorbs a pause starting beyond the base end date but inside the extended period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # --- Delinquency pause ---
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "02 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 04 January 2026 | 300            | 0          | 300               |                       |
    # --- Delinquency pause ---
    When Admin initiate a Working Capital loan delinquency pause with startDate "04 January 2026" and endDate "04 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 05 January 2026 | 300            | 0          | 300               |                       |
    # --- Delinquency reschedule ---
    When Admin creates WC delinquency reschedule action with the following parameters:
      | frequency | frequencyType |
      | 4         | DAYS          |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 06 January 2026 | 300            | 0          | 300               |                       |
    # --- Delinquency pause ---
    When Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "08 January 2026"
    # --- Delinquency pause ---
    And Admin initiate a Working Capital loan delinquency pause with startDate "10 January 2026" and endDate "10 January 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 06 January 2026 | 300            | 0          | 300               |                       |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 06 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 07 January 2026 | 12 January 2026 | 300            | 0          | 300               |                       |
    # --- Close loan ---
    When Admin closes the Working Capital loan with a full repayment on "07 January 2026"
