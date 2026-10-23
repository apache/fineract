@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalLoanChargeOffAccountingFeature
Feature: Working Capital Charge-Off Accounting Entries - Part2

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
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "250" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off --- #
    And Admin charges off the Working Capital loan on "03 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9750.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9750.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 750.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 750.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Charge-off                | 9750.0            | 9750.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 750.0             |                  |                   |                       | false    |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "03 January 2026"
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 January 2026"

  @TestRailId:C98206
  Scenario: Verify Working Capital charge-off accounting: charge-off after discount fee adjustment with following discount fee adj undo - UC51
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
# --- Discount fee adjustment --- #
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "250" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off --- #
    And Admin charges off the Working Capital loan on "03 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9750.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9750.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 750.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 750.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Charge-off                | 9750.0            | 9750.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 750.0             |                  |                   |                       | false    |
# --- undo last discount fee adjustment transaction --- #
# The loan is charged off, so undoing the adjustment reprocesses immediately: the charge-off transaction and its
# linked final discount-fee amortization are both replayed in place against the restored (fully undone) discount
# pool, rather than being left stranded at the smaller, adjusted amounts until some later transaction happens to
# catch them up.
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal |
      | 1000.0   | 10000.0   |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9750.0  |         |
      | ASSET   | 112601       | Loans Receivable     |         | 9750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |         | 9750.0  |
      | ASSET   | 112601       | Loans Receivable     | 9750.0  |         |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable     |         | 10000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
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
      | 02 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | true     |
      | 03 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "04 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 250.0             | 250.0            | 0.0               | 0.0                   | true     |
      | 03 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                 | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |

  @TestRailId:C98221
  Scenario: Verify Working Capital charge-off accounting: backdated full repayment before charge-off with an active discount pool lifts charge-off and reverses the final discount fee amortization - UC52
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 1000     |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off --- #
    And Admin charges off the Working Capital loan on "03 January 2026"
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable     |         | 10000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "03 January 2026"
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Backdated full repayment before charge-off lifts the flag and must also reverse the final amortization --- #
    And Customer makes repayment on "02 January 2026" with 10000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit  |
      | LIABILITY | 145023       | Suspense/Clearing account | 10000.0 |         |
      | ASSET     | 112601       | Loans Receivable          |         | 10000.0 |
    And Working Capital Loan Transactions tab has a reversed "CHARGE_OFF" transaction with date "03 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable     |         | 10000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |         | 10000.0 |
      | ASSET   | 112601       | Loans Receivable     | 10000.0 |         |
    And Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 1000.0 |        |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | INCOME    | 404000       | Interest Income           |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "02 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 03 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | true     |
      | 03 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | true     |
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
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# --- Charge-off: the full unreleased discount (1000) moves to charge-off expense in one shot --- #
    And Admin charges off the Working Capital loan on "04 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "04 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable        |         | 10000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
    Then a Working Capital Loan Discount Fee Amortization transaction business event is raised on "04 January 2026"
    Then Working capital loan account has the correct data:
      | chargedOff |
      | true       |
# --- Backdated Discount Fee Adjustment before charge-off --- #
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Adjustment   | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Charge-off                | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | true     |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "04 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit   | Credit  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 10000.0 |         |
      | ASSET   | 112601       | Loans Receivable        |         | 10000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 9000.0  |         |
      | ASSET   | 112601       | Loans Receivable        |         | 9000.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |         | 10000.0 |
      | ASSET   | 112601       | Loans Receivable        | 10000.0 |         |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 1000.0 |        |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_ADJUSTMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
