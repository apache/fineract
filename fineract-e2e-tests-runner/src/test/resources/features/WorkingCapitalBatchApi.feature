@WorkingCapitalBatchApi
Feature: Working Capital Batch API

  # ============================================
  # SECTION 1: Individual Operation Tests
  # ============================================

  @TestRailId:C83089
  Scenario: Verify Batch API - UC1: Create Working Capital Loan via Batch API
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Admin checks that all steps result 200OK
    And Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 100000.0           | 18.0              | null     |

  @TestRailId:C83090
  Scenario: Verify Batch API - UC2: Modify Working Capital Loan via Batch API by ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Batch API modifies the working capital loan principal to "8000" by loan ID
    Then Admin checks that all steps result 200OK
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 8000.0            | 100000.0           | 18.0              | null     |

  @TestRailId:C83091
  Scenario: Verify Batch API - UC3: Approve Working Capital Loan via Batch API
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Batch API approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin checks that all steps result 200OK
    And Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | approvedPrincipal | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null     |

  @TestRailId:C83092
  Scenario: Verify Batch API - UC4: Disburse Working Capital Loan via Batch API
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Batch API disburses the working capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin checks that all steps result 200OK
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |

  @TestRailId:C83093
  Scenario: Verify Batch API - UC5: Reject Working Capital Loan via Batch API
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Batch API rejects the working capital loan on "01 January 2026"
    Then Admin checks that all steps result 200OK
    And Working capital loan rejection was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Rejected | 9000.0            | 100000.0           | 18.0              | null     |

  @TestRailId:C83094
  Scenario: Verify Batch API - UC6: Withdraw/Delete Working Capital Loan via Batch API by ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 100000.0           | 18.0              | null     |
    When Batch API deletes the working capital loan by loan ID
    Then Admin checks that all steps result 200OK
    And Working capital loan no longer exists

  @TestRailId:C83095
  Scenario: Verify Batch API - UC7: Add Discount Fee via Batch API
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    When Batch API adds discount fee with "12" amount referencing the disbursement external-id in relatedResourceId on the working capital loan
    Then Verify that WCL step 1 throws an error with error code 400 and message "relatedResourceId.not.a.number"
    When Batch API adds discount fee with "12" amount on the working capital loan
    Then Admin checks that all steps result 200OK
    When Batch API adds discount fee adjustment with "5" amount referencing the discount fee external-id in relatedResourceId on the working capital loan
    Then Verify that WCL step 1 throws an error with error code 400 and message "relatedResourceId.not.a.number"
    When Batch API adds discount fee adjustment with "5" amount referencing the discount fee external-id in relatedExternalResourceId on the working capital loan
    Then Admin checks that all steps result 200OK
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee Adjustment | 5.0               | 5.0              | 0.0               | 0.0                   | false    |

  @TestRailId:C102522
  Scenario: Verify Batch API - discount fee on a charged-off Working Capital loan is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin charges off the Working Capital loan on "01 January 2026"
    When Batch API adds discount fee with "12" amount on the working capital loan
    Then Verify that WCL step 1 throws an error with error code 403 and message "error.msg.wc.loan.is.charged.off"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Charge-off   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    And Working capital loan account has the correct data:
      | discount | chargedOff |
      | null     | true       |

  @TestRailId:C83096
  Scenario: Verify Batch API - UC8: Fetch Working Capital Loan Details via Batch API by ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Batch API fetches working capital loan details by loan ID
    Then Admin checks that all steps result 200OK
    And Batch API response contains working capital loan with the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 100000.0           | 18.0              | null     |

  @TestRailId:C83097
  Scenario: Verify Batch API - UC9: Fetch Working Capital Loan Transactions via Batch API
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Batch API fetches working capital loan disbursement transaction by loan ID
    Then Admin checks that all steps result 200OK
    And Batch API response contains working capital transaction with the correct data:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |

  # ============================================
  # SECTION 2: Combined Workflow Tests
  # ============================================

  @TestRailId:C83098
  Scenario: Verify Batch API - UC10: Full Working Capital Loan lifecycle via Batch API in single call
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API call with working capital steps: "createWCLoan, approveWCLoan, disburseWCLoan" runs with enclosingTransaction: "true" and loan data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Admin checks that all steps result 200OK
    And Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |

  @TestRailId:C83099
  Scenario: Verify Batch API - UC11: Create, Approve, Disburse and Add Discount Fee in single Batch API call
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API call with working capital steps: "createWCLoan, approveWCLoan, disburseWCLoan, addDiscountFee" runs with enclosingTransaction: "true" and loan data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Admin checks that all steps result 200OK
    And Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C83100
  Scenario: Verify Batch API - UC12: Working Capital Batch API enclosing transaction FALSE with chained dependencies
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API call with working capital steps: "createWCLoan, approveWCLoan, disburseWCLoan" runs with enclosingTransaction: "false" and loan data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Verify that WCL step 1 results 200
    And Verify that WCL step 2 results 200
    And Verify that WCL step 3 results 200

  @TestRailId:C83101
  Scenario: Verify Batch API - UC13: Working Capital Batch API enclosing transaction FALSE with chained dependencies and failed last step
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API call with working capital steps: "createWCLoan, approveWCLoan, disburseWCLoan" runs with enclosingTransaction: "false", with failed disburse step and loan data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Verify that WCL step 1 results 200
    And Verify that WCL step 2 results 200
    And Verify that WCL step 3 throws an error with error code 400

  @TestRailId:C83102
  Scenario: Verify Batch API - UC14: Working Capital Batch API with failed step in enclosing transaction TRUE
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API call with working capital steps: "createWCLoan, approveWCLoan, disburseWCLoan" runs with enclosingTransaction: "true", with failed disburse step and loan data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Verify that WCL step 3 throws an error with error code 400
    And Nr. 1 Working capital loan creation was rolled back

  # ============================================
  # SECTION 3: External ID Tests
  # ============================================

  @TestRailId:C83103
  Scenario: Verify Batch API - UC15: Working Capital Loan lifecycle via Batch API by External ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 100000.0           | 18.0              | null     |
    When Batch API approves the working capital loan by external ID on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | approvedPrincipal | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null     |
    And Batch API disburses the working capital loan by external ID on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
  When Batch API fetches working capital loan details by external ID
    Then Admin checks that all steps result 200OK
    And Batch API response contains working capital loan with the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Batch API fetches working capital loan disbursement transaction by external ID
    Then Admin checks that all steps result 200OK
    And Batch API response contains working capital transaction with the correct data:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C83104
  Scenario: Verify Batch API - UC16: Modify Working Capital Loan via Batch API by External ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Batch API creates a working capital loan with external ID and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Batch API modifies the working capital loan principal to "8000" by external ID
    Then Admin checks that all steps result 200OK
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 8000.0            | 100000.0           | 18.0              | null     |

  @TestRailId:C83105
  Scenario: Verify Batch API - UC17: Working Capital Batch API with external IDs
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    When Batch API call with working capital steps by external IDs: "createWCLoan, approveWCLoan, disburseWCLoan, getWCLoanDetails" runs with enclosingTransaction: "true" and loan data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Admin checks that all steps result 200OK
    And Batch API response contains working capital loan details with the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C110973
  Scenario: Verify working capital loan delinquency range schedule via batch with payment full expectedAmount repaid after disbursement day - UC1
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
    Then Working Capital loan delinquency range schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
