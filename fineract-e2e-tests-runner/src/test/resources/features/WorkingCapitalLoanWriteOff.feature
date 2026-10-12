@WorkingCapital
@WorkingCapitalLoanWriteOffFeature
Feature: Working Capital Loan Write-off

  @TestRailId:С94007
  Scenario: Verify Working Capital Write-off: write-off on an accounting-enabled product zeroes balances and closes the loan as written-off - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then a Working Capital Loan Write Off transaction business event is raised with "100.0" EUR amount
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    Then Working Capital Loan Transactions tab has a "WRITE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 100.0  |
      | EXPENSE | e4           | Written off                | 100.0 |        |

  @TestRailId:С94008
  Scenario: Verify Working Capital Write-off: undo write-off on an accounting-enabled product reopens the loan and restores the balance - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    Then a Working Capital Loan Undo Write Off transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    Then Working Capital Loan Transactions tab has a reversed "WRITE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 100.0  |
      | EXPENSE | e4           | Written off                | 100.0  |        |
      | ASSET   | 112601       | Loans Receivable           | 100.0  |        |
      | EXPENSE | e4           | Written off                |        | 100.0  |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:С94009
  Scenario: Verify Working Capital Write-off: write-off on a product without accounting enabled posts and reverses cleanly - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:С94010
  Scenario: Verify Working Capital Write-off: write-off within external-id on an accounting-enabled product posts and reverses cleanly - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026" with external-id
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan with external-id
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:С94011
  Scenario: Verify Working Capital Write-off: backdated write-off on an accounting-enabled product posts and reverses cleanly - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "10 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:С94012
  Scenario: Verify Working Capital Write-off: write-off on an accounting-enabled product with discount and repayment posts and reverses cleanly - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    And Working Capital Loan has transactions:
      | transactionDate | type           | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee   | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
    And Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type           | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee   | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment      | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee           | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment              | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 62.0              | 62.0             | 0.0               | 0.0                   | false    |
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "62.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee           | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment              | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 62.0              | 62.0             | 0.0               | 0.0                   | true     |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C94013
  Scenario: Verify Working Capital Write-off: future-date write-off failed with an error - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Initiating a write-off the Working Capital loan on "25 January 2026" results an error with the following data:
      | HTTP response code | Error message            |
      | 400                | cannot.be.a.future.date. |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C94014
  Scenario: Verify Working Capital Write-off: write-off failed on already closed loan - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2026"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"
    And Initiating a write-off the Working Capital loan on "15 January 2026" results an error with the following data:
      | HTTP response code | Error message                    |
      | 400                | error.msg.wc.loan.is.not.active. |

  @TestRailId:C94015
  Scenario: Verify Working Capital Write-off: write-off failed on already closed as written-off loan account - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Initiating a write-off the Working Capital loan on "15 January 2026" results an error with the following data:
      | HTTP response code | Error message                    |
      | 400                | error.msg.wc.loan.is.not.active. |

  @TestRailId:C94016
  Scenario: Verify Working Capital Write-off: write-off failed on overpaid loan account - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 500 transaction amount on Working Capital loan
    And Initiating a write-off the Working Capital loan on "15 January 2026" results an error with the following data:
      | HTTP response code | Error message                    |
      | 400                | error.msg.wc.loan.is.not.active. |
    And Customer makes credit balance refund on "15 January 2026" with 400.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital loan balance principalOutstanding is "0.0"

  @TestRailId:C94017
  Scenario: Verify Working Capital Write-off: write-off failed on submitted and pending approve loan account - UC11
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_ACC_DEF_REV_AM | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 1000.0             | 18.0              | 0.0              |
    And Initiating a write-off the Working Capital loan on "01 January 2026" results an error with the following data:
      | HTTP response code | Error message                    |
      | 400                | error.msg.wc.loan.is.not.active. |
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful

  @TestRailId:C94018
  Scenario: Verify Working Capital Write-off: write-off failed on approved loan account - UC12
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_ACC_DEF_REV_AM | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 1000.0             | 18.0              | 0.0              |
    And Initiating a write-off the Working Capital loan on "01 January 2026" results an error with the following data:
      | HTTP response code | Error message                    |
      | 400                | error.msg.wc.loan.is.not.active. |
    And Initiating write-off undo of the Working Capital loan results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | error.msg.wc.loan.is.not.written.off. |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful

  @TestRailId:C94019
  Scenario: Verify Working Capital Write-off: undo write-off failed on active loan account with already undone write-off - UC13
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    And Initiating write-off undo of the Working Capital loan results an error with the following data:
      | HTTP response code | Error message                         |
      | 400                | error.msg.wc.loan.is.not.written.off. |
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C94020
  Scenario: Verify Working Capital Write-off: write-off on charged-off loan account - UC14
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "25 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin writes off the Working Capital loan on "25 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off             | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 25 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off             | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 25 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
    Then Admin closes the Working Capital loan with a full repayment on "25 January 2026"

  @TestRailId:C94021
  Scenario: Verify Working Capital Write-off: write-off payment allocation on loan account with charges fee - UC15
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 35.0 transaction amount
     Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 35.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin writes off the Working Capital loan on "15 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Accrual                | 35.0              | 0.0              | 35.0              | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 135.0             | 100.0            | 35.0              | 0.0                   | false    |
    Then Working Capital Loan Transactions tab has a "WRITE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 35.0   |
      | EXPENSE | e4           | Written off                | 135.0 |         |
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Accrual                | 35.0              | 0.0              | 35.0              | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 135.0             | 100.0            | 35.0              | 0.0                   | true     |
    Then Working Capital Loan Transactions tab has a reversed "WRITE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 35.0   |
      | EXPENSE | e4           | Written off                | 135.0  |        |
      | ASSET   | 112601       | Loans Receivable           | 100.0  |        |
      | ASSET   | 112603       | Interest/Fee Receivable    | 35.0   |        |
      | EXPENSE | e4           | Written off                |        | 135.0  |
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C94022
  Scenario: Verify Working Capital Write-off: write-off payment allocation on loan account with charges penalty - UC16
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "15 January 2026" due date and 50.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 15 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin writes off the Working Capital loan on "15 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 150.0             | 100.0            | 0.0               | 50.0                  | false    |
    Then Working Capital Loan Transactions tab has a "WRITE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 50.0   |
      | EXPENSE | e4           | Written off                | 150.0 |         |
