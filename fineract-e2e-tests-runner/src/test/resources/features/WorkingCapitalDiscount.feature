@WorkingCapital
@WorkingCapitalDiscountFeature
Feature: Working Capital Discount

  @TestRailId:C72393
  Scenario: Discount on WCL account added after disbursement as on the same as disburse date processed successfully -  UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
  # --- add discount after disbursement on the same disbursement date --- #
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 112.0     | 100.0             | 100.0              | 1.0               | null             | null             | 12.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
    And WorkingCapitalLoanDiscountFeeTransactionBusinessEvent is raised with amount "12" on "01 January 2026" date

  @TestRailId:C72394
  Scenario: Discount update on WCL account on diff from disbursement date outcomes with an error - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- add discount after disbursement on diff from same disbursement date should outcome with an error --- #
    When Admin sets the business date to "08 January 2026"
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                    |
      | 400                | transaction.date.must.be.equal.disbursement.date |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C72395
  Scenario: Discount update on WCL account on the same as disburse date failed as already added discount before while create loan - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 15       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 15.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "18" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 15.0             | 14.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "15" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0              | 1.0               | 15.0             | 14.0             | 13.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    And WorkingCapitalLoanDiscountFeeTransactionBusinessEvent is raised with amount "13" on "01 January 2026" date

  @TestRailId:C72396
  Scenario: Discount update on WCL account on the same as disburse date failed as already added discount before while modify loan - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    |                   | 15.0     |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 15.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "18" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 15.0             | 14.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "15" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0              | 1.0               | 15.0             | 14.0             | 13.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C72397
  Scenario: Discount update on WCL account on the same as disburse date failed as already added discount before while approve loan - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 14.0             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "14" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 114.0     | 100.0             | 100.0              | 1.0               | null             | 14.0             | 14.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 14.0              | 14.0             | 0.0               | 0.0                   | false    |
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 14.0              | 14.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C72490
  Scenario: Discount update od WCL loan account on the same as disburse date failed as already added discount before while disburse loan - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0              | 1.0               | null             | null             | 13.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C74491
  Scenario: Discount is forbidden to be added after disbursement as discount is set on loan product level with WCLP overrides disallowed - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 50.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "20" discount amount results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 50.0             | 50.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "15" discount amount results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 150.0     | 100.0             | 100.0              | 1.0               | 50.0             | 50.0             | 50.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Adding Discount fee with "20" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 50.0              | 50.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C74492
  Scenario: Discount is forbidden to be added after disbursement as NO discount is set on loan product level with WCLP overrides disallowed - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "20" discount amount results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "20" discount amount results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Adding Discount fee with "20" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C74493
  Scenario: Discount is disallowed to be added after disbursement as discount is set and inherited from WC loan product level with WCLP overrides allowed - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "50" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 150.0     | 100.0             | 100.0              | 1.0               | null             | null             | 50.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100               | 100              | 0                 | 0                     | false    |
      | 01 January 2026 | Discount Fee | 50                | 50               | 0                 | 0                     | false    |
 # --- add discount after disbursement is forbidden as discount is already added --- #
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 50                | 50               | 0                 | 0                     | false    |

  @TestRailId:C72494
  Scenario: Discount is forbidden to be added after Modify with modified discount amount allowed as discount is set on loan product level with WCLP overrides allowed - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    |                   | 55.0     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 55.0             | null             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.product.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "50" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 150.0     | 100.0             | 100.0              | 1.0               | 55.0             | null             | 50.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 50.0              | 50.0             | 0                 | 0                     | false    |
