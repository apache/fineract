@WorkingCapital
@WorkingCapitalLoanAdjustByDeltaTransactionFeature
Feature: Working Capital Loan Adjust By Delta Transaction - Adjust

  Scenario: Verify working capital loan transaction Adjust By Delta delta amount creates replacement - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8950.00         | 0.0                        | 270.0               | 0.0                      | 0.0                        | 8730.00       | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8680.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8730.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 270.00  |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan with amount "-120" and payment details:
      | paymentType | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | AUTOPAY     | acc123        | che456      | rou789      | rec012        | ban345     |
    Then a Working Capital Loan Adjust Transaction business event is raised for the adjusted "repayment" with new amount "150"
    And Working Capital loan transaction with type "REPAYMENT" has payment type "AUTOPAY"
    And Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 270.0  |
      | ASSET     | 112601       | Loans Receivable          | 270.0 |        |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8950.00         | 0.0                        | 150.0               | 0.0                      | 0.0                        | 8850.00       | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8800.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8850.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 150.00  |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 150.0      | 120.0             | null                  | null             | null           |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 270.0             | 270.0            | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment    | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"


  Scenario: Verify working capital loan transaction Adjust By Delta overshoot is rejected for REPAYMENT
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Customer makes "REPAYMENT" transaction on "01 January 2026" with 234.56 transaction amount on Working Capital loan
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "01 January 2026" on Working Capital loan with amount "-330.0" and date "01 January 2026" is failed:
      | httpCode | errorMessage                                                                                                        |
      | 400      | Failed data validation due to: delta.amount.should.not.result.in.negative. |

  Scenario: Verify working capital loan transaction Adjust By Delta overshoot is rejected for PAYOUT_REFUND
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Customer makes "PAYOUT_REFUND" transaction on "01 January 2026" with 234.56 transaction amount on Working Capital loan
    When Customer adjust by delta "1"th "PAYOUT_REFUND" transaction made on "01 January 2026" on Working Capital loan with amount "-330.0" and date "01 January 2026" is failed:
      | httpCode | errorMessage                                                                                                        |
      | 400      | Failed data validation due to: delta.amount.should.not.result.in.negative. |

  Scenario: Verify working capital loan transaction Adjust By Delta overshoot is rejected for GOODWILL_CREDIT
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Customer makes "GOODWILL_CREDIT" transaction on "01 January 2026" with 234.56 transaction amount on Working Capital loan
    When Customer adjust by delta "1"th "GOODWILL_CREDIT" transaction made on "01 January 2026" on Working Capital loan with amount "-330.0" and date "01 January 2026" is failed:
      | httpCode | errorMessage                                                                                                        |
      | 400      | Failed data validation due to: delta.amount.should.not.result.in.negative. |

  @TestRailId:TODO_05
  Scenario: Verify working capital loan transaction Adjust By Delta later repayment triggers reverse-replay - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8700.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 300.00  |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 300.0      | 0.0               | true                  | 0.0              | 0              |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan with amount "-50"
    Then a Working Capital Loan Adjust Transaction business event is raised for the adjusted "repayment" with new amount "50"
    And Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | ASSET     | 112601       | Loans Receivable          |       | 50.0   |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8950.00         | 0.0                        | 50.0                | 0.0                      | 0.0                        | 8950.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 8950.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 8950.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 8950.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 8950.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8900.00         | 0.0                        | 200.0               | 0.0                      | 0.0                        | 8750.00       | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8700.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8750.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 250.00  |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 250.0      | 20.0              | null                  | null             | null           |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
      | 05 January 2026 | Repayment    | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:TODO_06
  Scenario: Verify Working Capital Repayment transaction Adjust By Delta fee and penalty added with DUE_FEE_PENALTY_PRINCIPAL allocation - UC4
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DUE_FEE_PENALTY_PRINCIPAL | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 270.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 230.0              | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 0.0                 | 25.0         |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 270.0             | 230.0            | 15.0              | 25.0                  | false    |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "12 January 2026" on Working Capital loan with amount "-220"
    Then a Working Capital Loan Adjust Transaction business event is raised for the adjusted "repayment" with new amount "50"
    And Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "12 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 230.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 15.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 25.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 230.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 15.0  |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 25.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 270.0  |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "12 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 15.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 25.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 270.0             | 230.0            | 15.0              | 25.0                  | true     |
      | 12 January 2026 | Accrual      | 15.0              | 0.0              | 15.0              | 0.0                   | false    |
      | 12 January 2026 | Accrual      | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
      | 12 January 2026 | Repayment    | 50.0              | 10.0             | 15.0              | 25.0                  | false    |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:TODO_07
  Scenario: Verify Working Capital Repayment transaction Adjust By Delta on closed loan - UC5
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 500      |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 9500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment                 | 9500.0            | 9500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 500.0             |                  |                   |                       | false    |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan with amount "-9250"
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment                 | 9500.0            | 9500.0           | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Discount Fee Amortization | 500.0             |                  |                   |                       | false    |
      | 10 January 2026 | Repayment                 | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    And Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 9500.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 9500.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 9500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9500.0 |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:TODO_08
  Scenario: Verify Working Capital Payout Refund transaction Adjust By Delta on overpaid loan - UC6
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1500     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 2000.0 transaction amount on Working Capital loan
    And Customer makes repayment on "10 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1500.0            | 1500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund             | 2000.0            | 2000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment                 | 9000.0            | 8500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1500.0            |                  |                   |                       | false    |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer adjust by delta "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan with amount "-1650.0" and date "12 January 2026" and payment details:
      | paymentType | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | AUTOPAY     | acc123        | che456      | rou789      | rec012        | ban345     |
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1500.0            | 1500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund             | 2000.0            | 2000.0           | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment                 | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1500.0            |                  |                   |                       | false    |
      | 12 January 2026 | Payout Refund             | 350.0             | 350.0            | 0.0               | 0.0                   | false    |
    Then Working Capital loan transaction with type "PAYOUT_REFUND" with date "12 January 2026" and amount "350" has payment details:
      | paymentType | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | AUTOPAY     | acc123        | che456      | rou789      | rec012        | ban345     |
    And Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "12 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    And Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 2000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 2000.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 2000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 2000.0 |
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1500.0            | 1500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund                        | 2000.0            | 2000.0           | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment                            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization            | 1500.0            |                  |                   |                       | false    |
      | 10 January 2026 | Discount Fee Amortization Adjustment | 34.36             |                  |                   |                       | false    |
      | 12 January 2026 | Payout Refund                        | 350.0             | 350.0            | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Discount Fee Amortization            | 13.90             |                  |                   |                       | false    |
    Then Admin closes the Working Capital loan with a full repayment on "13 January 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1500.0            | 1500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund                        | 2000.0            | 2000.0           | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment                            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization            | 1500.0            |                  |                   |                       | false    |
      | 10 January 2026 | Discount Fee Amortization Adjustment | 34.36             |                  |                   |                       | false    |
      | 12 January 2026 | Payout Refund                        | 350.0             | 350.0            | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Discount Fee Amortization            | 13.90             |                  |                   |                       | false    |
      | 13 January 2026 | Repayment                            | 1150.0            | 1150.0           | 0.0               | 0.0                   | false    |
      | 13 January 2026 | Discount Fee Amortization            | 20.46             |                  |                   |                       | false    |

  @TestRailId:TODO_09
  Scenario: Verify Working Capital Goodwill Credit trn Adjust By Delta backdated to current biz date with bigger then initial amount move it to overpaid status - UC7
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 250.0 transaction amount on Working Capital loan
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 9000.0 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit           | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 47.62             |                  |                   |                       | false    |
      | 12 January 2026 | Repayment                 | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer adjust by delta "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan with amount "4250.0" and date "14 January 2026" and payment details:
      | paymentType | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | AUTOPAY     | acc123        | che456      | rou789      | rec012        | ban345     |
    Then a Working Capital Loan Adjust Transaction business event is raised for the adjusted "goodwillCredit" with new amount "4500"
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit           | 250.0             | 250.0            | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Discount Fee Amortization | 47.62             |                  |                   |                       | false    |
      | 12 January 2026 | Repayment                 | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Discount Fee Amortization | 946.01            |                  |                   |                       | false    |
      | 14 January 2026 | Goodwill Credit           | 4500.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 14 January 2026 | Discount Fee Amortization | 6.37              |                  |                   |                       | false    |
    Then Working Capital loan transaction with type "GOODWILL_CREDIT" with date "14 January 2026" and amount "4500" has payment details:
      | paymentType | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | AUTOPAY     | acc123        | che456      | rou789      | rec012        | ban345     |
    And Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 250.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 250.0  |
      | ASSET   | 112601       | Loans Receivable         | 250.0 |        |
      | EXPENSE | 744003       | Goodwill Expense Account |       | 250.0  |
    And Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "14 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit  | Credit |
      | EXPENSE   | 744003       | Goodwill Expense Account | 4500.0 |        |
      | ASSET     | 112601       | Loans Receivable         |        | 1000.0 |
      | LIABILITY | 245000       | Other Credit Liability   |        | 3500.0 |
    And Customer makes credit balance refund on "15 January 2026" with 3500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:TODO_10
  Scenario: Verify Working Capital Repayment trn Adjust By Delta backdated to last made trn with the following CBR transaction - UC8
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 10500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment                 | 10500.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes credit balance refund on "15 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment                 | 10500.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 15 January 2026 | Credit Balance Refund     | 500.0             | 0.0              | 0.0               | 0.0                   | false    |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan with amount "-5500" and date "12 January 2026"
    Then a Working Capital Loan Adjust Transaction business event is raised for the adjusted "repayment" with new amount "5000"
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment                 | 10500.0           | 10000.0          | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
      | 12 January 2026 | Repayment                 | 5000.0            | 5000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Credit Balance Refund     | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "12 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 5000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 5000.0 |        |
    And Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit  |
      | ASSET     | 112601       | Loans Receivable          |         | 10000.0 |
      | LIABILITY | 245000       | Other Credit Liability    |         | 500.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10500.0 |         |
      | ASSET     | 112601       | Loans Receivable          | 10000.0 |         |
      | LIABILITY | 245000       | Other Credit Liability    | 500.0   |         |
      | LIABILITY | 145023       | Suspense/Clearing account |         | 10500.0 |
    When Customer adjust by delta "1"th "CREDIT_BALANCE_REFUND" transaction type made on "15 January 2026" on Working Capital loan with amount "50" and date "15 January 2026" is failed
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:TODO_11
  Scenario: Verify Working Capital Payout Refund trn Adjust By Delta within the current biz date that closes loan - UC9
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1500     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 2000.0 transaction amount on Working Capital loan
    And Customer makes repayment on "10 January 2026" with 7000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee  | 1500.0            | 1500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 2000.0            | 2000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 7000.0            | 7000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer adjust by delta "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan with amount "1500" and date "12 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1500.0            | 1500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund             | 2000.0            | 2000.0           | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment                 | 7000.0            | 7000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Discount Fee Amortization | 1465.64           |                  |                   |                       | false    |
      | 12 January 2026 | Payout Refund             | 3500.0            | 3500.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Discount Fee Amortization | 34.36             |                  |                   |                       | false    |
    And Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "12 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 3500.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 3500.0 |        |
    And Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 2000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 2000.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 2000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 2000.0 |

  @TestRailId:TODO_12
  Scenario: Verify Working Capital Repayment transaction Adjust By Delta on charged-off loan account - UC10
    Given Admin sets the business date to "28 February 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 28 February 2026 | 28 February 2026         | 9000            | 100000             | 18                | 800      |
    And Customer makes repayment on "28 February 2026" with 4580.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "01 March 2026"
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
      | 01 March 2026    | Charge-off                | 5220.0            | 5220.0           | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Discount Fee Amortization | 234.04            |                  |                   |                       | false    |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "28 February 2026" on Working Capital loan with amount "-1080.0" and date "01 March 2026"
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | true     |
      | 28 February 2026 | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
      | 01 March 2026    | Charge-off                | 9800.0            | 9800.0           | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Discount Fee Amortization | 234.04            |                  |                   |                       | false    |
      | 01 March 2026    | Repayment                 | 3500.0            | 3500.0           | 0.0               | 0.0                   | false    |
    When Customer adjust by delta "1"th "CHARGE_OFF" transaction type made on "01 March 2026" on Working Capital loan with amount "-50" and date "01 March 2026" is failed
    Then Admin closes the Working Capital loan with a full repayment on "01 March 2026"

  @TestRailId:TODO_13
  Scenario: Verify Working Capital Repayment transaction Adjust By Delta date before original transaction date - UC11
    Given Admin sets the business date to "28 February 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 28 February 2026 | 28 February 2026         | 9000            | 100000             | 18                | 800      |
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "01 March 2026" with 4580.0 transaction amount on Working Capital loan
    When Admin sets the business date to "02 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "01 March 2026" on Working Capital loan with amount "-3080.0" and date "28 February 2026"
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Repayment                 | 1500.0            | 1500.0           | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | true     |
      | 01 March 2026    | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
    Then Admin closes the Working Capital loan with a full repayment on "02 March 2026"

  @TestRailId:TODO_14
  Scenario: Verify Working Capital Repayment transaction Adjust By Delta on written-off loan account is failed - UC12
    Given Admin sets the business date to "28 February 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 28 February 2026 | 28 February 2026         | 9000            | 100000             | 18                | 500      |
    And Customer makes repayment on "28 February 2026" with 4580.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin writes off the Working Capital loan on "01 March 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee Amortization | 362.9             |                  |                   |                       | false    |
      | 01 March 2026    | Close (as written-off)    | 4920.0            | 4920.0           | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Discount Fee Amortization | 137.1             |                  |                   |                       | false    |
    When Admin sets the business date to "02 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "28 February 2026" on Working Capital loan with amount "350" and date "02 March 2026" is failed:
      | httpCode | errorMessage                                                                            |
      | 400      | Failed data validation due to: adjust.by.delta.transaction.not.allowed.for.loan.status. |
    When Customer adjust by delta "1"th "WRITE_OFF" transaction type made on "01 March 2026" on Working Capital loan with amount "50" and date "01 March 2026" is failed

  @TestRailId:TODO_15
  Scenario: Verify Working Capital Repayment transaction Adjust By Delta on already undone Repayment trn is failed - U13
    Given Admin sets the business date to "28 February 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 28 February 2026 | 28 February 2026         | 9000            | 100000             | 18                | 800      |
    And Customer makes repayment on "28 February 2026" with 4580.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "28 February 2026" on Working Capital loan with amount "-1080.0" and date "01 March 2026"
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | true     |
      | 28 February 2026 | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
      | 01 March 2026    | Repayment                 | 3500.0            | 3500.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "02 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer adjust by delta already undone "1"th "REPAYMENT" transaction made on "28 February 2026" on Working Capital loan with amount "3500" and date "02 March 2026" is failed
    Then Admin closes the Working Capital loan with a full repayment on "02 March 2026"

  @TestRailId:TODO_16
  Scenario: Verify Working Capital Repayment transaction Adjust By Delta future date of transaction is failed - UC14
    Given Admin sets the business date to "28 February 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 28 February 2026 | 28 February 2026         | 9000            | 100000             | 18                | 800      |
    And Customer makes repayment on "28 February 2026" with 4580.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "28 February 2026" on Working Capital loan with amount "-1080.0" and date "10 March 2026" is failed:
      | httpCode | errorMessage                                            |
      | 400      | Failed data validation due to: cannot.be.a.future.date. |
    Then Admin closes the Working Capital loan with a full repayment on "01 March 2026"

  @TestRailId:TODO_17
  Scenario: Verify Working Capital Repayment transaction adjust by delta with date before disbursement date - UC15
    Given Admin sets the business date to "28 February 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 28 February 2026 | 28 February 2026         | 9000            | 100000             | 18                | 800      |
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "01 March 2026" with 4580.0 transaction amount on Working Capital loan
    When Admin sets the business date to "02 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "01 March 2026" on Working Capital loan with amount "-3080" and date "25 February 2026" is failed:
      | httpCode | errorMessage                                                    |
      | 400      | Failed data validation due to: cannot.be.before.disbursal.date. |
    Then Admin closes the Working Capital loan with a full repayment on "01 March 2026"

  @TestRailId:TODO_18
  Scenario: Verify Working Capital transaction Adjust by delta should be disallowed for not supported transaction types(complex scenario) - UC16
    Given Admin sets the business date to "28 February 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 28 February 2026 | 28 February 2026         | 9000            | 100000             | 18                | 800      |
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "01 March 2026" with 4580.0 transaction amount on Working Capital loan
    When Admin sets the business date to "02 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "200" amount on transaction date "02 March 2026" on Working Capital loan account for last discount added on disbursement
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
      | 02 March 2026    | Discount Fee Adjustment   | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
    When Customer adjust by delta "1"th "DISCOUNT_FEE" transaction type made on "28 February 2026" on Working Capital loan with amount "50" and date "28 February 2026" is failed
    When Customer adjust by delta "1"th "DISCOUNT_FEE_ADJUSTMENT" transaction type made on "02 March 2026" on Working Capital loan with amount "50" and date "02 March 2026" is failed
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "02 March 2026" due date and 100.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date      | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 02 March 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Admin waives the last added charge on working capital loan
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee              | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Repayment                 | 4580.0            | 4580.0           | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Discount Fee Amortization | 565.96            |                  |                   |                       | false    |
      | 02 March 2026    | Discount Fee Adjustment   | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
      | 02 March 2026    | Waive loan charges        | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Customer adjust by delta "1"th "WAIVE_CHARGES" transaction type made on "02 March 2026" on Working Capital loan with amount "50" and date "02 March 2026" is failed
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer adjust by delta "1"th "REPAYMENT" transaction made on "01 March 2026" on Working Capital loan with amount "8920.0" and date "03 March 2026"
    Then Working Capital loan status will be "OVERPAID"
    When Customer makes credit balance refund on "03 March 2026" with 3900.0 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate  | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 February 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 February 2026 | Discount Fee                         | 800.0             | 800.0            | 0.0               | 0.0                   | false    |
      | 01 March 2026    | Repayment                            | 4580.0            | 4580.0           | 0.0               | 0.0                   | true     |
      | 01 March 2026    | Discount Fee Amortization            | 565.96            |                  |                   |                       | false    |
      | 02 March 2026    | Discount Fee Adjustment              | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
      | 02 March 2026    | Waive loan charges                   | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 02 March 2026    | Discount Fee Amortization Adjustment | 134.19            |                  |                   |                       | false    |
      | 03 March 2026    | Repayment                            | 13500.0           | 9600.0           | 0.0               | 0.0                   | false    |
      | 03 March 2026    | Discount Fee Amortization            | 168.23            |                  |                   |                       | false    |
      | 03 March 2026    | Credit Balance Refund                | 3900.0            | 0.0              | 0.0               | 0.0                   | false    |
    When Customer adjust by delta "1"th "CREDIT_BALANCE_REFUND" transaction type made on "03 March 2026" on Working Capital loan with amount "50" and date "03 March 2026" is failed
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:TODO_19
  Scenario: Verify working capital loan goodwill credit adjust by delta with new amount creates replacement - UC17
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
    When Customer adjust by delta "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan with amount "-120."
    Then a Working Capital Loan Adjust Transaction business event is raised for the adjusted "goodwillCredit" with new amount "150"
    And Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
      | ASSET   | 112601       | Loans Receivable         | 270.0 |        |
      | EXPENSE | 744003       | Goodwill Expense Account |       | 270.0  |
    And Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 150.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 150.0  |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8850.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 150.00  |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit | 270.0             | 270.0            | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Goodwill Credit | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:TODO_20
  Scenario: Verify working capital loan payout refund adjust by delta with new amount creates replacement - UC18
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
    When Customer adjust by delta "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan with amount "-120.0"
    Then a Working Capital Loan Adjust Transaction business event is raised for the adjusted "payoutRefund" with new amount "150"
    And Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 270.0  |
      | ASSET     | 112601       | Loans Receivable          | 270.0 |        |
    And Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8850.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 150.00  |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 270.0            | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Payout Refund | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:TODO_21
  Scenario: Verify working capital loan charge adjustment transaction adjustment by delta with new amount creates replacement - UC19
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    When Admin makes a charge adjustment for the last added charge with 100.0 amount on working capital loan
    Then Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 100.0  |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
    When Customer adjust by delta "1"th "CHARGE_ADJUSTMENT" transaction made on "10 January 2026" on Working Capital loan with amount "-60"
    Then a Working Capital Loan Adjust Transaction business event is raised for the adjusted "chargeAdjustment" with new amount "40"
    And Working Capital Loan Transactions tab has a reversed "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 100.0 |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 100.0  |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
    And Working Capital Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | INCOME | 404007       | Fee Income              | 40.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 40.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment | 100.0             | 0.0              | 100.0             | 0.0                   | true     |
      | 10 January 2026 | Charge Adjustment | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 60.0            | 40.0     | 0.0            | 0.0                 | 0.0          |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:TODO_22
  Scenario: Verify working capital loan charge adjustment transaction adjustment by delta while having waive charge disallowed with higher amount - UC20
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
      | 10 January 2026 | Waive loan charges | 60.0              | 0.0              | 60.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual            | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404007       | Fee Income              |       | 40.0   |
# --- adjust Charge Adjustment with higher then allowed amount should fail --- #
    When Customer adjust by delta "1"th "CHARGE_ADJUSTMENT" transaction made on "10 January 2026" on Working Capital loan with amount "30.0" and date "11 January 2026" is failed:
      | httpCode | errorMessage                                                                                     |
      | 403      | "Transaction amount cannot be higher than the available charge amount for adjustment: 40.000000" |
# --- adjust Charge Adjustment with lower then allowed amount should success --- #
    When Customer adjust by delta "1"th "CHARGE_ADJUSTMENT" transaction made on "10 January 2026" on Working Capital loan with amount "-10" and date "11 January 2026"
    Then Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment  | 40.0              | 0.0              | 40.0              | 0.0                   | true     |
      | 10 January 2026 | Waive loan charges | 60.0              | 0.0              | 60.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual            | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 11 January 2026 | Charge Adjustment  | 30.0              | 0.0              | 30.0              | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "11 January 2026"
