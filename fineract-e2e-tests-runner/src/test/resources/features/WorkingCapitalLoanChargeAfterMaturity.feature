@WorkingCapital
@WorkingCapitalLoanChargeAfterMaturityFeature
Feature: Working Capital Loan Charge After Maturity

  @TestRailId:C85607
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC1: closed loan reopens to ACTIVE when charge added after maturity
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct             | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status                   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP_BREACH_NEAR_BREACH | 2026-01-01      | 2026-01-01               | Closed (obligations met) | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 0.0               |
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 June 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date     | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 June 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 100.0      |
      | balance.principalOutstanding | 0.0        |
      | timeline.actualMaturityDate  | 2026-06-01 |
      | delinquencyStartDate         | null       |
      | breachStartDate              | null       |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 12.30            | 12.30             | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "01 June 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85608
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC2: overpaid loan stays OVERPAID when charge is less than overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1150.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 150.0             |
    When Admin sets the business date to "01 July 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 July 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date     | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 July 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 50.0              |
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0  |
      | balance.principalOutstanding | 0.0  |
      | delinquencyStartDate         | null |
      | breachStartDate              | null |
    And Working Capital loan breach schedule has no data
    Then Customer makes credit balance refund on "01 July 2026" with 50.0 transaction amount on Working Capital loan
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85609
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC3: overpaid loan closes when charge equals overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 100.0             |
    When Admin sets the business date to "01 August 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 August 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date       | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 August 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Closed (obligations met) | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 0.0               |
    And Working capital loan details has the following field values:
      | balance.totalOutstanding | 0.0  |
      | delinquencyStartDate     | null |
      | breachStartDate          | null |
    And Working Capital loan breach schedule has no data

  @TestRailId:C85610
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC4: overpaid loan reopens to ACTIVE when charge is greater than overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct             | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1040.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP_BREACH_NEAR_BREACH | 2026-01-01      | 2026-01-01               | Overpaid | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 40.0              |
    When Admin sets the business date to "01 September 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 September 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date          | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 September 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 60.0            | 40.0     | 0.0            | 0.0                 | 0.0          |
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 60.0       |
      | balance.principalOutstanding | 0.0        |
      | timeline.actualMaturityDate  | 2026-09-01 |
      | delinquencyStartDate         | null       |
      | breachStartDate              | null       |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 12.30            | 12.30             | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "01 September 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85611
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC5: active loan past maturity updates maturity and schedules on charge
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct             | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    When Admin sets the business date to "01 October 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 October 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 October 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 1100.0     |
      | balance.principalOutstanding | 1000.0     |
      | timeline.actualMaturityDate  | 2026-10-01 |
      | delinquencyStartDate         | present    |
      | breachStartDate              | 2026-01-01 |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 12.30            | 12.30             | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 12.30            | 12.30             | null       | true   |
      | 3            | 2026-05-01 | 2026-06-30 | 61           | 12.30            | 12.30             | null       | true   |
      | 4            | 2026-07-01 | 2026-08-31 | 62           | 12.30            | 12.30             | null       | true   |
      | 5            | 2026-09-01 | 2026-10-31 | 61           | 12.30            | 12.30             | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "01 October 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85612
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC6: charge on accrual product after maturity does not create a spurious transaction
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "01 November 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 November 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date         | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 November 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment    | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "01 November 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85613
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC7: partial repayment on charge-reopened loan stays ACTIVE and clears fee
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct             | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "01 December 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 December 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date         | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 December 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Admin sets the business date to "02 December 2026"
    And Customer makes repayment on "02 December 2026" with 40.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 60.0            | 40.0     | 0.0            | 0.0                 | 0.0          |
    And Working capital loan details has the following field values:
      | balance.totalOutstanding | 60.0 |
    Then Admin closes the Working Capital loan with a full repayment on "02 December 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85614
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC8: overpayment consumed by charge survives backdated repayment reprocessing
    When Admin sets the business date to "01 August 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 August 2026  | 01 August 2026           | 1000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 August 2026"
    And Customer makes repayment on "10 August 2026" with 1150.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-08-01      | 2026-08-01               | Overpaid | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 150.0             |
    When Admin sets the business date to "15 August 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 August 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date       | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 20 August 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-08-01      | 2026-08-01               | Overpaid | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 50.0              |
    When Admin sets the business date to "20 August 2026"
    And Customer makes repayment on "05 August 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-08-01      | 2026-08-01               | Overpaid | 1000.0    | 1000.0            | 100000.0           | 18.0              | null     | 1000.0             | 100.0             |
    Then Customer makes credit balance refund on "20 August 2026" with 100.0 transaction amount on Working Capital loan
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85615
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC9: charge due date before business date is rejected (Negative)
    When Admin creates working capital loan charge without payment mode
    And Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trying to add working capital loan charge by loan id and charge id with amount 100.0 and due date "01-05-2026" results an error with the following data:
      | httpCode | errorMessage                  |
      | 400      | DueDate cannot be in the past |

  @TestRailId:C85616
  Scenario: Verify charges added after Maturity date, closed, overpaid loan - UC10: accounting entries after charge-driven reopen and repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Customer makes repayment on "01 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 June 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date     | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 June 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Admin sets the business date to "02 June 2026"
    And Customer makes repayment on "02 June 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 June 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 100.0  |