# --- add discount after disbursement is forbidden as discount is already added and updated on disbursement --- #
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 50.0              | 50.0             | 0                 | 0                     | false    |

  @TestRailId:C78836
  Scenario: Discount is forbidden to be added after Approve with modified discount amount allowed as discount is set on loan product level with WCLP overrides allowed - UC11
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.product.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "40" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 40.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "50" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "40" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 140.0     | 100.0             | 100.0              | 1.0               | null             | 40.0             | 40.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 40.0              | 40.0             | 0                 | 0                     | false    |
  # --- add discount after disbursement is forbidden as discount is already added --- #
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 40.0              | 40.0             | 0                 | 0                     | false    |

  @TestRailId:C78837
  Scenario: Discount is forbidden to be added after Disburse with modified discount amount allowed as discount is set on loan product level with WCLP overrides allowed - UC12
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.product.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.product.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "45" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 145.0     | 100.0             | 100.0              | 1.0               | null             | null             | 45.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 45.0              | 45.0             | 0                 | 0                     | false    |
# --- add discount after disbursement is forbidden as discount is already added and updated on disbursement --- #
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 45.0              | 45.0             | 0                 | 0                     | false    |

  @TestRailId:C78838
  Scenario: Discount is allowed to be added after disbursement as overridden with NO discount value from defined on WCLP on loan Create with WCLP overrides allowed - UC13
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | 0.0              | null             | null     |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              | null             | null     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | 0.0              | null             | null     |
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
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 0.0              | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
      | 01 January 2026 | Discount Fee | 10.0              | 10.0             | 0.0               | 0.0                   | true     |

  @TestRailId:C78840
  Scenario: Working Capital Loan Transaction - Discount added on disbursement on disbursement undo - UC15
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 22.0     |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 22.0             | null             | null     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 22.0             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "11" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 111.0     | 100.0             | 100.0              | 1.0               | 22.0             | null             | 11.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 11.0              | 11.0             | 0.0               | 0.0                   | false    |
# --- undo working capital disbursal --- #
    And Admin successfully undo Working Capital disbursal by externalId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 22.0             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
      | 01 January 2026 | Discount Fee | 11.0              | 11.0             | 0.0               | 0.0                   | true     |

  @TestRailId:C78854
  Scenario: Discount on Working Capital Loan account added while create WCP, updated while approve loan and check after updo approval - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 18       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 18.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 18.0             | 17.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 18.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "19" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 18.0             | null             | null     |

  @TestRailId:C78855
  Scenario: Discount on Working Capital Loan account added while create WCP, updated while approve/disburse loan and check after undo approval/disbursal - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 18       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 18.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 18.0             | 17.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "19" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "16" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 116.0     | 100.0             | 100.0              | 1.0               | 18.0             | 17.0             | 16.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 18.0             | 17.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | 18.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "19" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |

  @TestRailId:C78856
  Scenario: Discount on Working Capital Loan account added while create WCP, updated while modify/approve loan and check after updo approval - UC1.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 18       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 18.0             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    |                   | 19.0     |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 19.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 19.0             | 17.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 19.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "20" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 19.0             | null             | null     |

  @TestRailId:C78857
  Scenario: Discount on Working Capital Loan account added while modify WCP, updated while approve/disburse loan and check after undo approval/disbursal - UC2.2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    |                   | 18.0     |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 18.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 18.0             | 17.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "19" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "16" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 116.0     | 100.0             | 100.0              | 1.0               | 18.0             | 17.0             | 16.0     |
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 18.0             | 17.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | 18.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "19" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |

  @TestRailId:C78858
  Scenario: Discount on Working Capital Loan account added while approve WCP and check after updo approval - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "15" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 15.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "19" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 19.0             | null     |

  @TestRailId:C78859
  Scenario: Discount on Working Capital Loan account added while approve, updated on disbursal and check after updo disbursal/approval with additional update while approve and check after undo disbursal - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 17.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "19" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "16" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 116.0     | 100.0             | 100.0              | 1.0               | null             | 17.0             | 16.0     |
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | 17.0             | null     |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "19" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 19.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "20" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "19" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 119.0     | 100.0             | 100.0              | 1.0               | null             | 19.0             | 19.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | 19.0             | null     |

  @TestRailId:C78860
  Scenario: Discount on Working Capital Loan account added while approve, updated on disbursal and check after updo disbursal/approval with additional update while modify and check after undo disbursal - UC4.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "17" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 17.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "19" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "16" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 116.0     | 100.0             | 100.0              | 1.0               | null             | 17.0             | 16.0     |
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | 17.0             | null     |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    |                   | 20.0     |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | 20.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "20" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 20.0             | 20.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "21" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "18" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 118.0     | 100.0             | 100.0              | 1.0               | 20.0             | 20.0             | 18.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 20.0             | 20.0             | null     |

  @TestRailId:C78861
  Scenario: Discount on Working Capital Loan account added while update discount and check after undo disbursal/approval - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "12" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 112.0     | 100.0             | 100.0              | 1.0               | null             | null             | 12.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | null             | null             | null     |

  @TestRailId:C78862
  Scenario: Discount on Working Capital Loan account added while create loan, updated on disbursal and check after undo disbursal with additional disburse - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 18       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 18.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "18" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 18.0             | 18.0             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | 18.0             | 18.0             | null     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 18.0             | 18.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "19" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "17" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 117.0     | 100.0             | 100.0              | 1.0               | 18.0             | 18.0             | 17.0     |

  @TestRailId:C78863
  Scenario: Discount on Working Capital Loan account with NO discount is set on loan product level with WCLP overrides disallowed after undo approval/disbursal - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "20" discount amount results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "20" discount amount results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | null             | null             | null     |

  @TestRailId:C78864
  Scenario: Discount on Working Capital Loan account added after disbursement with discount set on loan product level and WCLP overrides allowed after undo approval/disbursal - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- add discount with exceed discount amount is forbidden as loan discount is null but It exceeds product discount value --- #
    Then Adding Discount fee with "60" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.product.discount |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "12" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 112.0     | 100.0             | 100.0              | 1.0               | null             | null             | 12.0     |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | null             | null             | null     |

  @TestRailId:C78865
  Scenario: Discount on Working Capital Loan account added on create loan account with modified discount amount while discount set on loan product level and WCLP overrides allowed after undo disbursal/approve - UC9.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 48.0     |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 48.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "40" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 48.0             | 40.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "50" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "40" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 140.0     | 100.0             | 100.0              | 1.0               | 48.0             | 40.0             | 40.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 48.0             | 40.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | 48.0             | null             | null     |
