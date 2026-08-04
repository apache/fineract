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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"

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
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 9.61   |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | INCOME    | 744008       | Recoveries                |       | 50.0   |
# --- Undo transaction ---
    When Customer undo "1"th "Repayment" transaction made on "02 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | INCOME    | 744008       | Recoveries                |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 50.0   |
      | INCOME    | 744008       | Recoveries                | 50.0  |        |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
      | Type      | Account code | Account name               | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue  | 9.61  |        |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |       | 9.61   |
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C93955
  Scenario: Verify Working Capital charge-off accounting - UC15: amortization adjustment after charge-off debits charge-off expense
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
# --- Discount fee adjustment ---
    And Admin adds Discount fee adjustment with "500" amount on transaction date "08 January 2026" on Working Capital loan account for last discount
    And Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "08 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 4.47  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.47   |
    Then Admin closes the Working Capital loan with a full repayment on "09 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
    And Admin closes the Working Capital loan with a full repayment on "10 January 2026"

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
