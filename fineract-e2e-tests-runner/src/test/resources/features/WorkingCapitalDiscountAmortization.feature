@WorkingCapital
@WorkingCapitalDiscountFeeAmortizationFeature
Feature: WorkingCapitalDiscountFeeAmortization

  @TestRailId:C80968
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB by repayment - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0     | 18.0              | null             | null             | 1000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
# -- make repayment on Jan, 5, 2026 --- #
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 150 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 100000.0           | 28.7           | 971.3            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 28.7   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 28.7  |        |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |

  @TestRailId:C80969
  Scenario: Verify NO Discount Fee Amortization transaction on Working Capital Loan account triggers on COB without repayment - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | null             | null             | 1000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 0.0                | 100000.0           | 0.0            | 1000.0           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C80970
  Scenario: Verify NO Discount Fee Amortization transaction on Working Capital Loan account triggers on COB if NO discount added - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
# -- make repayment on Jan, 5, 2026 --- #
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 150 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 150.0              | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 150.0             | 150.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C80971
  Scenario: Verify NO duplicated Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run again without new repayments - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | null             | null             | 1000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
# -- make repayment on Jan, 5, 2026 --- #
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 150 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 100000.0           | 28.7           | 971.3            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
# --- 2nd run WC COB shouldn't generate any more/new 'Discount Fee Amortization' transaction
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 100000.0           | 28.7           | 971.3            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 28.7   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 28.7  |        |

  @TestRailId:C80972
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run by each repayment  - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | null             | null             | 1000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
# -- make repayment on Jan, 5, 2026 --- #
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 100000.0           | 9.61           | 990.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
# -- make repayment on Jan, 10, 2026 --- #
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "10 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 10 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "11 January 2026"
    When Admin runs inline COB job for Working Capital Loan
