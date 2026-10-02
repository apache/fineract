@WorkingCapital
@WorkingCapitalLoanAdjustByDeltaTransactionFeature
Feature: Working Capital Loan Adjust By Delta Transaction - Adjust

  @TestRailId:TODO_ADD_01
  Scenario: Verify working capital loan undo transaction - simple repayment adjust by delta
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

    And Customer makes repayment on "10 January 2026" with 200.0 transaction amount on Working Capital loan
    When Customer adjust by delta "1"th working capital transaction made on "10 January 2026" by "70.0"

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
