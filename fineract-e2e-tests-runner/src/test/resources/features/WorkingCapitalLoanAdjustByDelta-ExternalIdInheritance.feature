@WorkingCapital
@WorkingCapitalLoanAdjustByDeltaTransactionFeature
Feature: Working Capital Loan Adjust By Delta Transaction - External Id Inheritance

  # An aggregated repayment is posted once under an external id, then partially reversed again and again, possibly
  # several times at once. Each adjustment is sent against the same transaction external id and asks for that same
  # external id on its replacement, so the external id always points to the live transaction.

  @TestRailId:C123845
  Scenario: Verify Working Capital Loan adjust by delta - external id inheritance - UC1: consecutive adjustments follow the external id
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount and external-id "WC-AGG-SEQ" on Working Capital loan
    # First sale reversal: the replacement takes the external id over
    When Customer adjusts by delta the Working Capital loan transaction with external-id "WC-AGG-SEQ" by "-5" inheriting its external-id
    Then the Working Capital loan transaction with external-id "WC-AGG-SEQ" is the replacement created by the last adjustment
    And Working Capital Loan transaction with external-id "WC-AGG-SEQ" has amount "95.0" and reversed "false"
    And a Working Capital Loan Adjust Transaction business event shows the external-id "WC-AGG-SEQ" moved to the replacement
    # Second sale reversal against the same external id lands on the first replacement
    When Customer adjusts by delta the Working Capital loan transaction with external-id "WC-AGG-SEQ" by "-5" inheriting its external-id
    Then the Working Capital loan transaction with external-id "WC-AGG-SEQ" is the replacement created by the last adjustment
    And Working Capital Loan transaction with external-id "WC-AGG-SEQ" has amount "90.0" and reversed "false"
    And a Working Capital Loan Adjust Transaction business event shows the external-id "WC-AGG-SEQ" moved to the replacement
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8910.00 |
      | totalPaidPrincipal   | 90.00   |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 90.0  |        |
      | ASSET     | 112601       | Loans Receivable          |       | 90.0   |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C123846
  Scenario: Verify Working Capital Loan adjust - external id inheritance - UC2: absolute adjust passes the external id on
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount and external-id "WC-AGG-ABS" on Working Capital loan
    When Customer adjusts the Working Capital loan transaction with external-id "WC-AGG-ABS" to amount "80" on "10 January 2026" inheriting its external-id
    Then the Working Capital loan transaction with external-id "WC-AGG-ABS" is the replacement created by the last adjustment
    And Working Capital Loan transaction with external-id "WC-AGG-ABS" has amount "80.0" and reversed "false"
    And a Working Capital Loan Adjust Transaction business event shows the external-id "WC-AGG-ABS" moved to the replacement
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C123847
  Scenario: Verify Working Capital Loan adjust by delta - external id inheritance - UC3: the external id of another transaction is still refused
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount and external-id "WC-AGG-OWN" on Working Capital loan
    And Customer makes repayment on "10 January 2026" with 50.0 transaction amount and external-id "WC-AGG-OTHER" on Working Capital loan
    When Customer adjusts by delta the Working Capital loan transaction with external-id "WC-AGG-OWN" by "-5" with external-id "WC-AGG-OTHER" and gets error:
      | httpCode | errorMessage                                                 |
      | 400      | validation.msg.WORKINGCAPITALLOAN.externalId.already.exists |
    Then Working Capital Loan transaction with external-id "WC-AGG-OWN" has amount "100.0" and reversed "false"
    And Working Capital Loan transaction with external-id "WC-AGG-OTHER" has amount "50.0" and reversed "false"
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C123848
  Scenario: Verify Working Capital Loan adjust by delta - external id inheritance - UC4: concurrent adjustments against the same external id are each applied once
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount and external-id "WC-AGG-CONC" on Working Capital loan
    # Both calls target the same external id at the same moment; the one that loses the race is retried and lands on the
    # replacement the winner created
    When Customer sends these adjust by delta calls at the same time against the Working Capital loan transaction with external-id "WC-AGG-CONC", each inheriting its external-id:
      | delta |
      | -5    |
      | -7.5  |
    Then Working Capital Loan transaction with external-id "WC-AGG-CONC" has amount "87.5" and reversed "false"
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8912.50 |
      | totalPaidPrincipal   | 87.50   |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 87.5  |        |
      | ASSET     | 112601       | Loans Receivable          |       | 87.5   |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C123849
  Scenario: Verify Working Capital Loan adjust by delta - external id inheritance - UC5: concurrent adjustments against the same numeric id let only one win
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount and external-id "WC-AGG-NUM" on Working Capital loan
    When Customer sends these adjust by delta calls at the same time against the numeric id of the Working Capital loan transaction with external-id "WC-AGG-NUM", only one succeeds:
      | delta |
      | -5    |
      | -7.5  |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C123850
  Scenario: Verify Working Capital Loan adjust by delta - external id inheritance - UC6: after a full reversal the external id stays on the reversed transaction
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount and external-id "WC-AGG-FULL" on Working Capital loan
    When Customer adjusts by delta the Working Capital loan transaction with external-id "WC-AGG-FULL" by "-100" inheriting its external-id
    Then Working Capital Loan transaction with external-id "WC-AGG-FULL" has amount "100.0" and reversed "true"
    # Every retry attempt finds the external id on the reversed transaction, so the call ends with the same error as a numeric id
    When Customer adjusts by delta the Working Capital loan transaction with external-id "WC-AGG-FULL" by "-5" with external-id "WC-AGG-FULL" and gets error:
      | httpCode | errorMessage                                 |
      | 403      | error.msg.wc.loan.transaction.already.undone |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C123851
  Scenario: Verify Working Capital Loan adjust by delta - external id inheritance - UC7: a different unique external id is set on the replacement, the reversed transaction keeps its own
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount and external-id "WC-AGG-A" on Working Capital loan
    # Opt-out: a different external id is requested, so the replacement gets that one and the reversed transaction keeps WC-AGG-A
    When Customer adjusts by delta the Working Capital loan transaction with external-id "WC-AGG-A" by "-5" with new external-id "WC-NEW-B"
    Then Working Capital Loan transaction with external-id "WC-AGG-A" has amount "100.0" and reversed "true"
    And Working Capital Loan transaction with external-id "WC-NEW-B" has amount "95.0" and reversed "false"
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"
