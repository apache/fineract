@WorkingCapital
@WorkingCapitalDelinquencyFeature
Feature: Working Capital Delinquency

  @TestRailId:C74462
  Scenario: Verify working capital loan delinquency range schedule - UC1: no delinquency range schedule created for loan with state: "Submitted and pending approval"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | 0.0              |
    Then Working Capital loan status will be "SUBMITTED_AND_PENDING_APPROVAL"
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has no data on a not yet disbursed loan

  @TestRailId:C74463
  Scenario: Verify working capital loan delinquency range schedule - UC2: no delinquency range schedule created for loan with state: "Approved"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working Capital loan status will be "APPROVED"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has no data on a not yet disbursed loan

  @TestRailId:C74464
  Scenario: Verify working capital loan delinquency range schedule - UC3: delinquency range schedule on disbursement date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  @TestRailId:C74465
  Scenario: Verify working capital loan delinquency range schedule - UC4: delinquency range schedule on last day of 1st range
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  @TestRailId:C74466
  Scenario: Verify working capital loan delinquency range schedule - UC5: delinquency range schedule on first day of 2nd range
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  @TestRailId:C74467
  Scenario: Verify working capital loan delinquency range schedule - UC6: delinquency range schedule, multiple ranges
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "19 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 16             |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 13             |
      | 3            | 2026-01-07 | 2026-01-09 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 10             |
      | 4            | 2026-01-10 | 2026-01-12 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 7              |
      | 5            | 2026-01-13 | 2026-01-15 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 4              |
      | 6            | 2026-01-16 | 2026-01-18 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 7            | 2026-01-19 | 2026-01-21 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  @TestRailId:C74468
  Scenario: Verify working capital loan delinquency range schedule - UC7: delinquency range schedule, multiple ranges with discount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | 1000.0           |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0   |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    When Admin sets the business date to "19 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 16             |
      | 2            | 2026-01-04 | 2026-01-06 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 13             |
      | 3            | 2026-01-07 | 2026-01-09 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 10             |
      | 4            | 2026-01-10 | 2026-01-12 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 7              |
      | 5            | 2026-01-13 | 2026-01-15 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 4              |
      | 6            | 2026-01-16 | 2026-01-18 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 1              |
      | 7            | 2026-01-19 | 2026-01-21 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |

  @TestRailId:C74525
  Scenario: Verify working capital loan delinquency tag history - UC1: multiple ranges
# NOTE: intentionally kept on the seeded 30-day bucket: D30 / D60 classifications need periods aged 31+ / 61+ days
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful on "01 January 2026" with "100" EUR transaction amount
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
# --- No delinquency tag history ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
# --- No delinquency tag history ---
    When Admin sets the business date to "30 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
# --- Delinquency tag history with 1 range ---
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
# --- Delinquency tag history with 3 ranges---
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 3            | 2026-04-01  |              | D00            | 1              | 30             |
      | 2            | 2026-04-01  |              | D30            | 31             | 60             |
      | 1            | 2026-04-01  |              | D60            | 61             | 90             |
      | 2            | 2026-03-02  |              | D00            | 1              | 30             |
      | 1            | 2026-03-02  |              | D30            | 31             | 60             |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |

  @TestRailId:C74526
  Scenario: Verify working capital loan delinquency tag history - UC2: multiple ranges with (internal) payment
    When Admin sets the business date to "01 December 2020"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 December 2020 | 01 December 2020         | 1800            | 1800               | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | 2020-12-01      | 2020-12-01               | Submitted and pending approval | 1800.0            | 0.0               | 1800.0             | 1.0               | 0.0              |
    Then Admin successfully approves the working capital loan on "01 December 2020" with "1800" amount and expected disbursement date on "01 December 2020"
    Then Admin successfully disburse the Working Capital loan on "01 December 2020" with "1800" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful on "01 December 2020" with "1800" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2020-12-01      | 2020-12-01               | Active | 1800.0    | 1800.0            | 1800.0             | 1.0               | null     |
    When Admin sets the business date to "02 December 2020"
    And Admin runs inline COB job for Working Capital Loan
# --- No delinquency tag history ---
    When Admin sets the business date to "03 December 2020"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 December 2020" with 30.0 transaction amount on Working Capital loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
# --- Delinquency tag history with 1 range ---
    When Admin sets the business date to "04 December 2020"
    And Admin runs inline COB job for Working Capital Loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2020-12-04  |              | D00            | 1              | 30             |
# --- Delinquency tag history with 1 range + internal payment---
    When Admin sets the business date to "05 December 2020"
    And Admin runs inline COB job for Working Capital Loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2020-12-04  |              | D00            | 1              | 30             |
    And Customer makes repayment on "05 December 2020" with 54.0 transaction amount on Working Capital loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2020-12-04  | 2020-12-05   | D00            | 1              | 30             |
    When Admin sets the business date to "06 December 2020"
    And Admin runs inline COB job for Working Capital Loan
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2020-12-04  | 2020-12-05   | D00            | 1              | 30             |

  @TestRailId:C74527
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC1: full expectedAmount repaid on disbursement day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "01 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    Then Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |

  @TestRailId:C74528
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC2: full expectedAmount repaid after disbursement day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |

  @TestRailId:C74529
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC3: full expectedAmount repaid on last day of 1st period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid ---
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |

  @TestRailId:C74530
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC4: full expectedAmount repaid on first day of 2nd period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid ---
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |
    And Customer makes repayment on "04 January 2026" with 270.0 transaction amount on Working Capital loan