# --- modify working capital loan account --- #
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    |                   | 49.0     |
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | 49.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 49.0             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | 49.0             | null             | null     |

  @TestRailId:C78866
  Scenario: Discount on Working Capital Loan account added on modify loan account with modified discount amount with discount set on loan product level and WCLP overrides allowed after undo disbursal/approve - UC9.2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    |                   | 30.0     |
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 30.0             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.created.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "20" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 30.0             | 20.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "50" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "20" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 120.0     | 100.0             | 100.0              | 1.0               | 30.0             | 20.0             | 20.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 30.0             | 20.0             | null     |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | 30.0             | null             | null     |

  @TestRailId:C78867
  Scenario: Discount on Working Capital Loan account added on Approve with modified discount amount with discount set on loan product level and WCLP overrides allowed after undo disbursal/approve - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.product.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "40" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 40.0             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "50" discount amount results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | amount.cannot.exceed.approved.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "40" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 140.0     | 100.0             | 100.0              | 1.0               | null             | 40.0             | 40.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | 40.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | null             | null             | null     |

  @TestRailId:C78868
  Scenario: Discount on Working Capital Loan account added on Disburse with modified discount amount while discount is set on loan product level with WCLP overrides allowed after undo disbursal/approval - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Approving the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.product.discount |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Disbursing the working capital loan on "01 January 2026" with "100" amount and "60" discount amount results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | amount.cannot.exceed.product.discount |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "45" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 145.0     | 100.0             | 100.0              | 1.0               | null             | null             | 45.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | Discount was already set before disbursement |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | null             | null             | null     |

  @TestRailId:C78869
  Scenario: Discount on Working Capital Loan account added after disbursement while discount is set on loan product level with WCLP overrides disallowed after undo disbursal/approval - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 50.0             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 50.0             | 50.0             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 150.0     | 100.0             | 100.0              | 1.0               | 50.0             | 50.0             | 50.0     |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Adding Discount fee with "20" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | 50.0             | 50.0             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | 50.0             | null             | null     |

  @TestRailId:C85503
  Scenario: Discount on Working Capital Loan account added after disbursement while discount is NOT set on loan product level with WCLP overrides disallowed after undo disbursal/approval - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 0.0       | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 100.0             | 100.0              | 1.0               | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Adding Discount fee with "20" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | override.not.allowed.by.product |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | null             | null     |
