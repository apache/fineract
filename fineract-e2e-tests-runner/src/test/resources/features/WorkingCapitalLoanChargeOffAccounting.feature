@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalLoanChargeOffAccountingFeature
Feature: Working Capital Charge-Off Accounting Entries

  @TestRailId:C93941
  Scenario: Verify Working Capital charge-off accounting - UC1: principal only non-fraud
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93942
  Scenario: Verify Working Capital charge-off accounting - UC2: fraud expense
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
# --- Fraud flag ---
    When Admin sets the fraud flag of the Working Capital loan to true
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable           |        | 9000.0 |
# --- Undo charge-off ---
    When Admin undoes the charge-off on the Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable           |        | 9000.0 |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable           | 9000.0 |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93943
  Scenario: Verify Working Capital charge-off accounting - UC3: fees and penalties
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 30.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | INCOME  | 404008       | Fee Charge Off          | 30.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 30.0   |
# --- Undo charge-off ---
    When Admin undoes the charge-off on the Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | INCOME  | 404008       | Fee Charge Off          | 30.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 30.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |        | 9000.0 |
      | INCOME  | 404008       | Fee Charge Off          |        | 50.0   |
      | INCOME  | 404008       | Fee Charge Off          |        | 30.0   |
      | ASSET   | 112601       | Loans Receivable        | 9000.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable | 50.0   |        |
      | ASSET   | 112603       | Interest/Fee Receivable | 30.0   |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93944
  Scenario: Verify Working Capital charge-off accounting - UC4: undo charge-off reverses journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
# --- Undo charge-off ---
    When Admin undoes the charge-off on the Working Capital loan
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93945
  Scenario: Verify Working Capital charge-off accounting - UC5: repayment after charge-off posts to recovery income
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
    And Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment ---
    And Customer makes repayment on "11 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
# --- Undo transaction ---
    When Customer undo "1"th "Repayment" transaction made on "11 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
      | INCOME    | 744008       | Recoveries                | 500.0 |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "11 January 2026"

  @TestRailId:C93946
  Scenario: Verify Working Capital charge-off accounting - UC6: amortization after charge-off credits charge-off expense
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
# --- Discount fee ---
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "02 January 2026"
# --- Repayment ---
    And Customer makes repayment on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | INCOME    | 744008       | Recoveries                |       | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
# --- Undo transaction ---
    When Customer undo "1"th "Repayment" transaction made on "02 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | INCOME    | 744008       | Recoveries                |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 50.0   |
      | INCOME    | 744008       | Recoveries                | 50.0  |        |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | true     |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 January 2026"

  @TestRailId:C93947
  Scenario: Verify Working Capital charge-off accounting - UC7: goodwill credit after charge-off posts to recoveries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Goodwill credit ---
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 320.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | INCOME  | 404008       | Fee Charge Off           | 50.0  |        |
      | INCOME  | 744008       | Recoveries               |       | 270.0  |
      | INCOME  | 744008       | Recoveries               |       | 50.0   |
# --- Undo transaction ---
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | INCOME  | 404008       | Fee Charge Off           | 50.0  |        |
      | INCOME  | 744008       | Recoveries               |       | 270.0  |
      | INCOME  | 744008       | Recoveries               |       | 50.0   |
      | EXPENSE | 744003       | Goodwill Expense Account |       | 270.0  |
      | INCOME  | 404008       | Fee Charge Off           |       | 50.0   |
      | INCOME  | 744008       | Recoveries               | 270.0 |        |
      | INCOME  | 744008       | Recoveries               | 50.0  |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93948
  Scenario: Verify Working Capital charge-off accounting - UC8: payout refund after charge-off credits charge-off expense
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Payout refund ---
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 500.0  |
# --- Undo transaction ---
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 500.0 |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93949
  Scenario: Verify Working Capital charge-off accounting - UC9: fee charge adjustment after charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Charge adjustment ---
    And Admin makes a charge adjustment for the last added fee charge with 50.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 50.0  |        |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
