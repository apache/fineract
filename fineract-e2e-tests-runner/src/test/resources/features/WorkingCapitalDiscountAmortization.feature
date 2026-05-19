@WorkingCapitalDiscountFeeAmortizationFeature
Feature: WorkingCapitalDiscountFeeAmortization

  @TestRailId:C80968
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB by repayment - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0    | 0.0               | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      |WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active   | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
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
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9850.0    | 150.0              | 100000.0     | 28.7           | 971.3            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit | Credit |
      | INCOME    | 404000       | Interest Income            |       | 28.7   |
      | LIABILITY | 240005       | Deferred Interest Revenue  | 28.7  |        |

  @TestRailId:C80969
  Scenario: Verify NO Discount Fee Amortization transaction on Working Capital Loan account triggers on COB without repayment - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0    | 0.0               | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      |WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active   | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0     | 18.0              | null             | null             | 1000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 0.0                | 100000.0     | 0.0            | 1000.0           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C80970
  Scenario: Verify NO Discount Fee Amortization transaction on Working Capital Loan account triggers on COB if NO discount added - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0    | 0.0               | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      |WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active   | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
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
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 8850.0    | 150.0              | 100000.0     | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 150.0             | 150.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C80971
  Scenario: Verify NO duplicated Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run again without new repayments - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0    | 0.0               | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      |WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active   | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
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
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9850.0    | 150.0              | 100000.0     | 28.7           | 971.3            | 0.0               |
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
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9850.0    | 150.0              | 100000.0     | 28.7           | 971.3            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 28.7   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 28.7   |        |

  @TestRailId:C80972
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run by each repayment  - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0    | 0.0               | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      |WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active   | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0     | 18.0              | null             | null             | 1000.0   |
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
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9950.0    | 50.0               | 100000.0     | 9.61           | 990.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 9.61   |        |
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
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9900.0    | 100.0              | 100000.0     | 19.18          | 980.82           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 10 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 9.57              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 9.57   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 9.57   |        |

  @TestRailId:C80973
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run by repayment at next day transaction date - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0    | 0.0               | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      |WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active   | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0     | 18.0              | null             | null             | 1000.0   |
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
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9850.0    | 150.0              | 100000.0     | 28.7           | 971.3            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 28.7   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 28.7   |        |

  @TestRailId:C8974
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run by repayment with amount less then discount amount - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0    | 0.0               | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      |WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active   | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0     | 18.0              | null             | null             | 1000.0   |
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
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9950.0    | 50.0               | 100000.0     | 9.61           | 990.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 9.61   |        |

  @TestRailId:C80975
  Scenario: Verify Discount Fee Amortization transaction on Working Capital Loan account triggers on COB run by a few repayments at the same date - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0    | 0.0               | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      |WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active   | 9000.0    | 9000.0            | 100000.0     | 18.0              | null             | null             | null     |
# --- update discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0     | 18.0              | null             | null             | 1000.0   |
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
      | principal | totalPaidPrincipal | totalPayment | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9900.0    | 100.0              | 100000.0     | 19.18          | 980.82           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 19.18             |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 19.18  |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 19.18  |        |