# --- undo discount fee adjustment trn --- #
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Adjustment   | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |
      | 04 January 2026 | Charge-off                | 10000.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "04 January 2026" which has the following Journal entries:
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
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 1000.0 |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_ADJUSTMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2026"

  @TestRailId:C106827
  Scenario: Verify Working Capital charge-off accounting: re-applying a backdated discount fee adjustment after its undo revives the amortization adjustment and books it once
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "05 January 2026"
    And Admin charges off the Working Capital loan on "05 January 2026"
    When Admin sets the business date to "06 January 2026"
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    Then Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | false    |
      | 02 January 2026 | Repayment                            | 50.0              | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 1000.0            | true     |
      | 04 January 2026 | Discount Fee Adjustment              | 1000.0            | false    |
      | 05 January 2026 | Charge-off                           | 8950.0            | false    |
      | 05 January 2026 | Discount Fee Amortization            | 990.39            | true     |
      | 05 January 2026 | Discount Fee Amortization Adjustment | 9.61              | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 9.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 9.61   |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 9.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 9.61   |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 990.39 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 990.39 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 990.39 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 990.39 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue | 990.39 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 990.39 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 990.39 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 990.39 |        |

  @TestRailId:C106652
  Scenario: Verify Working Capital charge-off accounting: same-day discount fee adjustment below realized income posts amortization adjustment - UC54
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    # --- Discount fee adjustment drops net discount below already realized income; no COB before charge-off --- #
    When Admin sets the business date to "04 January 2026"
    And Admin adds Discount fee adjustment with "995" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    And Admin charges off the Working Capital loan on "04 January 2026"
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 4.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.61   |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 995.0             | 995.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Charge-off                           | 8955.0            | 8955.0           | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9005.0    | 50.0               | 5.0            | 0.0              | 0.0               |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9005.0    | 50.0               | 5.0            | 0.0              | 0.0               |
    When Admin undoes the charge-off on the Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 4.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.61   |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 4.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 4.61  |        |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 995.0             | 995.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Charge-off                           | 8955.0            | 8955.0           | 0.0               | 0.0                   | true     |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.61              |                  |                   |                       | true     |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9005.0    | 50.0               | 9.61           | 0.0              | 0.0               |
    Then Admin closes the Working Capital loan with a full repayment on "07 January 2026"

  @TestRailId:C106653
  Scenario: Verify Working Capital charge-off accounting: undo of same-day discount fee adjustment on charged-off loan re-derives the final discount fee correction - UC55
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    # --- Discount fee adjustment drops net discount below already realized income; same-day charge-off posts the -4.61 correction --- #
    When Admin sets the business date to "04 January 2026"
    And Admin adds Discount fee adjustment with "995" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    And Admin charges off the Working Capital loan on "04 January 2026"
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 4.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.61   |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 9005.0    | 50.0               | 5.0            | 0.0              | 0.0               | true       |
    # --- undo the discount fee adjustment: the pool is back to 1000 on a still charged-off loan, so the final correction must be
    # --- re-derived to +990.39 (the -4.61 adjustment reversed, the remaining 990.39 pushed to charge-off expense as in UC51) --- #
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    Then Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 1000.0   | 10000.0   | 50.0               | 1000.0         | 0.0              | 0.0               | true       |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "04 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 8955.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 8955.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 8955.0 |
      | ASSET   | 112601       | Loans Receivable     | 8955.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9950.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9950.0 |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 4.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.61   |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 4.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 4.61  |        |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 990.39 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 990.39 |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 995.0             | 995.0            | 0.0               | 0.0                   | true     |
      | 04 January 2026 | Charge-off                           | 9950.0            | 9950.0           | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.61              |                  |                   |                       | true     |
      | 04 January 2026 | Discount Fee Amortization            | 990.39            |                  |                   |                       | false    |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "04 January 2026"

  @TestRailId:C106654
  Scenario: Verify Working Capital charge-off accounting: backdated discount fee adjustment below realized income on charged-off loan posts the negative final correction - UC56
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    # --- Charge-off first: the full unreleased discount (1000 - 9.61 = 990.39) moves to charge-off expense --- #
    When Admin sets the business date to "04 January 2026"
    And Admin charges off the Working Capital loan on "04 January 2026"
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 990.39 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 990.39 |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 10000.0   | 50.0               | 1000.0         | 0.0              | 0.0               | true       |
    # --- Backdated discount fee adjustment (before the charge-off date) drops net discount to 5.00 while 9.61 is already realized:
    # --- the reprocess must replace the +990.39 final amortization with a -4.61 correction, not just reverse it --- #
    When Admin sets the business date to "05 January 2026"
    And Admin adds Discount fee adjustment with "995" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    Then Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 9005.0    | 50.0               | 5.0            | 0.0              | 0.0               | true       |
    Then Working Capital Loan Transactions tab has a "CHARGE_OFF" transaction with date "04 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 9950.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 9950.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt |        | 9950.0 |
      | ASSET   | 112601       | Loans Receivable     | 9950.0 |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 8955.0 |        |
      | ASSET   | 112601       | Loans Receivable     |        | 8955.0 |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 990.39 |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 990.39 |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 990.39 |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 990.39 |        |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 4.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.61   |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment              | 995.0             | 995.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Charge-off                           | 8955.0            | 8955.0           | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization            | 990.39            |                  |                   |                       | true     |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.61              |                  |                   |                       | false    |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 9005.0    | 50.0               | 5.0            | 0.0              | 0.0               | true       |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "07 January 2026"

  @TestRailId:C106730
  Scenario: Verify Working Capital charge-off accounting: backdated discount fee adjustment on a re-charged-off loan without a linked final amortization posts the correction to charge-off expense and undo restores it - UC57
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- First charge-off: the unreleased discount 990.39 moves to charge-off expense --- #
    When Admin sets the business date to "04 January 2026"
    And Admin charges off the Working Capital loan on "04 January 2026"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 10000.0   | 50.0               | 1000.0         | 0.0              | 0.0               | true       |
    # --- Backdated payoff lifts the charge-off and closes the loan; the whole discount is realized as income --- #
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "03 January 2026" with 9950 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 10000.0   | 10000.0            | 1000.0         | 0.0              | 0.0               | false      |
    # --- Undo the payoff and charge off again: deferred income is already 0, so no final amortization is linked to this charge-off --- #
    When Customer undo "1"th "Repayment" transaction made on "03 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Admin charges off the Working Capital loan on "05 January 2026"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 10000.0   | 50.0               | 1000.0         | 0.0              | 0.0               | true       |
    # --- Backdated discount fee adjustment: realized income must drop to the net discount even without a linked final amortization --- #
    And Admin adds Discount fee adjustment with "100" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    Then Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 9900.0    | 50.0               | 900.0          | 0.0              | 0.0               | true       |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 100.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 100.0  |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                            | 9950.0            | 9950.0           | 0.0               | 0.0                   | true     |
      | 03 January 2026 | Discount Fee Amortization            | 990.39            |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment              | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Charge-off                           | 9950.0            | 9950.0           | 0.0               | 0.0                   | true     |
      | 04 January 2026 | Discount Fee Amortization            | 990.39            |                  |                   |                       | true     |
      | 05 January 2026 | Charge-off                           | 9850.0            | 9850.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization Adjustment | 100.0             |                  |                   |                       | false    |
    # --- Later COB runs change nothing on the charged-off loan --- #
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 9900.0    | 50.0               | 900.0          | 0.0              | 0.0               | true       |
    # --- Undo of the adjustment restores the previous state --- #
    When Admin undo the Discount fee adjustment with "100.0" amount on Working Capital loan account
    Then Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount | chargedOff |
      | 10000.0   | 50.0               | 1000.0         | 0.0              | 0.0               | true       |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 100.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 100.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 100.0  |
      | LIABILITY | 240005       | Deferred Interest Revenue | 100.0 |        |