# --- Revert charge adjustment ---
    When Admin reverts the last charge adjustment on working capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 50.0  |        |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
      | INCOME | 404007       | Fee Income     |       | 50.0   |
      | INCOME | 404008       | Fee Charge Off | 50.0  |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93950
  Scenario: Verify Working Capital charge-off accounting - UC10: penalty charge adjustment after charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 30.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Charge adjustment ---
    And Admin makes a charge adjustment for the last added penalty charge with 30.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 30.0  |        |
      | INCOME | 404008       | Fee Charge Off |       | 30.0   |
# --- Revert charge adjustment ---
    When Admin reverts the last charge adjustment on working capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 30.0  |        |
      | INCOME | 404008       | Fee Charge Off |       | 30.0   |
      | INCOME | 404007       | Fee Income     |       | 30.0   |
      | INCOME | 404008       | Fee Charge Off | 30.0  |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93951
  Scenario: Verify Working Capital charge-off accounting - UC11: payout refund after fraud charge-off credits fraud expense
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
# --- Fraud flag ---
    When Admin sets the fraud flag of the Working Capital loan to true
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Payout refund ---
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 400.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account  | 400.0 |        |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |       | 400.0  |
# --- Undo transaction ---
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account  | 400.0 |        |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |       | 400.0  |
      | LIABILITY | 145023       | Suspense/Clearing account  |       | 400.0  |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud | 400.0 |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93952
  Scenario: Verify Working Capital charge-off accounting - UC12: fee charge adjustment spilling to principal after charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 100.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Repayment ---
    And Customer makes repayment on "10 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | INCOME    | 744008       | Recoveries                |       | 50.0   |
# --- Charge adjustment ---
    And Admin makes a charge adjustment for the last added fee charge with 100.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 100.0 |        |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
# --- Revert charge adjustment ---
    When Admin reverts the last charge adjustment on working capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 100.0 |        |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
      | INCOME | 404007       | Fee Income     |       | 100.0  |
      | INCOME | 404008       | Fee Charge Off | 50.0  |        |
      | INCOME | 404008       | Fee Charge Off | 50.0  |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93953
  Scenario: Verify Working Capital charge-off accounting - UC13: repayment after charge-off with fees, penalties and overpayment
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 30.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Repayment ---
    And Customer makes repayment on "10 January 2026" with 10000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 10000.0 |        |
      | INCOME    | 744008       | Recoveries                |         | 9000.0 |
      | INCOME    | 744008       | Recoveries                |         | 50.0   |
      | INCOME    | 744008       | Recoveries                |         | 30.0   |
      | LIABILITY | 245000       | Other Credit Liability    |         | 920.0  |
# --- Undo transaction ---
    When Customer undo "1"th "Repayment" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 10000.0 |        |
      | INCOME    | 744008       | Recoveries                |         | 9000.0 |
      | INCOME    | 744008       | Recoveries                |         | 50.0   |
      | INCOME    | 744008       | Recoveries                |         | 30.0   |
      | LIABILITY | 245000       | Other Credit Liability    |         | 920.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |         | 10000.0 |
      | INCOME    | 744008       | Recoveries                | 9000.0  |        |
      | INCOME    | 744008       | Recoveries                | 50.0    |        |
      | INCOME    | 744008       | Recoveries                | 30.0    |        |
      | LIABILITY | 245000       | Other Credit Liability    | 920.0   |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93954
  Scenario: Verify Working Capital charge-off accounting - UC14: amortization after fraud charge-off credits fraud expense
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
# --- Discount fee ---
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
# --- Fraud flag ---
    When Admin sets the fraud flag of the Working Capital loan to true
    And Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "02 January 2026"
# --- Repayment ---
    And Customer makes repayment on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | INCOME    | 744008       | Recoveries                |       | 50.0   |
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue  | 1000.0 |        |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |        | 1000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 January 2026"

  @TestRailId:C93955
  Scenario: Verify Working Capital charge-off accounting - UC15: discount fee adjustment on or after the charge-off date is rejected
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
# --- Discount fee ---
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment ---
    And Customer makes repayment on "05 January 2026" with 50.0 transaction amount on Working Capital loan
    And Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "08 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 08 January 2026 | Charge-off                | 9950.0            | 9950.0           | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization | 990.39            |                  |                   |                       | false    |
