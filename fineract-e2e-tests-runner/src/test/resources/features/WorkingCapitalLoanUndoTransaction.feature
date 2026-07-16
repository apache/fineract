@WorkingCapital
@WorkingCapitalLoanUndoTransactionFeature
Feature: Working Capital Loan Undo Transaction

  @TestRailId:C85331
  Scenario: Verify working capital loan undo transaction - UC1: simple repayment undo
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ACC_DEF_REV_AM | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Delinquency: 9 days * 30 EUR/day = 270 EUR outstanding, not yet paid
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    # Make a repayment covering all outstanding amount; verify GL entries
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
    # Verify amortization schedule reflects repayment: balance reduced to 8730 from 10 Jan
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        | 270.0               | 0.0                      | 0.0                        | 8730.00       | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8730.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 270.00  |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    # Undo the repayment: transaction is reversed, mirror GL entries are created
    When Customer undo "1"th working capital transaction made on "10 January 2026"
    # Verify reversed transaction and its mirror GL entries (original + reversal rows)
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 270.0  |
      | ASSET     | 112601       | Loans Receivable          | 270.0 |        |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 9000.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 0.00    |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  @TestRailId:C85332
  Scenario: Verify working capital loan undo transaction - UC2: repayment undo updates breach schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        | 270.0               | 0.0                      | 0.0                        | 8730.00       | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8730.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 270.00  |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 0.0               | null       | false  |
    # Undo the repayment
    When Customer undo "1"th working capital transaction made on "10 January 2026"
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 9000.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 0.00    |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |

  @TestRailId:C85333
  Scenario: Verify working capital loan undo transaction - UC3: goodwill credit undo
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    # Make a goodwill credit transaction; verify GL entries (expense debit, loans receivable credit)
    When Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        | 270.0               | 0.0                      | 0.0                        | 8730.00       | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 8730.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 270.00  |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    # Undo the goodwill credit: transaction is reversed, mirror GL entries created
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
      | ASSET   | 112601       | Loans Receivable         | 270.0 |        |
      | EXPENSE | 744003       | Goodwill Expense Account |       | 270.0  |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 9000.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 0.00    |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  @TestRailId:C85334
  Scenario: Verify working capital loan undo transaction - UC4: undo already-reversed transaction returns error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    # First undo succeeds; verify the transaction is now reversed with mirror GL entries
    And Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 270.0  |
      | ASSET     | 112601       | Loans Receivable          | 270.0 |        |
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 9000.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 0.00    |
    # Second undo on the same (already reversed) transaction must fail with 403
    When Customer tries to undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan and gets error:
      | httpCode | errorMessage                                              |
      | 400      | Failed data validation due to: transaction.already.undone |

  @TestRailId:C85335
  Scenario: Verify working capital loan undo transaction - UC5: undo disbursement via undo-transaction endpoint returns error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 9000.00 |
      | overpaymentAmount    | 0.00    |
      | totalPaidPrincipal   | 0.00    |
    # Attempting to undo a DISBURSEMENT via the generic undo-transaction endpoint must be rejected
    When Customer tries to undo "1"th "DISBURSEMENT" transaction made on "01 January 2026" on Working Capital loan and gets error:
      | httpCode | errorMessage                                            |
      | 400      | Undo is not supported for transaction type DISBURSEMENT |


  @TestRailId:C85454
  Scenario: Verify undo repayment after a credit balance refund does not resurrect the refunded overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # An active charge is required so the undo triggers the reprocessing replay
    When Admin sets the business date to "05 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "06 January 2026"
    And Customer makes repayment on "06 January 2026" with 100 transaction amount on Working Capital loan
    When Admin sets the business date to "07 January 2026"
    And Customer makes repayment on "07 January 2026" with 9300 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance payload contains the following fields:
      | field             | value |
      | overpaymentAmount | 300.0 |
    # Refund 100 of the 300 overpayment
    When Admin sets the business date to "08 January 2026"
    And Customer makes credit balance refund on "08 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan balance payload contains the following fields:
      | field             | value |
      | overpaymentAmount | 200.0 |
    # Undo the fee-settling day-6 repayment. Replaying the remaining day-7 repayment yields fee 100 + principal
    # 9000 + overpayment 200; the 100 already refunded must stay subtracted: overpayment = 100, not 200.
    When Admin sets the business date to "09 January 2026"
    And Customer undo "1"th "REPAYMENT" transaction made on "06 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Repayment             | 100.0             | 0.0              | 100.0             | 0.0                   | true     |
      | 07 January 2026 | Repayment             | 9300.0            | 9000.0           | 100.0             | 0.0                   | false    |
      | 08 January 2026 | Credit Balance Refund | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | totalPaidPrincipal   | 9000.0 |
      | overpaymentAmount    | 100.0  |

  @TestRailId:C85455
  Scenario: Verify undo of an early repayment on an overpaid charge-free loan realigns the remaining allocation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 3000 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 6500 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | totalPaidPrincipal   | 9000.0 |
      | overpaymentAmount    | 500.0  |
    # Undo the day-5 repayment: the remaining day-10 repayment alone covers 6500 principal (no overpayment)
    When Admin sets the business date to "12 January 2026"
    And Customer undo "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 2500.0 |
      | totalPaidPrincipal   | 6500.0 |
      | overpaymentAmount    | 0.0    |
    # The allocation row of the remaining repayment must match the corrected balance (6500, not 6000)
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment    | 6500.0            | 6500.0           | 0.0               | 0.0                   | false    |
    # The amortization schedule must record the corrected 6500 principal payment on day 10
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details in first "11" lines:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 0.0                        | 9000.00       | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 3         | 2026-01-04 | 50.00                 | 8850.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 4         | 2026-01-05 | 50.00                 | 8800.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 5         | 2026-01-06 | 50.00                 | 8750.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 6         | 2026-01-07 | 50.00                 | 8700.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 7         | 2026-01-08 | 50.00                 | 8650.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.0                        | 0.0                 | 0.0                      | 0.0                        | 9000.00       | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.0                        | 6500.0              | 0.0                      | 0.0                        | 2500.00       | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.0                        |                     |                          | 0.0                        |               |                          |

  @TestRailId:C85457
  Scenario: Verify a new repayment on a closed working capital loan is accepted and moves the loan to overpaid
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 9000 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    # A repayment on a closed loan is accepted; the full amount becomes overpayment
    When Admin sets the business date to "06 January 2026"
    And Customer makes repayment on "06 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | totalPaidPrincipal   | 9000.0 |
      | overpaymentAmount    | 100.0  |

  @TestRailId:C85458
  Scenario: Verify undo repayment posts a discount fee amortization adjustment on the next COB
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin successfully add discount with "1000" amount on Working Capital loan account
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 3000 transaction amount on Working Capital loan
    # COB recognizes part of the discount as income via an amortization transaction
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | false    |
      | 02 January 2026 | Repayment                 | 3000.0            | false    |
      | 02 January 2026 | Discount Fee Amortization | 498.67            | false    |
    # Undo the repayment; the next COB re-evaluates and posts an amortization adjustment returning income to zero
    When Customer undo "1"th "REPAYMENT" transaction made on "02 January 2026" on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | false    |
      | 02 January 2026 | Repayment                            | 3000.0            | true     |
      | 02 January 2026 | Discount Fee Amortization            | 498.67            | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment | 498.67            | false    |
    And Working Capital loan balance payload contains the following fields:
      | field          | value |
      | realizedIncome | 0.0   |

  @TestRailId:C85459
  Scenario: Verify undoing the closing repayment re-opens the loan from closed to active
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 9000 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "06 January 2026"
    And Customer undo "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 9000.0 |
      | totalPaidPrincipal   | 0.0    |
      | overpaymentAmount    | 0.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |

  @TestRailId:C85487
  Scenario: Verify undo of an earlier repayment recomputes the principal split of a later overpaying repayment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 25 PERCENTAGE
    When Admin creates a new Working Capital Loan Product with delinquency bucket
    When Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 800             | 8000               | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "30 January 2026" with 700 transaction amount on Working Capital loan
    When Admin sets the business date to "28 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "28 February 2026" with 200 transaction amount on Working Capital loan
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 700        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 100            | 200        | 0                 | true                  |
    When Customer undo "1"th working capital transaction made on "30 January 2026"
    Then WC loan delinquency range schedule periods have specific data:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 200            | 200        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 200            | 0          | 200               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "28 February 2026"
