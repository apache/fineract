@WorkingCapital
@WorkingCapitalDelinquencyFeature
Feature: Working Capital Delinquency

  @TestRailId:C123852
  Scenario: Verify delinquency tags and delinquentDate are set on the date they should have happened when the first COB runs long after disbursement
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # No COB since disbursement: period 1 (toDate 2026-02-06 incl. 3 grace days) is past due (LD00) from 02-07 and delinquent (LD01) from 02-08
    When Admin sets the business date to "11 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 5              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  |              | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 270.0      |
      | delinquent.delinquentDate   | 2026-02-07 |
      | delinquent.delinquentDays   | 5          |
      | delinquent.pastDueDays      | 5          |
    Then Admin closes the Working Capital loan with a full repayment on "11 February 2026"

  @TestRailId:C123853
  Scenario: Verify delinquency tags and delinquentDate are the same when COB runs on every day since disbursement
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # COB on the disbursement day sets the last closed business date, so the next inline COB replays every day up to 02-11
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "11 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 5              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  |              | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 270.0      |
      | delinquent.delinquentDate   | 2026-02-07 |
      | delinquent.delinquentDays   | 5          |
      | delinquent.pastDueDays      | 5          |
    Then Admin closes the Working Capital loan with a full repayment on "11 February 2026"

  @TestRailId:C123854
  Scenario: Verify every period is tagged through all the ranges it passed on their own dates when the first COB runs months after disbursement
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # No COB since disbursement: each period enters LD00 / LD01 / LD30 / LD60 on toDate + 1 / 2 / 31 / 61
    When Admin sets the business date to "08 September 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 214            |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 184            |
      | 3            | 2026-03-09 | 2026-04-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 154            |
      | 4            | 2026-04-08 | 2026-05-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 124            |
      | 5            | 2026-05-08 | 2026-06-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 94             |
      | 6            | 2026-06-07 | 2026-07-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 64             |
      | 7            | 2026-07-07 | 2026-08-05 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 34             |
      | 8            | 2026-08-06 | 2026-09-04 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 4              |
      | 9            | 2026-09-05 | 2026-10-04 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 8            | 2026-09-06  |              | LD01           | 2              | 30             |
      | 8            | 2026-09-05  | 2026-09-06   | LD00           | 1              | 1              |
      | 7            | 2026-09-05  |              | LD30           | 31             | 60             |
      | 6            | 2026-09-05  |              | LD60           | 61             |                |
      | 7            | 2026-08-07  | 2026-09-05   | LD01           | 2              | 30             |
      | 7            | 2026-08-06  | 2026-08-07   | LD00           | 1              | 1              |
      | 6            | 2026-08-06  | 2026-09-05   | LD30           | 31             | 60             |
      | 5            | 2026-08-06  |              | LD60           | 61             |                |
      | 6            | 2026-07-08  | 2026-08-06   | LD01           | 2              | 30             |
      | 6            | 2026-07-07  | 2026-07-08   | LD00           | 1              | 1              |
      | 5            | 2026-07-07  | 2026-08-06   | LD30           | 31             | 60             |
      | 4            | 2026-07-07  |              | LD60           | 61             |                |
      | 5            | 2026-06-08  | 2026-07-07   | LD01           | 2              | 30             |
      | 5            | 2026-06-07  | 2026-06-08   | LD00           | 1              | 1              |
      | 4            | 2026-06-07  | 2026-07-07   | LD30           | 31             | 60             |
      | 3            | 2026-06-07  |              | LD60           | 61             |                |
      | 4            | 2026-05-09  | 2026-06-07   | LD01           | 2              | 30             |
      | 4            | 2026-05-08  | 2026-05-09   | LD00           | 1              | 1              |
      | 3            | 2026-05-08  | 2026-06-07   | LD30           | 31             | 60             |
      | 2            | 2026-05-08  |              | LD60           | 61             |                |
      | 3            | 2026-04-09  | 2026-05-08   | LD01           | 2              | 30             |
      | 3            | 2026-04-08  | 2026-04-09   | LD00           | 1              | 1              |
      | 2            | 2026-04-08  | 2026-05-08   | LD30           | 31             | 60             |
      | 1            | 2026-04-08  |              | LD60           | 61             |                |
      | 2            | 2026-03-10  | 2026-04-08   | LD01           | 2              | 30             |
      | 2            | 2026-03-09  | 2026-03-10   | LD00           | 1              | 1              |
      | 1            | 2026-03-09  | 2026-04-08   | LD30           | 31             | 60             |
      | 1            | 2026-02-08  | 2026-03-09   | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 2160.0     |
      | delinquent.delinquentDate   | 2026-02-07 |
      | delinquent.delinquentDays   | 214        |
      | delinquent.pastDueDays      | 214        |
    Then Admin closes the Working Capital loan with a full repayment on "08 September 2026"

  @TestRailId:C123863
  Scenario: Verify a second COB after catch-up does not re-date or duplicate delinquency tags
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # --- Catch-up COB: no COB since disbursement ---
    When Admin sets the business date to "11 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 5              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  |              | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    # --- Second COB: delinquentDays increments, tag dates and row count stay the same ---
    When Admin sets the business date to "12 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 6              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  |              | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 270.0      |
      | delinquent.delinquentDate   | 2026-02-07 |
      | delinquent.delinquentDays   | 6          |
      | delinquent.pastDueDays      | 6          |
    Then Admin closes the Working Capital loan with a full repayment on "12 February 2026"

  @TestRailId:C123864
  Scenario: Verify delinquency tags after a partial gap keep true entry dates when a mid-gap COB already ran
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # --- Mid-gap COB: tags LD00 / LD01 on their own dates ---
    When Admin sets the business date to "09 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 3              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  |              | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    # --- Second gap: period 1 passes LD30 / LD60, later periods are classified for the first time ---
    When Admin sets the business date to "15 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 68             |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 38             |
      | 3            | 2026-03-09 | 2026-04-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 8              |
      | 4            | 2026-04-08 | 2026-05-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 3            | 2026-04-09  |              | LD01           | 2              | 30             |
      | 3            | 2026-04-08  | 2026-04-09   | LD00           | 1              | 1              |
      | 2            | 2026-04-08  |              | LD30           | 31             | 60             |
      | 1            | 2026-04-08  |              | LD60           | 61             |                |
      | 2            | 2026-03-10  | 2026-04-08   | LD01           | 2              | 30             |
      | 2            | 2026-03-09  | 2026-03-10   | LD00           | 1              | 1              |
      | 1            | 2026-03-09  | 2026-04-08   | LD30           | 31             | 60             |
      | 1            | 2026-02-08  | 2026-03-09   | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 810.0      |
      | delinquent.delinquentDate   | 2026-02-07 |
      | delinquent.delinquentDays   | 68         |
      | delinquent.pastDueDays      | 68         |
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C123865
  Scenario: Verify instant classification on a repayment during the gap backdates delinquency tags the same way COB does
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # --- Repayment during the gap triggers instant classification: tags must be backdated, not dated 02-10 ---
    When Admin sets the business date to "10 February 2026"
    And Customer makes repayment on "10 February 2026" with 0.01 transaction amount on Working Capital loan
    Then Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 269.99     |
      | delinquent.delinquentDate   | 2026-02-07 |
      | delinquent.delinquentDays   | 4          |
      | delinquent.pastDueDays      | 4          |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  |              | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    # --- Catch-up COB one day later: no re-dating, no duplicates ---
    When Admin sets the business date to "11 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.01       | 269.99            | false                 | 269.99           | 5              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  |              | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 269.99     |
      | delinquent.delinquentDate   | 2026-02-07 |
      | delinquent.delinquentDays   | 5          |
      | delinquent.pastDueDays      | 5          |
    Then Admin closes the Working Capital loan with a full repayment on "11 February 2026"

  @TestRailId:C123866
  Scenario: Verify a repayment that cures the period during the gap lifts the backdated delinquency tags on the cure date
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # --- Partial repayment during the gap: instant classification writes backdated tags ---
    When Admin sets the business date to "09 February 2026"
    And Customer makes repayment on "09 February 2026" with 0.01 transaction amount on Working Capital loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  |              | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    # --- Repayment covering the minimum payment lifts the active tag on the cure date ---
    When Admin sets the business date to "10 February 2026"
    And Customer makes repayment on "10 February 2026" with 269.99 transaction amount on Working Capital loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  | 2026-02-10   | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 0.0 |
      | delinquent.delinquentDays   | 0   |
      | delinquent.pastDueDays      | 0   |
    # --- Catch-up COB keeps the lifted history and does not re-tag the cured period ---
    When Admin sets the business date to "11 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-08  | 2026-02-10   | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    Then Admin closes the Working Capital loan with a full repayment on "11 February 2026"

  @TestRailId:C123867
  Scenario: Verify delinquency range change business event carries the backdated delinquency dates after catch-up COB
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # --- Catch-up COB: the range change event must carry the same start dates a daily COB would have produced ---
    When Admin sets the business date to "11 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Delinquency Range Change business event is raised with delinquency data:
      | delinquencyStartType | delinquencyStartDate | delinquencyEffectiveStartDate | delinquentAmount | totalDelinquentAmount |
      | DISBURSEMENT         | 2026-01-05           | 2026-01-08                    | 270.0            | 270.0                 |
    Then Admin closes the Working Capital loan with a full repayment on "11 February 2026"

  @TestRailId:C123868
  Scenario: Verify catch-up COB landing exactly on a range entry date tags the period on that same date
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | LD00           | 1              | 1              |
      | LD01           | 2              | 30             |
      | LD30           | 31             | 60             |
      | LD60           | 61             |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # --- No COB since disbursement: 03-09 is exactly the day period 1 enters LD30 and period 2 enters LD00 ---
    When Admin sets the business date to "09 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 31             |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 3            | 2026-03-09 | 2026-04-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-03-09  |              | LD00           | 1              | 1              |
      | 1            | 2026-03-09  |              | LD30           | 31             | 60             |
      | 1            | 2026-02-08  | 2026-03-09   | LD01           | 2              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | LD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 540.0      |
      | delinquent.delinquentDate   | 2026-02-07 |
      | delinquent.delinquentDays   | 31         |
      | delinquent.pastDueDays      | 31         |
    Then Admin closes the Working Capital loan with a full repayment on "09 March 2026"

  @TestRailId:C123869
  Scenario: Verify a gapped delinquency bucket lifts the earlier range before the gap when the first COB runs late
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | GD00           | 1              | 1              |
      | GD05           | 5              | 30             |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 9              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-11  |              | GD05           | 5              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | GD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 270.0      |
      | delinquent.delinquentDate   | 2026-02-11 |
      | delinquent.delinquentDays   | 5          |
      | delinquent.pastDueDays      | 9          |
    Then Admin closes the Working Capital loan with a full repayment on "15 February 2026"

  @TestRailId:C123870
  Scenario: Verify a gapped delinquency bucket produces the same tags and delinquentDate when COB runs every day
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | GD00           | 1              | 1              |
      | GD05           | 5              | 30             |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 9              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-11  |              | GD05           | 5              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | GD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 270.0      |
      | delinquent.delinquentDate   | 2026-02-11 |
      | delinquent.delinquentDays   | 5          |
      | delinquent.pastDueDays      | 9          |
    Then Admin closes the Working Capital loan with a full repayment on "15 February 2026"

  @TestRailId:C123871
  Scenario: Verify a first COB that lands in a delinquency-bucket gap still records the earlier range on its entry and exit dates
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | GD00           | 1              | 1              |
      | GD05           | 5              | 30             |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # Gap is 02-09..02-10 (days 3..4); first COB on 02-09 must still write GD00 [02-07, 02-08], not leave history empty
    When Admin sets the business date to "09 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-05 | 2026-02-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 3              |
      | 2            | 2026-02-07 | 2026-03-08 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-07  | 2026-02-08   | GD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 0.0        |
      | delinquent.delinquentDate   | null       |
      | delinquent.delinquentDays   | 0          |
      | delinquent.pastDueDays      | 3          |
    Then Admin closes the Working Capital loan with a full repayment on "09 February 2026"

  @TestRailId:C123872
  Scenario: Verify an active pre-gap tag is lifted on the range exit date when COB first runs on a gap day
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | GD00           | 1              | 1              |
      | GD05           | 5              | 30             |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # Instant classification on GD00 entry leaves the tag active; gap-day COB must lift it on 02-08, not on 02-09
    When Admin sets the business date to "07 February 2026"
    And Customer makes repayment on "07 February 2026" with 0.01 transaction amount on Working Capital loan
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-07  |              | GD00           | 1              | 1              |
    When Admin sets the business date to "09 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-07  | 2026-02-08   | GD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 0.0        |
      | delinquent.delinquentDate   | null       |
      | delinquent.delinquentDays   | 0          |
      | delinquent.pastDueDays      | 3          |
    Then Admin closes the Working Capital loan with a full repayment on "09 February 2026"

  @TestRailId:C123873
  Scenario: Verify daily COB lifts the last range of a gapped delinquency bucket once, on its exit date, when the period moves past it
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | GD00           | 1              | 1              |
      | GD05           | 5              | 30             |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # Daily COB up to 03-10: period 1 leaves GD05 (day 30) on 03-09; GD05 must be lifted on 03-09 only, with no second GD05 tag
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "10 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-03-09  | 2026-03-10   | GD00           | 1              | 1              |
      | 1            | 2026-02-11  | 2026-03-09   | GD05           | 5              | 30             |
      | 1            | 2026-02-07  | 2026-02-08   | GD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 0.0  |
      | delinquent.delinquentDate   | null |
      | delinquent.delinquentDays   | 0    |
      | delinquent.pastDueDays      | 32   |
    Then Admin closes the Working Capital loan with a full repayment on "10 March 2026"

  @TestRailId:C123874
  Scenario: Verify a first COB on a gap day keeps the active tag of a later range and lifts it on that range's exit date
    When Admin sets the business date to "05 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS, minimumPayment 3 PERCENTAGE and the following ranges:
      | classification | minimumAgeDays | maximumAgeDays |
      | GD00           | 1              | 1              |
      | GD03           | 3              | 4              |
      | GD07           | 7              |                |
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" and the created delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 05 January 2026 | 05 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "05 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    # Instant classification on GD03 entry (day 3) leaves GD03 active
    When Admin sets the business date to "09 February 2026"
    And Customer makes repayment on "09 February 2026" with 0.01 transaction amount on Working Capital loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-09  |              | GD03           | 3              | 4              |
      | 1            | 2026-02-07  | 2026-02-08   | GD00           | 1              | 1              |
    # COB on 02-11 (day 5, gap 5..6): GD03 is lifted on its exit 02-11; lifting GD00 on 02-08 must not touch it
    When Admin sets the business date to "11 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-09  | 2026-02-11   | GD03           | 3              | 4              |
      | 1            | 2026-02-07  | 2026-02-08   | GD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 0.0  |
      | delinquent.delinquentDate   | null |
      | delinquent.delinquentDays   | 0    |
      | delinquent.pastDueDays      | 5    |
    # GD07 from day 7 (02-13): the chain starts again after the gap
    When Admin sets the business date to "14 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-13  |              | GD07           | 7              |                |
      | 1            | 2026-02-09  | 2026-02-11   | GD03           | 3              | 4              |
      | 1            | 2026-02-07  | 2026-02-08   | GD00           | 1              | 1              |
    And Working capital loan details has the following field values:
      | delinquent.delinquentAmount | 269.99     |
      | delinquent.delinquentDate   | 2026-02-13 |
      | delinquent.delinquentDays   | 2          |
      | delinquent.pastDueDays      | 8          |
    Then Admin closes the Working Capital loan with a full repayment on "14 February 2026"