# --- Discount fee adjustment dated on the charge-off date is rejected: charge-off is a terminal write-off, so only a
    Then Adding Discount fee adjustment with "500" amount on transaction date "08 January 2026" on Working Capital loan account for last discount results an error with the following data:
      | httpCode | message                          |
      | 403      | error.msg.wc.loan.is.charged.off |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C93956
  Scenario: Verify Working Capital charge-off accounting - UC16: CBR after charge-off keeps regular overpayment accounting
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Repayment ---
    And Customer makes repayment on "10 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9100.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 9000.0 |
      | LIABILITY | 245000       | Other Credit Liability    |        | 100.0  |
# --- Credit balance refund ---
    And Customer makes credit balance refund on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 245000       | Other Credit Liability    | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C93957
  Scenario: Verify Working Capital charge-off accounting - UC17: goodwill after charge-off with fees, penalties and overpayment
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 30.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Goodwill credit ---
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 10500.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit   | Credit |
      | EXPENSE   | 744003       | Goodwill Expense Account | 10420.0 |        |
      | INCOME    | 404008       | Fee Charge Off           | 50.0    |        |
      | INCOME    | 404008       | Fee Charge Off           | 30.0    |        |
      | INCOME    | 744008       | Recoveries               |         | 9000.0 |
      | INCOME    | 744008       | Recoveries               |         | 50.0   |
      | INCOME    | 744008       | Recoveries               |         | 30.0   |
      | LIABILITY | 245000       | Other Credit Liability   |         | 1420.0 |
# --- Undo transaction ---
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit   | Credit |
      | EXPENSE   | 744003       | Goodwill Expense Account | 10420.0 |        |
      | INCOME    | 404008       | Fee Charge Off           | 50.0    |        |
      | INCOME    | 404008       | Fee Charge Off           | 30.0    |        |
      | INCOME    | 744008       | Recoveries               |         | 9000.0 |
      | INCOME    | 744008       | Recoveries               |         | 50.0   |
      | INCOME    | 744008       | Recoveries               |         | 30.0   |
      | LIABILITY | 245000       | Other Credit Liability   |         | 1420.0 |
      | EXPENSE   | 744003       | Goodwill Expense Account |         | 10420.0 |
      | INCOME    | 404008       | Fee Charge Off           |         | 50.0   |
      | INCOME    | 404008       | Fee Charge Off           |         | 30.0   |
      | INCOME    | 744008       | Recoveries               | 9000.0  |        |
      | INCOME    | 744008       | Recoveries               | 50.0    |        |
      | INCOME    | 744008       | Recoveries               | 30.0    |        |
      | LIABILITY | 245000       | Other Credit Liability   | 1420.0  |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93958
  Scenario: Verify Working Capital charge-off accounting - UC18: fee charge adjustment with overpayment after charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 100.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Repayment ---
    And Customer makes repayment on "10 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9100.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 9000.0 |
      | INCOME    | 744008       | Recoveries                |        | 100.0  |
# --- Charge adjustment ---
    And Admin makes a charge adjustment for the last added fee charge with 100.0 amount on working capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name           | Debit | Credit |
      | INCOME    | 404007       | Fee Income             | 100.0 |        |
      | LIABILITY | 245000       | Other Credit Liability |       | 100.0  |
# --- Revert charge adjustment ---
    When Admin reverts the last charge adjustment on working capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name           | Debit | Credit |
      | INCOME    | 404007       | Fee Income             | 100.0 |        |
      | LIABILITY | 245000       | Other Credit Liability |       | 100.0  |
      | INCOME    | 404007       | Fee Income             |       | 100.0  |
      | LIABILITY | 245000       | Other Credit Liability | 100.0 |        |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C93959
  Scenario: Verify Working Capital charge-off accounting - UC19: payout refund after charge-off with fees, penalties and overpayment
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 25.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
# --- Payout refund ---
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 9140.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9140.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 9000.0 |
      | INCOME    | 404008       | Fee Charge Off            |        | 15.0   |
      | INCOME    | 404008       | Fee Charge Off            |        | 25.0   |
      | LIABILITY | 245000       | Other Credit Liability    |        | 100.0  |