#   --- Check ---
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  | 2026-01-04   | D00            | 1              | 30             |

  @TestRailId:C74531
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC5: full expectedAmount repaid in 1st period with multiple payments on same day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid in 2 payments on the same day---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |

  @TestRailId:C74532
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC6: full expectedAmount repaid in 1st period with multiple payments on different days
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid in 2 payments on different days---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 170.0      | 100.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |

  @TestRailId:C74533
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC7: partial expectedAmount repaid in 1st period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Partial expectedAmount paid ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 170.0      | 100.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 170.0      | 100.0             | false                 | 100.0            | 1              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |

  @TestRailId:C74534
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC8: partial expectedAmount repaid in 2nd period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Start of 2nd period ---
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |
    #   --- Partial expectedAmount paid ---
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 170.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 170.0      | 100.0             | false                 | 100.0            | 2              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |

  @TestRailId:C74535
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC9: expectedAmount overpaid in 1st period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    #   --- expectedAmount overpaid ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 370.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 370.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    #   --- Start of 2nd period ---
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 370.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |

  @TestRailId:C74536
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC10: expectedAmount overpaid in 2nd period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Start of 2nd period ---
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |
    #   --- expectedAmount overpaid ---
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 370.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 100.0      | 170.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-04  | 2026-01-05   | D00            | 1              | 30             |

  @TestRailId:C74537
  Scenario: Verify working capital loan delinquency range schedule with (internal) payments - UC11: expectedAmount overpaid in late period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Late period ---
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 10             |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 7              |
      | 3            | 2026-01-07 | 2026-01-09 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 4              |
      | 4            | 2026-01-10 | 2026-01-12 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 5            | 2026-01-13 | 2026-01-15 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 4            | 2026-01-13  |              | D00            | 1              | 30             |
      | 3            | 2026-01-10  |              | D00            | 1              | 30             |
      | 2            | 2026-01-07  |              | D00            | 1              | 30             |
      | 1            | 2026-01-04  |              | D00            | 1              | 30             |
    #   --- expectedAmount overpaid ---
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 1500.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 3            | 2026-01-07 | 2026-01-09 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 4            | 2026-01-10 | 2026-01-12 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 5            | 2026-01-13 | 2026-01-15 | 270.0          | 420.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 4            | 2026-01-13  | 2026-01-15   | D00            | 1              | 30             |
      | 3            | 2026-01-10  | 2026-01-15   | D00            | 1              | 30             |
      | 2            | 2026-01-07  | 2026-01-15   | D00            | 1              | 30             |
      | 1            | 2026-01-04  | 2026-01-15   | D00            | 1              | 30             |

  @TestRailId:C94056
  Scenario: Verify delinquency grace period boundary - UC1: delinquency range schedule with grace period set to 15 days.
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 270          | 15              | 15                   |
    And Admin creates WC Delinquency Bucket With Values:
      | frequency | frequencyType | minimumPaymentType | minimumPayment |
      | 3         | DAYS          | PERCENTAGE         | 3              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId |
      | 01 January 2026 | 01 January 2026          | 9000.0          | 100000.0           | 18.0              | 0.0      | LAST_CREATED        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-18 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    # --- Inside grace window: no delinquency tags ---
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-18 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    # --- First delinquency period is completed ---
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-18 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-18 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-18 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-19 | 2026-01-21 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    # --- Past grace period: delinquency tag appears ---
    When Admin sets the business date to "24 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- There is 1 delinquent period ---
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-18 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-19 | 2026-01-21 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 3              |
      | 3            | 2026-01-22 | 2026-01-24 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-01-22  |              | D00            | 1              | 30             |
    # --- Payment clears delinquency and lifts the tag ---
    When Customer makes repayment on "24 January 2026" with 270.0 transaction amount on Working Capital loan
    # --- There is no delinquent period, and current period has minimum payment criteria met ---
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-18 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-19 | 2026-01-21 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 3            | 2026-01-22 | 2026-01-24 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-01-22  | 2026-01-24   | D00            | 1              | 30             |
    When Customer makes repayment on "24 January 2026" with 270.0 transaction amount on Working Capital loan
    # --- There is no delinquent period, and current period has minimum payment criteria met ---
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-18 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-19 | 2026-01-21 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 3            | 2026-01-22 | 2026-01-24 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-01-22  | 2026-01-24   | D00            | 1              | 30             |

  @TestRailId:C94057
  Scenario: Verify delinquency grace period boundary - UC2: no delinquency tags before grace expiry with 3 days
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 01 January 2026 | 01 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- First period is extended by the 3-day grace window ---
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-02 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    # --- Last day of grace window: still no delinquency ---
    When Admin sets the business date to "02 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-02 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    # --- Day after grace expiry: D00 tag appears ---
    When Admin sets the business date to "03 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-02 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-02-03 | 2026-03-04 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-02-03  |              | D00            | 1              | 30             |

  @TestRailId:C94058
  Scenario: Verify delinquency grace period - UC3: payment on last grace day prevents delinquency
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | 01 January 2026 | 01 January 2026          | 9000.0          | 100000.0           | 18.0              |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-02 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    # --- Repayment on last grace day clears the period ---
    When Admin sets the business date to "02 February 2026"
    And Customer makes repayment on "02 February 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-02 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    # --- After grace expiry the period stays clear ---
    When Admin sets the business date to "03 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-02-02 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-02-03 | 2026-03-04 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |

  @TestRailId:C98172
  Scenario: Verify that delinquency Id is overridable and applied
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          | 5               |
    And Admin creates WC Delinquency Bucket With Values:
      | frequency | frequencyType | minimumPaymentType | minimumPayment |
      | 2         | WEEKS         | FLAT               | 248            |
    And Admin creates a working capital loan using created product with breachGraceDays 11 and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId | delinquencyGraceDays |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        | LAST_CREATED        | 13                   |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-27 | 248.0          | 0.0        | 248.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2026"
