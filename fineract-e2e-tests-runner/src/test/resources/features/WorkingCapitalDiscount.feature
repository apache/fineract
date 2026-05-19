@WorkingCapitalDiscountFeature
Feature: Working Capital Discount

  @TestRailId:C72393
  Scenario: Discount on WCL account added after disbursement as on the same as disburse date processed successfully -  UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
  # --- add discount after disbursement on the same disbursement date --- #
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 112.0     | 100.0             | 100.0        | 1.0               | null             | null             | 12.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 12.0              | 12.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C72394
  Scenario: Discount update on WCL account on diff from disbursement date outcomes with an error - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- add discount after disbursement on diff from same disbursement date should outcome with an error --- #
    When Admin sets the business date to "08 January 2026"
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to date diff from disbursement date
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C72395
  Scenario: Discount update on WCL account on the same as disburse date failed as already added discount before while create loan - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 15       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 15.0             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "18" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 15.0             | 14.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "15" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0        | 1.0               | 15.0             | 14.0             | 13.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to already added discount before disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C72396
  Scenario: Discount update on WCL account on the same as disburse date failed as already added discount before while modify loan - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 15.0     |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 15.0             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "18" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 15.0             | 14.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "15" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0        | 1.0               | 15.0             | 14.0             | 13.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to already added discount before disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C72397
  Scenario: Discount update on WCL account on the same as disburse date failed as already added discount before while approve loan - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 14.0             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "14" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 114.0     | 100.0             | 100.0        | 1.0               | null             | 14.0             | 14.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 14.0              | 14.0             | 0.0               | 0.0                   | false    |
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to already added discount before disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 14.0              | 14.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C72490
  Scenario: Discount update od WCL loan account on the same as disburse date failed as already added discount before while disburse loan - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0        | 1.0               | null             | null             | 13.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to already added discount before disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C74491
  Scenario: Discount is forbidden to be added after disbursement as discount is set on loan product level with WCLP overrides disallowed - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "20" discount amount due to override disallowed by product
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "15" discount amount due to override disallowed by product
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Add Discount fee with "20" amount on Working Capital loan account failed due to override disallowed by product
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C74492
  Scenario: Discount is forbidden to be added after disbursement as NO discount is set on loan product level with WCLP overrides disallowed - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "20" discount amount due to override disallowed by product
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "20" discount amount due to override disallowed by product
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Add Discount fee with "20" amount on Working Capital loan account failed due to override disallowed by product
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C74493
  Scenario: Discount is disallowed to be added after disbursement as discount is set and inherited from WC loan product level with WCLP overrides allowed - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "50" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 150.0     | 100.0             | 100.0        | 1.0               | null             | null             | 50.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100               | 100              | 0                 | 0                     | false    |
      | 01 January 2026 | Discount Fee | 50                | 50               | 0                 | 0                     | false    |
 # --- add discount after disbursement is forbidden as discount is already added --- #
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to already added discount before disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 50                | 50               | 0                 | 0                     | false    |

  @TestRailId:C72494
  Scenario: Discount is forbidden to be added after Modify with modified discount amount allowed as discount is set on loan product level with WCLP overrides allowed - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 55.0     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 55.0             | null             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "60" exceeded product discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "50" discount amount  
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 150.0     | 100.0             | 100.0        | 1.0               | 55.0             | null             | 50.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 50.0              | 50.0             | 0                 | 0                     | false    |
# --- add discount after disbursement is forbidden as discount is already added and updated on disbursement --- #
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to already added discount before disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 50.0              | 50.0             | 0                 | 0                     | false    |

  @TestRailId:C78836
  Scenario: Discount is forbidden to be added after Approve with modified discount amount allowed as discount is set on loan product level with WCLP overrides allowed - UC11
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded product discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "40" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 40.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "50" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "40" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 140.0     | 100.0             | 100.0        | 1.0               | null             | 40.0             | 40.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 40.0              | 40.0             | 0                 | 0                     | false    |
  # --- add discount after disbursement is forbidden as discount is already added --- #
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to already added discount before disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 40.0              | 40.0             | 0                 | 0                     | false    |

  @TestRailId:C78837
  Scenario: Discount is forbidden to be added after Disburse with modified discount amount allowed as discount is set on loan product level with WCLP overrides allowed - UC12
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded product discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "60" exceeded product discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "45" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 145.0     | 100.0             | 100.0        | 1.0               | null             | null             | 45.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 45.0              | 45.0             | 0                 | 0                     | false    |
# --- add discount after disbursement is forbidden as discount is already added and updated on disbursement --- #
    Then Add Discount fee with "10" amount on Working Capital loan account failed due to already added discount before disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 45.0              | 45.0             | 0                 | 0                     | false    |

  @TestRailId:C78838
  Scenario: Discount is allowed to be added after disbursement as overridden with NO discount value from defined on WCLP on loan Create with WCLP overrides allowed - UC13
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0              | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0              | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | 0.0              | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100               | 100              | 0                 | 0                     | false    |
 # --- add discount after disbursement is allowed as NO discount is added yet --- #
    Then Admin adds Discount fee with "10" amount on Working Capital loan account for last disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 10.0              | 10.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C78839
  Scenario: Working Capital Loan Transaction - Discount added after disbursement on disbursement undo - UC14
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0              | null             | null     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0              | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP          | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | 0.0              | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    Then Admin adds Discount fee with "10" amount on Working Capital loan account for last disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 10.0              | 10.0             | 0.0               | 0.0                   | false    |
# --- undo working capital disbursal --- #
    And Admin successfully undo Working Capital disbursal by externalId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0              | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
      | 01 January 2026 | Discount Fee | 10.0              | 10.0             | 0.0               | 0.0                   | true     |

  @TestRailId:C78840
  Scenario: Working Capital Loan Transaction - Discount added on disbursement on disbursement undo - UC15
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 22.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 22.0             | null             | null     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 22.0             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "11" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP          | 2026-01-01      | 2026-01-01               | Active | 111.0     | 100.0             | 100.0        | 1.0               | 22.0             | null             | 11.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 11.0              | 11.0             | 0.0               | 0.0                   | false    |
# --- undo working capital disbursal --- #
    And Admin successfully undo Working Capital disbursal by externalId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 22.0             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
      | 01 January 2026 | Discount Fee | 11.0              | 11.0             | 0.0               | 0.0                   | true     |

  @TestRailId:C78854
  Scenario: Discount on Working Capital Loan account added while create WCP, updated while approve loan and check after updo approval - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 18       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 18.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | 17.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 18.0             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "19" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | null             | null     |

  @TestRailId:C78855
  Scenario: Discount on Working Capital Loan account added while create WCP, updated while approve/disburse loan and check after undo approval/disbursal - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 18       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 18.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | 17.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "19" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "16" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 116.0     | 100.0             | 100.0        | 1.0               | 18.0             | 17.0             | 16.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | 17.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 18.0             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "19" exceeded discount amount

  @TestRailId:C78856
  Scenario: Discount on Working Capital Loan account added while create WCP, updated while modify/approve loan and check after updo approval - UC1.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 18       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 18.0             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 19.0     |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 19.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 19.0             | 17.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 19.0             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "20" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 19.0             | null             | null     |

  @TestRailId:C78857
  Scenario: Discount on Working Capital Loan account added while modify WCP, updated while approve/disburse loan and check after undo approval/disbursal - UC2.2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 18.0     |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 18.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | 17.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "19" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "16" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 116.0     | 100.0             | 100.0        | 1.0               | 18.0             | 17.0             | 16.0     |
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | 17.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 18.0             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "19" exceeded discount amount

  @TestRailId:C78858
  Scenario: Discount on Working Capital Loan account added while approve WCP and check after updo approval - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "15" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 15.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "19" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 19.0             | null     |

  @TestRailId:C78859
  Scenario: Discount on Working Capital Loan account added while approve, updated on disbursal and check after updo disbursal/approval with additional update while approve and check after undo disbursal - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 17.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "19" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "16" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active   | 116.0     | 100.0             | 100.0        | 1.0               | null             | 17.0             | 16.0     |
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 17.0             | null     |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "19" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 19.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "20" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "19" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 119.0     | 100.0             | 100.0        | 1.0               | null             | 19.0             | 19.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 19.0             | null     |

  @TestRailId:C78860
  Scenario: Discount on Working Capital Loan account added while approve, updated on disbursal and check after updo disbursal/approval with additional update while modify and check after undo disbursal - UC4.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 17.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "19" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "16" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active   | 116.0     | 100.0             | 100.0        | 1.0               | null             | 17.0             | 16.0     |
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 17.0             | null     |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 20.0     |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 20.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "20" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 20.0             | 20.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "21" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "18" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 118.0     | 100.0             | 100.0        | 1.0               | 20.0             | 20.0             | 18.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 20.0             | 20.0             | null     |

  @TestRailId:C78861
  Scenario: Discount on Working Capital Loan account added while update discount and check after undo disbursal/approval - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active   | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "12" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 112.0     | 100.0             | 100.0        | 1.0               | null             | null             | 12.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |

  @TestRailId:C78862
  Scenario: Discount on Working Capital Loan account added while create loan, updated on disbursal and check after undo disbursal with additional disburse - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 18       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 18.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "18" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | 18.0             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active   | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | 18.0             | null     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 18.0             | 18.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "19" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "17" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 117.0     | 100.0             | 100.0        | 1.0               | 18.0             | 18.0             | 17.0     |

  @TestRailId:C78863
  Scenario: Discount on Working Capital Loan account with NO discount is set on loan product level with WCLP overrides disallowed after undo approval/disbursal - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "20" discount amount due to override disallowed by product
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "20" discount amount due to override disallowed by product
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |

  @TestRailId:C78864
  Scenario: Discount on Working Capital Loan account added after disbursement with discount set on loan product level and WCLP overrides allowed after undo approval/disbursal - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- add discount with exceed discount amount is forbidden as loan discount is null but It exceeds product discount value --- #
    Then Update discount with "60" amount on Working Capital loan account failed due to exceed discount amount
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "12" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 112.0     | 100.0             | 100.0        | 1.0               | null             | null             | 12.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |

  @TestRailId:C78865
  Scenario: Discount on Working Capital Loan account added on create loan account with modified discount amount while discount set on loan product level and WCLP overrides allowed after undo disbursal/approve - UC9.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 48.0     |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 48.0             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "40" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 48.0             | 40.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "50" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "40" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 140.0     | 100.0             | 100.0        | 1.0               | 48.0             | 40.0             | 40.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Add discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 48.0             | 40.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 48.0             | null             | null     |