# --- undo working capital approval --- #
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0              | 1.0               | null             | null             | null     |

  @TestRailId:C74518
  Scenario: Verify that undo disbursal of WCL account set discount to null - UC5.2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null             | null             | null     |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null             | 14.0             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0              | 1.0               | null             | 14.0             | 13.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
# --- undo working capital disbursal --- #
    Then Admin successfully undo Working Capital disbursal
    Then Working Capital loan status will be "APPROVED"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0              | 1.0               | null             | 14.0             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | true     |

  @TestRailId:C83039
  Scenario: Discount fee added via DISCOUNTFEE without externalId gets an auto-generated externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
    Then In Working Capital Loan Transactions all transactions have non-blank external-id
    Then All active Discount Fee transactions have an auto-generated externalId

  @TestRailId:C83040
  Scenario: Discount fee added after disbursement with user-generated externalId is persisted as-is
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    Then Admin adds Discount fee with "12" amount and a random externalId on Working Capital loan account for last disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
    Then In Working Capital Loan Transactions all transactions have non-blank external-id
    Then Active Discount Fee transactions contain the user-generated externalId from DISCOUNTFEE

  @TestRailId:C83041
  Scenario: Discount provided during disbursement without externalId gets an auto-generated externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    Then In Working Capital Loan Transactions all transactions have non-blank external-id
    Then All active Discount Fee transactions have an auto-generated externalId

  @TestRailId:C83042
  Scenario: Discount provided during disbursement with user-generated discountExternalId is persisted as-is
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount and a random discountExternalId
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 13.0              | 13.0             | 0.0               | 0.0                   | false    |
    Then In Working Capital Loan Transactions all transactions have non-blank external-id
    Then Active Discount Fee transactions contain the user-generated discountExternalId from disburse

  @TestRailId:C83043
  Scenario: Reusing the same discountExternalId across two working capital loans on disburse is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount and a random discountExternalId
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    When Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Initiating disbursement on "01 January 2026" with "100" EUR transaction amount and "13" discount amount reusing the previously shared discountExternalId on Working Capital loan results an error with the following data:
      | HTTP response code | Error message  |
      | 400                | already.exists |

  @TestRailId:C83044
  Scenario: Providing discountExternalId on disburse without discountAmount is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Initiating disbursement on "01 January 2026" with "100" EUR transaction amount and discountExternalId "wcl-disburse-discount-no-amount-001" without discountAmount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | not.allowed.without.positive.discount |

  @TestRailId:C83045
  Scenario: Providing discountExternalId on disburse with zero discountAmount is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Initiating disbursement on "01 January 2026" with "100" EUR transaction amount and "0" discount amount and discountExternalId "wcl-disburse-discount-zero-amount-001" on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | not.allowed.without.positive.discount |

  @TestRailId:C83046
  Scenario: Providing the same externalId for both disburse and discountExternalId is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Initiating disbursement on "01 January 2026" with "100" EUR transaction amount and "13" discount amount using "wcl-disburse-shared-ext-id-001" for both externalId and discountExternalId on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                             |
      | 400                | must.differ.from.disbursement.external.id |

  @TestRailId:C83047
  Scenario: Reusing the same externalId on DISCOUNTFEE across two working capital loans is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    Then Admin adds Discount fee with "12" amount and a random externalId on Working Capital loan account for last disbursement
    When Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    Then Adding Discount fee with "12" amount reusing the previously shared externalId on Working Capital loan account for last disbursement results an error with the following data:
      | HTTP response code | Error message  |
      | 400                | already.exists |
