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
      | 9012.0    | 9000.0            | 100000.0           | 18.0              | 12.0     | 9012.0             | 0.0            | 12.0             | 8.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee    | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Repayment       | 9020.0            | 9012.0           | 0.0               | 0.0                  | false    |
#--- make backdated discount fee adjustment to make loan closed as per finalize test scenario ---#
    And Customer makes credit balance refund on "15 January 2026" with 8.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

