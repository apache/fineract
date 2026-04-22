@WorkingCapitalLoanRepaymentFeature
Feature: Working Capital Loan Repayment

  @TestRailId:C76617
  Scenario: Verify working capital loan repayment - UC1: simple repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 270.00              | 1                     | 270.00   | 4658.91 | 0.00                       | 0.00                  | 35.58                    | 35.58              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178936723646887 | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76618
  Scenario: Verify working capital loan repayment - UC2: simple repayment by loan external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment by loan external ID on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 270.00              | 1                     | 270.00   | 4658.91 | 0.00                       | 0.00                  | 35.58                    | 35.58              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178936723646887 | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76619
  Scenario: Verify working capital loan repayment - UC3: simple repayment with zero amount results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a repayment on "01 January 2026" with 0.0 transaction amount on Working Capital loan results an error with the following data:
      | httpCode | errorMessage                                              |
      | 400      | The parameter `transactionAmount` must be greater than 0. |

  @TestRailId:C76620
  Scenario: Verify working capital loan repayment - UC4: simple repayment with negative amount results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a repayment on "01 January 2026" with -100.0 transaction amount on Working Capital loan results an error with the following data:
      | httpCode | errorMessage                                              |
      | 400      | The parameter `transactionAmount` must be greater than 0. |

  @TestRailId:C76621
  Scenario: Verify working capital loan repayment - UC5: simple repayment with future date results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a repayment on "15 January 2026" with 270.0 transaction amount on Working Capital loan results an error with the following data:
      | httpCode | errorMessage                                            |
      | 400      | Failed data validation due to: cannot.be.a.future.date. |

  @TestRailId:C76622
  Scenario: Verify working capital loan repayment - UC6: full expectedAmount repaid on disbursement day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "01 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 270.00              | 1                     | 270.00   | 4658.91 | 0.00                       | 0.00                  | 35.58                    | 35.58              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178936723646887 | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76623
  Scenario: Verify working capital loan repayment - UC7: full expectedAmount repaid after disbursement day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 270.00              | 1                     | 270.00   | 4658.91 | 0.00                       | 0.00                  | 35.58                    | 35.58              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178936723646887 | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76624
  Scenario: Verify working capital loan repayment - UC8: full expectedAmount repaid on last day of 1st period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid ---
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "30 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 270.00              | 1                     | 270.00   | 4658.91 | 0.00                       | 0.00                  | 35.58                    | 35.58              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178936723646887 | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76625
  Scenario: Verify working capital loan repayment - UC9: full expectedAmount repaid on first day of 2nd period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid ---
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    And Customer makes repayment on "31 January 2026" with 270.0 transaction amount on Working Capital loan
#   --- Check ---
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  | 2026-01-31   | D00            | 1              | 30             |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 270.00              | 1                     | 270.00   | 4658.91 | 0.00                       | 0.00                  | 35.58                    | 35.58              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178936723646887 | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76626
  Scenario: Verify working capital loan repayment - UC10: full expectedAmount repaid in 1st period with multiple payments on same day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid in 2 payments on the same day---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    Then Working Capital loan amortization schedule has 5 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 4     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 3     | 0            | 5000.00               | 5000.00               | 170.00              | 1                     | 170.00   | 4658.91 | 0.00                       | 0.00                  | 22.40                    | 22.40              | 0.00            |
      | 2         | 03 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 100.00              | 1                     | 100.00   | 0.00    | 0.00                       | 0.00                  | 13.18                    | 13.18              | 0.00            |
      | 3         | 04 January 2026  | 1     | 1            |                       | 5000.00               |                     | 0.9317821063          | 4658.91  |         |                            | 0.00                  |                          |                    | 0.00            |
      | 4         | 05 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178937          | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76627
  Scenario: Verify working capital loan repayment - UC11: full expectedAmount repaid in 1st period with multiple payments on different days
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid in 2 payments on different days---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 170.0      | 100.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    Then Working Capital loan amortization schedule has 5 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 4     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 3     | 0            | 5000.00               | 5000.00               | 170.00              | 1                     | 170.00   | 4658.91 | 0.00                       | 0.00                  | 22.40                    | 22.40              | 0.00            |
      | 2         | 03 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 100.00              | 1                     | 100.00   | 0.00    | 0.00                       | 0.00                  | 13.18                    | 13.18              | 0.00            |
      | 3         | 04 January 2026  | 1     | 1            |                       | 5000.00               |                     | 0.9317821063          | 4658.91  |         |                            | 0.00                  |                          |                    | 0.00            |
      | 4         | 05 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178937          | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76628
  Scenario: Verify working capital loan repayment - UC12: partial expectedAmount repaid in 1st period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Partial expectedAmount paid ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 170.0      | 100.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 170.0      | 100.0             | false                 | 100.0            | 1              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 170.00              | 1                     | 170.00   | 4658.91 | 0.00                       | 0.00                  | 22.40                    | 22.40              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4830.00               |                     | 0.8682178937          | 4193.49  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76629
  Scenario: Verify working capital loan repayment - UC13: partial expectedAmount repaid in 2nd period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Start of 2nd period ---
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    #   --- Partial expectedAmount paid ---
    When Admin sets the business date to "10 February 2026"
    And Customer makes repayment on "10 February 2026" with 170.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 170.0      | 100.0             | false                 | 100.0            | 11             |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 170.00              | 1                     | 170.00   | 4658.91 | 0.00                       | 0.00                  | 22.40                    | 22.40              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4830.00               |                     | 0.8682178937          | 4193.49  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76630
  Scenario: Verify working capital loan repayment - UC14: expectedAmount overpaid in 1st period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    #   --- expectedAmount overpaid ---
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 370.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 370.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    #   --- Start of 2nd period ---
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 370.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 370.00              | 1                     | 370.00   | 4658.91 | 0.00                       | 0.00                  | 48.76                    | 48.76              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4630.00               |                     | 0.8682178937          | 4019.85  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76631
  Scenario: Verify working capital loan repayment - UC15: expectedAmount overpaid in 2nd period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Start of 2nd period ---
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    #   --- expectedAmount overpaid ---
    When Admin sets the business date to "10 February 2026"
    And Customer makes repayment on "10 February 2026" with 370.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 100.0      | 170.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  | 2026-02-10   | D00            | 1              | 30             |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 370.00              | 1                     | 370.00   | 4658.91 | 0.00                       | 0.00                  | 48.76                    | 48.76              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4630.00               |                     | 0.8682178937          | 4019.85  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76632
  Scenario: Verify working capital loan repayment - UC16: expectedAmount overpaid in late period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Late period ---
    When Admin sets the business date to "01 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 91             |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 61             |
      | 3            | 2026-03-02 | 2026-03-31 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 31             |
      | 4            | 2026-04-01 | 2026-04-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 5            | 2026-05-01 | 2026-05-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 4            | 2026-05-01  |              | D00            | 1              | 30             |
      | 3            | 2026-05-01  |              | D30            | 31             | 60             |
      | 2            | 2026-05-01  |              | D60            | 61             | 90             |
      | 1            | 2026-05-01  |              | D90            | 91             | 120            |
      | 3            | 2026-04-01  |              | D00            | 1              | 30             |
      | 2            | 2026-04-01  |              | D30            | 31             | 60             |
      | 1            | 2026-04-01  |              | D60            | 61             | 90             |
      | 2            | 2026-03-02  |              | D00            | 1              | 30             |
      | 1            | 2026-03-02  |              | D30            | 31             | 60             |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |
    #   --- expectedAmount overpaid ---
    When Admin sets the business date to "10 May 2026"
    And Customer makes repayment on "10 May 2026" with 1500.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 3            | 2026-03-02 | 2026-03-31 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 4            | 2026-04-01 | 2026-04-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 5            | 2026-05-01 | 2026-05-30 | 270.0          | 420.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 4            | 2026-05-01  | 2026-05-10   | D00            | 1              | 30             |
      | 3            | 2026-05-01  | 2026-05-10   | D30            | 31             | 60             |
      | 2            | 2026-05-01  | 2026-05-10   | D60            | 61             | 90             |
      | 1            | 2026-05-01  | 2026-05-10   | D90            | 91             | 120            |
      | 3            | 2026-04-01  | 2026-05-10   | D00            | 1              | 30             |
      | 2            | 2026-04-01  | 2026-05-10   | D30            | 31             | 60             |
      | 1            | 2026-04-01  | 2026-05-10   | D60            | 61             | 90             |
      | 2            | 2026-03-02  | 2026-05-10   | D00            | 1              | 30             |
      | 1            | 2026-03-02  | 2026-05-10   | D30            | 31             | 60             |
      | 1            | 2026-01-31  | 2026-05-10   | D00            | 1              | 30             |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 1500.00             | 1                     | 1500.00  | 4658.91 | 0.00                       | 0.00                  | 197.67                   | 197.67             | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 3500.00               |                     | 0.8682178937          | 3038.76  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76633
  Scenario: Verify working capital loan repayment - UC17: simple repayment with payment details
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | 0.0      |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan with the following payment details:
      | paymentType | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | AUTOPAY     | acc123        | che456      | rou789      | rec012        | ban345     |
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    Then Working Capital loan amortization schedule has 4 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2026  | 3     | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 0.00            |
      | 1         | 02 January 2026  | 2     | 0            | 5000.00               | 5000.00               | 270.00              | 1                     | 270.00   | 4658.91 | 0.00                       | 0.00                  | 35.58                    | 35.58              | 0.00            |
      | 2         | 03 January 2026  | 1     | 1            | 5000.00               | 5000.00               |                     | 0.9317821063276353179 | 4658.91  | 0.00    | 0.00                       | 0.00                  |                          | 0.00               | 0.00            |
      | 3         | 04 January 2026  | 0     | 2            |                       | 4730.00               |                     | 0.8682178936723646887 | 4106.67  |         |                            | 0.00                  |                          |                    | 0.00            |

  @TestRailId:C76634
  Scenario: Verify amortization schedule after repayment transaction - UC18
    When Admin sets the business date to "01 January 2019"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2019 | 01 January 2019          | 9000            | 100000       | 0.18              | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2019" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2019"
    And Admin successfully disburse the Working Capital loan on "01 January 2019" with "9000" EUR transaction amount
    Then Working Capital loan amortization schedule has 201 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2019  | 200   | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 1000.00         |
      | 1         | 02 January 2019  | 199   | 1            | 50.00                 | 50.00                 |                     | 0.9989333245          | 49.95    | 8959.61 | 9.61                       | 0.00                  |                          | -9.61              | 1000.00         |
      | 2         | 03 January 2019  | 198   | 2            | 50.00                 | 50.00                 |                     | 0.9978677868          | 49.89    | 8919.18 | 9.57                       | 0.00                  |                          | -9.57              | 1000.00         |
      | 3         | 04 January 2019  | 197   | 3            | 50.00                 | 50.00                 |                     | 0.9968033857          | 49.84    | 8878.70 | 9.52                       | 0.00                  |                          | -9.52              | 1000.00         |
      | 4         | 05 January 2019  | 196   | 4            | 50.00                 | 50.00                 |                     | 0.99574012            | 49.79    | 8838.18 | 9.48                       | 0.00                  |                          | -9.48              | 1000.00         |
      | 5         | 06 January 2019  | 195   | 5            | 50.00                 | 50.00                 |                     | 0.9946779885          | 49.73    | 8797.62 | 9.44                       | 0.00                  |                          | -9.44              | 1000.00         |
      | 6         | 07 January 2019  | 194   | 6            | 50.00                 | 50.00                 |                     | 0.9936169898          | 49.68    | 8757.01 | 9.39                       | 0.00                  |                          | -9.39              | 1000.00         |
      | 7         | 08 January 2019  | 193   | 7            | 50.00                 | 50.00                 |                     | 0.992557123           | 49.63    | 8716.36 | 9.35                       | 0.00                  |                          | -9.35              | 1000.00         |
      | 8         | 09 January 2019  | 192   | 8            | 50.00                 | 50.00                 |                     | 0.9914983866          | 49.57    | 8675.67 | 9.31                       | 0.00                  |                          | -9.31              | 1000.00         |
      | 9         | 10 January 2019  | 191   | 9            | 50.00                 | 50.00                 |                     | 0.9904407796          | 49.52    | 8634.94 | 9.26                       | 0.00                  |                          | -9.26              | 1000.00         |
      | 10        | 11 January 2019  | 190   | 10           | 50.00                 | 50.00                 |                     | 0.9893843007          | 49.47    | 8594.16 | 9.22                       | 0.00                  |                          | -9.22              | 1000.00         |
      | 11        | 12 January 2019  | 189   | 11           | 50.00                 | 50.00                 |                     | 0.9883289487          | 49.42    | 8553.33 | 9.18                       | 0.00                  |                          | -9.18              | 1000.00         |
      | 12        | 13 January 2019  | 188   | 12           | 50.00                 | 50.00                 |                     | 0.9872747225          | 49.36    | 8512.47 | 9.13                       | 0.00                  |                          | -9.13              | 1000.00         |
      | 13        | 14 January 2019  | 187   | 13           | 50.00                 | 50.00                 |                     | 0.9862216208          | 49.31    | 8471.56 | 9.09                       | 0.00                  |                          | -9.09              | 1000.00         |
      | 14        | 15 January 2019  | 186   | 14           | 50.00                 | 50.00                 |                     | 0.9851696423          | 49.26    | 8430.60 | 9.05                       | 0.00                  |                          | -9.05              | 1000.00         |
      | 15        | 16 January 2019  | 185   | 15           | 50.00                 | 50.00                 |                     | 0.984118786           | 49.21    | 8389.61 | 9.00                       | 0.00                  |                          | -9.00              | 1000.00         |
      | 16        | 17 January 2019  | 184   | 16           | 50.00                 | 50.00                 |                     | 0.9830690507          | 49.15    | 8348.56 | 8.96                       | 0.00                  |                          | -8.96              | 1000.00         |
      | 17        | 18 January 2019  | 183   | 17           | 50.00                 | 50.00                 |                     | 0.982020435           | 49.10    | 8307.48 | 8.91                       | 0.00                  |                          | -8.91              | 1000.00         |
      | 18        | 19 January 2019  | 182   | 18           | 50.00                 | 50.00                 |                     | 0.9809729379          | 49.05    | 8266.35 | 8.87                       | 0.00                  |                          | -8.87              | 1000.00         |
      | 19        | 20 January 2019  | 181   | 19           | 50.00                 | 50.00                 |                     | 0.9799265581          | 49.00    | 8225.18 | 8.83                       | 0.00                  |                          | -8.83              | 1000.00         |
      | 20        | 21 January 2019  | 180   | 20           | 50.00                 | 50.00                 |                     | 0.9788812945          | 48.94    | 8183.96 | 8.78                       | 0.00                  |                          | -8.78              | 1000.00         |
      | 21        | 22 January 2019  | 179   | 21           | 50.00                 | 50.00                 |                     | 0.9778371458          | 48.89    | 8142.70 | 8.74                       | 0.00                  |                          | -8.74              | 1000.00         |
      | 22        | 23 January 2019  | 178   | 22           | 50.00                 | 50.00                 |                     | 0.9767941109          | 48.84    | 8101.39 | 8.69                       | 0.00                  |                          | -8.69              | 1000.00         |
      | 23        | 24 January 2019  | 177   | 23           | 50.00                 | 50.00                 |                     | 0.9757521886          | 48.79    | 8060.04 | 8.65                       | 0.00                  |                          | -8.65              | 1000.00         |
      | 24        | 25 January 2019  | 176   | 24           | 50.00                 | 50.00                 |                     | 0.9747113777          | 48.74    | 8018.65 | 8.61                       | 0.00                  |                          | -8.61              | 1000.00         |
      | 25        | 26 January 2019  | 175   | 25           | 50.00                 | 50.00                 |                     | 0.9736716769498249835 | 48.68    | 7977.21 | 8.56                       | 0.00                  |                          | -8.56              | 1000.00         |
      | 26        | 27 January 2019  | 174   | 26           | 50.00                 | 50.00                 |                     | 0.9726330853          | 48.63    | 7935.73 | 8.52                       | 0.00                  |                          | -8.52              | 1000.00         |
      | 27        | 28 January 2019  | 173   | 27           | 50.00                 | 50.00                 |                     | 0.9715956014          | 48.58    | 7894.21 | 8.47                       | 0.00                  |                          | -8.47              | 1000.00         |
      | 28        | 29 January 2019  | 172   | 28           | 50.00                 | 50.00                 |                     | 0.9705592242          | 48.53    | 7852.63 | 8.43                       | 0.00                  |                          | -8.43              | 1000.00         |
      | 29        | 30 January 2019  | 171   | 29           | 50.00                 | 50.00                 |                     | 0.9695239525          | 48.48    | 7811.02 | 8.39                       | 0.00                  |                          | -8.39              | 1000.00         |
      | 30        | 31 January 2019  | 170   | 30           | 50.00                 | 50.00                 |                     | 0.968489785           | 48.42    | 7769.36 | 8.34                       | 0.00                  |                          | -8.34              | 1000.00         |
      | 31        | 01 February 2019 | 169   | 31           | 50.00                 | 50.00                 |                     | 0.9674567207          | 48.37    | 7727.66 | 8.30                       | 0.00                  |                          | -8.30              | 1000.00         |
      | 32        | 02 February 2019 | 168   | 32           | 50.00                 | 50.00                 |                     | 0.9664247584          | 48.32    | 7685.91 | 8.25                       | 0.00                  |                          | -8.25              | 1000.00         |
      | 33        | 03 February 2019 | 167   | 33           | 50.00                 | 50.00                 |                     | 0.9653938968          | 48.27    | 7644.12 | 8.21                       | 0.00                  |                          | -8.21              | 1000.00         |
      | 34        | 04 February 2019 | 166   | 34           | 50.00                 | 50.00                 |                     | 0.9643641348          | 48.22    | 7602.28 | 8.16                       | 0.00                  |                          | -8.16              | 1000.00         |
      | 35        | 05 February 2019 | 165   | 35           | 50.00                 | 50.00                 |                     | 0.9633354712          | 48.17    | 7560.40 | 8.12                       | 0.00                  |                          | -8.12              | 1000.00         |
      | 36        | 06 February 2019 | 164   | 36           | 50.00                 | 50.00                 |                     | 0.9623079049          | 48.12    | 7518.47 | 8.07                       | 0.00                  |                          | -8.07              | 1000.00         |
      | 37        | 07 February 2019 | 163   | 37           | 50.00                 | 50.00                 |                     | 0.9612814347          | 48.06    | 7476.50 | 8.03                       | 0.00                  |                          | -8.03              | 1000.00         |
      | 38        | 08 February 2019 | 162   | 38           | 50.00                 | 50.00                 |                     | 0.9602560593          | 48.01    | 7434.48 | 7.98                       | 0.00                  |                          | -7.98              | 1000.00         |
      | 39        | 09 February 2019 | 161   | 39           | 50.00                 | 50.00                 |                     | 0.9592317777          | 47.96    | 7392.42 | 7.94                       | 0.00                  |                          | -7.94              | 1000.00         |
      | 40        | 10 February 2019 | 160   | 40           | 50.00                 | 50.00                 |                     | 0.9582085887          | 47.91    | 7350.31 | 7.89                       | 0.00                  |                          | -7.89              | 1000.00         |
      | 41        | 11 February 2019 | 159   | 41           | 50.00                 | 50.00                 |                     | 0.9571864911          | 47.86    | 7308.16 | 7.85                       | 0.00                  |                          | -7.85              | 1000.00         |
      | 42        | 12 February 2019 | 158   | 42           | 50.00                 | 50.00                 |                     | 0.9561654838          | 47.81    | 7265.97 | 7.80                       | 0.00                  |                          | -7.80              | 1000.00         |
      | 43        | 13 February 2019 | 157   | 43           | 50.00                 | 50.00                 |                     | 0.9551455655          | 47.76    | 7223.72 | 7.76                       | 0.00                  |                          | -7.76              | 1000.00         |
      | 44        | 14 February 2019 | 156   | 44           | 50.00                 | 50.00                 |                     | 0.9541267351          | 47.71    | 7181.44 | 7.71                       | 0.00                  |                          | -7.71              | 1000.00         |
      | 45        | 15 February 2019 | 155   | 45           | 50.00                 | 50.00                 |                     | 0.9531089916          | 47.66    | 7139.11 | 7.67                       | 0.00                  |                          | -7.67              | 1000.00         |
      | 46        | 16 February 2019 | 154   | 46           | 50.00                 | 50.00                 |                     | 0.9520923336          | 47.60    | 7096.73 | 7.62                       | 0.00                  |                          | -7.62              | 1000.00         |
      | 47        | 17 February 2019 | 153   | 47           | 50.00                 | 50.00                 |                     | 0.95107676            | 47.55    | 7054.31 | 7.58                       | 0.00                  |                          | -7.58              | 1000.00         |
      | 48        | 18 February 2019 | 152   | 48           | 50.00                 | 50.00                 |                     | 0.9500622698          | 47.50    | 7011.84 | 7.53                       | 0.00                  |                          | -7.53              | 1000.00         |
      | 49        | 19 February 2019 | 151   | 49           | 50.00                 | 50.00                 |                     | 0.9490488616          | 47.45    | 6969.33 | 7.49                       | 0.00                  |                          | -7.49              | 1000.00         |
      | 50        | 20 February 2019 | 150   | 50           | 50.00                 | 50.00                 |                     | 0.9480365345          | 47.40    | 6926.77 | 7.44                       | 0.00                  |                          | -7.44              | 1000.00         |
      | 51        | 21 February 2019 | 149   | 51           | 50.00                 | 50.00                 |                     | 0.9470252872          | 47.35    | 6884.17 | 7.40                       | 0.00                  |                          | -7.40              | 1000.00         |
      | 52        | 22 February 2019 | 148   | 52           | 50.00                 | 50.00                 |                     | 0.9460151185          | 47.30    | 6841.52 | 7.35                       | 0.00                  |                          | -7.35              | 1000.00         |
      | 53        | 23 February 2019 | 147   | 53           | 50.00                 | 50.00                 |                     | 0.9450060274          | 47.25    | 6798.82 | 7.31                       | 0.00                  |                          | -7.31              | 1000.00         |
      | 54        | 24 February 2019 | 146   | 54           | 50.00                 | 50.00                 |                     | 0.9439980126          | 47.20    | 6756.08 | 7.26                       | 0.00                  |                          | -7.26              | 1000.00         |
      | 55        | 25 February 2019 | 145   | 55           | 50.00                 | 50.00                 |                     | 0.9429910731          | 47.15    | 6713.30 | 7.21                       | 0.00                  |                          | -7.21              | 1000.00         |
      | 56        | 26 February 2019 | 144   | 56           | 50.00                 | 50.00                 |                     | 0.9419852077          | 47.10    | 6670.47 | 7.17                       | 0.00                  |                          | -7.17              | 1000.00         |
      | 57        | 27 February 2019 | 143   | 57           | 50.00                 | 50.00                 |                     | 0.9409804151          | 47.05    | 6627.59 | 7.12                       | 0.00                  |                          | -7.12              | 1000.00         |
      | 58        | 28 February 2019 | 142   | 58           | 50.00                 | 50.00                 |                     | 0.9399766944          | 47.00    | 6584.67 | 7.08                       | 0.00                  |                          | -7.08              | 1000.00         |
      | 59        | 01 March 2019    | 141   | 59           | 50.00                 | 50.00                 |                     | 0.9389740443          | 46.95    | 6541.70 | 7.03                       | 0.00                  |                          | -7.03              | 1000.00         |
      | 60        | 02 March 2019    | 140   | 60           | 50.00                 | 50.00                 |                     | 0.9379724637          | 46.90    | 6498.68 | 6.99                       | 0.00                  |                          | -6.99              | 1000.00         |
      | 61        | 03 March 2019    | 139   | 61           | 50.00                 | 50.00                 |                     | 0.9369719515          | 46.85    | 6455.62 | 6.94                       | 0.00                  |                          | -6.94              | 1000.00         |
      | 62        | 04 March 2019    | 138   | 62           | 50.00                 | 50.00                 |                     | 0.9359725065          | 46.80    | 6412.51 | 6.89                       | 0.00                  |                          | -6.89              | 1000.00         |
      | 63        | 05 March 2019    | 137   | 63           | 50.00                 | 50.00                 |                     | 0.9349741276          | 46.75    | 6369.36 | 6.85                       | 0.00                  |                          | -6.85              | 1000.00         |
      | 64        | 06 March 2019    | 136   | 64           | 50.00                 | 50.00                 |                     | 0.9339768136          | 46.70    | 6326.16 | 6.80                       | 0.00                  |                          | -6.80              | 1000.00         |
      | 65        | 07 March 2019    | 135   | 65           | 50.00                 | 50.00                 |                     | 0.9329805635          | 46.65    | 6282.92 | 6.76                       | 0.00                  |                          | -6.76              | 1000.00         |
      | 66        | 08 March 2019    | 134   | 66           | 50.00                 | 50.00                 |                     | 0.931985376           | 46.60    | 6239.63 | 6.71                       | 0.00                  |                          | -6.71              | 1000.00         |
      | 67        | 09 March 2019    | 133   | 67           | 50.00                 | 50.00                 |                     | 0.93099125            | 46.55    | 6196.29 | 6.66                       | 0.00                  |                          | -6.66              | 1000.00         |
      | 68        | 10 March 2019    | 132   | 68           | 50.00                 | 50.00                 |                     | 0.9299981845          | 46.50    | 6152.91 | 6.62                       | 0.00                  |                          | -6.62              | 1000.00         |
      | 69        | 11 March 2019    | 131   | 69           | 50.00                 | 50.00                 |                     | 0.9290061782          | 46.45    | 6109.48 | 6.57                       | 0.00                  |                          | -6.57              | 1000.00         |
      | 70        | 12 March 2019    | 130   | 70           | 50.00                 | 50.00                 |                     | 0.9280152301          | 46.40    | 6066.00 | 6.52                       | 0.00                  |                          | -6.52              | 1000.00         |
      | 71        | 13 March 2019    | 129   | 71           | 50.00                 | 50.00                 |                     | 0.927025339           | 46.35    | 6022.48 | 6.48                       | 0.00                  |                          | -6.48              | 1000.00         |
      | 72        | 14 March 2019    | 128   | 72           | 50.00                 | 50.00                 |                     | 0.9260365038          | 46.30    | 5978.91 | 6.43                       | 0.00                  |                          | -6.43              | 1000.00         |
      | 73        | 15 March 2019    | 127   | 73           | 50.00                 | 50.00                 |                     | 0.9250487234          | 46.25    | 5935.29 | 6.38                       | 0.00                  |                          | -6.38              | 1000.00         |
      | 74        | 16 March 2019    | 126   | 74           | 50.00                 | 50.00                 |                     | 0.9240619966          | 46.20    | 5891.63 | 6.34                       | 0.00                  |                          | -6.34              | 1000.00         |
      | 75        | 17 March 2019    | 125   | 75           | 50.00                 | 50.00                 |                     | 0.9230763224          | 46.15    | 5847.92 | 6.29                       | 0.00                  |                          | -6.29              | 1000.00         |
      | 76        | 18 March 2019    | 124   | 76           | 50.00                 | 50.00                 |                     | 0.9220916995          | 46.10    | 5804.17 | 6.24                       | 0.00                  |                          | -6.24              | 1000.00         |
      | 77        | 19 March 2019    | 123   | 77           | 50.00                 | 50.00                 |                     | 0.9211081269          | 46.06    | 5760.36 | 6.20                       | 0.00                  |                          | -6.20              | 1000.00         |
      | 78        | 20 March 2019    | 122   | 78           | 50.00                 | 50.00                 |                     | 0.9201256034          | 46.01    | 5716.52 | 6.15                       | 0.00                  |                          | -6.15              | 1000.00         |
      | 79        | 21 March 2019    | 121   | 79           | 50.00                 | 50.00                 |                     | 0.919144128           | 45.96    | 5672.62 | 6.10                       | 0.00                  |                          | -6.10              | 1000.00         |
      | 80        | 22 March 2019    | 120   | 80           | 50.00                 | 50.00                 |                     | 0.9181636995          | 45.91    | 5628.68 | 6.06                       | 0.00                  |                          | -6.06              | 1000.00         |
      | 81        | 23 March 2019    | 119   | 81           | 50.00                 | 50.00                 |                     | 0.9171843168          | 45.86    | 5584.69 | 6.01                       | 0.00                  |                          | -6.01              | 1000.00         |
      | 82        | 24 March 2019    | 118   | 82           | 50.00                 | 50.00                 |                     | 0.9162059788          | 45.81    | 5540.65 | 5.96                       | 0.00                  |                          | -5.96              | 1000.00         |
      | 83        | 25 March 2019    | 117   | 83           | 50.00                 | 50.00                 |                     | 0.9152286843          | 45.76    | 5496.57 | 5.92                       | 0.00                  |                          | -5.92              | 1000.00         |
      | 84        | 26 March 2019    | 116   | 84           | 50.00                 | 50.00                 |                     | 0.9142524323          | 45.71    | 5452.44 | 5.87                       | 0.00                  |                          | -5.87              | 1000.00         |
      | 85        | 27 March 2019    | 115   | 85           | 50.00                 | 50.00                 |                     | 0.9132772217          | 45.66    | 5408.26 | 5.82                       | 0.00                  |                          | -5.82              | 1000.00         |
      | 86        | 28 March 2019    | 114   | 86           | 50.00                 | 50.00                 |                     | 0.9123030513          | 45.62    | 5364.03 | 5.78                       | 0.00                  |                          | -5.78              | 1000.00         |
      | 87        | 29 March 2019    | 113   | 87           | 50.00                 | 50.00                 |                     | 0.91132992            | 45.57    | 5319.76 | 5.73                       | 0.00                  |                          | -5.73              | 1000.00         |
      | 88        | 30 March 2019    | 112   | 88           | 50.00                 | 50.00                 |                     | 0.9103578267          | 45.52    | 5275.44 | 5.68                       | 0.00                  |                          | -5.68              | 1000.00         |
      | 89        | 31 March 2019    | 111   | 89           | 50.00                 | 50.00                 |                     | 0.9093867703          | 45.47    | 5231.08 | 5.63                       | 0.00                  |                          | -5.63              | 1000.00         |
      | 90        | 01 April 2019    | 110   | 90           | 50.00                 | 50.00                 |                     | 0.9084167498          | 45.42    | 5186.66 | 5.59                       | 0.00                  |                          | -5.59              | 1000.00         |
      | 91        | 02 April 2019    | 109   | 91           | 50.00                 | 50.00                 |                     | 0.9074477639          | 45.37    | 5142.20 | 5.54                       | 0.00                  |                          | -5.54              | 1000.00         |
      | 92        | 03 April 2019    | 108   | 92           | 50.00                 | 50.00                 |                     | 0.9064798116          | 45.32    | 5097.69 | 5.49                       | 0.00                  |                          | -5.49              | 1000.00         |
      | 93        | 04 April 2019    | 107   | 93           | 50.00                 | 50.00                 |                     | 0.9055128918          | 45.28    | 5053.13 | 5.44                       | 0.00                  |                          | -5.44              | 1000.00         |
      | 94        | 05 April 2019    | 106   | 94           | 50.00                 | 50.00                 |                     | 0.9045470035          | 45.23    | 5008.53 | 5.40                       | 0.00                  |                          | -5.40              | 1000.00         |
      | 95        | 06 April 2019    | 105   | 95           | 50.00                 | 50.00                 |                     | 0.9035821453          | 45.18    | 4963.88 | 5.35                       | 0.00                  |                          | -5.35              | 1000.00         |
      | 96        | 07 April 2019    | 104   | 96           | 50.00                 | 50.00                 |                     | 0.9026183164          | 45.13    | 4919.18 | 5.30                       | 0.00                  |                          | -5.30              | 1000.00         |
      | 97        | 08 April 2019    | 103   | 97           | 50.00                 | 50.00                 |                     | 0.9016555156          | 45.08    | 4874.43 | 5.25                       | 0.00                  |                          | -5.25              | 1000.00         |
      | 98        | 09 April 2019    | 102   | 98           | 50.00                 | 50.00                 |                     | 0.9006937418          | 45.03    | 4829.64 | 5.20                       | 0.00                  |                          | -5.20              | 1000.00         |
      | 99        | 10 April 2019    | 101   | 99           | 50.00                 | 50.00                 |                     | 0.8997329939          | 44.99    | 4784.79 | 5.16                       | 0.00                  |                          | -5.16              | 1000.00         |
      | 100       | 11 April 2019    | 100   | 100          | 50.00                 | 50.00                 |                     | 0.8987732707          | 44.94    | 4739.90 | 5.11                       | 0.00                  |                          | -5.11              | 1000.00         |
      | 101       | 12 April 2019    | 99    | 101          | 50.00                 | 50.00                 |                     | 0.8978145713          | 44.89    | 4694.96 | 5.06                       | 0.00                  |                          | -5.06              | 1000.00         |
      | 102       | 13 April 2019    | 98    | 102          | 50.00                 | 50.00                 |                     | 0.8968568945          | 44.84    | 4649.98 | 5.01                       | 0.00                  |                          | -5.01              | 1000.00         |
      | 103       | 14 April 2019    | 97    | 103          | 50.00                 | 50.00                 |                     | 0.8959002393          | 44.80    | 4604.94 | 4.97                       | 0.00                  |                          | -4.97              | 1000.00         |
      | 104       | 15 April 2019    | 96    | 104          | 50.00                 | 50.00                 |                     | 0.8949446045          | 44.75    | 4559.86 | 4.92                       | 0.00                  |                          | -4.92              | 1000.00         |
      | 105       | 16 April 2019    | 95    | 105          | 50.00                 | 50.00                 |                     | 0.893989989           | 44.70    | 4514.73 | 4.87                       | 0.00                  |                          | -4.87              | 1000.00         |
      | 106       | 17 April 2019    | 94    | 106          | 50.00                 | 50.00                 |                     | 0.8930363918          | 44.65    | 4469.55 | 4.82                       | 0.00                  |                          | -4.82              | 1000.00         |
      | 107       | 18 April 2019    | 93    | 107          | 50.00                 | 50.00                 |                     | 0.8920838118          | 44.60    | 4424.32 | 4.77                       | 0.00                  |                          | -4.77              | 1000.00         |
      | 108       | 19 April 2019    | 92    | 108          | 50.00                 | 50.00                 |                     | 0.8911322479          | 44.56    | 4379.05 | 4.72                       | 0.00                  |                          | -4.72              | 1000.00         |
      | 109       | 20 April 2019    | 91    | 109          | 50.00                 | 50.00                 |                     | 0.890181699           | 44.51    | 4333.72 | 4.68                       | 0.00                  |                          | -4.68              | 1000.00         |
      | 110       | 21 April 2019    | 90    | 110          | 50.00                 | 50.00                 |                     | 0.889232164           | 44.46    | 4288.35 | 4.63                       | 0.00                  |                          | -4.63              | 1000.00         |
      | 111       | 22 April 2019    | 89    | 111          | 50.00                 | 50.00                 |                     | 0.8882836418          | 44.41    | 4242.93 | 4.58                       | 0.00                  |                          | -4.58              | 1000.00         |
      | 112       | 23 April 2019    | 88    | 112          | 50.00                 | 50.00                 |                     | 0.8873361314          | 44.37    | 4197.46 | 4.53                       | 0.00                  |                          | -4.53              | 1000.00         |
      | 113       | 24 April 2019    | 87    | 113          | 50.00                 | 50.00                 |                     | 0.8863896318          | 44.32    | 4151.94 | 4.48                       | 0.00                  |                          | -4.48              | 1000.00         |
      | 114       | 25 April 2019    | 86    | 114          | 50.00                 | 50.00                 |                     | 0.8854441417          | 44.27    | 4106.38 | 4.43                       | 0.00                  |                          | -4.43              | 1000.00         |
      | 115       | 26 April 2019    | 85    | 115          | 50.00                 | 50.00                 |                     | 0.8844996601          | 44.22    | 4060.76 | 4.38                       | 0.00                  |                          | -4.38              | 1000.00         |
      | 116       | 27 April 2019    | 84    | 116          | 50.00                 | 50.00                 |                     | 0.883556186           | 44.18    | 4015.10 | 4.34                       | 0.00                  |                          | -4.34              | 1000.00         |
      | 117       | 28 April 2019    | 83    | 117          | 50.00                 | 50.00                 |                     | 0.8826137183          | 44.13    | 3969.38 | 4.29                       | 0.00                  |                          | -4.29              | 1000.00         |
      | 118       | 29 April 2019    | 82    | 118          | 50.00                 | 50.00                 |                     | 0.8816722559          | 44.08    | 3923.62 | 4.24                       | 0.00                  |                          | -4.24              | 1000.00         |
      | 119       | 30 April 2019    | 81    | 119          | 50.00                 | 50.00                 |                     | 0.8807317977          | 44.04    | 3877.81 | 4.19                       | 0.00                  |                          | -4.19              | 1000.00         |
      | 120       | 01 May 2019      | 80    | 120          | 50.00                 | 50.00                 |                     | 0.8797923427          | 43.99    | 3831.95 | 4.14                       | 0.00                  |                          | -4.14              | 1000.00         |
      | 121       | 02 May 2019      | 79    | 121          | 50.00                 | 50.00                 |                     | 0.8788538898          | 43.94    | 3786.04 | 4.09                       | 0.00                  |                          | -4.09              | 1000.00         |
      | 122       | 03 May 2019      | 78    | 122          | 50.00                 | 50.00                 |                     | 0.8779164379          | 43.90    | 3740.09 | 4.04                       | 0.00                  |                          | -4.04              | 1000.00         |
      | 123       | 04 May 2019      | 77    | 123          | 50.00                 | 50.00                 |                     | 0.876979986           | 43.85    | 3694.08 | 3.99                       | 0.00                  |                          | -3.99              | 1000.00         |
      | 124       | 05 May 2019      | 76    | 124          | 50.00                 | 50.00                 |                     | 0.8760445329          | 43.80    | 3648.03 | 3.94                       | 0.00                  |                          | -3.94              | 1000.00         |
      | 125       | 06 May 2019      | 75    | 125          | 50.00                 | 50.00                 |                     | 0.8751100777          | 43.76    | 3601.92 | 3.90                       | 0.00                  |                          | -3.90              | 1000.00         |
      | 126       | 07 May 2019      | 74    | 126          | 50.00                 | 50.00                 |                     | 0.8741766193          | 43.71    | 3555.77 | 3.85                       | 0.00                  |                          | -3.85              | 1000.00         |
      | 127       | 08 May 2019      | 73    | 127          | 50.00                 | 50.00                 |                     | 0.8732441565          | 43.66    | 3509.56 | 3.80                       | 0.00                  |                          | -3.80              | 1000.00         |
      | 128       | 09 May 2019      | 72    | 128          | 50.00                 | 50.00                 |                     | 0.8723126884          | 43.62    | 3463.31 | 3.75                       | 0.00                  |                          | -3.75              | 1000.00         |
      | 129       | 10 May 2019      | 71    | 129          | 50.00                 | 50.00                 |                     | 0.8713822138          | 43.57    | 3417.01 | 3.70                       | 0.00                  |                          | -3.70              | 1000.00         |
      | 130       | 11 May 2019      | 70    | 130          | 50.00                 | 50.00                 |                     | 0.8704527318          | 43.52    | 3370.66 | 3.65                       | 0.00                  |                          | -3.65              | 1000.00         |
      | 131       | 12 May 2019      | 69    | 131          | 50.00                 | 50.00                 |                     | 0.8695242412          | 43.48    | 3324.26 | 3.60                       | 0.00                  |                          | -3.60              | 1000.00         |
      | 132       | 13 May 2019      | 68    | 132          | 50.00                 | 50.00                 |                     | 0.868596741           | 43.43    | 3277.81 | 3.55                       | 0.00                  |                          | -3.55              | 1000.00         |
      | 133       | 14 May 2019      | 67    | 133          | 50.00                 | 50.00                 |                     | 0.8676702302          | 43.38    | 3231.31 | 3.50                       | 0.00                  |                          | -3.50              | 1000.00         |
      | 134       | 15 May 2019      | 66    | 134          | 50.00                 | 50.00                 |                     | 0.8667447076          | 43.34    | 3184.76 | 3.45                       | 0.00                  |                          | -3.45              | 1000.00         |
      | 135       | 16 May 2019      | 65    | 135          | 50.00                 | 50.00                 |                     | 0.8658201723          | 43.29    | 3138.16 | 3.40                       | 0.00                  |                          | -3.40              | 1000.00         |
      | 136       | 17 May 2019      | 64    | 136          | 50.00                 | 50.00                 |                     | 0.8648966231          | 43.24    | 3091.51 | 3.35                       | 0.00                  |                          | -3.35              | 1000.00         |
      | 137       | 18 May 2019      | 63    | 137          | 50.00                 | 50.00                 |                     | 0.8639740591          | 43.20    | 3044.81 | 3.30                       | 0.00                  |                          | -3.30              | 1000.00         |
      | 138       | 19 May 2019      | 62    | 138          | 50.00                 | 50.00                 |                     | 0.8630524792          | 43.15    | 2998.06 | 3.25                       | 0.00                  |                          | -3.25              | 1000.00         |
      | 139       | 20 May 2019      | 61    | 139          | 50.00                 | 50.00                 |                     | 0.8621318823          | 43.11    | 2951.26 | 3.20                       | 0.00                  |                          | -3.20              | 1000.00         |
      | 140       | 21 May 2019      | 60    | 140          | 50.00                 | 50.00                 |                     | 0.8612122673          | 43.06    | 2904.42 | 3.15                       | 0.00                  |                          | -3.15              | 1000.00         |
      | 141       | 22 May 2019      | 59    | 141          | 50.00                 | 50.00                 |                     | 0.8602936333          | 43.01    | 2857.52 | 3.10                       | 0.00                  |                          | -3.10              | 1000.00         |
      | 142       | 23 May 2019      | 58    | 142          | 50.00                 | 50.00                 |                     | 0.8593759792          | 42.97    | 2810.57 | 3.05                       | 0.00                  |                          | -3.05              | 1000.00         |
      | 143       | 24 May 2019      | 57    | 143          | 50.00                 | 50.00                 |                     | 0.8584593039          | 42.92    | 2763.57 | 3.00                       | 0.00                  |                          | -3.00              | 1000.00         |
      | 144       | 25 May 2019      | 56    | 144          | 50.00                 | 50.00                 |                     | 0.8575436064          | 42.88    | 2716.52 | 2.95                       | 0.00                  |                          | -2.95              | 1000.00         |
      | 145       | 26 May 2019      | 55    | 145          | 50.00                 | 50.00                 |                     | 0.8566288857          | 42.83    | 2669.42 | 2.90                       | 0.00                  |                          | -2.90              | 1000.00         |
      | 146       | 27 May 2019      | 54    | 146          | 50.00                 | 50.00                 |                     | 0.8557151407          | 42.79    | 2622.27 | 2.85                       | 0.00                  |                          | -2.85              | 1000.00         |
      | 147       | 28 May 2019      | 53    | 147          | 50.00                 | 50.00                 |                     | 0.8548023703          | 42.74    | 2575.07 | 2.80                       | 0.00                  |                          | -2.80              | 1000.00         |
      | 148       | 29 May 2019      | 52    | 148          | 50.00                 | 50.00                 |                     | 0.8538905736          | 42.69    | 2527.82 | 2.75                       | 0.00                  |                          | -2.75              | 1000.00         |
      | 149       | 30 May 2019      | 51    | 149          | 50.00                 | 50.00                 |                     | 0.8529797495          | 42.65    | 2480.52 | 2.70                       | 0.00                  |                          | -2.70              | 1000.00         |
      | 150       | 31 May 2019      | 50    | 150          | 50.00                 | 50.00                 |                     | 0.8520698969          | 42.60    | 2433.17 | 2.65                       | 0.00                  |                          | -2.65              | 1000.00         |
      | 151       | 01 June 2019     | 49    | 151          | 50.00                 | 50.00                 |                     | 0.8511610148          | 42.56    | 2385.77 | 2.60                       | 0.00                  |                          | -2.60              | 1000.00         |
      | 152       | 02 June 2019     | 48    | 152          | 50.00                 | 50.00                 |                     | 0.8502531022          | 42.51    | 2338.31 | 2.55                       | 0.00                  |                          | -2.55              | 1000.00         |
      | 153       | 03 June 2019     | 47    | 153          | 50.00                 | 50.00                 |                     | 0.8493461581          | 42.47    | 2290.81 | 2.50                       | 0.00                  |                          | -2.50              | 1000.00         |
      | 154       | 04 June 2019     | 46    | 154          | 50.00                 | 50.00                 |                     | 0.8484401814          | 42.42    | 2243.26 | 2.45                       | 0.00                  |                          | -2.45              | 1000.00         |
      | 155       | 05 June 2019     | 45    | 155          | 50.00                 | 50.00                 |                     | 0.8475351711          | 42.38    | 2195.65 | 2.40                       | 0.00                  |                          | -2.40              | 1000.00         |
      | 156       | 06 June 2019     | 44    | 156          | 50.00                 | 50.00                 |                     | 0.8466311261          | 42.33    | 2148.00 | 2.34                       | 0.00                  |                          | -2.34              | 1000.00         |
      | 157       | 07 June 2019     | 43    | 157          | 50.00                 | 50.00                 |                     | 0.8457280454          | 42.29    | 2100.29 | 2.29                       | 0.00                  |                          | -2.29              | 1000.00         |
      | 158       | 08 June 2019     | 42    | 158          | 50.00                 | 50.00                 |                     | 0.844825928           | 42.24    | 2052.53 | 2.24                       | 0.00                  |                          | -2.24              | 1000.00         |
      | 159       | 09 June 2019     | 41    | 159          | 50.00                 | 50.00                 |                     | 0.8439247729          | 42.20    | 2004.73 | 2.19                       | 0.00                  |                          | -2.19              | 1000.00         |
      | 160       | 10 June 2019     | 40    | 160          | 50.00                 | 50.00                 |                     | 0.8430245791          | 42.15    | 1956.87 | 2.14                       | 0.00                  |                          | -2.14              | 1000.00         |
      | 161       | 11 June 2019     | 39    | 161          | 50.00                 | 50.00                 |                     | 0.8421253454          | 42.11    | 1908.96 | 2.09                       | 0.00                  |                          | -2.09              | 1000.00         |
      | 162       | 12 June 2019     | 38    | 162          | 50.00                 | 50.00                 |                     | 0.841227071           | 42.06    | 1860.99 | 2.04                       | 0.00                  |                          | -2.04              | 1000.00         |
      | 163       | 13 June 2019     | 37    | 163          | 50.00                 | 50.00                 |                     | 0.8403297547          | 42.02    | 1812.98 | 1.99                       | 0.00                  |                          | -1.99              | 1000.00         |
      | 164       | 14 June 2019     | 36    | 164          | 50.00                 | 50.00                 |                     | 0.8394333956          | 41.97    | 1764.92 | 1.94                       | 0.00                  |                          | -1.94              | 1000.00         |
      | 165       | 15 June 2019     | 35    | 165          | 50.00                 | 50.00                 |                     | 0.8385379925          | 41.93    | 1716.80 | 1.88                       | 0.00                  |                          | -1.88              | 1000.00         |
      | 166       | 16 June 2019     | 34    | 166          | 50.00                 | 50.00                 |                     | 0.8376435446          | 41.88    | 1668.64 | 1.83                       | 0.00                  |                          | -1.83              | 1000.00         |
      | 167       | 17 June 2019     | 33    | 167          | 50.00                 | 50.00                 |                     | 0.8367500508          | 41.84    | 1620.42 | 1.78                       | 0.00                  |                          | -1.78              | 1000.00         |
      | 168       | 18 June 2019     | 32    | 168          | 50.00                 | 50.00                 |                     | 0.83585751            | 41.79    | 1572.15 | 1.73                       | 0.00                  |                          | -1.73              | 1000.00         |
      | 169       | 19 June 2019     | 31    | 169          | 50.00                 | 50.00                 |                     | 0.8349659213          | 41.75    | 1523.83 | 1.68                       | 0.00                  |                          | -1.68              | 1000.00         |
      | 170       | 20 June 2019     | 30    | 170          | 50.00                 | 50.00                 |                     | 0.8340752837          | 41.70    | 1475.45 | 1.63                       | 0.00                  |                          | -1.63              | 1000.00         |
      | 171       | 21 June 2019     | 29    | 171          | 50.00                 | 50.00                 |                     | 0.833185596           | 41.66    | 1427.03 | 1.58                       | 0.00                  |                          | -1.58              | 1000.00         |
      | 172       | 22 June 2019     | 28    | 172          | 50.00                 | 50.00                 |                     | 0.8322968574          | 41.61    | 1378.55 | 1.52                       | 0.00                  |                          | -1.52              | 1000.00         |
      | 173       | 23 June 2019     | 27    | 173          | 50.00                 | 50.00                 |                     | 0.8314090667          | 41.57    | 1330.02 | 1.47                       | 0.00                  |                          | -1.47              | 1000.00         |
      | 174       | 24 June 2019     | 26    | 174          | 50.00                 | 50.00                 |                     | 0.8305222231          | 41.53    | 1281.45 | 1.42                       | 0.00                  |                          | -1.42              | 1000.00         |
      | 175       | 25 June 2019     | 25    | 175          | 50.00                 | 50.00                 |                     | 0.8296363254          | 41.48    | 1232.81 | 1.37                       | 0.00                  |                          | -1.37              | 1000.00         |
      | 176       | 26 June 2019     | 24    | 176          | 50.00                 | 50.00                 |                     | 0.8287513727          | 41.44    | 1184.13 | 1.32                       | 0.00                  |                          | -1.32              | 1000.00         |
      | 177       | 27 June 2019     | 23    | 177          | 50.00                 | 50.00                 |                     | 0.8278673639          | 41.39    | 1135.39 | 1.26                       | 0.00                  |                          | -1.26              | 1000.00         |
      | 178       | 28 June 2019     | 22    | 178          | 50.00                 | 50.00                 |                     | 0.8269842981          | 41.35    | 1086.61 | 1.21                       | 0.00                  |                          | -1.21              | 1000.00         |
      | 179       | 29 June 2019     | 21    | 179          | 50.00                 | 50.00                 |                     | 0.8261021742          | 41.31    | 1037.77 | 1.16                       | 0.00                  |                          | -1.16              | 1000.00         |
      | 180       | 30 June 2019     | 20    | 180          | 50.00                 | 50.00                 |                     | 0.8252209913          | 41.26    | 988.88  | 1.11                       | 0.00                  |                          | -1.11              | 1000.00         |
      | 181       | 01 July 2019     | 19    | 181          | 50.00                 | 50.00                 |                     | 0.8243407483          | 41.22    | 939.93  | 1.06                       | 0.00                  |                          | -1.06              | 1000.00         |
      | 182       | 02 July 2019     | 18    | 182          | 50.00                 | 50.00                 |                     | 0.8234614442          | 41.17    | 890.93  | 1.00                       | 0.00                  |                          | -1.00              | 1000.00         |
      | 183       | 03 July 2019     | 17    | 183          | 50.00                 | 50.00                 |                     | 0.8225830781          | 41.13    | 841.89  | 0.95                       | 0.00                  |                          | -0.95              | 1000.00         |
      | 184       | 04 July 2019     | 16    | 184          | 50.00                 | 50.00                 |                     | 0.8217056489          | 41.09    | 792.79  | 0.90                       | 0.00                  |                          | -0.90              | 1000.00         |
      | 185       | 05 July 2019     | 15    | 185          | 50.00                 | 50.00                 |                     | 0.8208291556          | 41.04    | 743.63  | 0.85                       | 0.00                  |                          | -0.85              | 1000.00         |
      | 186       | 06 July 2019     | 14    | 186          | 50.00                 | 50.00                 |                     | 0.8199535973          | 41.00    | 694.43  | 0.79                       | 0.00                  |                          | -0.79              | 1000.00         |
      | 187       | 07 July 2019     | 13    | 187          | 50.00                 | 50.00                 |                     | 0.8190789729          | 40.95    | 645.17  | 0.74                       | 0.00                  |                          | -0.74              | 1000.00         |
      | 188       | 08 July 2019     | 12    | 188          | 50.00                 | 50.00                 |                     | 0.8182052815          | 40.91    | 595.86  | 0.69                       | 0.00                  |                          | -0.69              | 1000.00         |
      | 189       | 09 July 2019     | 11    | 189          | 50.00                 | 50.00                 |                     | 0.8173325219          | 40.87    | 546.49  | 0.64                       | 0.00                  |                          | -0.64              | 1000.00         |
      | 190       | 10 July 2019     | 10    | 190          | 50.00                 | 50.00                 |                     | 0.8164606934          | 40.82    | 497.08  | 0.58                       | 0.00                  |                          | -0.58              | 1000.00         |
      | 191       | 11 July 2019     | 9     | 191          | 50.00                 | 50.00                 |                     | 0.8155897948          | 40.78    | 447.61  | 0.53                       | 0.00                  |                          | -0.53              | 1000.00         |
      | 192       | 12 July 2019     | 8     | 192          | 50.00                 | 50.00                 |                     | 0.8147198252          | 40.74    | 398.08  | 0.48                       | 0.00                  |                          | -0.48              | 1000.00         |
      | 193       | 13 July 2019     | 7     | 193          | 50.00                 | 50.00                 |                     | 0.8138507835          | 40.69    | 348.51  | 0.43                       | 0.00                  |                          | -0.43              | 1000.00         |
      | 194       | 14 July 2019     | 6     | 194          | 50.00                 | 50.00                 |                     | 0.8129826688          | 40.65    | 298.88  | 0.37                       | 0.00                  |                          | -0.37              | 1000.00         |
      | 195       | 15 July 2019     | 5     | 195          | 50.00                 | 50.00                 |                     | 0.8121154802          | 40.61    | 249.20  | 0.32                       | 0.00                  |                          | -0.32              | 1000.00         |
      | 196       | 16 July 2019     | 4     | 196          | 50.00                 | 50.00                 |                     | 0.8112492165          | 40.56    | 199.47  | 0.27                       | 0.00                  |                          | -0.27              | 1000.00         |
      | 197       | 17 July 2019     | 3     | 197          | 50.00                 | 50.00                 |                     | 0.8103838768          | 40.52    | 149.68  | 0.21                       | 0.00                  |                          | -0.21              | 1000.00         |
      | 198       | 18 July 2019     | 2     | 198          | 50.00                 | 50.00                 |                     | 0.8095194602          | 40.48    | 99.84   | 0.16                       | 0.00                  |                          | -0.16              | 1000.00         |
      | 199       | 19 July 2019     | 1     | 199          | 50.00                 | 50.00                 |                     | 0.8086559657          | 40.43    | 49.95   | 0.11                       | 0.00                  |                          | -0.11              | 1000.00         |
      | 200       | 20 July 2019     | 0     | 200          | 50.00                 | 50.00                 |                     | 0.8077933922          | 40.39    | 0.00    | 0.05                       | 0.00                  |                          | -0.05              | 1000.00         |
    When Admin sets the business date to "02 January 2019"
    And Customer makes repayment on "02 January 2019" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2019"
    And Customer makes repayment on "03 January 2019" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2019"
    And Customer makes repayment on "04 January 2019" with 50 transaction amount on Working Capital loan
    Then Working Capital loan amortization schedule has 201 periods, with the following data for periods:
      | paymentNo | paymentDate      | count | paymentsLeft | expectedPaymentAmount | forecastPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | netAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2019  | 203   | 0            | -9000.00              |                       |                     | 1                     | -9000.00 | 9000.00 |                            |                       |                          |                    | 1000.00         |
      | 1         | 02 January 2019  | 202   | 0            | 50.00                 | 50.00                 | 50.00               | 1                     | 50.00    | 8959.61 | 9.61                       | 28.70                 | 9.61                     | 0.00               | 990.39          |
      | 2         | 03 January 2019  | 201   | 0            | 50.00                 | 50.00                 | 50.00               | 1                     | 50.00    | 8919.18 | 9.57                       | 19.09                 | 9.57                     | 0.00               | 980.82          |
      | 3         | 04 January 2019  | 200   | 0            | 50.00                 | 50.00                 | 50.00               | 1                     | 50.00    | 8878.70 | 9.52                       | 9.52                  | 9.52                     | 0.00               | 971.30          |
      | 4         | 05 January 2019  | 199   | 1            | 50.00                 | 50.00                 |                     | 0.9989333245          | 49.95    | 8838.18 | 9.48                       | 9.52                  |                          | -9.48              | 971.30          |
      | 5         | 06 January 2019  | 198   | 2            | 50.00                 | 50.00                 |                     | 0.9978677868          | 49.89    | 8797.62 | 9.44                       | 9.52                  |                          | -9.44              | 971.30          |
      | 6         | 07 January 2019  | 197   | 3            | 50.00                 | 50.00                 |                     | 0.9968033857          | 49.84    | 8757.01 | 9.39                       | 9.52                  |                          | -9.39              | 971.30          |
      | 7         | 08 January 2019  | 196   | 4            | 50.00                 | 50.00                 |                     | 0.99574012            | 49.79    | 8716.36 | 9.35                       | 9.52                  |                          | -9.35              | 971.30          |
      | 8         | 09 January 2019  | 195   | 5            | 50.00                 | 50.00                 |                     | 0.9946779885          | 49.73    | 8675.67 | 9.31                       | 9.52                  |                          | -9.31              | 971.30          |
      | 9         | 10 January 2019  | 194   | 6            | 50.00                 | 50.00                 |                     | 0.9936169898          | 49.68    | 8634.94 | 9.26                       | 9.52                  |                          | -9.26              | 971.30          |
      | 10        | 11 January 2019  | 193   | 7            | 50.00                 | 50.00                 |                     | 0.992557123           | 49.63    | 8594.16 | 9.22                       | 9.52                  |                          | -9.22              | 971.30          |
      | 11        | 12 January 2019  | 192   | 8            | 50.00                 | 50.00                 |                     | 0.9914983866          | 49.57    | 8553.33 | 9.18                       | 9.52                  |                          | -9.18              | 971.30          |
      | 12        | 13 January 2019  | 191   | 9            | 50.00                 | 50.00                 |                     | 0.9904407796          | 49.52    | 8512.47 | 9.13                       | 9.52                  |                          | -9.13              | 971.30          |
      | 13        | 14 January 2019  | 190   | 10           | 50.00                 | 50.00                 |                     | 0.9893843007          | 49.47    | 8471.56 | 9.09                       | 9.52                  |                          | -9.09              | 971.30          |
      | 14        | 15 January 2019  | 189   | 11           | 50.00                 | 50.00                 |                     | 0.9883289487          | 49.42    | 8430.60 | 9.05                       | 9.52                  |                          | -9.05              | 971.30          |
      | 15        | 16 January 2019  | 188   | 12           | 50.00                 | 50.00                 |                     | 0.9872747225          | 49.36    | 8389.61 | 9.00                       | 9.52                  |                          | -9.00              | 971.30          |
      | 16        | 17 January 2019  | 187   | 13           | 50.00                 | 50.00                 |                     | 0.9862216208          | 49.31    | 8348.56 | 8.96                       | 9.52                  |                          | -8.96              | 971.30          |
      | 17        | 18 January 2019  | 186   | 14           | 50.00                 | 50.00                 |                     | 0.9851696423          | 49.26    | 8307.48 | 8.91                       | 9.52                  |                          | -8.91              | 971.30          |
      | 18        | 19 January 2019  | 185   | 15           | 50.00                 | 50.00                 |                     | 0.984118786           | 49.21    | 8266.35 | 8.87                       | 9.52                  |                          | -8.87              | 971.30          |
      | 19        | 20 January 2019  | 184   | 16           | 50.00                 | 50.00                 |                     | 0.9830690507          | 49.15    | 8225.18 | 8.83                       | 9.52                  |                          | -8.83              | 971.30          |
      | 20        | 21 January 2019  | 183   | 17           | 50.00                 | 50.00                 |                     | 0.982020435           | 49.10    | 8183.96 | 8.78                       | 9.52                  |                          | -8.78              | 971.30          |
      | 21        | 22 January 2019  | 182   | 18           | 50.00                 | 50.00                 |                     | 0.9809729379          | 49.05    | 8142.70 | 8.74                       | 9.52                  |                          | -8.74              | 971.30          |
      | 22        | 23 January 2019  | 181   | 19           | 50.00                 | 50.00                 |                     | 0.9799265581          | 49.00    | 8101.39 | 8.69                       | 9.52                  |                          | -8.69              | 971.30          |
      | 23        | 24 January 2019  | 180   | 20           | 50.00                 | 50.00                 |                     | 0.9788812945          | 48.94    | 8060.04 | 8.65                       | 9.52                  |                          | -8.65              | 971.30          |
      | 24        | 25 January 2019  | 179   | 21           | 50.00                 | 50.00                 |                     | 0.9778371458          | 48.89    | 8018.65 | 8.61                       | 9.52                  |                          | -8.61              | 971.30          |
      | 25        | 26 January 2019  | 178   | 22           | 50.00                 | 50.00                 |                     | 0.9767941109          | 48.84    | 7977.21 | 8.56                       | 9.52                  |                          | -8.56              | 971.30          |
      | 26        | 27 January 2019  | 177   | 23           | 50.00                 | 50.00                 |                     | 0.9757521886          | 48.79    | 7935.73 | 8.52                       | 9.52                  |                          | -8.52              | 971.30          |
      | 27        | 28 January 2019  | 176   | 24           | 50.00                 | 50.00                 |                     | 0.9747113777          | 48.74    | 7894.21 | 8.47                       | 9.52                  |                          | -8.47              | 971.30          |
      | 28        | 29 January 2019  | 175   | 25           | 50.00                 | 50.00                 |                     | 0.9736716769498249835 | 48.68    | 7852.63 | 8.43                       | 9.52                  |                          | -8.43              | 971.30          |
      | 29        | 30 January 2019  | 174   | 26           | 50.00                 | 50.00                 |                     | 0.9726330853          | 48.63    | 7811.02 | 8.39                       | 9.52                  |                          | -8.39              | 971.30          |
      | 30        | 31 January 2019  | 173   | 27           | 50.00                 | 50.00                 |                     | 0.9715956014          | 48.58    | 7769.36 | 8.34                       | 9.52                  |                          | -8.34              | 971.30          |
      | 31        | 01 February 2019 | 172   | 28           | 50.00                 | 50.00                 |                     | 0.9705592242          | 48.53    | 7727.66 | 8.30                       | 9.52                  |                          | -8.30              | 971.30          |
      | 32        | 02 February 2019 | 171   | 29           | 50.00                 | 50.00                 |                     | 0.9695239525          | 48.48    | 7685.91 | 8.25                       | 9.52                  |                          | -8.25              | 971.30          |
      | 33        | 03 February 2019 | 170   | 30           | 50.00                 | 50.00                 |                     | 0.968489785           | 48.42    | 7644.12 | 8.21                       | 9.52                  |                          | -8.21              | 971.30          |
      | 34        | 04 February 2019 | 169   | 31           | 50.00                 | 50.00                 |                     | 0.9674567207          | 48.37    | 7602.28 | 8.16                       | 9.52                  |                          | -8.16              | 971.30          |
      | 35        | 05 February 2019 | 168   | 32           | 50.00                 | 50.00                 |                     | 0.9664247584          | 48.32    | 7560.40 | 8.12                       | 9.52                  |                          | -8.12              | 971.30          |
      | 36        | 06 February 2019 | 167   | 33           | 50.00                 | 50.00                 |                     | 0.9653938968          | 48.27    | 7518.47 | 8.07                       | 9.52                  |                          | -8.07              | 971.30          |
      | 37        | 07 February 2019 | 166   | 34           | 50.00                 | 50.00                 |                     | 0.9643641348          | 48.22    | 7476.50 | 8.03                       | 9.52                  |                          | -8.03              | 971.30          |
      | 38        | 08 February 2019 | 165   | 35           | 50.00                 | 50.00                 |                     | 0.9633354712          | 48.17    | 7434.48 | 7.98                       | 9.52                  |                          | -7.98              | 971.30          |
      | 39        | 09 February 2019 | 164   | 36           | 50.00                 | 50.00                 |                     | 0.9623079049          | 48.12    | 7392.42 | 7.94                       | 9.52                  |                          | -7.94              | 971.30          |
      | 40        | 10 February 2019 | 163   | 37           | 50.00                 | 50.00                 |                     | 0.9612814347          | 48.06    | 7350.31 | 7.89                       | 9.52                  |                          | -7.89              | 971.30          |
      | 41        | 11 February 2019 | 162   | 38           | 50.00                 | 50.00                 |                     | 0.9602560593          | 48.01    | 7308.16 | 7.85                       | 9.52                  |                          | -7.85              | 971.30          |
      | 42        | 12 February 2019 | 161   | 39           | 50.00                 | 50.00                 |                     | 0.9592317777          | 47.96    | 7265.97 | 7.80                       | 9.52                  |                          | -7.80              | 971.30          |
      | 43        | 13 February 2019 | 160   | 40           | 50.00                 | 50.00                 |                     | 0.9582085887          | 47.91    | 7223.72 | 7.76                       | 9.52                  |                          | -7.76              | 971.30          |
      | 44        | 14 February 2019 | 159   | 41           | 50.00                 | 50.00                 |                     | 0.9571864911          | 47.86    | 7181.44 | 7.71                       | 9.52                  |                          | -7.71              | 971.30          |
      | 45        | 15 February 2019 | 158   | 42           | 50.00                 | 50.00                 |                     | 0.9561654838          | 47.81    | 7139.11 | 7.67                       | 9.52                  |                          | -7.67              | 971.30          |
      | 46        | 16 February 2019 | 157   | 43           | 50.00                 | 50.00                 |                     | 0.9551455655          | 47.76    | 7096.73 | 7.62                       | 9.52                  |                          | -7.62              | 971.30          |
      | 47        | 17 February 2019 | 156   | 44           | 50.00                 | 50.00                 |                     | 0.9541267351          | 47.71    | 7054.31 | 7.58                       | 9.52                  |                          | -7.58              | 971.30          |
      | 48        | 18 February 2019 | 155   | 45           | 50.00                 | 50.00                 |                     | 0.9531089916          | 47.66    | 7011.84 | 7.53                       | 9.52                  |                          | -7.53              | 971.30          |
      | 49        | 19 February 2019 | 154   | 46           | 50.00                 | 50.00                 |                     | 0.9520923336          | 47.60    | 6969.33 | 7.49                       | 9.52                  |                          | -7.49              | 971.30          |
      | 50        | 20 February 2019 | 153   | 47           | 50.00                 | 50.00                 |                     | 0.95107676            | 47.55    | 6926.77 | 7.44                       | 9.52                  |                          | -7.44              | 971.30          |
      | 51        | 21 February 2019 | 152   | 48           | 50.00                 | 50.00                 |                     | 0.9500622698          | 47.50    | 6884.17 | 7.40                       | 9.52                  |                          | -7.40              | 971.30          |
      | 52        | 22 February 2019 | 151   | 49           | 50.00                 | 50.00                 |                     | 0.9490488616          | 47.45    | 6841.52 | 7.35                       | 9.52                  |                          | -7.35              | 971.30          |
      | 53        | 23 February 2019 | 150   | 50           | 50.00                 | 50.00                 |                     | 0.9480365345          | 47.40    | 6798.82 | 7.31                       | 9.52                  |                          | -7.31              | 971.30          |
      | 54        | 24 February 2019 | 149   | 51           | 50.00                 | 50.00                 |                     | 0.9470252872          | 47.35    | 6756.08 | 7.26                       | 9.52                  |                          | -7.26              | 971.30          |
      | 55        | 25 February 2019 | 148   | 52           | 50.00                 | 50.00                 |                     | 0.9460151185          | 47.30    | 6713.30 | 7.21                       | 9.52                  |                          | -7.21              | 971.30          |
      | 56        | 26 February 2019 | 147   | 53           | 50.00                 | 50.00                 |                     | 0.9450060274          | 47.25    | 6670.47 | 7.17                       | 9.52                  |                          | -7.17              | 971.30          |
      | 57        | 27 February 2019 | 146   | 54           | 50.00                 | 50.00                 |                     | 0.9439980126          | 47.20    | 6627.59 | 7.12                       | 9.52                  |                          | -7.12              | 971.30          |
      | 58        | 28 February 2019 | 145   | 55           | 50.00                 | 50.00                 |                     | 0.9429910731          | 47.15    | 6584.67 | 7.08                       | 9.52                  |                          | -7.08              | 971.30          |
      | 59        | 01 March 2019    | 144   | 56           | 50.00                 | 50.00                 |                     | 0.9419852077          | 47.10    | 6541.70 | 7.03                       | 9.52                  |                          | -7.03              | 971.30          |
      | 60        | 02 March 2019    | 143   | 57           | 50.00                 | 50.00                 |                     | 0.9409804151          | 47.05    | 6498.68 | 6.99                       | 9.52                  |                          | -6.99              | 971.30          |
      | 61        | 03 March 2019    | 142   | 58           | 50.00                 | 50.00                 |                     | 0.9399766944          | 47.00    | 6455.62 | 6.94                       | 9.52                  |                          | -6.94              | 971.30          |
      | 62        | 04 March 2019    | 141   | 59           | 50.00                 | 50.00                 |                     | 0.9389740443          | 46.95    | 6412.51 | 6.89                       | 9.52                  |                          | -6.89              | 971.30          |
      | 63        | 05 March 2019    | 140   | 60           | 50.00                 | 50.00                 |                     | 0.9379724637          | 46.90    | 6369.36 | 6.85                       | 9.52                  |                          | -6.85              | 971.30          |
      | 64        | 06 March 2019    | 139   | 61           | 50.00                 | 50.00                 |                     | 0.9369719515          | 46.85    | 6326.16 | 6.80                       | 9.52                  |                          | -6.80              | 971.30          |
      | 65        | 07 March 2019    | 138   | 62           | 50.00                 | 50.00                 |                     | 0.9359725065          | 46.80    | 6282.92 | 6.76                       | 9.52                  |                          | -6.76              | 971.30          |
      | 66        | 08 March 2019    | 137   | 63           | 50.00                 | 50.00                 |                     | 0.9349741276          | 46.75    | 6239.63 | 6.71                       | 9.52                  |                          | -6.71              | 971.30          |
      | 67        | 09 March 2019    | 136   | 64           | 50.00                 | 50.00                 |                     | 0.9339768136          | 46.70    | 6196.29 | 6.66                       | 9.52                  |                          | -6.66              | 971.30          |
      | 68        | 10 March 2019    | 135   | 65           | 50.00                 | 50.00                 |                     | 0.9329805635          | 46.65    | 6152.91 | 6.62                       | 9.52                  |                          | -6.62              | 971.30          |
      | 69        | 11 March 2019    | 134   | 66           | 50.00                 | 50.00                 |                     | 0.931985376           | 46.60    | 6109.48 | 6.57                       | 9.52                  |                          | -6.57              | 971.30          |
      | 70        | 12 March 2019    | 133   | 67           | 50.00                 | 50.00                 |                     | 0.93099125            | 46.55    | 6066.00 | 6.52                       | 9.52                  |                          | -6.52              | 971.30          |
      | 71        | 13 March 2019    | 132   | 68           | 50.00                 | 50.00                 |                     | 0.9299981845          | 46.50    | 6022.48 | 6.48                       | 9.52                  |                          | -6.48              | 971.30          |
      | 72        | 14 March 2019    | 131   | 69           | 50.00                 | 50.00                 |                     | 0.9290061782          | 46.45    | 5978.91 | 6.43                       | 9.52                  |                          | -6.43              | 971.30          |
      | 73        | 15 March 2019    | 130   | 70           | 50.00                 | 50.00                 |                     | 0.9280152301          | 46.40    | 5935.29 | 6.38                       | 9.52                  |                          | -6.38              | 971.30          |
      | 74        | 16 March 2019    | 129   | 71           | 50.00                 | 50.00                 |                     | 0.927025339           | 46.35    | 5891.63 | 6.34                       | 9.52                  |                          | -6.34              | 971.30          |
      | 75        | 17 March 2019    | 128   | 72           | 50.00                 | 50.00                 |                     | 0.9260365038          | 46.30    | 5847.92 | 6.29                       | 9.52                  |                          | -6.29              | 971.30          |
      | 76        | 18 March 2019    | 127   | 73           | 50.00                 | 50.00                 |                     | 0.9250487234          | 46.25    | 5804.17 | 6.24                       | 9.52                  |                          | -6.24              | 971.30          |
      | 77        | 19 March 2019    | 126   | 74           | 50.00                 | 50.00                 |                     | 0.9240619966          | 46.20    | 5760.36 | 6.20                       | 9.52                  |                          | -6.20              | 971.30          |
      | 78        | 20 March 2019    | 125   | 75           | 50.00                 | 50.00                 |                     | 0.9230763224          | 46.15    | 5716.52 | 6.15                       | 9.52                  |                          | -6.15              | 971.30          |
      | 79        | 21 March 2019    | 124   | 76           | 50.00                 | 50.00                 |                     | 0.9220916995          | 46.10    | 5672.62 | 6.10                       | 9.52                  |                          | -6.10              | 971.30          |
      | 80        | 22 March 2019    | 123   | 77           | 50.00                 | 50.00                 |                     | 0.9211081269          | 46.06    | 5628.68 | 6.06                       | 9.52                  |                          | -6.06              | 971.30          |
      | 81        | 23 March 2019    | 122   | 78           | 50.00                 | 50.00                 |                     | 0.9201256034          | 46.01    | 5584.69 | 6.01                       | 9.52                  |                          | -6.01              | 971.30          |
      | 82        | 24 March 2019    | 121   | 79           | 50.00                 | 50.00                 |                     | 0.919144128           | 45.96    | 5540.65 | 5.96                       | 9.52                  |                          | -5.96              | 971.30          |
      | 83        | 25 March 2019    | 120   | 80           | 50.00                 | 50.00                 |                     | 0.9181636995          | 45.91    | 5496.57 | 5.92                       | 9.52                  |                          | -5.92              | 971.30          |
      | 84        | 26 March 2019    | 119   | 81           | 50.00                 | 50.00                 |                     | 0.9171843168          | 45.86    | 5452.44 | 5.87                       | 9.52                  |                          | -5.87              | 971.30          |
      | 85        | 27 March 2019    | 118   | 82           | 50.00                 | 50.00                 |                     | 0.9162059788          | 45.81    | 5408.26 | 5.82                       | 9.52                  |                          | -5.82              | 971.30          |
      | 86        | 28 March 2019    | 117   | 83           | 50.00                 | 50.00                 |                     | 0.9152286843          | 45.76    | 5364.03 | 5.78                       | 9.52                  |                          | -5.78              | 971.30          |
      | 87        | 29 March 2019    | 116   | 84           | 50.00                 | 50.00                 |                     | 0.9142524323          | 45.71    | 5319.76 | 5.73                       | 9.52                  |                          | -5.73              | 971.30          |
      | 88        | 30 March 2019    | 115   | 85           | 50.00                 | 50.00                 |                     | 0.9132772217          | 45.66    | 5275.44 | 5.68                       | 9.52                  |                          | -5.68              | 971.30          |
      | 89        | 31 March 2019    | 114   | 86           | 50.00                 | 50.00                 |                     | 0.9123030513          | 45.62    | 5231.08 | 5.63                       | 9.52                  |                          | -5.63              | 971.30          |
      | 90        | 01 April 2019    | 113   | 87           | 50.00                 | 50.00                 |                     | 0.91132992            | 45.57    | 5186.66 | 5.59                       | 9.52                  |                          | -5.59              | 971.30          |
      | 91        | 02 April 2019    | 112   | 88           | 50.00                 | 50.00                 |                     | 0.9103578267          | 45.52    | 5142.20 | 5.54                       | 9.52                  |                          | -5.54              | 971.30          |
      | 92        | 03 April 2019    | 111   | 89           | 50.00                 | 50.00                 |                     | 0.9093867703          | 45.47    | 5097.69 | 5.49                       | 9.52                  |                          | -5.49              | 971.30          |
      | 93        | 04 April 2019    | 110   | 90           | 50.00                 | 50.00                 |                     | 0.9084167498          | 45.42    | 5053.13 | 5.44                       | 9.52                  |                          | -5.44              | 971.30          |
      | 94        | 05 April 2019    | 109   | 91           | 50.00                 | 50.00                 |                     | 0.9074477639          | 45.37    | 5008.53 | 5.40                       | 9.52                  |                          | -5.40              | 971.30          |
      | 95        | 06 April 2019    | 108   | 92           | 50.00                 | 50.00                 |                     | 0.9064798116          | 45.32    | 4963.88 | 5.35                       | 9.52                  |                          | -5.35              | 971.30          |
      | 96        | 07 April 2019    | 107   | 93           | 50.00                 | 50.00                 |                     | 0.9055128918          | 45.28    | 4919.18 | 5.30                       | 9.52                  |                          | -5.30              | 971.30          |
      | 97        | 08 April 2019    | 106   | 94           | 50.00                 | 50.00                 |                     | 0.9045470035          | 45.23    | 4874.43 | 5.25                       | 9.52                  |                          | -5.25              | 971.30          |
      | 98        | 09 April 2019    | 105   | 95           | 50.00                 | 50.00                 |                     | 0.9035821453          | 45.18    | 4829.64 | 5.20                       | 9.52                  |                          | -5.20              | 971.30          |
      | 99        | 10 April 2019    | 104   | 96           | 50.00                 | 50.00                 |                     | 0.9026183164          | 45.13    | 4784.79 | 5.16                       | 9.52                  |                          | -5.16              | 971.30          |
      | 100       | 11 April 2019    | 103   | 97           | 50.00                 | 50.00                 |                     | 0.9016555156          | 45.08    | 4739.90 | 5.11                       | 9.52                  |                          | -5.11              | 971.30          |
      | 101       | 12 April 2019    | 102   | 98           | 50.00                 | 50.00                 |                     | 0.9006937418          | 45.03    | 4694.96 | 5.06                       | 9.52                  |                          | -5.06              | 971.30          |
      | 102       | 13 April 2019    | 101   | 99           | 50.00                 | 50.00                 |                     | 0.8997329939          | 44.99    | 4649.98 | 5.01                       | 9.52                  |                          | -5.01              | 971.30          |
      | 103       | 14 April 2019    | 100   | 100          | 50.00                 | 50.00                 |                     | 0.8987732707          | 44.94    | 4604.94 | 4.97                       | 9.52                  |                          | -4.97              | 971.30          |
      | 104       | 15 April 2019    | 99    | 101          | 50.00                 | 50.00                 |                     | 0.8978145713          | 44.89    | 4559.86 | 4.92                       | 9.52                  |                          | -4.92              | 971.30          |
      | 105       | 16 April 2019    | 98    | 102          | 50.00                 | 50.00                 |                     | 0.8968568945          | 44.84    | 4514.73 | 4.87                       | 9.52                  |                          | -4.87              | 971.30          |
      | 106       | 17 April 2019    | 97    | 103          | 50.00                 | 50.00                 |                     | 0.8959002393          | 44.80    | 4469.55 | 4.82                       | 9.52                  |                          | -4.82              | 971.30          |
      | 107       | 18 April 2019    | 96    | 104          | 50.00                 | 50.00                 |                     | 0.8949446045          | 44.75    | 4424.32 | 4.77                       | 9.52                  |                          | -4.77              | 971.30          |
      | 108       | 19 April 2019    | 95    | 105          | 50.00                 | 50.00                 |                     | 0.893989989           | 44.70    | 4379.05 | 4.72                       | 9.52                  |                          | -4.72              | 971.30          |
      | 109       | 20 April 2019    | 94    | 106          | 50.00                 | 50.00                 |                     | 0.8930363918          | 44.65    | 4333.72 | 4.68                       | 9.52                  |                          | -4.68              | 971.30          |
      | 110       | 21 April 2019    | 93    | 107          | 50.00                 | 50.00                 |                     | 0.8920838118          | 44.60    | 4288.35 | 4.63                       | 9.52                  |                          | -4.63              | 971.30          |
      | 111       | 22 April 2019    | 92    | 108          | 50.00                 | 50.00                 |                     | 0.8911322479          | 44.56    | 4242.93 | 4.58                       | 9.52                  |                          | -4.58              | 971.30          |
      | 112       | 23 April 2019    | 91    | 109          | 50.00                 | 50.00                 |                     | 0.890181699           | 44.51    | 4197.46 | 4.53                       | 9.52                  |                          | -4.53              | 971.30          |
      | 113       | 24 April 2019    | 90    | 110          | 50.00                 | 50.00                 |                     | 0.889232164           | 44.46    | 4151.94 | 4.48                       | 9.52                  |                          | -4.48              | 971.30          |
      | 114       | 25 April 2019    | 89    | 111          | 50.00                 | 50.00                 |                     | 0.8882836418          | 44.41    | 4106.38 | 4.43                       | 9.52                  |                          | -4.43              | 971.30          |
      | 115       | 26 April 2019    | 88    | 112          | 50.00                 | 50.00                 |                     | 0.8873361314          | 44.37    | 4060.76 | 4.38                       | 9.52                  |                          | -4.38              | 971.30          |
      | 116       | 27 April 2019    | 87    | 113          | 50.00                 | 50.00                 |                     | 0.8863896318          | 44.32    | 4015.10 | 4.34                       | 9.52                  |                          | -4.34              | 971.30          |
      | 117       | 28 April 2019    | 86    | 114          | 50.00                 | 50.00                 |                     | 0.8854441417          | 44.27    | 3969.38 | 4.29                       | 9.52                  |                          | -4.29              | 971.30          |
      | 118       | 29 April 2019    | 85    | 115          | 50.00                 | 50.00                 |                     | 0.8844996601          | 44.22    | 3923.62 | 4.24                       | 9.52                  |                          | -4.24              | 971.30          |
      | 119       | 30 April 2019    | 84    | 116          | 50.00                 | 50.00                 |                     | 0.883556186           | 44.18    | 3877.81 | 4.19                       | 9.52                  |                          | -4.19              | 971.30          |
      | 120       | 01 May 2019      | 83    | 117          | 50.00                 | 50.00                 |                     | 0.8826137183          | 44.13    | 3831.95 | 4.14                       | 9.52                  |                          | -4.14              | 971.30          |
      | 121       | 02 May 2019      | 82    | 118          | 50.00                 | 50.00                 |                     | 0.8816722559          | 44.08    | 3786.04 | 4.09                       | 9.52                  |                          | -4.09              | 971.30          |
      | 122       | 03 May 2019      | 81    | 119          | 50.00                 | 50.00                 |                     | 0.8807317977          | 44.04    | 3740.09 | 4.04                       | 9.52                  |                          | -4.04              | 971.30          |
      | 123       | 04 May 2019      | 80    | 120          | 50.00                 | 50.00                 |                     | 0.8797923427          | 43.99    | 3694.08 | 3.99                       | 9.52                  |                          | -3.99              | 971.30          |
      | 124       | 05 May 2019      | 79    | 121          | 50.00                 | 50.00                 |                     | 0.8788538898          | 43.94    | 3648.03 | 3.94                       | 9.52                  |                          | -3.94              | 971.30          |
      | 125       | 06 May 2019      | 78    | 122          | 50.00                 | 50.00                 |                     | 0.8779164379          | 43.90    | 3601.92 | 3.90                       | 9.52                  |                          | -3.90              | 971.30          |
      | 126       | 07 May 2019      | 77    | 123          | 50.00                 | 50.00                 |                     | 0.876979986           | 43.85    | 3555.77 | 3.85                       | 9.52                  |                          | -3.85              | 971.30          |
      | 127       | 08 May 2019      | 76    | 124          | 50.00                 | 50.00                 |                     | 0.8760445329          | 43.80    | 3509.56 | 3.80                       | 9.52                  |                          | -3.80              | 971.30          |
      | 128       | 09 May 2019      | 75    | 125          | 50.00                 | 50.00                 |                     | 0.8751100777          | 43.76    | 3463.31 | 3.75                       | 9.52                  |                          | -3.75              | 971.30          |
      | 129       | 10 May 2019      | 74    | 126          | 50.00                 | 50.00                 |                     | 0.8741766193          | 43.71    | 3417.01 | 3.70                       | 9.52                  |                          | -3.70              | 971.30          |
      | 130       | 11 May 2019      | 73    | 127          | 50.00                 | 50.00                 |                     | 0.8732441565          | 43.66    | 3370.66 | 3.65                       | 9.52                  |                          | -3.65              | 971.30          |
      | 131       | 12 May 2019      | 72    | 128          | 50.00                 | 50.00                 |                     | 0.8723126884          | 43.62    | 3324.26 | 3.60                       | 9.52                  |                          | -3.60              | 971.30          |
      | 132       | 13 May 2019      | 71    | 129          | 50.00                 | 50.00                 |                     | 0.8713822138          | 43.57    | 3277.81 | 3.55                       | 9.52                  |                          | -3.55              | 971.30          |
      | 133       | 14 May 2019      | 70    | 130          | 50.00                 | 50.00                 |                     | 0.8704527318          | 43.52    | 3231.31 | 3.50                       | 9.52                  |                          | -3.50              | 971.30          |
      | 134       | 15 May 2019      | 69    | 131          | 50.00                 | 50.00                 |                     | 0.8695242412          | 43.48    | 3184.76 | 3.45                       | 9.52                  |                          | -3.45              | 971.30          |
      | 135       | 16 May 2019      | 68    | 132          | 50.00                 | 50.00                 |                     | 0.868596741           | 43.43    | 3138.16 | 3.40                       | 9.52                  |                          | -3.40              | 971.30          |
      | 136       | 17 May 2019      | 67    | 133          | 50.00                 | 50.00                 |                     | 0.8676702302          | 43.38    | 3091.51 | 3.35                       | 9.52                  |                          | -3.35              | 971.30          |
      | 137       | 18 May 2019      | 66    | 134          | 50.00                 | 50.00                 |                     | 0.8667447076          | 43.34    | 3044.81 | 3.30                       | 9.52                  |                          | -3.30              | 971.30          |
      | 138       | 19 May 2019      | 65    | 135          | 50.00                 | 50.00                 |                     | 0.8658201723          | 43.29    | 2998.06 | 3.25                       | 9.52                  |                          | -3.25              | 971.30          |
      | 139       | 20 May 2019      | 64    | 136          | 50.00                 | 50.00                 |                     | 0.8648966231          | 43.24    | 2951.26 | 3.20                       | 9.52                  |                          | -3.20              | 971.30          |
      | 140       | 21 May 2019      | 63    | 137          | 50.00                 | 50.00                 |                     | 0.8639740591          | 43.20    | 2904.42 | 3.15                       | 9.52                  |                          | -3.15              | 971.30          |
      | 141       | 22 May 2019      | 62    | 138          | 50.00                 | 50.00                 |                     | 0.8630524792          | 43.15    | 2857.52 | 3.10                       | 9.52                  |                          | -3.10              | 971.30          |
      | 142       | 23 May 2019      | 61    | 139          | 50.00                 | 50.00                 |                     | 0.8621318823          | 43.11    | 2810.57 | 3.05                       | 9.52                  |                          | -3.05              | 971.30          |
      | 143       | 24 May 2019      | 60    | 140          | 50.00                 | 50.00                 |                     | 0.8612122673          | 43.06    | 2763.57 | 3.00                       | 9.52                  |                          | -3.00              | 971.30          |
      | 144       | 25 May 2019      | 59    | 141          | 50.00                 | 50.00                 |                     | 0.8602936333          | 43.01    | 2716.52 | 2.95                       | 9.52                  |                          | -2.95              | 971.30          |
      | 145       | 26 May 2019      | 58    | 142          | 50.00                 | 50.00                 |                     | 0.8593759792          | 42.97    | 2669.42 | 2.90                       | 9.52                  |                          | -2.90              | 971.30          |
      | 146       | 27 May 2019      | 57    | 143          | 50.00                 | 50.00                 |                     | 0.8584593039          | 42.92    | 2622.27 | 2.85                       | 9.52                  |                          | -2.85              | 971.30          |
      | 147       | 28 May 2019      | 56    | 144          | 50.00                 | 50.00                 |                     | 0.8575436064          | 42.88    | 2575.07 | 2.80                       | 9.52                  |                          | -2.80              | 971.30          |
      | 148       | 29 May 2019      | 55    | 145          | 50.00                 | 50.00                 |                     | 0.8566288857          | 42.83    | 2527.82 | 2.75                       | 9.52                  |                          | -2.75              | 971.30          |
      | 149       | 30 May 2019      | 54    | 146          | 50.00                 | 50.00                 |                     | 0.8557151407          | 42.79    | 2480.52 | 2.70                       | 9.52                  |                          | -2.70              | 971.30          |
      | 150       | 31 May 2019      | 53    | 147          | 50.00                 | 50.00                 |                     | 0.8548023703          | 42.74    | 2433.17 | 2.65                       | 9.52                  |                          | -2.65              | 971.30          |
      | 151       | 01 June 2019     | 52    | 148          | 50.00                 | 50.00                 |                     | 0.8538905736          | 42.69    | 2385.77 | 2.60                       | 9.52                  |                          | -2.60              | 971.30          |
      | 152       | 02 June 2019     | 51    | 149          | 50.00                 | 50.00                 |                     | 0.8529797495          | 42.65    | 2338.31 | 2.55                       | 9.52                  |                          | -2.55              | 971.30          |
      | 153       | 03 June 2019     | 50    | 150          | 50.00                 | 50.00                 |                     | 0.8520698969          | 42.60    | 2290.81 | 2.50                       | 9.52                  |                          | -2.50              | 971.30          |
      | 154       | 04 June 2019     | 49    | 151          | 50.00                 | 50.00                 |                     | 0.8511610148          | 42.56    | 2243.26 | 2.45                       | 9.52                  |                          | -2.45              | 971.30          |
      | 155       | 05 June 2019     | 48    | 152          | 50.00                 | 50.00                 |                     | 0.8502531022          | 42.51    | 2195.65 | 2.40                       | 9.52                  |                          | -2.40              | 971.30          |
      | 156       | 06 June 2019     | 47    | 153          | 50.00                 | 50.00                 |                     | 0.8493461581          | 42.47    | 2148.00 | 2.34                       | 9.52                  |                          | -2.34              | 971.30          |
      | 157       | 07 June 2019     | 46    | 154          | 50.00                 | 50.00                 |                     | 0.8484401814          | 42.42    | 2100.29 | 2.29                       | 9.52                  |                          | -2.29              | 971.30          |
      | 158       | 08 June 2019     | 45    | 155          | 50.00                 | 50.00                 |                     | 0.8475351711          | 42.38    | 2052.53 | 2.24                       | 9.52                  |                          | -2.24              | 971.30          |
      | 159       | 09 June 2019     | 44    | 156          | 50.00                 | 50.00                 |                     | 0.8466311261          | 42.33    | 2004.73 | 2.19                       | 9.52                  |                          | -2.19              | 971.30          |
      | 160       | 10 June 2019     | 43    | 157          | 50.00                 | 50.00                 |                     | 0.8457280454          | 42.29    | 1956.87 | 2.14                       | 9.52                  |                          | -2.14              | 971.30          |
      | 161       | 11 June 2019     | 42    | 158          | 50.00                 | 50.00                 |                     | 0.844825928           | 42.24    | 1908.96 | 2.09                       | 9.52                  |                          | -2.09              | 971.30          |
      | 162       | 12 June 2019     | 41    | 159          | 50.00                 | 50.00                 |                     | 0.8439247729          | 42.20    | 1860.99 | 2.04                       | 9.52                  |                          | -2.04              | 971.30          |
      | 163       | 13 June 2019     | 40    | 160          | 50.00                 | 50.00                 |                     | 0.8430245791          | 42.15    | 1812.98 | 1.99                       | 9.52                  |                          | -1.99              | 971.30          |
      | 164       | 14 June 2019     | 39    | 161          | 50.00                 | 50.00                 |                     | 0.8421253454          | 42.11    | 1764.92 | 1.94                       | 9.52                  |                          | -1.94              | 971.30          |
      | 165       | 15 June 2019     | 38    | 162          | 50.00                 | 50.00                 |                     | 0.841227071           | 42.06    | 1716.80 | 1.88                       | 9.52                  |                          | -1.88              | 971.30          |
      | 166       | 16 June 2019     | 37    | 163          | 50.00                 | 50.00                 |                     | 0.8403297547          | 42.02    | 1668.64 | 1.83                       | 9.52                  |                          | -1.83              | 971.30          |
      | 167       | 17 June 2019     | 36    | 164          | 50.00                 | 50.00                 |                     | 0.8394333956          | 41.97    | 1620.42 | 1.78                       | 9.52                  |                          | -1.78              | 971.30          |
      | 168       | 18 June 2019     | 35    | 165          | 50.00                 | 50.00                 |                     | 0.8385379925          | 41.93    | 1572.15 | 1.73                       | 9.52                  |                          | -1.73              | 971.30          |
      | 169       | 19 June 2019     | 34    | 166          | 50.00                 | 50.00                 |                     | 0.8376435446          | 41.88    | 1523.83 | 1.68                       | 9.52                  |                          | -1.68              | 971.30          |
      | 170       | 20 June 2019     | 33    | 167          | 50.00                 | 50.00                 |                     | 0.8367500508          | 41.84    | 1475.45 | 1.63                       | 9.52                  |                          | -1.63              | 971.30          |
      | 171       | 21 June 2019     | 32    | 168          | 50.00                 | 50.00                 |                     | 0.83585751            | 41.79    | 1427.03 | 1.58                       | 9.52                  |                          | -1.58              | 971.30          |
      | 172       | 22 June 2019     | 31    | 169          | 50.00                 | 50.00                 |                     | 0.8349659213          | 41.75    | 1378.55 | 1.52                       | 9.52                  |                          | -1.52              | 971.30          |
      | 173       | 23 June 2019     | 30    | 170          | 50.00                 | 50.00                 |                     | 0.8340752837          | 41.70    | 1330.02 | 1.47                       | 9.52                  |                          | -1.47              | 971.30          |
      | 174       | 24 June 2019     | 29    | 171          | 50.00                 | 50.00                 |                     | 0.833185596           | 41.66    | 1281.45 | 1.42                       | 9.52                  |                          | -1.42              | 971.30          |
      | 175       | 25 June 2019     | 28    | 172          | 50.00                 | 50.00                 |                     | 0.8322968574          | 41.61    | 1232.81 | 1.37                       | 9.52                  |                          | -1.37              | 971.30          |
      | 176       | 26 June 2019     | 27    | 173          | 50.00                 | 50.00                 |                     | 0.8314090667          | 41.57    | 1184.13 | 1.32                       | 9.52                  |                          | -1.32              | 971.30          |
      | 177       | 27 June 2019     | 26    | 174          | 50.00                 | 50.00                 |                     | 0.8305222231          | 41.53    | 1135.39 | 1.26                       | 9.52                  |                          | -1.26              | 971.30          |
      | 178       | 28 June 2019     | 25    | 175          | 50.00                 | 50.00                 |                     | 0.8296363254          | 41.48    | 1086.61 | 1.21                       | 9.52                  |                          | -1.21              | 971.30          |
      | 179       | 29 June 2019     | 24    | 176          | 50.00                 | 50.00                 |                     | 0.8287513727          | 41.44    | 1037.77 | 1.16                       | 9.52                  |                          | -1.16              | 971.30          |
      | 180       | 30 June 2019     | 23    | 177          | 50.00                 | 50.00                 |                     | 0.8278673639          | 41.39    | 988.88  | 1.11                       | 9.52                  |                          | -1.11              | 971.30          |
      | 181       | 01 July 2019     | 22    | 178          | 50.00                 | 50.00                 |                     | 0.8269842981          | 41.35    | 939.93  | 1.06                       | 9.52                  |                          | -1.06              | 971.30          |
      | 182       | 02 July 2019     | 21    | 179          | 50.00                 | 50.00                 |                     | 0.8261021742          | 41.31    | 890.93  | 1.00                       | 9.52                  |                          | -1.00              | 971.30          |
      | 183       | 03 July 2019     | 20    | 180          | 50.00                 | 50.00                 |                     | 0.8252209913          | 41.26    | 841.89  | 0.95                       | 9.52                  |                          | -0.95              | 971.30          |
      | 184       | 04 July 2019     | 19    | 181          | 50.00                 | 50.00                 |                     | 0.8243407483          | 41.22    | 792.79  | 0.90                       | 9.52                  |                          | -0.90              | 971.30          |
      | 185       | 05 July 2019     | 18    | 182          | 50.00                 | 50.00                 |                     | 0.8234614442          | 41.17    | 743.63  | 0.85                       | 9.52                  |                          | -0.85              | 971.30          |
      | 186       | 06 July 2019     | 17    | 183          | 50.00                 | 50.00                 |                     | 0.8225830781          | 41.13    | 694.43  | 0.79                       | 9.52                  |                          | -0.79              | 971.30          |
      | 187       | 07 July 2019     | 16    | 184          | 50.00                 | 50.00                 |                     | 0.8217056489          | 41.09    | 645.17  | 0.74                       | 9.52                  |                          | -0.74              | 971.30          |
      | 188       | 08 July 2019     | 15    | 185          | 50.00                 | 50.00                 |                     | 0.8208291556          | 41.04    | 595.86  | 0.69                       | 9.52                  |                          | -0.69              | 971.30          |
      | 189       | 09 July 2019     | 14    | 186          | 50.00                 | 50.00                 |                     | 0.8199535973          | 41.00    | 546.49  | 0.64                       | 9.52                  |                          | -0.64              | 971.30          |
      | 190       | 10 July 2019     | 13    | 187          | 50.00                 | 50.00                 |                     | 0.8190789729          | 40.95    | 497.08  | 0.58                       | 9.52                  |                          | -0.58              | 971.30          |
      | 191       | 11 July 2019     | 12    | 188          | 50.00                 | 50.00                 |                     | 0.8182052815          | 40.91    | 447.61  | 0.53                       | 9.52                  |                          | -0.53              | 971.30          |
      | 192       | 12 July 2019     | 11    | 189          | 50.00                 | 50.00                 |                     | 0.8173325219          | 40.87    | 398.08  | 0.48                       | 9.52                  |                          | -0.48              | 971.30          |
      | 193       | 13 July 2019     | 10    | 190          | 50.00                 | 50.00                 |                     | 0.8164606934          | 40.82    | 348.51  | 0.43                       | 9.52                  |                          | -0.43              | 971.30          |
      | 194       | 14 July 2019     | 9     | 191          | 50.00                 | 50.00                 |                     | 0.8155897948          | 40.78    | 298.88  | 0.37                       | 9.52                  |                          | -0.37              | 971.30          |
      | 195       | 15 July 2019     | 8     | 192          | 50.00                 | 50.00                 |                     | 0.8147198252          | 40.74    | 249.20  | 0.32                       | 9.52                  |                          | -0.32              | 971.30          |
      | 196       | 16 July 2019     | 7     | 193          | 50.00                 | 50.00                 |                     | 0.8138507835          | 40.69    | 199.47  | 0.27                       | 9.52                  |                          | -0.27              | 971.30          |
      | 197       | 17 July 2019     | 6     | 194          | 50.00                 | 50.00                 |                     | 0.8129826688          | 40.65    | 149.68  | 0.21                       | 9.52                  |                          | -0.21              | 971.30          |
      | 198       | 18 July 2019     | 5     | 195          | 50.00                 | 50.00                 |                     | 0.8121154802          | 40.61    | 99.84   | 0.16                       | 9.52                  |                          | -0.16              | 971.30          |
      | 199       | 19 July 2019     | 4     | 196          | 50.00                 | 50.00                 |                     | 0.8112492165          | 40.56    | 49.95   | 0.11                       | 9.52                  |                          | -0.11              | 971.30          |
      | 200       | 20 July 2019     | 3     | 197          | 50.00                 | 50.00                 |                     | 0.8103838768          | 40.52    | 0.00    | 0.05                       | 9.52                  |                          | -0.05              | 971.30          |