# --- modify working capital loan account --- #
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 49.0     |
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 49.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 49.0             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 49.0             | null             | null     |

  @TestRailId:C78866
  Scenario: Discount on Working Capital Loan account added on modify loan account with modified discount amount with discount set on loan product level and WCLP overrides allowed after undo disbursal/approve - UC9.2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 30.0     |
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 30.0             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "20" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 30.0             | 20.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "50" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "20" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 120.0     | 100.0             | 100.0        | 1.0               | 30.0             | 20.0             | 20.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Add discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 30.0             | 20.0             | null     |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 30.0             | null             | null     |

  @TestRailId:C78867
  Scenario: Discount on Working Capital Loan account added on Approve with modified discount amount with discount set on loan product level and WCLP overrides allowed after undo disbursal/approve - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded product discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "40" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 40.0             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "50" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "40" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 140.0     | 100.0             | 100.0        | 1.0               | null             | 40.0             | 40.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Add discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 40.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |

  @TestRailId:C78868
  Scenario: Discount on Working Capital Loan account added on Disburse with modified discount amount while discount is set on loan product level with WCLP overrides allowed after undo disbursal/approval - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded product discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "60" exceeded product discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "45" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 145.0     | 100.0             | 100.0        | 1.0               | null             | null             | 45.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Add discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |

  @TestRailId:C78869
  Scenario: Discount on Working Capital Loan account added after disbursement while discount is set on loan product level with WCLP overrides disallowed after undo disbursal/approval - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Update discount with "20" amount on Working Capital loan account failed due to override disallowed by product
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |

  @TestRailId:C74518
  Scenario: Verify that undo disbursal of WCL account set discount to null - UC5.2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null             | null             | null     |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 14.0             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0        | 1.0               | null             | 14.0             | 13.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    Then Working Capital loan status will be "APPROVED"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null             | 14.0             | null     |

  Scenario: Discount fee adjustment is processed successfully - UC16
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    Then Admin adds Discount fee adjustment with "5" amount on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 107.0     | 100.0             | 100.0        | 1.0               | 7.0      |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee Adjustment | 5.0               | 5.0              | 0.0               | 0.0                   | false    |

  Scenario: Discount fee adjustment fails when amount exceeds discount fee remainder - UC17
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    Then Add Discount fee adjustment with "13" amount on Working Capital loan account failed due to exceeding discount amount

  Scenario: Discount fee adjustment fails when transaction date is before discount fee date - UC18
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    Then Add Discount fee adjustment with "2" amount and transaction date "31 December 2025" on Working Capital loan account failed due to transaction date before discount fee date

  Scenario: Discount fee adjustment fails when transaction date is before business date - UC19
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "20 January 2026"
    Then Add Discount fee adjustment with "2" amount and transaction date "15 January 2026" on Working Capital loan account failed due to backdated transaction date

  Scenario: Verify amortization schedule after discount fee adjustment - EIR discount adjustment S1
    When Admin sets the business date to "01 January 2019"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2019 | 01 January 2019          | 9000            | 100000       | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2019" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2019"
    And Admin successfully disburse the Working Capital loan on "01 January 2019" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital loan amortization schedule has 201 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2019  | -9000.00              |                     | 1                     | -9000.00 | 9000.00 |                            |                          |                    | 1000.00         |
      | 1         | 02 January 2019  | 50.00                 |                     | 0.9989333245          | 49.95    | 8959.61 | 9.61                       |                          |                    | 1000.00         |
      | 2         | 03 January 2019  | 50.00                 |                     | 0.9978677868          | 49.89    | 8919.18 | 9.57                       |                          |                    | 1000.00         |
      | 3         | 04 January 2019  | 50.00                 |                     | 0.9968033857          | 49.84    | 8878.70 | 9.52                       |                          |                    | 1000.00         |
      | 4         | 05 January 2019  | 50.00                 |                     | 0.99574012            | 49.79    | 8838.18 | 9.48                       |                          |                    | 1000.00         |
      | 5         | 06 January 2019  | 50.00                 |                     | 0.9946779885          | 49.73    | 8797.62 | 9.44                       |                          |                    | 1000.00         |
      | 6         | 07 January 2019  | 50.00                 |                     | 0.9936169898          | 49.68    | 8757.01 | 9.39                       |                          |                    | 1000.00         |
      | 7         | 08 January 2019  | 50.00                 |                     | 0.992557123           | 49.63    | 8716.36 | 9.35                       |                          |                    | 1000.00         |
      | 8         | 09 January 2019  | 50.00                 |                     | 0.9914983866          | 49.57    | 8675.67 | 9.31                       |                          |                    | 1000.00         |
      | 9         | 10 January 2019  | 50.00                 |                     | 0.9904407796          | 49.52    | 8634.94 | 9.26                       |                          |                    | 1000.00         |
      | 10        | 11 January 2019  | 50.00                 |                     | 0.9893843007          | 49.47    | 8594.16 | 9.22                       |                          |                    | 1000.00         |
      | 11        | 12 January 2019  | 50.00                 |                     | 0.9883289487          | 49.42    | 8553.33 | 9.18                       |                          |                    | 1000.00         |
      | 12        | 13 January 2019  | 50.00                 |                     | 0.9872747225          | 49.36    | 8512.47 | 9.13                       |                          |                    | 1000.00         |
      | 13        | 14 January 2019  | 50.00                 |                     | 0.9862216208          | 49.31    | 8471.56 | 9.09                       |                          |                    | 1000.00         |
      | 14        | 15 January 2019  | 50.00                 |                     | 0.9851696423          | 49.26    | 8430.60 | 9.05                       |                          |                    | 1000.00         |
      | 15        | 16 January 2019  | 50.00                 |                     | 0.984118786           | 49.21    | 8389.61 | 9.00                       |                          |                    | 1000.00         |
      | 16        | 17 January 2019  | 50.00                 |                     | 0.9830690507          | 49.15    | 8348.56 | 8.96                       |                          |                    | 1000.00         |
      | 17        | 18 January 2019  | 50.00                 |                     | 0.982020435           | 49.10    | 8307.48 | 8.91                       |                          |                    | 1000.00         |
      | 18        | 19 January 2019  | 50.00                 |                     | 0.9809729379          | 49.05    | 8266.35 | 8.87                       |                          |                    | 1000.00         |
      | 19        | 20 January 2019  | 50.00                 |                     | 0.9799265581          | 49.00    | 8225.18 | 8.83                       |                          |                    | 1000.00         |
      | 20        | 21 January 2019  | 50.00                 |                     | 0.9788812945          | 48.94    | 8183.96 | 8.78                       |                          |                    | 1000.00         |
      | 21        | 22 January 2019  | 50.00                 |                     | 0.9778371458          | 48.89    | 8142.70 | 8.74                       |                          |                    | 1000.00         |
      | 22        | 23 January 2019  | 50.00                 |                     | 0.9767941109          | 48.84    | 8101.39 | 8.69                       |                          |                    | 1000.00         |
      | 23        | 24 January 2019  | 50.00                 |                     | 0.9757521886          | 48.79    | 8060.04 | 8.65                       |                          |                    | 1000.00         |
      | 24        | 25 January 2019  | 50.00                 |                     | 0.9747113777          | 48.74    | 8018.65 | 8.61                       |                          |                    | 1000.00         |
      | 25        | 26 January 2019  | 50.00                 |                     | 0.9736716769498249835 | 48.68    | 7977.21 | 8.56                       |                          |                    | 1000.00         |
      | 26        | 27 January 2019  | 50.00                 |                     | 0.9726330853          | 48.63    | 7935.73 | 8.52                       |                          |                    | 1000.00         |
      | 27        | 28 January 2019  | 50.00                 |                     | 0.9715956014          | 48.58    | 7894.21 | 8.47                       |                          |                    | 1000.00         |
      | 28        | 29 January 2019  | 50.00                 |                     | 0.9705592242          | 48.53    | 7852.63 | 8.43                       |                          |                    | 1000.00         |
      | 29        | 30 January 2019  | 50.00                 |                     | 0.9695239525          | 48.48    | 7811.02 | 8.39                       |                          |                    | 1000.00         |
      | 30        | 31 January 2019  | 50.00                 |                     | 0.968489785           | 48.42    | 7769.36 | 8.34                       |                          |                    | 1000.00         |
      | 31        | 01 February 2019 | 50.00                 |                     | 0.9674567207          | 48.37    | 7727.66 | 8.30                       |                          |                    | 1000.00         |
      | 32        | 02 February 2019 | 50.00                 |                     | 0.9664247584          | 48.32    | 7685.91 | 8.25                       |                          |                    | 1000.00         |
      | 33        | 03 February 2019 | 50.00                 |                     | 0.9653938968          | 48.27    | 7644.12 | 8.21                       |                          |                    | 1000.00         |
      | 34        | 04 February 2019 | 50.00                 |                     | 0.9643641348          | 48.22    | 7602.28 | 8.16                       |                          |                    | 1000.00         |
      | 35        | 05 February 2019 | 50.00                 |                     | 0.9633354712          | 48.17    | 7560.40 | 8.12                       |                          |                    | 1000.00         |
      | 36        | 06 February 2019 | 50.00                 |                     | 0.9623079049          | 48.12    | 7518.47 | 8.07                       |                          |                    | 1000.00         |
      | 37        | 07 February 2019 | 50.00                 |                     | 0.9612814347          | 48.06    | 7476.50 | 8.03                       |                          |                    | 1000.00         |
      | 38        | 08 February 2019 | 50.00                 |                     | 0.9602560593          | 48.01    | 7434.48 | 7.98                       |                          |                    | 1000.00         |
      | 39        | 09 February 2019 | 50.00                 |                     | 0.9592317777          | 47.96    | 7392.42 | 7.94                       |                          |                    | 1000.00         |
      | 40        | 10 February 2019 | 50.00                 |                     | 0.9582085887          | 47.91    | 7350.31 | 7.89                       |                          |                    | 1000.00         |
      | 41        | 11 February 2019 | 50.00                 |                     | 0.9571864911          | 47.86    | 7308.16 | 7.85                       |                          |                    | 1000.00         |
      | 42        | 12 February 2019 | 50.00                 |                     | 0.9561654838          | 47.81    | 7265.97 | 7.80                       |                          |                    | 1000.00         |
      | 43        | 13 February 2019 | 50.00                 |                     | 0.9551455655          | 47.76    | 7223.72 | 7.76                       |                          |                    | 1000.00         |
      | 44        | 14 February 2019 | 50.00                 |                     | 0.9541267351          | 47.71    | 7181.44 | 7.71                       |                          |                    | 1000.00         |
      | 45        | 15 February 2019 | 50.00                 |                     | 0.9531089916          | 47.66    | 7139.11 | 7.67                       |                          |                    | 1000.00         |
      | 46        | 16 February 2019 | 50.00                 |                     | 0.9520923336          | 47.60    | 7096.73 | 7.62                       |                          |                    | 1000.00         |
      | 47        | 17 February 2019 | 50.00                 |                     | 0.95107676            | 47.55    | 7054.31 | 7.58                       |                          |                    | 1000.00         |
      | 48        | 18 February 2019 | 50.00                 |                     | 0.9500622698          | 47.50    | 7011.84 | 7.53                       |                          |                    | 1000.00         |
      | 49        | 19 February 2019 | 50.00                 |                     | 0.9490488616          | 47.45    | 6969.33 | 7.49                       |                          |                    | 1000.00         |
      | 50        | 20 February 2019 | 50.00                 |                     | 0.9480365345          | 47.40    | 6926.77 | 7.44                       |                          |                    | 1000.00         |
      | 51        | 21 February 2019 | 50.00                 |                     | 0.9470252872          | 47.35    | 6884.17 | 7.40                       |                          |                    | 1000.00         |
      | 52        | 22 February 2019 | 50.00                 |                     | 0.9460151185          | 47.30    | 6841.52 | 7.35                       |                          |                    | 1000.00         |
      | 53        | 23 February 2019 | 50.00                 |                     | 0.9450060274          | 47.25    | 6798.82 | 7.31                       |                          |                    | 1000.00         |
      | 54        | 24 February 2019 | 50.00                 |                     | 0.9439980126          | 47.20    | 6756.08 | 7.26                       |                          |                    | 1000.00         |
      | 55        | 25 February 2019 | 50.00                 |                     | 0.9429910731          | 47.15    | 6713.30 | 7.21                       |                          |                    | 1000.00         |
      | 56        | 26 February 2019 | 50.00                 |                     | 0.9419852077          | 47.10    | 6670.47 | 7.17                       |                          |                    | 1000.00         |
      | 57        | 27 February 2019 | 50.00                 |                     | 0.9409804151          | 47.05    | 6627.59 | 7.12                       |                          |                    | 1000.00         |
      | 58        | 28 February 2019 | 50.00                 |                     | 0.9399766944          | 47.00    | 6584.67 | 7.08                       |                          |                    | 1000.00         |
      | 59        | 01 March 2019    | 50.00                 |                     | 0.9389740443          | 46.95    | 6541.70 | 7.03                       |                          |                    | 1000.00         |
      | 60        | 02 March 2019    | 50.00                 |                     | 0.9379724637          | 46.90    | 6498.68 | 6.99                       |                          |                    | 1000.00         |
      | 61        | 03 March 2019    | 50.00                 |                     | 0.9369719515          | 46.85    | 6455.62 | 6.94                       |                          |                    | 1000.00         |
      | 62        | 04 March 2019    | 50.00                 |                     | 0.9359725065          | 46.80    | 6412.51 | 6.89                       |                          |                    | 1000.00         |
      | 63        | 05 March 2019    | 50.00                 |                     | 0.9349741276          | 46.75    | 6369.36 | 6.85                       |                          |                    | 1000.00         |
      | 64        | 06 March 2019    | 50.00                 |                     | 0.9339768136          | 46.70    | 6326.16 | 6.80                       |                          |                    | 1000.00         |
      | 65        | 07 March 2019    | 50.00                 |                     | 0.9329805635          | 46.65    | 6282.92 | 6.76                       |                          |                    | 1000.00         |
      | 66        | 08 March 2019    | 50.00                 |                     | 0.931985376           | 46.60    | 6239.63 | 6.71                       |                          |                    | 1000.00         |
      | 67        | 09 March 2019    | 50.00                 |                     | 0.93099125            | 46.55    | 6196.29 | 6.66                       |                          |                    | 1000.00         |
      | 68        | 10 March 2019    | 50.00                 |                     | 0.9299981845          | 46.50    | 6152.91 | 6.62                       |                          |                    | 1000.00         |
      | 69        | 11 March 2019    | 50.00                 |                     | 0.9290061782          | 46.45    | 6109.48 | 6.57                       |                          |                    | 1000.00         |
      | 70        | 12 March 2019    | 50.00                 |                     | 0.9280152301          | 46.40    | 6066.00 | 6.52                       |                          |                    | 1000.00         |
      | 71        | 13 March 2019    | 50.00                 |                     | 0.927025339           | 46.35    | 6022.48 | 6.48                       |                          |                    | 1000.00         |
      | 72        | 14 March 2019    | 50.00                 |                     | 0.9260365038          | 46.30    | 5978.91 | 6.43                       |                          |                    | 1000.00         |
      | 73        | 15 March 2019    | 50.00                 |                     | 0.9250487234          | 46.25    | 5935.29 | 6.38                       |                          |                    | 1000.00         |
      | 74        | 16 March 2019    | 50.00                 |                     | 0.9240619966          | 46.20    | 5891.63 | 6.34                       |                          |                    | 1000.00         |
      | 75        | 17 March 2019    | 50.00                 |                     | 0.9230763224          | 46.15    | 5847.92 | 6.29                       |                          |                    | 1000.00         |
      | 76        | 18 March 2019    | 50.00                 |                     | 0.9220916995          | 46.10    | 5804.17 | 6.24                       |                          |                    | 1000.00         |
      | 77        | 19 March 2019    | 50.00                 |                     | 0.9211081269          | 46.06    | 5760.36 | 6.20                       |                          |                    | 1000.00         |
      | 78        | 20 March 2019    | 50.00                 |                     | 0.9201256034          | 46.01    | 5716.52 | 6.15                       |                          |                    | 1000.00         |
      | 79        | 21 March 2019    | 50.00                 |                     | 0.919144128           | 45.96    | 5672.62 | 6.10                       |                          |                    | 1000.00         |
      | 80        | 22 March 2019    | 50.00                 |                     | 0.9181636995          | 45.91    | 5628.68 | 6.06                       |                          |                    | 1000.00         |
      | 81        | 23 March 2019    | 50.00                 |                     | 0.9171843168          | 45.86    | 5584.69 | 6.01                       |                          |                    | 1000.00         |
      | 82        | 24 March 2019    | 50.00                 |                     | 0.9162059788          | 45.81    | 5540.65 | 5.96                       |                          |                    | 1000.00         |
      | 83        | 25 March 2019    | 50.00                 |                     | 0.9152286843          | 45.76    | 5496.57 | 5.92                       |                          |                    | 1000.00         |
      | 84        | 26 March 2019    | 50.00                 |                     | 0.9142524323          | 45.71    | 5452.44 | 5.87                       |                          |                    | 1000.00         |
      | 85        | 27 March 2019    | 50.00                 |                     | 0.9132772217          | 45.66    | 5408.26 | 5.82                       |                          |                    | 1000.00         |
      | 86        | 28 March 2019    | 50.00                 |                     | 0.9123030513          | 45.62    | 5364.03 | 5.78                       |                          |                    | 1000.00         |
      | 87        | 29 March 2019    | 50.00                 |                     | 0.91132992            | 45.57    | 5319.76 | 5.73                       |                          |                    | 1000.00         |
      | 88        | 30 March 2019    | 50.00                 |                     | 0.9103578267          | 45.52    | 5275.44 | 5.68                       |                          |                    | 1000.00         |
      | 89        | 31 March 2019    | 50.00                 |                     | 0.9093867703          | 45.47    | 5231.08 | 5.63                       |                          |                    | 1000.00         |
      | 90        | 01 April 2019    | 50.00                 |                     | 0.9084167498          | 45.42    | 5186.66 | 5.59                       |                          |                    | 1000.00         |
      | 91        | 02 April 2019    | 50.00                 |                     | 0.9074477639          | 45.37    | 5142.20 | 5.54                       |                          |                    | 1000.00         |
      | 92        | 03 April 2019    | 50.00                 |                     | 0.9064798116          | 45.32    | 5097.69 | 5.49                       |                          |                    | 1000.00         |
      | 93        | 04 April 2019    | 50.00                 |                     | 0.9055128918          | 45.28    | 5053.13 | 5.44                       |                          |                    | 1000.00         |
      | 94        | 05 April 2019    | 50.00                 |                     | 0.9045470035          | 45.23    | 5008.53 | 5.40                       |                          |                    | 1000.00         |
      | 95        | 06 April 2019    | 50.00                 |                     | 0.9035821453          | 45.18    | 4963.88 | 5.35                       |                          |                    | 1000.00         |
      | 96        | 07 April 2019    | 50.00                 |                     | 0.9026183164          | 45.13    | 4919.18 | 5.30                       |                          |                    | 1000.00         |
      | 97        | 08 April 2019    | 50.00                 |                     | 0.9016555156          | 45.08    | 4874.43 | 5.25                       |                          |                    | 1000.00         |
      | 98        | 09 April 2019    | 50.00                 |                     | 0.9006937418          | 45.03    | 4829.64 | 5.20                       |                          |                    | 1000.00         |
      | 99        | 10 April 2019    | 50.00                 |                     | 0.8997329939          | 44.99    | 4784.79 | 5.16                       |                          |                    | 1000.00         |
      | 100       | 11 April 2019    | 50.00                 |                     | 0.8987732707          | 44.94    | 4739.90 | 5.11                       |                          |                    | 1000.00         |
      | 101       | 12 April 2019    | 50.00                 |                     | 0.8978145713          | 44.89    | 4694.96 | 5.06                       |                          |                    | 1000.00         |
      | 102       | 13 April 2019    | 50.00                 |                     | 0.8968568945          | 44.84    | 4649.98 | 5.01                       |                          |                    | 1000.00         |
      | 103       | 14 April 2019    | 50.00                 |                     | 0.8959002393          | 44.80    | 4604.94 | 4.97                       |                          |                    | 1000.00         |
      | 104       | 15 April 2019    | 50.00                 |                     | 0.8949446045          | 44.75    | 4559.86 | 4.92                       |                          |                    | 1000.00         |
      | 105       | 16 April 2019    | 50.00                 |                     | 0.893989989           | 44.70    | 4514.73 | 4.87                       |                          |                    | 1000.00         |
      | 106       | 17 April 2019    | 50.00                 |                     | 0.8930363918          | 44.65    | 4469.55 | 4.82                       |                          |                    | 1000.00         |
      | 107       | 18 April 2019    | 50.00                 |                     | 0.8920838118          | 44.60    | 4424.32 | 4.77                       |                          |                    | 1000.00         |
      | 108       | 19 April 2019    | 50.00                 |                     | 0.8911322479          | 44.56    | 4379.05 | 4.72                       |                          |                    | 1000.00         |
      | 109       | 20 April 2019    | 50.00                 |                     | 0.890181699           | 44.51    | 4333.72 | 4.68                       |                          |                    | 1000.00         |
      | 110       | 21 April 2019    | 50.00                 |                     | 0.889232164           | 44.46    | 4288.35 | 4.63                       |                          |                    | 1000.00         |
      | 111       | 22 April 2019    | 50.00                 |                     | 0.8882836418          | 44.41    | 4242.93 | 4.58                       |                          |                    | 1000.00         |
      | 112       | 23 April 2019    | 50.00                 |                     | 0.8873361314          | 44.37    | 4197.46 | 4.53                       |                          |                    | 1000.00         |
      | 113       | 24 April 2019    | 50.00                 |                     | 0.8863896318          | 44.32    | 4151.94 | 4.48                       |                          |                    | 1000.00         |
      | 114       | 25 April 2019    | 50.00                 |                     | 0.8854441417          | 44.27    | 4106.38 | 4.43                       |                          |                    | 1000.00         |
      | 115       | 26 April 2019    | 50.00                 |                     | 0.8844996601          | 44.22    | 4060.76 | 4.38                       |                          |                    | 1000.00         |
      | 116       | 27 April 2019    | 50.00                 |                     | 0.883556186           | 44.18    | 4015.10 | 4.34                       |                          |                    | 1000.00         |
      | 117       | 28 April 2019    | 50.00                 |                     | 0.8826137183          | 44.13    | 3969.38 | 4.29                       |                          |                    | 1000.00         |
      | 118       | 29 April 2019    | 50.00                 |                     | 0.8816722559          | 44.08    | 3923.62 | 4.24                       |                          |                    | 1000.00         |
      | 119       | 30 April 2019    | 50.00                 |                     | 0.8807317977          | 44.04    | 3877.81 | 4.19                       |                          |                    | 1000.00         |
      | 120       | 01 May 2019      | 50.00                 |                     | 0.8797923427          | 43.99    | 3831.95 | 4.14                       |                          |                    | 1000.00         |
      | 121       | 02 May 2019      | 50.00                 |                     | 0.8788538898          | 43.94    | 3786.04 | 4.09                       |                          |                    | 1000.00         |
      | 122       | 03 May 2019      | 50.00                 |                     | 0.8779164379          | 43.90    | 3740.09 | 4.04                       |                          |                    | 1000.00         |
      | 123       | 04 May 2019      | 50.00                 |                     | 0.876979986           | 43.85    | 3694.08 | 3.99                       |                          |                    | 1000.00         |
      | 124       | 05 May 2019      | 50.00                 |                     | 0.8760445329          | 43.80    | 3648.03 | 3.94                       |                          |                    | 1000.00         |
      | 125       | 06 May 2019      | 50.00                 |                     | 0.8751100777          | 43.76    | 3601.92 | 3.90                       |                          |                    | 1000.00         |
      | 126       | 07 May 2019      | 50.00                 |                     | 0.8741766193          | 43.71    | 3555.77 | 3.85                       |                          |                    | 1000.00         |
      | 127       | 08 May 2019      | 50.00                 |                     | 0.8732441565          | 43.66    | 3509.56 | 3.80                       |                          |                    | 1000.00         |
      | 128       | 09 May 2019      | 50.00                 |                     | 0.8723126884          | 43.62    | 3463.31 | 3.75                       |                          |                    | 1000.00         |
      | 129       | 10 May 2019      | 50.00                 |                     | 0.8713822138          | 43.57    | 3417.01 | 3.70                       |                          |                    | 1000.00         |
      | 130       | 11 May 2019      | 50.00                 |                     | 0.8704527318          | 43.52    | 3370.66 | 3.65                       |                          |                    | 1000.00         |
      | 131       | 12 May 2019      | 50.00                 |                     | 0.8695242412          | 43.48    | 3324.26 | 3.60                       |                          |                    | 1000.00         |
      | 132       | 13 May 2019      | 50.00                 |                     | 0.868596741           | 43.43    | 3277.81 | 3.55                       |                          |                    | 1000.00         |
      | 133       | 14 May 2019      | 50.00                 |                     | 0.8676702302          | 43.38    | 3231.31 | 3.50                       |                          |                    | 1000.00         |
      | 134       | 15 May 2019      | 50.00                 |                     | 0.8667447076          | 43.34    | 3184.76 | 3.45                       |                          |                    | 1000.00         |
      | 135       | 16 May 2019      | 50.00                 |                     | 0.8658201723          | 43.29    | 3138.16 | 3.40                       |                          |                    | 1000.00         |
      | 136       | 17 May 2019      | 50.00                 |                     | 0.8648966231          | 43.24    | 3091.51 | 3.35                       |                          |                    | 1000.00         |
      | 137       | 18 May 2019      | 50.00                 |                     | 0.8639740591          | 43.20    | 3044.81 | 3.30                       |                          |                    | 1000.00         |
      | 138       | 19 May 2019      | 50.00                 |                     | 0.8630524792          | 43.15    | 2998.06 | 3.25                       |                          |                    | 1000.00         |
      | 139       | 20 May 2019      | 50.00                 |                     | 0.8621318823          | 43.11    | 2951.26 | 3.20                       |                          |                    | 1000.00         |
      | 140       | 21 May 2019      | 50.00                 |                     | 0.8612122673          | 43.06    | 2904.42 | 3.15                       |                          |                    | 1000.00         |
      | 141       | 22 May 2019      | 50.00                 |                     | 0.8602936333          | 43.01    | 2857.52 | 3.10                       |                          |                    | 1000.00         |
      | 142       | 23 May 2019      | 50.00                 |                     | 0.8593759792          | 42.97    | 2810.57 | 3.05                       |                          |                    | 1000.00         |
      | 143       | 24 May 2019      | 50.00                 |                     | 0.8584593039          | 42.92    | 2763.57 | 3.00                       |                          |                    | 1000.00         |
      | 144       | 25 May 2019      | 50.00                 |                     | 0.8575436064          | 42.88    | 2716.52 | 2.95                       |                          |                    | 1000.00         |
      | 145       | 26 May 2019      | 50.00                 |                     | 0.8566288857          | 42.83    | 2669.42 | 2.90                       |                          |                    | 1000.00         |
      | 146       | 27 May 2019      | 50.00                 |                     | 0.8557151407          | 42.79    | 2622.27 | 2.85                       |                          |                    | 1000.00         |
      | 147       | 28 May 2019      | 50.00                 |                     | 0.8548023703          | 42.74    | 2575.07 | 2.80                       |                          |                    | 1000.00         |
      | 148       | 29 May 2019      | 50.00                 |                     | 0.8538905736          | 42.69    | 2527.82 | 2.75                       |                          |                    | 1000.00         |
      | 149       | 30 May 2019      | 50.00                 |                     | 0.8529797495          | 42.65    | 2480.52 | 2.70                       |                          |                    | 1000.00         |
      | 150       | 31 May 2019      | 50.00                 |                     | 0.8520698969          | 42.60    | 2433.17 | 2.65                       |                          |                    | 1000.00         |
      | 151       | 01 June 2019     | 50.00                 |                     | 0.8511610148          | 42.56    | 2385.77 | 2.60                       |                          |                    | 1000.00         |
      | 152       | 02 June 2019     | 50.00                 |                     | 0.8502531022          | 42.51    | 2338.31 | 2.55                       |                          |                    | 1000.00         |
      | 153       | 03 June 2019     | 50.00                 |                     | 0.8493461581          | 42.47    | 2290.81 | 2.50                       |                          |                    | 1000.00         |
      | 154       | 04 June 2019     | 50.00                 |                     | 0.8484401814          | 42.42    | 2243.26 | 2.45                       |                          |                    | 1000.00         |
      | 155       | 05 June 2019     | 50.00                 |                     | 0.8475351711          | 42.38    | 2195.65 | 2.40                       |                          |                    | 1000.00         |
      | 156       | 06 June 2019     | 50.00                 |                     | 0.8466311261          | 42.33    | 2148.00 | 2.34                       |                          |                    | 1000.00         |
      | 157       | 07 June 2019     | 50.00                 |                     | 0.8457280454          | 42.29    | 2100.29 | 2.29                       |                          |                    | 1000.00         |
      | 158       | 08 June 2019     | 50.00                 |                     | 0.844825928           | 42.24    | 2052.53 | 2.24                       |                          |                    | 1000.00         |
      | 159       | 09 June 2019     | 50.00                 |                     | 0.8439247729          | 42.20    | 2004.73 | 2.19                       |                          |                    | 1000.00         |
      | 160       | 10 June 2019     | 50.00                 |                     | 0.8430245791          | 42.15    | 1956.87 | 2.14                       |                          |                    | 1000.00         |
      | 161       | 11 June 2019     | 50.00                 |                     | 0.8421253454          | 42.11    | 1908.96 | 2.09                       |                          |                    | 1000.00         |
      | 162       | 12 June 2019     | 50.00                 |                     | 0.841227071           | 42.06    | 1860.99 | 2.04                       |                          |                    | 1000.00         |
      | 163       | 13 June 2019     | 50.00                 |                     | 0.8403297547          | 42.02    | 1812.98 | 1.99                       |                          |                    | 1000.00         |
      | 164       | 14 June 2019     | 50.00                 |                     | 0.8394333956          | 41.97    | 1764.92 | 1.94                       |                          |                    | 1000.00         |
      | 165       | 15 June 2019     | 50.00                 |                     | 0.8385379925          | 41.93    | 1716.80 | 1.88                       |                          |                    | 1000.00         |
      | 166       | 16 June 2019     | 50.00                 |                     | 0.8376435446          | 41.88    | 1668.64 | 1.83                       |                          |                    | 1000.00         |
      | 167       | 17 June 2019     | 50.00                 |                     | 0.8367500508          | 41.84    | 1620.42 | 1.78                       |                          |                    | 1000.00         |
      | 168       | 18 June 2019     | 50.00                 |                     | 0.83585751            | 41.79    | 1572.15 | 1.73                       |                          |                    | 1000.00         |
      | 169       | 19 June 2019     | 50.00                 |                     | 0.8349659213          | 41.75    | 1523.83 | 1.68                       |                          |                    | 1000.00         |
      | 170       | 20 June 2019     | 50.00                 |                     | 0.8340752837          | 41.70    | 1475.45 | 1.63                       |                          |                    | 1000.00         |
      | 171       | 21 June 2019     | 50.00                 |                     | 0.833185596           | 41.66    | 1427.03 | 1.58                       |                          |                    | 1000.00         |
      | 172       | 22 June 2019     | 50.00                 |                     | 0.8322968574          | 41.61    | 1378.55 | 1.52                       |                          |                    | 1000.00         |
      | 173       | 23 June 2019     | 50.00                 |                     | 0.8314090667          | 41.57    | 1330.02 | 1.47                       |                          |                    | 1000.00         |
      | 174       | 24 June 2019     | 50.00                 |                     | 0.8305222231          | 41.53    | 1281.45 | 1.42                       |                          |                    | 1000.00         |
      | 175       | 25 June 2019     | 50.00                 |                     | 0.8296363254          | 41.48    | 1232.81 | 1.37                       |                          |                    | 1000.00         |
      | 176       | 26 June 2019     | 50.00                 |                     | 0.8287513727          | 41.44    | 1184.13 | 1.32                       |                          |                    | 1000.00         |
      | 177       | 27 June 2019     | 50.00                 |                     | 0.8278673639          | 41.39    | 1135.39 | 1.26                       |                          |                    | 1000.00         |
      | 178       | 28 June 2019     | 50.00                 |                     | 0.8269842981          | 41.35    | 1086.61 | 1.21                       |                          |                    | 1000.00         |
      | 179       | 29 June 2019     | 50.00                 |                     | 0.8261021742          | 41.31    | 1037.77 | 1.16                       |                          |                    | 1000.00         |
      | 180       | 30 June 2019     | 50.00                 |                     | 0.8252209913          | 41.26    | 988.88  | 1.11                       |                          |                    | 1000.00         |
      | 181       | 01 July 2019     | 50.00                 |                     | 0.8243407483          | 41.22    | 939.93  | 1.06                       |                          |                    | 1000.00         |
      | 182       | 02 July 2019     | 50.00                 |                     | 0.8234614442          | 41.17    | 890.93  | 1.00                       |                          |                    | 1000.00         |
      | 183       | 03 July 2019     | 50.00                 |                     | 0.8225830781          | 41.13    | 841.89  | 0.95                       |                          |                    | 1000.00         |
      | 184       | 04 July 2019     | 50.00                 |                     | 0.8217056489          | 41.09    | 792.79  | 0.90                       |                          |                    | 1000.00         |
      | 185       | 05 July 2019     | 50.00                 |                     | 0.8208291556          | 41.04    | 743.63  | 0.85                       |                          |                    | 1000.00         |
      | 186       | 06 July 2019     | 50.00                 |                     | 0.8199535973          | 41.00    | 694.43  | 0.79                       |                          |                    | 1000.00         |
      | 187       | 07 July 2019     | 50.00                 |                     | 0.8190789729          | 40.95    | 645.17  | 0.74                       |                          |                    | 1000.00         |
      | 188       | 08 July 2019     | 50.00                 |                     | 0.8182052815          | 40.91    | 595.86  | 0.69                       |                          |                    | 1000.00         |
      | 189       | 09 July 2019     | 50.00                 |                     | 0.8173325219          | 40.87    | 546.49  | 0.64                       |                          |                    | 1000.00         |
      | 190       | 10 July 2019     | 50.00                 |                     | 0.8164606934          | 40.82    | 497.08  | 0.58                       |                          |                    | 1000.00         |
      | 191       | 11 July 2019     | 50.00                 |                     | 0.8155897948          | 40.78    | 447.61  | 0.53                       |                          |                    | 1000.00         |
      | 192       | 12 July 2019     | 50.00                 |                     | 0.8147198252          | 40.74    | 398.08  | 0.48                       |                          |                    | 1000.00         |
      | 193       | 13 July 2019     | 50.00                 |                     | 0.8138507835          | 40.69    | 348.51  | 0.43                       |                          |                    | 1000.00         |
      | 194       | 14 July 2019     | 50.00                 |                     | 0.8129826688          | 40.65    | 298.88  | 0.37                       |                          |                    | 1000.00         |
      | 195       | 15 July 2019     | 50.00                 |                     | 0.8121154802          | 40.61    | 249.20  | 0.32                       |                          |                    | 1000.00         |
      | 196       | 16 July 2019     | 50.00                 |                     | 0.8112492165          | 40.56    | 199.47  | 0.27                       |                          |                    | 1000.00         |
      | 197       | 17 July 2019     | 50.00                 |                     | 0.8103838768          | 40.52    | 149.68  | 0.21                       |                          |                    | 1000.00         |
      | 198       | 18 July 2019     | 50.00                 |                     | 0.8095194602          | 40.48    | 99.84   | 0.16                       |                          |                    | 1000.00         |
      | 199       | 19 July 2019     | 50.00                 |                     | 0.8086559657          | 40.43    | 49.95   | 0.11                       |                          |                    | 1000.00         |
      | 200       | 20 July 2019     | 50.00                 |                     | 0.8077933922          | 40.39    | 0.00    | 0.05                       |                          |                    | 1000.00         |
    When Admin sets the business date to "02 January 2019"
    And Customer makes repayment on "02 January 2019" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2019"
    And Customer makes repayment on "03 January 2019" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2019"
    And Customer makes repayment on "04 January 2019" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2019"
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    And Admin adds Discount fee adjustment with "500" amount on transaction date "05 January 2019" on Working Capital loan account for last discount
    Then Working Capital loan amortization schedule has 191 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | discountFactor        | npvValue | balance | expectedAmortizationAmount | actualAmortizationAmount | incomeModification | deferredBalance |
      | 0         | 01 January 2019  | -9000.00              |                     | 1                     | -9000    | 9000.00 |                            |                          |                    | 500.00          |
      | 1         | 02 January 2019  | 50.00                 | 50.00               | 1                     | 50       | 8955.14 | 5.14                       | 5.14                     | 0.00               | 494.86          |
      | 2         | 03 January 2019  | 50.00                 | 50.00               | 1                     | 50       | 8910.26 | 5.12                       | 5.12                     | 0.00               | 489.74          |
      | 3         | 04 January 2019  | 50.00                 | 50.00               | 1                     | 50       | 8865.35 | 5.09                       | 5.09                     | 0.00               | 484.65          |
      | 4         | 05 January 2019  | 50.00                 |                     | 0.9994288744          | 49.97    | 8820.42 | 5.07                       |                          |                    | 484.65          |
      | 5         | 06 January 2019  | 50.00                 |                     | 0.998858075           | 49.94    | 8775.46 | 5.04                       |                          |                    | 484.65          |
      | 6         | 07 January 2019  | 50.00                 |                     | 0.9982876016          | 49.91    | 8730.47 | 5.01                       |                          |                    | 484.65          |
      | 7         | 08 January 2019  | 50.00                 |                     | 0.997717454           | 49.89    | 8685.46 | 4.99                       |                          |                    | 484.65          |
      | 8         | 09 January 2019  | 50.00                 |                     | 0.997147632           | 49.86    | 8640.43 | 4.96                       |                          |                    | 484.65          |
      | 9         | 10 January 2019  | 50.00                 |                     | 0.9965781355          | 49.83    | 8595.36 | 4.94                       |                          |                    | 484.65          |
      | 10        | 11 January 2019  | 50.00                 |                     | 0.9960089642          | 49.80    | 8550.28 | 4.91                       |                          |                    | 484.65          |
      | 11        | 12 January 2019  | 50.00                 |                     | 0.995440118           | 49.77    | 8505.16 | 4.89                       |                          |                    | 484.65          |
      | 12        | 13 January 2019  | 50.00                 |                     | 0.9948715966          | 49.74    | 8460.02 | 4.86                       |                          |                    | 484.65          |
      | 13        | 14 January 2019  | 50.00                 |                     | 0.9943034             | 49.72    | 8414.86 | 4.83                       |                          |                    | 484.65          |
      | 14        | 15 January 2019  | 50.00                 |                     | 0.9937355279          | 49.69    | 8369.66 | 4.81                       |                          |                    | 484.65          |
      | 15        | 16 January 2019  | 50.00                 |                     | 0.9931679801          | 49.66    | 8324.45 | 4.78                       |                          |                    | 484.65          |
      | 16        | 17 January 2019  | 50.00                 |                     | 0.9926007564          | 49.63    | 8279.20 | 4.76                       |                          |                    | 484.65          |
      | 17        | 18 January 2019  | 50.00                 |                     | 0.9920338567          | 49.60    | 8233.94 | 4.73                       |                          |                    | 484.65          |
      | 18        | 19 January 2019  | 50.00                 |                     | 0.9914672808          | 49.57    | 8188.64 | 4.71                       |                          |                    | 484.65          |
      | 19        | 20 January 2019  | 50.00                 |                     | 0.9909010285          | 49.55    | 8143.32 | 4.68                       |                          |                    | 484.65          |
      | 20        | 21 January 2019  | 50.00                 |                     | 0.9903350995          | 49.52    | 8097.97 | 4.65                       |                          |                    | 484.65          |
      | 21        | 22 January 2019  | 50.00                 |                     | 0.9897694938          | 49.49    | 8052.60 | 4.63                       |                          |                    | 484.65          |
      | 22        | 23 January 2019  | 50.00                 |                     | 0.9892042111          | 49.46    | 8007.20 | 4.60                       |                          |                    | 484.65          |
      | 23        | 24 January 2019  | 50.00                 |                     | 0.9886392513          | 49.43    | 7961.78 | 4.58                       |                          |                    | 484.65          |
      | 24        | 25 January 2019  | 50.00                 |                     | 0.9880746141          | 49.40    | 7916.33 | 4.55                       |                          |                    | 484.65          |
      | 25        | 26 January 2019  | 50.00                 |                     | 0.9875102994          | 49.38    | 7870.85 | 4.52                       |                          |                    | 484.65          |
      | 26        | 27 January 2019  | 50.00                 |                     | 0.986946307           | 49.35    | 7825.35 | 4.50                       |                          |                    | 484.65          |
      | 27        | 28 January 2019  | 50.00                 |                     | 0.9863826367          | 49.32    | 7779.82 | 4.47                       |                          |                    | 484.65          |
      | 28        | 29 January 2019  | 50.00                 |                     | 0.9858192883          | 49.29    | 7734.27 | 4.45                       |                          |                    | 484.65          |
      | 29        | 30 January 2019  | 50.00                 |                     | 0.9852562617          | 49.26    | 7688.69 | 4.42                       |                          |                    | 484.65          |
      | 30        | 31 January 2019  | 50.00                 |                     | 0.9846935566          | 49.23    | 7643.08 | 4.39                       |                          |                    | 484.65          |
      | 31        | 01 February 2019 | 50.00                 |                     | 0.9841311729          | 49.21    | 7597.45 | 4.37                       |                          |                    | 484.65          |
      | 32        | 02 February 2019 | 50.00                 |                     | 0.9835691104          | 49.18    | 7551.79 | 4.34                       |                          |                    | 484.65          |
      | 33        | 03 February 2019 | 50.00                 |                     | 0.9830073689          | 49.15    | 7506.11 | 4.32                       |                          |                    | 484.65          |
      | 34        | 04 February 2019 | 50.00                 |                     | 0.9824459483          | 49.12    | 7460.40 | 4.29                       |                          |                    | 484.65          |
      | 35        | 05 February 2019 | 50.00                 |                     | 0.9818848482          | 49.09    | 7414.66 | 4.26                       |                          |                    | 484.65          |
      | 36        | 06 February 2019 | 50.00                 |                     | 0.9813240687          | 49.07    | 7368.90 | 4.24                       |                          |                    | 484.65          |
      | 37        | 07 February 2019 | 50.00                 |                     | 0.9807636094          | 49.04    | 7323.11 | 4.21                       |                          |                    | 484.65          |
      | 38        | 08 February 2019 | 50.00                 |                     | 0.9802034702          | 49.01    | 7277.29 | 4.18                       |                          |                    | 484.65          |
      | 39        | 09 February 2019 | 50.00                 |                     | 0.9796436509          | 48.98    | 7231.45 | 4.16                       |                          |                    | 484.65          |
      | 40        | 10 February 2019 | 50.00                 |                     | 0.9790841513          | 48.95    | 7185.58 | 4.13                       |                          |                    | 484.65          |
      | 41        | 11 February 2019 | 50.00                 |                     | 0.9785249713          | 48.93    | 7139.69 | 4.11                       |                          |                    | 484.65          |
      | 42        | 12 February 2019 | 50.00                 |                     | 0.9779661106          | 48.90    | 7093.77 | 4.08                       |                          |                    | 484.65          |
      | 43        | 13 February 2019 | 50.00                 |                     | 0.9774075692          | 48.87    | 7047.82 | 4.05                       |                          |                    | 484.65          |
      | 44        | 14 February 2019 | 50.00                 |                     | 0.9768493467          | 48.84    | 7001.85 | 4.03                       |                          |                    | 484.65          |
      | 45        | 15 February 2019 | 50.00                 |                     | 0.976291443           | 48.81    | 6955.85 | 4.00                       |                          |                    | 484.65          |
      | 46        | 16 February 2019 | 50.00                 |                     | 0.975733858           | 48.79    | 6909.83 | 3.97                       |                          |                    | 484.65          |
      | 47        | 17 February 2019 | 50.00                 |                     | 0.9751765914          | 48.76    | 6863.78 | 3.95                       |                          |                    | 484.65          |
      | 48        | 18 February 2019 | 50.00                 |                     | 0.9746196431          | 48.73    | 6817.70 | 3.92                       |                          |                    | 484.65          |
      | 49        | 19 February 2019 | 50.00                 |                     | 0.9740630129          | 48.70    | 6771.59 | 3.90                       |                          |                    | 484.65          |
      | 50        | 20 February 2019 | 50.00                 |                     | 0.9735067006          | 48.68    | 6725.46 | 3.87                       |                          |                    | 484.65          |
      | 51        | 21 February 2019 | 50.00                 |                     | 0.972950706           | 48.65    | 6679.31 | 3.84                       |                          |                    | 484.65          |
      | 52        | 22 February 2019 | 50.00                 |                     | 0.9723950289          | 48.62    | 6633.12 | 3.82                       |                          |                    | 484.65          |
      | 53        | 23 February 2019 | 50.00                 |                     | 0.9718396692          | 48.59    | 6586.91 | 3.79                       |                          |                    | 484.65          |
      | 54        | 24 February 2019 | 50.00                 |                     | 0.9712846267          | 48.56    | 6540.68 | 3.76                       |                          |                    | 484.65          |
      | 55        | 25 February 2019 | 50.00                 |                     | 0.9707299012          | 48.54    | 6494.42 | 3.74                       |                          |                    | 484.65          |
      | 56        | 26 February 2019 | 50.00                 |                     | 0.9701754925          | 48.51    | 6448.13 | 3.71                       |                          |                    | 484.65          |
      | 57        | 27 February 2019 | 50.00                 |                     | 0.9696214004          | 48.48    | 6401.81 | 3.68                       |                          |                    | 484.65          |
      | 58        | 28 February 2019 | 50.00                 |                     | 0.9690676248          | 48.45    | 6355.47 | 3.66                       |                          |                    | 484.65          |
      | 59        | 01 March 2019    | 50.00                 |                     | 0.9685141655          | 48.43    | 6309.10 | 3.63                       |                          |                    | 484.65          |
      | 60        | 02 March 2019    | 50.00                 |                     | 0.9679610223          | 48.40    | 6262.71 | 3.61                       |                          |                    | 484.65          |
      | 61        | 03 March 2019    | 50.00                 |                     | 0.967408195           | 48.37    | 6216.29 | 3.58                       |                          |                    | 484.65          |
      | 62        | 04 March 2019    | 50.00                 |                     | 0.9668556834          | 48.34    | 6169.84 | 3.55                       |                          |                    | 484.65          |
      | 63        | 05 March 2019    | 50.00                 |                     | 0.9663034874          | 48.32    | 6123.36 | 3.53                       |                          |                    | 484.65          |
      | 64        | 06 March 2019    | 50.00                 |                     | 0.9657516067          | 48.29    | 6076.86 | 3.50                       |                          |                    | 484.65          |
      | 65        | 07 March 2019    | 50.00                 |                     | 0.9652000413          | 48.26    | 6030.34 | 3.47                       |                          |                    | 484.65          |
      | 66        | 08 March 2019    | 50.00                 |                     | 0.9646487908          | 48.23    | 5983.78 | 3.45                       |                          |                    | 484.65          |
      | 67        | 09 March 2019    | 50.00                 |                     | 0.9640978552          | 48.20    | 5937.20 | 3.42                       |                          |                    | 484.65          |
      | 68        | 10 March 2019    | 50.00                 |                     | 0.9635472342          | 48.18    | 5890.59 | 3.39                       |                          |                    | 484.65          |
      | 69        | 11 March 2019    | 50.00                 |                     | 0.9629969277          | 48.15    | 5843.96 | 3.37                       |                          |                    | 484.65          |
      | 70        | 12 March 2019    | 50.00                 |                     | 0.9624469355          | 48.12    | 5797.30 | 3.34                       |                          |                    | 484.65          |
      | 71        | 13 March 2019    | 50.00                 |                     | 0.9618972575          | 48.09    | 5750.61 | 3.31                       |                          |                    | 484.65          |
      | 72        | 14 March 2019    | 50.00                 |                     | 0.9613478933          | 48.07    | 5703.90 | 3.29                       |                          |                    | 484.65          |
      | 73        | 15 March 2019    | 50.00                 |                     | 0.9607988429          | 48.04    | 5657.16 | 3.26                       |                          |                    | 484.65          |
      | 74        | 16 March 2019    | 50.00                 |                     | 0.9602501061          | 48.01    | 5610.39 | 3.23                       |                          |                    | 484.65          |
      | 75        | 17 March 2019    | 50.00                 |                     | 0.9597016827          | 47.99    | 5563.60 | 3.21                       |                          |                    | 484.65          |
      | 76        | 18 March 2019    | 50.00                 |                     | 0.9591535725          | 47.96    | 5516.78 | 3.18                       |                          |                    | 484.65          |
      | 77        | 19 March 2019    | 50.00                 |                     | 0.9586057754          | 47.93    | 5469.93 | 3.15                       |                          |                    | 484.65          |
      | 78        | 20 March 2019    | 50.00                 |                     | 0.9580582911          | 47.90    | 5423.06 | 3.13                       |                          |                    | 484.65          |
      | 79        | 21 March 2019    | 50.00                 |                     | 0.9575111195          | 47.88    | 5376.15 | 3.10                       |                          |                    | 484.65          |
      | 80        | 22 March 2019    | 50.00                 |                     | 0.9569642603          | 47.85    | 5329.23 | 3.07                       |                          |                    | 484.65          |
      | 81        | 23 March 2019    | 50.00                 |                     | 0.9564177136          | 47.82    | 5282.27 | 3.05                       |                          |                    | 484.65          |
      | 82        | 24 March 2019    | 50.00                 |                     | 0.9558714789          | 47.79    | 5235.29 | 3.02                       |                          |                    | 484.65          |
      | 83        | 25 March 2019    | 50.00                 |                     | 0.9553255563          | 47.77    | 5188.28 | 2.99                       |                          |                    | 484.65          |
      | 84        | 26 March 2019    | 50.00                 |                     | 0.9547799454          | 47.74    | 5141.25 | 2.96                       |                          |                    | 484.65          |
      | 85        | 27 March 2019    | 50.00                 |                     | 0.9542346461          | 47.71    | 5094.18 | 2.94                       |                          |                    | 484.65          |
      | 86        | 28 March 2019    | 50.00                 |                     | 0.9536896583          | 47.68    | 5047.10 | 2.91                       |                          |                    | 484.65          |
      | 87        | 29 March 2019    | 50.00                 |                     | 0.9531449817          | 47.66    | 4999.98 | 2.88                       |                          |                    | 484.65          |
      | 88        | 30 March 2019    | 50.00                 |                     | 0.9526006162          | 47.63    | 4952.84 | 2.86                       |                          |                    | 484.65          |
      | 89        | 31 March 2019    | 50.00                 |                     | 0.9520565616          | 47.60    | 4905.67 | 2.83                       |                          |                    | 484.65          |
      | 90        | 01 April 2019    | 50.00                 |                     | 0.9515128177          | 47.58    | 4858.47 | 2.80                       |                          |                    | 484.65          |
      | 91        | 02 April 2019    | 50.00                 |                     | 0.9509693844          | 47.55    | 4811.25 | 2.78                       |                          |                    | 484.65          |
      | 92        | 03 April 2019    | 50.00                 |                     | 0.9504262615          | 47.52    | 4764.00 | 2.75                       |                          |                    | 484.65          |
      | 93        | 04 April 2019    | 50.00                 |                     | 0.9498834487          | 47.49    | 4716.72 | 2.72                       |                          |                    | 484.65          |
      | 94        | 05 April 2019    | 50.00                 |                     | 0.949340946           | 47.47    | 4669.41 | 2.70                       |                          |                    | 484.65          |
      | 95        | 06 April 2019    | 50.00                 |                     | 0.948798753           | 47.44    | 4622.08 | 2.67                       |                          |                    | 484.65          |
      | 96        | 07 April 2019    | 50.00                 |                     | 0.9482568698          | 47.41    | 4574.72 | 2.64                       |                          |                    | 484.65          |
      | 97        | 08 April 2019    | 50.00                 |                     | 0.947715296           | 47.39    | 4527.34 | 2.61                       |                          |                    | 484.65          |
      | 98        | 09 April 2019    | 50.00                 |                     | 0.9471740316          | 47.36    | 4479.93 | 2.59                       |                          |                    | 484.65          |
      | 99        | 10 April 2019    | 50.00                 |                     | 0.9466330762          | 47.33    | 4432.49 | 2.56                       |                          |                    | 484.65          |
      | 100       | 11 April 2019    | 50.00                 |                     | 0.9460924298          | 47.30    | 4385.02 | 2.53                       |                          |                    | 484.65          |
      | 101       | 12 April 2019    | 50.00                 |                     | 0.9455520922          | 47.28    | 4337.52 | 2.51                       |                          |                    | 484.65          |
      | 102       | 13 April 2019    | 50.00                 |                     | 0.9450120632          | 47.25    | 4290.00 | 2.48                       |                          |                    | 484.65          |
      | 103       | 14 April 2019    | 50.00                 |                     | 0.9444723427          | 47.22    | 4242.45 | 2.45                       |                          |                    | 484.65          |
      | 104       | 15 April 2019    | 50.00                 |                     | 0.9439329303          | 47.20    | 4194.88 | 2.42                       |                          |                    | 484.65          |
      | 105       | 16 April 2019    | 50.00                 |                     | 0.9433938261          | 47.17    | 4147.28 | 2.40                       |                          |                    | 484.65          |
      | 106       | 17 April 2019    | 50.00                 |                     | 0.9428550297          | 47.14    | 4099.65 | 2.37                       |                          |                    | 484.65          |
      | 107       | 18 April 2019    | 50.00                 |                     | 0.9423165411          | 47.12    | 4051.99 | 2.34                       |                          |                    | 484.65          |
      | 108       | 19 April 2019    | 50.00                 |                     | 0.94177836            | 47.09    | 4004.30 | 2.32                       |                          |                    | 484.65          |
      | 109       | 20 April 2019    | 50.00                 |                     | 0.9412404863          | 47.06    | 3956.59 | 2.29                       |                          |                    | 484.65          |
      | 110       | 21 April 2019    | 50.00                 |                     | 0.9407029197          | 47.04    | 3908.85 | 2.26                       |                          |                    | 484.65          |
      | 111       | 22 April 2019    | 50.00                 |                     | 0.9401656602          | 47.01    | 3861.09 | 2.23                       |                          |                    | 484.65          |
      | 112       | 23 April 2019    | 50.00                 |                     | 0.9396287075          | 46.98    | 3813.29 | 2.21                       |                          |                    | 484.65          |
      | 113       | 24 April 2019    | 50.00                 |                     | 0.9390920615          | 46.95    | 3765.47 | 2.18                       |                          |                    | 484.65          |
      | 114       | 25 April 2019    | 50.00                 |                     | 0.938555722           | 46.93    | 3717.62 | 2.15                       |                          |                    | 484.65          |
      | 115       | 26 April 2019    | 50.00                 |                     | 0.9380196888          | 46.90    | 3669.75 | 2.12                       |                          |                    | 484.65          |
      | 116       | 27 April 2019    | 50.00                 |                     | 0.9374839618          | 46.87    | 3621.85 | 2.10                       |                          |                    | 484.65          |
      | 117       | 28 April 2019    | 50.00                 |                     | 0.9369485407          | 46.85    | 3573.92 | 2.07                       |                          |                    | 484.65          |
      | 118       | 29 April 2019    | 50.00                 |                     | 0.9364134254          | 46.82    | 3525.96 | 2.04                       |                          |                    | 484.65          |
      | 119       | 30 April 2019    | 50.00                 |                     | 0.9358786157          | 46.79    | 3477.97 | 2.01                       |                          |                    | 484.65          |
      | 120       | 01 May 2019      | 50.00                 |                     | 0.9353441115          | 46.77    | 3429.96 | 1.99                       |                          |                    | 484.65          |
      | 121       | 02 May 2019      | 50.00                 |                     | 0.9348099125          | 46.74    | 3381.92 | 1.96                       |                          |                    | 484.65          |
      | 122       | 03 May 2019      | 50.00                 |                     | 0.9342760186          | 46.71    | 3333.85 | 1.93                       |                          |                    | 484.65          |
      | 123       | 04 May 2019      | 50.00                 |                     | 0.9337424297          | 46.69    | 3285.76 | 1.91                       |                          |                    | 484.65          |
      | 124       | 05 May 2019      | 50.00                 |                     | 0.9332091455          | 46.66    | 3237.64 | 1.88                       |                          |                    | 484.65          |
      | 125       | 06 May 2019      | 50.00                 |                     | 0.9326761659          | 46.63    | 3189.49 | 1.85                       |                          |                    | 484.65          |
      | 126       | 07 May 2019      | 50.00                 |                     | 0.9321434906          | 46.61    | 3141.31 | 1.82                       |                          |                    | 484.65          |
      | 127       | 08 May 2019      | 50.00                 |                     | 0.9316111196          | 46.58    | 3093.10 | 1.80                       |                          |                    | 484.65          |
      | 128       | 09 May 2019      | 50.00                 |                     | 0.9310790527          | 46.55    | 3044.87 | 1.77                       |                          |                    | 484.65          |
      | 129       | 10 May 2019      | 50.00                 |                     | 0.9305472896          | 46.53    | 2996.61 | 1.74                       |                          |                    | 484.65          |
      | 130       | 11 May 2019      | 50.00                 |                     | 0.9300158302          | 46.50    | 2948.32 | 1.71                       |                          |                    | 484.65          |
      | 131       | 12 May 2019      | 50.00                 |                     | 0.9294846744          | 46.47    | 2900.01 | 1.68                       |                          |                    | 484.65          |
      | 132       | 13 May 2019      | 50.00                 |                     | 0.9289538219          | 46.45    | 2851.67 | 1.66                       |                          |                    | 484.65          |
      | 133       | 14 May 2019      | 50.00                 |                     | 0.9284232726          | 46.42    | 2803.30 | 1.63                       |                          |                    | 484.65          |
      | 134       | 15 May 2019      | 50.00                 |                     | 0.9278930263          | 46.39    | 2754.90 | 1.60                       |                          |                    | 484.65          |
      | 135       | 16 May 2019      | 50.00                 |                     | 0.9273630828          | 46.37    | 2706.47 | 1.57                       |                          |                    | 484.65          |
      | 136       | 17 May 2019      | 50.00                 |                     | 0.926833442           | 46.34    | 2658.02 | 1.55                       |                          |                    | 484.65          |
      | 137       | 18 May 2019      | 50.00                 |                     | 0.9263041037          | 46.32    | 2609.54 | 1.52                       |                          |                    | 484.65          |
      | 138       | 19 May 2019      | 50.00                 |                     | 0.9257750678          | 46.29    | 2561.03 | 1.49                       |                          |                    | 484.65          |
      | 139       | 20 May 2019      | 50.00                 |                     | 0.9252463339          | 46.26    | 2512.49 | 1.46                       |                          |                    | 484.65          |
      | 140       | 21 May 2019      | 50.00                 |                     | 0.9247179021          | 46.24    | 2463.93 | 1.44                       |                          |                    | 484.65          |
      | 141       | 22 May 2019      | 50.00                 |                     | 0.924189772           | 46.21    | 2415.34 | 1.41                       |                          |                    | 484.65          |
      | 142       | 23 May 2019      | 50.00                 |                     | 0.9236619436          | 46.18    | 2366.72 | 1.38                       |                          |                    | 484.65          |
      | 143       | 24 May 2019      | 50.00                 |                     | 0.9231344166          | 46.16    | 2318.07 | 1.35                       |                          |                    | 484.65          |
      | 144       | 25 May 2019      | 50.00                 |                     | 0.9226071909          | 46.13    | 2269.39 | 1.32                       |                          |                    | 484.65          |
      | 145       | 26 May 2019      | 50.00                 |                     | 0.9220802663          | 46.10    | 2220.69 | 1.30                       |                          |                    | 484.65          |
      | 146       | 27 May 2019      | 50.00                 |                     | 0.9215536427          | 46.08    | 2171.96 | 1.27                       |                          |                    | 484.65          |
      | 147       | 28 May 2019      | 50.00                 |                     | 0.9210273198          | 46.05    | 2123.20 | 1.24                       |                          |                    | 484.65          |
      | 148       | 29 May 2019      | 50.00                 |                     | 0.9205012975          | 46.03    | 2074.41 | 1.21                       |                          |                    | 484.65          |
      | 149       | 30 May 2019      | 50.00                 |                     | 0.9199755757          | 46.00    | 2025.60 | 1.19                       |                          |                    | 484.65          |
      | 150       | 31 May 2019      | 50.00                 |                     | 0.9194501541          | 45.97    | 1976.76 | 1.16                       |                          |                    | 484.65          |
      | 151       | 01 June 2019     | 50.00                 |                     | 0.9189250325          | 45.95    | 1927.89 | 1.13                       |                          |                    | 484.65          |
      | 152       | 02 June 2019     | 50.00                 |                     | 0.9184002109          | 45.92    | 1878.99 | 1.10                       |                          |                    | 484.65          |
      | 153       | 03 June 2019     | 50.00                 |                     | 0.9178756891          | 45.89    | 1830.06 | 1.07                       |                          |                    | 484.65          |
      | 154       | 04 June 2019     | 50.00                 |                     | 0.9173514668          | 45.87    | 1781.11 | 1.05                       |                          |                    | 484.65          |
      | 155       | 05 June 2019     | 50.00                 |                     | 0.9168275439          | 45.84    | 1732.13 | 1.02                       |                          |                    | 484.65          |
      | 156       | 06 June 2019     | 50.00                 |                     | 0.9163039202          | 45.82    | 1683.12 | 0.99                       |                          |                    | 484.65          |
      | 157       | 07 June 2019     | 50.00                 |                     | 0.9157805956          | 45.79    | 1634.08 | 0.96                       |                          |                    | 484.65          |
      | 158       | 08 June 2019     | 50.00                 |                     | 0.9152575698          | 45.76    | 1585.01 | 0.93                       |                          |                    | 484.65          |
      | 159       | 09 June 2019     | 50.00                 |                     | 0.9147348428          | 45.74    | 1535.92 | 0.91                       |                          |                    | 484.65          |
      | 160       | 10 June 2019     | 50.00                 |                     | 0.9142124143          | 45.71    | 1486.79 | 0.88                       |                          |                    | 484.65          |
      | 161       | 11 June 2019     | 50.00                 |                     | 0.9136902842          | 45.68    | 1437.64 | 0.85                       |                          |                    | 484.65          |
      | 162       | 12 June 2019     | 50.00                 |                     | 0.9131684523          | 45.66    | 1388.47 | 0.82                       |                          |                    | 484.65          |
      | 163       | 13 June 2019     | 50.00                 |                     | 0.9126469184          | 45.63    | 1339.26 | 0.79                       |                          |                    | 484.65          |
      | 164       | 14 June 2019     | 50.00                 |                     | 0.9121256824          | 45.61    | 1290.02 | 0.77                       |                          |                    | 484.65          |
      | 165       | 15 June 2019     | 50.00                 |                     | 0.9116047441          | 45.58    | 1240.76 | 0.74                       |                          |                    | 484.65          |
      | 166       | 16 June 2019     | 50.00                 |                     | 0.9110841033          | 45.55    | 1191.47 | 0.71                       |                          |                    | 484.65          |
      | 167       | 17 June 2019     | 50.00                 |                     | 0.9105637598          | 45.53    | 1142.15 | 0.68                       |                          |                    | 484.65          |
      | 168       | 18 June 2019     | 50.00                 |                     | 0.9100437136          | 45.50    | 1092.80 | 0.65                       |                          |                    | 484.65          |
      | 169       | 19 June 2019     | 50.00                 |                     | 0.9095239643          | 45.48    | 1043.43 | 0.62                       |                          |                    | 484.65          |
      | 170       | 20 June 2019     | 50.00                 |                     | 0.9090045119          | 45.45    | 994.02  | 0.60                       |                          |                    | 484.65          |
      | 171       | 21 June 2019     | 50.00                 |                     | 0.9084853562          | 45.42    | 944.59  | 0.57                       |                          |                    | 484.65          |
      | 172       | 22 June 2019     | 50.00                 |                     | 0.9079664969          | 45.40    | 895.13  | 0.54                       |                          |                    | 484.65          |
      | 173       | 23 June 2019     | 50.00                 |                     | 0.907447934           | 45.37    | 845.64  | 0.51                       |                          |                    | 484.65          |
      | 174       | 24 June 2019     | 50.00                 |                     | 0.9069296673          | 45.35    | 796.13  | 0.48                       |                          |                    | 484.65          |
      | 175       | 25 June 2019     | 50.00                 |                     | 0.9064116965          | 45.32    | 746.58  | 0.45                       |                          |                    | 484.65          |
      | 176       | 26 June 2019     | 50.00                 |                     | 0.9058940216          | 45.29    | 697.01  | 0.43                       |                          |                    | 484.65          |
      | 177       | 27 June 2019     | 50.00                 |                     | 0.9053766423          | 45.27    | 647.41  | 0.40                       |                          |                    | 484.65          |
      | 178       | 28 June 2019     | 50.00                 |                     | 0.9048595586          | 45.24    | 597.78  | 0.37                       |                          |                    | 484.65          |
      | 179       | 29 June 2019     | 50.00                 |                     | 0.9043427701          | 45.22    | 548.12  | 0.34                       |                          |                    | 484.65          |
      | 180       | 30 June 2019     | 50.00                 |                     | 0.9038262768          | 45.19    | 498.43  | 0.31                       |                          |                    | 484.65          |
      | 181       | 01 July 2019     | 50.00                 |                     | 0.9033100785          | 45.17    | 448.72  | 0.28                       |                          |                    | 484.65          |
      | 182       | 02 July 2019     | 50.00                 |                     | 0.902794175           | 45.14    | 398.97  | 0.26                       |                          |                    | 484.65          |
      | 183       | 03 July 2019     | 50.00                 |                     | 0.9022785661          | 45.11    | 349.20  | 0.23                       |                          |                    | 484.65          |
      | 184       | 04 July 2019     | 50.00                 |                     | 0.9017632517          | 45.09    | 299.40  | 0.20                       |                          |                    | 484.65          |
      | 185       | 05 July 2019     | 50.00                 |                     | 0.9012482317          | 45.06    | 249.57  | 0.17                       |                          |                    | 484.65          |
      | 186       | 06 July 2019     | 50.00                 |                     | 0.9007335057          | 45.04    | 199.71  | 0.14                       |                          |                    | 484.65          |
      | 187       | 07 July 2019     | 50.00                 |                     | 0.9002190738          | 45.01    | 149.83  | 0.11                       |                          |                    | 484.65          |
      | 188       | 08 July 2019     | 50.00                 |                     | 0.8997049356          | 44.99    | 99.91   | 0.09                       |                          |                    | 484.65          |
      | 189       | 09 July 2019     | 50.00                 |                     | 0.8991910911          | 44.96    | 49.97   | 0.06                       |                          |                    | 484.65          |
      | 190       | 10 July 2019     | 50.00                 |                     | 0.8986775401          | 44.93    | 0.00    | 0.03                       |                          |                    | 484.65          |