# --- Undo transaction ---
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9140.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 9000.0 |
      | INCOME    | 404008       | Fee Charge Off            |        | 15.0   |
      | INCOME    | 404008       | Fee Charge Off            |        | 25.0   |
      | LIABILITY | 245000       | Other Credit Liability    |        | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9140.0 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 9000.0 |        |
      | INCOME    | 404008       | Fee Charge Off            | 15.0   |        |
      | INCOME    | 404008       | Fee Charge Off            | 25.0   |        |
      | LIABILITY | 245000       | Other Credit Liability    | 100.0  |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93960
  Scenario: Verify Working Capital charge-off accounting - UC20: partial repayment before charge-off writes off remaining principal only
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment ---
    And Customer makes repayment on "05 January 2026" with 3000.0 transaction amount on Working Capital loan
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93961
  Scenario: Verify Working Capital charge-off accounting - UC21: future charge-off date is rejected (Negative)
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Charging off the Working Capital loan on "11 January 2026" results an error with the following data:
      | httpCode | errorMessage |
      | 400      | future.date  |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C93962
  Scenario: Verify Working Capital charge-off accounting - UC22: second charge-off attempt is rejected (Negative)
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Charging off the Working Capital loan on "10 January 2026" results an error with the following data:
      | httpCode | errorMessage     |
      | 400      | already.charged.off |
    And Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C94004
  Scenario: Verify Working Capital charge-off accounting - UC23: NONE accounting rule produces no journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type | Account code | Account name | Debit | Credit |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C94033
  Scenario: Verify Working Capital charge-off accounting - UC24: backdated repayment before charge-off keeps regular JE and restates charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated repayment before charge-off ---
    And Customer makes repayment on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94034
  Scenario: Verify Working Capital charge-off accounting - UC25: backdated full repayment before charge-off reverses charge-off and lifts flag
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated full repayment before charge-off ---
    And Customer makes repayment on "10 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C94035
  Scenario: Verify Working Capital charge-off accounting - UC26: undo repayment before charge-off restates charge-off up
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment before charge-off ---
    And Customer makes repayment on "05 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Undo repayment that sits before charge-off ---
    When Customer undo "1"th "Repayment" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 3000.0 |
      | ASSET     | 112601       | Loans Receivable          | 3000.0 |        |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | true     |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 6000.0 |
      | ASSET   | 112601       | Loans Receivable     | 6000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94036
  Scenario: Verify Working Capital charge-off accounting - UC27: same-day repayment after charge-off posts to recoveries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Same-day repayment after charge-off ---
    And Customer makes repayment on "15 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Repayment    | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "15 January 2026"

  @TestRailId:C94037
  Scenario: Verify Working Capital charge-off accounting - UC28: undo earlier repayment after charge-off leaves charge-off amount unchanged
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment after charge-off ---
    And Customer makes repayment on "16 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "16 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 1000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 16 January 2026 | Repayment    | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment after charge-off ---
    And Customer makes repayment on "20 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 16 January 2026 | Repayment    | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 20 January 2026 | Repayment    | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Undo earlier post-charge-off repayment; charge-off stays in prefix ---
    When Customer undo "1"th "Repayment" transaction made on "16 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "16 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
      | INCOME    | 744008       | Recoveries                | 1000.0 |        |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 16 January 2026 | Repayment    | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |
      | 20 January 2026 | Repayment    | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94038
  Scenario: Verify Working Capital charge-off accounting - UC29: backdated repayment before charge-off restates charge-off and keeps later recovery JE
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment after charge-off ---
    And Customer makes repayment on "20 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 20 January 2026 | Repayment    | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Backdated repayment before charge-off; later repayment stays after charge-off ---
    And Customer makes repayment on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
      | 20 January 2026 | Repayment    | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94039
  Scenario: Verify Working Capital charge-off accounting - UC30: backdated repayment before charge-off with fee restates charge-off fee portion
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9050.0            | 9000.0           | 50.0              | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated repayment covers the fee before charge-off ---
    And Customer makes repayment on "10 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 50.0   |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |        | 9000.0 |
      | INCOME  | 404008       | Fee Charge Off          |        | 50.0   |
      | ASSET   | 112601       | Loans Receivable        | 9000.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable | 50.0   |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94040
  Scenario: Verify Working Capital charge-off accounting - UC31: backdated goodwill credit before charge-off keeps regular JE and restates charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated goodwill credit before charge-off ---
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit  | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 3000.0 |        |
      | ASSET   | 112601       | Loans Receivable         |        | 3000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off      | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94041
  Scenario: Verify Working Capital charge-off accounting - UC32: backdated payout refund before charge-off keeps regular JE and restates charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated payout refund before charge-off ---
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off    | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94042
  Scenario: Verify Working Capital charge-off accounting - UC33: backdated full repayment before charge-off restates later recovery to overpayment
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment after charge-off ---
    And Customer makes repayment on "20 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 1000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 20 January 2026 | Repayment    | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Backdated full repayment lifts charge-off; later repayment becomes overpayment ---
    And Customer makes repayment on "10 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |
      | 20 January 2026 | Repayment    | 1000.0            | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
      | INCOME    | 744008       | Recoveries                | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | LIABILITY | 245000       | Other Credit Liability    |        | 1000.0 |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "20 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C94043
  Scenario: Verify Working Capital charge-off accounting - UC34: undo backdated repayment before charge-off restates charge-off back up
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated repayment before charge-off ---
    And Customer makes repayment on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Undo that backdated repayment ---
    When Customer undo "1"th "Repayment" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 3000.0 |
      | ASSET     | 112601       | Loans Receivable          | 3000.0 |        |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | true     |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 6000.0 |
      | ASSET   | 112601       | Loans Receivable     | 6000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94044
  Scenario: Verify Working Capital charge-off accounting - UC35: two successive backdated repayments before charge-off restate charge-off twice
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- First backdated repayment ---
    And Customer makes repayment on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Second backdated repayment ---
    And Customer makes repayment on "12 January 2026" with 2000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "12 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 2000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 2000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 2000.0            | 2000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 4000.0            | 4000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 6000.0 |
      | ASSET   | 112601       | Loans Receivable     | 6000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 4000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 4000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94045
  Scenario: Verify Working Capital charge-off accounting - UC36: backdated repayment before charge-off restates later full recovery into recovery plus overpayment
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment after charge-off ---
    And Customer makes repayment on "20 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 9000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 20 January 2026 | Repayment    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Backdated repayment before charge-off changes later allocation ---
    And Customer makes repayment on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
      | 20 January 2026 | Repayment    | 9000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 9000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |
      | INCOME    | 744008       | Recoveries                | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |
      | INCOME    | 744008       | Recoveries                |        | 6000.0 |
      | LIABILITY | 245000       | Other Credit Liability    |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "20 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C94046
  Scenario: Verify Working Capital charge-off accounting - UC37: backdated overpayment before charge-off reverses charge-off and books overpayment on the backdated repayment
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated overpayment before charge-off ---
    And Customer makes repayment on "10 January 2026" with 10000.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 10000.0           | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 10000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |         | 9000.0 |
      | LIABILITY | 245000       | Other Credit Liability    |         | 1000.0 |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "20 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C94047
  Scenario: Verify Working Capital charge-off accounting - UC38: backdated repayment before fraud charge-off restates fraud expense
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
# --- Fraud flag ---
    When Admin sets the fraud flag of the Working Capital loan to true
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable           |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated repayment before charge-off ---
    And Customer makes repayment on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable           |        | 9000.0 |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable           | 9000.0 |        |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable           |        | 6000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94048
  Scenario: Verify Working Capital charge-off accounting - UC39: undo charge-off is still allowed after a backdated repayment before charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated repayment is earlier by date, so charge-off remains the last user transaction ---
    And Customer makes repayment on "10 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    When Admin undoes the charge-off on the Working Capital loan
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 6000.0            | 6000.0           | 0.0               | 0.0                   | true     |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9000.0 |
      | ASSET   | 112601       | Loans Receivable     | 9000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 6000.0 |
      | ASSET   | 112601       | Loans Receivable     | 6000.0 |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94049
  Scenario: Verify Working Capital charge-off accounting - UC40: undo goodwill credit before charge-off restates charge-off up
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Goodwill credit before charge-off ---
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit  | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 3000.0 |        |
      | ASSET   | 112601       | Loans Receivable         |        | 3000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Goodwill Credit | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit  | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 3000.0 |        |
      | ASSET   | 112601       | Loans Receivable         |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Goodwill Credit | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off      | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Undo goodwill credit that sits before charge-off ---
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit  | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 3000.0 |        |
      | ASSET   | 112601       | Loans Receivable         |        | 3000.0 |
      | EXPENSE | 744003       | Goodwill Expense Account |        | 3000.0 |
      | ASSET   | 112601       | Loans Receivable         | 3000.0 |        |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Goodwill Credit | 3000.0            | 3000.0           | 0.0               | 0.0                   | true     |
      | 15 January 2026 | Charge-off      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 6000.0 |
      | ASSET   | 112601       | Loans Receivable     | 6000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94050
  Scenario: Verify Working Capital charge-off accounting - UC41: undo payout refund before charge-off restates charge-off up
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Payout refund before charge-off ---
    And Customer makes "PAYOUT_REFUND" transaction on "05 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off    | 6000.0            | 6000.0           | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Undo payout refund that sits before charge-off ---
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 3000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 3000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 3000.0 |
      | ASSET     | 112601       | Loans Receivable          | 3000.0 |        |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 3000.0            | 3000.0           | 0.0               | 0.0                   | true     |
      | 15 January 2026 | Charge-off    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 6000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 6000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 6000.0 |
      | ASSET   | 112601       | Loans Receivable     | 6000.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94051
  Scenario: Verify Working Capital charge-off accounting - UC42: backdated repayment before charge-off covers penalty only and leaves fee on charge-off
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 30.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | INCOME  | 404008       | Fee Charge Off          | 30.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 30.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 05 January 2026 | Accrual      | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
      | 15 January 2026 | Charge-off   | 9080.0            | 9000.0           | 50.0              | 30.0                  | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated repayment covers penalty first ---
    And Customer makes repayment on "10 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 30.0  |        |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | INCOME  | 404008       | Fee Charge Off          | 30.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 30.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |        | 9000.0 |
      | INCOME  | 404008       | Fee Charge Off          |        | 50.0   |
      | INCOME  | 404008       | Fee Charge Off          |        | 30.0   |
      | ASSET   | 112601       | Loans Receivable        | 9000.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable | 50.0   |        |
      | ASSET   | 112603       | Interest/Fee Receivable | 30.0   |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 05 January 2026 | Accrual      | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
      | 10 January 2026 | Repayment    | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
      | 15 January 2026 | Charge-off   | 9050.0            | 9000.0           | 50.0              | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94052
  Scenario: Verify Working Capital charge-off accounting - UC43: fee charge adjustment before charge-off keeps receivable JE and after charge-off posts to charge-off income
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 100.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge adjustment before charge-off ---
    And Admin makes a charge adjustment for the last added fee charge with 50.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "08 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 08 January 2026 | Charge Adjustment | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "08 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 08 January 2026 | Charge Adjustment | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 15 January 2026 | Charge-off        | 9050.0            | 9000.0           | 50.0              | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge adjustment after charge-off ---
    And Admin makes a charge adjustment for the last added fee charge with 50.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 50.0  |        |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 08 January 2026 | Charge Adjustment | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 15 January 2026 | Charge-off        | 9050.0            | 9000.0           | 50.0              | 0.0                   | false    |
      | 20 January 2026 | Charge Adjustment | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94053
  Scenario: Verify Working Capital charge-off accounting - UC44: penalty charge adjustment before charge-off keeps receivable JE and after charge-off posts to charge-off income
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 100.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge adjustment before charge-off ---
    And Admin makes a charge adjustment for the last added penalty charge with 50.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "08 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual           | 100.0             | 0.0              | 0.0               | 100.0                 | false    |
      | 08 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "08 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
    And Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual           | 100.0             | 0.0              | 0.0               | 100.0                 | false    |
      | 08 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 15 January 2026 | Charge-off        | 9050.0            | 9000.0           | 0.0               | 50.0                  | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge adjustment after charge-off ---
    And Admin makes a charge adjustment for the last added penalty charge with 50.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 50.0  |        |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual           | 100.0             | 0.0              | 0.0               | 100.0                 | false    |
      | 08 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 15 January 2026 | Charge-off        | 9050.0            | 9000.0           | 0.0               | 50.0                  | false    |
      | 20 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C94054
  Scenario: Verify Working Capital charge-off accounting - UC45: backdated repayment before charge-off lifts flag and restates post-charge-off fee charge adjustment to regular JE
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    And Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off ---
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 9050.0            | 9000.0           | 50.0              | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge adjustment after charge-off ---
    And Admin makes a charge adjustment for the last added fee charge with 50.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name   | Debit | Credit |
      | INCOME | 404007       | Fee Income     | 50.0  |        |
      | INCOME | 404008       | Fee Charge Off |       | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual           | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 15 January 2026 | Charge-off        | 9050.0            | 9000.0           | 50.0              | 0.0                   | false    |
      | 20 January 2026 | Charge Adjustment | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Backdated repayment before charge-off lifts charge-off ---
    And Customer makes repayment on "10 January 2026" with 9050.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9050.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 50.0   |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 50.0   |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 50.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |        | 9000.0 |
      | INCOME  | 404008       | Fee Charge Off          |        | 50.0   |
      | ASSET   | 112601       | Loans Receivable        | 9000.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable | 50.0   |        |
    And Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name            | Debit | Credit |
      | INCOME    | 404007       | Fee Income              | 50.0  |        |
      | INCOME    | 404008       | Fee Charge Off          |       | 50.0   |
      | INCOME    | 404007       | Fee Income              |       | 50.0   |
      | INCOME    | 404008       | Fee Charge Off          | 50.0  |        |
      | INCOME    | 404007       | Fee Income              | 50.0  |        |
      | LIABILITY | 245000       | Other Credit Liability  |       | 50.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Accrual           | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 9050.0            | 9000.0           | 50.0              | 0.0                   | false    |
      | 15 January 2026 | Charge-off        | 9050.0            | 9000.0           | 50.0              | 0.0                   | true     |
      | 20 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 0.0                   | false    |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "20 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C94055
  Scenario: Verify Working Capital charge-off accounting - UC46: backdated discount fee adjustment before charge-off restates charge-off expense
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
# --- Discount fee ---
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Repayment before charge-off ---
    And Customer makes repayment on "05 January 2026" with 50.0 transaction amount on Working Capital loan
    And Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
