@WorkingCapital
@WorkingCapitalLoanRecoveryPaymentFeature
Feature: Working Capital Loan Recovery Payment

  @TestRailId:C98214
  Scenario: Verify Working Capital Write-off Recovery Payment - UC1: full cycle write-off, recovery, undo recovery, undo write-off
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
# --- write off the loan --- #
    When Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance payload contains the following fields:
      | field                 | value |
      | principalOutstanding  | 0.0   |
      | totalWrittenOff       | 100.0 |
      | totalRecovered        | 0.0   |
      | writtenOffOutstanding | 100.0 |
# --- collect a partial recovery --- #
    When Admin sets the business date to "20 January 2026"
    And Admin makes a recovery payment of "40" on the Working Capital loan on "20 January 2026"
    Then Working Capital Loan Transactions tab has a "RECOVERY_REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 40.0  |        |
      | INCOME    | 744008       | Recoveries                |       | 40.0   |
# --- the recovery is income, not a repayment: the loan stays closed and the balance stays zeroed --- #
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance payload contains the following fields:
      | field                 | value |
      | principalOutstanding  | 0.0   |
      | totalPaidPrincipal    | 0.0   |
      | totalWrittenOff       | 100.0 |
      | totalRecovered        | 40.0  |
      | writtenOffOutstanding | 60.0  |
# --- the recovery carries no allocation, so its portion columns come back null, not zero --- #
    And Working Capital Loan has transactions:
      | transactionDate | type                        | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Close (as written-off)      | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 20 January 2026 | Repayment (after write-off) | 40.0              |                  |                   |                       | false    |
# --- reverse the recovery: the mirror entries are appended and the money becomes recoverable again --- #
    When Admin undoes the last recovery payment on the Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "RECOVERY_REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 40.0  |        |
      | INCOME    | 744008       | Recoveries                |       | 40.0   |
      | INCOME    | 744008       | Recoveries                | 40.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 40.0   |
    And Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance payload contains the following fields:
      | field                 | value |
      | totalRecovered        | 0.0   |
      | writtenOffOutstanding | 100.0 |
# --- with nothing recovered, the write-off can be undone --- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  @TestRailId:C98215
  Scenario: Verify Working Capital Write-off Recovery Payment - UC2: successive recoveries cannot collect more than was written off
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    And Admin makes a recovery payment of "60" on the Working Capital loan on "15 January 2026"
# --- 100 was written off and 60 recovered, so only 40 is left: a 50 recovery is rejected --- #
    Then Initiating a recovery payment of "50" on the Working Capital loan on "15 January 2026" results an error with the following data:
      | HTTP response code | Error message                                     |
      | 400                | cannot.be.greater.than.remaining.written.off.amount |
    And Working Capital loan balance payload contains the following fields:
      | field                 | value |
      | totalRecovered        | 60.0  |
      | writtenOffOutstanding | 40.0  |
# --- exactly the remainder is accepted, and then nothing more --- #
    When Admin makes a recovery payment of "40" on the Working Capital loan on "15 January 2026"
    Then Working Capital loan balance payload contains the following fields:
      | field                 | value |
      | totalRecovered        | 100.0 |
      | writtenOffOutstanding | 0.0   |
    Then Initiating a recovery payment of "1" on the Working Capital loan on "15 January 2026" results an error with the following data:
      | HTTP response code | Error message                                     |
      | 400                | cannot.be.greater.than.remaining.written.off.amount |

  @TestRailId:C98216
  Scenario: Verify Working Capital Write-off Recovery Payment - UC3: the write-off cannot be undone while a recovery stands
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    And Admin makes a recovery payment of "40" on the Working Capital loan on "15 January 2026"
# --- undoing the write-off would restore the full outstanding while the recovered cash stays booked as income --- #
    Then Initiating write-off undo of the Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                |
      | 400                | cannot.undo.write.off.with.recovery.payments |
    And Working Capital loan status will be "CLOSED_WRITTEN_OFF"
# --- reversing the recovery clears the way --- #
    When Admin undoes the last recovery payment on the Working Capital loan
    And Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "100.0"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C98217
  Scenario: Verify Working Capital Write-off Recovery Payment - UC4: a recovery is rejected on a loan that is not written off
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Initiating a recovery payment of "40" on the Working Capital loan on "01 January 2026" results an error with the following data:
      | HTTP response code | Error message                        |
      | 400                | error.msg.wc.loan.is.not.written.off |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C98218
  Scenario: Verify Working Capital Write-off Recovery Payment - UC5: the transaction template offers the remaining recoverable amount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
# --- before any recovery the template offers the whole amount written off --- #
    When Admin requests the Working Capital loan transaction template for command "recoveryPayment"
    Then The Working Capital loan transaction template expectedAmount is "100.0"
# --- after a partial recovery it offers the remainder, not the gross figure --- #
    When Admin makes a recovery payment of "30" on the Working Capital loan on "15 January 2026"
    And Admin requests the Working Capital loan transaction template for command "recoveryPayment"
    Then The Working Capital loan transaction template expectedAmount is "70.0"

  @TestRailId:C98219
  Scenario: Verify Working Capital Write-off Recovery Payment - UC6: recovery payment and undo recovery payment business events are raised
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    Then Working capital loan creation was successful
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
# --- write off the loan --- #
    When Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And a Working Capital Loan Write Off transaction business event is raised with "100.0" EUR amount
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
# --- collect a recovery and verify the business event --- #
    When Admin sets the business date to "20 January 2026"
    And Admin makes a recovery payment of "40" on the Working Capital loan on "20 January 2026"
    Then a Working Capital Loan Recovery Payment transaction business event is raised with "40.0" EUR amount
    And Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance payload contains the following fields:
      | field                 | value |
      | principalOutstanding  | 0.0   |
      | totalRecovered        | 40.0  |
      | writtenOffOutstanding | 60.0  |
# --- reverse the recovery and verify the undo business event --- #
    When Admin undoes the last recovery payment on the Working Capital loan
    And a Working Capital Loan Adjust Transaction business event is raised for the reversed "recoveryRepayment" transaction
    And Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance payload contains the following fields:
      | field                 | value |
      | totalRecovered        | 0.0   |
      | writtenOffOutstanding | 100.0 |
# --- clean up: undo write-off and close the loan --- #
    When Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Undo Write-Off transaction business event is raised
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  @TestRailId:C98220
  Scenario: Verify Working Capital Write-off Recovery Payment - UC7: recovery payment cannot be backdated before the write-off date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    Then Working capital loan creation was successful
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
# --- recovery on the write-off date is allowed --- #
    When Admin makes a recovery payment of "20" on the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance payload contains the following fields:
      | field                 | value |
      | totalRecovered        | 20.0  |
      | writtenOffOutstanding | 80.0  |
# --- recovery before the write-off date is rejected --- #
    Then Initiating a recovery payment of "10" on the Working Capital loan on "14 January 2026" results an error with the following data:
      | HTTP response code | Error message                      |
      | 400                | cannot.be.before.last.transaction.date |
# --- clean up: reverse the recovery, undo write-off and close the loan --- #
    When Admin undoes the last recovery payment on the Working Capital loan
    And Admin undoes the write-off on the Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"