# --- undo write-off the working capital loan account--- #
    And Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin undoes the write-off on the Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 150.0             | 100.0            | 0.0               | 50.0                  | true     |
    Then Working Capital Loan Transactions tab has a reversed "WRITE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 50.0   |
      | EXPENSE | e4           | Written off                | 150.0  |        |
      | ASSET   | 112601       | Loans Receivable           | 100.0  |        |
      | ASSET   | 112603       | Interest/Fee Receivable    | 50.0   |        |
      | EXPENSE | e4           | Written off                |        | 150.0  |
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    And Admin sets the business date to "17 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 150.0             | 100.0            | 0.0               | 50.0                  | true     |
      | 15 January 2026 | Accrual                | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    Then Admin closes the Working Capital loan with a full repayment on "17 January 2026"

  @TestRailId:C94023
  Scenario: Verify Working Capital Write-off: write-off payment allocation on loan account with charges fees and penalties - UC17
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 35.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "13 January 2026" due date and 50.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 35.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 13 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin writes off the Working Capital loan on "15 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Accrual                | 35.0              | 0.0              | 35.0              | 0.0                   | false    |
      | 13 January 2026 | Accrual                | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 15 January 2026 | Close (as written-off) | 185.0             | 100.0            | 35.0              | 50.0                  | false    |
    Then Working Capital Loan Transactions tab has a "WRITE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 35.0   |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 50.0   |
      | EXPENSE | e4           | Written off                | 185.0  |        |