# --- Charge-off: the full unreleased discount (1000 - 9.61 = 990.39) moves to charge-off expense in one shot ---
    And Admin charges off the Working Capital loan on "08 January 2026"
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "08 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 990.39 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 990.39 |
    And Admin adds Discount fee adjustment with "500" amount on transaction date "07 January 2026" on Working Capital loan account for last discount
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "08 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 990.39 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 990.39 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 990.39 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 990.39 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue | 490.39 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 490.39 |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 07 January 2026 | Discount Fee Adjustment   | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Charge-off                | 9450.0            | 9450.0           | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization | 490.39            |                  |                   |                       | false    |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C94065
  Scenario: Working Capital loan raises Fraud Changed business event when the fraud flag is set - UC47
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the fraud flag of the Working Capital loan to true
    Then a Working Capital Loan Fraud Changed business event is raised
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 January 2026"

  @TestRailId:C99203
  Scenario: Verify Working Capital charge-off accounting: undo charge-off with discount fee reverses journal entries - UC48
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 1000     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off --- #
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable     |         | 10000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
# --- Undo charge-off ---
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin undoes the charge-off on the Working Capital loan
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable     |         | 10000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |         | 10000.0 |
      | ASSET   | 112601       | Loans Receivable     | 10000.0 |         |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 1000.0 |        |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | true     |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "11 January 2026"

  @TestRailId:C98204
  Scenario: Verify Working Capital charge-off accounting: undo charge-off with discount fee reverses journal entries with fraud expense - UC49
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 1000     |
# --- Fraud flag --- #
    When Admin sets the fraud flag of the Working Capital loan to true
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off --- #
    And Admin charges off the Working Capital loan on "10 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit   | Credit  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable           |         | 10000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue  | 1000.0 |        |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |        | 1000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "10 January 2026"
