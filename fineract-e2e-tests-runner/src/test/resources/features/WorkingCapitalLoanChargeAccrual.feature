@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalLoanChargeAccrualFeature
Feature: Working Capital Loan Charge Accrual

  @TestRailId:C85589
  Scenario: Verify Working Capital fee charge accrual is posted on charge due date when charge-accrual-date is due-date
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 50.0 transaction amount
    And Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026  | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026  | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026  | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026  | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
      | INCOME | 404007       | Fee Income              |       | 50.0   |
    When Admin sets the business date to "17 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026  | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026  | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    And Admin sets the business date to "20 January 2026"
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9050.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 50.0   |

  @TestRailId:C85590
  Scenario: Verify Working Capital penalty charge accrual is posted in real time when charge-accrual-date is submitted-date
    Given Admin sets the business date to "01 February 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 February 2026 | 01 February 2026         | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "submitted-date"
    And Admin sets the business date to "10 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "20 February 2026" due date and 30.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 February 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 February 2026 | Accrual      | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "10 February 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 30.0  |        |
      | INCOME | 404007       | Fee Income              |       | 30.0   |
    When Admin sets the business date to "11 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 February 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 February 2026 | Accrual      | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
    And Admin sets the business date to "20 February 2026"
    When Global config "charge-accrual-date" value set to "due-date"
    Then Admin closes the Working Capital loan with a full repayment on "20 February 2026"

  @TestRailId:C85591
  Scenario: Verify Working Capital fee and penalty charge accrual is posted in real time when charge-accrual-date is submitted-date
    Given Admin sets the business date to "01 March 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 March 2026   | 01 March 2026            | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "submitted-date"
    And Admin sets the business date to "10 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 March 2026" due date and 40.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "21 March 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 March 2026   | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 March 2026   | Accrual      | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 10 March 2026   | Accrual      | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
    And Working Capital Loan Transactions tab has 2 "ACCRUAL" transactions with date "10 March 2026" which have the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404007       | Fee Income              |       | 40.0   |
      | ASSET  | 112603       | Interest/Fee Receivable | 25.0  |        |
      | INCOME | 404007       | Fee Income              |       | 25.0   |
    And Admin sets the business date to "25 March 2026"
    When Global config "charge-accrual-date" value set to "due-date"
    Then Admin closes the Working Capital loan with a full repayment on "25 March 2026"

  @TestRailId:C85592
  Scenario: Verify Working Capital fee charge accrual is posted in real time when charge-accrual-date is submitted-date
    Given Admin sets the business date to "01 April 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 April 2026   | 01 April 2026            | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "submitted-date"
    And Admin sets the business date to "10 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 April 2026" due date and 50.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 April 2026   | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 April 2026   | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "10 April 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
      | INCOME | 404007       | Fee Income              |       | 50.0   |
    When Admin sets the business date to "11 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 April 2026   | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 April 2026   | Accrual      | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
    And Admin sets the business date to "25 April 2026"
    When Global config "charge-accrual-date" value set to "due-date"
    Then Admin closes the Working Capital loan with a full repayment on "25 April 2026"

  @TestRailId:C85593
  Scenario: Verify Working Capital penalty charge accrual is posted on charge due date when charge-accrual-date is due-date
    Given Admin sets the business date to "01 May 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 May 2026     | 01 May 2026              | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "15 May 2026" due date and 30.0 transaction amount
    And Admin sets the business date to "14 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 May 2026     | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "16 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 May 2026     | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 May 2026     | Accrual      | 30.0              | 0.0              | 0.0               | 30.0                  | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 May 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 30.0  |        |
      | INCOME | 404007       | Fee Income              |       | 30.0   |
    And Admin sets the business date to "20 May 2026"
    Then Admin closes the Working Capital loan with a full repayment on "20 May 2026"

  @TestRailId:C85594
  Scenario: Verify Working Capital fee and penalty charge accrual is posted on charge due dates when charge-accrual-date is due-date
    Given Admin sets the business date to "01 June 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 June 2026    | 01 June 2026             | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 June 2026" due date and 40.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "21 June 2026" due date and 25.0 transaction amount
    And Admin sets the business date to "14 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 June 2026    | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "22 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 June 2026    | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 20 June 2026    | Accrual      | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 21 June 2026    | Accrual      | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "20 June 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404007       | Fee Income              |       | 40.0   |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "21 June 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 25.0  |        |
      | INCOME | 404007       | Fee Income              |       | 25.0   |
    And Admin sets the business date to "25 June 2026"
    Then Admin closes the Working Capital loan with a full repayment on "25 June 2026"

  @TestRailId:C85595
  Scenario: Verify Working Capital fee charge adjustment before accrual when charge-accrual-date is due-date
    Given Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 July 2026    | 01 July 2026             | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 July 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 July 2026" due date and 100.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 100.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 July 2026    | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 July 2026    | Charge Adjustment | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    And Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 July 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 100.0  |
    When Admin sets the business date to "16 July 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 July 2026    | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 July 2026    | Charge Adjustment | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 15 July 2026    | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 July 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    And Admin sets the business date to "20 July 2026"
    Then Admin closes the Working Capital loan with a full repayment on "20 July 2026"

  @TestRailId:C85596
  Scenario: Verify Working Capital penalty charge adjustment before accrual when charge-accrual-date is due-date
    Given Admin sets the business date to "01 August 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 August 2026  | 01 August 2026           | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 August 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "15 August 2026" due date and 50.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 50.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 August 2026  | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 August 2026  | Charge Adjustment | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 0.0                 | 50.0         |
    And Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 August 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
    When Admin sets the business date to "16 August 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 August 2026  | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 August 2026  | Charge Adjustment | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 15 August 2026  | Accrual           | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 August 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
      | INCOME | 404007       | Fee Income              |       | 50.0   |
    And Admin sets the business date to "20 August 2026"
    Then Admin closes the Working Capital loan with a full repayment on "20 August 2026"

  @TestRailId:C85597
  Scenario: Verify Working Capital partial fee charge adjustment before accrual when charge-accrual-date is due-date
    Given Admin sets the business date to "01 November 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 November 2026 | 01 November 2026         | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 November 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 November 2026" due date and 100.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate  | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 November 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 November 2026 | Charge Adjustment | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 60.0            | 40.0     | 0.0            | 0.0                 | 0.0          |
    And Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 November 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 40.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 40.0   |
    When Admin sets the business date to "16 November 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate  | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 November 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 November 2026 | Charge Adjustment | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 15 November 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 November 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    And Admin sets the business date to "20 November 2026"
    Then Admin closes the Working Capital loan with a full repayment on "20 November 2026"

  @TestRailId:C85598
  Scenario: Verify Working Capital partial penalty charge adjustment before accrual when charge-accrual-date is due-date
    Given Admin sets the business date to "01 December 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 December 2026 | 01 December 2026         | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 December 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "15 December 2026" due date and 60.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 25.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate  | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 December 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 December 2026 | Charge Adjustment | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 60.0           | 35.0                | 25.0         |
    And Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 December 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 25.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 25.0   |
    When Admin sets the business date to "16 December 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate  | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 December 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 December 2026 | Charge Adjustment | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
      | 15 December 2026 | Accrual           | 60.0              | 0.0              | 0.0               | 60.0                  | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 December 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 60.0  |        |
      | INCOME | 404007       | Fee Income              |       | 60.0   |
    And Admin sets the business date to "20 December 2026"
    Then Admin closes the Working Capital loan with a full repayment on "20 December 2026"

  @TestRailId:C85599
  Scenario: Verify Working Capital fee charge accrual is still posted on due date when the charge was fully repaid before the accrual runs
    Given Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2027 | 01 January 2027          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2027"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2027" due date and 100.0 transaction amount
    And Admin sets the business date to "12 January 2027"
    And Customer makes repayment on "12 January 2027" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2027 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2027 | Repayment    | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "12 January 2027" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 100.0  |
    When Admin sets the business date to "13 January 2027"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2027 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2027 | Repayment    | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 12 January 2027 | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "12 January 2027" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    And Admin sets the business date to "20 January 2027"
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2027"

  @TestRailId:C85600
  Scenario: Verify Working Capital fee charge accrual is posted on the closing date when the loan is closed before the charge due date
    Given Admin sets the business date to "01 February 2027"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 February 2027 | 01 February 2027         | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 February 2027"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 February 2027" due date and 100.0 transaction amount
    And Admin sets the business date to "15 February 2027"
    And Customer makes repayment on "15 February 2027" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 February 2027 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 February 2027 | Repayment    | 9100.0            | 9000.0           | 100.0             | 0.0                   | false    |
      | 15 February 2027 | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 February 2027" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "15 February 2027" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 100.0  |

  @TestRailId:C85601
  Scenario: Verify Working Capital charge accrual is reversed when the disbursal is undone
    Given Admin sets the business date to "01 March 2027"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 March 2027   | 01 March 2027            | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "submitted-date"
    And Admin sets the business date to "10 March 2027"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 March 2027" due date and 100.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 March 2027   | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 March 2027   | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "10 March 2027" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    When Admin successfully undo Working Capital disbursal
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 March 2027   | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |
      | 10 March 2027   | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | true     |
    And Working Capital Loan Transactions tab has a reversed "ACCRUAL" transaction with date "10 March 2027" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 100.0  |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin successfully disburse the Working Capital loan on "10 March 2027" with "9000" EUR transaction amount
    And Admin sets the business date to "21 March 2027"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 March 2027   | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |
      | 10 March 2027   | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | true     |
      | 10 March 2027   | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 20 March 2027   | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "20 March 2027" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    And Admin sets the business date to "25 March 2027"
    Then Admin closes the Working Capital loan with a full repayment on "25 March 2027"

  @TestRailId:C85602
  Scenario: Verify Working Capital fee charge adjustment after accrual when charge-accrual-date is submitted-date
    Given Admin sets the business date to "01 September 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate   | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 September 2026 | 01 September 2026        | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "submitted-date"
    And Admin sets the business date to "10 September 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 September 2026" due date and 100.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate   | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 September 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 September 2026 | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    When Admin makes a charge adjustment for the last added charge with 100.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate   | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 September 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 September 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 10 September 2026 | Charge Adjustment | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "10 September 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    And Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 September 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 100.0  |
    And Admin sets the business date to "25 September 2026"
    When Global config "charge-accrual-date" value set to "due-date"
    Then Admin closes the Working Capital loan with a full repayment on "25 September 2026"

  @TestRailId:C85603
  Scenario: Verify no accrual is posted for Working Capital loan charges when product accounting rule is NONE
    Given Admin sets the business date to "01 October 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 October 2026 | 01 October 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 October 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 October 2026" due date and 50.0 transaction amount
    When Admin sets the business date to "16 October 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 October 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Global config "charge-accrual-date" value set to "submitted-date"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "20 October 2026" due date and 30.0 transaction amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 October 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 50.0       | 50.0            | 0.0      | 30.0           | 30.0                | 0.0          |
    And Admin sets the business date to "20 October 2026"
    When Global config "charge-accrual-date" value set to "due-date"
    Then Admin closes the Working Capital loan with a full repayment on "20 October 2026"

  @TestRailId:C85643
  Scenario: Verify pending charge accrual is posted at closure even when charge-accrual-date is submitted-date
    Given Admin sets the business date to "01 November 2027"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 November 2027 | 01 November 2027         | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 November 2027"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 November 2027" due date and 100.0 transaction amount
    When Global config "charge-accrual-date" value set to "submitted-date"
    And Admin sets the business date to "15 November 2027"
    Then Admin closes the Working Capital loan with a full repayment on "15 November 2027"
    And Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 November 2027 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 November 2027 | Repayment    | 9100.0            | 9000.0           | 100.0             | 0.0                   | false    |
      | 15 November 2027 | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 November 2027" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |

  @TestRailId:C85643
  Scenario: Verify closure does not duplicate the accrual when the charge was already accrued in real time under submitted-date
    Given Admin sets the business date to "01 December 2027"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 December 2027 | 01 December 2027         | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "submitted-date"
    And Admin sets the business date to "05 December 2027"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 December 2027" due date and 100.0 transaction amount
    And Admin sets the business date to "10 December 2027"
    Then Admin closes the Working Capital loan with a full repayment on "10 December 2027"
    And Working Capital Loan has transactions:
      | transactionDate  | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 December 2027 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 December 2027 | Accrual      | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 10 December 2027 | Repayment    | 9100.0            | 9000.0           | 100.0             | 0.0                   | false    |
