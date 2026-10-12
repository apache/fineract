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
    When Batch API adds discount fee with "12" amount on the working capital loan
    Then Admin checks that all steps result 200OK
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 12.0              | 12.0             | 0.0               | 0.0                   | false    |

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
