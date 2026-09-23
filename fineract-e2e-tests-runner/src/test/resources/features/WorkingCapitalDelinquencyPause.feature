@WorkingCapital
@WorkingCapitalDelinquencyPauseFeature
Feature: Working Capital Delinquency Pause

  @TestRailId:C74480
  Scenario: Verify working capital loan delinquency pause - UC1: delinquency pause on disbursement day
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "02 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-02 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-05 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 January 2026"

  @TestRailId:C74481
  Scenario: Verify working capital loan delinquency pause - UC2: delinquency pause in the middle of first period
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "03 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-03 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-05 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2026"

  @TestRailId:C74482
  Scenario: Verify working capital loan delinquency pause - UC3: backdated delinquency pause, whole pause period before actual business date
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "03 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-03 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 January 2026"

  @TestRailId:C74483
  Scenario: Verify working capital loan delinquency pause - UC4: backdated delinquency pause, pause period is overlapping actual business date
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "04 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-04 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2026"

  @TestRailId:C74484
  Scenario: Verify working capital loan delinquency pause - UC5: delinquency pause in the middle of second period
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "05 January 2026" and endDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-05 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 2              |
      | 2            | 2026-01-04 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"

  @TestRailId:C74485
  Scenario: Verify working capital loan delinquency pause - UC6: delinquency pause in the middle of first period with pause overlapping to second period
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 2              |
      | 2            | 2026-01-08 | 2026-01-10 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"

  @TestRailId:C98197
  Scenario: Verify working capital loan delinquency pause - UC6.3: second future-dated pause overlapping an existing future-dated pause results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin initiate a Working Capital loan delinquency pause with startDate "10 January 2026" and endDate "12 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
      | PAUSE  | 2026-01-10 | 2026-01-12 |
    Then Initiating a Working Capital loan delinquency pause with startDate "10 January 2026" and endDate "15 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                      |
      | 400      | Delinquency pause period cannot overlap with another pause period |
    And Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
      | PAUSE  | 2026-01-10 | 2026-01-12 |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2026"

  @TestRailId:C98198
  Scenario: Verify working capital loan delinquency pause - UC6.4: backdated delinquency pause overlaps to an existing pause results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "05 January 2026" and endDate "08 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-08 |
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "02 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-08 |
      | PAUSE  | 2026-01-01 | 2026-01-02 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-05 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Initiating a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "03 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                      |
      | 400      | Delinquency pause period cannot overlap with another pause period |
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-08 |
      | PAUSE  | 2026-01-01 | 2026-01-02 |
    Then Initiating a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "04 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                      |
      | 400      | Delinquency pause period cannot overlap with another pause period |
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-08 |
      | PAUSE  | 2026-01-01 | 2026-01-02 |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 January 2026"

  @TestRailId:C78810
  Scenario: Verify working capital loan delinquency pause - UC6.1: delinquency pause in the middle of first period with future pauses overlapping to a few periods
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Admin initiate a Working Capital loan delinquency pause with startDate "13 January 2026" and endDate "15 January 2026"
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "09 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
      | PAUSE  | 2026-01-13 | 2026-01-15 |
      | PAUSE  | 2026-01-08 | 2026-01-09 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-08 | 2026-01-12 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
      | PAUSE  | 2026-01-13 | 2026-01-15 |
      | PAUSE  | 2026-01-08 | 2026-01-09 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 3              |
      | 2            | 2026-01-08 | 2026-01-12 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "17 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 10             |
      | 2            | 2026-01-08 | 2026-01-12 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 5              |
      | 3            | 2026-01-13 | 2026-01-18 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "17 January 2026"

  @TestRailId:C78811
  Scenario: Verify working capital loan delinquency pause - UC6.2: delinquency pause in the middle of first period with pauses overlapping to a few periods
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-08 | 2026-01-10 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Admin initiate a Working Capital loan delinquency pause with startDate "08 January 2026" and endDate "09 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
      | PAUSE  | 2026-01-08 | 2026-01-09 |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
      | PAUSE  | 2026-01-08 | 2026-01-09 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 3              |
      | 2            | 2026-01-08 | 2026-01-12 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
      | PAUSE  | 2026-01-08 | 2026-01-09 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 8              |
      | 2            | 2026-01-08 | 2026-01-12 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 3              |
      | 3            | 2026-01-13 | 2026-01-15 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Admin initiate a Working Capital loan delinquency pause with startDate "13 January 2026" and endDate "14 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
      | PAUSE  | 2026-01-08 | 2026-01-09 |
      | PAUSE  | 2026-01-13 | 2026-01-14 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 8              |
      | 2            | 2026-01-08 | 2026-01-12 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 3              |
      | 3            | 2026-01-13 | 2026-01-17 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "15 January 2026"

  @TestRailId:C74486
  Scenario: Verify working capital loan delinquency pause - UC7: backdated delinquency pause into an already evaluated period shifts and re-evaluates the schedule
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 2              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Admin initiate a Working Capital loan delinquency pause with startDate "03 January 2026" and endDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-03 | 2026-01-05 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
      | 2            | 2026-01-07 | 2026-01-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-07 | 2026-01-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "07 January 2026"

  @TestRailId:C89782
  Scenario: Verify working capital loan delinquency pause - UC11: backdated pause into an evaluated period shifts subsequent generated periods and reopens them until re-evaluated
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 2              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "04 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-04 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
      | 2            | 2026-01-07 | 2026-01-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-07 | 2026-01-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "07 January 2026"

  @TestRailId:C89783
  Scenario: Verify working capital loan delinquency pause - UC12: backdated pause into a delinquent period lifts the loan's delinquency classification until re-evaluation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 2              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Working capital loan account has the correct data:
      | delinquencyStartDate |
      | 2026-01-01           |
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "04 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-04 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
      | 2            | 2026-01-07 | 2026-01-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Working capital loan account has the correct data:
      | delinquencyStartDate |
      | null                 |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-06 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-07 | 2026-01-09 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Working capital loan account has the correct data:
      | delinquencyStartDate |
      | 2026-01-01           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "07 January 2026"

  @TestRailId:C74487
  Scenario: Verify working capital loan delinquency pause - UC8: multiple delinquency pauses in the same period
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "03 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-03 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-05 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "05 January 2026" and endDate "06 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-03 |
      | PAUSE  | 2026-01-05 | 2026-01-06 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"

  @TestRailId:C74488
  Scenario: Verify working capital loan delinquency pause - UC9: second delinquency pause overlaps with first pause period results an error (Negative)
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-07 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan delinquency pause with startDate "04 January 2026" and endDate "08 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                      |
      | 400      | Delinquency pause period cannot overlap with another pause period |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "04 January 2026"

  @TestRailId:C74489
  Scenario: Verify working capital loan delinquency pause - UC10: delinquency pause by external ID in the middle of first period
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
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause by external ID with startDate "02 January 2026" and endDate "03 January 2026"
    Then Working Capital loan delinquency action by external ID has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-03 |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-05 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2026"

  @TestRailId:C85313
  Scenario: Verify working capital loan delinquency pause - touching consecutive pauses are rejected (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "05 January 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-02 | 2026-01-05 |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan delinquency pause with startDate "05 January 2026" and endDate "08 January 2026" results an error with the following data:
      | httpCode | errorMessage                                                      |
      | 400      | Delinquency pause period cannot overlap with another pause period |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"
