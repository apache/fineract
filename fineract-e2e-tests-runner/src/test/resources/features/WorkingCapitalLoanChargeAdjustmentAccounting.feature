@WorkingCapital
@WorkingCapitalLoanChargeAdjustmentAccountingFeature
Feature: WorkingCapitalLoanChargeAdjustmentAccountingFeature

  @TestRailId:C89820
  Scenario: Verify Working Capital fee charge adjustment accounting entries - UC1: full fee charge adjustment has correct journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Admin makes a charge adjustment for the last added charge with 100.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 100.0  |
    When Customer undo "1"th "CHARGE_ADJUSTMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 100.0  |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment | 100.0             | 0.0              | 100.0             | 0.0                   | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C89821
  Scenario: Verify Working Capital penalty charge adjustment accounting entries - UC2: full penalty charge adjustment has correct journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 50.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 50.0                | 0.0          |
    When Admin makes a charge adjustment for the last added charge with 50.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 0.0                 | 50.0         |
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
    When Customer undo "1"th "CHARGE_ADJUSTMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
      | INCOME | 404007       | Fee Income              |       | 50.0   |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 50.0                  | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 50.0                | 0.0          |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C89822
  Scenario: Verify Working Capital fee charge adjustment accounting entries - UC3: partially paid fee - full fee charge adjustment has correct journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Customer makes repayment on "10 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 50.0            | 50.0     | 0.0            | 0.0                 | 0.0          |
    When Admin makes a charge adjustment for the last added charge with 100.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment | 100.0             | 50.0             | 50.0              | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112601       | Loans Receivable        |       | 50.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "8950.0"
    And Working Capital loan balance overpaymentAmount is "0.0"
    When Customer undo "1"th "CHARGE_ADJUSTMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112601       | Loans Receivable        |       | 50.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
      | ASSET  | 112601       | Loans Receivable        | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "9000.0"
    And Working Capital loan balance overpaymentAmount is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment | 100.0             | 50.0             | 50.0              | 0.0                   | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 50.0            | 50.0     | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C89823
  Scenario: Verify Working Capital penalty charge adjustment accounting entries - UC4: partially paid penalty - full penalty charge adjustment has correct journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 50.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 50.0                | 0.0          |
    When Customer makes repayment on "10 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 20.0                | 30.0         |
    When Admin makes a charge adjustment for the last added charge with 50.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
      | 10 January 2026 | Charge Adjustment | 50.0              | 30.0             | 0.0               | 20.0                  | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 0.0                 | 50.0         |
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112601       | Loans Receivable        |       | 30.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 20.0   |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "8970.0"
    And Working Capital loan balance overpaymentAmount is "0.0"
    When Customer undo "1"th "CHARGE_ADJUSTMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112601       | Loans Receivable        |       | 30.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 20.0   |
      | INCOME | 404007       | Fee Income              |       | 50.0   |
      | ASSET  | 112601       | Loans Receivable        | 30.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 20.0  |        |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "9000.0"
    And Working Capital loan balance overpaymentAmount is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
      | 10 January 2026 | Charge Adjustment | 50.0              | 30.0             | 0.0               | 20.0                  | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 20.0                | 30.0         |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C89824
  Scenario: Verify Working Capital fee charge adjustment accounting entries - UC5: partially paid fee - full fee charge adjustment has correct journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "11 January 2026" due date and 50.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 11 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 50.0           | 50.0                | 0.0          |
    When Customer makes repayment on "10 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 11 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 50.0            | 50.0     | 50.0           | 50.0                | 0.0          |
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 11 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 50.0            | 50.0     | 50.0           | 50.0                | 0.0          |
    When Admin makes a charge adjustment for the last added fee charge with 100.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 11 January 2026 | Charge Adjustment | 100.0             | 0.0              | 50.0              | 50.0                  | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 50.0           | 0.0                 | 50.0         |
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance overpaymentAmount is "0.0"
    When Customer undo "1"th "CHARGE_ADJUSTMENT" transaction made on "11 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance overpaymentAmount is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 11 January 2026 | Charge Adjustment | 100.0             | 0.0              | 50.0              | 50.0                  | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 11 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 50.0            | 50.0     | 50.0           | 50.0                | 0.0          |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"

  @TestRailId:C89825
  Scenario: Verify Working Capital penalty charge adjustment accounting entries - UC6: partially paid penalty - full penalty charge adjustment has correct journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 50.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "11 January 2026" due date and 60.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Fee     | 11 January 2026 | 60.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 60.0       | 60.0            | 0.0      | 50.0           | 50.0                | 0.0          |
    When Customer makes repayment on "10 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Fee     | 11 January 2026 | 60.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 60.0       | 60.0            | 0.0      | 50.0           | 20.0                | 30.0         |
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
      | 10 January 2026 | Accrual      | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Fee     | 11 January 2026 | 60.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 60.0       | 60.0            | 0.0      | 50.0           | 20.0                | 30.0         |
    When Admin makes a charge adjustment for the last added penalty charge with 50.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
      | 10 January 2026 | Accrual           | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 11 January 2026 | Charge Adjustment | 50.0              | 0.0              | 30.0              | 20.0                  | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 60.0       | 30.0            | 30.0     | 50.0           | 0.0                 | 50.0         |
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 30.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 20.0   |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance overpaymentAmount is "0.0"
    When Customer undo "1"th "CHARGE_ADJUSTMENT" transaction made on "11 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 30.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 20.0   |
      | INCOME | 404007       | Fee Income              |       | 50.0   |
      | ASSET  | 112603       | Interest/Fee Receivable | 30.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 20.0  |        |
    And Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance overpaymentAmount is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
      | 10 January 2026 | Accrual           | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 11 January 2026 | Charge Adjustment | 50.0              | 0.0              | 30.0              | 20.0                  | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Fee     | 11 January 2026 | 60.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 60.0       | 60.0            | 0.0      | 50.0           | 20.0                | 30.0         |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"


  @TestRailId:C89826
  Scenario: Verify Working Capital fee charge adjustment accounting entries - UC7: fully repaid loan - partial fee charge adjustment has overpayment journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 45.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 45.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 45.0       | 45.0            | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Customer makes repayment on "10 January 2026" with 9045.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 9045.0            | 9000.0           | 45.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual      | 45.0              | 0.0              | 45.0              | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 45.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 45.0       | 0.0             | 45.0     | 0.0            | 0.0                 | 0.0          |
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin makes a charge adjustment for the last added fee charge with 25.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 9045.0            | 9000.0           | 45.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual           | 45.0              | 0.0              | 45.0              | 0.0                   | false    |
      | 11 January 2026 | Charge Adjustment | 25.0              | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 45.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 45.0       | 0.0             | 45.0     | 0.0            | 0.0                 | 0.0          |
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name           | Debit | Credit |
      | INCOME    | 404007       | Fee Income             | 25.0  |        |
      | LIABILITY | 245000       | Other Credit Liability |       | 25.0   |
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "25.0"
    When Customer undo "1"th "CHARGE_ADJUSTMENT" transaction made on "11 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name           | Debit | Credit |
      | INCOME    | 404007       | Fee Income             | 25.0  |        |
      | LIABILITY | 245000       | Other Credit Liability |       | 25.0   |
      | INCOME    | 404007       | Fee Income             |       | 25.0   |
      | LIABILITY | 245000       | Other Credit Liability | 25.0  |        |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital loan balance overpaymentAmount is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 9045.0            | 9000.0           | 45.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual           | 45.0              | 0.0              | 45.0              | 0.0                   | false    |
      | 11 January 2026 | Charge Adjustment | 25.0              | 0.0              | 0.0               | 0.0                   | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 45.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 45.0       | 0.0             | 45.0     | 0.0            | 0.0                 | 0.0          |
    When Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "9000.0"
    And Working Capital loan balance overpaymentAmount is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 9045.0            | 9000.0           | 45.0              | 0.0                   | true     |
      | 10 January 2026 | Accrual           | 45.0              | 0.0              | 45.0              | 0.0                   | false    |
      | 11 January 2026 | Charge Adjustment | 25.0              | 0.0              | 0.0               | 0.0                   | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 45.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 45.0       | 45.0            | 0.0      | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"

  @TestRailId:C89827
  Scenario: Verify Working Capital penalty charge adjustment accounting entries - UC8: fully repaid loan - partial penalty charge adjustment has overpayment journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 77.7 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 77.7   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 77.7           | 77.7                | 0.0          |
    When Customer makes repayment on "10 January 2026" with 9077.7 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 9077.7            | 9000.0           | 0.0               | 77.7                  | false    |
      | 10 January 2026 | Accrual      | 77.7              | 0.0              | 0.0               | 77.7                  | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 77.7   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 77.7           | 0.0                 | 77.7         |
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin makes a charge adjustment for the last added penalty charge with 50.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 9077.7            | 9000.0           | 0.0               | 77.7                  | false    |
      | 10 January 2026 | Accrual           | 77.7              | 0.0              | 0.0               | 77.7                  | false    |
      | 11 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 77.7   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 77.7           | 0.0                 | 77.7         |
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name           | Debit | Credit |
      | INCOME    | 404007       | Fee Income             | 50.0  |        |
      | LIABILITY | 245000       | Other Credit Liability |       | 50.0   |
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "50.0"
    When Customer undo "1"th "CHARGE_ADJUSTMENT" transaction made on "11 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name           | Debit | Credit |
      | INCOME    | 404007       | Fee Income             | 50.0  |        |
      | LIABILITY | 245000       | Other Credit Liability |       | 50.0   |
      | INCOME    | 404007       | Fee Income             |       | 50.0   |
      | LIABILITY | 245000       | Other Credit Liability | 50.0  |        |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital loan balance overpaymentAmount is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 9077.7            | 9000.0           | 0.0               | 77.7                  | false    |
      | 10 January 2026 | Accrual           | 77.7              | 0.0              | 0.0               | 77.7                  | false    |
      | 11 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 0.0                   | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 77.7   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 77.7           | 0.0                 | 77.7         |
    When Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance principalOutstanding is "9000.0"
    And Working Capital loan balance overpaymentAmount is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 9077.7            | 9000.0           | 0.0               | 77.7                  | true     |
      | 10 January 2026 | Accrual           | 77.7              | 0.0              | 0.0               | 77.7                  | false    |
      | 11 January 2026 | Charge Adjustment | 50.0              | 0.0              | 0.0               | 0.0                   | true     |
    And Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 77.7   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 77.7           | 77.7                | 0.0          |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"