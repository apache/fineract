@WorkingCapital
@WorkingCapitalLoanPayoutRefundFeature
Feature: Working Capital Loan Payout Refund

  @TestRailId:C85624
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC1: simple Payout Refund transaction then undo
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
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 270.0            | 0.0               | 0.0                   | false    |
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 270.0            | 0.0               | 0.0                   | true     |
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "10 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85625
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC2: Payout Refund transaction with charges then undo
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
    # Add charges
    When Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 25.0 transaction amount
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 230.0            | 15.0              | 25.0                  | false    |
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 230.0            | 15.0              | 25.0                  | true     |
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "10 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85626
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC3: overpay with Payout Refund transaction with charges then undo and verify Journal Entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ACC_DEF_REV_AM | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    # Add charges
    When Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 25.0 transaction amount
    Then Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 9140.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 9140.0            | 9000.0           | 15.0              | 25.0                  | false    |
      | 10 January 2026 | Accrual       | 15.0              | 0.0              | 15.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual       | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
    And Working Capital loan status will be "OVERPAID"
    And Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9140.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 15.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 25.0   |
      | LIABILITY | 245000       | Other Credit Liability    |        | 100.0  |
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 9140.0            | 9000.0           | 15.0              | 25.0                  | true     |
      | 10 January 2026 | Accrual       | 15.0              | 0.0              | 15.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual       | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9140.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 15.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 25.0   |
      | LIABILITY | 245000       | Other Credit Liability    |        | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9140.0 |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 15.0   |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 25.0   |        |
      | LIABILITY | 245000       | Other Credit Liability    | 100.0  |        |
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "10 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85627
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC4: Payout Refund + repayments with charges then undo Payout Refund
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
    # Add charges
    When Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 25.0 transaction amount
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin sets the business date to "5 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "5 January 2026" with 123.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 83.0               | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | false    |
    When Admin sets the business date to "7 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "7 January 2026" with 73.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 156.0              | 0.0               |
    # Verify loan transactions
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | false    |
      | 07 January 2026 | Repayment     | 73.0              | 73.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "10 January 2026" with 89.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 245.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | false    |
      | 07 January 2026 | Repayment     | 73.0              | 73.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 89.0              | 89.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Undo transaction
    And Customer undo "1"th "PAYOUT_REFUND" transaction made on "05 January 2026" on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 122.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | true     |
      | 07 January 2026 | Repayment     | 73.0              | 33.0             | 15.0              | 25.0                  | false    |
      | 10 January 2026 | Repayment     | 89.0              | 89.0             | 0.0               | 0.0                   | false    |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "12 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85628
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC5: Backdated Payout Refund + repayments with charges then undo Payout Refund
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
    # Add charges
    When Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 25.0 transaction amount
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin sets the business date to "7 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "7 January 2026" with 73.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 33.0               | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 07 January 2026 | Repayment    | 73.0              | 33.0             | 15.0              | 25.0                  | false    |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "10 January 2026" with 89.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 122.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 07 January 2026 | Repayment    | 73.0              | 33.0             | 15.0              | 25.0                  | false    |
      | 10 January 2026 | Repayment    | 89.0              | 89.0             | 0.0               | 0.0                   | false    |
    # Set business date
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "5 January 2026" with 123.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 245.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | false    |
      | 07 January 2026 | Repayment     | 73.0              | 73.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 89.0              | 89.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Undo transaction
    And Customer undo "1"th "PAYOUT_REFUND" transaction made on "05 January 2026" on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 122.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | true     |
      | 07 January 2026 | Repayment     | 73.0              | 33.0             | 15.0              | 25.0                  | false    |
      | 10 January 2026 | Repayment     | 89.0              | 89.0             | 0.0               | 0.0                   | false    |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "13 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85629
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC6: example scenario with discount fee and amortization
    When Admin sets the business date to "01 January 2019"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2019 | 01 January 2019          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2019" with "9000" amount and expected disbursement date on "01 January 2019"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2019" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    # Add discount fee
    When Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    Then Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | WCLP_ACC_DEF_REV_AM | 2019-01-01      | 2019-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0   | 0.0                | 0.0            | 1000.0           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2019 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2019 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "02 January 2019"
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "02 January 2019" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2019"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | product.name        | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | WCLP_ACC_DEF_REV_AM | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2019 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2019 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2019 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2019 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "03 January 2019" with 4000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2019"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | product.name        | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | WCLP_ACC_DEF_REV_AM | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0   | 4050.0             | 634.67         | 365.33           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2019 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2019 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2019 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2019 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2019 | Payout Refund             | 4000.0            | 4000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2019 | Discount Fee Amortization | 625.06            |                  |                   |                       | false    |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "04 January 2019"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85630
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC7: Payout Refund on a closed WCP loan, undo refund on OVERPAID status
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "10 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    # Execute PAYOUT_REFUND transaction
    When Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name        | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | WCLP_ACC_DEF_REV_AM | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0             | 0.0            | 0.0              | 100.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 100.0             | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital loan status will be "OVERPAID"
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 100.0             | 0.0              | 0.0               | 0.0                   | true     |
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85631
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC8: Payout Refund on an already overpaid WCP loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "10 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | product.name        | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP_ACC_DEF_REV_AM | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0             | 100.0             |
    # Execute PAYOUT_REFUND transaction
    When Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name        | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP_ACC_DEF_REV_AM | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0             | 150.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 9100.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 50.0              | 0.0              | 0.0               | 0.0                   | false    |
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 9100.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 50.0              | 0.0              | 0.0               | 0.0                   | true     |
    And Working Capital loan status will be "OVERPAID"

  @TestRailId:C85632
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC9: Payout Refund allocation with WCLP_DUE_FEE_PENALTY_PRINCIPAL
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DUE_FEE_PENALTY_PRINCIPAL | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Add charges
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "12 January 2026" with 35.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 0.0                | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 5.0                 | 20.0         |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Payout Refund | 35.0              | 0.0              | 15.0              | 20.0                  | false    |
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "12 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Payout Refund | 35.0              | 0.0              | 15.0              | 20.0                  | true     |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "12 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85633
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC10: Payout Refund allocation with WCLP_IN_ADVANCE_PENALTY_FEE_PRINCIPAL
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                           | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_IN_ADVANCE_PENALTY_FEE_PRINCIPAL | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Add charges
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "12 January 2026" with 35.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 35.0               | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 15.0            | 0.0      | 25.0           | 25.0                | 0.0          |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Payout Refund | 35.0              | 35.0             | 0.0               | 0.0                   | false    |
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "12 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Payout Refund | 35.0              | 35.0             | 0.0               | 0.0                   | true     |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "10 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85634
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC11: Payout Refund allocation with WCLP_DUE_FEE_PRINCIPAL_PENALTY
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DUE_FEE_PRINCIPAL_PENALTY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Add charges
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "12 January 2026" with 35.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 20.0               | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 25.0                | 0.0          |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Payout Refund | 35.0              | 20.0             | 15.0              | 0.0                   | false    |
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "12 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Payout Refund | 35.0              | 20.0             | 15.0              | 0.0                   | true     |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "12 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85635
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC12: Payout Refund allocation with WCLP_DUE_PRINCIPAL_FEE_PENALTY
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DUE_PRINCIPAL_FEE_PENALTY | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Add charges
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "12 January 2026" with 35.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 35.0               | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 15.0            | 0.0      | 25.0           | 25.0                | 0.0          |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Payout Refund | 35.0              | 35.0             | 0.0               | 0.0                   | false    |
    # Undo transaction
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "12 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Payout Refund | 35.0              | 35.0             | 0.0               | 0.0                   | true     |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "12 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85636
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC13: Discount fee amortization after Payout Refund and verify Journal Entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    # Add discount fee
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name        | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | WCLP_ACC_DEF_REV_AM | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0   | 0.0                | 0.0            | 1000.0           | 0.0               |
    When Admin sets the business date to "02 January 2026"
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | product.name        | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP_ACC_DEF_REV_AM | 10000.0   | 50.0               | 9.61           | 990.39           |
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "03 January 2026" with 4000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | product.name        | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP_ACC_DEF_REV_AM | 10000.0   | 4050.0             | 634.67         | 365.33           |
    And Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 4000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 4000.0 |
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "04 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85637
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC14: Backdated Payout Refund with discount fee and amortization adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    # Add discount fee
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | product.name        | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP_ACC_DEF_REV_AM | 10000.0   | 50.0               | 9.61           | 990.39           |
    # Execute backdated PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "02 January 2026" with 4000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | product.name        | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP_ACC_DEF_REV_AM | 10000.0   | 4050.0             | 634.67         | 365.33           |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 02 January 2026 | Payout Refund             | 4000.0            | 4000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 625.06            |                  |                   |                       | false    |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "04 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85638
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC15: Undo Payout Refund on a discounted loan reverses amortization
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    # Add discount fee
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    # Execute REPAYMENT transaction
    And Customer makes "REPAYMENT" transaction on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working capital loan account has the correct data:
      | product.name        | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP_ACC_DEF_REV_AM | 50.0               | 9.61           | 990.39           |
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "03 January 2026" with 4000.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name        | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP_ACC_DEF_REV_AM | 4050.0             | 9.61           | 990.39           |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Payout Refund             | 4000.0            | 4000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "04 January 2026"
    # Undo transaction
    And Customer undo "1"th "PAYOUT_REFUND" transaction made on "03 January 2026" on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name        | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP_ACC_DEF_REV_AM | 50.0               | 9.61           | 990.39           |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Payout Refund             | 4000.0            | 4000.0           | 0.0               | 0.0                   | true     |
    And Working Capital loan status will be "ACTIVE"
    # Close loan
    When Admin closes the Working Capital loan with a full repayment on "04 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85639
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC16: Payout Refund on a loan with APPROVED status results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | status   |
      | WCLP         | Approved |
    When Initiating a "PAYOUT_REFUND" transaction on "01 January 2026" with 100.0 transaction amount on Working Capital loan results an error with the following data:
      | httpCode | errorMessage                                                                   |
      | 400      | Payout Refund is allowed only for active/closed obligations met/overpaid loans |

  @TestRailId:C85640
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC17: Payout Refund with negative amount results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a "PAYOUT_REFUND" transaction on "10 January 2026" with -100.0 transaction amount on Working Capital loan results an error with the following data:
      | httpCode | errorMessage                                              |
      | 400      | The parameter `transactionAmount` must be greater than 0. |

  @TestRailId:C85641
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC18: Payout Refund with zero amount results in an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a "PAYOUT_REFUND" transaction on "10 January 2026" with 0.0 transaction amount on Working Capital loan results an error with the following data:
      | httpCode | errorMessage                                              |
      | 400      | The parameter `transactionAmount` must be greater than 0. |

  @TestRailId:C85642
  Scenario: Verify working capital loan Payout Refund backdated/undo transaction - UC19: Undo an already reversed Payout Refund transaction results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Execute PAYOUT_REFUND transaction
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    And Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Customer tries to undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan and gets error:
      | httpCode | errorMessage                |
      | 400      | transaction.already.undone. |