# --- realized income should be equal to discount amount --- #
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 100.0              | 100000.0           | 19.18          | 980.82           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 10 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 9.57              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.57   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.57  |        |

  @TestRailId:C80973
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run by repayment at next day transaction date - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | null             | null             | 1000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
# -- make repayment on Jan, 1, 2026 --- #
    And Customer makes repayment on "01 January 2026" with 150 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment    | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 100000.0           | 28.7           | 971.3            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 28.7   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 28.7  |        |

  @TestRailId:C8974
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run by repayment with amount less then discount amount - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | null             | null             | 1000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
# -- make repayment on Jan, 3, 2026 --- #
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment    | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 100000.0           | 9.61           | 990.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |

  @TestRailId:C80975
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run by a few repayments at the same date - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null             | null             | null     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | null             | null             | 1000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
# -- make repayment on Jan, 3, 2026 --- #
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment    | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
# -- make one more repayment on Jan, 3, 2026 --- #
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment    | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment    | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 100.0              | 100000.0           | 19.18          | 980.82           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 19.18             |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 19.18  |
      | LIABILITY | 240005       | Deferred Interest Revenue | 19.18 |        |

  @TestRailId:C85160
  Scenario: Verify Discount Fee Amortization when repayment is made on the disbursement date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Customer makes repayment on "01 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment    | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         | 9000.00       |                            |                     |                          | 1000.00                    | 1000.00                  |
      | 1         | 2026-01-01 | 50.00                 | 8959.61         | 8959.61       | 9.61                       | 50.00               | 9.61                     | 990.39                     | 990.39                   |
      | 2         | 2026-01-02 | 50.00                 | 8919.18         |               | 9.57                       |                     |                          | 980.82                     |                          |
      | 3         | 2026-01-03 | 50.00                 | 8878.70         |               | 9.52                       |                     |                          | 971.30                     |                          |
      | 4         | 2026-01-04 | 50.00                 | 8838.18         |               | 9.48                       |                     |                          | 961.82                     |                          |
      | 5         | 2026-01-05 | 50.00                 | 8797.62         |               | 9.44                       |                     |                          | 952.38                     |                          |
      | 6         | 2026-01-06 | 50.00                 | 8757.01         |               | 9.39                       |                     |                          | 942.99                     |                          |
      | 7         | 2026-01-07 | 50.00                 | 8716.36         |               | 9.35                       |                     |                          | 933.64                     |                          |
      | 8         | 2026-01-08 | 50.00                 | 8675.67         |               | 9.31                       |                     |                          | 924.33                     |                          |
      | 9         | 2026-01-09 | 50.00                 | 8634.94         |               | 9.27                       |                     |                          | 915.06                     |                          |
      | 10        | 2026-01-10 | 50.00                 | 8594.16         |               | 9.22                       |                     |                          | 905.84                     |                          |
      | 11        | 2026-01-11 | 50.00                 | 8553.33         |               | 9.17                       |                     |                          | 896.67                     |                          |
      | 12        | 2026-01-12 | 50.00                 | 8512.47         |               | 9.14                       |                     |                          | 887.53                     |                          |
      | 13        | 2026-01-13 | 50.00                 | 8471.56         |               | 9.09                       |                     |                          | 878.44                     |                          |
      | 14        | 2026-01-14 | 50.00                 | 8430.60         |               | 9.04                       |                     |                          | 869.40                     |                          |
      | 15        | 2026-01-15 | 50.00                 | 8389.61         |               | 9.01                       |                     |                          | 860.39                     |                          |
      | 16        | 2026-01-16 | 50.00                 | 8348.56         |               | 8.95                       |                     |                          | 851.44                     |                          |
      | 17        | 2026-01-17 | 50.00                 | 8307.48         |               | 8.92                       |                     |                          | 842.52                     |                          |
      | 18        | 2026-01-18 | 50.00                 | 8266.35         |               | 8.87                       |                     |                          | 833.65                     |                          |
      | 19        | 2026-01-19 | 50.00                 | 8225.18         |               | 8.83                       |                     |                          | 824.82                     |                          |
      | 20        | 2026-01-20 | 50.00                 | 8183.96         |               | 8.78                       |                     |                          | 816.04                     |                          |
      | 21        | 2026-01-21 | 50.00                 | 8142.70         |               | 8.74                       |                     |                          | 807.30                     |                          |
      | 22        | 2026-01-22 | 50.00                 | 8101.39         |               | 8.69                       |                     |                          | 798.61                     |                          |
      | 23        | 2026-01-23 | 50.00                 | 8060.04         |               | 8.65                       |                     |                          | 789.96                     |                          |
      | 24        | 2026-01-24 | 50.00                 | 8018.65         |               | 8.61                       |                     |                          | 781.35                     |                          |
      | 25        | 2026-01-25 | 50.00                 | 7977.21         |               | 8.56                       |                     |                          | 772.79                     |                          |
      | 26        | 2026-01-26 | 50.00                 | 7935.73         |               | 8.52                       |                     |                          | 764.27                     |                          |
      | 27        | 2026-01-27 | 50.00                 | 7894.21         |               | 8.48                       |                     |                          | 755.79                     |                          |
      | 28        | 2026-01-28 | 50.00                 | 7852.63         |               | 8.42                       |                     |                          | 747.37                     |                          |
      | 29        | 2026-01-29 | 50.00                 | 7811.02         |               | 8.39                       |                     |                          | 738.98                     |                          |
      | 30        | 2026-01-30 | 50.00                 | 7769.36         |               | 8.34                       |                     |                          | 730.64                     |                          |
      | 31        | 2026-01-31 | 50.00                 | 7727.66         |               | 8.30                       |                     |                          | 722.34                     |                          |
      | 32        | 2026-02-01 | 50.00                 | 7685.91         |               | 8.25                       |                     |                          | 714.09                     |                          |
      | 33        | 2026-02-02 | 50.00                 | 7644.12         |               | 8.21                       |                     |                          | 705.88                     |                          |
      | 34        | 2026-02-03 | 50.00                 | 7602.28         |               | 8.16                       |                     |                          | 697.72                     |                          |
      | 35        | 2026-02-04 | 50.00                 | 7560.40         |               | 8.12                       |                     |                          | 689.60                     |                          |
      | 36        | 2026-02-05 | 50.00                 | 7518.47         |               | 8.07                       |                     |                          | 681.53                     |                          |
      | 37        | 2026-02-06 | 50.00                 | 7476.50         |               | 8.03                       |                     |                          | 673.50                     |                          |
      | 38        | 2026-02-07 | 50.00                 | 7434.48         |               | 7.98                       |                     |                          | 665.52                     |                          |
      | 39        | 2026-02-08 | 50.00                 | 7392.42         |               | 7.94                       |                     |                          | 657.58                     |                          |
      | 40        | 2026-02-09 | 50.00                 | 7350.31         |               | 7.89                       |                     |                          | 649.69                     |                          |
      | 41        | 2026-02-10 | 50.00                 | 7308.16         |               | 7.85                       |                     |                          | 641.84                     |                          |
      | 42        | 2026-02-11 | 50.00                 | 7265.97         |               | 7.81                       |                     |                          | 634.03                     |                          |
      | 43        | 2026-02-12 | 50.00                 | 7223.72         |               | 7.75                       |                     |                          | 626.28                     |                          |
      | 44        | 2026-02-13 | 50.00                 | 7181.44         |               | 7.72                       |                     |                          | 618.56                     |                          |
      | 45        | 2026-02-14 | 50.00                 | 7139.11         |               | 7.67                       |                     |                          | 610.89                     |                          |
      | 46        | 2026-02-15 | 50.00                 | 7096.73         |               | 7.62                       |                     |                          | 603.27                     |                          |
      | 47        | 2026-02-16 | 50.00                 | 7054.31         |               | 7.58                       |                     |                          | 595.69                     |                          |
      | 48        | 2026-02-17 | 50.00                 | 7011.84         |               | 7.53                       |                     |                          | 588.16                     |                          |
      | 49        | 2026-02-18 | 50.00                 | 6969.33         |               | 7.49                       |                     |                          | 580.67                     |                          |
      | 50        | 2026-02-19 | 50.00                 | 6926.77         |               | 7.44                       |                     |                          | 573.23                     |                          |
      | 51        | 2026-02-20 | 50.00                 | 6884.17         |               | 7.40                       |                     |                          | 565.83                     |                          |
      | 52        | 2026-02-21 | 50.00                 | 6841.52         |               | 7.35                       |                     |                          | 558.48                     |                          |
      | 53        | 2026-02-22 | 50.00                 | 6798.82         |               | 7.30                       |                     |                          | 551.18                     |                          |
      | 54        | 2026-02-23 | 50.00                 | 6756.08         |               | 7.26                       |                     |                          | 543.92                     |                          |
      | 55        | 2026-02-24 | 50.00                 | 6713.30         |               | 7.22                       |                     |                          | 536.70                     |                          |
      | 56        | 2026-02-25 | 50.00                 | 6670.47         |               | 7.17                       |                     |                          | 529.53                     |                          |
      | 57        | 2026-02-26 | 50.00                 | 6627.59         |               | 7.12                       |                     |                          | 522.41                     |                          |
      | 58        | 2026-02-27 | 50.00                 | 6584.67         |               | 7.08                       |                     |                          | 515.33                     |                          |
      | 59        | 2026-02-28 | 50.00                 | 6541.70         |               | 7.03                       |                     |                          | 508.30                     |                          |
      | 60        | 2026-03-01 | 50.00                 | 6498.68         |               | 6.98                       |                     |                          | 501.32                     |                          |
      | 61        | 2026-03-02 | 50.00                 | 6455.62         |               | 6.94                       |                     |                          | 494.38                     |                          |
      | 62        | 2026-03-03 | 50.00                 | 6412.51         |               | 6.89                       |                     |                          | 487.49                     |                          |
      | 63        | 2026-03-04 | 50.00                 | 6369.36         |               | 6.85                       |                     |                          | 480.64                     |                          |
      | 64        | 2026-03-05 | 50.00                 | 6326.16         |               | 6.80                       |                     |                          | 473.84                     |                          |
      | 65        | 2026-03-06 | 50.00                 | 6282.92         |               | 6.76                       |                     |                          | 467.08                     |                          |
      | 66        | 2026-03-07 | 50.00                 | 6239.63         |               | 6.71                       |                     |                          | 460.37                     |                          |
      | 67        | 2026-03-08 | 50.00                 | 6196.29         |               | 6.66                       |                     |                          | 453.71                     |                          |
      | 68        | 2026-03-09 | 50.00                 | 6152.91         |               | 6.62                       |                     |                          | 447.09                     |                          |
      | 69        | 2026-03-10 | 50.00                 | 6109.48         |               | 6.57                       |                     |                          | 440.52                     |                          |
      | 70        | 2026-03-11 | 50.00                 | 6066.00         |               | 6.52                       |                     |                          | 434.00                     |                          |
      | 71        | 2026-03-12 | 50.00                 | 6022.48         |               | 6.48                       |                     |                          | 427.52                     |                          |
      | 72        | 2026-03-13 | 50.00                 | 5978.91         |               | 6.43                       |                     |                          | 421.09                     |                          |
      | 73        | 2026-03-14 | 50.00                 | 5935.29         |               | 6.38                       |                     |                          | 414.71                     |                          |
      | 74        | 2026-03-15 | 50.00                 | 5891.63         |               | 6.34                       |                     |                          | 408.37                     |                          |
      | 75        | 2026-03-16 | 50.00                 | 5847.92         |               | 6.29                       |                     |                          | 402.08                     |                          |
      | 76        | 2026-03-17 | 50.00                 | 5804.17         |               | 6.25                       |                     |                          | 395.83                     |                          |
      | 77        | 2026-03-18 | 50.00                 | 5760.36         |               | 6.19                       |                     |                          | 389.64                     |                          |
      | 78        | 2026-03-19 | 50.00                 | 5716.52         |               | 6.16                       |                     |                          | 383.48                     |                          |
      | 79        | 2026-03-20 | 50.00                 | 5672.62         |               | 6.10                       |                     |                          | 377.38                     |                          |
      | 80        | 2026-03-21 | 50.00                 | 5628.68         |               | 6.06                       |                     |                          | 371.32                     |                          |
      | 81        | 2026-03-22 | 50.00                 | 5584.69         |               | 6.01                       |                     |                          | 365.31                     |                          |
      | 82        | 2026-03-23 | 50.00                 | 5540.65         |               | 5.96                       |                     |                          | 359.35                     |                          |
      | 83        | 2026-03-24 | 50.00                 | 5496.57         |               | 5.92                       |                     |                          | 353.43                     |                          |
      | 84        | 2026-03-25 | 50.00                 | 5452.44         |               | 5.87                       |                     |                          | 347.56                     |                          |
      | 85        | 2026-03-26 | 50.00                 | 5408.26         |               | 5.82                       |                     |                          | 341.74                     |                          |
      | 86        | 2026-03-27 | 50.00                 | 5364.03         |               | 5.77                       |                     |                          | 335.97                     |                          |
      | 87        | 2026-03-28 | 50.00                 | 5319.76         |               | 5.73                       |                     |                          | 330.24                     |                          |
      | 88        | 2026-03-29 | 50.00                 | 5275.44         |               | 5.68                       |                     |                          | 324.56                     |                          |
      | 89        | 2026-03-30 | 50.00                 | 5231.08         |               | 5.64                       |                     |                          | 318.92                     |                          |
      | 90        | 2026-03-31 | 50.00                 | 5186.66         |               | 5.58                       |                     |                          | 313.34                     |                          |
      | 91        | 2026-04-01 | 50.00                 | 5142.20         |               | 5.54                       |                     |                          | 307.80                     |                          |
      | 92        | 2026-04-02 | 50.00                 | 5097.69         |               | 5.49                       |                     |                          | 302.31                     |                          |
      | 93        | 2026-04-03 | 50.00                 | 5053.13         |               | 5.44                       |                     |                          | 296.87                     |                          |
      | 94        | 2026-04-04 | 50.00                 | 5008.53         |               | 5.40                       |                     |                          | 291.47                     |                          |
      | 95        | 2026-04-05 | 50.00                 | 4963.88         |               | 5.35                       |                     |                          | 286.12                     |                          |
      | 96        | 2026-04-06 | 50.00                 | 4919.18         |               | 5.30                       |                     |                          | 280.82                     |                          |
      | 97        | 2026-04-07 | 50.00                 | 4874.43         |               | 5.25                       |                     |                          | 275.57                     |                          |
      | 98        | 2026-04-08 | 50.00                 | 4829.64         |               | 5.21                       |                     |                          | 270.36                     |                          |
      | 99        | 2026-04-09 | 50.00                 | 4784.79         |               | 5.15                       |                     |                          | 265.21                     |                          |
      | 100       | 2026-04-10 | 50.00                 | 4739.90         |               | 5.11                       |                     |                          | 260.10                     |                          |
      | 101       | 2026-04-11 | 50.00                 | 4694.96         |               | 5.06                       |                     |                          | 255.04                     |                          |
      | 102       | 2026-04-12 | 50.00                 | 4649.98         |               | 5.02                       |                     |                          | 250.02                     |                          |
      | 103       | 2026-04-13 | 50.00                 | 4604.94         |               | 4.96                       |                     |                          | 245.06                     |                          |
      | 104       | 2026-04-14 | 50.00                 | 4559.86         |               | 4.92                       |                     |                          | 240.14                     |                          |
      | 105       | 2026-04-15 | 50.00                 | 4514.73         |               | 4.87                       |                     |                          | 235.27                     |                          |
      | 106       | 2026-04-16 | 50.00                 | 4469.55         |               | 4.82                       |                     |                          | 230.45                     |                          |
      | 107       | 2026-04-17 | 50.00                 | 4424.32         |               | 4.77                       |                     |                          | 225.68                     |                          |
      | 108       | 2026-04-18 | 50.00                 | 4379.05         |               | 4.73                       |                     |                          | 220.95                     |                          |
      | 109       | 2026-04-19 | 50.00                 | 4333.72         |               | 4.67                       |                     |                          | 216.28                     |                          |
      | 110       | 2026-04-20 | 50.00                 | 4288.35         |               | 4.63                       |                     |                          | 211.65                     |                          |
      | 111       | 2026-04-21 | 50.00                 | 4242.93         |               | 4.58                       |                     |                          | 207.07                     |                          |
      | 112       | 2026-04-22 | 50.00                 | 4197.46         |               | 4.53                       |                     |                          | 202.54                     |                          |
      | 113       | 2026-04-23 | 50.00                 | 4151.94         |               | 4.48                       |                     |                          | 198.06                     |                          |
      | 114       | 2026-04-24 | 50.00                 | 4106.38         |               | 4.44                       |                     |                          | 193.62                     |                          |
      | 115       | 2026-04-25 | 50.00                 | 4060.76         |               | 4.38                       |                     |                          | 189.24                     |                          |
      | 116       | 2026-04-26 | 50.00                 | 4015.10         |               | 4.34                       |                     |                          | 184.90                     |                          |
      | 117       | 2026-04-27 | 50.00                 | 3969.38         |               | 4.28                       |                     |                          | 180.62                     |                          |
      | 118       | 2026-04-28 | 50.00                 | 3923.62         |               | 4.24                       |                     |                          | 176.38                     |                          |
      | 119       | 2026-04-29 | 50.00                 | 3877.81         |               | 4.19                       |                     |                          | 172.19                     |                          |
      | 120       | 2026-04-30 | 50.00                 | 3831.95         |               | 4.14                       |                     |                          | 168.05                     |                          |
      | 121       | 2026-05-01 | 50.00                 | 3786.04         |               | 4.09                       |                     |                          | 163.96                     |                          |
      | 122       | 2026-05-02 | 50.00                 | 3740.09         |               | 4.05                       |                     |                          | 159.91                     |                          |
      | 123       | 2026-05-03 | 50.00                 | 3694.08         |               | 3.99                       |                     |                          | 155.92                     |                          |
      | 124       | 2026-05-04 | 50.00                 | 3648.03         |               | 3.95                       |                     |                          | 151.97                     |                          |
      | 125       | 2026-05-05 | 50.00                 | 3601.92         |               | 3.89                       |                     |                          | 148.08                     |                          |
      | 126       | 2026-05-06 | 50.00                 | 3555.77         |               | 3.85                       |                     |                          | 144.23                     |                          |
      | 127       | 2026-05-07 | 50.00                 | 3509.56         |               | 3.79                       |                     |                          | 140.44                     |                          |
      | 128       | 2026-05-08 | 50.00                 | 3463.31         |               | 3.75                       |                     |                          | 136.69                     |                          |
      | 129       | 2026-05-09 | 50.00                 | 3417.01         |               | 3.70                       |                     |                          | 132.99                     |                          |
      | 130       | 2026-05-10 | 50.00                 | 3370.66         |               | 3.65                       |                     |                          | 129.34                     |                          |
      | 131       | 2026-05-11 | 50.00                 | 3324.26         |               | 3.60                       |                     |                          | 125.74                     |                          |
      | 132       | 2026-05-12 | 50.00                 | 3277.81         |               | 3.55                       |                     |                          | 122.19                     |                          |
      | 133       | 2026-05-13 | 50.00                 | 3231.31         |               | 3.50                       |                     |                          | 118.69                     |                          |
      | 134       | 2026-05-14 | 50.00                 | 3184.76         |               | 3.45                       |                     |                          | 115.24                     |                          |
      | 135       | 2026-05-15 | 50.00                 | 3138.16         |               | 3.40                       |                     |                          | 111.84                     |                          |
      | 136       | 2026-05-16 | 50.00                 | 3091.51         |               | 3.35                       |                     |                          | 108.49                     |                          |
      | 137       | 2026-05-17 | 50.00                 | 3044.81         |               | 3.30                       |                     |                          | 105.19                     |                          |
      | 138       | 2026-05-18 | 50.00                 | 2998.06         |               | 3.25                       |                     |                          | 101.94                     |                          |
      | 139       | 2026-05-19 | 50.00                 | 2951.26         |               | 3.20                       |                     |                          | 98.74                      |                          |
      | 140       | 2026-05-20 | 50.00                 | 2904.42         |               | 3.16                       |                     |                          | 95.58                      |                          |
      | 141       | 2026-05-21 | 50.00                 | 2857.52         |               | 3.10                       |                     |                          | 92.48                      |                          |
      | 142       | 2026-05-22 | 50.00                 | 2810.57         |               | 3.05                       |                     |                          | 89.43                      |                          |
      | 143       | 2026-05-23 | 50.00                 | 2763.57         |               | 3.00                       |                     |                          | 86.43                      |                          |
      | 144       | 2026-05-24 | 50.00                 | 2716.52         |               | 2.95                       |                     |                          | 83.48                      |                          |
      | 145       | 2026-05-25 | 50.00                 | 2669.42         |               | 2.90                       |                     |                          | 80.58                      |                          |
      | 146       | 2026-05-26 | 50.00                 | 2622.27         |               | 2.85                       |                     |                          | 77.73                      |                          |
      | 147       | 2026-05-27 | 50.00                 | 2575.07         |               | 2.80                       |                     |                          | 74.93                      |                          |
      | 148       | 2026-05-28 | 50.00                 | 2527.82         |               | 2.75                       |                     |                          | 72.18                      |                          |
      | 149       | 2026-05-29 | 50.00                 | 2480.52         |               | 2.70                       |                     |                          | 69.48                      |                          |
      | 150       | 2026-05-30 | 50.00                 | 2433.17         |               | 2.65                       |                     |                          | 66.83                      |                          |
      | 151       | 2026-05-31 | 50.00                 | 2385.77         |               | 2.60                       |                     |                          | 64.23                      |                          |
      | 152       | 2026-06-01 | 50.00                 | 2338.31         |               | 2.54                       |                     |                          | 61.69                      |                          |
      | 153       | 2026-06-02 | 50.00                 | 2290.81         |               | 2.50                       |                     |                          | 59.19                      |                          |
      | 154       | 2026-06-03 | 50.00                 | 2243.26         |               | 2.45                       |                     |                          | 56.74                      |                          |
      | 155       | 2026-06-04 | 50.00                 | 2195.65         |               | 2.39                       |                     |                          | 54.35                      |                          |
      | 156       | 2026-06-05 | 50.00                 | 2148.00         |               | 2.35                       |                     |                          | 52.00                      |                          |
      | 157       | 2026-06-06 | 50.00                 | 2100.29         |               | 2.29                       |                     |                          | 49.71                      |                          |
      | 158       | 2026-06-07 | 50.00                 | 2052.53         |               | 2.24                       |                     |                          | 47.47                      |                          |
      | 159       | 2026-06-08 | 50.00                 | 2004.73         |               | 2.20                       |                     |                          | 45.27                      |                          |
      | 160       | 2026-06-09 | 50.00                 | 1956.87         |               | 2.14                       |                     |                          | 43.13                      |                          |
      | 161       | 2026-06-10 | 50.00                 | 1908.96         |               | 2.09                       |                     |                          | 41.04                      |                          |
      | 162       | 2026-06-11 | 50.00                 | 1860.99         |               | 2.03                       |                     |                          | 39.01                      |                          |
      | 163       | 2026-06-12 | 50.00                 | 1812.98         |               | 1.99                       |                     |                          | 37.02                      |                          |
      | 164       | 2026-06-13 | 50.00                 | 1764.92         |               | 1.94                       |                     |                          | 35.08                      |                          |
      | 165       | 2026-06-14 | 50.00                 | 1716.80         |               | 1.88                       |                     |                          | 33.20                      |                          |
      | 166       | 2026-06-15 | 50.00                 | 1668.64         |               | 1.84                       |                     |                          | 31.36                      |                          |
      | 167       | 2026-06-16 | 50.00                 | 1620.42         |               | 1.78                       |                     |                          | 29.58                      |                          |
      | 168       | 2026-06-17 | 50.00                 | 1572.15         |               | 1.73                       |                     |                          | 27.85                      |                          |
      | 169       | 2026-06-18 | 50.00                 | 1523.83         |               | 1.68                       |                     |                          | 26.17                      |                          |
      | 170       | 2026-06-19 | 50.00                 | 1475.45         |               | 1.62                       |                     |                          | 24.55                      |                          |
      | 171       | 2026-06-20 | 50.00                 | 1427.03         |               | 1.58                       |                     |                          | 22.97                      |                          |
      | 172       | 2026-06-21 | 50.00                 | 1378.55         |               | 1.52                       |                     |                          | 21.45                      |                          |
      | 173       | 2026-06-22 | 50.00                 | 1330.02         |               | 1.47                       |                     |                          | 19.98                      |                          |
      | 174       | 2026-06-23 | 50.00                 | 1281.45         |               | 1.43                       |                     |                          | 18.55                      |                          |
      | 175       | 2026-06-24 | 50.00                 | 1232.81         |               | 1.36                       |                     |                          | 17.19                      |                          |
      | 176       | 2026-06-25 | 50.00                 | 1184.13         |               | 1.32                       |                     |                          | 15.87                      |                          |
      | 177       | 2026-06-26 | 50.00                 | 1135.39         |               | 1.26                       |                     |                          | 14.61                      |                          |
      | 178       | 2026-06-27 | 50.00                 | 1086.61         |               | 1.22                       |                     |                          | 13.39                      |                          |
      | 179       | 2026-06-28 | 50.00                 | 1037.77         |               | 1.16                       |                     |                          | 12.23                      |                          |
      | 180       | 2026-06-29 | 50.00                 | 988.88          |               | 1.11                       |                     |                          | 11.12                      |                          |
      | 181       | 2026-06-30 | 50.00                 | 939.93          |               | 1.05                       |                     |                          | 10.07                      |                          |
      | 182       | 2026-07-01 | 50.00                 | 890.93          |               | 1.00                       |                     |                          | 9.07                       |                          |
      | 183       | 2026-07-02 | 50.00                 | 841.89          |               | 0.96                       |                     |                          | 8.11                       |                          |
      | 184       | 2026-07-03 | 50.00                 | 792.79          |               | 0.90                       |                     |                          | 7.21                       |                          |
      | 185       | 2026-07-04 | 50.00                 | 743.63          |               | 0.84                       |                     |                          | 6.37                       |                          |
      | 186       | 2026-07-05 | 50.00                 | 694.43          |               | 0.80                       |                     |                          | 5.57                       |                          |
      | 187       | 2026-07-06 | 50.00                 | 645.17          |               | 0.74                       |                     |                          | 4.83                       |                          |
      | 188       | 2026-07-07 | 50.00                 | 595.86          |               | 0.69                       |                     |                          | 4.14                       |                          |
      | 189       | 2026-07-08 | 50.00                 | 546.49          |               | 0.63                       |                     |                          | 3.51                       |                          |
      | 190       | 2026-07-09 | 50.00                 | 497.08          |               | 0.59                       |                     |                          | 2.92                       |                          |
      | 191       | 2026-07-10 | 50.00                 | 447.61          |               | 0.53                       |                     |                          | 2.39                       |                          |
      | 192       | 2026-07-11 | 50.00                 | 398.08          |               | 0.47                       |                     |                          | 1.92                       |                          |
      | 193       | 2026-07-12 | 50.00                 | 348.51          |               | 0.43                       |                     |                          | 1.49                       |                          |
      | 194       | 2026-07-13 | 50.00                 | 298.88          |               | 0.37                       |                     |                          | 1.12                       |                          |
      | 195       | 2026-07-14 | 50.00                 | 249.20          |               | 0.32                       |                     |                          | 0.80                       |                          |
      | 196       | 2026-07-15 | 50.00                 | 199.47          |               | 0.27                       |                     |                          | 0.53                       |                          |
      | 197       | 2026-07-16 | 50.00                 | 149.68          |               | 0.21                       |                     |                          | 0.32                       |                          |
      | 198       | 2026-07-17 | 50.00                 | 99.84           |               | 0.16                       |                     |                          | 0.16                       |                          |
      | 199       | 2026-07-18 | 50.00                 | 49.95           |               | 0.11                       |                     |                          | 0.05                       |                          |
      | 200       | 2026-07-19 | 50.00                 | 0.00            |               | 0.05                       |                     |                          | 0.00                       |                          |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 100000.0           | 9.61           | 990.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |

  @TestRailId:C85161
  Scenario: Discount amortization schedule with repayment on disbursement date and a later rate change starts the new rate on the rate change date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 900             | 100000             | 18                |          |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "900" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "900" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "100" amount on Working Capital loan account
    And Customer makes repayment on "01 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin update Working Capital period payment rate with "17" value
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 100.00            | 900.00                | 100000.00          | 18                | 360         | 50.00                 | 20                    |
    And The retrieved amortization schedule has payments with the following details:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -900.00               | 900.00          | 900.00        |                            |                     |                          | 100.00                     | 100.00                   |
      | 1         | 2026-01-01 | 50.00                 | 859.23          | 859.23        | 9.23                       | 50.00               | 9.23                     | 90.77                      | 90.77                    |
      | 2         | 2026-01-02 | 50.00                 | 818.03          | 859.23        | 8.80                       | 0.00                | 0.00                     | 81.97                      | 90.77                    |
      | 3         | 2026-01-03 | 50.00                 | 818.03          | 859.23        | 8.80                       | 0.00                | 0.00                     | 81.97                      | 90.77                    |
      | 4         | 2026-01-04 | 50.00                 | 818.03          | 859.23        | 8.80                       | 0.00                | 0.00                     | 81.97                      | 90.77                    |
      | 5         | 2026-01-05 | 50.00                 | 818.03          | 859.23        | 8.80                       | 0.00                | 0.00                     | 81.97                      | 90.77                    |
      | 6         | 2026-01-06 | 47.22                 | 820.34          |               | 8.33                       |                     |                          | 82.44                      |                          |
      | 7         | 2026-01-07 | 47.22                 | 781.08          |               | 7.96                       |                     |                          | 74.48                      |                          |
      | 8         | 2026-01-08 | 47.22                 | 741.44          |               | 7.58                       |                     |                          | 66.90                      |                          |
      | 9         | 2026-01-09 | 47.22                 | 701.42          |               | 7.20                       |                     |                          | 59.70                      |                          |
      | 10        | 2026-01-10 | 47.22                 | 661.01          |               | 6.81                       |                     |                          | 52.89                      |                          |
      | 11        | 2026-01-11 | 47.22                 | 620.20          |               | 6.41                       |                     |                          | 46.48                      |                          |
      | 12        | 2026-01-12 | 47.22                 | 579.00          |               | 6.02                       |                     |                          | 40.46                      |                          |
      | 13        | 2026-01-13 | 47.22                 | 537.40          |               | 5.62                       |                     |                          | 34.84                      |                          |
      | 14        | 2026-01-14 | 47.22                 | 495.39          |               | 5.21                       |                     |                          | 29.63                      |                          |
      | 15        | 2026-01-15 | 47.22                 | 452.98          |               | 4.81                       |                     |                          | 24.82                      |                          |
      | 16        | 2026-01-16 | 47.22                 | 410.15          |               | 4.39                       |                     |                          | 20.43                      |                          |
      | 17        | 2026-01-17 | 47.22                 | 366.91          |               | 3.98                       |                     |                          | 16.45                      |                          |
      | 18        | 2026-01-18 | 47.22                 | 323.25          |               | 3.56                       |                     |                          | 12.89                      |                          |
      | 19        | 2026-01-19 | 47.22                 | 279.17          |               | 3.14                       |                     |                          | 9.75                       |                          |
      | 20        | 2026-01-20 | 47.22                 | 234.66          |               | 2.71                       |                     |                          | 7.04                       |                          |
      | 21        | 2026-01-21 | 47.22                 | 189.72          |               | 2.28                       |                     |                          | 4.76                       |                          |
      | 22        | 2026-01-22 | 47.22                 | 144.34          |               | 1.84                       |                     |                          | 2.92                       |                          |
      | 23        | 2026-01-23 | 47.22                 | 98.52           |               | 1.40                       |                     |                          | 1.52                       |                          |
      | 24        | 2026-01-24 | 47.22                 | 52.26           |               | 0.96                       |                     |                          | 0.56                       |                          |
      | 25        | 2026-01-25 | 47.22                 | 5.54            |               | 0.50                       |                     |                          | 0.06                       |                          |
      | 26        | 2026-01-26 | 5.60                  | 0.00            |               | 0.06                       |                     |                          | 0.00                       |                          |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 900.0             | 900.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.23              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.23   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.23  |        |

  @TestRailId:C85162
  Scenario: Discount amortization schedule with a repayment on the disbursement date and a later repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 900             | 100000             | 18                |          |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "900" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "900" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "100" amount on Working Capital loan account
    And Customer makes repayment on "01 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 100.00            | 900.00                | 100000.00          | 18                | 360         | 50.00                 | 20                    |
    And The retrieved amortization schedule has payments with the following details:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -900.00               | 900.00          | 900.00        |                            |                     |                          | 100.00                     | 100.00                   |
      | 1         | 2026-01-01 | 50.00                 | 859.23          | 859.23        | 9.23                       | 50.00               | 9.23                     | 90.77                      | 90.77                    |
      | 2         | 2026-01-02 | 50.00                 | 818.03          | 818.03        | 8.80                       | 50.00               | 8.80                     | 81.97                      | 81.97                    |
      | 3         | 2026-01-03 | 50.00                 | 776.42          |               | 8.39                       |                     |                          | 73.58                      |                          |
      | 4         | 2026-01-04 | 50.00                 | 734.38          |               | 7.96                       |                     |                          | 65.62                      |                          |
      | 5         | 2026-01-05 | 50.00                 | 691.91          |               | 7.53                       |                     |                          | 58.09                      |                          |
      | 6         | 2026-01-06 | 50.00                 | 649.00          |               | 7.09                       |                     |                          | 51.00                      |                          |
      | 7         | 2026-01-07 | 50.00                 | 605.65          |               | 6.65                       |                     |                          | 44.35                      |                          |
      | 8         | 2026-01-08 | 50.00                 | 561.86          |               | 6.21                       |                     |                          | 38.14                      |                          |
      | 9         | 2026-01-09 | 50.00                 | 517.62          |               | 5.76                       |                     |                          | 32.38                      |                          |
      | 10        | 2026-01-10 | 50.00                 | 472.93          |               | 5.31                       |                     |                          | 27.07                      |                          |
      | 11        | 2026-01-11 | 50.00                 | 427.78          |               | 4.85                       |                     |                          | 22.22                      |                          |
      | 12        | 2026-01-12 | 50.00                 | 382.16          |               | 4.38                       |                     |                          | 17.84                      |                          |
      | 13        | 2026-01-13 | 50.00                 | 336.08          |               | 3.92                       |                     |                          | 13.92                      |                          |
      | 14        | 2026-01-14 | 50.00                 | 289.52          |               | 3.44                       |                     |                          | 10.48                      |                          |
      | 15        | 2026-01-15 | 50.00                 | 242.49          |               | 2.97                       |                     |                          | 7.51                       |                          |
      | 16        | 2026-01-16 | 50.00                 | 194.98          |               | 2.49                       |                     |                          | 5.02                       |                          |
      | 17        | 2026-01-17 | 50.00                 | 146.98          |               | 2.00                       |                     |                          | 3.02                       |                          |
      | 18        | 2026-01-18 | 50.00                 | 98.48           |               | 1.50                       |                     |                          | 1.52                       |                          |
      | 19        | 2026-01-19 | 50.00                 | 49.49           |               | 1.01                       |                     |                          | 0.51                       |                          |
      | 20        | 2026-01-20 | 50.00                 | 0.00            |               | 0.51                       |                     |                          | 0.00                       |                          |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 1000.0    | 100.0              | 100000.0           | 18.03          | 81.97            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 900.0             | 900.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 18.03             |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 18.03  |
      | LIABILITY | 240005       | Deferred Interest Revenue | 18.03 |        |

  @TestRailId:C85163
  Scenario: Verify Discount Fee Adjustment with following discount fee amortization when repayment is made on the disbursement date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Customer makes repayment on "01 January 2026" with 50 transaction amount on Working Capital loan
    And Admin adds Discount fee adjustment with "500" amount on transaction date "01 January 2026" on Working Capital loan account for last discount
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment               | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee Adjustment | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 500.00            | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 190                   |
    Then Working Capital loan amortization schedule has 191 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         |                            |                          | 500.00                     |
      | 1         | 01 January 2026  | 50.00                 | 50.00               | 8955.14         | 5.14                       | 5.14                     | 494.86                     |
      | 2         | 02 January 2026  | 50.00                 |                     | 8910.26         | 5.12                       |                          | 489.74                     |
      | 3         | 03 January 2026  | 50.00                 |                     | 8865.35         | 5.09                       |                          | 484.65                     |
      | 4         | 04 January 2026  | 50.00                 |                     | 8820.42         | 5.07                       |                          | 479.58                     |
      | 5         | 05 January 2026  | 50.00                 |                     | 8775.46         | 5.04                       |                          | 474.54                     |
      | 6         | 06 January 2026  | 50.00                 |                     | 8730.47         | 5.01                       |                          | 469.53                     |
      | 7         | 07 January 2026  | 50.00                 |                     | 8685.46         | 4.99                       |                          | 464.54                     |
      | 8         | 08 January 2026  | 50.00                 |                     | 8640.43         | 4.97                       |                          | 459.57                     |
      | 9         | 09 January 2026  | 50.00                 |                     | 8595.36         | 4.93                       |                          | 454.64                     |
      | 10        | 10 January 2026  | 50.00                 |                     | 8550.28         | 4.92                       |                          | 449.72                     |
      | 11        | 11 January 2026  | 50.00                 |                     | 8505.16         | 4.88                       |                          | 444.84                     |
      | 12        | 12 January 2026  | 50.00                 |                     | 8460.02         | 4.86                       |                          | 439.98                     |
      | 13        | 13 January 2026  | 50.00                 |                     | 8414.86         | 4.84                       |                          | 435.14                     |
      | 14        | 14 January 2026  | 50.00                 |                     | 8369.66         | 4.80                       |                          | 430.34                     |
      | 15        | 15 January 2026  | 50.00                 |                     | 8324.45         | 4.79                       |                          | 425.55                     |
      | 16        | 16 January 2026  | 50.00                 |                     | 8279.20         | 4.75                       |                          | 420.80                     |
      | 17        | 17 January 2026  | 50.00                 |                     | 8233.94         | 4.74                       |                          | 416.06                     |
      | 18        | 18 January 2026  | 50.00                 |                     | 8188.64         | 4.70                       |                          | 411.36                     |
      | 19        | 19 January 2026  | 50.00                 |                     | 8143.32         | 4.68                       |                          | 406.68                     |
      | 20        | 20 January 2026  | 50.00                 |                     | 8097.97         | 4.65                       |                          | 402.03                     |
      | 21        | 21 January 2026  | 50.00                 |                     | 8052.60         | 4.63                       |                          | 397.40                     |
      | 22        | 22 January 2026  | 50.00                 |                     | 8007.20         | 4.60                       |                          | 392.80                     |
      | 23        | 23 January 2026  | 50.00                 |                     | 7961.78         | 4.58                       |                          | 388.22                     |
      | 24        | 24 January 2026  | 50.00                 |                     | 7916.33         | 4.55                       |                          | 383.67                     |
      | 25        | 25 January 2026  | 50.00                 |                     | 7870.85         | 4.52                       |                          | 379.15                     |
      | 26        | 26 January 2026  | 50.00                 |                     | 7825.35         | 4.50                       |                          | 374.65                     |
      | 27        | 27 January 2026  | 50.00                 |                     | 7779.82         | 4.47                       |                          | 370.18                     |
      | 28        | 28 January 2026  | 50.00                 |                     | 7734.27         | 4.45                       |                          | 365.73                     |
      | 29        | 29 January 2026  | 50.00                 |                     | 7688.69         | 4.42                       |                          | 361.31                     |
      | 30        | 30 January 2026  | 50.00                 |                     | 7643.08         | 4.39                       |                          | 356.92                     |
      | 31        | 31 January 2026  | 50.00                 |                     | 7597.45         | 4.37                       |                          | 352.55                     |
      | 32        | 01 February 2026 | 50.00                 |                     | 7551.79         | 4.34                       |                          | 348.21                     |
      | 33        | 02 February 2026 | 50.00                 |                     | 7506.11         | 4.32                       |                          | 343.89                     |
      | 34        | 03 February 2026 | 50.00                 |                     | 7460.40         | 4.29                       |                          | 339.60                     |
      | 35        | 04 February 2026 | 50.00                 |                     | 7414.66         | 4.26                       |                          | 335.34                     |
      | 36        | 05 February 2026 | 50.00                 |                     | 7368.90         | 4.24                       |                          | 331.10                     |
      | 37        | 06 February 2026 | 50.00                 |                     | 7323.11         | 4.21                       |                          | 326.89                     |
      | 38        | 07 February 2026 | 50.00                 |                     | 7277.29         | 4.18                       |                          | 322.71                     |
      | 39        | 08 February 2026 | 50.00                 |                     | 7231.45         | 4.16                       |                          | 318.55                     |
      | 40        | 09 February 2026 | 50.00                 |                     | 7185.58         | 4.13                       |                          | 314.42                     |
      | 41        | 10 February 2026 | 50.00                 |                     | 7139.69         | 4.11                       |                          | 310.31                     |
      | 42        | 11 February 2026 | 50.00                 |                     | 7093.77         | 4.08                       |                          | 306.23                     |
      | 43        | 12 February 2026 | 50.00                 |                     | 7047.82         | 4.05                       |                          | 302.18                     |
      | 44        | 13 February 2026 | 50.00                 |                     | 7001.85         | 4.03                       |                          | 298.15                     |
      | 45        | 14 February 2026 | 50.00                 |                     | 6955.85         | 4.00                       |                          | 294.15                     |
      | 46        | 15 February 2026 | 50.00                 |                     | 6909.83         | 3.98                       |                          | 290.17                     |
      | 47        | 16 February 2026 | 50.00                 |                     | 6863.78         | 3.95                       |                          | 286.22                     |
      | 48        | 17 February 2026 | 50.00                 |                     | 6817.70         | 3.92                       |                          | 282.30                     |
      | 49        | 18 February 2026 | 50.00                 |                     | 6771.59         | 3.89                       |                          | 278.41                     |
      | 50        | 19 February 2026 | 50.00                 |                     | 6725.46         | 3.87                       |                          | 274.54                     |
      | 51        | 20 February 2026 | 50.00                 |                     | 6679.31         | 3.85                       |                          | 270.69                     |
      | 52        | 21 February 2026 | 50.00                 |                     | 6633.12         | 3.81                       |                          | 266.88                     |
      | 53        | 22 February 2026 | 50.00                 |                     | 6586.91         | 3.79                       |                          | 263.09                     |
      | 54        | 23 February 2026 | 50.00                 |                     | 6540.68         | 3.77                       |                          | 259.32                     |
      | 55        | 24 February 2026 | 50.00                 |                     | 6494.42         | 3.74                       |                          | 255.58                     |
      | 56        | 25 February 2026 | 50.00                 |                     | 6448.13         | 3.71                       |                          | 251.87                     |
      | 57        | 26 February 2026 | 50.00                 |                     | 6401.81         | 3.68                       |                          | 248.19                     |
      | 58        | 27 February 2026 | 50.00                 |                     | 6355.47         | 3.66                       |                          | 244.53                     |
      | 59        | 28 February 2026 | 50.00                 |                     | 6309.10         | 3.63                       |                          | 240.90                     |
      | 60        | 01 March 2026    | 50.00                 |                     | 6262.71         | 3.61                       |                          | 237.29                     |
      | 61        | 02 March 2026    | 50.00                 |                     | 6216.29         | 3.58                       |                          | 233.71                     |
      | 62        | 03 March 2026    | 50.00                 |                     | 6169.84         | 3.55                       |                          | 230.16                     |
      | 63        | 04 March 2026    | 50.00                 |                     | 6123.36         | 3.52                       |                          | 226.64                     |
      | 64        | 05 March 2026    | 50.00                 |                     | 6076.86         | 3.50                       |                          | 223.14                     |
      | 65        | 06 March 2026    | 50.00                 |                     | 6030.34         | 3.48                       |                          | 219.66                     |
      | 66        | 07 March 2026    | 50.00                 |                     | 5983.78         | 3.44                       |                          | 216.22                     |
      | 67        | 08 March 2026    | 50.00                 |                     | 5937.20         | 3.42                       |                          | 212.80                     |
      | 68        | 09 March 2026    | 50.00                 |                     | 5890.59         | 3.39                       |                          | 209.41                     |
      | 69        | 10 March 2026    | 50.00                 |                     | 5843.96         | 3.37                       |                          | 206.04                     |
      | 70        | 11 March 2026    | 50.00                 |                     | 5797.30         | 3.34                       |                          | 202.70                     |
      | 71        | 12 March 2026    | 50.00                 |                     | 5750.61         | 3.31                       |                          | 199.39                     |
      | 72        | 13 March 2026    | 50.00                 |                     | 5703.90         | 3.29                       |                          | 196.10                     |
      | 73        | 14 March 2026    | 50.00                 |                     | 5657.16         | 3.26                       |                          | 192.84                     |
      | 74        | 15 March 2026    | 50.00                 |                     | 5610.39         | 3.23                       |                          | 189.61                     |
      | 75        | 16 March 2026    | 50.00                 |                     | 5563.60         | 3.21                       |                          | 186.40                     |
      | 76        | 17 March 2026    | 50.00                 |                     | 5516.78         | 3.18                       |                          | 183.22                     |
      | 77        | 18 March 2026    | 50.00                 |                     | 5469.93         | 3.15                       |                          | 180.07                     |
      | 78        | 19 March 2026    | 50.00                 |                     | 5423.06         | 3.13                       |                          | 176.94                     |
      | 79        | 20 March 2026    | 50.00                 |                     | 5376.15         | 3.09                       |                          | 173.85                     |
      | 80        | 21 March 2026    | 50.00                 |                     | 5329.23         | 3.08                       |                          | 170.77                     |
      | 81        | 22 March 2026    | 50.00                 |                     | 5282.27         | 3.04                       |                          | 167.73                     |
      | 82        | 23 March 2026    | 50.00                 |                     | 5235.29         | 3.02                       |                          | 164.71                     |
      | 83        | 24 March 2026    | 50.00                 |                     | 5188.28         | 2.99                       |                          | 161.72                     |
      | 84        | 25 March 2026    | 50.00                 |                     | 5141.25         | 2.97                       |                          | 158.75                     |
      | 85        | 26 March 2026    | 50.00                 |                     | 5094.18         | 2.93                       |                          | 155.82                     |
      | 86        | 27 March 2026    | 50.00                 |                     | 5047.10         | 2.92                       |                          | 152.90                     |
      | 87        | 28 March 2026    | 50.00                 |                     | 4999.98         | 2.88                       |                          | 150.02                     |
      | 88        | 29 March 2026    | 50.00                 |                     | 4952.84         | 2.86                       |                          | 147.16                     |
      | 89        | 30 March 2026    | 50.00                 |                     | 4905.67         | 2.83                       |                          | 144.33                     |
      | 90        | 31 March 2026    | 50.00                 |                     | 4858.47         | 2.80                       |                          | 141.53                     |
      | 91        | 01 April 2026    | 50.00                 |                     | 4811.25         | 2.78                       |                          | 138.75                     |
      | 92        | 02 April 2026    | 50.00                 |                     | 4764.00         | 2.75                       |                          | 136.00                     |
      | 93        | 03 April 2026    | 50.00                 |                     | 4716.72         | 2.72                       |                          | 133.28                     |
      | 94        | 04 April 2026    | 50.00                 |                     | 4669.41         | 2.69                       |                          | 130.59                     |
      | 95        | 05 April 2026    | 50.00                 |                     | 4622.08         | 2.67                       |                          | 127.92                     |
      | 96        | 06 April 2026    | 50.00                 |                     | 4574.72         | 2.64                       |                          | 125.28                     |
      | 97        | 07 April 2026    | 50.00                 |                     | 4527.34         | 2.62                       |                          | 122.66                     |
      | 98        | 08 April 2026    | 50.00                 |                     | 4479.93         | 2.59                       |                          | 120.07                     |
      | 99        | 09 April 2026    | 50.00                 |                     | 4432.49         | 2.56                       |                          | 117.51                     |
      | 100       | 10 April 2026    | 50.00                 |                     | 4385.02         | 2.53                       |                          | 114.98                     |
      | 101       | 11 April 2026    | 50.00                 |                     | 4337.52         | 2.50                       |                          | 112.48                     |
      | 102       | 12 April 2026    | 50.00                 |                     | 4290.00         | 2.48                       |                          | 110.00                     |
      | 103       | 13 April 2026    | 50.00                 |                     | 4242.45         | 2.45                       |                          | 107.55                     |
      | 104       | 14 April 2026    | 50.00                 |                     | 4194.88         | 2.43                       |                          | 105.12                     |
      | 105       | 15 April 2026    | 50.00                 |                     | 4147.28         | 2.40                       |                          | 102.72                     |
      | 106       | 16 April 2026    | 50.00                 |                     | 4099.65         | 2.37                       |                          | 100.35                     |
      | 107       | 17 April 2026    | 50.00                 |                     | 4051.99         | 2.34                       |                          | 98.01                      |
      | 108       | 18 April 2026    | 50.00                 |                     | 4004.30         | 2.31                       |                          | 95.70                      |
      | 109       | 19 April 2026    | 50.00                 |                     | 3956.59         | 2.29                       |                          | 93.41                      |
      | 110       | 20 April 2026    | 50.00                 |                     | 3908.85         | 2.26                       |                          | 91.15                      |
      | 111       | 21 April 2026    | 50.00                 |                     | 3861.09         | 2.24                       |                          | 88.91                      |
      | 112       | 22 April 2026    | 50.00                 |                     | 3813.29         | 2.20                       |                          | 86.71                      |
      | 113       | 23 April 2026    | 50.00                 |                     | 3765.47         | 2.18                       |                          | 84.53                      |
      | 114       | 24 April 2026    | 50.00                 |                     | 3717.62         | 2.15                       |                          | 82.38                      |
      | 115       | 25 April 2026    | 50.00                 |                     | 3669.75         | 2.13                       |                          | 80.25                      |
      | 116       | 26 April 2026    | 50.00                 |                     | 3621.85         | 2.10                       |                          | 78.15                      |
      | 117       | 27 April 2026    | 50.00                 |                     | 3573.92         | 2.07                       |                          | 76.08                      |
      | 118       | 28 April 2026    | 50.00                 |                     | 3525.96         | 2.04                       |                          | 74.04                      |
      | 119       | 29 April 2026    | 50.00                 |                     | 3477.97         | 2.01                       |                          | 72.03                      |
      | 120       | 30 April 2026    | 50.00                 |                     | 3429.96         | 1.99                       |                          | 70.04                      |
      | 121       | 01 May 2026      | 50.00                 |                     | 3381.92         | 1.96                       |                          | 68.08                      |
      | 122       | 02 May 2026      | 50.00                 |                     | 3333.85         | 1.93                       |                          | 66.15                      |
      | 123       | 03 May 2026      | 50.00                 |                     | 3285.76         | 1.91                       |                          | 64.24                      |
      | 124       | 04 May 2026      | 50.00                 |                     | 3237.64         | 1.88                       |                          | 62.36                      |
      | 125       | 05 May 2026      | 50.00                 |                     | 3189.49         | 1.85                       |                          | 60.51                      |
      | 126       | 06 May 2026      | 50.00                 |                     | 3141.31         | 1.82                       |                          | 58.69                      |
      | 127       | 07 May 2026      | 50.00                 |                     | 3093.10         | 1.79                       |                          | 56.90                      |
      | 128       | 08 May 2026      | 50.00                 |                     | 3044.87         | 1.77                       |                          | 55.13                      |
      | 129       | 09 May 2026      | 50.00                 |                     | 2996.61         | 1.74                       |                          | 53.39                      |
      | 130       | 10 May 2026      | 50.00                 |                     | 2948.32         | 1.71                       |                          | 51.68                      |
      | 131       | 11 May 2026      | 50.00                 |                     | 2900.01         | 1.69                       |                          | 49.99                      |
      | 132       | 12 May 2026      | 50.00                 |                     | 2851.67         | 1.66                       |                          | 48.33                      |
      | 133       | 13 May 2026      | 50.00                 |                     | 2803.30         | 1.63                       |                          | 46.70                      |
      | 134       | 14 May 2026      | 50.00                 |                     | 2754.90         | 1.60                       |                          | 45.10                      |
      | 135       | 15 May 2026      | 50.00                 |                     | 2706.47         | 1.57                       |                          | 43.53                      |
      | 136       | 16 May 2026      | 50.00                 |                     | 2658.02         | 1.55                       |                          | 41.98                      |
      | 137       | 17 May 2026      | 50.00                 |                     | 2609.54         | 1.52                       |                          | 40.46                      |
      | 138       | 18 May 2026      | 50.00                 |                     | 2561.03         | 1.49                       |                          | 38.97                      |
      | 139       | 19 May 2026      | 50.00                 |                     | 2512.49         | 1.46                       |                          | 37.51                      |
      | 140       | 20 May 2026      | 50.00                 |                     | 2463.93         | 1.44                       |                          | 36.07                      |
      | 141       | 21 May 2026      | 50.00                 |                     | 2415.34         | 1.41                       |                          | 34.66                      |
      | 142       | 22 May 2026      | 50.00                 |                     | 2366.72         | 1.38                       |                          | 33.28                      |
      | 143       | 23 May 2026      | 50.00                 |                     | 2318.07         | 1.35                       |                          | 31.93                      |
      | 144       | 24 May 2026      | 50.00                 |                     | 2269.39         | 1.32                       |                          | 30.61                      |
      | 145       | 25 May 2026      | 50.00                 |                     | 2220.69         | 1.30                       |                          | 29.31                      |
      | 146       | 26 May 2026      | 50.00                 |                     | 2171.96         | 1.27                       |                          | 28.04                      |
      | 147       | 27 May 2026      | 50.00                 |                     | 2123.20         | 1.24                       |                          | 26.80                      |
      | 148       | 28 May 2026      | 50.00                 |                     | 2074.41         | 1.21                       |                          | 25.59                      |
      | 149       | 29 May 2026      | 50.00                 |                     | 2025.60         | 1.19                       |                          | 24.40                      |
      | 150       | 30 May 2026      | 50.00                 |                     | 1976.76         | 1.16                       |                          | 23.24                      |
      | 151       | 31 May 2026      | 50.00                 |                     | 1927.89         | 1.13                       |                          | 22.11                      |
      | 152       | 01 June 2026     | 50.00                 |                     | 1878.99         | 1.10                       |                          | 21.01                      |
      | 153       | 02 June 2026     | 50.00                 |                     | 1830.06         | 1.07                       |                          | 19.94                      |
      | 154       | 03 June 2026     | 50.00                 |                     | 1781.11         | 1.05                       |                          | 18.89                      |
      | 155       | 04 June 2026     | 50.00                 |                     | 1732.13         | 1.02                       |                          | 17.87                      |
      | 156       | 05 June 2026     | 50.00                 |                     | 1683.12         | 0.99                       |                          | 16.88                      |
      | 157       | 06 June 2026     | 50.00                 |                     | 1634.08         | 0.96                       |                          | 15.92                      |
      | 158       | 07 June 2026     | 50.00                 |                     | 1585.01         | 0.93                       |                          | 14.99                      |
      | 159       | 08 June 2026     | 50.00                 |                     | 1535.92         | 0.91                       |                          | 14.08                      |
      | 160       | 09 June 2026     | 50.00                 |                     | 1486.79         | 0.87                       |                          | 13.21                      |
      | 161       | 10 June 2026     | 50.00                 |                     | 1437.64         | 0.85                       |                          | 12.36                      |
      | 162       | 11 June 2026     | 50.00                 |                     | 1388.47         | 0.83                       |                          | 11.53                      |
      | 163       | 12 June 2026     | 50.00                 |                     | 1339.26         | 0.79                       |                          | 10.74                      |
      | 164       | 13 June 2026     | 50.00                 |                     | 1290.02         | 0.76                       |                          | 9.98                       |
      | 165       | 14 June 2026     | 50.00                 |                     | 1240.76         | 0.74                       |                          | 9.24                       |
      | 166       | 15 June 2026     | 50.00                 |                     | 1191.47         | 0.71                       |                          | 8.53                       |
      | 167       | 16 June 2026     | 50.00                 |                     | 1142.15         | 0.68                       |                          | 7.85                       |
      | 168       | 17 June 2026     | 50.00                 |                     | 1092.80         | 0.65                       |                          | 7.20                       |
      | 169       | 18 June 2026     | 50.00                 |                     | 1043.43         | 0.63                       |                          | 6.57                       |
      | 170       | 19 June 2026     | 50.00                 |                     | 994.02          | 0.59                       |                          | 5.98                       |
      | 171       | 20 June 2026     | 50.00                 |                     | 944.59          | 0.57                       |                          | 5.41                       |
      | 172       | 21 June 2026     | 50.00                 |                     | 895.13          | 0.54                       |                          | 4.87                       |
      | 173       | 22 June 2026     | 50.00                 |                     | 845.64          | 0.51                       |                          | 4.36                       |
      | 174       | 23 June 2026     | 50.00                 |                     | 796.13          | 0.49                       |                          | 3.87                       |
      | 175       | 24 June 2026     | 50.00                 |                     | 746.58          | 0.45                       |                          | 3.42                       |
      | 176       | 25 June 2026     | 50.00                 |                     | 697.01          | 0.43                       |                          | 2.99                       |
      | 177       | 26 June 2026     | 50.00                 |                     | 647.41          | 0.40                       |                          | 2.59                       |
      | 178       | 27 June 2026     | 50.00                 |                     | 597.78          | 0.37                       |                          | 2.22                       |
      | 179       | 28 June 2026     | 50.00                 |                     | 548.12          | 0.34                       |                          | 1.88                       |
      | 180       | 29 June 2026     | 50.00                 |                     | 498.43          | 0.31                       |                          | 1.57                       |
      | 181       | 30 June 2026     | 50.00                 |                     | 448.72          | 0.29                       |                          | 1.28                       |
      | 182       | 01 July 2026     | 50.00                 |                     | 398.97          | 0.25                       |                          | 1.03                       |
      | 183       | 02 July 2026     | 50.00                 |                     | 349.20          | 0.23                       |                          | 0.80                       |
      | 184       | 03 July 2026     | 50.00                 |                     | 299.40          | 0.20                       |                          | 0.60                       |
      | 185       | 04 July 2026     | 50.00                 |                     | 249.57          | 0.17                       |                          | 0.43                       |
      | 186       | 05 July 2026     | 50.00                 |                     | 199.71          | 0.14                       |                          | 0.29                       |
      | 187       | 06 July 2026     | 50.00                 |                     | 149.83          | 0.12                       |                          | 0.17                       |
      | 188       | 07 July 2026     | 50.00                 |                     | 99.91           | 0.08                       |                          | 0.09                       |
      | 189       | 08 July 2026     | 50.00                 |                     | 49.97           | 0.06                       |                          | 0.03                       |
      | 190       | 09 July 2026     | 50.00                 |                     | 0.00            | 0.03                       |                          | 0.00                       |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 100000.0           | 0.0            | 500.0            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee Adjustment   | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 100000.0           | 5.14           | 494.86           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee Adjustment   | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 5.14              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 5.14   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 5.14  |        |

  @TestRailId:C94061
  Scenario: Working Capital loan raises Discount Fee Amortization transaction business event from the COB run
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "05 January 2026"
    Then Admin closes the Working Capital loan with a full repayment on "06 January 2026"

  @TestRailId:C98177
  Scenario: Verify that discount fee amortization after a repayment above the daily amount stays consistent with the schedule over the following on-time repayments
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 17                |          |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 17                | 360         | 47.22                 | 212                   |
    And The retrieved amortization schedule has payments with the following details in first "5" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         | 9000.00       |                            |                     |                          | 1000.00                    | 1000.00                  |
      | 1         | 2026-01-02 | 47.22                 | 8961.86         | 8959.61       | 9.08                       | 50.00               | 9.61                     | 990.92                     | 990.39                   |
      | 2         | 2026-01-03 | 47.22                 | 8921.43         |               | 9.04                       |                     |                          | 981.35                     |                          |
      | 3         | 2026-01-04 | 47.22                 | 8883.21         |               | 9.00                       |                     |                          | 972.35                     |                          |
      | 4         | 2026-01-05 | 47.22                 | 8844.95         |               | 8.96                       |                     |                          | 963.39                     |                          |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 210       | 2026-07-30 | 47.22                 | 80.90           | 0.13                       | 0.12                       |
      | 211       | 2026-07-31 | 47.22                 | 33.77           | 0.09                       | 0.03                       |
      | 212       | 2026-08-01 | 33.80                 | 0.00            | 0.03                       | 0.00                       |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
    And Customer makes repayment on "03 January 2026" with 47.22 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 97.22              | 18.65          | 981.35           | 0.0               |
    And Customer makes repayment on "04 January 2026" with 47.22 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 47.22 transaction amount on Working Capital loan
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 191.66             | 36.61          | 963.39           | 0.0               |
    And Customer makes repayment on "06 January 2026" with 47.22 transaction amount on Working Capital loan
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "07 January 2026" with 47.22 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 286.1              | 54.41          | 945.59           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 47.22             | 47.22            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 9.04              |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                 | 47.22             | 47.22            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 9.0               |                  |                   |                       | false    |
      | 05 January 2026 | Repayment                 | 47.22             | 47.22            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 8.96              |                  |                   |                       | false    |
      | 06 January 2026 | Repayment                 | 47.22             | 47.22            | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Discount Fee Amortization | 8.92              |                  |                   |                       | false    |
      | 07 January 2026 | Repayment                 | 47.22             | 47.22            | 0.0               | 0.0                   | false    |
      | 07 January 2026 | Discount Fee Amortization | 8.88              |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualDiscountFeeBalance |
      | 1         | 2026-01-02 | 47.22                 | 9.08                       | 50.00               | 9.61                     | 990.92                     | 990.39                   |
      | 2         | 2026-01-03 | 47.22                 | 9.04                       | 47.22               | 9.04                     | 981.35                     | 981.35                   |
      | 3         | 2026-01-04 | 47.22                 | 9.00                       | 47.22               | 9.00                     | 972.35                     | 972.35                   |
      | 4         | 2026-01-05 | 47.22                 | 8.96                       | 47.22               | 8.96                     | 963.39                     | 963.39                   |
      | 5         | 2026-01-06 | 47.22                 | 8.92                       | 47.22               | 8.92                     | 954.47                     | 954.47                   |
      | 6         | 2026-01-07 | 47.22                 | 8.88                       | 47.22               | 8.88                     | 945.59                     | 945.59                   |
    And The retrieved amortization schedule has no negative amounts
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business

  @TestRailId:C98178
  Scenario: Verify that discount fee amortization after a repayment below the daily amount stays consistent with the schedule over the following on-time repayments
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 17                |          |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 40 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 40.0               | 7.69           | 992.31           | 0.0               |
    And Customer makes repayment on "03 January 2026" with 47.22 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "04 January 2026" with 47.22 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 47.22 transaction amount on Working Capital loan
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 181.66             | 34.71          | 965.29           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 40.0              | 40.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 7.69              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 47.22             | 47.22            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 9.05              |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                 | 47.22             | 47.22            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 9.0               |                  |                   |                       | false    |
      | 05 January 2026 | Repayment                 | 47.22             | 47.22            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 8.97              |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | actualBalance | actualDiscountFeeBalance |
      | 1         | 2026-01-02 | 47.22                 | 9.08                       | 40.00               | 7.69                     | 8967.69       | 992.31                   |
      | 2         | 2026-01-03 | 47.22                 | 9.05                       | 47.22               | 9.05                     | 8929.52       | 983.26                   |
      | 3         | 2026-01-04 | 47.22                 | 9.00                       | 47.22               | 9.00                     | 8891.30       | 974.26                   |
      | 4         | 2026-01-05 | 47.22                 | 8.97                       | 47.22               | 8.97                     | 8853.05       | 965.29                   |
    And The retrieved amortization schedule has no negative amounts
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business

  @TestRailId:C98179
  Scenario: Verify that discount fee amortization after a fractional repayment closes to the discount fee without an amortization adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 450             | 100000             | 18                |          |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "450" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "450" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "50" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 50.00             | 450.00                | 100000.00          | 18                | 360         | 50.00                 | 10                    |
    And The retrieved amortization schedule expected amortization sums to the discount fee and both expected balances close to zero
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50.25 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 500.0     | 100.25             | 16.89          | 33.11            | 0.0               |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business
    And Customer makes repayment on "04 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "06 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "07 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "08 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "09 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 500.0     | 450.25             | 49.04          | 0.96             | 0.0               |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business
    And Customer makes repayment on "11 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 500.0     | 500.0              | 50.0           | 0.0              | 0.25              |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 450.0             | 450.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.25             | 50.25            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 8.87              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 8.02              |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 7.20              |                  |                   |                       | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 6.36              |                  |                   |                       | false    |
      | 06 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Discount Fee Amortization | 5.50              |                  |                   |                       | false    |
      | 07 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 07 January 2026 | Discount Fee Amortization | 4.63              |                  |                   |                       | false    |
      | 08 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization | 3.73              |                  |                   |                       | false    |
      | 09 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 09 January 2026 | Discount Fee Amortization | 2.83              |                  |                   |                       | false    |
      | 10 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1.90              |                  |                   |                       | false    |
      | 11 January 2026 | Repayment                 | 50.0              | 49.75            | 0.0               | 0.0                   | false    |
      | 11 January 2026 | Discount Fee Amortization | 0.96              |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has no negative amounts
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business

  @TestRailId:C98213
  Scenario: Verify that a lump-sum payoff of a working capital loan with a remainder final payment amortizes the whole discount fee on the schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 17                |          |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 10000 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 10000.0            | 1000.0         | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | actualPaymentAmount | actualAmortizationAmount | actualBalance | actualDiscountFeeBalance |
      | 1         | 2026-01-02 | 47.22                 | 10000.00            | 1000.00                  | 0.00          | 0.00                     |