#   --- Full expectedAmount paid ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital loan delinquency range schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-04 | 2026-01-06 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "04 January 2026"

  @TestRailId:C110974
  Scenario: Verify working capital loan delinquency range schedule via batch with multiple ranges with discount - UC2
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
    Then Working Capital loan delinquency range schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    When Admin sets the business date to "19 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-03 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 16             |
      | 2            | 2026-01-04 | 2026-01-06 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 13             |
      | 3            | 2026-01-07 | 2026-01-09 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 10             |
      | 4            | 2026-01-10 | 2026-01-12 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 7              |
      | 5            | 2026-01-13 | 2026-01-15 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 4              |
      | 6            | 2026-01-16 | 2026-01-18 | 300.0          | 0.0        | 300.0             | false                 | 300.0            | 1              |
      | 7            | 2026-01-19 | 2026-01-21 | 300.0          | 0.0        | 300.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "19 January 2026"

  @TestRailId:C110975
  Scenario: Verify working capital loan delinquency range schedule via batch api with delinquency pause in the middle of second period - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful on "01 January 2026" with "9000" EUR transaction amount
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "15 February 2026" and endDate "25 February 2026"
    Then Working Capital loan delinquency action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-02-15 | 2026-02-25 |
    Then Working Capital loan delinquency range schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                  | 270.0             | 16           |
      | 2            | 2026-01-31 | 2026-03-12 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "15 February 2026"

  @TestRailId:C110976
  Scenario: Verify working capital loan breach schedule via batch api with first day after 1st period - 2nd period generated, 1st evaluated - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 March 2026"

  @TestRailId:C110977
  Scenario: Verify working capital loan breach schedule via batch api with breach pause - resume shortens the pause so the period breaches earlier - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 6               | DAYS                | PERCENTAGE                  | 1.23         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-06 | 6            | 110.70           | 110.70            | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "01 January 2026" and endDate "06 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
    Then Working Capital loan breach schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 12           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "03 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-01 | 2026-01-06 |
      | RESUME | 2026-01-03 |            |
    Then Working Capital loan breach schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-01-10 | 2026-01-15 | 6            | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C110978
  Scenario: Verify breach breach schedule via batch api with breach pause with start date type inside the pre-disbursement breach window - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | breachStartType |
      | 3               | DAYS                | FLAT                        | 100          | 0               | LOAN_CREATION   |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 10 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "10 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Admin successfully disburse the Working Capital loan on "10 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-03 | 3            | 100              | 100               | null       | true   |
      | 2            | 2026-01-04 | 2026-01-06 | 3            | 100              | 100               | null       | true   |
      | 3            | 2026-01-07 | 2026-01-09 | 3            | 100              | 100               | null       | true   |
      | 4            | 2026-01-10 | 2026-01-12 | 3            | 100              | 100               | null       | null   |
    And Admin initiate a Working Capital loan breach pause with startDate "05 January 2026" and endDate "07 January 2026"
    Then Working Capital loan breach action has the following data:
      | action | startDate  | endDate    |
      | PAUSE  | 2026-01-05 | 2026-01-07 |
    Then Working Capital loan breach schedule has the following data via batch api:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-03 | 3            | 100              | 100               | null       | true   |
      | 2            | 2026-01-04 | 2026-01-09 | 6            | 100              | 100               | null       | true   |
      | 3            | 2026-01-10 | 2026-01-12 | 3            | 100              | 100               | null       | null   |
      | 4            | 2026-01-13 | 2026-01-15 | 3            | 100              | 100               | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

