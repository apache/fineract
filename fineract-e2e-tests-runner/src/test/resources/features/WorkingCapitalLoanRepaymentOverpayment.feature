@WorkingCapital
@WorkingCapitalLoanRepaymentOverpaymentFeature
Feature: Working Capital Loan Repayment - Overpayment

  @TestRailId:C85372
  Scenario: Verify fully repaid Working Capital loan accepts repayment and its status goes to overpaid - UC1
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
# --- repay loan to make it closed --- #
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "20 January 2026" with 9000.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Closed (obligations met) | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0                | 0.0               |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- repay closed loan to make it overpaid --- #
    And Customer makes repayment on "21 January 2026" with 199.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0             | 199.0             |
    Then Working Capital loan status will be "OVERPAID"
#--- make CBR to make loan closed as per finalize test scenario ---#
    And Customer makes credit balance refund on "21 January 2026" with 199.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85373
  Scenario: Verify overpaid Working Capital loan accepts repayment and its status is overpaid - UC2
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
# --- repay active loan with amount to make it overpaid --- #
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "20 January 2026" with 9200.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0                | 200.0               |
    Then Working Capital loan status will be "OVERPAID"
# --- repay on overpaid loan should be allowed --- #
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "21 January 2026" with 199.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0             | 399.0             |
    Then Working Capital loan status will be "OVERPAID"
#--- make CBR to make loan closed as per finalize test scenario ---#
    And Customer makes credit balance refund on "21 January 2026" with 399.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85374
  Scenario: Verify working capital loan overpaid with repayment after Goodwill Credit transaction - UC3
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
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0     | 18.0              | null     |
    When Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
#   --- Mixed: repayment + goodwill credit in same period ---
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 170.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 170.0              | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit | 170.0             | 170.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "12 January 2026"
    And Customer makes repayment on "12 January 2026" with 8900.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0             | 0.0            | 0.0              | 70.0              |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit | 170.0             | 170.0            | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment       | 8900.0            | 8830.0           | 0.0               | 0.0                   | false    |
#--- make CBR to make loan closed as per finalize test scenario ---#
    And Customer makes credit balance refund on "12 January 2026" with 70.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85375
  Scenario: Verify Working Capital loan overpaid with multiple repayments after Charge on loan account level - UC4
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 35.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working capital loan account has the correct data:
      | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 0.0                | 0.0            | 0.0              | 0.0               |
# --- repay loan to make it closed --- #
    When Admin sets the business date to "12 January 2026"
    And Customer makes repayment on "12 January 2026" with 9035.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment       | 9035.0            | 9000.0           | 0.0               | 35.0                  | false    |
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 120.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0             | 0.0            | 0.0              | 120.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment       | 9035.0            | 9000.0           | 0.0               | 35.0                  | false    |
      | 15 January 2026 | Repayment       | 120.0             | 0.0              | 0.0               | 0.0                   | false    |
#--- make CBR to make loan closed as per finalize test scenario ---#
    And Customer makes credit balance refund on "15 January 2026" with 120.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85376
  Scenario: Verify Working Capital loan account goes overpaid with repayment on loan with added discount before - UC5
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9012.0    | 9000.0            | 100000.0           | 18.0              | 0.0              | null             | 12.0     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 9020.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9012.0    | 9000.0            | 100000.0           | 18.0              | 12.0     | 9012.0             | 12.0           | 0.0              | 8.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Repayment                 | 9020.0            | 9012.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Discount Fee Amortization | 12.0              |                  |                   |                       | false    |
#--- make backdated discount fee adjustment to make loan closed as per finalize test scenario ---#
    And Customer makes credit balance refund on "15 January 2026" with 8.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C98268
  Scenario: Verify overpaidOnDate is set when an ACTIVE loan becomes overpaid, is stable across further repayments and is cleared by a full CBR - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan details has the following field values:
      | overpaidOnDate | null |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "04 January 2026" with 1150.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate            | 2026-01-04 |
      | balance.overpaymentAmount | 150.0      |
      | timeline.closedOnDate     | null       |
    # a further repayment on an already overpaid loan must not move the date
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "06 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate            | 2026-01-04 |
      | balance.overpaymentAmount | 200.0      |
    # full CBR closes the loan, overpaidOnDate must be cleared
    And Customer makes credit balance refund on "06 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan details has the following field values:
      | overpaidOnDate        | null       |
      | timeline.closedOnDate | 2026-01-06 |

  @TestRailId:C98269
  Scenario: Verify overpaidOnDate is set when a CLOSED loan is overpaid by a later repayment - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan details has the following field values:
      | overpaidOnDate        | null       |
      | timeline.closedOnDate | 2026-01-01 |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "04 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate        | 2026-01-04 |
      | timeline.closedOnDate | null       |

  @TestRailId:C98270
  Scenario: Verify overpaidOnDate reflects the transaction date of a backdated overpaying repayment, not the business date - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "03 January 2026" with 1150.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate | 2026-01-03 |

  @TestRailId:C98271
  Scenario: Verify overpaidOnDate is cleared when the overpaying repayment is undone and the loan goes back to ACTIVE - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "04 January 2026" with 1150.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate | 2026-01-04 |
    When Admin sets the business date to "06 January 2026"
    And Customer undo "1"th "REPAYMENT" transaction made on "04 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan details has the following field values:
      | overpaidOnDate | null |

  @TestRailId:C98272
  Scenario: Verify overpaidOnDate is cleared when a charge larger than the overpayment reopens the loan to ACTIVE - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1150.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate | 2026-01-01 |
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "06 January 2026" due date and 200.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan details has the following field values:
      | overpaidOnDate | null |

  @TestRailId:C98273
  Scenario: Verify overpaidOnDate when a backdated repayment tips an already closed loan into overpayment - UC11
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "06 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    # the loan was only fully paid on 06 January, so the overpayment can not start earlier than that
    And Customer makes repayment on "03 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate | 2026-01-06 |

  @TestRailId:C98274
  Scenario: Verify overpaidOnDate after undoing the earlier of two overpaying repayments - UC12
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "03 January 2026" with 1200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate | 2026-01-03 |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 1200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate | 2026-01-03 |
    # after removing the 03 January payment only the 05 January payment is left, so the loan cannot have been
    # overpaid before 05 January
    When Customer undo "1"th "REPAYMENT" transaction made on "03 January 2026" on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate            | 2026-01-05 |
      | balance.overpaymentAmount | 200.0      |

  @TestRailId:C98275
  Scenario: Verify the overpaidOnDate exposed on the loan is also carried by the Status Changed business event - UC13
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "1000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "04 January 2026" with 1150.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan details has the following field values:
      | overpaidOnDate | 2026-01-04 |
    And a Working Capital Loan Status Changed business event is raised with overpaidOnDate matching the API