# --- undo write-off the working capital loan account--- #
    And Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin undoes the write-off on the Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Accrual                | 35.0              | 0.0              | 35.0              | 0.0                   | false    |
      | 13 January 2026 | Accrual                | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 15 January 2026 | Close (as written-off) | 185.0             | 100.0            | 35.0              | 50.0                  | true     |
    Then Working Capital Loan Transactions tab has a reversed "WRITE_OFF" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 35.0   |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 50.0   |
      | EXPENSE | e4           | Written off                | 185.0  |        |
      | ASSET   | 112601       | Loans Receivable           | 100.0  |        |
      | ASSET   | 112603       | Interest/Fee Receivable    | 35.0   |        |
      | ASSET   | 112603       | Interest/Fee Receivable    | 50.0   |        |
      | EXPENSE | e4           | Written off                |        | 185.0  |
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    Then Admin closes the Working Capital loan with a full repayment on "16 January 2026"

  @TestRailId:C94024
  Scenario: Verify Working Capital Write-off: backdated write-off before repayment failed with error - UC18
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "15 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Repayment     | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    And Initiating a write-off the Working Capital loan on "10 January 2026" results an error with the following data:
      | HTTP response code | Error message                          |
      | 400                | cannot.be.before.last.transaction.date.|
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C94025
  Scenario: Verify Working Capital Write-off: backdated repayment before write-off failed with error - UC19
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin writes off the Working Capital loan on "15 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    Then Customer fails to make repayment on "10 January 2026" with 50 EUR transaction amount outcomes with error message
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C94026
  Scenario: Verify Working Capital Write-off: undo backdated repayment before write-off failed with error - UC20
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 50 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin writes off the Working Capital loan on "15 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment              | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    When Customer tries to undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan and gets error:
      | httpCode | errorMessage                                  |
      | 400      | undo.transaction.not.allowed.for.loan.status. |
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment              | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off) | 50.0              | 50.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C94027
  Scenario: Verify Working Capital Write-off: discount after write-off on disbursement date failed with error - UC21
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- write-off the working capital loan account--- #
    And Admin writes off the Working Capital loan on "01 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    Then Adding Discount fee with "10" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                 |
      | 400                | validation.msg.wc.loan.transition.not.allowed |
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C94028
  Scenario: Verify Working Capital Write-off: write-off loan account with write-off reason mapping enabled and write-off reason Bad Debt - UC22
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin does write-off the Working Capital loan on "10 January 2026" with write off reason: "BAD_DEBT"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    Then Working Capital Loan Transactions tab has a "WRITE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit   | Credit |
      | ASSET   | 112601       | Loans Receivable        |         | 100.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 100.0   |        |
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | true    |
    Then Working Capital Loan Transactions tab has a reversed "WRITE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit   | Credit |
      | ASSET   | 112601       | Loans Receivable        |         | 100.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 100.0   |        |
      | ASSET   | 112601       | Loans Receivable        | 100.0   |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    |         | 100.0  |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C94029
  Scenario: Verify Working Capital Write-off: write-off loan account without write-off reason mapping enabled and write-off reason Bad Debt - UC23
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# --- write-off the working capital loan account--- #
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin does write-off the Working Capital loan on "10 January 2026" with write off reason: "BAD_DEBT"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    Then Working Capital Loan Transactions tab has a "WRITE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable        |       | 100.0  |
      | EXPENSE | e4           | Written off             | 100.0 |        |
# --- undo write-off the working capital loan account--- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    And Working Capital Loan has transactions:
      | transactionDate | type                   | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement           | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Close (as written-off) | 100.0             | 100.0            | 0.0               | 0.0                   | true    |
    Then Working Capital Loan Transactions tab has a reversed "WRITE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit   | Credit |
      | ASSET   | 112601       | Loans Receivable        |         | 100.0  |
      | EXPENSE | e4           | Written off             | 100.0   |        |
      | ASSET   | 112601       | Loans Receivable        | 100.0   |        |
      | EXPENSE | e4           | Written off             |         | 100.0  |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