# --- Undo charge-off --- #
    When Admin undoes the charge-off on the Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit   | Credit  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable           |         | 10000.0 |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud |         | 10000.0 |
      | ASSET   | 112601       | Loans Receivable           | 10000.0 |         |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue  | 1000.0 |        |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue  |        | 1000.0 |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | true     |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C98205
  Scenario: Verify Working Capital charge-off accounting: charge-off after discount fee adjustment - UC50
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
# --- Discount fee adjustment --- #
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "250" amount on transaction date "05 January 2026" on Working Capital loan account for last discount
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
    And Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off --- #
    And Admin charges off the Working Capital loan on "08 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "08 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9750.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9750.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "08 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 750.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 750.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Charge-off                | 9750.0            | 9750.0           | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization | 750.0             |                  |                   |                       | false    |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "08 January 2026"
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C98206
  Scenario: Verify Working Capital charge-off accounting: charge-off after discount fee adjustment with following discount fee adj undo - UC51
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
# --- Discount fee adjustment --- #
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "250" amount on transaction date "05 January 2026" on Working Capital loan account for last discount
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
    And Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off --- #
    And Admin charges off the Working Capital loan on "08 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "08 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9750.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9750.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "08 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 750.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 750.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Charge-off                | 9750.0            | 9750.0           | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization | 750.0             |                  |                   |                       | false    |
