@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalLoanChargeWaiverAccountingFeature
Feature: Working Capital Loan Charge Waiver Accounting

  @TestRailId:C102471
  Scenario: Waiving an accrued fee charge books Write-off Expense against Fees Receivable - UC1
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    When Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Accrual            | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 15 January 2026 | Waive loan charges | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 100.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 100.0  |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "16 January 2026"

  @TestRailId:C102472
  Scenario: Waiving an accrued penalty charge books against Penalties Receivable - UC2
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "15 January 2026" due date and 50.0 transaction amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "50.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Accrual            | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 15 January 2026 | Waive loan charges | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 50.0  |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 50.0   |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "16 January 2026"

  @TestRailId:C102473
  Scenario: Waiving a charge that was never accrued books no journal entries - UC3
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 100.0 transaction amount
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
# There is no receivable behind income that was never recognized, so crediting one would drive it negative.
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "10 January 2026" which has the following Journal entries:
      | Type | Account code | Account name | Debit | Credit |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C102474
  Scenario: Undoing a charge waiver reverses its journal entries - UC4
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    When Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
    And Working Capital Loan Transactions tab has a reversed "WAIVE_CHARGES" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 100.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 100.0  |
      | EXPENSE | e4           | Written off             |       | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable | 100.0 |        |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "16 January 2026"

  @TestRailId:C102475
  Scenario: A waiver dated before the charge-off credits the receivable and takes the fee back out of the charge-off - UC5
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
# The waiver is dated on the charge due date, so it sorts before the charge-off even though it is booked after it.
# As of that date the borrower no longer owed the fee, so the charge-off must stop writing it off.
    When Admin sets the business date to "18 January 2026"
    And Admin charges off the Working Capital loan on "18 January 2026"
    When Admin sets the business date to "20 January 2026"
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 100.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 100.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Accrual            | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 15 January 2026 | Waive loan charges | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 18 January 2026 | Charge-off         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"

  @TestRailId:C102476
  Scenario: Waiving the remainder of a partially paid charge before it is accrued accrues only the paid portion - UC6
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
# The charge payment credits the receivable whether or not an accrual preceded it, leaving it at -40. The later
# accrual has to recognize exactly the paid part to net that back to zero.
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment  | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 60.0              | 0.0              | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Accrual            | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404007       | Fee Income              |       | 40.0   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "11 January 2026"

  @TestRailId:C102477
  Scenario: Undoing a charge waiver tops the reduced accrual back up to the full charge amount - UC7
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
# The restored part of the charge must still be able to reach the income statement. A boolean "already accrued"
# guard would silence every later accrual and leave it unrecognized forever.
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment  | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 60.0              | 0.0              | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Accrual            | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual            | 60.0              | 0.0              | 60.0              | 0.0                   | false    |
    And Working Capital Loan Transactions tab has 2 "ACCRUAL" transactions with date "10 January 2026" which have the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404007       | Fee Income              |       | 40.0   |
      | ASSET  | 112603       | Interest/Fee Receivable | 60.0  |        |
      | INCOME | 404007       | Fee Income              |       | 60.0   |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 60.0            | 40.0     | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "12 January 2026"

  @TestRailId:C102478
  Scenario: Re-waiving a charge before the accrual catches up books no journal entries - UC8
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
# Only 40 was ever accrued, and the payment already consumed all of it, so the 60 being waived again has no
# receivable behind it - a flat "accrued or not" rule would credit Fees Receivable against nothing.
    When Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "10 January 2026" which has the following Journal entries:
      | Type | Account code | Account name | Debit | Credit |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "11 January 2026"

  @TestRailId:C102479
  Scenario: A waiver dated after the charge-off leaves the fee the charge-off wrote off in place - UC9
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "25 January 2026" due date and 100.0 transaction amount
# The charge is outstanding from the day it is added, so the charge-off writes it off before it has been accrued.
    When Admin sets the business date to "18 January 2026"
    And Admin charges off the Working Capital loan on "18 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "18 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 100.0  |        |
      | ASSET   | 112601       | Loans Receivable        |        | 9000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 100.0  |
# The accrual lands after the charge-off and debits the receivable that charge-off had already credited, so the two
# net out and nothing is left stranded on the books.
    When Admin sets the business date to "26 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "25 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    When Admin waives the last added charge on working capital loan
    Then Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "25 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name   | Debit | Credit |
      | EXPENSE | e4           | Written off    | 100.0 |        |
      | INCOME  | 404008       | Fee Charge Off |       | 100.0  |
# Replaying the whole history must not take that fee back out of the charge-off: on 18 January it was still owed.
    When Customer makes repayment on "12 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment          | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 18 January 2026 | Charge-off         | 8100.0            | 8000.0           | 100.0             | 0.0                   | false    |
      | 25 January 2026 | Accrual            | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 25 January 2026 | Waive loan charges | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "26 January 2026"

  @TestRailId:C102480
  Scenario: Undoing a waiver on a charged-off loan gives the fee back to the charge-off - UC10
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 100.0 transaction amount
# Waived before its due date, so nothing was accrued yet and the waiver books no journal entries.
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    When Admin sets the business date to "15 January 2026"
    And Admin charges off the Working Capital loan on "15 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9000.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9000.0 |
# Undoing the waiver puts the fee back into the outstanding, so the charge-off has to cover it after all.
    When Admin sets the business date to "16 January 2026"
    And Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 100.0             | 0.0              | 0.0               | 0.0                   | true     |
      | 15 January 2026 | Charge-off         | 9100.0            | 9000.0           | 100.0             | 0.0                   | false    |
# The receivable the restated charge-off credited is matched by the accrual the undo made due again.
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "20 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
# Waiving again on the charged-off loan credits the charge-off income the charge-off itself debited.
    When Admin waives the last added charge on working capital loan
    Then Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "20 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name   | Debit | Credit |
      | EXPENSE | e4           | Written off    | 100.0 |        |
      | INCOME  | 404008       | Fee Charge Off |       | 100.0  |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "21 January 2026"