# --- undo last discount fee adjustment transaction --- #
# The loan is charged off, so undoing the adjustment reprocesses immediately: the charge-off transaction and its
# linked final discount-fee amortization are both replayed in place against the restored (fully undone) discount
# pool, rather than being left stranded at the smaller, adjusted amounts until some later transaction happens to
# catch them up.
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal |
      | 1000.0   | 10000.0   |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "08 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9750.0  |         |
      | ASSET   | 112601       | Loans Receivable     |         | 9750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |         | 9750.0  |
      | ASSET   | 112601       | Loans Receivable     | 9750.0  |         |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable     |         | 10000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "08 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 750.0  |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 750.0  |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 750.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 750.0  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | true     |
      | 08 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    And Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "09 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | true     |
      | 08 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 09 January 2026 | Repayment                 | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |

  @TestRailId:C98221
  Scenario: Verify Working Capital charge-off accounting: backdated full repayment before charge-off with an active discount pool lifts charge-off and reverses the final discount fee amortization - UC52
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 1000     |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off --- #
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable     |         | 10000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "15 January 2026"
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated full repayment before charge-off lifts the flag and must also reverse the final amortization --- #
    And Customer makes repayment on "10 January 2026" with 10000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit  |
      | LIABILITY | 145023       | Suspense/Clearing account | 10000.0 |         |
      | ASSET     | 112601       | Loans Receivable          |         | 10000.0 |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable     |         | 10000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |         | 10000.0 |
      | ASSET   | 112601       | Loans Receivable     | 10000.0 |         |
    And Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 1000.0 |        |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | INCOME    | 404000       | Interest Income           |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "10 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment                 | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 15 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | true     |
      | 15 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | true     |
    Then Working capital loan account has the correct data:
      | chargedOff |
      | false      |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C98222
  Scenario: Verify Working Capital charge-off accounting: backdated discount fee adjustment that fully covers discount fee before charge-off restates charge-off expense - UC53
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off: the full unreleased discount (1000) moves to charge-off expense in one shot --- #
    And Admin charges off the Working Capital loan on "15 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable        |         | 10000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "15 January 2026"
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Backdated Discount Fee Adjustment before charge-off --- #
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "10 January 2026" on Working Capital loan account for last discount
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Adjustment   | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off                | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | true     |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable        |         | 10000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0  |         |
      | ASSET   | 112601       | Loans Receivable        |         | 9000.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |         | 10000.0 |
      | ASSET   | 112601       | Loans Receivable        | 10000.0 |         |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 1000.0 |        |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
# --- undo discount fee adjustment trn --- #
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Adjustment   | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |
      | 15 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable        |         | 10000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0  |         |
      | ASSET   | 112601       | Loans Receivable        |         | 9000.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable        |         | 10000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |         | 10000.0 |
      | ASSET   | 112601       | Loans Receivable        | 10000.0 |         |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |         | 9000.0  |
      | ASSET   | 112601       | Loans Receivable        | 9000.0  |         |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |         | 1000.0 |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

